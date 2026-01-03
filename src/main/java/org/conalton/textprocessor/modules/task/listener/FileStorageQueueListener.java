package org.conalton.textprocessor.modules.task.listener;

import java.util.List;
import org.conalton.textprocessor.domain.messaging.interfaces.MessageHandler;
import org.conalton.textprocessor.domain.messaging.port.FileStorageEventListenerPort;
import org.conalton.textprocessor.domain.messaging.port.MessageSubscriptionPort;
import org.conalton.textprocessor.domain.messaging.types.MessageEnvelope;
import org.conalton.textprocessor.domain.storage.types.FileStorageItem;
import org.conalton.textprocessor.modules.task.config.MessagingConfig;
import org.conalton.textprocessor.modules.task.service.TaskPresignedFilesProcessorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties({MessagingConfig.class})
@ConditionalOnProperty(prefix = "cloud.messaging", name = "enabled", havingValue = "true")
public class FileStorageQueueListener implements MessageHandler {
  private static final Logger log = LoggerFactory.getLogger(FileStorageQueueListener.class);
  private final MessageSubscriptionPort subscriptionPort;
  private final String fileUploadedQueueName;
  private final FileStorageEventListenerPort fileStorageEventListenerPort;
  private final TaskPresignedFilesProcessorService fileStorageService;

  public FileStorageQueueListener(
      MessageSubscriptionPort subscriptionPort,
      MessagingConfig messagingConfig,
      FileStorageEventListenerPort fileStorageEventListenerPort,
      TaskPresignedFilesProcessorService fileStorageService) {
    this.subscriptionPort = subscriptionPort;
    this.fileUploadedQueueName = messagingConfig.getFileUploadedQueueName();
    this.fileStorageEventListenerPort = fileStorageEventListenerPort;
    this.fileStorageService = fileStorageService;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void start() {
    log.info(
        "Starting listening to file storage upload events on queue: {}", fileUploadedQueueName);
    subscriptionPort.subscribe(fileUploadedQueueName, this);
  }

  /** When a file is uploaded to file storage using presigned URL */
  @Override
  public void handle(MessageEnvelope envelope) {
    if (envelope.payload() == null || envelope.payload().isBlank()) {
      log.warn("Received empty payload in file storage event");
      return;
    }

    List<FileStorageItem> files = fileStorageEventListenerPort.onJsonPayload(envelope.payload());

    if (files == null || files.isEmpty()) {
      log.warn("No valid file storage items found in payload");
      return;
    }

    fileStorageService.processFilesUploads(files);
  }
}
