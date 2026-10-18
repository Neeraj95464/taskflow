package dev.neeraj.taskflow.task;

import dev.neeraj.taskflow.common.PageResponse;
import dev.neeraj.taskflow.task.TaskDtos.StatusChangeRequest;
import dev.neeraj.taskflow.task.TaskDtos.TaskRequest;
import dev.neeraj.taskflow.task.TaskDtos.TaskResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks")
class TaskController {

    private final TaskService service;

    TaskController(TaskService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List tasks, optionally filtered by project and/or status (paginated)")
    PageResponse<TaskResponse> list(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) TaskStatus status,
            @ParameterObject @PageableDefault(size = 50, sort = "createdAt") Pageable pageable) {
        return service.search(projectId, status, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a task by id")
    TaskResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @Operation(summary = "Create a task (starts in TODO)")
    ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request) {
        TaskResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/tasks/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update the details of a task")
    TaskResponse update(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Move a task to another status column")
    TaskResponse changeStatus(@PathVariable Long id, @Valid @RequestBody StatusChangeRequest request) {
        return service.changeStatus(id, request.status());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task")
    ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
