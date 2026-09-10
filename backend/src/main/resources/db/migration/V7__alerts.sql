create table alerts (
    id               uuid primary key,
    user_id          uuid not null references users (id) on delete cascade,
    instrument_id    uuid not null references instruments (id) on delete cascade,
    direction        varchar(10) not null,
    threshold        numeric(18, 4) not null,
    status           varchar(12) not null,
    note             varchar(200),
    created_at       timestamptz not null,
    triggered_at     timestamptz,
    price_at_trigger numeric(18, 4),
    acknowledged     boolean not null default false
);

create index idx_alerts_user on alerts (user_id, created_at);
create index idx_alerts_status on alerts (status);
