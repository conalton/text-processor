package org.conalton.textprocessor.domain.service.storage;

import org.springframework.stereotype.Component;

@Component
public class StorageLocationResolver {
  private final StorageProperties storageProperties;

  public StorageLocationResolver(StorageProperties storageProperties) {
    this.storageProperties = storageProperties;
  }

  public String resolveStorageBucket(StorageLocation location) {
    return switch (location) {
      case TASKS -> storageProperties.getTasksBucketName();
    };
  }
}
