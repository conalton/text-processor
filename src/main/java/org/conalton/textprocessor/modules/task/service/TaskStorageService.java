package org.conalton.textprocessor.modules.task.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.conalton.textprocessor.domain.storage.service.StorageLocationResolver;
import org.conalton.textprocessor.domain.storage.types.FileStorageItem;
import org.conalton.textprocessor.domain.storage.types.StorageLocation;
import org.conalton.textprocessor.modules.task.entity.Task;
import org.conalton.textprocessor.modules.task.entity.TaskStatus;
import org.conalton.textprocessor.modules.task.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskStorageService {
  private final TaskRepository taskRepository;
  private final StorageLocationResolver storageLocationResolver;
  private static final Logger log = LoggerFactory.getLogger(TaskStorageService.class);

  public TaskStorageService(
      TaskRepository taskRepository, StorageLocationResolver storageLocationResolver) {
    this.taskRepository = taskRepository;
    this.storageLocationResolver = storageLocationResolver;
  }

  @Transactional
  public void processFilesUploads(@NonNull List<FileStorageItem> files) {
    if (files.isEmpty()) {
      return;
    }

    Set<String> normalizedUploadPaths = prepareFilesLocations(files);

    if (normalizedUploadPaths.isEmpty()) {
      return;
    }

    markTaskAsProcessingByUploadPaths(normalizedUploadPaths);
  }

  private Set<String> prepareFilesLocations(@NonNull List<FileStorageItem> files) {
    String tasksBucket = storageLocationResolver.resolveStorageBucket(StorageLocation.TASKS);

    return files.stream()
        .filter(Objects::nonNull)
        .filter(file -> tasksBucket.equals(file.location()))
        .map(FileStorageItem::uploadPath)
        .filter(Objects::nonNull)
        .map(String::trim)
        .filter(path -> !path.isBlank())
        .map(path -> path.startsWith("/") ? path.substring(1) : path)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  private void markTaskAsProcessingByUploadPaths(@NonNull Set<String> paths) {
    List<Task> tasks = taskRepository.findAllBySourcePathIn(paths);
    if (tasks.isEmpty()) {
      return;
    }

    List<Task> unprocessedTasks =
        tasks.stream().filter(task -> task.getStatus() != TaskStatus.NEW).toList();

    if (!unprocessedTasks.isEmpty()) {
      log.warn(
          "Found {} tasks that are not in NEW status while processing file uploads: {}",
          unprocessedTasks.size(),
          unprocessedTasks.stream()
              .map(task -> String.format("Task{id=%s, status=%s}", task.getId(), task.getStatus()))
              .collect(Collectors.joining(", ")));
    }

    tasks.stream()
        .filter(task -> task.getStatus() == TaskStatus.NEW)
        .forEach(task -> task.setStatus(TaskStatus.FILE_UPLOADED));

    taskRepository.saveAll(tasks);
  }
}
