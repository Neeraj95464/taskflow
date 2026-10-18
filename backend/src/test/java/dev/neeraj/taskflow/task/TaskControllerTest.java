package dev.neeraj.taskflow.task;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.neeraj.taskflow.common.PageResponse;
import dev.neeraj.taskflow.common.ResourceNotFoundException;
import dev.neeraj.taskflow.task.TaskDtos.TaskRequest;
import dev.neeraj.taskflow.task.TaskDtos.TaskResponse;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private TaskService service;

    private static TaskResponse task(long id, TaskStatus status) {
        Instant now = Instant.parse("2030-01-01T10:00:00Z");
        return new TaskResponse(id, 1L, "Task " + id, null, status, TaskPriority.MEDIUM, null, now, now);
    }

    @Test
    void listFiltersByStatus() throws Exception {
        given(service.search(eq(1L), eq(TaskStatus.DONE), any()))
                .willReturn(new PageResponse<>(List.of(task(1, TaskStatus.DONE)), 0, 50, 1, 1));

        mvc.perform(get("/api/tasks").param("projectId", "1").param("status", "DONE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status").value("DONE"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void createReturns201WithLocation() throws Exception {
        given(service.create(any(TaskRequest.class))).willReturn(task(10, TaskStatus.TODO));

        mvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId": 1, "title": "Task 10", "priority": "HIGH"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/tasks/10"))
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void createWithBlankTitleReturnsProblemDetail() throws Exception {
        mvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId": 1, "title": "  "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.title").exists());
    }

    @Test
    void changeStatusOfUnknownTaskReturns404() throws Exception {
        given(service.changeStatus(99L, TaskStatus.DONE)).willThrow(new ResourceNotFoundException("Task", 99L));

        mvc.perform(patch("/api/tasks/99/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "DONE"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Task with id 99 not found"));
    }

    @Test
    void changeStatusWithInvalidValueReturns400() throws Exception {
        mvc.perform(patch("/api/tasks/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "ARCHIVED"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }
}
