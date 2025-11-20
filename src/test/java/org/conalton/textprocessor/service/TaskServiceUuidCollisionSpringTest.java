package org.conalton.textprocessor.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import org.conalton.textprocessor.domain.service.storage.DateBasedKeyGenerator;
import org.conalton.textprocessor.domain.service.storage.FileStoragePort;
import org.conalton.textprocessor.dto.internal.PresignedUrlData;
import org.conalton.textprocessor.infrastructure.persistence.constraints.ConstraintViolationClassifier;
import org.conalton.textprocessor.repository.task.TaskRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class TaskServiceUuidCollisionSpringTest {
  @Autowired private TaskService taskService;

  @MockitoBean private TaskRepository taskRepository;
  @MockitoBean private FileStoragePort fileStorage;
  @MockitoBean private DateBasedKeyGenerator keyGenerator;
  @MockitoBean private ConstraintViolationClassifier constraintViolationClassifier;

  @BeforeEach
  void setUp() {
    when(keyGenerator.generateDateBasedKey(any(), any())).thenReturn("2025/01/file.txt");
    when(fileStorage.generatePresignedUploadUrl(any(), any()))
        .thenReturn(new PresignedUrlData("key", "https://example.com/upload"));
  }

  @Test
  void createTask_shouldDetectPrimaryKey_whenConstraintNameMissing_butPresentInMessage() {
    SQLException sqlEx = new SQLException("Duplicate entry '1' for key 'PRIMARY'", "23000", 1062);
    ConstraintViolationException cve =
        new ConstraintViolationException("Duplicate entry", sqlEx, null);
    DataIntegrityViolationException dbEx = new DataIntegrityViolationException("wrapper", cve);

    when(taskRepository.saveAndFlush(any())).thenThrow(dbEx);
    when(constraintViolationClassifier.isPrimaryKeyViolation(dbEx)).thenReturn(true);

    assertThatThrownBy(() -> taskService.createTask())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("UUID collision detected")
        .hasCause(dbEx);
  }

  @Test
  void createTask_shouldThrowIllegalStateException_whenPrimaryKeyViolationOccurs() {
    SQLException sqlEx = new SQLException("Duplicate entry", "23000", 1062);
    ConstraintViolationException cve = new ConstraintViolationException("", sqlEx, "tasks.PRIMARY");
    DataIntegrityViolationException dbEx = new DataIntegrityViolationException("", cve);

    when(taskRepository.saveAndFlush(any())).thenThrow(dbEx);
    when(constraintViolationClassifier.isPrimaryKeyViolation(dbEx)).thenReturn(true);

    assertThatThrownBy(() -> taskService.createTask())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("UUID collision detected")
        .hasCause(dbEx);
  }

  @Test
  void createTask_shouldRethrowOriginalException_whenNotPrimaryKeyViolation() {
    SQLException sqlEx = new SQLException("Duplicate entry", "23000", 1062);
    ConstraintViolationException cve =
        new ConstraintViolationException("", sqlEx, "tasks.uk_non_pk");
    DataIntegrityViolationException dbEx = new DataIntegrityViolationException("", cve);

    when(taskRepository.saveAndFlush(any())).thenThrow(dbEx);
    when(constraintViolationClassifier.isPrimaryKeyViolation(dbEx)).thenReturn(false);

    assertThatThrownBy(() -> taskService.createTask())
        .isInstanceOf(DataIntegrityViolationException.class)
        .isSameAs(dbEx);
  }
}
