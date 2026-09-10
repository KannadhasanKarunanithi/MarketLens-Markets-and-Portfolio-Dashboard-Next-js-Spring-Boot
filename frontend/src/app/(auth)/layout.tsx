export default function AuthLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="grid min-h-screen lg:grid-cols-2">
      <div className="relative hidden flex-col justify-between overflow-hidden border-r border-line bg-panel p-12 lg:flex">
        <div
          className="pointer-events-none absolute inset-0 opacity-[0.06]"
          style={{
            backgroundImage:
              "repeating-linear-gradient(0deg, transparent, transparent 27px, #f2b74a 28px), repeating-linear-gradient(90deg, transparent, transparent 27px, #f2b74a 28px)",
          }}
        />
        <div className="relative flex items-center gap-2 text-sm font-semibold">
          <span className="grid h-8 w-8 place-items-center rounded bg-gold text-xs font-bold text-base">
            ML
          </span>
          MarketLens
        </div>
        <div className="relative">
          <h1 className="text-3xl font-semibold leading-tight tracking-tight">
            The terminal for your
            <br />
            markets and portfolio
          </h1>
          <ul className="mt-6 space-y-2 text-sm text-ink-dim">
            <li>Live quotes and interactive price charts</li>
            <li>A portfolio built from your own trade history</li>
            <li>Allocation, returns and price alerts</li>
          </ul>
        </div>
        <p className="relative text-xs text-ink-faint">
          A practice project. Prices are simulated and nothing here is investment advice.
        </p>
      </div>
      <div className="flex items-center justify-center bg-base p-6">
        <div className="w-full max-w-sm">{children}</div>
      </div>
    </div>
  );
}
