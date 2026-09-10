export default function AuthLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="grid min-h-screen lg:grid-cols-2">
      <div className="hidden flex-col justify-between bg-slate-900 p-12 text-slate-100 lg:flex">
        <div className="flex items-center gap-2 text-lg font-semibold">
          <span className="grid h-9 w-9 place-items-center rounded-lg bg-blue-600 text-sm font-bold">
            ML
          </span>
          MarketLens
        </div>
        <div>
          <h1 className="text-3xl font-semibold leading-tight">
            Track markets and your portfolio in one place
          </h1>
          <ul className="mt-6 space-y-3 text-sm text-slate-300">
            <li>Live quotes and interactive price charts</li>
            <li>A portfolio built from your own trade history</li>
            <li>Allocation, returns and price alerts</li>
          </ul>
        </div>
        <p className="text-xs text-slate-500">
          A practice project. Prices are simulated and nothing here is investment advice.
        </p>
      </div>
      <div className="flex items-center justify-center bg-slate-100 p-6">
        <div className="w-full max-w-sm">{children}</div>
      </div>
    </div>
  );
}
