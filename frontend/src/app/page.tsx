const apiBase = process.env.API_PROXY_TARGET ?? "http://localhost:8090";

type Meta = { name: string; version: string; time: string };

async function loadMeta(): Promise<Meta | null> {
  try {
    const res = await fetch(`${apiBase}/api/meta`, { cache: "no-store" });
    if (!res.ok) return null;
    return (await res.json()) as Meta;
  } catch {
    return null;
  }
}

export default async function Home() {
  const meta = await loadMeta();

  return (
    <main className="mx-auto flex w-full max-w-2xl flex-1 flex-col justify-center gap-6 px-6 py-16">
      <div>
        <h1 className="text-3xl font-semibold tracking-tight">MarketLens</h1>
        <p className="mt-2 text-slate-600">
          Markets and portfolio dashboard. Project scaffold is in place.
        </p>
      </div>

      <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
        <h2 className="text-sm font-medium text-slate-500">Backend</h2>
        {meta ? (
          <dl className="mt-3 grid grid-cols-[auto_1fr] gap-x-6 gap-y-1 text-sm">
            <dt className="text-slate-500">Status</dt>
            <dd className="font-medium text-emerald-600">reachable</dd>
            <dt className="text-slate-500">Service</dt>
            <dd>{meta.name}</dd>
            <dt className="text-slate-500">Version</dt>
            <dd>{meta.version}</dd>
          </dl>
        ) : (
          <p className="mt-3 text-sm text-amber-600">
            Not reachable. Start the API with{" "}
            <code className="rounded bg-slate-100 px-1">./mvnw spring-boot:run</code>{" "}
            in the backend folder.
          </p>
        )}
      </div>
    </main>
  );
}
