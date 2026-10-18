package dev.neeraj.taskflow.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;

/** Request and response payloads for the task API. */
public final class TaskDtos {

    private TaskDtos() {
    }

    public record TaskRequest(
            @NotNull Long projectId,
            @NotBlank @Size(max = 200) String title,
            @Size(max = 2000) String description,
            TaskPriority priority,
            LocalDate dueDate) {
    }

    public record StatusChangeRequest(@NotNull TaskStatus status) {
    }

    public record TaskResponse(
            Long id,
            Long projectId,
            String title,
            String description,
            TaskStatus status,
            TaskPriority priority,
            LocalDate dueDate,
            Instant createdAt,
            Instant updatedAt) {

        static TaskResponse from(Task task) {
            return new TaskResponse(task.getId(), task.getProject().getId(), task.getTitle(), task.getDescription(),
                    task.getStatus(), task.getPriority(), task.getDueDate(), task.getCreatedAt(), task.getUpdatedAt());
        }
    }
}
