import type { PotentialSuppliersResponse } from '../types/supplier'
import { ApiError } from '../types/supplier'

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? ''

export async function getPotentialSuppliers(params: {
  rate: number
  limit: number
  offset: number
}): Promise<PotentialSuppliersResponse> {
  const url = `${BASE_URL}/suppliers/potential?rate=${params.rate}&limit=${params.limit}&offset=${params.offset}`
  const res = await fetch(url)
  if (!res.ok) {
    const body = await res.json().catch(() => ({ info: res.statusText }))
    throw new ApiError(res.status, body.info ?? 'Request failed')
  }
  return res.json()
}
