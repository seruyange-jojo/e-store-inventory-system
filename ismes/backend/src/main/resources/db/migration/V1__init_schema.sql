-- ============================================================
-- Inventory, Sales and Expense Management System
-- PostgreSQL Schema v1.0
-- ============================================================

-- Enable UUID generation if you prefer UUID PKs over serial ints.
-- CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================
-- USERS & AUTH
-- ============================================================
CREATE TABLE users (
    id              SERIAL PRIMARY KEY,
    username        VARCHAR(50) UNIQUE NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(100) NOT NULL,
    email           VARCHAR(100),
    phone           VARCHAR(20),
    role            VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'SALES')),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================================
-- CATEGORIES
-- ============================================================
CREATE TABLE categories (
    id              SERIAL PRIMARY KEY,
    name            VARCHAR(100) UNIQUE NOT NULL,
    description     TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE expense_categories (
    id              SERIAL PRIMARY KEY,
    name            VARCHAR(100) UNIQUE NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================================
-- PRODUCTS / INVENTORY
-- ============================================================
CREATE TABLE products (
    id                SERIAL PRIMARY KEY,
    product_code      VARCHAR(50) UNIQUE NOT NULL,
    name              VARCHAR(150) NOT NULL,
    category_id       INTEGER REFERENCES categories(id),
    description       TEXT,
    unit              VARCHAR(20) NOT NULL DEFAULT 'pcs', -- pcs, box, carton, etc.
    buying_price      NUMERIC(14,2) NOT NULL CHECK (buying_price >= 0),
    selling_price     NUMERIC(14,2) NOT NULL CHECK (selling_price >= 0),
    opening_stock     INTEGER NOT NULL DEFAULT 0,
    current_stock     INTEGER NOT NULL DEFAULT 0 CHECK (current_stock >= 0),
    min_stock_level   INTEGER NOT NULL DEFAULT 5,
    is_active         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_products_code ON products(product_code);
CREATE INDEX idx_products_name ON products(name);
CREATE INDEX idx_products_category ON products(category_id);

-- ============================================================
-- SUPPLIERS
-- ============================================================
CREATE TABLE suppliers (
    id              SERIAL PRIMARY KEY,
    name            VARCHAR(150) NOT NULL,
    contact_person  VARCHAR(100),
    phone           VARCHAR(20),
    email           VARCHAR(100),
    address         TEXT,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================================
-- PURCHASES (Stock IN)
-- ============================================================
CREATE TABLE purchases (
    id              SERIAL PRIMARY KEY,
    supplier_id     INTEGER NOT NULL REFERENCES suppliers(id),
    purchase_date   DATE NOT NULL DEFAULT CURRENT_DATE,
    reference_no    VARCHAR(50),           -- invoice/delivery note number from supplier
    total_amount    NUMERIC(14,2) NOT NULL DEFAULT 0,
    notes           TEXT,
    created_by      INTEGER NOT NULL REFERENCES users(id),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE purchase_items (
    id              SERIAL PRIMARY KEY,
    purchase_id     INTEGER NOT NULL REFERENCES purchases(id) ON DELETE CASCADE,
    product_id      INTEGER NOT NULL REFERENCES products(id),
    quantity        INTEGER NOT NULL CHECK (quantity > 0),
    unit_cost       NUMERIC(14,2) NOT NULL CHECK (unit_cost >= 0),
    subtotal        NUMERIC(14,2) NOT NULL
);

CREATE INDEX idx_purchase_items_purchase ON purchase_items(purchase_id);
CREATE INDEX idx_purchase_items_product ON purchase_items(product_id);

-- ============================================================
-- SALES (Stock OUT)
-- ============================================================
CREATE TABLE sales (
    id                SERIAL PRIMARY KEY,
    receipt_number    VARCHAR(30) UNIQUE NOT NULL,
    sale_date         TIMESTAMP NOT NULL DEFAULT NOW(),
    customer_name     VARCHAR(100),          -- optional, no customer table in v1
    customer_phone    VARCHAR(20),
    payment_method    VARCHAR(20) NOT NULL DEFAULT 'CASH' CHECK (payment_method IN ('CASH','MOBILE_MONEY','BANK','OTHER')),
    total_amount      NUMERIC(14,2) NOT NULL DEFAULT 0,
    created_by        INTEGER NOT NULL REFERENCES users(id),
    created_at        TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE sale_items (
    id                    SERIAL PRIMARY KEY,
    sale_id               INTEGER NOT NULL REFERENCES sales(id) ON DELETE CASCADE,
    product_id            INTEGER NOT NULL REFERENCES products(id),
    quantity              INTEGER NOT NULL CHECK (quantity > 0),
    unit_price            NUMERIC(14,2) NOT NULL CHECK (unit_price >= 0),
    buying_price_at_sale  NUMERIC(14,2) NOT NULL,   -- snapshot for accurate profit calc later
    subtotal              NUMERIC(14,2) NOT NULL,
    is_below_cost         BOOLEAN NOT NULL DEFAULT FALSE,
    authorized_by         INTEGER REFERENCES users(id)  -- admin who approved below-cost sale, NULL otherwise
);

CREATE INDEX idx_sale_items_sale ON sale_items(sale_id);
CREATE INDEX idx_sale_items_product ON sale_items(product_id);

-- ============================================================
-- EXPENSES
-- ============================================================
CREATE TABLE expenses (
    id                  SERIAL PRIMARY KEY,
    expense_category_id INTEGER REFERENCES expense_categories(id),
    description         VARCHAR(255) NOT NULL,
    amount              NUMERIC(14,2) NOT NULL CHECK (amount >= 0),
    expense_date        DATE NOT NULL DEFAULT CURRENT_DATE,
    created_by          INTEGER NOT NULL REFERENCES users(id),
    created_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_expenses_date ON expenses(expense_date);

-- ============================================================
-- NOTIFICATIONS (low stock etc.)
-- ============================================================
CREATE TABLE notifications (
    id              SERIAL PRIMARY KEY,
    type            VARCHAR(30) NOT NULL,      -- LOW_STOCK, BELOW_COST_APPROVAL, etc.
    message         TEXT NOT NULL,
    related_product_id INTEGER REFERENCES products(id),
    is_read         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================================
-- ACTIVITY LOGS (audit trail)
-- ============================================================
CREATE TABLE activity_logs (
    id              SERIAL PRIMARY KEY,
    user_id         INTEGER REFERENCES users(id),
    action          VARCHAR(50) NOT NULL,      -- CREATE, UPDATE, DELETE, LOGIN, etc.
    entity_type     VARCHAR(50) NOT NULL,      -- PRODUCT, SALE, PURCHASE, EXPENSE, USER
    entity_id       INTEGER,
    details         TEXT,
    ip_address      VARCHAR(45),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_activity_logs_user ON activity_logs(user_id);
CREATE INDEX idx_activity_logs_entity ON activity_logs(entity_type, entity_id);

-- ============================================================
-- USEFUL VIEWS
-- ============================================================

-- Daily profit view: (sale revenue - cost of goods sold) - expenses, per day
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

-- Low stock view
CREATE VIEW v_low_stock AS
SELECT id, product_code, name, current_stock, min_stock_level
FROM products
WHERE current_stock <= min_stock_level AND is_active = TRUE;
