package org.conalton.textprocessor.domain.storage.port;

import org.conalton.textprocessor.domain.storage.types.PresignedUrlData;
import org.conalton.textprocessor.domain.storage.types.StorageLocation;

public interface FileStoragePort {
  PresignedUrlData generatePresignedUploadUrl(StorageLocation location, String uploadPath);
}
