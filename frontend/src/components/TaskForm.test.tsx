import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import type { Project } from '../types'
import { TaskForm } from './TaskForm'

const projects: Project[] = [
  { id: 1, name: 'Website', description: null },
  { id: 2, name: 'Mobile', description: null },
]

describe('TaskForm', () => {
  it('disables submit until a title is entered', async () => {
    render(<TaskForm projects={projects} onCreate={vi.fn()} />)
    const submit = screen.getByRole('button', { name: 'Add task' })

    expect(submit).toBeDisabled()
    await userEvent.type(screen.getByLabelText('Title'), '   ')
    expect(submit).toBeDisabled()
  })

  it('submits a trimmed task for the selected project and resets the title', async () => {
    const onCreate = vi.fn().mockResolvedValue(true)
    render(<TaskForm projects={projects} defaultProjectId={1} onCreate={onCreate} />)

    await userEvent.type(screen.getByLabelText('Title'), '  Fix login bug ')
    await userEvent.selectOptions(screen.getByLabelText('Project'), '2')
    await userEvent.selectOptions(screen.getByLabelText('Priority'), 'HIGH')
    await userEvent.click(screen.getByRole('button', { name: 'Add task' }))

    expect(onCreate).toHaveBeenCalledWith({
      projectId: 2,
      title: 'Fix login bug',
      description: undefined,
      priority: 'HIGH',
      dueDate: undefined,
    })
    expect(screen.getByLabelText('Title')).toHaveValue('')
  })

  it('keeps the input when creation fails', async () => {
    render(<TaskForm projects={projects} onCreate={vi.fn().mockResolvedValue(false)} />)

    await userEvent.type(screen.getByLabelText('Title'), 'Retry me')
    await userEvent.click(screen.getByRole('button', { name: 'Add task' }))

    expect(screen.getByLabelText('Title')).toHaveValue('Retry me')
  })
})
