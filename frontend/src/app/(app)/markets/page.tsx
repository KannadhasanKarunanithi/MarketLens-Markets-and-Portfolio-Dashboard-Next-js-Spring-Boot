"use client";

import Link from "next/link";
import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import { useQuoteStream } from "@/lib/useQuoteStream";
import type { Instrument, Watchlist } from "@/lib/types";
import { formatCurrency, formatPercent, trendClass } from "@/lib/format";
import {
  Button,
  Card,
  CardBody,
  CardHeader,
  EmptyState,
  ErrorNote,
  PageHeader,
  Spinner,
  cx,
  inputClass,
} from "@/components/ui";

export default function MarketsPage() {
  const queryClient = useQueryClient();
  const [selectedId, setSelectedId] = useState<string | null>(null);
  const [newListName, setNewListName] = useState("");
  const [search, setSearch] = useState("");
  const [error, setError] = useState<string | null>(null);

  const live = useQuoteStream();

  const watchlists = useQuery({
    queryKey: ["watchlists"],
    queryFn: () => api.get<Watchlist[]>("/watchlists"),
  });

  const results = useQuery({
    queryKey: ["instrument-search", search],
    queryFn: () => api.get<Instrument[]>(`/instruments?q=${encodeURIComponent(search)}`),
    enabled: search.trim().length >= 2,
  });

  const lists = useMemo(() => watchlists.data ?? [], [watchlists.data]);
  const selected = useMemo(
    () => lists.find((l) => l.id === selectedId) ?? lists[0],
    [lists, selectedId],
  );

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ["watchlists"] });

  const createList = useMutation({
    mutationFn: (name: string) => api.post<Watchlist>("/watchlists", { name }),
    onSuccess: (list) => {
      setNewListName("");
      setSelectedId(list.id);
      invalidate();
    },
    onError: (e: Error) => setError(e.message),
  });

  const addItem = useMutation({
    mutationFn: (instrumentId: string) =>
      api.post<Watchlist>(`/watchlists/${selected.id}/items`, { instrumentId }),
    onSuccess: () => {
      setSearch("");
      invalidate();
    },
    onError: (e: Error) => setError(e.message),
  });

  const removeItem = useMutation({
    mutationFn: (instrumentId: string) =>
      api.delete<Watchlist>(`/watchlists/${selected.id}/items/${instrumentId}`),
    onSuccess: invalidate,
    onError: (e: Error) => setError(e.message),
  });

  const deleteList = useMutation({
    mutationFn: (id: string) => api.delete<void>(`/watchlists/${id}`),
    onSuccess: () => {
      setSelectedId(null);
      invalidate();
    },
  });

  return (
    <div className="space-y-6">
      <PageHeader title="Markets" subtitle="Watchlists with live prices" />

      {error ? <ErrorNote message={error} /> : null}

      <div className="flex flex-wrap items-center gap-2">
        {lists.map((list) => (
          <button
            key={list.id}
            onClick={() => setSelectedId(list.id)}
            className={cx(
              "rounded px-3 py-1.5 text-sm font-medium",
              selected?.id === list.id
                ? "bg-panel-2 text-ink"
                : "border border-line bg-panel text-ink hover:bg-panel-2",
            )}
          >
            {list.name}
            <span className="ml-1.5 text-xs text-ink-faint">{list.items.length}</span>
          </button>
        ))}
        <form
          className="flex items-center gap-2"
          onSubmit={(e) => {
            e.preventDefault();
            if (newListName.trim()) createList.mutate(newListName.trim());
          }}
        >
          <input
            className={cx(inputClass, "w-40")}
            placeholder="New watchlist"
            value={newListName}
            onChange={(e) => setNewListName(e.target.value)}
          />
          <Button type="submit" variant="secondary" disabled={createList.isPending}>
            Add
          </Button>
        </form>
      </div>

      {watchlists.isLoading ? (
        <Spinner label="Loading watchlists" />
      ) : !selected ? (
        <EmptyState title="No watchlists yet" hint="Create one above to start tracking prices." />
      ) : (
        <div className="grid gap-6 lg:grid-cols-[1fr_320px]">
          <Card>
            <CardHeader
              title={selected.name}
              subtitle={`${selected.items.length} instruments`}
              action={
                <Button
                  variant="danger"
                  onClick={() => deleteList.mutate(selected.id)}
                  disabled={deleteList.isPending}
                >
                  Delete list
                </Button>
              }
            />
            <CardBody className="p-0">
              {selected.items.length === 0 ? (
                <div className="px-5 py-6">
                  <EmptyState title="Nothing here yet" hint="Search on the right to add instruments." />
                </div>
              ) : (
                <table className="w-full text-sm">
                  <thead className="border-b border-line-soft text-left text-xs uppercase text-ink-dim">
                    <tr>
                      <th className="px-5 py-2 font-medium">Symbol</th>
                      <th className="px-5 py-2 text-right font-medium">Last</th>
                      <th className="px-5 py-2 text-right font-medium">Change</th>
                      <th className="px-5 py-2" />
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-line-soft">
                    {selected.items.map((item) => {
                      const liveQuote = live[item.symbol];
                      const price = liveQuote?.lastPrice ?? item.lastPrice;
                      const changePct = liveQuote?.changePct ?? item.changePct;
                      return (
                        <tr key={item.instrumentId} className="hover:bg-panel-2">
                          <td className="px-5 py-3">
                            <Link
                              href={`/instruments/${item.instrumentId}`}
                              className="font-medium text-ink hover:text-gold"
                            >
                              {item.symbol}
                            </Link>
                            <p className="text-xs text-ink-dim">{item.sector}</p>
                          </td>
                          <td className="px-5 py-3 text-right tabular-nums">
                            {formatCurrency(price)}
                          </td>
                          <td className={cx("px-5 py-3 text-right tabular-nums", trendClass(changePct))}>
                            {formatPercent(changePct)}
                          </td>
                          <td className="px-5 py-3 text-right">
                            <button
                              onClick={() => removeItem.mutate(item.instrumentId)}
                              className="text-xs text-ink-faint hover:text-down"
                            >
                              Remove
                            </button>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              )}
            </CardBody>
          </Card>

          <Card className="h-fit">
            <CardHeader title="Add instrument" subtitle="Search by symbol or name" />
            <CardBody className="space-y-3">
              <input
                className={inputClass}
                placeholder="e.g. HDFC or TCS"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
              <ul className="max-h-80 space-y-1 overflow-y-auto">
                {(results.data ?? []).slice(0, 20).map((instrument) => (
                  <li key={instrument.id}>
                    <button
                      onClick={() => addItem.mutate(instrument.id)}
                      className="flex w-full items-center justify-between rounded px-2 py-2 text-left text-sm hover:bg-panel-2"
                    >
                      <span>
                        <span className="font-medium text-ink">{instrument.symbol}</span>
                        <span className="ml-2 text-xs text-ink-dim">{instrument.name}</span>
                      </span>
                      <span className="text-xs text-gold">Add</span>
                    </button>
                  </li>
                ))}
              </ul>
            </CardBody>
          </Card>
        </div>
      )}
    </div>
  );
}
