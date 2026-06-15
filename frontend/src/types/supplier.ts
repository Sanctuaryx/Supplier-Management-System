export type SustainabilityRating = 'A' | 'B' | 'C' | 'D' | 'E'

export interface PotentialSupplier {
  duns: number
  name: string
  country: string
  annualTurnover: number
  sustainabilityRating: SustainabilityRating
  status: 'Active' | 'Disqualified'
  score: number
}

export interface Pagination {
  limit: number
  offset: number
  total: number
}

export interface PotentialSuppliersResponse {
  data: PotentialSupplier[]
  pagination: Pagination
}

export class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message)
    this.name = 'ApiError'
  }
}
