package org.conalton.textprocessor.infrastructure.aws.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "cloud.aws")
@Validated
public class AwsProperties {
  @NotBlank private String accessKey;

  @NotBlank private String secretKey;

  @NotBlank private String region;

  private String endpoint = "";

  public boolean hasEndpointOverride() {
    return endpoint != null && !endpoint.isBlank();
  }

  public String getAccessKey() {
    return accessKey;
  }

  public void setAccessKey(String accessKey) {
    this.accessKey = accessKey;
  }

  public String getSecretKey() {
    return secretKey;
  }

  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }
}
