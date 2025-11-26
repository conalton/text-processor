package org.conalton.textprocessor.infrastructure.aws.sqs;

import io.awspring.cloud.sqs.listener.SqsMessageListenerContainer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.conalton.textprocessor.domain.messaging.interfaces.MessageHandler;
import org.conalton.textprocessor.domain.messaging.port.MessageSubscriptionPort;
import org.conalton.textprocessor.domain.messaging.types.MessageEnvelope;
import org.conalton.textprocessor.infrastructure.aws.properties.AwsSqsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

public class SqsMessageSubscriptionAdapter implements MessageSubscriptionPort, DisposableBean {

  private static final Logger log = LoggerFactory.getLogger(SqsMessageSubscriptionAdapter.class);

  private final SqsAsyncClient sqsAsyncClient;
  private final AwsSqsProperties sqsProperties;
  private final List<SqsMessageListenerContainer<String>> containers = new CopyOnWriteArrayList<>();

  public SqsMessageSubscriptionAdapter(
      SqsAsyncClient sqsAsyncClient, AwsSqsProperties sqsProperties) {
    this.sqsAsyncClient = sqsAsyncClient;
    this.sqsProperties = sqsProperties;
  }

  @Override
  public void subscribe(String queueName, MessageHandler handler) {
    SqsMessageListenerContainer<String> container = buildContainer(queueName, handler);

    try {
      container.start();
      containers.add(container);
      log.info("Subscribed to SQS queue: {}", queueName);
    } catch (Exception ex) {
      try {
        container.stop();
      } catch (Exception stopEx) {
        log.warn("Failed to stop container after failed start", stopEx);
      }

      throw new IllegalStateException("Failed to start container for queue " + queueName, ex);
    }
  }

  @Override
  public void destroy() {
    containers.forEach(
        container -> {
          try {
            container.stop();
          } catch (Exception ex) {
            log.warn("Failed to stop container for queues: {}", container.getQueueNames(), ex);
          }
        });

    containers.clear();
  }

  protected SqsMessageListenerContainer<String> buildContainer(
      String queueName, MessageHandler handler) {
    return SqsMessageListenerContainer.<String>builder()
        .sqsAsyncClient(sqsAsyncClient)
        .queueNames(queueName)
        .messageListener(
            message ->
                handler.handle(new MessageEnvelope(message.getPayload(), message.getHeaders())))
        .configure(
            options ->
                options
                    .maxConcurrentMessages(sqsProperties.getMaxConcurrentMessages())
                    .maxMessagesPerPoll(sqsProperties.getMaxMessagesPerPoll())
                    .maxDelayBetweenPolls(sqsProperties.getMaxDelayBetweenPolls()))
        .build();
  }
}
