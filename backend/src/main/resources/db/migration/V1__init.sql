-- Baseline migration. Later migrations add the domain tables.
-- pgcrypto gives us gen_random_uuid() for primary keys.
create extension if not exists pgcrypto;
