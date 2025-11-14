package org.conalton.textprocessor.domain.service.storage;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "cloud.storage")
@Validated
public class StorageProperties {
  @NotBlank private String tasksBucketName;

  @Min(1)
  @Max(60)
  private int presignedUrlExpirationMinutes;

  public String getTasksBucketName() {
    return tasksBucketName;
  }

  public void setTasksBucketName(String tasksBucketName) {
    this.tasksBucketName = tasksBucketName;
  }

  public int getPresignedUrlExpirationMinutes() {
    return presignedUrlExpirationMinutes;
  }

  public void setPresignedUrlExpirationMinutes(int presignedUrlExpirationMinutes) {
    this.presignedUrlExpirationMinutes = presignedUrlExpirationMinutes;
  }
}
