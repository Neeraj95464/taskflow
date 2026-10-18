package dev.neeraj.taskflow.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import dev.neeraj.taskflow.common.ResourceNotFoundException;
import dev.neeraj.taskflow.project.Project;
import dev.neeraj.taskflow.project.ProjectService;
import dev.neeraj.taskflow.task.TaskDtos.TaskRequest;
import dev.neeraj.taskflow.task.TaskDtos.TaskResponse;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository tasks;

    @Mock
    private ProjectService projects;

    @InjectMocks
    private TaskService service;

    private final Project project = new Project("Website", "Marketing site");

    @Test
    void createStartsInTodoWithTrimmedTitleAndDefaultPriority() {
        given(projects.find(1L)).willReturn(project);
        given(tasks.save(any(Task.class))).willAnswer(invocation -> invocation.getArgument(0));

        TaskResponse created = service.create(
                new TaskRequest(1L, "  Write docs  ", null, null, LocalDate.of(2030, 1, 15)));

        assertThat(created.title()).isEqualTo("Write docs");
        assertThat(created.status()).isEqualTo(TaskStatus.TODO);
        assertThat(created.priority()).isEqualTo(TaskPriority.MEDIUM);
        assertThat(created.dueDate()).isEqualTo(LocalDate.of(2030, 1, 15));
    }

    @Test
    void createFailsWhenProjectDoesNotExist() {
        given(projects.find(99L)).willThrow(new ResourceNotFoundException("Project", 99L));

        assertThatThrownBy(() -> service.create(new TaskRequest(99L, "Orphan", null, TaskPriority.LOW, null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Project with id 99");
        then(tasks).should(never()).save(any());
    }

    @Test
    void changeStatusMovesTask() {
        Task task = new Task(project, "Deploy", null, TaskPriority.HIGH, null);
        given(tasks.findById(7L)).willReturn(Optional.of(task));
        given(tasks.saveAndFlush(task)).willReturn(task);

        TaskResponse moved = service.changeStatus(7L, TaskStatus.IN_PROGRESS);

        assertThat(moved.status()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    void changeStatusOfUnknownTaskThrowsNotFound() {
        given(tasks.findById(42L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.changeStatus(42L, TaskStatus.DONE))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Task with id 42 not found");
    }

    @Test
    void deleteRemovesExistingTask() {
        Task task = new Task(project, "Cleanup", null, null, null);
        given(tasks.findById(3L)).willReturn(Optional.of(task));

        service.delete(3L);

        then(tasks).should().delete(task);
    }
}
