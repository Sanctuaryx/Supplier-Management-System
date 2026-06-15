import { describe, it, expect } from 'vitest'
import type { PotentialSupplier } from '../types/supplier'
import { applyFilters, applySorting } from './filterAndSort'

const suppliers: PotentialSupplier[] = [
  { duns: 111111111, name: 'Acme Corp', country: 'ES', annualTurnover: 2000000, sustainabilityRating: 'A', status: 'Active', score: 200000 },
  { duns: 222222222, name: 'Beta Ltd', country: 'FR', annualTurnover: 1500000, sustainabilityRating: 'B', status: 'Active', score: 112500 },
  { duns: 333333333, name: 'Gamma SA', country: 'ES', annualTurnover: 1000000, sustainabilityRating: 'C', status: 'Active', score: 50000 },
  { duns: 444444444, name: 'Delta GmbH', country: 'DE', annualTurnover: 3000000, sustainabilityRating: 'D', status: 'Active', score: 75000 },
]

describe('applyFilters', () => {
  it('returns all when no filters', () => {
    expect(applyFilters(suppliers, '', [], [])).toHaveLength(4)
  })

  it('filters by name (case insensitive)', () => {
    expect(applyFilters(suppliers, 'acme', [], [])).toHaveLength(1)
    expect(applyFilters(suppliers, 'ACME', [], [])).toHaveLength(1)
  })

  it('filters by DUNS substring', () => {
    expect(applyFilters(suppliers, '1111', [], [])).toHaveLength(1)
  })

  it('filters by country', () => {
    const result = applyFilters(suppliers, '', ['ES'], [])
    expect(result).toHaveLength(2)
    expect(result.every((s) => s.country === 'ES')).toBe(true)
  })

  it('filters by multiple countries', () => {
    expect(applyFilters(suppliers, '', ['ES', 'FR'], [])).toHaveLength(3)
  })

  it('filters by rating', () => {
    expect(applyFilters(suppliers, '', [], ['A', 'B'])).toHaveLength(2)
  })

  it('combines name and country filters', () => {
    expect(applyFilters(suppliers, 'acme', ['ES'], [])).toHaveLength(1)
    expect(applyFilters(suppliers, 'acme', ['FR'], [])).toHaveLength(0)
  })
})

describe('applySorting', () => {
  it('sorts by score desc', () => {
    const result = applySorting(suppliers, 'score', 'desc')
    expect(result[0].score).toBeGreaterThanOrEqual(result[1].score)
  })

  it('sorts by score asc', () => {
    const result = applySorting(suppliers, 'score', 'asc')
    expect(result[0].score).toBeLessThanOrEqual(result[1].score)
  })

  it('sorts by name asc', () => {
    const result = applySorting(suppliers, 'name', 'asc')
    expect(result[0].name).toBe('Acme Corp')
  })

  it('does not mutate the original array', () => {
    const original = [...suppliers]
    applySorting(suppliers, 'annualTurnover', 'asc')
    expect(suppliers).toEqual(original)
  })
})
