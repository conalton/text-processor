package org.conalton.textprocessor.modules.task.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "cloud.messaging")
public class MessagingConfig {
  @NotBlank private String fileUploadedQueueName;

  public String getFileUploadedQueueName() {
    return fileUploadedQueueName;
  }

  public void setFileUploadedQueueName(String value) {
    fileUploadedQueueName = value;
  }
}
