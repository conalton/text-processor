package org.conalton.textprocessor.domain.storage.port;

import org.conalton.textprocessor.domain.storage.types.PresignedUrlData;
import org.conalton.textprocessor.domain.storage.types.StorageLocation;

public interface FileStoragePort {
  PresignedUrlData generatePresignedUploadUrl(StorageLocation location, String uploadPath);

  void copy(StorageLocation locFrom, String from, StorageLocation locTo, String to);

  void delete(StorageLocation loc, String path);
}
