package org.conalton.textprocessor.infrastructure.aws.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "cloud.aws.s3")
public class AwsS3Properties {
  @NotBlank private String publicEndpoint;

  private boolean pathStyleAccessEnabled;

  public String getPublicEndpoint() {
    return publicEndpoint;
  }

  public void setPublicEndpoint(String publicEndpoint) {
    this.publicEndpoint = publicEndpoint;
  }

  public boolean isPathStyleAccessEnabled() {
    return pathStyleAccessEnabled;
  }

  public void setPathStyleAccessEnabled(boolean pathStyleAccessEnabled) {
    this.pathStyleAccessEnabled = pathStyleAccessEnabled;
  }
}
