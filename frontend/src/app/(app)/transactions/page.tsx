"use client";

import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { Instrument, Transaction } from "@/lib/types";
import { formatCurrency, formatDate, formatNumber } from "@/lib/format";
import {
  Badge,
  Button,
  Card,
  CardBody,
  CardHeader,
  EmptyState,
  ErrorNote,
  Field,
  PageHeader,
  Spinner,
  inputClass,
} from "@/components/ui";

const today = () => new Date().toISOString().slice(0, 10);

export default function TransactionsPage() {
  const queryClient = useQueryClient();
  const [form, setForm] = useState({
    instrumentId: "",
    type: "BUY",
    quantity: "",
    price: "",
    fees: "0",
    tradedOn: today(),
    note: "",
  });
  const [error, setError] = useState<string | null>(null);

  const instruments = useQuery({
    queryKey: ["instruments", "tradeable"],
    queryFn: () => api.get<Instrument[]>("/instruments/tradeable"),
  });
  const transactions = useQuery({
    queryKey: ["transactions"],
    queryFn: () => api.get<Transaction[]>("/transactions"),
  });

  const create = useMutation({
    mutationFn: () =>
      api.post<Transaction>("/transactions", {
        instrumentId: form.instrumentId,
        type: form.type,
        quantity: Number(form.quantity),
        price: Number(form.price),
        fees: Number(form.fees || 0),
        tradedOn: form.tradedOn,
        note: form.note || null,
      }),
    onSuccess: () => {
      setError(null);
      setForm((f) => ({ ...f, quantity: "", price: "", note: "" }));
      queryClient.invalidateQueries({ queryKey: ["transactions"] });
      queryClient.invalidateQueries({ queryKey: ["portfolio"] });
      queryClient.invalidateQueries({ queryKey: ["allocation"] });
      queryClient.invalidateQueries({ queryKey: ["performance"] });
    },
    onError: (e: Error) => setError(e.message),
  });

  const remove = useMutation({
    mutationFn: (id: string) => api.delete<void>(`/transactions/${id}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["transactions"] });
      queryClient.invalidateQueries({ queryKey: ["portfolio"] });
    },
  });

  const rows = transactions.data ?? [];

  return (
    <div className="space-y-6">
      <PageHeader title="Transactions" subtitle="Your buy, sell and dividend history" />

      <Card>
        <CardHeader title="Record a transaction" />
        <CardBody>
          <form
            className="grid gap-4 md:grid-cols-3"
            onSubmit={(e) => {
              e.preventDefault();
              if (form.instrumentId) create.mutate();
            }}
          >
            <Field label="Instrument">
              <select
                className={inputClass}
                value={form.instrumentId}
                onChange={(e) => setForm({ ...form, instrumentId: e.target.value })}
                required
              >
                <option value="">Select</option>
                {(instruments.data ?? []).map((i) => (
                  <option key={i.id} value={i.id}>
                    {i.symbol} - {i.name}
                  </option>
                ))}
              </select>
            </Field>
            <Field label="Type">
              <select
                className={inputClass}
                value={form.type}
                onChange={(e) => setForm({ ...form, type: e.target.value })}
              >
                <option value="BUY">Buy</option>
                <option value="SELL">Sell</option>
                <option value="DIVIDEND">Dividend</option>
              </select>
            </Field>
            <Field label="Trade date">
              <input
                className={inputClass}
                type="date"
                value={form.tradedOn}
                max={today()}
                onChange={(e) => setForm({ ...form, tradedOn: e.target.value })}
              />
            </Field>
            <Field label="Quantity">
              <input
                className={inputClass}
                type="number"
                step="0.0001"
                min="0"
                value={form.quantity}
                onChange={(e) => setForm({ ...form, quantity: e.target.value })}
                required
              />
            </Field>
            <Field label={form.type === "DIVIDEND" ? "Amount per unit" : "Price per unit"}>
              <input
                className={inputClass}
                type="number"
                step="0.01"
                min="0"
                value={form.price}
                onChange={(e) => setForm({ ...form, price: e.target.value })}
                required
              />
            </Field>
            <Field label="Fees">
              <input
                className={inputClass}
                type="number"
                step="0.01"
                min="0"
                value={form.fees}
                onChange={(e) => setForm({ ...form, fees: e.target.value })}
              />
            </Field>
            <div className="md:col-span-3">
              <Field label="Note (optional)">
                <input
                  className={inputClass}
                  value={form.note}
                  maxLength={200}
                  onChange={(e) => setForm({ ...form, note: e.target.value })}
                />
              </Field>
            </div>
            {error ? (
              <div className="md:col-span-3">
                <ErrorNote message={error} />
              </div>
            ) : null}
            <div className="md:col-span-3">
              <Button type="submit" disabled={create.isPending}>
                {create.isPending ? "Saving" : "Add transaction"}
              </Button>
            </div>
          </form>
        </CardBody>
      </Card>

      <Card>
        <CardHeader title="History" subtitle={`${rows.length} entries`} />
        <CardBody className="p-0">
          {transactions.isLoading ? (
            <div className="px-5 py-4">
              <Spinner />
            </div>
          ) : rows.length === 0 ? (
            <div className="px-5 py-6">
              <EmptyState title="No transactions yet" hint="Record your first trade above." />
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead className="border-b border-line-soft text-left text-xs uppercase text-ink-dim">
                  <tr>
                    <th className="px-5 py-2 font-medium">Date</th>
                    <th className="px-5 py-2 font-medium">Type</th>
                    <th className="px-5 py-2 font-medium">Instrument</th>
                    <th className="px-5 py-2 text-right font-medium">Qty</th>
                    <th className="px-5 py-2 text-right font-medium">Price</th>
                    <th className="px-5 py-2 text-right font-medium">Gross</th>
                    <th className="px-5 py-2" />
                  </tr>
                </thead>
                <tbody className="divide-y divide-line-soft">
                  {rows.map((tx) => (
                    <tr key={tx.id} className="hover:bg-panel-2">
                      <td className="px-5 py-3 whitespace-nowrap">{formatDate(tx.tradedOn)}</td>
                      <td className="px-5 py-3">
                        <Badge
                          tone={tx.type === "BUY" ? "blue" : tx.type === "SELL" ? "amber" : "green"}
                        >
                          {tx.type}
                        </Badge>
                      </td>
                      <td className="px-5 py-3">
                        <span className="font-medium text-ink">{tx.symbol}</span>
                        {tx.note ? <p className="text-xs text-ink-dim">{tx.note}</p> : null}
                      </td>
                      <td className="px-5 py-3 text-right tabular-nums">{formatNumber(tx.quantity)}</td>
                      <td className="px-5 py-3 text-right tabular-nums">{formatCurrency(tx.price)}</td>
                      <td className="px-5 py-3 text-right tabular-nums">{formatCurrency(tx.grossValue)}</td>
                      <td className="px-5 py-3 text-right">
                        <button
                          onClick={() => remove.mutate(tx.id)}
                          className="text-xs text-ink-faint hover:text-down"
                        >
                          Delete
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CardBody>
      </Card>
    </div>
  );
}
