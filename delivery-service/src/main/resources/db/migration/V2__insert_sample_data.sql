-- Insert sample couriers
INSERT INTO couriers (name, phone, status, current_orders_count) VALUES
('Алмат Сейтжанов', '+77011234567', 'AVAILABLE', 0),
('Нурлан Батыров', '+77012345678', 'AVAILABLE', 0),
('Асем Кенжебекова', '+77013456789', 'AVAILABLE', 0),
('Ерлан Досмухамедов', '+77014567890', 'AVAILABLE', 0),
('Айгуль Токтарбекова', '+77015678901', 'BUSY', 2);

-- Insert sample delivery zones
INSERT INTO delivery_zones (name, delivery_fee, estimated_minutes) VALUES
('Central Almaty', 500.00, 30),
('Medeu District', 700.00, 40),
('Bostandyk District', 600.00, 35),
('Almaly District', 500.00, 30),
('Auezov District', 800.00, 45),
('Turksib District', 900.00, 50);

