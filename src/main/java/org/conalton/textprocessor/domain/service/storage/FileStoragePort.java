package org.conalton.textprocessor.domain.service.storage;

import org.conalton.textprocessor.dto.internal.PresignedUrlData;

public interface FileStoragePort {
  PresignedUrlData generatePresignedUploadUrl(StorageLocation location, String uploadPath);
}
