package org.conalton.textprocessor.modules.task.worker.service;

import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.conalton.textprocessor.modules.task.entity.Task;
import org.conalton.textprocessor.modules.task.repository.TaskRepository;
import org.conalton.textprocessor.modules.task.service.TaskStatusFlowService;
import org.springframework.stereotype.Service;

@Service
public class TaskAcquisitionService {
  private final TaskRepository taskRepository;
  private final TaskStatusFlowService taskStatusFlowService;

  public TaskAcquisitionService(
      TaskRepository taskRepository, TaskStatusFlowService taskStatusFlowService) {
    this.taskRepository = taskRepository;
    this.taskStatusFlowService = taskStatusFlowService;
  }

  @Transactional
  public Optional<Task> getFirstTaskAndProcessIt(String lockBy) {
    List<String> statuses =
        this.taskStatusFlowService.getProcessingStatuses().stream().map(Enum::name).toList();

    Optional<Task> taskOpt = this.taskRepository.getNextAvailableForUpdate(statuses);

    if (taskOpt.isEmpty()) {
      return Optional.empty();
    }

    Task task = taskOpt.get();

    task.setLockedAt(Instant.now());
    task.setLockedBy(lockBy);
    taskRepository.save(task);

    return taskOpt;
  }
}
