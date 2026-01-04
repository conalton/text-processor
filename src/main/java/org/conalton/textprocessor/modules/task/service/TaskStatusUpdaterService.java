package org.conalton.textprocessor.modules.task.service;

import jakarta.transaction.Transactional;
import java.util.List;
import org.conalton.textprocessor.modules.task.entity.TaskStatus;
import org.conalton.textprocessor.modules.task.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TaskStatusUpdaterService {
  private static final Logger log = LoggerFactory.getLogger(TaskStatusUpdaterService.class);
  private final TaskStatusFlowService taskStatusFlowService;
  private final TaskRepository taskRepository;

  public TaskStatusUpdaterService(
      TaskStatusFlowService taskStatusFlowService, TaskRepository taskRepository) {
    this.taskStatusFlowService = taskStatusFlowService;
    this.taskRepository = taskRepository;
  }

  @Transactional
  public void markTasksAsFileSuccessfullyUploadedBySourcePaths(List<String> paths) {
    if (paths.isEmpty()) {
      return;
    }

    TaskStatus currentStatus = taskStatusFlowService.getTaskStatusWhenFileUploadIsPending();
    TaskStatus newStatus = taskStatusFlowService.getTaskNextTaskStatus(currentStatus);

    int updatedAmount =
        taskRepository.updateTasksWithNewStatusBySourcePathAndCurrentStatus(
            paths, currentStatus, newStatus);

    if (updatedAmount != paths.size()) {
      log.warn(
          "Expected to update {} tasks to status {}, but only {} were updated; some tasks may be missing or in an unexpected current status.",
          paths.size(),
          newStatus,
          updatedAmount);

      return;
    }

    log.debug("Updated {} tasks with new status: {}", updatedAmount, newStatus);
  }
}
