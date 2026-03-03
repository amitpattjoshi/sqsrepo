-- Create tables for e-commerce

CREATE TABLE IF NOT EXISTS products (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS orders (
    id VARCHAR(36) PRIMARY KEY,
    customer_id VARCHAR(36) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    total_amount DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS order_items (
    id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- Insert sample products (use MERGE to avoid duplicates)
MERGE INTO products (id, name, description, price, stock_quantity) KEY(id) VALUES
('prod-001', 'iPhone 15 Pro', 'Latest Apple iPhone', 999.99, 100),
('prod-002', 'MacBook Pro M3', '14-inch MacBook Pro', 1999.99, 50),
('prod-003', 'AirPods Pro', 'Wireless earbuds', 249.99, 200),
('prod-004', 'iPad Air', '10.9-inch iPad Air', 599.99, 75);
