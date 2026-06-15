import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { SuppliersTable } from './SuppliersTable'
import type { PotentialSupplier } from '../types/supplier'

const mockSuppliers: PotentialSupplier[] = [
  { duns: 111111111, name: 'Acme Corp', country: 'ES', annualTurnover: 2000000, sustainabilityRating: 'A', status: 'Active', score: 200000 },
]

function renderTable(overrides: Partial<Parameters<typeof SuppliersTable>[0]> = {}) {
  const defaults = {
    suppliers: mockSuppliers,
    isLoading: false,
    isError: false,
    errorMessage: '',
    hasSearched: true,
    sortColumn: 'score' as const,
    sortDirection: 'desc' as const,
    onSort: vi.fn(),
  }
  return render(<SuppliersTable {...defaults} {...overrides} />)
}

describe('SuppliersTable', () => {
  it('shows loading spinner when isLoading', () => {
    renderTable({ isLoading: true, suppliers: [] })
    expect(screen.getByText(/loading/i)).toBeInTheDocument()
  })

  it('shows error message when isError', () => {
    renderTable({ isLoading: false, isError: true, errorMessage: 'Server error', suppliers: [] })
    expect(screen.getByText('Server error')).toBeInTheDocument()
  })

  it('shows empty state when no suppliers and has searched', () => {
    renderTable({ suppliers: [], hasSearched: true })
    expect(screen.getByText(/no suppliers match/i)).toBeInTheDocument()
  })

  it('renders supplier rows with formatted values', () => {
    renderTable()
    expect(screen.getByText('Acme Corp')).toBeInTheDocument()
    expect(screen.getByText('111111111')).toBeInTheDocument()
    expect(screen.getByText('200000.00')).toBeInTheDocument()
  })

  it('calls onSort when header is clicked', async () => {
    const onSort = vi.fn()
    renderTable({ onSort })
    await userEvent.click(screen.getByText(/name/i))
    expect(onSort).toHaveBeenCalledWith('name')
  })

  it('formats turnover with currency symbol', () => {
    renderTable()
    const cell = screen.getByText(/€/)
    expect(cell).toBeInTheDocument()
  })
})
