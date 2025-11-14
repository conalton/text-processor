package org.conalton.textprocessor.infrastructure.aws.config;

import java.net.URI;
import org.conalton.textprocessor.domain.service.storage.FileStoragePort;
import org.conalton.textprocessor.domain.service.storage.StorageLocationResolver;
import org.conalton.textprocessor.domain.service.storage.StorageProperties;
import org.conalton.textprocessor.infrastructure.aws.properties.AwsProperties;
import org.conalton.textprocessor.infrastructure.aws.properties.AwsS3Properties;
import org.conalton.textprocessor.infrastructure.aws.s3.S3FileStorageAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@EnableConfigurationProperties({AwsProperties.class, AwsS3Properties.class})
@ConditionalOnProperty(
    prefix = "cloud.aws",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
public class AwsConfig {

  @Bean(destroyMethod = "close")
  public S3Presigner s3Presigner(AwsProperties awsProperties, AwsS3Properties s3Properties) {

    var builder =
        S3Presigner.builder()
            .region(Region.of(awsProperties.getRegion()))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                        awsProperties.getAccessKey(), awsProperties.getSecretKey())));

    if (awsProperties.hasEndpointOverride()) {
      builder
          .endpointOverride(URI.create(awsProperties.getEndpoint()))
          .serviceConfiguration(
              S3Configuration.builder()
                  .pathStyleAccessEnabled(s3Properties.isPathStyleAccessEnabled())
                  .build());
    }

    return builder.build();
  }

  @Bean
  public FileStoragePort s3FileStorageAdapter(
      S3Presigner s3Presigner, StorageProperties storageProps, StorageLocationResolver resolver) {
    return new S3FileStorageAdapter(s3Presigner, storageProps, resolver);
  }
}
