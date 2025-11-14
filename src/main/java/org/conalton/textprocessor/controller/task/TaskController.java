package org.conalton.textprocessor.controller.task;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.conalton.textprocessor.dto.response.PresignedUploadResponse;
import org.conalton.textprocessor.dto.response.common.ErrorResponse;
import org.conalton.textprocessor.service.TaskService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/v1/text-processor/tasks")
public class TaskController {
  private final TaskService taskService;

  public TaskController(TaskService taskService) {
    this.taskService = taskService;
  }

  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Presigned URL successfully issued",
        content = @Content(schema = @Schema(implementation = PresignedUploadResponse.class))),
    @ApiResponse(
        responseCode = "429",
        description = "Rate limiter triggered (Too Many Requests)",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        name = "Rate limit exceeded",
                        value =
                            """
                        {
                          "timestamp": "2025-11-14T17:45:30.087Z",
                          "status": 429,
                          "message": "Too many requests"
                        }
                        """))),
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        name = "InternalServerError",
                        value =
                            """
                        {
                          "timestamp": "2025-11-14T17:45:30.087Z",
                          "status": 500,
                          "message": "Internal server error"
                        }
                        """)))
  })
  @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
  @RateLimiter(name = "webApi")
  public ResponseEntity<PresignedUploadResponse> create() {
    PresignedUploadResponse response = taskService.createTask();
    return ResponseEntity.ok(response);
  }
}
