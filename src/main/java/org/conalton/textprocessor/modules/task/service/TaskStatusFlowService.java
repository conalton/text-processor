package org.conalton.textprocessor.modules.task.service;

import java.util.List;
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

  public TaskStatus getTaskStatusWhenFileUploadIsPending() {
    return TaskStatus.NEW;
  }

  public TaskStatus getTaskNextTaskStatus(TaskStatus status) {
    return switch (status) {
      case TaskStatus.NEW -> TaskStatus.FILE_UPLOADED;
      case TaskStatus.FILE_UPLOADED -> TaskStatus.FILE_IS_READY;
      default -> TaskStatus.DONE;
    };
  }

  public List<TaskStatus> getProcessingStatuses() {
    return List.of(TaskStatus.FILE_UPLOADED);
  }
}
