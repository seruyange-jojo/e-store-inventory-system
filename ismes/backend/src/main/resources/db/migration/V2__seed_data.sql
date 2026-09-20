-- NOTE: This inserts a placeholder admin with a bcrypt hash for password "admin123".
-- CHANGE THIS PASSWORD immediately after first login in production.
-- Hash below is bcrypt("admin123") — verify at login via Spring Security's BCryptPasswordEncoder.
INSERT INTO users (username, password_hash, full_name, role, is_active)
VALUES (
    'admin',
    '$2b$10$MqJnsBdG3MAxvAy6zKHKs.PlGltRJaZbdRjIF7UOHIaovr5XFdeFi', -- bcrypt hash of "admin123" -- CHANGE AFTER FIRST LOGIN
    'System Administrator',
    'ADMIN',
    TRUE
)
ON CONFLICT (username) DO NOTHING;

INSERT INTO categories (name, description) VALUES
    ('Phones', 'Mobile phones and accessories'),
    ('Televisions', 'TVs and home entertainment'),
    ('Audio', 'Speakers, headphones, sound systems'),
    ('Cables & Accessories', 'Cables, chargers, adapters')
ON CONFLICT (name) DO NOTHING;

INSERT INTO expense_categories (name) VALUES
    ('Rent'), ('Utilities'), ('Transport'), ('Salaries'), ('Miscellaneous')
ON CONFLICT (name) DO NOTHING;
