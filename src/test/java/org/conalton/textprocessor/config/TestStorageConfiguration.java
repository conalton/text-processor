package org.conalton.textprocessor.config;

import org.conalton.textprocessor.domain.storage.config.StorageProperties;
import org.conalton.textprocessor.domain.storage.port.FileStoragePort;
import org.conalton.textprocessor.domain.storage.service.StorageLocationResolver;
import org.conalton.textprocessor.domain.storage.types.PresignedUrlData;
import org.conalton.textprocessor.domain.storage.types.StorageLocation;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
public class TestStorageConfiguration {

  @Bean
  @Primary
  FileStoragePort fileStoragePortStub(
      StorageProperties storageProps, StorageLocationResolver resolver) {

    return (StorageLocation location, String uploadPath) -> {
      String bucketName = resolver.resolveStorageBucket(location);

      String fakeUrl =
          "https://stub.local/"
              + bucketName
              + "/"
              + uploadPath
              + "?expires="
              + storageProps.getPresignedUrlExpirationMinutes();

      return new PresignedUrlData(fakeUrl, uploadPath);
    };
  }
}
