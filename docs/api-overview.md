# API overview

Base path is `/api`. All responses are JSON. Authentication is a bearer access
token; a refresh token is exchanged at `/api/auth/refresh`.

| Method | Path | Access | Purpose |
| --- | --- | --- | --- |
| GET | `/api/meta` | public | service name and version |
| POST | `/api/auth/register` | public | create an account, returns tokens |
| POST | `/api/auth/login` | public | sign in, returns tokens |
| POST | `/api/auth/refresh` | public | exchange a refresh token |
| GET | `/api/auth/me` | user | the current user |
| GET | `/api/instruments` | public | list or search instruments (`q=`) |
| GET | `/api/instruments/tradeable` | public | equities that can be traded |
| GET | `/api/instruments/{id}` | public | one instrument |
| GET | `/api/instruments/{id}/candles` | public | OHLC history (`range=5d..max`) |
| GET | `/api/quotes` | public | latest quotes, all or `symbols=` |
| GET | `/api/quotes/stream` | public | server sent event stream of quote updates |
| GET POST PUT DELETE | `/api/watchlists` | user | manage watchlists and their items |
| GET POST DELETE | `/api/transactions` | user | the trade ledger |
| GET | `/api/portfolio` | user | holdings and summary |
| GET | `/api/portfolio/allocation` | user | allocation by asset class and sector |
| GET | `/api/portfolio/performance` | user | value series, returns and XIRR (`range=`) |
| GET POST DELETE | `/api/alerts` | user | price alerts |
| POST | `/api/alerts/{id}/acknowledge` | user | mark a triggered alert as seen |
| GET | `/api/audit` | admin | the activity trail, paged |
