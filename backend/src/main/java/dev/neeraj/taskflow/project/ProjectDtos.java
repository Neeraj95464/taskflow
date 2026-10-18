package dev.neeraj.taskflow.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/** Request and response payloads for the project API. */
public final class ProjectDtos {

    private ProjectDtos() {
    }

    public record ProjectRequest(
            @NotBlank @Size(max = 100) String name,
            @Size(max = 500) String description) {
    }

    public record ProjectResponse(Long id, String name, String description, Instant createdAt, Instant updatedAt) {

        static ProjectResponse from(Project project) {
            return new ProjectResponse(project.getId(), project.getName(), project.getDescription(),
                    project.getCreatedAt(), project.getUpdatedAt());
        }
    }
}
