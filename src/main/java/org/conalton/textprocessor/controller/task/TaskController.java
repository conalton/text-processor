package org.conalton.textprocessor.controller.task;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.conalton.textprocessor.dto.response.PresignedUploadResponse;
import org.conalton.textprocessor.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/text-processor/tasks")
public class TaskController {
  private final TaskService taskService;

  public TaskController(TaskService taskService) {
    this.taskService = taskService;
  }

  @PostMapping("/create")
  @RateLimiter(name = "webApi")
  public ResponseEntity<PresignedUploadResponse> create() {
    PresignedUploadResponse response = taskService.createTask();
    return ResponseEntity.ok(response);
  }
}
