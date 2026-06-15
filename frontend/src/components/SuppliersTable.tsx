import type { PotentialSupplier } from '../types/supplier'
import type { SortColumn, SortDirection } from '../utils/filterAndSort'
import { formatTurnover, formatScore } from '../utils/formatters'

interface Props {
  suppliers: PotentialSupplier[]
  isLoading: boolean
  isError: boolean
  errorMessage: string
  hasSearched: boolean
  sortColumn: SortColumn
  sortDirection: SortDirection
  onSort: (column: SortColumn) => void
}

const COLUMNS: { key: SortColumn; label: string; hiddenOnMobile?: boolean }[] = [
  { key: 'duns', label: 'DUNS', hiddenOnMobile: true },
  { key: 'name', label: 'Name' },
  { key: 'country', label: 'Country' },
  { key: 'annualTurnover', label: 'Annual Turnover', hiddenOnMobile: true },
  { key: 'sustainabilityRating', label: 'Rating' },
  { key: 'score', label: 'Score' },
]

function SortIcon({ column, sortColumn, sortDirection }: { column: SortColumn; sortColumn: SortColumn; sortDirection: SortDirection }) {
  if (column !== sortColumn) return <span className="text-gray-300 ml-1">↕</span>
  return <span className="ml-1">{sortDirection === 'asc' ? '↑' : '↓'}</span>
}

export function SuppliersTable({
  suppliers, isLoading, isError, errorMessage, hasSearched,
  sortColumn, sortDirection, onSort,
}: Props) {
  return (
    <div className="overflow-x-auto border border-gray-100">
      <table className="min-w-full text-sm">
        <thead className="bg-black text-white">
          <tr>
            {COLUMNS.map(({ key, label, hiddenOnMobile }) => (
              <th
                key={key}
                onClick={() => onSort(key)}
                className={`px-6 py-4 text-left cursor-pointer select-none text-xs tracking-[0.15em] uppercase font-medium hover:bg-gray-800 whitespace-nowrap transition-colors${hiddenOnMobile ? ' hidden sm:table-cell' : ''}`}
              >
                {label}
                <SortIcon column={key} sortColumn={sortColumn} sortDirection={sortDirection} />
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {isLoading && (
            <tr>
              <td colSpan={COLUMNS.length} className="px-6 py-16 text-center text-gray-400">
                <div className="flex items-center justify-center gap-3">
                  <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-black" />
                  <span className="text-sm tracking-widest uppercase">Loading suppliers…</span>
                </div>
              </td>
            </tr>
          )}
          {!isLoading && isError && (
            <tr>
              <td colSpan={COLUMNS.length} className="px-6 py-16 text-center text-red-500 text-sm tracking-wide">
                {errorMessage || 'An error occurred. Please try again.'}
              </td>
            </tr>
          )}
          {!isLoading && !isError && hasSearched && suppliers.length === 0 && (
            <tr>
              <td colSpan={COLUMNS.length} className="px-6 py-16 text-center text-gray-400 text-sm tracking-widest uppercase">
                No suppliers match the criteria
              </td>
            </tr>
          )}
          {!isLoading && !isError && suppliers.map((s) => (
            <tr key={s.duns} className="border-t border-gray-100 hover:bg-gray-50 transition-colors">
              <td className="px-6 py-4 text-gray-400 text-sm font-mono hidden sm:table-cell">{s.duns}</td>
              <td className="px-6 py-4 font-medium text-gray-900 text-base">{s.name}</td>
              <td className="px-6 py-4">
                <span className="inline-block px-3 py-1 border border-gray-200 text-sm text-gray-600 tracking-wide">
                  {s.country}
                </span>
              </td>
              <td className="px-6 py-4 text-right text-gray-600 font-mono text-sm hidden sm:table-cell">{formatTurnover(s.annualTurnover)}</td>
              <td className="px-6 py-4 text-center">
                <span className={`inline-flex w-8 h-8 text-sm font-bold items-center justify-center mx-auto ${ratingColor(s.sustainabilityRating)}`}>
                  {s.sustainabilityRating}
                </span>
              </td>
              <td className="px-6 py-4 text-right font-mono text-gray-900 font-medium text-base">{formatScore(s.score)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

function ratingColor(r: string): string {
  switch (r) {
    case 'A': return 'bg-emerald-50 text-emerald-700 border border-emerald-200'
    case 'B': return 'bg-teal-50 text-teal-700 border border-teal-200'
    case 'C': return 'bg-amber-50 text-amber-700 border border-amber-200'
    case 'D': return 'bg-orange-50 text-orange-700 border border-orange-200'
    case 'E': return 'bg-rose-50 text-rose-700 border border-rose-200'
    default: return 'bg-gray-50 text-gray-600 border border-gray-200'
  }
}
