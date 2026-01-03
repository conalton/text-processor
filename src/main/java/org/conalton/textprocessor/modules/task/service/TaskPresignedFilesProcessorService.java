package org.conalton.textprocessor.modules.task.service;

import java.util.*;
import org.conalton.textprocessor.domain.storage.service.StorageLocationResolver;
import org.conalton.textprocessor.domain.storage.types.FileStorageItem;
import org.conalton.textprocessor.domain.storage.types.StorageLocation;
import org.springframework.stereotype.Service;

@Service
public class TaskPresignedFilesProcessorService {
  private final StorageLocationResolver storageLocationResolver;
  private final TaskStatusUpdaterService taskStatusUpdaterService;

  public TaskPresignedFilesProcessorService(
      StorageLocationResolver storageLocationResolver,
      TaskStatusUpdaterService taskStatusUpdaterService) {
    this.storageLocationResolver = storageLocationResolver;
    this.taskStatusUpdaterService = taskStatusUpdaterService;
  }

  public void processFilesUploads(List<FileStorageItem> files) {
    if (files.isEmpty()) {
      return;
    }

    List<String> normalizedUploadPaths = prepareFilesLocations(files);

    if (normalizedUploadPaths.isEmpty()) {
      return;
    }

    taskStatusUpdaterService.markTasksAsFileSuccessfullyUploadedBySourcePaths(
        normalizedUploadPaths);
  }

  private List<String> prepareFilesLocations(List<FileStorageItem> files) {
    String tasksBucket =
        storageLocationResolver.resolveStorageBucket(StorageLocation.TASKS_PRESIGNED_UPLOADS);

    return files.stream()
        .filter(Objects::nonNull)
        .filter(file -> tasksBucket.equals(file.location()))
        .map(FileStorageItem::uploadPath)
        .filter(Objects::nonNull)
        .map(String::trim)
        .filter(path -> !path.isBlank())
        .map(path -> path.startsWith("/") ? path.substring(1) : path)
        .toList();
  }
}
