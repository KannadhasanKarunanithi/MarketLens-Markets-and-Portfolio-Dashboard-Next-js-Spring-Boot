const inr = new Intl.NumberFormat("en-IN", {
  style: "currency",
  currency: "INR",
  maximumFractionDigits: 2,
});

const inrCompact = new Intl.NumberFormat("en-IN", {
  style: "currency",
  currency: "INR",
  notation: "compact",
  maximumFractionDigits: 2,
});

const plainNumber = new Intl.NumberFormat("en-IN", { maximumFractionDigits: 2 });

export function formatCurrency(value: number | null | undefined): string {
  if (value === null || value === undefined) return "-";
  return inr.format(value);
}

export function formatCurrencyCompact(value: number | null | undefined): string {
  if (value === null || value === undefined) return "-";
  return inrCompact.format(value);
}

export function formatNumber(value: number | null | undefined): string {
  if (value === null || value === undefined) return "-";
  return plainNumber.format(value);
}

export function formatSignedCurrency(value: number): string {
  const sign = value > 0 ? "+" : value < 0 ? "-" : "";
  return `${sign}${inr.format(Math.abs(value))}`;
}

export function formatPercent(value: number | null | undefined): string {
  if (value === null || value === undefined) return "-";
  const sign = value > 0 ? "+" : "";
  return `${sign}${value.toFixed(2)}%`;
}

export function formatDate(value: string | null | undefined): string {
  if (!value) return "-";
  return new Date(value).toLocaleDateString("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  });
}

export function formatTime(value: string | null | undefined): string {
  if (!value) return "-";
  return new Date(value).toLocaleTimeString("en-IN", {
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
  });
}

export function trendClass(value: number): string {
  if (value > 0) return "text-up";
  if (value < 0) return "text-down";
  return "text-ink-dim";
}
