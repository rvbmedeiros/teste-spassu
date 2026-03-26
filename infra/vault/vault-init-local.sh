#!/usr/bin/env sh
# =============================================================================
# vault-init-local.sh
#
# Semeia os secrets no Vault para desenvolvimento LOCAL:
# os serviços Spring Boot rodam direto no host (não em Docker), portanto
# os hostnames internos (postgres, rabbitmq, etc.) são substituídos por
# "localhost".
#
# Executado automaticamente pelo serviço vault-init do docker-compose.infra.yml.
# =============================================================================

set -e

echo ">>> Aguardando o Vault estar disponível..."
until vault status > /dev/null 2>&1; do
  sleep 2
done

echo ">>> Habilitando KV v2 em secret/"
vault secrets enable -path=secret kv-v2 2>/dev/null || true

# ─── Secrets compartilhados (default-context = application) ──────────────────
vault kv put secret/application \
  spassu.keycloak.issuer-uri="http://localhost:8080/realms/master"

# ─── Microservice ─────────────────────────────────────────────────────────────
# HOST = localhost pois o Spring Boot roda fora do Docker
vault kv put secret/spassu/microservice \
  spring.datasource.url="jdbc:postgresql://localhost:5432/${POSTGRES_DB}" \
  spring.datasource.username="${POSTGRES_USER}" \
  spring.datasource.password="${POSTGRES_PASSWORD}" \
  spring.rabbitmq.host="localhost" \
  spring.rabbitmq.port="5672" \
  spring.rabbitmq.username="${RABBITMQ_USER}" \
  spring.rabbitmq.password="${RABBITMQ_PASSWORD}" \
  spassu.allowed-origins="${BFF_ORIGIN}"

# ─── Orchestration ────────────────────────────────────────────────────────────
vault kv put secret/spassu/orchestration \
  spring.rabbitmq.host="localhost" \
  spring.rabbitmq.port="5672" \
  spring.rabbitmq.username="${RABBITMQ_USER}" \
  spring.rabbitmq.password="${RABBITMQ_PASSWORD}" \
  spassu.allowed-origins="${BFF_ORIGIN}"

# ─── BFF ──────────────────────────────────────────────────────────────────────
vault kv put secret/spassu/bff \
  spassu.orchestration.url="http://127.0.0.1:8082" \
  spassu.microservice.url="http://127.0.0.1:8081" \
  spassu.allowed-origins="${FRONTEND_ORIGIN}"

echo ">>> Vault semeado com sucesso (modo local)."
