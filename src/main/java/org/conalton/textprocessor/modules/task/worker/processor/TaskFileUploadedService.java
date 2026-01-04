package org.conalton.textprocessor.modules.task.worker.processor;

import org.conalton.textprocessor.domain.storage.port.FileStoragePort;
import org.conalton.textprocessor.domain.storage.service.DateBasedKeyGenerator;
import org.conalton.textprocessor.domain.storage.types.StorageLocation;
import org.conalton.textprocessor.modules.task.entity.Task;
import org.conalton.textprocessor.modules.task.entity.TaskStatus;
import org.conalton.textprocessor.modules.task.repository.TaskRepository;
import org.conalton.textprocessor.modules.task.service.TaskStatusFlowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class TaskFileUploadedService implements TaskProcessor {
  private final TaskRepository taskRepository;
  private final TaskStatusFlowService taskStatusFlowService;
  private final DateBasedKeyGenerator keyGenerator;
  private final FileStoragePort fileStoragePort;
  private final TransactionTemplate transactionTemplate;
  private static final Logger log = LoggerFactory.getLogger(TaskFileUploadedService.class);

  public TaskFileUploadedService(
      TaskRepository taskRepository,
      TaskStatusFlowService taskStatusFlowService,
      DateBasedKeyGenerator keyGenerator,
      FileStoragePort fileStoragePort,
      TransactionTemplate transactionTemplate) {
    this.taskRepository = taskRepository;
    this.taskStatusFlowService = taskStatusFlowService;
    this.keyGenerator = keyGenerator;
    this.fileStoragePort = fileStoragePort;
    this.transactionTemplate = transactionTemplate;
  }

  @Override
  public TaskStatus status() {
    return TaskStatus.FILE_UPLOADED;
  }

  @Override
  public void processTask(Task task) {
    if (task == null) {
      return;
    }

    log.debug("Processing task with id[{}], status[{}]", task.getId(), task.getStatus());

    // deletion should be done automatically by storage lifecycle policy
    String newPath = this.copyFilesIntoUploadedFolder(task);

    TaskStatus nextStatus = this.taskStatusFlowService.getTaskNextTaskStatus(task.getStatus());

    log.debug("Updating task data with id[{}], status[{}]", task.getId(), task.getStatus());

    transactionTemplate.executeWithoutResult(
        tx -> {
          task.unlock();
          task.setStatus(nextStatus);
          task.setSourcePath(newPath);
          taskRepository.save(task);
        });

    log.debug(
        "Successfully processed task with id[{}], new status[{}]", task.getId(), task.getStatus());
  }

  private String copyFilesIntoUploadedFolder(Task task) {
    log.debug("Copying files for task with id[{}], status[{}]", task.getId(), task.getStatus());

    String pathForUploaded =
        keyGenerator.generateDateBasedKey(
            task.getId(), StorageLocation.TASKS_UPLOADED_FILES.getUploadPrefix());

    fileStoragePort.copy(
        StorageLocation.TASKS_PRESIGNED_UPLOADS,
        task.getSourcePath(),
        StorageLocation.TASKS_UPLOADED_FILES,
        pathForUploaded);

    fileStoragePort.delete(StorageLocation.TASKS_PRESIGNED_UPLOADS, task.getSourcePath());

    return pathForUploaded;
  }
}
