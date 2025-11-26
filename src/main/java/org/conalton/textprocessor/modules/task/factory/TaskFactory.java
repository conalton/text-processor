package org.conalton.textprocessor.modules.task.factory;

import com.github.f4b6a3.uuid.UuidCreator;
import java.time.Instant;
import org.conalton.textprocessor.modules.task.entity.Task;
import org.conalton.textprocessor.modules.task.service.TaskStatusFlowService;
import org.springframework.stereotype.Component;

@Component
public class TaskFactory {
  private final TaskStatusFlowService taskStatusFlowService;

  public TaskFactory(TaskStatusFlowService taskStatusFlowService) {
    this.taskStatusFlowService = taskStatusFlowService;
  }

  public Task create() {
    Task task = new Task();
    task.setId(UuidCreator.getTimeOrderedEpoch().toString());
    task.setCreatedAt(Instant.now());
    task.markEntityAsNew();
    taskStatusFlowService.markAsNew(task);
    return task;
  }
}
