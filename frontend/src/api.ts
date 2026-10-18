import type { NewTask, Page, Project, Task, TaskStatus } from './types'

/** Error carrying the RFC 7807 problem detail returned by the API. */
export class ApiError extends Error {
  readonly status: number

  constructor(status: number, message: string) {
    super(message)
    this.status = status
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`/api${path}`, {
    ...init,
    headers: { 'Content-Type': 'application/json', ...init?.headers },
  })
  if (!response.ok) {
    const problem = await response.json().catch(() => null)
    throw new ApiError(response.status, problem?.detail ?? response.statusText)
  }
  return (response.status === 204 ? undefined : await response.json()) as T
}

export const api = {
  listProjects: () => request<Page<Project>>('/projects?size=100').then((page) => page.content),

  createProject: (name: string) =>
    request<Project>('/projects', { method: 'POST', body: JSON.stringify({ name }) }),

  listTasks: (projectId?: number) => {
    const query = new URLSearchParams({ size: '200', sort: 'createdAt,desc' })
    if (projectId !== undefined) query.set('projectId', String(projectId))
    return request<Page<Task>>(`/tasks?${query}`).then((page) => page.content)
  },

  createTask: (task: NewTask) => request<Task>('/tasks', { method: 'POST', body: JSON.stringify(task) }),

  moveTask: (id: number, status: TaskStatus) =>
    request<Task>(`/tasks/${id}/status`, { method: 'PATCH', body: JSON.stringify({ status }) }),

  deleteTask: (id: number) => request<void>(`/tasks/${id}`, { method: 'DELETE' }),
}
