"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { cx } from "@/components/ui";

type NavItem = { href: string; label: string; adminOnly?: boolean };

const NAV: NavItem[] = [
  { href: "/dashboard", label: "Overview" },
  { href: "/markets", label: "Markets" },
  { href: "/portfolio", label: "Portfolio" },
  { href: "/transactions", label: "Ledger" },
  { href: "/alerts", label: "Alerts" },
  { href: "/audit", label: "Audit", adminOnly: true },
];

export function AppShell({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const { user, logout } = useAuth();
  const isAdmin = user?.roles.includes("ADMIN") ?? false;

  const alertCount = useQuery({
    queryKey: ["alerts", "unread"],
    queryFn: () => api.get<{ count: number }>("/alerts/unread-count"),
    refetchInterval: 30_000,
  });

  const items = NAV.filter((item) => !item.adminOnly || isAdmin);

  return (
    <div className="min-h-screen">
      <header className="sticky top-0 z-20 border-b border-line bg-base/95 backdrop-blur">
        <div className="mx-auto flex h-14 max-w-6xl items-center gap-6 px-6">
          <Link href="/dashboard" className="flex items-center gap-2">
            <span className="grid h-7 w-7 place-items-center rounded bg-gold text-xs font-bold text-base">
              ML
            </span>
            <span className="text-sm font-semibold tracking-tight">MarketLens</span>
          </Link>

          <nav className="flex flex-1 items-center gap-1 overflow-x-auto">
            {items.map((item) => {
              const active = pathname === item.href || pathname.startsWith(`${item.href}/`);
              const badge =
                item.href === "/alerts" && (alertCount.data?.count ?? 0) > 0
                  ? alertCount.data?.count
                  : null;
              return (
                <Link
                  key={item.href}
                  href={item.href}
                  className={cx(
                    "relative whitespace-nowrap rounded px-3 py-1.5 text-sm transition",
                    active
                      ? "bg-panel-2 text-ink"
                      : "text-ink-dim hover:bg-panel hover:text-ink",
                  )}
                >
                  {item.label}
                  {badge ? (
                    <span className="ml-1.5 rounded-full bg-gold px-1.5 text-[11px] font-semibold text-base">
                      {badge}
                    </span>
                  ) : null}
                </Link>
              );
            })}
          </nav>

          <div className="flex items-center gap-3 text-sm">
            <span className="hidden text-ink-faint sm:inline">
              {user?.username}
              {isAdmin ? " · admin" : ""}
            </span>
            <button
              onClick={logout}
              className="rounded border border-line px-2.5 py-1 text-xs text-ink-dim hover:border-ink-faint hover:text-ink"
            >
              Sign out
            </button>
          </div>
        </div>
      </header>

      <main className="mx-auto w-full max-w-6xl space-y-6 px-6 py-6">{children}</main>
    </div>
  );
}
