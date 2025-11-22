package org.conalton.textprocessor.modules.task.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.conalton.textprocessor.config.TestStorageConfiguration;
import org.conalton.textprocessor.domain.storage.config.StorageProperties;
import org.conalton.textprocessor.modules.task.api.dto.response.PresignedUpload;
import org.conalton.textprocessor.modules.task.entity.Task;
import org.conalton.textprocessor.modules.task.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@Import(TestStorageConfiguration.class)
@ActiveProfiles("test")
class TaskServiceTest {

  @Autowired private TaskService taskService;

  @MockitoBean private TaskRepository taskRepository;

  @Autowired private StorageProperties storageProperties;

  @Test
  void createTask_generatesKeyAndUsesBucket() {
    PresignedUpload response = taskService.createTask();

    assertThat(response.uploadUrl())
        .startsWith("https://stub.local/" + storageProperties.getTasksBucketName() + "/")
        .contains("?expires=" + storageProperties.getPresignedUrlExpirationMinutes());

    ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
    verify(taskRepository).saveAndFlush(captor.capture());
    assertThat(captor.getValue().getSourcePath()).isNotBlank();
  }
}
