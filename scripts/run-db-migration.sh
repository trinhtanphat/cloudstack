#!/bin/bash
# Database Migration Script for CloudStack Custom Plugins
# Run this script on the CloudStack management server after deploying the plugins.
#
# Usage: ./run-db-migration.sh [mysql_host] [mysql_user] [mysql_password]
# Example: ./run-db-migration.sh localhost cloud cloud
#          ./run-db-migration.sh 127.0.0.1 root mysecretpass

set -e

MYSQL_HOST="${1:-localhost}"
MYSQL_USER="${2:-cloud}"
MYSQL_PASS="${3:-cloud}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CLOUDSTACK_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

BULK_SQL="$CLOUDSTACK_ROOT/plugins/integrations/bulk-service/src/main/resources/db/bulk-service-schema.sql"
DBAAS_SQL="$CLOUDSTACK_ROOT/plugins/integrations/dbaas-service/src/main/resources/db/dbaas-service-schema.sql"

echo "============================================="
echo " CloudStack Custom Plugins - DB Migration"
echo "============================================="
echo "MySQL Host: $MYSQL_HOST"
echo "MySQL User: $MYSQL_USER"
echo ""

run_sql() {
    local sql_file="$1"
    local plugin_name="$2"

    if [ ! -f "$sql_file" ]; then
        echo "[ERROR] SQL file not found: $sql_file"
        return 1
    fi

    echo "[INFO] Running migration for: $plugin_name"
    echo "       SQL file: $sql_file"

    if mysql -h "$MYSQL_HOST" -u "$MYSQL_USER" -p"$MYSQL_PASS" < "$sql_file" 2>&1; then
        echo "[OK] $plugin_name migration completed successfully"
    else
        echo "[ERROR] $plugin_name migration failed!"
        return 1
    fi
    echo ""
}

run_sql "$BULK_SQL" "Bulk Provisioning Service"
run_sql "$DBAAS_SQL" "Database-as-a-Service"

echo "============================================="
echo " Verifying tables..."
echo "============================================="

mysql -h "$MYSQL_HOST" -u "$MYSQL_USER" -p"$MYSQL_PASS" -e "
SELECT 'bulk_provisioning_jobs' AS table_name, COUNT(*) AS row_count FROM cloud.bulk_provisioning_jobs
UNION ALL
SELECT 'dbaas_instances', COUNT(*) FROM cloud.dbaas_instances
UNION ALL
SELECT 'dbaas_backups', COUNT(*) FROM cloud.dbaas_backups;
" 2>&1

echo ""
echo "[DONE] All migrations completed successfully!"
echo "       Restart CloudStack Management Server to activate plugins."
