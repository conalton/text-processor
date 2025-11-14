package org.conalton.textprocessor.domain.factory;

import com.github.f4b6a3.uuid.UuidCreator;
import java.time.Instant;
import org.conalton.textprocessor.entity.Task;
import org.conalton.textprocessor.entity.TaskStatus;

public class TaskFactory {
  private TaskFactory() {}

  public static Task create() {
    Task task = new Task();
    task.setId(UuidCreator.getTimeOrderedEpoch().toString());
    task.setStatus(TaskStatus.PENDING);
    task.setCreatedAt(Instant.now());
    return task;
  }
}
