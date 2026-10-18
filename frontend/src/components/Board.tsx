import { STATUSES, STATUS_LABELS, type Project, type Task, type TaskStatus } from '../types'
import { TaskCard } from './TaskCard'

interface Props {
  tasks: Task[]
  projects: Project[]
  onMove: (task: Task, status: TaskStatus) => void
  onDelete: (task: Task) => void
}

export function Board({ tasks, projects, onMove, onDelete }: Props) {
  const projectNames = new Map(projects.map((p) => [p.id, p.name]))

  return (
    <div className="board">
      {STATUSES.map((status) => {
        const column = tasks.filter((t) => t.status === status)
        return (
          <section key={status} className="column" aria-labelledby={`col-${status}`}>
            <h2 id={`col-${status}`}>
              {STATUS_LABELS[status]} <span className="count">{column.length}</span>
            </h2>
            {column.length === 0 && <p className="empty">No tasks</p>}
            {column.map((task) => (
              <TaskCard
                key={task.id}
                task={task}
                projectName={projectNames.get(task.projectId)}
                onMove={onMove}
                onDelete={onDelete}
              />
            ))}
          </section>
        )
      })}
    </div>
  )
}
