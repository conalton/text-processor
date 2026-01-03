package org.conalton.textprocessor.modules.task.service;

import org.conalton.textprocessor.modules.task.entity.Task;
import org.conalton.textprocessor.modules.task.entity.TaskStatus;
import org.springframework.stereotype.Service;

@Service
public class TaskStatusFlowService {
  public void markAsNew(Task task) {
    task.setStatus(TaskStatus.NEW);
  }

  public void markTaskAsFileUploaded(Task task) {
    task.setStatus(TaskStatus.FILE_UPLOADED);
  }

  public boolean canMarkFileUploaded(Task task) {
    return task.getStatus() == TaskStatus.NEW;
  }

  public TaskStatus getTaskStatusWhenFiledUploadIsPending() {
    return TaskStatus.NEW;
  }

  public TaskStatus getTaskNextTaskStatus(TaskStatus status) {
    return switch (status) {
      case TaskStatus.NEW -> TaskStatus.FILE_UPLOADED;
      default -> null;
    };
  }
}
