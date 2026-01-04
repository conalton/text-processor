package org.conalton.textprocessor.infrastructure.aws.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import org.conalton.textprocessor.domain.messaging.port.FileStorageEventListenerPort;
import org.conalton.textprocessor.domain.messaging.port.MessageSubscriptionPort;
import org.conalton.textprocessor.domain.storage.config.StorageProperties;
import org.conalton.textprocessor.domain.storage.port.FileStoragePort;
import org.conalton.textprocessor.domain.storage.service.StorageLocationResolver;
import org.conalton.textprocessor.infrastructure.aws.properties.AwsProperties;
import org.conalton.textprocessor.infrastructure.aws.properties.AwsS3Properties;
import org.conalton.textprocessor.infrastructure.aws.properties.AwsSqsProperties;
import org.conalton.textprocessor.infrastructure.aws.s3.S3FileStorageAdapter;
import org.conalton.textprocessor.infrastructure.aws.sqs.S3EventListenerAdapter;
import org.conalton.textprocessor.infrastructure.aws.sqs.SqsMessageSubscriptionAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Configuration
@EnableConfigurationProperties({AwsProperties.class, AwsS3Properties.class, AwsSqsProperties.class})
@ConditionalOnProperty(prefix = "cloud.aws", name = "enabled", havingValue = "true")
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
  public SqsAsyncClient sqsAsyncClient(AwsProperties awsProperties) {
    var builder =
        SqsAsyncClient.builder()
            .region(Region.of(awsProperties.getRegion()))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                        awsProperties.getAccessKey(), awsProperties.getSecretKey())));

    if (awsProperties.hasEndpointOverride()) {
      builder.endpointOverride(URI.create(awsProperties.getEndpoint()));
    }

    return builder.build();
  }

  @Bean
  public FileStoragePort s3FileStorageAdapter(
      S3Presigner s3Presigner,
      StorageProperties storageProps,
      StorageLocationResolver resolver,
      S3Client s3Client) {
    return new S3FileStorageAdapter(s3Presigner, storageProps, resolver, s3Client);
  }

  @Bean
  public MessageSubscriptionPort messageSubscriptionPort(
      SqsAsyncClient sqsAsyncClient, AwsSqsProperties sqsProperties) {
    return new SqsMessageSubscriptionAdapter(sqsAsyncClient, sqsProperties);
  }

  @Bean
  public FileStorageEventListenerPort fileStorageEventListenerPort(ObjectMapper objectMapper) {
    return new S3EventListenerAdapter(objectMapper);
  }

  @Bean(destroyMethod = "close")
  public S3Client s3Client(AwsProperties awsProperties, AwsS3Properties s3Properties) {
    var builder =
        S3Client.builder()
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
}
