package dev.neeraj.taskflow.task;

import static dev.neeraj.taskflow.task.TaskRepository.hasStatus;
import static dev.neeraj.taskflow.task.TaskRepository.inProject;

import dev.neeraj.taskflow.common.PageResponse;
import dev.neeraj.taskflow.common.ResourceNotFoundException;
import dev.neeraj.taskflow.project.Project;
import dev.neeraj.taskflow.project.ProjectService;
import dev.neeraj.taskflow.task.TaskDtos.TaskRequest;
import dev.neeraj.taskflow.task.TaskDtos.TaskResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository tasks;
    private final ProjectService projects;

    public TaskService(TaskRepository tasks, ProjectService projects) {
        this.tasks = tasks;
        this.projects = projects;
    }

    public PageResponse<TaskResponse> search(Long projectId, TaskStatus status, Pageable pageable) {
        return PageResponse.from(
                tasks.findAll(inProject(projectId).and(hasStatus(status)), pageable),
                TaskResponse::from);
    }

    public TaskResponse get(Long id) {
        return TaskResponse.from(find(id));
    }

    @Transactional
    public TaskResponse create(TaskRequest request) {
        Project project = projects.find(request.projectId());
        Task task = new Task(project, request.title().trim(), request.description(),
                request.priority(), request.dueDate());
        return TaskResponse.from(tasks.save(task));
    }

    @Transactional
    public TaskResponse update(Long id, TaskRequest request) {
        Task task = find(id);
        Project project = projects.find(request.projectId());
        task.update(project, request.title().trim(), request.description(), request.priority(), request.dueDate());
        return TaskResponse.from(tasks.saveAndFlush(task));
    }

    @Transactional
    public TaskResponse changeStatus(Long id, TaskStatus status) {
        Task task = find(id);
        task.moveTo(status);
        return TaskResponse.from(tasks.saveAndFlush(task));
    }

    @Transactional
    public void delete(Long id) {
        tasks.delete(find(id));
    }

    private Task find(Long id) {
        return tasks.findById(id).orElseThrow(() -> new ResourceNotFoundException("Task", id));
    }
}
