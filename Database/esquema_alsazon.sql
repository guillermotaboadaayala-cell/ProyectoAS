DROP DATABASE IF EXISTS alsazon_db;
CREATE DATABASE alsazon_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE alsazon_db;

CREATE TABLE Administradores (
    id_admin INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    correo VARCHAR(120) DEFAULT 'admin@alsazon.mx',
    pin_acceso VARCHAR(255) NOT NULL
);

CREATE TABLE Productos (
    id_producto INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    tipo ENUM('Carne', 'Refresco', 'Insumo') NOT NULL,
    unidad_medida ENUM('Kg', 'Unidad', 'Litro') NOT NULL,
    precio_venta DECIMAL(10,2) DEFAULT 0.00
);

CREATE TABLE Lotes (
    id_lote INT AUTO_INCREMENT PRIMARY KEY,
    id_producto INT NOT NULL,
    cantidad_inicial DECIMAL(10,2) NOT NULL CHECK (cantidad_inicial > 0),
    cantidad_actual DECIMAL(10,2) NOT NULL CHECK (cantidad_actual >= 0),
    fecha_ingreso DATE NOT NULL,
    fecha_caducidad DATE NOT NULL,
    costo_total DECIMAL(10,2) NOT NULL CHECK (costo_total >= 0),
    FOREIGN KEY (id_producto) REFERENCES Productos(id_producto)
);

CREATE TABLE Ventas (
    id_venta INT AUTO_INCREMENT PRIMARY KEY,
    fecha_hora DATETIME DEFAULT CURRENT_TIMESTAMP,
    monto_total DECIMAL(10,2) NOT NULL CHECK (monto_total >= 0)
);

CREATE TABLE Detalle_Ventas (
    id_detalle INT AUTO_INCREMENT PRIMARY KEY,
    id_venta INT NOT NULL,
    id_producto INT NOT NULL,
    cantidad_vendida DECIMAL(10,2) NOT NULL CHECK (cantidad_vendida > 0),
    subtotal DECIMAL(10,2) NOT NULL CHECK (subtotal >= 0),
    FOREIGN KEY (id_venta) REFERENCES Ventas(id_venta),
    FOREIGN KEY (id_producto) REFERENCES Productos(id_producto)
);

CREATE TABLE Flujo_Caja (
    id_movimiento INT AUTO_INCREMENT PRIMARY KEY,
    tipo_movimiento ENUM('Ingreso_Venta', 'Gasto_Insumo', 'Retiro_Caja', 'Corte_Z', 'Ajuste') NOT NULL,
    monto DECIMAL(10,2) NOT NULL CHECK (monto >= 0),
    fecha_hora DATETIME DEFAULT CURRENT_TIMESTAMP,
    concepto VARCHAR(255) NOT NULL,
    efectivo_declarado DECIMAL(10,2) DEFAULT NULL,
    descuadre DECIMAL(10,2) DEFAULT NULL
);

-- Unico PIN de acceso: 0691
INSERT INTO Administradores (nombre, correo, pin_acceso) VALUES ('Administrador Al Sazon', 'admin@alsazon.mx', '0691');

-- Productos alineados con el Look & Feel (Carnitas, Manteca y los 4 Refrescos)
INSERT INTO Productos (nombre, tipo, unidad_medida, precio_venta) VALUES 
('Carnitas Maciza / Surtida (Kg)', 'Carne', 'Kg', 280.00),
('Orden de Tacos de Carnitas', 'Carne', 'Unidad', 75.00),
('Coca-Cola 600ml', 'Refresco', 'Unidad', 25.00),
('Del Valle Guayaba', 'Refresco', 'Unidad', 25.00),
('Del Valle Mango', 'Refresco', 'Unidad', 25.00),
('Ciel Mineral', 'Refresco', 'Unidad', 22.00),
('Manteca de Cerdo (Kg)', 'Insumo', 'Kg', 65.00);

INSERT INTO Lotes (id_producto, cantidad_inicial, cantidad_actual, fecha_ingreso, fecha_caducidad, costo_total) VALUES 
(1, 60.00, 42.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 5 DAY), 5200.00),
(3, 60.00, 45.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 12 DAY), 900.00),
(4, 60.00, 12.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 6 DAY), 850.00),
(5, 60.00, 11.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 8 DAY), 850.00),
(6, 60.00, 10.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 15 DAY), 720.00);

INSERT INTO Flujo_Caja (tipo_movimiento, monto, concepto) VALUES 
('Ajuste', 10000.00, 'Presupuesto Inicial de Caja'),
('Gasto_Insumo', 8520.00, 'Compra inicial de lotes de carne y refrescos');

INSERT INTO Ventas (monto_total) VALUES (14200.00);
INSERT INTO Detalle_Ventas (id_venta, id_producto, cantidad_vendida, subtotal) VALUES 
(1, 1, 45.00, 12600.00),
(1, 3, 64.00, 1600.00);
INSERT INTO Flujo_Caja (tipo_movimiento, monto, concepto) VALUES ('Ingreso_Venta', 14200.00, 'Ventas acumuladas iniciales');