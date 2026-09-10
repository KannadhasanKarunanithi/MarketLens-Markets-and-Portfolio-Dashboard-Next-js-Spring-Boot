"use client";

import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { Alert, Instrument } from "@/lib/types";
import { formatCurrency, formatDate } from "@/lib/format";
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

export default function AlertsPage() {
  const queryClient = useQueryClient();
  const [form, setForm] = useState({ instrumentId: "", direction: "ABOVE", threshold: "", note: "" });
  const [error, setError] = useState<string | null>(null);

  const instruments = useQuery({
    queryKey: ["instruments", "tradeable"],
    queryFn: () => api.get<Instrument[]>("/instruments/tradeable"),
  });
  const alerts = useQuery({
    queryKey: ["alerts"],
    queryFn: () => api.get<Alert[]>("/alerts"),
    refetchInterval: 20_000,
  });

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ["alerts"] });
    queryClient.invalidateQueries({ queryKey: ["alerts", "unread"] });
  };

  const create = useMutation({
    mutationFn: () =>
      api.post<Alert>("/alerts", {
        instrumentId: form.instrumentId,
        direction: form.direction,
        threshold: Number(form.threshold),
        note: form.note || null,
      }),
    onSuccess: () => {
      setError(null);
      setForm({ instrumentId: "", direction: "ABOVE", threshold: "", note: "" });
      invalidate();
    },
    onError: (e: Error) => setError(e.message),
  });

  const acknowledge = useMutation({
    mutationFn: (id: string) => api.post<Alert>(`/alerts/${id}/acknowledge`),
    onSuccess: invalidate,
  });
  const remove = useMutation({
    mutationFn: (id: string) => api.delete<void>(`/alerts/${id}`),
    onSuccess: invalidate,
  });

  const rows = alerts.data ?? [];
  const active = rows.filter((a) => a.status === "ACTIVE");
  const triggered = rows.filter((a) => a.status === "TRIGGERED");

  return (
    <div className="space-y-6">
      <PageHeader title="Alerts" subtitle="Get notified when a price crosses a level" />

      <Card>
        <CardHeader title="New alert" />
        <CardBody>
          <form
            className="grid gap-4 md:grid-cols-4"
            onSubmit={(e) => {
              e.preventDefault();
              if (form.instrumentId && form.threshold) create.mutate();
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
                    {i.symbol}
                  </option>
                ))}
              </select>
            </Field>
            <Field label="Trigger when price is">
              <select
                className={inputClass}
                value={form.direction}
                onChange={(e) => setForm({ ...form, direction: e.target.value })}
              >
                <option value="ABOVE">at or above</option>
                <option value="BELOW">at or below</option>
              </select>
            </Field>
            <Field label="Threshold">
              <input
                className={inputClass}
                type="number"
                step="0.01"
                min="0"
                value={form.threshold}
                onChange={(e) => setForm({ ...form, threshold: e.target.value })}
                required
              />
            </Field>
            <Field label="Note (optional)">
              <input
                className={inputClass}
                value={form.note}
                maxLength={200}
                onChange={(e) => setForm({ ...form, note: e.target.value })}
              />
            </Field>
            {error ? (
              <div className="md:col-span-4">
                <ErrorNote message={error} />
              </div>
            ) : null}
            <div className="md:col-span-4">
              <Button type="submit" disabled={create.isPending}>
                {create.isPending ? "Saving" : "Create alert"}
              </Button>
            </div>
          </form>
        </CardBody>
      </Card>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader title="Triggered" subtitle={`${triggered.length} fired`} />
          <CardBody className="space-y-3">
            {alerts.isLoading ? (
              <Spinner />
            ) : triggered.length === 0 ? (
              <EmptyState title="Nothing triggered" />
            ) : (
              triggered.map((alert) => (
                <div
                  key={alert.id}
                  className="rounded border border-line p-3 text-sm"
                >
                  <div className="flex items-center justify-between">
                    <span className="font-medium text-ink">{alert.symbol}</span>
                    <Badge tone={alert.acknowledged ? "slate" : "amber"}>
                      {alert.acknowledged ? "Seen" : "New"}
                    </Badge>
                  </div>
                  <p className="mt-1 text-ink-dim">
                    {alert.direction === "ABOVE" ? "Rose to" : "Fell to"}{" "}
                    {formatCurrency(alert.priceAtTrigger)} on {formatDate(alert.triggeredAt)}
                  </p>
                  <div className="mt-2 flex gap-3 text-xs">
                    {!alert.acknowledged ? (
                      <button
                        onClick={() => acknowledge.mutate(alert.id)}
                        className="text-gold hover:underline"
                      >
                        Mark as seen
                      </button>
                    ) : null}
                    <button
                      onClick={() => remove.mutate(alert.id)}
                      className="text-ink-faint hover:text-down"
                    >
                      Delete
                    </button>
                  </div>
                </div>
              ))
            )}
          </CardBody>
        </Card>

        <Card>
          <CardHeader title="Active" subtitle={`${active.length} watching`} />
          <CardBody className="space-y-3">
            {alerts.isLoading ? (
              <Spinner />
            ) : active.length === 0 ? (
              <EmptyState title="No active alerts" />
            ) : (
              active.map((alert) => (
                <div
                  key={alert.id}
                  className="flex items-center justify-between rounded border border-line p-3 text-sm"
                >
                  <div>
                    <span className="font-medium text-ink">{alert.symbol}</span>
                    <p className="text-ink-dim">
                      {alert.direction === "ABOVE" ? "at or above" : "at or below"}{" "}
                      {formatCurrency(alert.threshold)}
                    </p>
                    {alert.note ? <p className="text-xs text-ink-faint">{alert.note}</p> : null}
                  </div>
                  <button
                    onClick={() => remove.mutate(alert.id)}
                    className="text-xs text-ink-faint hover:text-down"
                  >
                    Delete
                  </button>
                </div>
              ))
            )}
          </CardBody>
        </Card>
      </div>
    </div>
  );
}
