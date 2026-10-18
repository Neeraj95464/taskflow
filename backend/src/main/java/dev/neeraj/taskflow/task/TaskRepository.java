package dev.neeraj.taskflow.task;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    /** Matches tasks of the given project; a {@code null} id matches everything. */
    static Specification<Task> inProject(Long projectId) {
        return (root, query, cb) -> projectId == null ? null : cb.equal(root.get("project").get("id"), projectId);
    }

    /** Matches tasks in the given status; a {@code null} status matches everything. */
    static Specification<Task> hasStatus(TaskStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }
}
