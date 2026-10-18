import { STATUSES, STATUS_LABELS, type Task, type TaskStatus } from '../types'

interface Props {
  task: Task
  projectName?: string
  onMove: (task: Task, status: TaskStatus) => void
  onDelete: (task: Task) => void
}

export function TaskCard({ task, projectName, onMove, onDelete }: Props) {
  const index = STATUSES.indexOf(task.status)
  const previous = STATUSES[index - 1]
  const next = STATUSES[index + 1]

  return (
    <article className="card" aria-label={task.title}>
      <header className="card-header">
        <h3>{task.title}</h3>
        <span className={`badge badge-${task.priority.toLowerCase()}`}>{task.priority}</span>
      </header>
      {task.description && <p className="card-description">{task.description}</p>}
      <footer className="card-footer">
        <span className="card-meta">
          {projectName && <span>{projectName}</span>}
          {task.dueDate && <span>Due {task.dueDate}</span>}
        </span>
        <span className="card-actions">
          {previous && (
            <button type="button" onClick={() => onMove(task, previous)} aria-label={`Move to ${STATUS_LABELS[previous]}`}>
              ←
            </button>
          )}
          {next && (
            <button type="button" onClick={() => onMove(task, next)} aria-label={`Move to ${STATUS_LABELS[next]}`}>
              →
            </button>
          )}
          <button type="button" className="danger" onClick={() => onDelete(task)} aria-label="Delete task">
            ✕
          </button>
        </span>
      </footer>
    </article>
  )
}
