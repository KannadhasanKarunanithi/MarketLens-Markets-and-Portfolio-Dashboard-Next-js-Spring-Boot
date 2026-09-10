create table users (
    id            uuid primary key,
    username      varchar(30) not null unique,
    display_name  varchar(80) not null,
    password_hash varchar(100) not null,
    created_at    timestamptz not null
);

create table user_roles (
    user_id uuid not null references users (id) on delete cascade,
    role    varchar(20) not null,
    primary key (user_id, role)
);
