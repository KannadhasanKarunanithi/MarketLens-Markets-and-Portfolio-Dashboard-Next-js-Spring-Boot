create table audit_events (
    id          uuid primary key,
    actor       varchar(60) not null,
    method      varchar(10) not null,
    path        varchar(200) not null,
    status_code int not null,
    at          timestamptz not null
);

create index idx_audit_events_at on audit_events (at desc);
