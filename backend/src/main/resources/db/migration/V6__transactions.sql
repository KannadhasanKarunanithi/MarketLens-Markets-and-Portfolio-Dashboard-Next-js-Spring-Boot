create table transactions (
    id            uuid primary key,
    user_id       uuid not null references users (id) on delete cascade,
    instrument_id uuid not null references instruments (id),
    type          varchar(10) not null,
    quantity      numeric(18, 4) not null,
    price         numeric(18, 4) not null,
    fees          numeric(18, 2) not null default 0,
    traded_on     date not null,
    note          varchar(200),
    created_at    timestamptz not null
);

create index idx_transactions_user on transactions (user_id, traded_on);
create index idx_transactions_user_instrument on transactions (user_id, instrument_id);
