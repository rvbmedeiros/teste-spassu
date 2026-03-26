#!/bin/sh
# =============================================================================
# keycloak-init.sh — Configura o realm 'master' com clients e usuários
#
# Executado uma vez pelo serviço keycloak-init após o Keycloak estar saudável.
# Idempotente: cria ou atualiza os recursos necessários da aplicação.
# =============================================================================
set -e

KCADM="/opt/keycloak/bin/kcadm.sh"
SERVER="http://keycloak:8080"
REALM="master"

echo "==> Autenticando no Keycloak como admin..."
$KCADM config credentials \
  --server "$SERVER" \
  --realm master \
  --user "${KEYCLOAK_ADMIN_USER}" \
  --password "${KEYCLOAK_ADMIN_PASSWORD}"

# ─── Roles ────────────────────────────────────────────────────────────────────
echo "==> Criando realm roles..."
for ROLE in ADMIN USER; do
  if $KCADM get roles -r "$REALM" --fields name 2>/dev/null | grep -q "\"$ROLE\""; then
    echo "  Role $ROLE já existe, pulando."
  else
    $KCADM create roles -r "$REALM" -s "name=$ROLE"
    echo "  Role $ROLE criada."
  fi
done

get_client_id() {
  $KCADM get clients -r "$REALM" -q "clientId=$1" -q exact=true --fields id,clientId 2>/dev/null | sed -n 's/.*"id" : "\([^"]*\)".*/\1/p' | head -1
}

get_user_id() {
  $KCADM get users -r "$REALM" -q "username=$1" -q exact=true --fields id,username 2>/dev/null | sed -n 's/.*"id" : "\([^"]*\)".*/\1/p' | head -1
}

FRONTEND_CLIENT_FILE="$(mktemp)"
cat > "$FRONTEND_CLIENT_FILE" <<'EOF'
{
  "clientId": "spassu-frontend",
  "name": "Spassu Frontend",
  "enabled": true,
  "publicClient": true,
  "standardFlowEnabled": true,
  "directAccessGrantsEnabled": false,
  "redirectUris": [
    "http://localhost:3000/*",
    "http://localhost:5173/*"
  ],
  "webOrigins": [
    "http://localhost:3000",
    "http://localhost:5173"
  ],
  "attributes": {
    "pkce.code.challenge.method": "S256"
  }
}
EOF

BFF_CLIENT_FILE="$(mktemp)"
cat > "$BFF_CLIENT_FILE" <<EOF
{
  "clientId": "spassu-bff",
  "name": "Spassu BFF",
  "enabled": true,
  "publicClient": false,
  "secret": "${BFF_CLIENT_SECRET:-spassu-bff-secret-2026}",
  "standardFlowEnabled": false,
  "serviceAccountsEnabled": true,
  "directAccessGrantsEnabled": false
}
EOF

cleanup() {
  rm -f "$FRONTEND_CLIENT_FILE" "$BFF_CLIENT_FILE"
}

trap cleanup EXIT

# ─── Client: spassu-frontend ──────────────────────────────────────────────────
echo "==> Configurando client spassu-frontend..."
FRONTEND_CLIENT_ID="$(get_client_id spassu-frontend)"
if [ -n "$FRONTEND_CLIENT_ID" ]; then
  $KCADM update clients/"$FRONTEND_CLIENT_ID" -r "$REALM" -f "$FRONTEND_CLIENT_FILE"
  echo "  Client spassu-frontend atualizado."
else
  $KCADM create clients -r "$REALM" -f "$FRONTEND_CLIENT_FILE"
  echo "  Client spassu-frontend criado."
fi

# ─── Client: spassu-bff ───────────────────────────────────────────────────────
echo "==> Configurando client spassu-bff..."
BFF_KC_CLIENT_ID="$(get_client_id spassu-bff)"
if [ -n "$BFF_KC_CLIENT_ID" ]; then
  $KCADM update clients/"$BFF_KC_CLIENT_ID" -r "$REALM" -f "$BFF_CLIENT_FILE"
  echo "  Client spassu-bff atualizado."
else
  $KCADM create clients -r "$REALM" -f "$BFF_CLIENT_FILE"
  echo "  Client spassu-bff criado."
fi

# ─── Usuários ─────────────────────────────────────────────────────────────────
ensure_user() {
  USERNAME="$1"
  EMAIL="$2"
  PASSWORD="$3"
  shift 3

  USER_ID="$(get_user_id "$USERNAME")"
  if [ -n "$USER_ID" ]; then
    $KCADM update users/"$USER_ID" -r "$REALM" \
      -s "username=$USERNAME" \
      -s "email=$EMAIL" \
      -s "enabled=true"
    $KCADM set-password -r "$REALM" --username "$USERNAME" --new-password "$PASSWORD" --temporary=false
    echo "  Usuário $USERNAME atualizado."
  else
    $KCADM create users -r "$REALM" \
      -s "username=$USERNAME" \
      -s "email=$EMAIL" \
      -s "enabled=true"
    $KCADM set-password -r "$REALM" --username "$USERNAME" --new-password "$PASSWORD" --temporary=false
    echo "  Usuário $USERNAME criado."
  fi

  for ROLE in "$@"; do
    $KCADM add-roles -r "$REALM" --uusername "$USERNAME" --rolename "$ROLE" >/dev/null 2>&1 || true
  done
}

echo "==> Criando usuários..."
ensure_user "admin"   "admin@spassu.com.br"   "admin123"   ADMIN USER
ensure_user "usuario" "usuario@spassu.com.br" "usuario123" USER

echo "==> Keycloak init concluído com sucesso."
