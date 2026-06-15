import { useQuery } from '@tanstack/react-query'
import { getPotentialSuppliers } from '../api/suppliersApi'

const PAGE_LIMIT = 10

export function usePotentialSuppliers(submittedRate: number | null, offset: number) {
  return useQuery({
    queryKey: ['potential-suppliers', submittedRate, PAGE_LIMIT, offset],
    queryFn: () => getPotentialSuppliers({ rate: submittedRate!, limit: PAGE_LIMIT, offset }),
    enabled: submittedRate !== null && submittedRate >= 250,
  })
}

export { PAGE_LIMIT }
