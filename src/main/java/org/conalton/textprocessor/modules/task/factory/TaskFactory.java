package org.conalton.textprocessor.modules.task.factory;

import com.github.f4b6a3.uuid.UuidCreator;
import java.time.Instant;
import org.conalton.textprocessor.modules.task.entity.Task;
import org.conalton.textprocessor.modules.task.entity.TaskStatus;

public class TaskFactory {
  private TaskFactory() {}

  public static Task create() {
    Task task = new Task();
    task.setId(UuidCreator.getTimeOrderedEpoch().toString());
    task.setStatus(TaskStatus.PENDING);
    task.setCreatedAt(Instant.now());
    task.markAsNew();
    return task;
  }
}
