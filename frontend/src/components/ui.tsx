import type { ReactNode } from "react";

export function cx(...classes: (string | false | null | undefined)[]): string {
  return classes.filter(Boolean).join(" ");
}

export function Card({
  children,
  className,
}: {
  children: ReactNode;
  className?: string;
}) {
  return (
    <div className={cx("rounded-md border border-line bg-panel", className)}>{children}</div>
  );
}

export function CardHeader({
  title,
  subtitle,
  action,
}: {
  title: ReactNode;
  subtitle?: ReactNode;
  action?: ReactNode;
}) {
  return (
    <div className="flex items-start justify-between gap-4 border-b border-line-soft px-4 py-3">
      <div>
        <h2 className="text-xs font-semibold uppercase tracking-widest text-ink-dim">{title}</h2>
        {subtitle ? <p className="mt-1 text-xs text-ink-faint">{subtitle}</p> : null}
      </div>
      {action}
    </div>
  );
}

export function CardBody({ children, className }: { children: ReactNode; className?: string }) {
  return <div className={cx("px-4 py-4", className)}>{children}</div>;
}

export function StatCard({
  label,
  value,
  hint,
  tone = "neutral",
}: {
  label: string;
  value: ReactNode;
  hint?: ReactNode;
  tone?: "neutral" | "up" | "down";
}) {
  const toneClass = tone === "up" ? "text-up" : tone === "down" ? "text-down" : "text-ink";
  return (
    <div className="rounded-md border border-line bg-panel p-4">
      <p className="text-[11px] font-medium uppercase tracking-widest text-ink-faint">{label}</p>
      <p className={cx("tnum mt-2 text-xl font-semibold", toneClass)}>{value}</p>
      {hint ? <p className="tnum mt-1 text-xs text-ink-dim">{hint}</p> : null}
    </div>
  );
}

export function Badge({
  children,
  tone = "slate",
}: {
  children: ReactNode;
  tone?: "slate" | "green" | "red" | "amber" | "blue";
}) {
  const tones: Record<string, string> = {
    slate: "border-line bg-panel-2 text-ink-dim",
    green: "border-up/30 bg-up/10 text-up",
    red: "border-down/30 bg-down/10 text-down",
    amber: "border-gold/30 bg-gold/10 text-gold",
    blue: "border-gold/30 bg-gold/10 text-gold",
  };
  return (
    <span
      className={cx(
        "inline-flex items-center rounded border px-1.5 py-0.5 text-[11px] font-medium uppercase tracking-wide",
        tones[tone],
      )}
    >
      {children}
    </span>
  );
}

export function Button({
  children,
  type = "button",
  variant = "primary",
  disabled,
  onClick,
  className,
}: {
  children: ReactNode;
  type?: "button" | "submit";
  variant?: "primary" | "secondary" | "ghost" | "danger";
  disabled?: boolean;
  onClick?: () => void;
  className?: string;
}) {
  const variants: Record<string, string> = {
    primary: "bg-gold text-base hover:bg-gold-dim disabled:opacity-50 font-semibold",
    secondary: "border border-line bg-panel-2 text-ink hover:border-ink-faint",
    ghost: "text-ink-dim hover:bg-panel-2 hover:text-ink",
    danger: "border border-down/40 bg-transparent text-down hover:bg-down/10",
  };
  return (
    <button
      type={type}
      onClick={onClick}
      disabled={disabled}
      className={cx(
        "inline-flex items-center justify-center rounded px-3 py-1.5 text-sm transition disabled:cursor-not-allowed",
        variants[variant],
        className,
      )}
    >
      {children}
    </button>
  );
}

export function PageHeader({
  title,
  subtitle,
  action,
}: {
  title: string;
  subtitle?: string;
  action?: ReactNode;
}) {
  return (
    <div className="flex flex-wrap items-end justify-between gap-3 border-b border-line-soft pb-4">
      <div>
        <h1 className="text-lg font-semibold tracking-tight text-ink">{title}</h1>
        {subtitle ? <p className="mt-1 text-sm text-ink-dim">{subtitle}</p> : null}
      </div>
      {action}
    </div>
  );
}

export function Spinner({ label }: { label?: string }) {
  return (
    <div className="flex items-center gap-2 text-sm text-ink-dim">
      <span className="h-3.5 w-3.5 animate-spin rounded-full border-2 border-line border-t-gold" />
      {label ?? "Loading"}
    </div>
  );
}

export function EmptyState({ title, hint }: { title: string; hint?: string }) {
  return (
    <div className="rounded-md border border-dashed border-line px-5 py-8 text-center">
      <p className="text-sm font-medium text-ink-dim">{title}</p>
      {hint ? <p className="mt-1 text-xs text-ink-faint">{hint}</p> : null}
    </div>
  );
}

export function ErrorNote({ message }: { message: string }) {
  return (
    <div className="rounded-md border border-down/40 bg-down/10 px-4 py-3 text-sm text-down">
      {message}
    </div>
  );
}

export function Field({
  label,
  children,
  error,
}: {
  label: string;
  children: ReactNode;
  error?: string;
}) {
  return (
    <label className="block">
      <span className="mb-1 block text-xs font-medium uppercase tracking-wide text-ink-faint">
        {label}
      </span>
      {children}
      {error ? <span className="mt-1 block text-xs text-down">{error}</span> : null}
    </label>
  );
}

export const inputClass =
  "w-full rounded border border-line bg-base px-3 py-2 text-sm text-ink outline-none placeholder:text-ink-faint focus:border-gold/70";
