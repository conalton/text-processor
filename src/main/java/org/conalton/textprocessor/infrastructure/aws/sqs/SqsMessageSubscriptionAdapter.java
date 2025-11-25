package org.conalton.textprocessor.infrastructure.aws.sqs;

import io.awspring.cloud.sqs.listener.MessageListener;
import io.awspring.cloud.sqs.listener.SqsMessageListenerContainer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.conalton.textprocessor.domain.messaging.interfaces.MessageHandler;
import org.conalton.textprocessor.domain.messaging.port.MessageSubscriptionPort;
import org.conalton.textprocessor.domain.messaging.types.MessageEnvelope;
import org.conalton.textprocessor.infrastructure.aws.properties.AwsSqsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

public class SqsMessageSubscriptionAdapter implements MessageSubscriptionPort, SmartLifecycle {

  private static final Logger log = LoggerFactory.getLogger(SqsMessageSubscriptionAdapter.class);

  private final SqsAsyncClient sqsAsyncClient;
  private final AwsSqsProperties sqsProperties;
  private final List<SqsMessageListenerContainer<String>> containers = new ArrayList<>();
  private final Lock lifecycleLock = new ReentrantLock();
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
    boolean shouldStart;

    lifecycleLock.lock();

    try {
      containers.add(container);
      shouldStart = running;
    } finally {
      lifecycleLock.unlock();
    }

    if (shouldStart) {
      try {
        container.start();
      } catch (Exception ex) {
        removeContainer(container);
        throw new IllegalStateException(
            "Failed to start SQS listener container for queue " + queueName, ex);
      }
    }
  }

  @Override
  public void start() {
    List<SqsMessageListenerContainer<String>> snapshot;

    lifecycleLock.lock();

    try {
      if (running) {
        return;
      }

      running = true;
      snapshot = new ArrayList<>(containers);
    } finally {
      lifecycleLock.unlock();
    }

    List<SqsMessageListenerContainer<String>> started = new ArrayList<>();
    try {
      for (SqsMessageListenerContainer<String> container : snapshot) {
        container.start();
        started.add(container);
      }
    } catch (Exception ex) {
      log.error("Failed to start one of the SQS listener containers. Rolling back.", ex);

      started.forEach(this::stopQuietly);

      lifecycleLock.lock();

      try {
        running = false;
      } finally {
        lifecycleLock.unlock();
      }

      throw new IllegalStateException("Failed to start SQS message subscription adapter", ex);
    }
  }

  @Override
  public void stop() {
    List<SqsMessageListenerContainer<String>> snapshot;

    lifecycleLock.lock();

    try {
      if (!running) {
        return;
      }

      running = false;
      snapshot = new ArrayList<>(containers);
    } finally {
      lifecycleLock.unlock();
    }

    snapshot.forEach(this::stopQuietly);
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  private void removeContainer(SqsMessageListenerContainer<String> container) {
    lifecycleLock.lock();

    try {
      containers.remove(container);
    } finally {
      lifecycleLock.unlock();
    }
  }

  private void stopQuietly(SqsMessageListenerContainer<String> container) {
    try {
      container.stop();
    } catch (Exception ex) {
      log.error(
          "Failed to stop SQS listener container for queues: {}", container.getQueueNames(), ex);
    }
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
