"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { cx } from "@/components/ui";

type NavItem = { href: string; label: string; adminOnly?: boolean };

const NAV: NavItem[] = [
  { href: "/dashboard", label: "Dashboard" },
  { href: "/markets", label: "Markets" },
  { href: "/portfolio", label: "Portfolio" },
  { href: "/transactions", label: "Transactions" },
  { href: "/alerts", label: "Alerts" },
  { href: "/audit", label: "Audit log", adminOnly: true },
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
    <div className="flex min-h-screen bg-slate-100">
      <aside className="hidden w-60 shrink-0 flex-col border-r border-slate-200 bg-slate-900 text-slate-300 lg:flex">
        <div className="flex items-center gap-2 px-5 py-5 text-white">
          <span className="grid h-9 w-9 place-items-center rounded-lg bg-blue-600 text-sm font-bold">
            ML
          </span>
          <div>
            <p className="text-sm font-semibold">MarketLens</p>
            <p className="text-xs text-slate-400">Portfolio dashboard</p>
          </div>
        </div>
        <nav className="flex-1 space-y-1 px-3 py-2">
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
                  "flex items-center justify-between rounded-lg px-3 py-2 text-sm font-medium transition",
                  active ? "bg-slate-800 text-white" : "text-slate-300 hover:bg-slate-800/60",
                )}
              >
                <span>{item.label}</span>
                {badge ? (
                  <span className="rounded-full bg-blue-600 px-1.5 py-0.5 text-xs text-white">
                    {badge}
                  </span>
                ) : null}
              </Link>
            );
          })}
        </nav>
        <div className="border-t border-slate-800 px-5 py-4 text-sm">
          <p className="font-medium text-white">{user?.displayName}</p>
          <p className="text-xs text-slate-400">{isAdmin ? "Administrator" : "Investor"}</p>
        </div>
      </aside>

      <div className="flex min-w-0 flex-1 flex-col">
        <header className="flex items-center justify-between border-b border-slate-200 bg-white px-6 py-3">
          <nav className="flex gap-1 lg:hidden">
            {items.map((item) => {
              const active = pathname === item.href;
              return (
                <Link
                  key={item.href}
                  href={item.href}
                  className={cx(
                    "rounded-md px-2 py-1 text-xs font-medium",
                    active ? "bg-slate-900 text-white" : "text-slate-600",
                  )}
                >
                  {item.label}
                </Link>
              );
            })}
          </nav>
          <div className="ml-auto flex items-center gap-3 text-sm">
            <span className="hidden text-slate-500 sm:inline">{user?.username}</span>
            <button
              onClick={logout}
              className="rounded-lg border border-slate-300 px-3 py-1.5 text-sm font-medium text-slate-700 hover:bg-slate-50"
            >
              Sign out
            </button>
          </div>
        </header>
        <main className="mx-auto w-full max-w-6xl flex-1 space-y-6 px-6 py-6">{children}</main>
      </div>
    </div>
  );
}
