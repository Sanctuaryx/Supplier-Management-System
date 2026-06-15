interface Props {
  offset: number
  limit: number
  total: number
  onPrev: () => void
  onNext: () => void
}

export function PaginationControls({ offset, limit, total, onPrev, onNext }: Props) {
  const from = total === 0 ? 0 : offset + 1
  const to = Math.min(offset + limit, total)
  const hasPrev = offset > 0
  const hasNext = offset + limit < total

  return (
    <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between border-t border-gray-100 pt-5">
      <span className="text-sm text-gray-400 tracking-[0.15em] uppercase">
        Showing {from}–{to} of {total} suppliers
      </span>
      <div className="flex gap-0">
        <button
          onClick={onPrev}
          disabled={!hasPrev}
          className="px-6 py-2.5 border border-gray-200 text-sm tracking-widest uppercase hover:bg-gray-50 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
        >
          ← Prev
        </button>
        <button
          onClick={onNext}
          disabled={!hasNext}
          className="px-6 py-2.5 border border-l-0 border-gray-200 text-sm tracking-widest uppercase hover:bg-gray-50 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
        >
          Next →
        </button>
      </div>
    </div>
  )
}
