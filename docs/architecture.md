# Architecture

## Overview

MarketLens is a two part application:

- a Spring Boot service that exposes a REST API and owns all data and rules
- a Next.js app that talks to that API

PostgreSQL is the only datastore. Flyway manages the schema. There is no message
broker or cache in the current design.

## Backend structure

The backend is organised by feature. Each feature package holds its own
controller, service, repository, entities and DTOs.

```
com.marketlens
  common        cross cutting code: web helpers, and later security, error handling, money
  auth          login, token issue and refresh
  user          accounts and roles
  instrument    the instrument master and search
  marketdata    the market data provider interface, the simulator, ingestion jobs
  quote         latest quotes and the streaming endpoint
  pricebar      historical OHLC storage and the candles endpoint
  watchlist     user watchlists
  transaction   the buy, sell and dividend ledger
  portfolio     holdings, cost basis, profit and loss, allocation, return math
  benchmark     index series for performance comparison
  alert         price alerts and the evaluation job
  audit         a filter that records every state changing request
```

## Market data

Market data comes through a `MarketDataProvider` interface. The default
implementation is a simulator that generates OHLC history and current prices from
a seeded instrument list, so the app runs with no external dependency. A second
implementation can call a real provider when an API key is set. Either way, prices
are stored in `price_bars` and served from there, so the UI never depends on a
live third party call.

## Portfolio math

Holdings are derived from the transaction ledger rather than stored directly.
Cost basis and realised profit use FIFO. Unrealised profit and day change come
from the latest stored quote. The daily portfolio value series is rebuilt from
transactions and price bars, which keeps the performance chart and XIRR
consistent with the ledger.

## Time

The service runs in UTC. Instrument prices and bars are stored with UTC
timestamps. Display formatting and any market calendar logic live in the frontend.
