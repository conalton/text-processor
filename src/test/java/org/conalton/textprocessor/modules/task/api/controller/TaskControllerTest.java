package org.conalton.textprocessor.modules.task.api.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.conalton.textprocessor.modules.task.api.dto.response.PresignedUpload;
import org.conalton.textprocessor.modules.task.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

@WebMvcTest(TaskController.class)
@ActiveProfiles("test")
class TaskControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private TaskService taskService;

  @Test
  void create_returnsOkAndBody() throws Exception {
    String taskId = UUID.randomUUID().toString();
    PresignedUpload stub = new PresignedUpload(taskId, "https://fake.url/upload");

    when(taskService.createTask()).thenReturn(stub);

    mockMvc
        .perform(
            post("/api/v1/text-processor/tasks/create").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.taskId").value(taskId))
        .andExpect(jsonPath("$.uploadUrl").value("https://fake.url/upload"));
  }

  @Test
  void create_whenServiceFails_returnsInternalServerError() throws Exception {
    when(taskService.createTask()).thenThrow(new RuntimeException("Storage error"));

    mockMvc
        .perform(
            post("/api/v1/text-processor/tasks/create").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
  }
}
