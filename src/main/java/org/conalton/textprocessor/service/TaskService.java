package org.conalton.textprocessor.service;

import org.conalton.textprocessor.domain.factory.TaskFactory;
import org.conalton.textprocessor.domain.service.storage.DateBasedKeyGenerator;
import org.conalton.textprocessor.domain.service.storage.FileStoragePort;
import org.conalton.textprocessor.domain.service.storage.StorageLocation;
import org.conalton.textprocessor.dto.internal.PresignedUrlData;
import org.conalton.textprocessor.dto.response.PresignedUploadResponse;
import org.conalton.textprocessor.entity.Task;
import org.conalton.textprocessor.infrastructure.persistence.constraints.ConstraintViolationClassifier;
import org.conalton.textprocessor.repository.task.TaskRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {
  private final TaskRepository taskRepository;
  private final FileStoragePort fileStorage;
  private final DateBasedKeyGenerator keyGenerator;
  private final ConstraintViolationClassifier constraintViolationClassifier;

  public TaskService(
      TaskRepository taskRepository,
      FileStoragePort fileStorage,
      DateBasedKeyGenerator keyGenerator,
      ConstraintViolationClassifier constraintViolationClassifier) {
    this.taskRepository = taskRepository;
    this.fileStorage = fileStorage;
    this.keyGenerator = keyGenerator;
    this.constraintViolationClassifier = constraintViolationClassifier;
  }

  @Transactional
  public PresignedUploadResponse createTask() {
    Task task = TaskFactory.create();
    String uploadPath =
        keyGenerator.generateDateBasedKey(task.getId(), StorageLocation.TASKS.getUploadPrefix());

    PresignedUrlData fileData =
        this.fileStorage.generatePresignedUploadUrl(StorageLocation.TASKS, uploadPath);

    task.setSourcePath(fileData.key());

    try {
      taskRepository.saveAndFlush(task);
    } catch (DataIntegrityViolationException ex) {
      if (constraintViolationClassifier.isPrimaryKeyViolation(ex)) {
        throw new IllegalStateException("UUID collision detected: " + task.getId(), ex);
      }
      throw ex;
    }

    return new PresignedUploadResponse(task.getId(), fileData.url());
  }
}
