package org.conalton.textprocessor.modules.task.worker;

import java.util.Optional;
import org.conalton.textprocessor.modules.task.entity.Task;
import org.conalton.textprocessor.modules.task.worker.processor.TaskProcessingService;
import org.conalton.textprocessor.modules.task.worker.service.TaskAcquisitionService;

public class TaskRunnable implements Runnable {
  private final int jobId;
  private final TaskAcquisitionService taskAcquisitionService;
  private final String lockBy;
  private final TaskProcessingService taskProcessingService;

  public TaskRunnable(
      TaskAcquisitionService taskAcquisitionService,
      TaskProcessingService taskProcessingService,
      String lockBy,
      int jobId) {
    this.jobId = jobId;
    this.taskAcquisitionService = taskAcquisitionService;
    this.taskProcessingService = taskProcessingService;
    this.lockBy = lockBy;
  }

  @Override
  public void run() {
    Optional<Task> taskOpt = this.taskAcquisitionService.getFirstTaskAndProcessIt(lockBy);

    if (taskOpt.isEmpty()) {
      return;
    }

    this.taskProcessingService.processTask(taskOpt.get());
  }
}
