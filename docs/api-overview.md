# API overview

Base path is `/api`. All responses are JSON. Endpoints are added milestone by
milestone; this file tracks what exists.

## Available now

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/api/meta` | service name, version and current time |
| GET | `/actuator/health` | health check |

## Planned

- `/api/auth` login, refresh, current user
- `/api/instruments` search and detail
- `/api/instruments/{id}/candles` OHLC history
- `/api/quotes` latest quotes, and a stream
- `/api/watchlists` user watchlists
- `/api/transactions` the trade ledger
- `/api/portfolio` holdings, profit and loss, allocation, performance
- `/api/alerts` price alerts
- `/api/audit` the activity trail
