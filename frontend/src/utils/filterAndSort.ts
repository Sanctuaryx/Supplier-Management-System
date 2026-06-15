import type { PotentialSupplier, SustainabilityRating } from '../types/supplier'

export type SortColumn = keyof PotentialSupplier
export type SortDirection = 'asc' | 'desc'

export function applyFilters(
  suppliers: PotentialSupplier[],
  nameFilter: string,
  countryFilter: string[],
  ratingFilter: SustainabilityRating[],
): PotentialSupplier[] {
  let result = suppliers

  if (nameFilter.trim()) {
    const lower = nameFilter.toLowerCase()
    result = result.filter(
      (s) => s.name.toLowerCase().includes(lower) || String(s.duns).includes(lower),
    )
  }

  if (countryFilter.length > 0) {
    result = result.filter((s) => countryFilter.includes(s.country))
  }

  if (ratingFilter.length > 0) {
    result = result.filter((s) => ratingFilter.includes(s.sustainabilityRating))
  }

  return result
}

export function applySorting(
  suppliers: PotentialSupplier[],
  column: SortColumn,
  direction: SortDirection,
): PotentialSupplier[] {
  return [...suppliers].sort((a, b) => {
    const valA = a[column]
    const valB = b[column]
    let cmp = 0
    if (typeof valA === 'number' && typeof valB === 'number') {
      cmp = valA - valB
    } else {
      cmp = String(valA).localeCompare(String(valB))
    }
    return direction === 'asc' ? cmp : -cmp
  })
}
