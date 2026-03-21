#!/bin/sh
set -eu

export PGPASSWORD="${POSTGRES_PASSWORD}"

DB_HOST="${POSTGRES_HOST:-postgres}"
DB_PORT="${POSTGRES_PORT:-5432}"
DB_NAME="${POSTGRES_DB}"
DB_USER="${POSTGRES_USER}"
MAX_ATTEMPTS="${MAX_ATTEMPTS:-90}"
SLEEP_SECONDS="${SLEEP_SECONDS:-2}"

TABLES_QUERY="SELECT CASE WHEN to_regclass('public.autor') IS NOT NULL AND to_regclass('public.livro') IS NOT NULL AND to_regclass('public.livro_autor') IS NOT NULL AND to_regclass('public.assunto') IS NOT NULL AND to_regclass('public.livro_assunto') IS NOT NULL THEN 1 ELSE 0 END;"

attempt=1
while [ "$attempt" -le "$MAX_ATTEMPTS" ]; do
  if psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -tAc "$TABLES_QUERY" | grep -qx "1"; then
    echo "[postgres-view-init] Tabelas base detectadas. Aplicando view de relatorio..."
    psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -v ON_ERROR_STOP=1 -f /scripts/init-relatorio-view.sql
    echo "[postgres-view-init] View vw_relatorio_livros pronta."
    exit 0
  fi

  echo "[postgres-view-init] Aguardando tabelas do Hibernate (tentativa ${attempt}/${MAX_ATTEMPTS})..."
  attempt=$((attempt + 1))
  sleep "$SLEEP_SECONDS"
done

echo "[postgres-view-init] Timeout aguardando tabelas base."
exit 1
