#!/usr/bin/env bash
# Exercise the V1/V2 -> V3 upgrade in an isolated, disposable PostgreSQL 16.
set -euo pipefail

project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
migrations_dir="$project_dir/backend/src/main/resources/db/migration"
container_id=""
cleanup() {
    if [[ -n "$container_id" ]]; then
        docker rm -f "$container_id" >/dev/null
    fi
}
trap cleanup EXIT

container_id="$(docker run --detach --rm --network none \
    --tmpfs /var/lib/postgresql/data \
    -e POSTGRES_HOST_AUTH_METHOD=trust -e POSTGRES_DB=ismes_validation \
    postgres:16-alpine)"

ready=false
for attempt in {1..60}; do
    if docker exec "$container_id" pg_isready -U postgres -d ismes_validation >/dev/null 2>&1; then
        ready=true
        break
    fi
    sleep 1
done
if [[ "$ready" != true ]]; then
    docker logs "$container_id"
    exit 1
fi

psql_check() {
    docker exec -i "$container_id" psql -X -v ON_ERROR_STOP=1 -U postgres -d ismes_validation "$@"
}

psql_check -q < "$migrations_dir/V1__init_schema.sql"
psql_check -q < "$migrations_dir/V2__seed_data.sql"

# Existing data across all nine references must survive the upgrade.
psql_check -q <<'SQL'
INSERT INTO products (product_code, name, category_id, buying_price, selling_price, current_stock)
VALUES ('MIGRATION-CHECK', 'Existing product', 1, 10, 20, 3);
INSERT INTO suppliers (name) VALUES ('Existing supplier');
INSERT INTO purchases (supplier_id, created_by) VALUES (1, 1);
INSERT INTO purchase_items (purchase_id, product_id, quantity, unit_cost, subtotal)
VALUES (1, 1, 2, 10, 20);
INSERT INTO sales (receipt_number, created_by) VALUES ('MIGRATION-CHECK', 1);
INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, buying_price_at_sale, subtotal, authorized_by)
VALUES (1, 1, 1, 20, 10, 20, 1);
INSERT INTO expenses (description, amount, created_by) VALUES ('Existing expense', 5, 1);
INSERT INTO notifications (type, message, related_product_id) VALUES ('LOW_STOCK', 'Existing alert', 1);
INSERT INTO activity_logs (user_id, action, entity_type, entity_id) VALUES (1, 'CREATE', 'PRODUCT', 1);
SELECT setval('users_id_seq', 100), setval('categories_id_seq', 200), setval('products_id_seq', 300);
SQL

# A single transaction matches Flyway's PostgreSQL migration behavior.
psql_check -q --single-transaction < "$migrations_dir/V3__align_identifier_types.sql"

psql_check -q <<'SQL'
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM products WHERE id = 1 AND category_id = 1 AND current_stock = 3)
        OR NOT EXISTS (SELECT 1 FROM users WHERE id = 1 AND username = 'admin')
        OR NOT EXISTS (SELECT 1 FROM purchase_items WHERE product_id = 1 AND purchase_id = 1)
        OR NOT EXISTS (SELECT 1 FROM sale_items WHERE product_id = 1 AND authorized_by = 1)
        OR NOT EXISTS (SELECT 1 FROM expenses WHERE created_by = 1)
        OR NOT EXISTS (SELECT 1 FROM notifications WHERE related_product_id = 1)
        OR NOT EXISTS (SELECT 1 FROM activity_logs WHERE user_id = 1 AND entity_id = 1) THEN
        RAISE EXCEPTION 'Existing data was not preserved';
    END IF;
    IF nextval('users_id_seq') != 101 OR nextval('categories_id_seq') != 201
        OR nextval('products_id_seq') != 301 THEN
        RAISE EXCEPTION 'Sequence positions were reset';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM v_low_stock WHERE id = 1)
        OR NOT EXISTS (SELECT 1 FROM v_daily_profit WHERE estimated_profit = 5) THEN
        RAISE EXCEPTION 'A reporting view is missing or incorrect';
    END IF;
END $$;

-- IDs beyond the INTEGER limit must work end-to-end, including the sequences.
SELECT setval('users_id_seq', 2147483648), setval('categories_id_seq', 2147483648), setval('products_id_seq', 2147483648);
INSERT INTO users (username, password_hash, full_name, role)
VALUES ('large-id-check', 'test-only', 'Large ID check', 'ADMIN');
INSERT INTO categories (name) VALUES ('Large ID check');
INSERT INTO products (product_code, name, category_id, buying_price, selling_price)
VALUES ('LARGE-ID-CHECK', 'Large ID check', 2147483649, 10, 20);
UPDATE purchases SET created_by = 2147483649;
UPDATE sales SET created_by = 2147483649;
UPDATE sale_items SET product_id = 2147483649, authorized_by = 2147483649;
UPDATE purchase_items SET product_id = 2147483649;
UPDATE expenses SET created_by = 2147483649;
UPDATE notifications SET related_product_id = 2147483649;
UPDATE activity_logs SET user_id = 2147483649, entity_id = 2147483649;

DO $$
DECLARE
    orphan_update text;
BEGIN
    FOREACH orphan_update IN ARRAY ARRAY[
        'UPDATE products SET category_id = -1',
        'UPDATE purchases SET created_by = -1',
        'UPDATE sales SET created_by = -1',
        'UPDATE sale_items SET authorized_by = -1',
        'UPDATE sale_items SET product_id = -1',
        'UPDATE purchase_items SET product_id = -1',
        'UPDATE expenses SET created_by = -1',
        'UPDATE notifications SET related_product_id = -1',
        'UPDATE activity_logs SET user_id = -1'
    ] LOOP
        BEGIN
            EXECUTE orphan_update;
            RAISE EXCEPTION 'Foreign key missing: %', orphan_update;
        EXCEPTION WHEN foreign_key_violation THEN
            NULL;
        END;
    END LOOP;
END $$;

DELETE FROM purchases;
DELETE FROM sales;
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM purchase_items) OR EXISTS (SELECT 1 FROM sale_items) THEN
        RAISE EXCEPTION 'Existing item cascade behavior was lost';
    END IF;
END $$;
SQL

printf 'Migration checks passed: existing data, large IDs, sequences, views, foreign keys and cascades.\n'
