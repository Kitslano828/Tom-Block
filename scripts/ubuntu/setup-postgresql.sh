#!/usr/bin/env bash
set -euo pipefail

if [[ $EUID -ne 0 ]]; then
  echo "Run this script through sudo." >&2
  exit 1
fi

export DEBIAN_FRONTEND=noninteractive
apt-get update
apt-get install -y postgresql postgresql-client
systemctl enable --now postgresql

# The credential is generated on Ubuntu, stored only in a protected systemd
# environment file, and never printed or transported over SSH.
db_password="$(openssl rand -hex 32)"
runuser -u postgres -- psql --set=ON_ERROR_STOP=1 --set=db_password="$db_password" <<'SQL'
SELECT format('CREATE ROLE tomblock LOGIN PASSWORD %L', :'db_password')
WHERE NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'tomblock')\gexec
ALTER ROLE tomblock WITH LOGIN PASSWORD :'db_password';
SELECT 'CREATE DATABASE tomblock OWNER tomblock'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'tomblock')\gexec
ALTER DATABASE tomblock OWNER TO tomblock;
SQL

install -d -m 0750 -o root -g minecraft /etc/tomblock
umask 077
printf 'TOMBLOCK_DB_PASSWORD=%s\n' "$db_password" > /etc/tomblock/build-server.env
chown root:minecraft /etc/tomblock/build-server.env
chmod 0640 /etc/tomblock/build-server.env

install -d -m 0755 /etc/systemd/system/tomblock-build.service.d
cat > /etc/systemd/system/tomblock-build.service.d/database.conf <<'EOF'
[Unit]
After=postgresql.service
Wants=postgresql.service

[Service]
EnvironmentFile=/etc/tomblock/build-server.env
EOF

systemctl daemon-reload
# tom is a member of minecraft and may atomically deploy reviewed plugin builds
# without granting general passwordless sudo access.
chmod 2775 /opt/tomblock/build-server/plugins
chmod 2775 /opt/tomblock/build-server/plugins/TomBlock 2>/dev/null || true
systemctl is-active --quiet postgresql
runuser -u postgres -- psql -Atqc \
  "SELECT datname || ':' || pg_get_userbyid(datdba) FROM pg_database WHERE datname = 'tomblock';"
echo TOMBLOCK_POSTGRES_SETUP_COMPLETE
