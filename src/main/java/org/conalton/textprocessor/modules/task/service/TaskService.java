package org.conalton.textprocessor.modules.task.service;

import static org.conalton.textprocessor.infrastructure.persistence.constraints.ConstraintViolationClassifier.BEAN_NAME;

import org.conalton.textprocessor.domain.storage.port.FileStoragePort;
import org.conalton.textprocessor.domain.storage.service.DateBasedKeyGenerator;
import org.conalton.textprocessor.domain.storage.types.PresignedUrlData;
import org.conalton.textprocessor.domain.storage.types.StorageLocation;
import org.conalton.textprocessor.infrastructure.persistence.constraints.ConstraintViolationClassifier;
import org.conalton.textprocessor.modules.task.api.dto.response.PresignedUpload;
import org.conalton.textprocessor.modules.task.entity.Task;
import org.conalton.textprocessor.modules.task.factory.TaskFactory;
import org.conalton.textprocessor.modules.task.repository.TaskRepository;
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
  private final TaskFactory taskFactory;

  public TaskService(
      TaskRepository taskRepository,
      FileStoragePort fileStorage,
      DateBasedKeyGenerator keyGenerator,
      ConstraintViolationClassifier constraintViolationClassifier,
      TaskFactory taskFactory) {
    this.taskRepository = taskRepository;
    this.fileStorage = fileStorage;
    this.keyGenerator = keyGenerator;
    this.constraintViolationClassifier = constraintViolationClassifier;
    this.taskFactory = taskFactory;
  }

  @Retryable(
      retryFor = {DataIntegrityViolationException.class},
      maxAttempts = MAX_PRIMARY_KEY_RETRIES,
      exceptionExpression = "@" + BEAN_NAME + ".isPrimaryKeyViolation(#root)",
      backoff = @Backoff(delay = 0))
  @Transactional
  public PresignedUpload createTask() {
    Task task = taskFactory.create();
    String uploadPath =
        keyGenerator.generateDateBasedKey(task.getId(), StorageLocation.TASKS.getUploadPrefix());

    PresignedUrlData fileData =
        this.fileStorage.generatePresignedUploadUrl(StorageLocation.TASKS, uploadPath);
    task.setSourcePath(fileData.key());
    taskRepository.saveAndFlush(task);

    return new PresignedUpload(task.getId(), fileData.url());
  }

  @Recover
  protected PresignedUpload recover(DataIntegrityViolationException ex) {
    if (constraintViolationClassifier.isPrimaryKeyViolation(ex)) {
      throw new IllegalStateException(
          String.format("UUID collision detected after %d attempts", MAX_PRIMARY_KEY_RETRIES), ex);
    }
    throw ex;
  }
}
