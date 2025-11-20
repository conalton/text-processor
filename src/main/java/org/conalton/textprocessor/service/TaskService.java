package org.conalton.textprocessor.service;

import static org.conalton.textprocessor.infrastructure.persistence.constraints.ConstraintViolationClassifier.BEAN_NAME;

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
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {
  private static final int MAX_PRIMARY_KEY_RETRIES = 5;
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

  @Retryable(
      retryFor = {DataIntegrityViolationException.class},
      maxAttempts = MAX_PRIMARY_KEY_RETRIES,
      exceptionExpression = "@" + BEAN_NAME + ".isPrimaryKeyViolation(#root)",
      backoff = @Backoff(delay = 0))
  @Transactional
  public PresignedUploadResponse createTask() {
    Task task = TaskFactory.create();
    String uploadPath =
        keyGenerator.generateDateBasedKey(task.getId(), StorageLocation.TASKS.getUploadPrefix());

    PresignedUrlData fileData =
        this.fileStorage.generatePresignedUploadUrl(StorageLocation.TASKS, uploadPath);
    task.setSourcePath(fileData.key());
    taskRepository.saveAndFlush(task);

    return new PresignedUploadResponse(task.getId(), fileData.url());
  }

  @Recover
  protected PresignedUploadResponse recover(DataIntegrityViolationException ex) {
    if (constraintViolationClassifier.isPrimaryKeyViolation(ex)) {
      throw new IllegalStateException(
          String.format("UUID collision detected after %d attempts", MAX_PRIMARY_KEY_RETRIES), ex);
    }
    throw ex;
  }
}
