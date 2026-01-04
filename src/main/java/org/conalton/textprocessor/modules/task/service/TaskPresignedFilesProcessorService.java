package org.conalton.textprocessor.modules.task.service;

import java.util.*;
import org.conalton.textprocessor.domain.storage.service.StorageLocationResolver;
import org.conalton.textprocessor.domain.storage.service.StoragePathNormalizer;
import org.conalton.textprocessor.domain.storage.types.FileStorageItem;
import org.conalton.textprocessor.domain.storage.types.StorageLocation;
import org.springframework.stereotype.Service;

@Service
public class TaskPresignedFilesProcessorService {
  private final StorageLocationResolver storageLocationResolver;
  private final TaskStatusUpdaterService taskStatusUpdaterService;
  private final StoragePathNormalizer storagePathNormalizer;

  public TaskPresignedFilesProcessorService(
      StorageLocationResolver storageLocationResolver,
      TaskStatusUpdaterService taskStatusUpdaterService,
      StoragePathNormalizer storagePathNormalizer) {
    this.storageLocationResolver = storageLocationResolver;
    this.taskStatusUpdaterService = taskStatusUpdaterService;
    this.storagePathNormalizer = storagePathNormalizer;
  }

  public void processFilesUploads(List<FileStorageItem> files) {
    String bucket =
        storageLocationResolver.resolveStorageBucket(StorageLocation.TASKS_PRESIGNED_UPLOADS);
    String prefix = StorageLocation.TASKS_PRESIGNED_UPLOADS.getUploadPrefix();

    List<String> paths =
        files.stream()
            .filter(Objects::nonNull)
            .filter(item -> bucket.equals(item.location()))
            .map(FileStorageItem::uploadPath)
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(path -> !path.isBlank())
            .map(path -> storagePathNormalizer.normalize(path).orElse(path))
            .filter(path -> path.startsWith(prefix))
            .toList();

    if (!paths.isEmpty()) {
      taskStatusUpdaterService.markTasksAsFileSuccessfullyUploadedBySourcePaths(paths);
    }
  }
}
