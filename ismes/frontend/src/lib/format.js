const currencyFormatter = new Intl.NumberFormat("en-UG", {
  style: "currency",
  currency: "UGX",
  maximumFractionDigits: 0,
});

const numberFormatter = new Intl.NumberFormat("en-UG");

export function formatCurrency(value) {
  if (value === null || value === undefined) return currencyFormatter.format(0);
  return currencyFormatter.format(value);
}

export function formatNumber(value) {
  if (value === null || value === undefined) return "0";
  return numberFormatter.format(value);
}

export function formatDateTime(isoString) {
  if (!isoString) return "—";
  return new Date(isoString).toLocaleString("en-UG", {
    day: "2-digit",
    month: "short",
    hour: "2-digit",
    minute: "2-digit",
  });
}
