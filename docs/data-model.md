# Data model

Tables are introduced by Flyway migrations, one milestone at a time. This file
tracks the current schema.

## V1 init

- `pgcrypto` extension for `gen_random_uuid()`
- Flyway keeps its own `flyway_schema_history` table

## Planned tables

- `users`, `user_roles`
- `instruments`
- `price_bars`, `quotes`
- `watchlists`, `watchlist_items`
- `transactions`
- `benchmark_bars`
- `alerts`
- `audit_events`
