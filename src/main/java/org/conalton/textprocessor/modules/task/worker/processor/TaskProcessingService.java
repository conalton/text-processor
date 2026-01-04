package org.conalton.textprocessor.modules.task.worker.processor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.conalton.textprocessor.modules.task.entity.Task;
import org.conalton.textprocessor.modules.task.entity.TaskStatus;
import org.springframework.stereotype.Service;

@Service
public class TaskProcessingService {
  private final Map<TaskStatus, TaskProcessor> processors;

  public TaskProcessingService(List<TaskProcessor> processorList) {
    processors =
        Collections.unmodifiableMap(
            processorList.stream()
                .collect(Collectors.toMap(TaskProcessor::status, Function.identity())));
  }

  public void processTask(Task task) {
    TaskProcessor processor = processors.get(task.getStatus());

    if (processor == null) {
      throw new IllegalStateException(
          String.format("There is no processor for status: %s", task.getStatus()));
    }

    processor.processTask(task);
  }
}
