export const STATUSES = ['TODO', 'IN_PROGRESS', 'DONE'] as const
export type TaskStatus = (typeof STATUSES)[number]

export const PRIORITIES = ['LOW', 'MEDIUM', 'HIGH'] as const
export type TaskPriority = (typeof PRIORITIES)[number]

export const STATUS_LABELS: Record<TaskStatus, string> = {
  TODO: 'To do',
  IN_PROGRESS: 'In progress',
  DONE: 'Done',
}

export interface Project {
  id: number
  name: string
  description: string | null
}

export interface Task {
  id: number
  projectId: number
  title: string
  description: string | null
  status: TaskStatus
  priority: TaskPriority
  dueDate: string | null
  createdAt: string
  updatedAt: string
}

export interface NewTask {
  projectId: number
  title: string
  description?: string
  priority: TaskPriority
  dueDate?: string
}

export interface Page<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}
