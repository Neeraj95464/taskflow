package dev.neeraj.taskflow.project;

import dev.neeraj.taskflow.common.PageResponse;
import dev.neeraj.taskflow.common.ResourceNotFoundException;
import dev.neeraj.taskflow.project.ProjectDtos.ProjectRequest;
import dev.neeraj.taskflow.project.ProjectDtos.ProjectResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projects;

    public ProjectService(ProjectRepository projects) {
        this.projects = projects;
    }

    public PageResponse<ProjectResponse> list(Pageable pageable) {
        return PageResponse.from(projects.findAll(pageable), ProjectResponse::from);
    }

    public ProjectResponse get(Long id) {
        return ProjectResponse.from(find(id));
    }

    @Transactional
    public ProjectResponse create(ProjectRequest request) {
        Project saved = projects.save(new Project(request.name().trim(), request.description()));
        return ProjectResponse.from(saved);
    }

    @Transactional
    public ProjectResponse update(Long id, ProjectRequest request) {
        Project project = find(id);
        project.update(request.name().trim(), request.description());
        return ProjectResponse.from(projects.saveAndFlush(project));
    }

    @Transactional
    public void delete(Long id) {
        projects.delete(find(id));
    }

    /** Loads a project or fails with a 404-mapped exception. */
    public Project find(Long id) {
        return projects.findById(id).orElseThrow(() -> new ResourceNotFoundException("Project", id));
    }
}
