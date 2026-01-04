package org.conalton.textprocessor.modules.task.worker.processor;

import org.conalton.textprocessor.modules.task.entity.Task;
import org.conalton.textprocessor.modules.task.entity.TaskStatus;

public interface TaskProcessor {
  TaskStatus status();

  void processTask(Task task);
}
