import { FormEvent, useState } from 'react'

interface Props {
  onSearch: (rate: number) => void
  isLoading: boolean
}

export function SearchBar({ onSearch, isLoading }: Props) {
  const [value, setValue] = useState('')
  const [error, setError] = useState('')

  function handleSubmit(e: FormEvent) {
    e.preventDefault()
    const num = Number(value)
    if (!value || isNaN(num) || num < 250) {
      setError('Amount must be at least 250')
      return
    }
    setError('')
    onSearch(num)
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-2 sm:flex-row sm:items-start">
      <div className="flex flex-col gap-1">
        <div className="flex gap-0">
          <input
            type="number"
            value={value}
            onChange={(e) => { setValue(e.target.value); setError('') }}
            placeholder="Enter order amount (min €250)"
            min={250}
            className="border border-gray-300 border-r-0 px-5 py-3.5 w-full sm:w-80 text-base focus:outline-none focus:ring-1 focus:ring-black focus:border-black"
            aria-label="Order amount"
          />
          <button
            type="submit"
            disabled={isLoading}
            className="bg-black text-white px-8 py-3.5 text-sm tracking-widest uppercase hover:bg-gray-800 disabled:opacity-40 disabled:cursor-not-allowed whitespace-nowrap transition-colors"
          >
            {isLoading ? 'Searching…' : 'Search'}
          </button>
        </div>
        {error && <p className="text-red-500 text-sm tracking-wide mt-1">{error}</p>}
      </div>
    </form>
  )
}
