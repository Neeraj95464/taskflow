package dev.neeraj.taskflow.project;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.neeraj.taskflow.project.ProjectDtos.ProjectRequest;
import dev.neeraj.taskflow.project.ProjectDtos.ProjectResponse;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ProjectService service;

    @Test
    void getReturnsProject() throws Exception {
        Instant now = Instant.parse("2030-01-01T10:00:00Z");
        given(service.get(1L)).willReturn(new ProjectResponse(1L, "Website", null, now, now));

        mvc.perform(get("/api/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Website"));
    }

    @Test
    void createWithTooLongNameIsRejected() throws Exception {
        String longName = "x".repeat(101);

        mvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"" + longName + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());

        then(service).shouldHaveNoInteractions();
    }

    @Test
    void createReturns201() throws Exception {
        Instant now = Instant.now();
        given(service.create(any(ProjectRequest.class)))
                .willReturn(new ProjectResponse(3L, "Infra", "Platform work", now, now));

        mvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Infra\", \"description\": \"Platform work\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3));
    }

    @Test
    void deleteReturns204() throws Exception {
        mvc.perform(delete("/api/projects/3")).andExpect(status().isNoContent());

        then(service).should().delete(3L);
    }
}
