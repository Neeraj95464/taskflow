package dev.neeraj.taskflow;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * End-to-end test against a real PostgreSQL started by Testcontainers.
 * Runs in the Maven {@code integration-test} phase (failsafe) and is skipped when Docker is unavailable.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureObservability(tracing = false) // keep the Prometheus registry in tests
class TaskFlowApiIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TestRestTemplate http;

    @Test
    void flywaySeedsDemoProject() {
        ResponseEntity<JsonNode> response = http.getForEntity("/api/projects", JsonNode.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("content").findValuesAsText("name")).contains("Demo");
    }

    @Test
    void taskLifecycle() {
        long projectId = http.postForEntity("/api/projects",
                Map.of("name", "IT project", "description", "created by TaskFlowApiIT"), JsonNode.class)
                .getBody().get("id").asLong();

        ResponseEntity<JsonNode> created = http.postForEntity("/api/tasks",
                Map.of("projectId", projectId, "title", "Ship it", "priority", "HIGH", "dueDate", "2030-06-30"),
                JsonNode.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        long taskId = created.getBody().get("id").asLong();
        assertThat(created.getBody().get("status").asText()).isEqualTo("TODO");

        ResponseEntity<JsonNode> moved = http.exchange("/api/tasks/{id}/status", HttpMethod.PATCH,
                new HttpEntity<>(Map.of("status", "DONE")), JsonNode.class, taskId);
        assertThat(moved.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(moved.getBody().get("status").asText()).isEqualTo("DONE");

        JsonNode done = http.getForObject("/api/tasks?projectId={p}&status=DONE", JsonNode.class, projectId);
        assertThat(done.get("totalElements").asLong()).isEqualTo(1);
        assertThat(done.get("content").get(0).get("id").asLong()).isEqualTo(taskId);

        JsonNode todo = http.getForObject("/api/tasks?projectId={p}&status=TODO", JsonNode.class, projectId);
        assertThat(todo.get("totalElements").asLong()).isZero();

        http.delete("/api/projects/{id}", projectId);
        assertThat(http.getForEntity("/api/tasks/{id}", JsonNode.class, taskId).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void duplicateProjectNameReturns409() {
        Map<String, String> body = Map.of("name", "Unique name");
        http.postForEntity("/api/projects", body, JsonNode.class);

        ResponseEntity<JsonNode> duplicate = http.postForEntity("/api/projects", body, JsonNode.class);

        assertThat(duplicate.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(duplicate.getBody().get("title").asText()).isEqualTo("Data conflict");
    }

    @Test
    void actuatorExposesHealthAndPrometheus() {
        assertThat(http.getForObject("/actuator/health", JsonNode.class).get("status").asText()).isEqualTo("UP");
        assertThat(http.getForObject("/actuator/prometheus", String.class)).contains("jvm_memory_used_bytes");
    }
}
