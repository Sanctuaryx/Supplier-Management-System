import { useMemo, useState } from 'react'
import type { SustainabilityRating } from '../types/supplier'
import type { SortColumn, SortDirection } from '../utils/filterAndSort'
import { applyFilters, applySorting } from '../utils/filterAndSort'
import { usePotentialSuppliers, PAGE_LIMIT } from '../hooks/usePotentialSuppliers'
import { ApiError } from '../types/supplier'
import { SearchBar } from './SearchBar'
import { FilterBar } from './FilterBar'
import { SuppliersTable } from './SuppliersTable'
import { PaginationControls } from './PaginationControls'

export function PotentialSuppliersPage() {
  const [submittedRate, setSubmittedRate] = useState<number | null>(null)
  const [offset, setOffset] = useState(0)
  const [nameFilter, setNameFilter] = useState('')
  const [countryFilter, setCountryFilter] = useState<string[]>([])
  const [ratingFilter, setRatingFilter] = useState<SustainabilityRating[]>([])
  const [sortColumn, setSortColumn] = useState<SortColumn>('score')
  const [sortDirection, setSortDirection] = useState<SortDirection>('desc')

  const { data, isLoading, isError, error } = usePotentialSuppliers(submittedRate, offset)

  const availableCountries = useMemo(
    () => [...new Set((data?.data ?? []).map((s) => s.country))].sort(),
    [data],
  )

  const displayedSuppliers = useMemo(() => {
    const raw = data?.data ?? []
    const filtered = applyFilters(raw, nameFilter, countryFilter, ratingFilter)
    return applySorting(filtered, sortColumn, sortDirection)
  }, [data, nameFilter, countryFilter, ratingFilter, sortColumn, sortDirection])

  function handleSearch(rate: number) {
    setSubmittedRate(rate)
    setOffset(0)
    setNameFilter('')
    setCountryFilter([])
    setRatingFilter([])
    setSortColumn('score')
    setSortDirection('desc')
  }

  function handleSort(column: SortColumn) {
    if (column === sortColumn) {
      setSortDirection((d) => (d === 'asc' ? 'desc' : 'asc'))
    } else {
      setSortColumn(column)
      setSortDirection('desc')
    }
  }

  const errorMessage =
    error instanceof ApiError
      ? `Error ${error.status}: ${error.message}`
      : error instanceof Error
        ? error.message
        : 'Unknown error'

  const pagination = data?.pagination
  const total = pagination?.total ?? 0

  return (
    <div className="w-full">
      <div className="border-b border-gray-100 bg-gray-50 px-8 py-10">
        <h1 className="text-sm tracking-[0.25em] uppercase text-gray-400 mb-2">Dashboard</h1>
        <p className="text-3xl font-light text-gray-900 tracking-tight">Potential Suppliers</p>
        <p className="text-gray-400 text-base mt-3">Find eligible suppliers for a given order amount</p>
        <div className="mt-8">
          <SearchBar onSearch={handleSearch} isLoading={isLoading} />
        </div>
      </div>

      <div className="px-8 py-8 space-y-6">
        {(data || isError) && (
          <FilterBar
            nameFilter={nameFilter}
            onNameChange={setNameFilter}
            availableCountries={availableCountries}
            countryFilter={countryFilter}
            onCountryChange={setCountryFilter}
            ratingFilter={ratingFilter}
            onRatingChange={setRatingFilter}
          />
        )}

        {data && !isLoading && (
          <p className="text-sm text-gray-400 tracking-wide uppercase">
            <span className="text-gray-900 font-semibold">{displayedSuppliers.length}</span> supplier{displayedSuppliers.length !== 1 ? 's' : ''} shown
            {total !== displayedSuppliers.length && ` — ${total} total matching rate`}
          </p>
        )}

        <SuppliersTable
          suppliers={displayedSuppliers}
          isLoading={isLoading}
          isError={isError}
          errorMessage={errorMessage}
          hasSearched={submittedRate !== null}
          sortColumn={sortColumn}
          sortDirection={sortDirection}
          onSort={handleSort}
        />

        {data && total > PAGE_LIMIT && (
          <PaginationControls
            offset={offset}
            limit={PAGE_LIMIT}
            total={total}
            onPrev={() => setOffset((o) => Math.max(0, o - PAGE_LIMIT))}
            onNext={() => setOffset((o) => o + PAGE_LIMIT)}
          />
        )}
      </div>
    </div>
  )
}
