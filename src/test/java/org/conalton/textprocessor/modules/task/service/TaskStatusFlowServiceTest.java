package org.conalton.textprocessor.modules.task.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.conalton.textprocessor.modules.task.entity.Task;
import org.conalton.textprocessor.modules.task.entity.TaskStatus;
import org.junit.jupiter.api.Test;

class TaskStatusFlowServiceTest {

  private final TaskStatusFlowService flow = new TaskStatusFlowService();

  @Test
  void markAsNew_setsStatusNew() {
    Task task = new Task();
    flow.markAsNew(task);
    assertThat(task.getStatus()).isEqualTo(TaskStatus.NEW);
  }

  @Test
  void canMarkFileUploaded_trueOnlyForNew() {
    Task task = new Task();
    task.setStatus(TaskStatus.NEW);
    assertThat(flow.canMarkFileUploaded(task)).isTrue();

    task.setStatus(TaskStatus.FILE_UPLOADED);
    assertThat(flow.canMarkFileUploaded(task)).isFalse();
  }

  @Test
  void getTaskNextTaskStatus_returnsNextOrNull() {
    assertThat(flow.getTaskNextTaskStatus(TaskStatus.NEW)).isEqualTo(TaskStatus.FILE_UPLOADED);
    assertThat(flow.getTaskNextTaskStatus(TaskStatus.FILE_UPLOADED))
        .isEqualTo(TaskStatus.FILE_IS_READY);
    assertThat(flow.getTaskNextTaskStatus(TaskStatus.FILE_IS_READY)).isEqualTo(TaskStatus.DONE);
  }

  @Test
  void getProcessingStatuses_containsOnlyFileUploaded() {
    assertThat(flow.getProcessingStatuses()).containsExactly(TaskStatus.FILE_UPLOADED);
  }
}
