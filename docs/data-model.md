# Data model

Tables are introduced by Flyway migrations, one milestone at a time.

| Migration | Tables |
| --- | --- |
| V1 init | pgcrypto extension |
| V2 users | `users`, `user_roles` |
| V3 instruments | `instruments` (seeded with about 45 names) |
| V4 market data | `price_bars`, `quotes` |
| V5 watchlists | `watchlists`, `watchlist_items` |
| V6 transactions | `transactions` |
| V7 alerts | `alerts` |
| V8 audit | `audit_events` |

## Notes

- `instruments.asset_class` is EQUITY, ETF or INDEX. Index instruments are not
  tradeable; the Nifty 50 index is used as the performance benchmark.
- `price_bars` holds one daily OHLC row per instrument per date. `quotes` holds
  the current price and the day's open, high, low and previous close.
- Holdings are not stored. They are derived from `transactions` with first in
  first out cost basis whenever the portfolio is read.
- `audit_events` gets one row per POST, PUT, PATCH or DELETE to `/api`.
