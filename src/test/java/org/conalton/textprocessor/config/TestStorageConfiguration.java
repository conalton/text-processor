package org.conalton.textprocessor.config;

import org.conalton.textprocessor.domain.service.storage.FileStoragePort;
import org.conalton.textprocessor.domain.service.storage.StorageLocation;
import org.conalton.textprocessor.domain.service.storage.StorageLocationResolver;
import org.conalton.textprocessor.domain.service.storage.StorageProperties;
import org.conalton.textprocessor.dto.internal.PresignedUrlData;
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
