import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { SearchBar } from './SearchBar'

describe('SearchBar', () => {
  it('shows validation error when submitted with value below 250', async () => {
    render(<SearchBar onSearch={vi.fn()} isLoading={false} />)
    const input = screen.getByRole('spinbutton')
    await userEvent.type(input, '100')
    await userEvent.click(screen.getByRole('button', { name: /search/i }))
    expect(screen.getByText(/at least 250/i)).toBeInTheDocument()
  })

  it('shows validation error when submitted empty', async () => {
    render(<SearchBar onSearch={vi.fn()} isLoading={false} />)
    await userEvent.click(screen.getByRole('button', { name: /search/i }))
    expect(screen.getByText(/at least 250/i)).toBeInTheDocument()
  })

  it('calls onSearch with the entered value when valid', async () => {
    const onSearch = vi.fn()
    render(<SearchBar onSearch={onSearch} isLoading={false} />)
    const input = screen.getByRole('spinbutton')
    await userEvent.type(input, '1000')
    await userEvent.click(screen.getByRole('button', { name: /search/i }))
    expect(onSearch).toHaveBeenCalledWith(1000)
  })

  it('shows loading text when isLoading', () => {
    render(<SearchBar onSearch={vi.fn()} isLoading={true} />)
    expect(screen.getByText(/searching/i)).toBeInTheDocument()
  })

  it('disables button when isLoading', () => {
    render(<SearchBar onSearch={vi.fn()} isLoading={true} />)
    expect(screen.getByRole('button')).toBeDisabled()
  })
})
