import type { SustainabilityRating } from '../types/supplier'

const ALL_RATINGS: SustainabilityRating[] = ['A', 'B', 'C', 'D', 'E']

interface Props {
  nameFilter: string
  onNameChange: (v: string) => void
  availableCountries: string[]
  countryFilter: string[]
  onCountryChange: (v: string[]) => void
  ratingFilter: SustainabilityRating[]
  onRatingChange: (v: SustainabilityRating[]) => void
}

export function FilterBar({
  nameFilter, onNameChange,
  availableCountries, countryFilter, onCountryChange,
  ratingFilter, onRatingChange,
}: Props) {
  function toggleCountry(country: string) {
    onCountryChange(
      countryFilter.includes(country)
        ? countryFilter.filter((c) => c !== country)
        : [...countryFilter, country],
    )
  }

  function toggleRating(rating: SustainabilityRating) {
    onRatingChange(
      ratingFilter.includes(rating)
        ? ratingFilter.filter((r) => r !== rating)
        : [...ratingFilter, rating],
    )
  }

  return (
    <div className="flex flex-wrap gap-8 items-end border-y border-gray-100 py-5">
      <div className="flex flex-col gap-2">
        <label className="text-xs font-semibold text-gray-400 uppercase tracking-[0.15em]">
          Search
        </label>
        <input
          type="text"
          value={nameFilter}
          onChange={(e) => onNameChange(e.target.value)}
          placeholder="Name or DUNS…"
          className="border border-gray-200 px-4 py-2.5 text-sm w-full sm:w-56 focus:outline-none focus:ring-1 focus:ring-black focus:border-black"
        />
      </div>

      {availableCountries.length > 0 && (
        <div className="flex flex-col gap-2">
          <label className="text-xs font-semibold text-gray-400 uppercase tracking-[0.15em]">
            Country
          </label>
          <div className="flex flex-wrap gap-1.5">
            {availableCountries.map((c) => (
              <button
                key={c}
                onClick={() => toggleCountry(c)}
                className={`px-4 py-2 text-sm tracking-wide border transition-colors ${
                  countryFilter.includes(c)
                    ? 'bg-black text-white border-black'
                    : 'bg-white text-gray-600 border-gray-200 hover:border-gray-400'
                }`}
              >
                {c}
              </button>
            ))}
          </div>
        </div>
      )}

      <div className="flex flex-col gap-2">
        <label className="text-xs font-semibold text-gray-400 uppercase tracking-[0.15em]">
          Rating
        </label>
        <div className="flex gap-1.5">
          {ALL_RATINGS.map((r) => (
            <button
              key={r}
              onClick={() => toggleRating(r)}
              className={`w-9 h-9 text-sm font-bold border transition-colors ${
                ratingFilter.includes(r)
                  ? 'bg-black text-white border-black'
                  : 'bg-white text-gray-600 border-gray-200 hover:border-gray-400'
              }`}
            >
              {r}
            </button>
          ))}
        </div>
      </div>

      {(nameFilter || countryFilter.length > 0 || ratingFilter.length > 0) && (
        <button
          onClick={() => { onNameChange(''); onCountryChange([]); onRatingChange([]) }}
          className="text-xs text-gray-400 hover:text-black uppercase tracking-widest underline underline-offset-2 transition-colors"
        >
          Clear
        </button>
      )}
    </div>
  )
}
