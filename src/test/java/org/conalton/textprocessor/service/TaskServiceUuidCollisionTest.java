package org.conalton.textprocessor.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.sql.SQLException;
import org.conalton.textprocessor.domain.service.storage.DateBasedKeyGenerator;
import org.conalton.textprocessor.domain.service.storage.FileStoragePort;
import org.conalton.textprocessor.dto.internal.PresignedUrlData;
import org.conalton.textprocessor.infrastructure.persistence.constraints.ConstraintViolationClassifier;
import org.conalton.textprocessor.repository.task.TaskRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class TaskServiceUuidCollisionTest {

  @Mock private TaskRepository taskRepository;
  @Mock private FileStoragePort fileStorage;
  @Mock private DateBasedKeyGenerator keyGenerator;
  @Mock private ConstraintViolationClassifier constraintViolationClassifier;

  private TaskService taskService;

  @BeforeEach
  void setUp() {
    taskService =
        new TaskService(taskRepository, fileStorage, keyGenerator, constraintViolationClassifier);

    when(keyGenerator.generateDateBasedKey(any(), any())).thenReturn("2025/01/file.txt");
    when(fileStorage.generatePresignedUploadUrl(any(), any()))
        .thenReturn(new PresignedUrlData("key", "https://example.com/upload"));
  }

  @Test
  void createTask_shouldThrowIllegalStateException_whenPrimaryKeyViolationOccurs() {
    // Given: MySQL duplicate PRIMARY KEY exception
    SQLException sqlEx = new SQLException("Duplicate entry", "23000", 1062);
    ConstraintViolationException cve = new ConstraintViolationException("", sqlEx, "tasks.PRIMARY");
    DataIntegrityViolationException dbEx = new DataIntegrityViolationException("", cve);

    when(taskRepository.saveAndFlush(any())).thenThrow(dbEx);
    when(constraintViolationClassifier.isPrimaryKeyViolation(dbEx)).thenReturn(true);

    // When/Then: Should detect UUID collision and throw IllegalStateException
    assertThatThrownBy(() -> taskService.createTask())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("UUID collision detected")
        .hasCause(dbEx);
  }

  @Test
  void createTask_shouldRethrowOriginalException_whenNotPrimaryKeyViolation() {
    // Given: Non-PRIMARY KEY violation (e.g., UNIQUE constraint)
    SQLException sqlEx = new SQLException("Duplicate entry", "23000", 1062);
    ConstraintViolationException cve =
        new ConstraintViolationException("", sqlEx, "tasks.uk_email");
    DataIntegrityViolationException dbEx = new DataIntegrityViolationException("", cve);

    when(taskRepository.saveAndFlush(any())).thenThrow(dbEx);
    when(constraintViolationClassifier.isPrimaryKeyViolation(dbEx)).thenReturn(false);

    // When/Then: Should rethrow original exception
    assertThatThrownBy(() -> taskService.createTask())
        .isInstanceOf(DataIntegrityViolationException.class)
        .isSameAs(dbEx);
  }
}
