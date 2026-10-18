import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import type { Task } from '../types'
import { TaskCard } from './TaskCard'

const task: Task = {
  id: 1,
  projectId: 7,
  title: 'Write release notes',
  description: 'Summarise the sprint',
  status: 'TODO',
  priority: 'HIGH',
  dueDate: '2030-01-31',
  createdAt: '2030-01-01T10:00:00Z',
  updatedAt: '2030-01-01T10:00:00Z',
}

describe('TaskCard', () => {
  it('renders task details', () => {
    render(<TaskCard task={task} projectName="Website" onMove={vi.fn()} onDelete={vi.fn()} />)

    expect(screen.getByRole('heading', { name: 'Write release notes' })).toBeInTheDocument()
    expect(screen.getByText('HIGH')).toBeInTheDocument()
    expect(screen.getByText('Website')).toBeInTheDocument()
    expect(screen.getByText('Due 2030-01-31')).toBeInTheDocument()
  })

  it('only offers moving forward from the first column', async () => {
    const onMove = vi.fn()
    render(<TaskCard task={task} onMove={onMove} onDelete={vi.fn()} />)

    expect(screen.queryByRole('button', { name: 'Move to To do' })).not.toBeInTheDocument()
    await userEvent.click(screen.getByRole('button', { name: 'Move to In progress' }))

    expect(onMove).toHaveBeenCalledWith(task, 'IN_PROGRESS')
  })

  it('offers moving back but not forward from the last column', async () => {
    const onMove = vi.fn()
    render(<TaskCard task={{ ...task, status: 'DONE' }} onMove={onMove} onDelete={vi.fn()} />)

    expect(screen.queryByRole('button', { name: /Move to Done/ })).not.toBeInTheDocument()
    await userEvent.click(screen.getByRole('button', { name: 'Move to In progress' }))

    expect(onMove).toHaveBeenCalledWith(expect.objectContaining({ id: 1 }), 'IN_PROGRESS')
  })
})
