create table utenti (
    id            bigserial primary key,
    email         varchar(254) not null unique,
    nome          varchar(60)  not null,
    password_hash varchar(100) not null,
    ruolo         varchar(10)  not null check (ruolo in ('USER', 'ADMIN')),
    created_at    timestamptz  not null
);

create table auto (
    id              bigserial primary key,
    marca           varchar(50)   not null,
    modello         varchar(80)   not null,
    anno            integer       not null,
    chilometri      integer       not null check (chilometri >= 0),
    descrizione     varchar(4000) not null,
    prezzo          numeric(12,2) not null check (prezzo > 0),
    prezzo_acquisto numeric(12,2) not null check (prezzo_acquisto >= 0),
    stato           varchar(12)   not null check (stato in ('BOZZA', 'PUBBLICATA')),
    created_at      timestamptz   not null,
    updated_at      timestamptz   not null,
    version         bigint        not null default 0
);
create index idx_auto_stato_prezzo on auto (stato, prezzo);

create table preferiti (
    id         bigserial primary key,
    utente_id  bigint      not null references utenti (id) on delete cascade,
    auto_id    bigint      not null references auto (id) on delete cascade,
    created_at timestamptz not null,
    constraint uk_preferito_utente_auto unique (utente_id, auto_id)
);

create table avvisi (
    id                        bigserial primary key,
    utente_id                 bigint        not null references utenti (id) on delete cascade,
    auto_id                   bigint        not null references auto (id) on delete cascade,
    soglia                    numeric(12,2) not null check (soglia > 0),
    inviato                   boolean       not null default false,
    token_disattivazione_hash varchar(64)   unique,
    created_at                timestamptz   not null,
    constraint uk_avviso_utente_auto unique (utente_id, auto_id)
);
-- Indice parziale: il listener cerca solo gli avvisi non ancora inviati di un'auto
create index idx_avvisi_auto_attivi on avvisi (auto_id) where inviato = false;
