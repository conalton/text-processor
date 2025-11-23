package org.conalton.textprocessor.infrastructure.aws.sqs;

import io.awspring.cloud.sqs.listener.MessageListener;
import io.awspring.cloud.sqs.listener.SqsMessageListenerContainer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.conalton.textprocessor.domain.messaging.interfaces.MessageHandler;
import org.conalton.textprocessor.domain.messaging.port.MessageSubscriptionPort;
import org.conalton.textprocessor.domain.messaging.types.MessageEnvelope;
import org.conalton.textprocessor.infrastructure.aws.properties.AwsSqsProperties;
import org.springframework.context.SmartLifecycle;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

public class SqsMessageSubscriptionAdapter implements MessageSubscriptionPort, SmartLifecycle {

  private final SqsAsyncClient sqsAsyncClient;
  private final AwsSqsProperties sqsProperties;
  private final List<SqsMessageListenerContainer<String>> containers = new CopyOnWriteArrayList<>();
  private volatile boolean running;

  public SqsMessageSubscriptionAdapter(
      SqsAsyncClient sqsAsyncClient, AwsSqsProperties sqsProperties) {
    this.sqsAsyncClient = sqsAsyncClient;
    this.sqsProperties = sqsProperties;
  }

  @Override
  public void subscribe(String queueName, MessageHandler handler) {
    Assert.hasText(queueName, "Queue name must not be empty");

    SqsMessageListenerContainer<String> container = buildContainer(queueName, handler);
    containers.add(container);

    if (running) {
      container.start();
    }
  }

  @Override
  public void start() {
    containers.forEach(SqsMessageListenerContainer::start);
    running = true;
  }

  @Override
  public void stop() {
    containers.forEach(SqsMessageListenerContainer::stop);
    running = false;
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  private SqsMessageListenerContainer<String> buildContainer(
      String queueName, MessageHandler handler) {
    return SqsMessageListenerContainer.<String>builder()
        .sqsAsyncClient(sqsAsyncClient)
        .queueNames(queueName)
        .messageListener(new SqsMessageDelegate(handler))
        .configure(
            options ->
                options
                    .maxConcurrentMessages(sqsProperties.getMaxConcurrentMessages())
                    .maxMessagesPerPoll(sqsProperties.getMaxMessagesPerPoll())
                    .maxDelayBetweenPolls(sqsProperties.getMaxDelayBetweenPolls()))
        .build();
  }

  private static class SqsMessageDelegate implements MessageListener<String> {

    private final MessageHandler handler;

    SqsMessageDelegate(MessageHandler handler) {
      this.handler = handler;
    }

    @Override
    public void onMessage(Message<String> message) {
      handler.handle(new MessageEnvelope(message.getPayload(), message.getHeaders()));
    }
  }
}
