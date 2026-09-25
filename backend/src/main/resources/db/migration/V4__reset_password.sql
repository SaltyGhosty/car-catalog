create table token_reset_password (
    id         bigserial primary key,
    utente_id  bigint      not null references utenti (id) on delete cascade,
    token_hash varchar(64) not null unique,
    scadenza   timestamptz not null,
    usato      boolean     not null default false,
    created_at timestamptz not null
);
create index idx_reset_utente on token_reset_password (utente_id, created_at);
