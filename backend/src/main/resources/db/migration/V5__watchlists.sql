create table watchlists (
    id         uuid primary key,
    user_id    uuid not null references users (id) on delete cascade,
    name       varchar(60) not null,
    position   int not null default 0,
    created_at timestamptz not null
);

create index idx_watchlists_user on watchlists (user_id);

create table watchlist_items (
    id            uuid primary key,
    watchlist_id  uuid not null references watchlists (id) on delete cascade,
    instrument_id uuid not null references instruments (id) on delete cascade,
    position      int not null default 0,
    added_at      timestamptz not null,
    unique (watchlist_id, instrument_id)
);

create index idx_watchlist_items_list on watchlist_items (watchlist_id);
