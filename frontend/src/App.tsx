import { useEffect, useState, type FormEvent } from 'react'
import { api } from './api'
import { Board } from './components/Board'
import { TaskForm } from './components/TaskForm'
import type { NewTask, Project, Task, TaskStatus } from './types'

const toMessage = (e: unknown) => (e instanceof Error ? e.message : 'Something went wrong')

export default function App() {
  const [projects, setProjects] = useState<Project[]>([])
  const [tasks, setTasks] = useState<Task[]>([])
  const [projectFilter, setProjectFilter] = useState<number | undefined>()
  const [newProjectName, setNewProjectName] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [refreshKey, setRefreshKey] = useState(0)

  useEffect(() => {
    api
      .listProjects()
      .then(setProjects)
      .catch((e) => setError(toMessage(e)))
  }, [])

  useEffect(() => {
    let active = true
    api
      .listTasks(projectFilter)
      .then((loaded) => active && setTasks(loaded))
      .catch((e) => active && setError(toMessage(e)))
    return () => {
      active = false
    }
  }, [projectFilter, refreshKey])

  /** Runs a user action, surfacing API errors in the alert banner. Resolves to true on success. */
  async function run(action: () => Promise<void>): Promise<boolean> {
    try {
      setError(null)
      await action()
      return true
    } catch (e) {
      setError(toMessage(e))
      return false
    }
  }

  const createTask = (task: NewTask) =>
    run(async () => {
      await api.createTask(task)
      setRefreshKey((key) => key + 1)
    })

  const moveTask = (task: Task, status: TaskStatus) => run(async () => {
    const updated = await api.moveTask(task.id, status)
    setTasks((current) => current.map((t) => (t.id === updated.id ? updated : t)))
  })

  const deleteTask = (task: Task) => run(async () => {
    await api.deleteTask(task.id)
    setTasks((current) => current.filter((t) => t.id !== task.id))
  })

  const createProject = (event: FormEvent) => {
    event.preventDefault()
    const name = newProjectName.trim()
    if (!name) return
    run(async () => {
      const project = await api.createProject(name)
      setProjects((current) => [...current, project].sort((a, b) => a.name.localeCompare(b.name)))
      setNewProjectName('')
      setProjectFilter(project.id)
    })
  }

  return (
    <main className="app">
      <header className="topbar">
        <h1>TaskFlow</h1>
        <div className="toolbar">
          <label>
            Project{' '}
            <select
              aria-label="Filter by project"
              value={projectFilter ?? ''}
              onChange={(e) => setProjectFilter(e.target.value ? Number(e.target.value) : undefined)}
            >
              <option value="">All projects</option>
              {projects.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.name}
                </option>
              ))}
            </select>
          </label>
          <form onSubmit={createProject} className="inline-form" aria-label="Create project">
            <input
              aria-label="New project name"
              placeholder="New project"
              value={newProjectName}
              maxLength={100}
              onChange={(e) => setNewProjectName(e.target.value)}
            />
            <button type="submit">Add</button>
          </form>
        </div>
      </header>

      {error && (
        <div role="alert" className="error">
          {error}
        </div>
      )}

      {projects.length > 0 ? (
        <TaskForm projects={projects} defaultProjectId={projectFilter} onCreate={createTask} />
      ) : (
        <p className="hint">Create a project to start adding tasks.</p>
      )}

      <Board tasks={tasks} projects={projects} onMove={moveTask} onDelete={deleteTask} />
    </main>
  )
}
