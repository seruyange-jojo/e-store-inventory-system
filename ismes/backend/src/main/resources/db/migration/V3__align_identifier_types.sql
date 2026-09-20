-- Match the Long identifiers used by the JPA entities with the PostgreSQL schema
-- without dropping the existing data or resetting sequences.
-- Flyway runs this migration in a transaction.

DROP VIEW IF EXISTS v_daily_profit;
DROP VIEW IF EXISTS v_low_stock;

ALTER TABLE products DROP CONSTRAINT IF EXISTS products_category_id_fkey;
ALTER TABLE purchases DROP CONSTRAINT IF EXISTS purchases_created_by_fkey;
ALTER TABLE purchases DROP CONSTRAINT IF EXISTS purchases_supplier_id_fkey;
ALTER TABLE sales DROP CONSTRAINT IF EXISTS sales_created_by_fkey;
ALTER TABLE sale_items DROP CONSTRAINT IF EXISTS sale_items_authorized_by_fkey;
ALTER TABLE sale_items DROP CONSTRAINT IF EXISTS sale_items_product_id_fkey;
ALTER TABLE sale_items DROP CONSTRAINT IF EXISTS sale_items_sale_id_fkey;
ALTER TABLE expenses DROP CONSTRAINT IF EXISTS expenses_created_by_fkey;
ALTER TABLE expenses DROP CONSTRAINT IF EXISTS expenses_expense_category_id_fkey;
ALTER TABLE activity_logs DROP CONSTRAINT IF EXISTS activity_logs_user_id_fkey;
ALTER TABLE purchase_items DROP CONSTRAINT IF EXISTS purchase_items_product_id_fkey;
ALTER TABLE purchase_items DROP CONSTRAINT IF EXISTS purchase_items_purchase_id_fkey;
ALTER TABLE notifications DROP CONSTRAINT IF EXISTS notifications_related_product_id_fkey;
ALTER TABLE notifications DROP CONSTRAINT IF EXISTS notifications_type_check;

ALTER TABLE users ALTER COLUMN id TYPE BIGINT;
ALTER TABLE categories ALTER COLUMN id TYPE BIGINT;
ALTER TABLE expense_categories ALTER COLUMN id TYPE BIGINT;
ALTER TABLE suppliers ALTER COLUMN id TYPE BIGINT;
ALTER TABLE products
    ALTER COLUMN id TYPE BIGINT,
    ALTER COLUMN category_id TYPE BIGINT;

ALTER TABLE purchases
    ALTER COLUMN id TYPE BIGINT,
    ALTER COLUMN supplier_id TYPE BIGINT,
    ALTER COLUMN created_by TYPE BIGINT;
ALTER TABLE purchase_items
    ALTER COLUMN id TYPE BIGINT,
    ALTER COLUMN purchase_id TYPE BIGINT,
    ALTER COLUMN product_id TYPE BIGINT;
ALTER TABLE sales
    ALTER COLUMN id TYPE BIGINT,
    ALTER COLUMN created_by TYPE BIGINT;
ALTER TABLE sale_items
    ALTER COLUMN id TYPE BIGINT,
    ALTER COLUMN sale_id TYPE BIGINT,
    ALTER COLUMN product_id TYPE BIGINT,
    ALTER COLUMN authorized_by TYPE BIGINT;
ALTER TABLE expenses
    ALTER COLUMN id TYPE BIGINT,
    ALTER COLUMN expense_category_id TYPE BIGINT,
    ALTER COLUMN created_by TYPE BIGINT;
ALTER TABLE notifications
    ALTER COLUMN id TYPE BIGINT,
    ALTER COLUMN related_product_id TYPE BIGINT;
ALTER TABLE activity_logs
    ALTER COLUMN id TYPE BIGINT,
    ALTER COLUMN user_id TYPE BIGINT,
    ALTER COLUMN entity_id TYPE BIGINT;

