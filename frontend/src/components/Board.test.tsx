import { render, screen, within } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import type { Task, TaskStatus } from '../types'
import { Board } from './Board'

const task = (id: number, status: TaskStatus): Task => ({
  id,
  projectId: 1,
  title: `Task ${id}`,
  description: null,
  status,
  priority: 'MEDIUM',
  dueDate: null,
  createdAt: '2030-01-01T10:00:00Z',
  updatedAt: '2030-01-01T10:00:00Z',
})

describe('Board', () => {
  it('groups tasks into the three status columns', () => {
    render(
      <Board
        tasks={[task(1, 'TODO'), task(2, 'DONE'), task(3, 'DONE')]}
        projects={[{ id: 1, name: 'Website', description: null }]}
        onMove={vi.fn()}
        onDelete={vi.fn()}
      />,
    )

    const todo = screen.getByRole('region', { name: /To do/ })
    const inProgress = screen.getByRole('region', { name: /In progress/ })
    const done = screen.getByRole('region', { name: /Done/ })

    expect(within(todo).getAllByRole('article')).toHaveLength(1)
    expect(within(inProgress).getByText('No tasks')).toBeInTheDocument()
    expect(within(done).getAllByRole('article')).toHaveLength(2)
  })
})
