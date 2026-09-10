create table price_bars (
    id            uuid primary key,
    instrument_id uuid not null references instruments (id) on delete cascade,
    bar_date      date not null,
    open          numeric(18, 4) not null,
    high          numeric(18, 4) not null,
    low           numeric(18, 4) not null,
    close         numeric(18, 4) not null,
    volume        bigint not null,
    unique (instrument_id, bar_date)
);

create index idx_price_bars_instrument_date on price_bars (instrument_id, bar_date);

create table quotes (
    instrument_id uuid primary key references instruments (id) on delete cascade,
    last_price    numeric(18, 4) not null,
    prev_close    numeric(18, 4) not null,
    day_open      numeric(18, 4) not null,
    day_high      numeric(18, 4) not null,
    day_low       numeric(18, 4) not null,
    change_abs    numeric(18, 4) not null,
    change_pct    numeric(10, 4) not null,
    as_of         timestamptz not null
);