ALTER SEQUENCE IF EXISTS users_id_seq AS BIGINT;
ALTER SEQUENCE IF EXISTS categories_id_seq AS BIGINT;
ALTER SEQUENCE IF EXISTS expense_categories_id_seq AS BIGINT;
ALTER SEQUENCE IF EXISTS suppliers_id_seq AS BIGINT;
ALTER SEQUENCE IF EXISTS products_id_seq AS BIGINT;
ALTER SEQUENCE IF EXISTS purchases_id_seq AS BIGINT;
ALTER SEQUENCE IF EXISTS purchase_items_id_seq AS BIGINT;
ALTER SEQUENCE IF EXISTS sales_id_seq AS BIGINT;
ALTER SEQUENCE IF EXISTS sale_items_id_seq AS BIGINT;
ALTER SEQUENCE IF EXISTS expenses_id_seq AS BIGINT;
ALTER SEQUENCE IF EXISTS notifications_id_seq AS BIGINT;
ALTER SEQUENCE IF EXISTS activity_logs_id_seq AS BIGINT;

ALTER TABLE products ADD CONSTRAINT products_category_id_fkey
    FOREIGN KEY (category_id) REFERENCES categories(id);
ALTER TABLE purchases ADD CONSTRAINT purchases_created_by_fkey
    FOREIGN KEY (created_by) REFERENCES users(id);
ALTER TABLE purchases ADD CONSTRAINT purchases_supplier_id_fkey
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id);
ALTER TABLE sales ADD CONSTRAINT sales_created_by_fkey
    FOREIGN KEY (created_by) REFERENCES users(id);
ALTER TABLE sale_items ADD CONSTRAINT sale_items_authorized_by_fkey
    FOREIGN KEY (authorized_by) REFERENCES users(id);
ALTER TABLE sale_items ADD CONSTRAINT sale_items_product_id_fkey
    FOREIGN KEY (product_id) REFERENCES products(id);
ALTER TABLE sale_items ADD CONSTRAINT sale_items_sale_id_fkey
    FOREIGN KEY (sale_id) REFERENCES sales(id);
ALTER TABLE expenses ADD CONSTRAINT expenses_created_by_fkey
    FOREIGN KEY (created_by) REFERENCES users(id);
ALTER TABLE expenses ADD CONSTRAINT expenses_expense_category_id_fkey
    FOREIGN KEY (expense_category_id) REFERENCES expense_categories(id);
ALTER TABLE activity_logs ADD CONSTRAINT activity_logs_user_id_fkey
    FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE purchase_items ADD CONSTRAINT purchase_items_product_id_fkey
    FOREIGN KEY (product_id) REFERENCES products(id);
ALTER TABLE purchase_items ADD CONSTRAINT purchase_items_purchase_id_fkey
    FOREIGN KEY (purchase_id) REFERENCES purchases(id);
ALTER TABLE notifications ADD CONSTRAINT notifications_related_product_id_fkey
    FOREIGN KEY (related_product_id) REFERENCES products(id);

CREATE VIEW v_low_stock AS
SELECT id, product_code, name, current_stock, min_stock_level
FROM products
WHERE current_stock <= min_stock_level AND is_active = TRUE;

CREATE VIEW v_daily_profit AS
SELECT
    d.day,
    COALESCE(sales_total, 0)   AS total_sales,
    COALESCE(cogs_total, 0)    AS cost_of_goods_sold,
    COALESCE(expenses_total, 0) AS total_expenses,
    COALESCE(sales_total, 0) - COALESCE(cogs_total, 0) - COALESCE(expenses_total, 0) AS estimated_profit
FROM (
    SELECT DISTINCT sale_date::date AS day FROM sales
    UNION
    SELECT DISTINCT expense_date AS day FROM expenses
) d
LEFT JOIN (
    SELECT sale_date::date AS day, SUM(subtotal) AS sales_total
    FROM sale_items si JOIN sales s ON si.sale_id = s.id
    GROUP BY sale_date::date
) sales ON sales.day = d.day
LEFT JOIN (
    SELECT sale_date::date AS day, SUM(quantity * buying_price_at_sale) AS cogs_total
    FROM sale_items si JOIN sales s ON si.sale_id = s.id
    GROUP BY sale_date::date
) cogs ON cogs.day = d.day
LEFT JOIN (
    SELECT expense_date AS day, SUM(amount) AS expenses_total
    FROM expenses
    GROUP BY expense_date
) exp ON exp.day = d.day
ORDER BY d.day DESC;
