#!/usr/bin/env sh
# =============================================================================
# vault-init.sh
#
# Seeds all secrets that the three Spring Boot services (microservice,
# orchestration, bff) need into HashiCorp Vault KV v2.
#
# Usage (runs automatically from docker-compose after Vault starts):
#   ./vault-init.sh
#
# Environment variables expected (set via docker-compose .env.docker):
#   VAULT_ADDR, VAULT_TOKEN
#   POSTGRES_DB, POSTGRES_USER, POSTGRES_PASSWORD
#   RABBITMQ_USER, RABBITMQ_PASSWORD
#   KEYCLOAK_ISSUER_URI
#   BFF_ORIGIN, FRONTEND_ORIGIN
#   BFF_CLIENT_SECRET
# =============================================================================

set -e

echo ">>> Waiting for Vault to be ready..."
until vault status > /dev/null 2>&1; do
  sleep 2
done

echo ">>> Enabling KV v2 secrets engine at secret/"
vault secrets enable -path=secret kv-v2 2>/dev/null || true

# ─── Shared secrets (default-context = application) ──────────────────────────
vault kv put secret/application \
  spassu.keycloak.issuer-uri="${KEYCLOAK_ISSUER_URI}"

# ─── Microservice secrets ────────────────────────────────────────────────────
vault kv put secret/spassu/microservice \
  spring.datasource.url="jdbc:postgresql://postgres:5432/${POSTGRES_DB}" \
  spring.datasource.username="${POSTGRES_USER}" \
  spring.datasource.password="${POSTGRES_PASSWORD}" \
  spring.rabbitmq.host="rabbitmq" \
  spring.rabbitmq.port="5672" \
  spring.rabbitmq.username="${RABBITMQ_USER}" \
  spring.rabbitmq.password="${RABBITMQ_PASSWORD}" \
  spassu.allowed-origins="${BFF_ORIGIN}"

# ─── Orchestration secrets ────────────────────────────────────────────────────
vault kv put secret/spassu/orchestration \
  spring.rabbitmq.host="rabbitmq" \
  spring.rabbitmq.port="5672" \
  spring.rabbitmq.username="${RABBITMQ_USER}" \
  spring.rabbitmq.password="${RABBITMQ_PASSWORD}" \
  spassu.allowed-origins="${BFF_ORIGIN}"

# ─── BFF secrets ──────────────────────────────────────────────────────────────
vault kv put secret/spassu/bff \
  spassu.orchestration.url="http://orchestration:8082" \
  spassu.microservice.url="http://microservice:8081" \
  spassu.allowed-origins="${FRONTEND_ORIGIN}"

echo ">>> Vault seeded successfully."
