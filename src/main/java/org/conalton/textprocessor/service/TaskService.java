package org.conalton.textprocessor.service;

import org.conalton.textprocessor.domain.factory.TaskFactory;
import org.conalton.textprocessor.domain.service.storage.DateBasedKeyGenerator;
import org.conalton.textprocessor.domain.service.storage.FileStoragePort;
import org.conalton.textprocessor.domain.service.storage.StorageLocation;
import org.conalton.textprocessor.dto.internal.PresignedUrlData;
import org.conalton.textprocessor.dto.response.PresignedUploadResponse;
import org.conalton.textprocessor.entity.Task;
import org.conalton.textprocessor.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TaskService {
  private final TaskRepository taskRepository;
  private final FileStoragePort fileStorage;
  private final DateBasedKeyGenerator keyGenerator;

  public TaskService(
      TaskRepository taskRepository,
      FileStoragePort fileStorage,
      DateBasedKeyGenerator keyGenerator) {
    this.taskRepository = taskRepository;
    this.fileStorage = fileStorage;
    this.keyGenerator = keyGenerator;
  }

  @Transactional
  public PresignedUploadResponse createTask() {
    Task task = TaskFactory.create();
    String uploadPath =
        keyGenerator.generateDateBasedKey(task.getId(), StorageLocation.TASKS.getUploadPrefix());

    PresignedUrlData fileData =
        this.fileStorage.generatePresignedUploadUrl(StorageLocation.TASKS, uploadPath);

    task.setSourcePath(fileData.key());

    taskRepository.save(task);

    return new PresignedUploadResponse(task.getId(), fileData.url());
  }
}
