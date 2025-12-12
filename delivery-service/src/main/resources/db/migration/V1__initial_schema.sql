-- Создание таблицы couriers
CREATE TABLE couriers (
                          id BIGSERIAL PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          phone VARCHAR(20) NOT NULL,
                          status VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE',
                          current_orders_count INT DEFAULT 0,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Создание таблицы deliveries
CREATE TABLE deliveries (
                            id BIGSERIAL PRIMARY KEY,
                            order_id BIGINT NOT NULL UNIQUE,
                            courier_id BIGINT REFERENCES couriers(id),
                            address TEXT NOT NULL,
                            phone VARCHAR(20) NOT NULL,
                            status VARCHAR(50) NOT NULL,
                            estimated_time INT,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Создание таблицы delivery_zones
CREATE TABLE delivery_zones (
                                id BIGSERIAL PRIMARY KEY,
                                name VARCHAR(255) NOT NULL,
                                estimated_minutes INT,
                                delivery_fee DECIMAL(10,2),
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Создание индексов
CREATE INDEX idx_couriers_status ON couriers(status);
CREATE INDEX idx_deliveries_status ON deliveries(status);
CREATE INDEX idx_deliveries_courier_id ON deliveries(courier_id);
CREATE INDEX idx_deliveries_order_id ON deliveries(order_id);
