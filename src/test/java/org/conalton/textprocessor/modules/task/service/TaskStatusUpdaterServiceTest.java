package org.conalton.textprocessor.modules.task.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.conalton.textprocessor.modules.task.entity.TaskStatus;
import org.conalton.textprocessor.modules.task.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskStatusUpdaterServiceTest {

  @Mock TaskStatusFlowService flow;
  @Mock TaskRepository repository;
  @InjectMocks TaskStatusUpdaterService service;

  private void stubFlow() {
    when(flow.getTaskStatusWhenFileUploadIsPending()).thenReturn(TaskStatus.NEW);
    when(flow.getTaskNextTaskStatus(TaskStatus.NEW)).thenReturn(TaskStatus.FILE_UPLOADED);
  }

  @Test
  void markTasksAsFileSuccessfullyUploadedBySourcePaths_emptyList_noCall() {
    service.markTasksAsFileSuccessfullyUploadedBySourcePaths(List.of());
    verifyNoInteractions(repository);
    verifyNoInteractions(flow);
  }

  @Test
  void markTasksAsFileSuccessfullyUploadedBySourcePaths_callsUpdateWithStatuses() {
    stubFlow();
    List<String> paths = List.of("p1", "p2");
    when(repository.updateTasksWithNewStatusBySourcePathAndCurrentStatus(
            paths, TaskStatus.NEW, TaskStatus.FILE_UPLOADED))
        .thenReturn(paths.size());

    service.markTasksAsFileSuccessfullyUploadedBySourcePaths(paths);

    verify(repository)
        .updateTasksWithNewStatusBySourcePathAndCurrentStatus(
            paths, TaskStatus.NEW, TaskStatus.FILE_UPLOADED);
  }

  @Test
  void markTasksAsFileSuccessfullyUploadedBySourcePaths_whenNotAllUpdated_justReturns() {
    stubFlow();
    List<String> paths = List.of("p1", "p2", "p3");
    when(repository.updateTasksWithNewStatusBySourcePathAndCurrentStatus(anyList(), any(), any()))
        .thenReturn(1);

    service.markTasksAsFileSuccessfullyUploadedBySourcePaths(paths);

    verify(repository)
        .updateTasksWithNewStatusBySourcePathAndCurrentStatus(
            paths, TaskStatus.NEW, TaskStatus.FILE_UPLOADED);
  }
}
