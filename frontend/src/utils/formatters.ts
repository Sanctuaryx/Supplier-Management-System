const currencyFormatter = new Intl.NumberFormat('en-GB', {
  style: 'currency',
  currency: 'EUR',
  minimumFractionDigits: 0,
  maximumFractionDigits: 0,
})

export function formatTurnover(value: number): string {
  return currencyFormatter.format(value)
}

export function formatScore(value: number): string {
  return value.toFixed(2)
}
