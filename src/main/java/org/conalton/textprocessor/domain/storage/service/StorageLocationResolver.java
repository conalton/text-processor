package org.conalton.textprocessor.domain.storage.service;

import org.conalton.textprocessor.domain.storage.config.StorageProperties;
import org.conalton.textprocessor.domain.storage.types.StorageLocation;
import org.springframework.stereotype.Component;

@Component
public class StorageLocationResolver {
  private final StorageProperties storageProperties;

  public StorageLocationResolver(StorageProperties storageProperties) {
    this.storageProperties = storageProperties;
  }

  public String resolveStorageBucket(StorageLocation location) {
    return switch (location) {
      case TASKS_PRESIGNED_UPLOADS, TASKS_UPLOADED_FILES -> storageProperties.getTasksBucketName();
    };
  }
}
