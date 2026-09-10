"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { AuditEvent, Page } from "@/lib/types";
import { formatDate, formatTime } from "@/lib/format";
import { Badge, Button, Card, CardBody, CardHeader, PageHeader, Spinner } from "@/components/ui";

export default function AuditPage() {
  const [page, setPage] = useState(0);
  const events = useQuery({
    queryKey: ["audit", page],
    queryFn: () => api.get<Page<AuditEvent>>(`/audit?page=${page}&size=25`),
  });

  const data = events.data;

  return (
    <div className="space-y-6">
      <PageHeader title="Audit log" subtitle="Every state changing request, newest first" />

      <Card>
        <CardHeader
          title={data ? `${data.totalElements} events` : "Events"}
          action={
            <div className="flex gap-2">
              <Button
                variant="secondary"
                disabled={page === 0}
                onClick={() => setPage((p) => Math.max(0, p - 1))}
              >
                Previous
              </Button>
              <Button
                variant="secondary"
                disabled={!data || page + 1 >= data.totalPages}
                onClick={() => setPage((p) => p + 1)}
              >
                Next
              </Button>
            </div>
          }
        />
        <CardBody className="p-0">
          {events.isLoading ? (
            <div className="px-5 py-4">
              <Spinner />
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead className="border-b border-line-soft text-left text-xs uppercase text-ink-dim">
                  <tr>
                    <th className="px-5 py-2 font-medium">When</th>
                    <th className="px-5 py-2 font-medium">Actor</th>
                    <th className="px-5 py-2 font-medium">Action</th>
                    <th className="px-5 py-2 text-right font-medium">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-line-soft">
                  {(data?.content ?? []).map((event) => (
                    <tr key={event.id} className="hover:bg-panel-2">
                      <td className="px-5 py-3 whitespace-nowrap text-ink-dim">
                        {formatDate(event.at)} {formatTime(event.at)}
                      </td>
                      <td className="px-5 py-3">{event.actor}</td>
                      <td className="px-5 py-3">
                        <span className="mr-2 font-medium text-ink">{event.method}</span>
                        <span className="text-xs text-ink-dim">{event.path}</span>
                      </td>
                      <td className="px-5 py-3 text-right">
                        <Badge tone={event.statusCode < 400 ? "green" : "red"}>{event.statusCode}</Badge>
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
