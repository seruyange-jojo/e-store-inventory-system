-- Demo data for local/customer walkthroughs.
-- This migration is applied once by Flyway and does not alter existing records.

INSERT INTO suppliers (name, contact_person, phone, email, address)
SELECT 'Kampala Digital Supplies', 'Grace Namara', '+256 701 234 567', 'orders@kampaladigital.example', 'Nasser Road, Kampala'
WHERE NOT EXISTS (SELECT 1 FROM suppliers WHERE name = 'Kampala Digital Supplies');

INSERT INTO suppliers (name, contact_person, phone, email, address)
SELECT 'East Africa Electronics', 'Peter Okello', '+256 772 345 678', 'sales@eaelectronics.example', 'Industrial Area, Kampala'
WHERE NOT EXISTS (SELECT 1 FROM suppliers WHERE name = 'East Africa Electronics');

INSERT INTO products (product_code, name, category_id, description, unit, buying_price, selling_price, opening_stock, current_stock, min_stock_level)
SELECT 'SAM-A15', 'Samsung Galaxy A15', c.id, '128GB dual-SIM smartphone', 'pcs', 480000, 620000, 12, 8, 4
FROM categories c WHERE c.name = 'Phones'
ON CONFLICT (product_code) DO NOTHING;

INSERT INTO products (product_code, name, category_id, description, unit, buying_price, selling_price, opening_stock, current_stock, min_stock_level)
SELECT 'LG-43UQ', 'LG 43-inch Smart TV', c.id, '4K UHD smart television', 'pcs', 1250000, 1550000, 5, 2, 2
FROM categories c WHERE c.name = 'Televisions'
ON CONFLICT (product_code) DO NOTHING;

INSERT INTO products (product_code, name, category_id, description, unit, buying_price, selling_price, opening_stock, current_stock, min_stock_level)
SELECT 'JBL-FLX5', 'JBL Flip 5 Speaker', c.id, 'Portable Bluetooth speaker', 'pcs', 280000, 375000, 8, 8, 3
FROM categories c WHERE c.name = 'Audio'
ON CONFLICT (product_code) DO NOTHING;

INSERT INTO products (product_code, name, category_id, description, unit, buying_price, selling_price, opening_stock, current_stock, min_stock_level)
SELECT 'ANK-C20', 'Anker USB-C Cable', c.id, 'Durable one-metre fast-charge cable', 'pcs', 25000, 45000, 30, 18, 10
FROM categories c WHERE c.name = 'Cables & Accessories'
ON CONFLICT (product_code) DO NOTHING;

DO $$
DECLARE
    admin_id BIGINT;
    kampala_supplier_id BIGINT;
    east_supplier_id BIGINT;
    phone_id BIGINT;
    tv_id BIGINT;
    speaker_id BIGINT;
    cable_id BIGINT;
    rent_category_id BIGINT;
    transport_category_id BIGINT;
    purchase_id BIGINT;
    sale_id BIGINT;
BEGIN
    SELECT id INTO admin_id FROM users WHERE username = 'admin' LIMIT 1;
    SELECT id INTO kampala_supplier_id FROM suppliers WHERE name = 'Kampala Digital Supplies' LIMIT 1;
    SELECT id INTO east_supplier_id FROM suppliers WHERE name = 'East Africa Electronics' LIMIT 1;
    SELECT id INTO phone_id FROM products WHERE product_code = 'SAM-A15';
    SELECT id INTO tv_id FROM products WHERE product_code = 'LG-43UQ';
    SELECT id INTO speaker_id FROM products WHERE product_code = 'JBL-FLX5';
    SELECT id INTO cable_id FROM products WHERE product_code = 'ANK-C20';
    SELECT id INTO rent_category_id FROM expense_categories WHERE name = 'Rent' LIMIT 1;
    SELECT id INTO transport_category_id FROM expense_categories WHERE name = 'Transport' LIMIT 1;

    IF admin_id IS NULL THEN
        RAISE EXCEPTION 'Demo data requires the seeded admin user';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM purchases WHERE reference_no = 'DEMO-PO-001') THEN
        INSERT INTO purchases (supplier_id, purchase_date, reference_no, total_amount, notes, created_by)
        VALUES (kampala_supplier_id, CURRENT_DATE - 5, 'DEMO-PO-001', 7140000, 'Demo opening stock receipt', admin_id)
        RETURNING id INTO purchase_id;

        INSERT INTO purchase_items (purchase_id, product_id, quantity, unit_cost, subtotal) VALUES
            (purchase_id, phone_id, 12, 480000, 5760000),
            (purchase_id, cable_id, 30, 25000, 750000),
            (purchase_id, speaker_id, 2, 315000, 630000);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM purchases WHERE reference_no = 'DEMO-PO-002') THEN
        INSERT INTO purchases (supplier_id, purchase_date, reference_no, total_amount, notes, created_by)
        VALUES (east_supplier_id, CURRENT_DATE - 2, 'DEMO-PO-002', 6250000, 'Demo television and audio replenishment', admin_id)
        RETURNING id INTO purchase_id;

        INSERT INTO purchase_items (purchase_id, product_id, quantity, unit_cost, subtotal) VALUES
            (purchase_id, tv_id, 5, 1250000, 6250000);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM sales WHERE receipt_number = 'DEMO-REC-001') THEN
        INSERT INTO sales (receipt_number, sale_date, customer_name, customer_phone, payment_method, total_amount, created_by)
        VALUES ('DEMO-REC-001', CURRENT_TIMESTAMP - INTERVAL '2 hours', 'Mirembe Office Solutions', '+256 758 111 222', 'MOBILE_MONEY', 2660000, admin_id)
        RETURNING id INTO sale_id;

        INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, buying_price_at_sale, subtotal, is_below_cost, authorized_by) VALUES
            (sale_id, phone_id, 4, 620000, 480000, 2480000, FALSE, NULL),
            (sale_id, cable_id, 4, 45000, 25000, 180000, FALSE, NULL);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM sales WHERE receipt_number = 'DEMO-REC-002') THEN
        INSERT INTO sales (receipt_number, sale_date, customer_name, payment_method, total_amount, created_by)
        VALUES ('DEMO-REC-002', CURRENT_TIMESTAMP - INTERVAL '1 day', 'Walk-in customer', 'CASH', 375000, admin_id)
        RETURNING id INTO sale_id;

        INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, buying_price_at_sale, subtotal, is_below_cost, authorized_by)
        VALUES (sale_id, speaker_id, 1, 375000, 280000, 375000, FALSE, NULL);
    END IF;

    INSERT INTO expenses (expense_category_id, description, amount, expense_date, created_by)
    SELECT rent_category_id, 'Demo shop rent', 850000, CURRENT_DATE, admin_id
    WHERE NOT EXISTS (SELECT 1 FROM expenses WHERE description = 'Demo shop rent' AND expense_date = CURRENT_DATE);

    INSERT INTO expenses (expense_category_id, description, amount, expense_date, created_by)
    SELECT transport_category_id, 'Demo supplier delivery', 120000, CURRENT_DATE - 1, admin_id
    WHERE NOT EXISTS (SELECT 1 FROM expenses WHERE description = 'Demo supplier delivery' AND expense_date = CURRENT_DATE - 1);
END $$;