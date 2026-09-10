export type User = {
  username: string;
  displayName: string;
  roles: string[];
};

export type AuthResponse = {
  accessToken: string;
  refreshToken: string;
  user: User;
};

export type Instrument = {
  id: string;
  symbol: string;
  name: string;
  exchange: string;
  assetClass: string;
  sector: string;
  currency: string;
  referencePrice: number;
  tradeable: boolean;
};

export type Quote = {
  instrumentId: string;
  symbol: string;
  name: string;
  lastPrice: number;
  changeAbs: number;
  changePct: number;
  dayOpen: number;
  dayHigh: number;
  dayLow: number;
  prevClose: number;
  asOf: string;
};

export type Candle = {
  date: string;
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number;
};

export type CandleResponse = {
  range: string;
  candles: Candle[];
};

export type WatchlistItem = {
  instrumentId: string;
  symbol: string;
  name: string;
  sector: string;
  lastPrice: number;
  changeAbs: number;
  changePct: number;
};

export type Watchlist = {
  id: string;
  name: string;
  position: number;
  items: WatchlistItem[];
};

export type Transaction = {
  id: string;
  instrumentId: string;
  symbol: string;
  name: string;
  type: "BUY" | "SELL" | "DIVIDEND";
  quantity: number;
  price: number;
  fees: number;
  grossValue: number;
  tradedOn: string;
  note: string | null;
};

export type Holding = {
  instrumentId: string;
  symbol: string;
  name: string;
  sector: string;
  assetClass: string;
  quantity: number;
  averageCost: number;
  investedValue: number;
  lastPrice: number;
  marketValue: number;
  unrealisedPnl: number;
  unrealisedPnlPct: number;
  dayChange: number;
  portfolioWeight: number;
};

export type PortfolioSummary = {
  investedValue: number;
  marketValue: number;
  unrealisedPnl: number;
  unrealisedPnlPct: number;
  realisedPnl: number;
  dividendIncome: number;
  dayChange: number;
  dayChangePct: number;
  holdingsCount: number;
};

export type Portfolio = {
  summary: PortfolioSummary;
  holdings: Holding[];
};

export type AllocationSlice = { label: string; value: number; weight: number };

export type Allocation = {
  byAssetClass: AllocationSlice[];
  bySector: AllocationSlice[];
};

export type PerformancePoint = {
  date: string;
  portfolioValue: number;
  benchmarkValue: number;
};

export type Performance = {
  series: PerformancePoint[];
  absoluteReturnPct: number;
  benchmarkReturnPct: number;
  xirrPct: number;
};

export type Alert = {
  id: string;
  instrumentId: string;
  symbol: string;
  name: string;
  direction: "ABOVE" | "BELOW";
  threshold: number;
  status: "ACTIVE" | "TRIGGERED";
  note: string | null;
  createdAt: string;
  triggeredAt: string | null;
  priceAtTrigger: number | null;
  acknowledged: boolean;
};

export type AuditEvent = {
  id: string;
  actor: string;
  method: string;
  path: string;
  statusCode: number;
  at: string;
};

export type Page<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
};
