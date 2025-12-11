-- Create couriers table
CREATE INDEX idx_couriers_status ON couriers(status);
CREATE INDEX idx_deliveries_status ON deliveries(status);
CREATE INDEX idx_deliveries_courier_id ON deliveries(courier_id);
CREATE INDEX idx_deliveries_order_id ON deliveries(order_id);
-- Create indexes

);
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    estimated_minutes INT,
    delivery_fee DECIMAL(10,2),
    name VARCHAR(255) NOT NULL,
    id BIGSERIAL PRIMARY KEY,
CREATE TABLE delivery_zones (
-- Create delivery_zones table

);
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estimated_time INT,
    status VARCHAR(50) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    address TEXT NOT NULL,
    courier_id BIGINT REFERENCES couriers(id),
    order_id BIGINT NOT NULL UNIQUE,
    id BIGSERIAL PRIMARY KEY,
CREATE TABLE deliveries (
-- Create deliveries table

);
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    current_orders_count INT DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE',
    phone VARCHAR(20) NOT NULL,
    name VARCHAR(255) NOT NULL,
    id BIGSERIAL PRIMARY KEY,
CREATE TABLE couriers (

