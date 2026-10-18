import { useState, type FormEvent } from 'react'
import { PRIORITIES, type NewTask, type Project, type TaskPriority } from '../types'

interface Props {
  projects: Project[]
  defaultProjectId?: number
  /** Resolves to true when the task was created, so the form can reset. */
  onCreate: (task: NewTask) => Promise<boolean>
}

export function TaskForm({ projects, defaultProjectId, onCreate }: Props) {
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [priority, setPriority] = useState<TaskPriority>('MEDIUM')
  const [dueDate, setDueDate] = useState('')
  const [projectId, setProjectId] = useState<number | ''>('')
  const [submitting, setSubmitting] = useState(false)

  const selectedProject = projectId === '' ? (defaultProjectId ?? projects[0]?.id) : projectId
  const canSubmit = title.trim().length > 0 && selectedProject !== undefined && !submitting

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    if (!canSubmit || selectedProject === undefined) return
    setSubmitting(true)
    try {
      const created = await onCreate({
        projectId: selectedProject,
        title: title.trim(),
        description: description.trim() || undefined,
        priority,
        dueDate: dueDate || undefined,
      })
      if (created) {
        setTitle('')
        setDescription('')
        setDueDate('')
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form className="task-form" onSubmit={handleSubmit} aria-label="Create task">
      <input
        aria-label="Title"
        placeholder="What needs to be done?"
        value={title}
        maxLength={200}
        onChange={(e) => setTitle(e.target.value)}
      />
      <input
        aria-label="Description"
        placeholder="Description (optional)"
        value={description}
        maxLength={2000}
        onChange={(e) => setDescription(e.target.value)}
      />
      <select
        aria-label="Project"
        value={selectedProject ?? ''}
        onChange={(e) => setProjectId(Number(e.target.value))}
      >
        {projects.map((p) => (
          <option key={p.id} value={p.id}>
            {p.name}
          </option>
        ))}
      </select>
      <select aria-label="Priority" value={priority} onChange={(e) => setPriority(e.target.value as TaskPriority)}>
        {PRIORITIES.map((p) => (
          <option key={p} value={p}>
            {p}
          </option>
        ))}
      </select>
      <input aria-label="Due date" type="date" value={dueDate} onChange={(e) => setDueDate(e.target.value)} />
      <button type="submit" className="primary" disabled={!canSubmit}>
        Add task
      </button>
    </form>
  )
}
