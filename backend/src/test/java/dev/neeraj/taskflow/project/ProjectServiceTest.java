package dev.neeraj.taskflow.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import dev.neeraj.taskflow.common.ResourceNotFoundException;
import dev.neeraj.taskflow.project.ProjectDtos.ProjectRequest;
import dev.neeraj.taskflow.project.ProjectDtos.ProjectResponse;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projects;

    @InjectMocks
    private ProjectService service;

    @Test
    void createTrimsName() {
        given(projects.save(any(Project.class))).willAnswer(invocation -> invocation.getArgument(0));

        ProjectResponse created = service.create(new ProjectRequest("  Mobile app ", "iOS + Android"));

        assertThat(created.name()).isEqualTo("Mobile app");
        assertThat(created.description()).isEqualTo("iOS + Android");
    }

    @Test
    void updateChangesNameAndDescription() {
        Project project = new Project("Old", null);
        given(projects.findById(1L)).willReturn(Optional.of(project));
        given(projects.saveAndFlush(project)).willReturn(project);

        ProjectResponse updated = service.update(1L, new ProjectRequest("New", "Renamed"));

        assertThat(updated.name()).isEqualTo("New");
        assertThat(updated.description()).isEqualTo("Renamed");
    }

    @Test
    void findUnknownProjectThrowsNotFound() {
        given(projects.findById(5L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(5L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Project with id 5 not found");
    }
}
