-- BASES DE DATOS II - MODO SHAKE
-- Script consolidado de los avances 1 y 2
-- MySQL 8.0.16 o posterior. DBeaver: ejecutar por bloques completos con Ctrl + Enter.
-- Instalacion inicial: base, tablas y datos; luego rutinas, triggers y pruebas.
-- No volver a ejecutar la carga inicial sobre una base con los triggers instalados.
-- La prueba final provoca un error intencional; ejecutar despues su SELECT de verificacion.

-- AVANCE 1
CREATE DATABASE IF NOT EXISTS modo_shake
  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

USE modo_shake;

-- Categorías
CREATE TABLE IF NOT EXISTS categorias (
  id_categoria INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(60) NOT NULL UNIQUE,
  descripcion VARCHAR(180)
) ENGINE=InnoDB;

-- Clientes
CREATE TABLE IF NOT EXISTS clientes (
  id_cliente INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(60) NOT NULL,
  telefono VARCHAR(20),
  correo VARCHAR(120) UNIQUE
) ENGINE=InnoDB;

-- Perfiles del sistema
CREATE TABLE IF NOT EXISTS roles (
  id_rol TINYINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(20) NOT NULL UNIQUE,
  descripcion VARCHAR(150),
  CONSTRAINT ck_roles_nombre
    CHECK (nombre IN ('ADMIN', 'USUARIO'))
) ENGINE=InnoDB;

-- Perfiles con identificadores fijos para relacionarlos con empleados
INSERT IGNORE INTO roles (id_rol, nombre, descripcion) VALUES
  (1, 'ADMIN', 'Administra el sistema y consulta inventario y ventas'),
  (2, 'USUARIO', 'Gestiona operaciones sin acceso a inventario ni ventas');

-- Empleados que usarán el sistema
CREATE TABLE IF NOT EXISTS empleados (
  id_empleado INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(60) NOT NULL,
  apellido VARCHAR(60) NOT NULL,
  telefono VARCHAR(20),
  puesto VARCHAR(40) NOT NULL,
  id_rol TINYINT UNSIGNED NOT NULL,
  usuario VARCHAR(40) NOT NULL UNIQUE,
  contrasena_hash VARCHAR(255) NOT NULL,
  estado VARCHAR(15) NOT NULL DEFAULT 'Activo',
  CONSTRAINT ck_empleados_estado
    CHECK (estado IN ('Activo', 'Inactivo')),
  CONSTRAINT fk_empleados_rol
    FOREIGN KEY (id_rol) REFERENCES roles(id_rol)
) ENGINE=InnoDB;

-- Productos ofrecidos por el restaurante
CREATE TABLE IF NOT EXISTS productos (
  id_producto INT AUTO_INCREMENT PRIMARY KEY,
  id_categoria INT NOT NULL,
  nombre VARCHAR(90) NOT NULL UNIQUE,
  descripcion VARCHAR(220),
  precio DECIMAL(10,2) NOT NULL,
  estado VARCHAR(15) NOT NULL DEFAULT 'Activo',
  CONSTRAINT ck_productos_precio CHECK (precio >= 0),
  CONSTRAINT ck_productos_estado
    CHECK (estado IN ('Activo', 'Inactivo')),
  CONSTRAINT fk_productos_categoria
    FOREIGN KEY (id_categoria) REFERENCES categorias(id_categoria)
) ENGINE=InnoDB;

-- Insumos y materias primas
CREATE TABLE IF NOT EXISTS ingredientes (
  id_ingrediente INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(80) NOT NULL UNIQUE,
  unidad_medida VARCHAR(20) NOT NULL,
  stock_minimo DECIMAL(10,3) NOT NULL DEFAULT 0,
  estado VARCHAR(15) NOT NULL DEFAULT 'Activo',
  CONSTRAINT ck_ingredientes_minimo CHECK (stock_minimo >= 0),
  CONSTRAINT ck_ingredientes_estado
    CHECK (estado IN ('Activo', 'Inactivo'))
) ENGINE=InnoDB;

-- Receta: relación entre productos e ingredientes
CREATE TABLE IF NOT EXISTS producto_ingrediente (
  id_producto INT NOT NULL,
  id_ingrediente INT NOT NULL,
  cantidad_requerida DECIMAL(10,3) NOT NULL,
  PRIMARY KEY (id_producto, id_ingrediente),
  CONSTRAINT ck_receta_cantidad CHECK (cantidad_requerida > 0),
  CONSTRAINT fk_receta_producto
    FOREIGN KEY (id_producto) REFERENCES productos(id_producto)
    ON DELETE CASCADE,
  CONSTRAINT fk_receta_ingrediente
    FOREIGN KEY (id_ingrediente) REFERENCES ingredientes(id_ingrediente)
) ENGINE=InnoDB;

-- Una fila de inventario por ingrediente
CREATE TABLE IF NOT EXISTS inventario (
  ingrediente_id INT PRIMARY KEY,
  cantidad_actual DECIMAL(10,3) NOT NULL DEFAULT 0,
  fecha_actualizacion DATE NOT NULL,
  CONSTRAINT ck_inventario_cantidad CHECK (cantidad_actual >= 0),
  CONSTRAINT fk_inventario_ingrediente
    FOREIGN KEY (ingrediente_id) REFERENCES ingredientes(id_ingrediente)
) ENGINE=InnoDB;

-- Pedidos registrados por los empleados
CREATE TABLE IF NOT EXISTS pedidos (
  id_pedido INT AUTO_INCREMENT PRIMARY KEY,
  id_cliente INT NULL,
  id_empleado INT NOT NULL,
  fecha DATE NOT NULL,
  estado VARCHAR(20) NOT NULL DEFAULT 'Recibido',
  CONSTRAINT ck_pedidos_estado
    CHECK (estado IN ('Recibido', 'En preparación', 'Entregado', 'Cancelado')),
  CONSTRAINT fk_pedidos_cliente
    FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente)
    ON DELETE SET NULL,
  CONSTRAINT fk_pedidos_empleado
    FOREIGN KEY (id_empleado) REFERENCES empleados(id_empleado)
) ENGINE=InnoDB;

-- Productos y cantidades de cada pedido
CREATE TABLE IF NOT EXISTS detalle_pedido (
  id_detalle INT AUTO_INCREMENT PRIMARY KEY,
  id_pedido INT NOT NULL,
  id_producto INT NOT NULL,
  cantidad INT NOT NULL,
  precio_unitario DECIMAL(10,2) NOT NULL,
  CONSTRAINT uq_detalle_pedido_producto
    UNIQUE (id_pedido, id_producto),
  CONSTRAINT ck_detalle_cantidad CHECK (cantidad > 0),
  CONSTRAINT ck_detalle_precio CHECK (precio_unitario >= 0),
  CONSTRAINT fk_detalle_pedido
    FOREIGN KEY (id_pedido) REFERENCES pedidos(id_pedido)
    ON DELETE CASCADE,
  CONSTRAINT fk_detalle_producto
    FOREIGN KEY (id_producto) REFERENCES productos(id_producto)
) ENGINE=InnoDB;

-- Pagos asociados a los pedidos
CREATE TABLE IF NOT EXISTS pagos (
  id_pago INT AUTO_INCREMENT PRIMARY KEY,
  id_pedido INT NOT NULL,
  fecha_pago DATE NOT NULL,
  metodo_pago VARCHAR(25) NOT NULL,
  monto DECIMAL(10,2) NOT NULL,
  estado VARCHAR(15) NOT NULL DEFAULT 'Registrado',
  CONSTRAINT ck_pagos_monto CHECK (monto > 0),
  CONSTRAINT ck_pagos_estado
    CHECK (estado IN ('Registrado', 'Anulado')),
  CONSTRAINT fk_pagos_pedido
    FOREIGN KEY (id_pedido) REFERENCES pedidos(id_pedido)
    ON DELETE CASCADE
) ENGINE=InnoDB;

-- Datos de prueba: categorías
INSERT IGNORE INTO categorias (id_categoria, nombre, descripcion) VALUES
(1,'Batidos','Batidos de frutas y proteína'),
(2,'Waffles','Waffles saludables'),
(3,'Pancakes','Pancakes y variantes'),
(4,'Bowls de helado','Bowls fríos'),
(5,'Sándwiches','Sándwiches y wraps'),
(6,'Suplementos','Suplementos deportivos'),
(7,'Sodas','Bebidas gasificadas'),
(8,'Bowls de fruta','Fruta, granola y toppings'),
(9,'Combos','Paquetes de productos'),
(10,'Snacks saludables','Porciones y snacks'),
(11,'Café','Café y bebidas con café'),
(12,'Jugos','Jugos naturales'),
(13,'Tés','Tés e infusiones'),
(14,'Postres fitness','Postres saludables'),
(15,'Bebidas especiales','Bebidas de temporada');

-- Datos de prueba: clientes
INSERT IGNORE INTO clientes (id_cliente, nombre, telefono, correo) VALUES
(1,'Juan Pérez','3015550201','juan.perez@example.com'),
(2,'María Gómez','3015550202','maria.gomez@example.com'),
(3,'Carlos Ruiz','3015550203','carlos.ruiz@example.com'),
(4,'Ana Torres','3015550204','ana.torres@example.com'),
(5,'Luis Martínez','3015550205','luis.martinez@example.com'),
(6,'Paula Díaz','3015550206','paula.diaz@example.com'),
(7,'Diego Castro','3015550207','diego.castro@example.com'),
(8,'Sara López','3015550208','sara.lopez@example.com'),
(9,'Miguel Herrera','3015550209','miguel.herrera@example.com'),
(10,'Elena Moreno','3015550210','elena.moreno@example.com'),
(11,'David Rojas','3015550211','david.rojas@example.com'),
(12,'Lucía Vega','3015550212','lucia.vega@example.com'),
(13,'Tomás Gil','3015550213','tomas.gil@example.com'),
(14,'Natalia León','3015550214','natalia.leon@example.com'),
(15,'Andrés Silva','3015550215','andres.silva@example.com');

-- Datos de prueba: empleados.
-- DEMO_NO_VALIDO es un marcador, no un hash de contraseña utilizable.
INSERT IGNORE INTO empleados
(id_empleado, nombre, apellido, telefono, puesto, id_rol, usuario,
 contrasena_hash, estado) VALUES
(1,'Laura','Pérez','3005550101','Administradora',1,'laura.admin','DEMO_NO_VALIDO','Activo'),
(2,'Andrés','Gómez','3005550102','Cajero',2,'andres.gomez','DEMO_NO_VALIDO','Activo'),
(3,'María','Ruiz','3005550103','Cajera',2,'maria.ruiz','DEMO_NO_VALIDO','Activo'),
(4,'Carlos','Díaz','3005550104','Preparador',2,'carlos.diaz','DEMO_NO_VALIDO','Activo'),
(5,'Sofía','Torres','3005550105','Preparadora',2,'sofia.torres','DEMO_NO_VALIDO','Activo'),
(6,'Daniel','Castro','3005550106','Cajero',2,'daniel.castro','DEMO_NO_VALIDO','Activo'),
(7,'Valentina','López','3005550107','Preparadora',2,'valentina.lopez','DEMO_NO_VALIDO','Activo'),
(8,'Mateo','Herrera','3005550108','Preparador',2,'mateo.herrera','DEMO_NO_VALIDO','Activo'),
(9,'Camila','Moreno','3005550109','Cajera',2,'camila.moreno','DEMO_NO_VALIDO','Activo'),
(10,'Samuel','Ortega','3005550110','Cajero',2,'samuel.ortega','DEMO_NO_VALIDO','Activo'),
(11,'Isabella','Ramos','3005550111','Preparadora',2,'isabella.ramos','DEMO_NO_VALIDO','Activo'),
(12,'Nicolás','Vargas','3005550112','Cajero',2,'nicolas.vargas','DEMO_NO_VALIDO','Activo'),
(13,'Gabriela','Mendoza','3005550113','Preparadora',2,'gabriela.mendoza','DEMO_NO_VALIDO','Activo'),
(14,'Julián','Navarro','3005550114','Preparador',2,'julian.navarro','DEMO_NO_VALIDO','Activo'),
(15,'Daniela','Reyes','3005550115','Cajera',2,'daniela.reyes','DEMO_NO_VALIDO','Inactivo');

-- Datos de prueba: productos
INSERT IGNORE INTO productos
(id_producto, id_categoria, nombre, descripcion, precio, estado) VALUES
(1,1,'Batido Verde Detox','Piña, espinaca y banano',18000,'Activo'),
(2,1,'Batido Proteico','Banano y proteína de suero',22000,'Activo'),
(3,2,'Waffle de Avena','Waffle con avena y fruta',20000,'Activo'),
(4,3,'Pancakes de Banano','Pancakes integrales',19000,'Activo'),
(5,4,'Bowl de Açaí','Açaí con fruta y granola',26000,'Activo'),
(6,5,'Sándwich Fitness','Pollo y vegetales',24000,'Activo'),
(7,6,'Proteína Whey','Porción individual',12000,'Activo'),
(8,7,'Soda de Frutos Rojos','Soda artesanal',10000,'Activo'),
(9,8,'Bowl Tropical','Mango, piña y granola',21000,'Activo'),
(10,9,'Combo Energía','Batido y waffle',34000,'Activo'),
(11,10,'Barra de Granola','Barra de avena y miel',7000,'Activo'),
(12,11,'Café Americano','Café negro',6000,'Activo'),
(13,12,'Jugo de Naranja','Jugo natural',9000,'Activo'),
(14,13,'Té de Hierbabuena','Infusión natural',6500,'Activo'),
(15,14,'Brownie Proteico','Brownie de cacao',11000,'Activo');

-- Datos de prueba: ingredientes
INSERT IGNORE INTO ingredientes
(id_ingrediente, nombre, unidad_medida, stock_minimo, estado) VALUES
(1,'Banano','g',1000,'Activo'),
(2,'Piña','g',1000,'Activo'),
(3,'Espinaca','g',300,'Activo'),
(4,'Proteína de suero','g',500,'Activo'),
(5,'Avena','g',1000,'Activo'),
(6,'Huevo','unidad',30,'Activo'),
(7,'Leche','ml',2000,'Activo'),
(8,'Fresa','g',700,'Activo'),
(9,'Granola','g',800,'Activo'),
(10,'Açaí','g',500,'Activo'),
(11,'Pollo','g',1000,'Activo'),
(12,'Pan integral','unidad',20,'Activo'),
(13,'Mango','g',800,'Activo'),
(14,'Naranja','unidad',25,'Activo'),
(15,'Cacao','g',400,'Activo'),
(16,'Café molido','g',300,'Activo'),
(17,'Hierbabuena','g',100,'Activo'),
(18,'Agua','ml',3000,'Activo'),
(19,'Base de soda','ml',1000,'Activo');

-- Ingredientes requeridos por producto
INSERT IGNORE INTO producto_ingrediente
(id_producto, id_ingrediente, cantidad_requerida) VALUES
(1,1,80),(1,2,120),(1,3,30),
(2,1,100),(2,4,30),(2,7,200),
(3,5,100),(3,6,1),(3,7,100),
(4,1,100),(4,5,80),(4,6,2),
(5,10,150),(5,8,60),(5,9,40),
(6,11,120),(6,12,2),
(7,4,30),
(8,8,100),(8,18,200),(8,19,150),
(9,13,100),(9,2,80),(9,7,100),
(10,1,80),(10,5,60),
(11,5,40),(11,9,10),
(12,16,12),(12,18,180),
(13,14,2),
(14,17,8),(14,18,200),
(15,15,35),(15,5,40),(15,6,1);

-- Existencias actuales: una fila por ingrediente
INSERT IGNORE INTO inventario
(ingrediente_id, cantidad_actual, fecha_actualizacion) VALUES
(1,5400,'2026-10-08'),(2,3200,'2026-10-08'),(3,900,'2026-10-08'),
(4,1400,'2026-10-08'),(5,4100,'2026-10-08'),(6,48,'2026-10-08'),
(7,6500,'2026-10-08'),(8,1300,'2026-10-08'),(9,2100,'2026-10-08'),
(10,800,'2026-10-08'),(11,2600,'2026-10-08'),(12,35,'2026-10-08'),
(13,1700,'2026-10-08'),(14,40,'2026-10-08'),(15,750,'2026-10-08'),
(16,600,'2026-10-08'),(17,180,'2026-10-08'),(18,12000,'2026-10-08'),
(19,2500,'2026-10-08');

-- Pedidos
INSERT IGNORE INTO pedidos
(id_pedido, id_cliente, id_empleado, fecha, estado) VALUES
(1,1,2,'2026-10-01','Entregado'),
(2,1,3,'2026-10-01','Entregado'),
(3,1,4,'2026-10-02','Entregado'),
(4,4,5,'2026-10-02','Entregado'),
(5,5,6,'2026-10-03','Entregado'),
(6,6,7,'2026-10-03','Entregado'),
(7,7,8,'2026-10-04','Entregado'),
(8,8,9,'2026-10-04','Entregado'),
(9,9,10,'2026-10-05','Entregado'),
(10,10,11,'2026-10-05','Entregado'),
(11,11,12,'2026-10-06','Entregado'),
(12,12,13,'2026-10-06','Entregado'),
(13,13,14,'2026-10-07','Entregado'),
(14,14,2,'2026-10-07','En preparación'),
(15,15,3,'2026-10-08','Recibido');

-- Líneas de pedido
INSERT IGNORE INTO detalle_pedido
(id_detalle, id_pedido, id_producto, cantidad, precio_unitario) VALUES
(1,1,1,2,18000),(2,1,3,1,20000),
(3,2,2,1,22000),(4,2,5,1,26000),
(5,3,1,1,18000),(6,3,6,1,24000),
(7,4,4,2,19000),(8,4,8,1,10000),
(9,5,5,1,26000),(10,5,7,2,12000),
(11,6,2,2,22000),(12,6,11,2,7000),
(13,7,3,1,20000),(14,7,12,2,6000),
(15,8,1,1,18000),(16,8,9,1,21000),
(17,9,6,1,24000),(18,9,13,1,9000),
(19,10,4,1,19000),(20,10,14,1,6500),
(21,11,2,1,22000),(22,11,15,2,11000),
(23,12,5,2,26000),(24,12,8,1,10000),
(25,13,1,1,18000),(26,13,10,1,34000),
(27,14,3,2,20000),(28,14,7,1,12000),
(29,15,9,1,21000),(30,15,12,1,6000);

-- Pagos asociados a los pedidos
INSERT IGNORE INTO pagos
(id_pago, id_pedido, fecha_pago, metodo_pago, monto, estado) VALUES
(1,1,'2026-10-01','Efectivo',56000,'Registrado'),
(2,2,'2026-10-01','Transferencia',48000,'Registrado'),
(3,3,'2026-10-02','Tarjeta',42000,'Registrado'),
(4,4,'2026-10-02','Efectivo',48000,'Registrado'),
(5,5,'2026-10-03','Transferencia',50000,'Registrado'),
(6,6,'2026-10-03','Tarjeta',58000,'Registrado'),
(7,7,'2026-10-04','Efectivo',32000,'Registrado'),
(8,8,'2026-10-04','Transferencia',39000,'Registrado'),
(9,9,'2026-10-05','Tarjeta',33000,'Registrado'),
(10,10,'2026-10-05','Efectivo',25500,'Registrado'),
(11,11,'2026-10-06','Transferencia',44000,'Registrado'),
(12,12,'2026-10-06','Tarjeta',62000,'Registrado'),
(13,13,'2026-10-07','Efectivo',52000,'Registrado'),
(14,14,'2026-10-07','Transferencia',52000,'Registrado'),
(15,15,'2026-10-08','Tarjeta',27000,'Registrado');

-- Verificar cuántos registros tiene cada tabla
SELECT 'categorias' AS tabla, COUNT(*) AS registros FROM categorias
UNION ALL SELECT 'clientes', COUNT(*) FROM clientes
UNION ALL SELECT 'roles', COUNT(*) FROM roles
UNION ALL SELECT 'empleados', COUNT(*) FROM empleados
UNION ALL SELECT 'productos', COUNT(*) FROM productos
UNION ALL SELECT 'ingredientes', COUNT(*) FROM ingredientes
UNION ALL SELECT 'producto_ingrediente', COUNT(*) FROM producto_ingrediente
UNION ALL SELECT 'inventario', COUNT(*) FROM inventario
UNION ALL SELECT 'pedidos', COUNT(*) FROM pedidos
UNION ALL SELECT 'detalle_pedido', COUNT(*) FROM detalle_pedido
UNION ALL SELECT 'pagos', COUNT(*) FROM pagos;

-- Ver pedidos con cliente opcional, empleado y productos
SELECT
    pe.id_pedido,
    pe.fecha,
    COALESCE(c.nombre, 'CLIENTE GENERAL') AS cliente,
    CONCAT(e.nombre, ' ', e.apellido) AS empleado,
    pr.nombre AS producto,
    dp.cantidad,
    dp.precio_unitario,
    dp.cantidad * dp.precio_unitario AS subtotal,
    pe.estado
FROM pedidos pe
LEFT JOIN clientes c
    ON c.id_cliente = pe.id_cliente
INNER JOIN empleados e
    ON e.id_empleado = pe.id_empleado
INNER JOIN detalle_pedido dp
    ON dp.id_pedido = pe.id_pedido
INNER JOIN productos pr
    ON pr.id_producto = dp.id_producto
ORDER BY pe.id_pedido
LIMIT 10;

-- Revisar los perfiles asignados a los empleados
SELECT
    e.id_empleado,
    CONCAT(e.nombre, ' ', e.apellido) AS empleado,
    e.usuario,
    r.nombre AS perfil
FROM empleados e
INNER JOIN roles r
    ON r.id_rol = e.id_rol
ORDER BY e.id_empleado;

-- Comparar el valor de cada pedido con sus pagos registrados
SELECT
    pe.id_pedido,
    COALESCE(det.total_pedido, 0) AS total_pedido,
    COALESCE(pag.total_pagado, 0) AS total_pagado,
    CASE
        WHEN COALESCE(det.total_pedido, 0) = COALESCE(pag.total_pagado, 0)
        THEN 'Coincide'
        ELSE 'Revisar'
    END AS validacion
FROM pedidos pe
LEFT JOIN (
    SELECT id_pedido, SUM(cantidad * precio_unitario) AS total_pedido
    FROM detalle_pedido
    GROUP BY id_pedido
) det
    ON det.id_pedido = pe.id_pedido
LEFT JOIN (
    SELECT id_pedido, SUM(monto) AS total_pagado
    FROM pagos
    WHERE estado = 'Registrado'
    GROUP BY id_pedido
) pag
    ON pag.id_pedido = pe.id_pedido
ORDER BY pe.id_pedido;

-- CONSULTAS AVANZADAS: subconsultas, uniones y agregaciones

-- 1. Productos activos que superan el precio promedio de los activos.
-- Esta es una subconsulta escalar ubicada en WHERE.
SELECT
    pr.id_producto,
    pr.nombre,
    pr.precio
FROM productos pr
WHERE pr.estado = 'Activo'
  AND pr.precio > (
      SELECT AVG(p2.precio)
      FROM productos p2
      WHERE p2.estado = 'Activo'
  )
ORDER BY pr.precio DESC;

-- 2. Clientes con dos o más pedidos entregados.
-- Usa agrupaciones y funciones de agregación; no es subconsulta.
SELECT
    c.id_cliente,
    c.nombre AS cliente,
    COUNT(DISTINCT pe.id_pedido) AS pedidos_entregados,
    SUM(dp.cantidad * dp.precio_unitario) AS total_comprado,
    MAX(pe.fecha) AS fecha_ultimo_pedido
FROM clientes c
INNER JOIN pedidos pe
    ON pe.id_cliente = c.id_cliente
INNER JOIN detalle_pedido dp
    ON dp.id_pedido = pe.id_pedido
WHERE pe.estado = 'Entregado'
GROUP BY c.id_cliente, c.nombre
HAVING COUNT(DISTINCT pe.id_pedido) >= 2
ORDER BY total_comprado DESC;

-- 3. Detalle de pedidos con cliente, empleado, producto y categoría.
-- LEFT JOIN conserva el pedido cuando no tiene cliente registrado.
SELECT
    pe.id_pedido,
    pe.fecha,
    COALESCE(c.nombre, 'CLIENTE GENERAL') AS cliente,
    CONCAT(e.nombre, ' ', e.apellido) AS empleado,
    cat.nombre AS categoria,
    pr.nombre AS producto,
    dp.cantidad,
    dp.precio_unitario,
    dp.cantidad * dp.precio_unitario AS subtotal,
    pe.estado
FROM pedidos pe
LEFT JOIN clientes c
    ON c.id_cliente = pe.id_cliente
INNER JOIN empleados e
    ON e.id_empleado = pe.id_empleado
INNER JOIN detalle_pedido dp
    ON dp.id_pedido = pe.id_pedido
INNER JOIN productos pr
    ON pr.id_producto = dp.id_producto
INNER JOIN categorias cat
    ON cat.id_categoria = pr.id_categoria
ORDER BY pe.fecha DESC, pe.id_pedido;

-- 4. Total por pedido para octubre de 2026.
-- Agrupa las líneas por pedido; no calcula un único total del mes.
SELECT
    pe.id_pedido,
    UPPER(CONCAT(
        COALESCE(c.nombre, 'CLIENTE GENERAL'),
        ' - ',
        COALESCE(c.telefono, 'SIN TELÉFONO')
    )) AS cliente,
    YEAR(pe.fecha) AS anio,
    MONTH(pe.fecha) AS mes,
    SUM(dp.cantidad * dp.precio_unitario) AS total_pedido
FROM pedidos pe
LEFT JOIN clientes c
    ON c.id_cliente = pe.id_cliente
INNER JOIN detalle_pedido dp
    ON dp.id_pedido = pe.id_pedido
WHERE pe.fecha >= '2026-10-01'
  AND pe.fecha < '2026-11-01'
GROUP BY pe.id_pedido, c.nombre, c.telefono, pe.fecha
ORDER BY pe.fecha, pe.id_pedido;



-- Endpoint 1. Agrupa las ventas por categoría y conserva las categorías cuyo promedio por operación supera el promedio general.

SELECT
    c.id_categoria,
    c.nombre AS categoria,
    SUM(operaciones.total_categoria) AS consumo_total,
    AVG(operaciones.total_categoria) AS promedio_por_operacion
FROM categorias c
INNER JOIN (
    SELECT
        pe.id_pedido,
        pr.id_categoria,
        SUM(dp.cantidad * dp.precio_unitario) AS total_categoria
    FROM pedidos pe
    INNER JOIN detalle_pedido dp
        ON dp.id_pedido = pe.id_pedido
    INNER JOIN productos pr
        ON pr.id_producto = dp.id_producto
    WHERE pe.estado <> 'Cancelado'
    GROUP BY pe.id_pedido, pr.id_categoria
) AS operaciones
    ON operaciones.id_categoria = c.id_categoria
GROUP BY c.id_categoria, c.nombre
HAVING AVG(operaciones.total_categoria) > (
    SELECT AVG(transacciones.total_categoria)
    FROM (
        SELECT
            pe2.id_pedido,
            pr2.id_categoria,
            SUM(dp2.cantidad * dp2.precio_unitario) AS total_categoria
        FROM pedidos pe2
        INNER JOIN detalle_pedido dp2
            ON dp2.id_pedido = pe2.id_pedido
        INNER JOIN productos pr2
            ON pr2.id_producto = dp2.id_producto
        WHERE pe2.estado <> 'Cancelado'
        GROUP BY pe2.id_pedido, pr2.id_categoria
    ) AS transacciones
)
ORDER BY promedio_por_operacion DESC;



-- Endpoint 2. Clientes cuyo consumo acumulado supera el promedio de consumo de todos los clientes.

SELECT
    c.id_cliente,
    c.nombre AS cliente,
    SUM(dp.cantidad * dp.precio_unitario) AS consumo_acumulado
FROM clientes c
INNER JOIN pedidos pe
    ON pe.id_cliente = c.id_cliente
INNER JOIN detalle_pedido dp
    ON dp.id_pedido = pe.id_pedido
WHERE pe.estado <> 'Cancelado'
GROUP BY c.id_cliente, c.nombre
HAVING SUM(dp.cantidad * dp.precio_unitario) > (
    SELECT AVG(consumos.total_cliente)
    FROM (
        SELECT
            pe2.id_cliente,
            SUM(dp2.cantidad * dp2.precio_unitario) AS total_cliente
        FROM pedidos pe2
        INNER JOIN detalle_pedido dp2
            ON dp2.id_pedido = pe2.id_pedido
        WHERE pe2.id_cliente IS NOT NULL
          AND pe2.estado <> 'Cancelado'
        GROUP BY pe2.id_cliente
    ) AS consumos
)
ORDER BY consumo_acumulado DESC;

-- Endpoint 3: busca productos cuya cantidad vendida está por debajo del promedio de ventas de los productos de su misma categoría.

SELECT
    p.id_producto,
    p.nombre AS producto,
    c.nombre AS categoria,
    COALESCE(
        SUM(CASE
            WHEN pe.id_pedido IS NOT NULL THEN dp.cantidad
            ELSE 0
        END),
        0
    ) AS unidades_vendidas
FROM productos p
INNER JOIN categorias c
    ON c.id_categoria = p.id_categoria
LEFT JOIN detalle_pedido dp
    ON dp.id_producto = p.id_producto
LEFT JOIN pedidos pe
    ON pe.id_pedido = dp.id_pedido
   AND pe.estado <> 'Cancelado'
GROUP BY
    p.id_producto,
    p.id_categoria,
    p.nombre,
    c.nombre
HAVING COALESCE(
    SUM(CASE
        WHEN pe.id_pedido IS NOT NULL THEN dp.cantidad
        ELSE 0
    END),
    0
) < (
    SELECT
        COALESCE(
            SUM(CASE
                WHEN pe2.id_pedido IS NOT NULL THEN dp2.cantidad
                ELSE 0
            END),
            0
        ) / NULLIF(COUNT(DISTINCT p2.id_producto), 0)
    FROM productos p2
    LEFT JOIN detalle_pedido dp2
        ON dp2.id_producto = p2.id_producto
    LEFT JOIN pedidos pe2
        ON pe2.id_pedido = dp2.id_pedido
       AND pe2.estado <> 'Cancelado'
    WHERE p2.id_categoria = p.id_categoria
)
ORDER BY unidades_vendidas ASC;


-- Endpoint 4. Resumen de pedidos y ventas por cargo

SELECT
    e.puesto AS contexto_operativo,
    COUNT(DISTINCT pe.id_pedido) AS cantidad_pedidos,
    SUM(dp.cantidad * dp.precio_unitario) AS total_ventas
FROM empleados e
INNER JOIN pedidos pe
    ON pe.id_empleado = e.id_empleado
INNER JOIN detalle_pedido dp
    ON dp.id_pedido = pe.id_pedido
WHERE NOT EXISTS (
    SELECT 1
    FROM pedidos cancelado
    WHERE cancelado.id_pedido = pe.id_pedido
      AND cancelado.estado = 'Cancelado'
)
GROUP BY e.puesto
ORDER BY total_ventas DESC;

-- Endpoint 5. Agrupa los pagos por metodo

SELECT
    metodo_pago,
    COUNT(*) AS cantidad_operaciones,
    SUM(monto) AS total_recaudado,
    AVG(monto) AS promedio_por_pago
FROM pagos
WHERE estado = 'Registrado'
GROUP BY metodo_pago
HAVING COUNT(*) >= 2
   AND AVG(monto) > (
       SELECT MIN(precio)
       FROM productos
       WHERE estado = 'Activo'
   )
ORDER BY total_recaudado DESC;

SELECT
    metodo_pago,
    COUNT(*) AS cantidad_operaciones,
    SUM(monto) AS total_recaudado,
    AVG(monto) AS promedio_por_pago
FROM pagos
WHERE estado = 'Registrado'
GROUP BY metodo_pago
HAVING COUNT(*) >= 2
   AND AVG(monto) > (
       SELECT MIN(precio)
       FROM productos
       WHERE estado = 'Activo'
   )
ORDER BY total_recaudado DESC;

-- AVANCE 2

USE modo_shake;

-- 1. Tabla de auditoría

CREATE TABLE IF NOT EXISTS Historial_Auditoria (
    id_auditoria INT AUTO_INCREMENT PRIMARY KEY,
    tabla_afectada VARCHAR(60) NOT NULL,
    id_registro INT NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    accion VARCHAR(20) NOT NULL,
    usuario VARCHAR(100) NOT NULL,
    descripcion TEXT
) ENGINE = InnoDB;

-- 2. Registrar o actualizar un producto.

CREATE PROCEDURE sp_guardar_producto(
    IN p_id_producto INT,
    IN p_id_categoria INT,
    IN p_nombre VARCHAR(90),
    IN p_descripcion VARCHAR(220),
    IN p_precio DECIMAL(10,2),
    IN p_estado VARCHAR(15)
)
BEGIN
    DECLARE v_id_producto INT;

    IF p_nombre IS NULL OR TRIM(p_nombre) = '' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'El nombre del producto es obligatorio';
    END IF;

    IF p_precio IS NULL OR p_precio < 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'El precio debe ser mayor o igual a cero';
    END IF;

    IF p_estado IS NULL
       OR p_estado NOT IN ('Activo', 'Inactivo') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'El estado del producto no es válido';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM categorias
        WHERE id_categoria = p_id_categoria
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'La categoría no existe';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM productos
        WHERE nombre = TRIM(p_nombre)
          AND id_producto <> COALESCE(p_id_producto, 0)
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Ya existe otro producto con ese nombre';
    END IF;

    IF p_id_producto IS NULL OR p_id_producto = 0 THEN

        INSERT INTO productos (
            id_categoria, nombre, descripcion, precio, estado
        )
        VALUES (
            p_id_categoria,
            TRIM(p_nombre),
            p_descripcion,
            p_precio,
            p_estado
        );

        SET v_id_producto = LAST_INSERT_ID();

    ELSE

        IF NOT EXISTS (
            SELECT 1
            FROM productos
            WHERE id_producto = p_id_producto
        ) THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'El producto que desea actualizar no existe';
        END IF;

        UPDATE productos
        SET id_categoria = p_id_categoria,
            nombre = TRIM(p_nombre),
            descripcion = p_descripcion,
            precio = p_precio,
            estado = p_estado
        WHERE id_producto = p_id_producto;

        SET v_id_producto = p_id_producto;

    END IF;

    SELECT v_id_producto AS id_producto,
           'Producto guardado correctamente' AS mensaje;
END;

-- 3. Reporte de pedidos por intervalo de fechas.

CREATE PROCEDURE sp_consultar_pedidos(
    IN p_fecha_inicio DATE,
    IN p_fecha_fin DATE
)
BEGIN
    IF p_fecha_inicio IS NULL OR p_fecha_fin IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Debe ingresar ambas fechas';
    END IF;

    IF p_fecha_inicio > p_fecha_fin THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'La fecha inicial no puede superar la final';
    END IF;

    SELECT
        pe.id_pedido,
        pe.fecha,
        COALESCE(c.nombre, 'CLIENTE GENERAL') AS cliente,
        CONCAT(e.nombre, ' ', e.apellido) AS empleado,
        cat.nombre AS categoria,
        pr.nombre AS producto,
        dp.cantidad,
        dp.precio_unitario,
        dp.cantidad * dp.precio_unitario AS subtotal,
        pe.estado
    FROM pedidos pe
    LEFT JOIN clientes c
        ON c.id_cliente = pe.id_cliente
    INNER JOIN empleados e
        ON e.id_empleado = pe.id_empleado
    INNER JOIN detalle_pedido dp
        ON dp.id_pedido = pe.id_pedido
    INNER JOIN productos pr
        ON pr.id_producto = dp.id_producto
    INNER JOIN categorias cat
        ON cat.id_categoria = pr.id_categoria
    WHERE pe.fecha BETWEEN p_fecha_inicio AND p_fecha_fin
    ORDER BY pe.fecha DESC, pe.id_pedido, dp.id_detalle;
END;

-- 4. Auditoría automática al actualizar un producto.

CREATE TRIGGER tr_auditoria_producto
AFTER UPDATE ON productos
FOR EACH ROW
BEGIN
    INSERT INTO Historial_Auditoria (
        tabla_afectada,
        id_registro,
        fecha,
        accion,
        usuario,
        descripcion
    )
    VALUES (
        'productos',
        NEW.id_producto,
        NOW(),
        'UPDATE',
        USER(),
        CONCAT(
            'Nombre anterior: ', OLD.nombre,
            '. Nombre nuevo: ', NEW.nombre,
            '. Precio anterior: ', OLD.precio,
            '. Precio nuevo: ', NEW.precio,
            '. Estado anterior: ', OLD.estado,
            '. Estado nuevo: ', NEW.estado
        )
    );
END;

-- 5. Descontar ingredientes al agregar un producto a un pedido.

CREATE TRIGGER tr_descontar_inventario
AFTER INSERT ON detalle_pedido
FOR EACH ROW
BEGIN
    DECLARE v_ingredientes INT DEFAULT 0;
    DECLARE v_actualizados INT DEFAULT 0;

    SELECT COUNT(*)
    INTO v_ingredientes
    FROM producto_ingrediente
    WHERE id_producto = NEW.id_producto;

    IF v_ingredientes = 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'El producto no tiene una receta registrada';
    END IF;

    UPDATE inventario inv
    INNER JOIN producto_ingrediente receta
        ON receta.id_ingrediente = inv.ingrediente_id
    SET inv.cantidad_actual =
            inv.cantidad_actual
            - (receta.cantidad_requerida * NEW.cantidad),
        inv.fecha_actualizacion = CURDATE()
    WHERE receta.id_producto = NEW.id_producto
      AND inv.cantidad_actual >=
          receta.cantidad_requerida * NEW.cantidad;

    SET v_actualizados = ROW_COUNT();

    IF v_actualizados <> v_ingredientes THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Inventario insuficiente o ingrediente sin inventario';
    END IF;
END;

-- 6. Transacción: registrar un pedido con un producto.

CREATE PROCEDURE sp_registrar_pedido(
    IN p_id_cliente INT,
    IN p_id_empleado INT,
    IN p_id_producto INT,
    IN p_cantidad INT
)
BEGIN
    DECLARE v_id_pedido INT;
    DECLARE v_precio DECIMAL(10,2);
    DECLARE v_estado VARCHAR(15);
    DECLARE v_encontrado BOOLEAN DEFAULT TRUE;

    DECLARE CONTINUE HANDLER FOR NOT FOUND
        SET v_encontrado = FALSE;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    IF p_cantidad IS NULL OR p_cantidad <= 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'La cantidad debe ser mayor a cero';
    END IF;

    IF p_id_cliente IS NOT NULL AND NOT EXISTS (
        SELECT 1
        FROM clientes
        WHERE id_cliente = p_id_cliente
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'El cliente no existe';
    END IF;

    SET v_encontrado = TRUE;

    SELECT estado
    INTO v_estado
    FROM empleados
    WHERE id_empleado = p_id_empleado
    FOR UPDATE;

    IF NOT v_encontrado OR v_estado <> 'Activo' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'El empleado no existe o está inactivo';
    END IF;

    SET v_encontrado = TRUE;

    SELECT precio, estado
    INTO v_precio, v_estado
    FROM productos
    WHERE id_producto = p_id_producto
    FOR UPDATE;

    IF NOT v_encontrado OR v_estado <> 'Activo' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'El producto no existe o está inactivo';
    END IF;

    INSERT INTO pedidos (
        id_cliente, id_empleado, fecha, estado
    )
    VALUES (
        p_id_cliente, p_id_empleado, CURDATE(), 'Recibido'
    );

    SET v_id_pedido = LAST_INSERT_ID();

    INSERT INTO detalle_pedido (
        id_pedido, id_producto, cantidad, precio_unitario
    )
    VALUES (
        v_id_pedido, p_id_producto, p_cantidad, v_precio
    );

    COMMIT;

    SELECT
        v_id_pedido AS id_pedido,
        v_precio * p_cantidad AS total_pedido,
        'Pedido registrado correctamente' AS mensaje;
END;

CALL modo_shake.sp_consultar_pedidos('2026-10-01', '2026-10-31');
SET @nombre_prueba = CONCAT('Prueba Avance 2 ', UUID());
CALL modo_shake.sp_guardar_producto(
    NULL, 1, @nombre_prueba, 'Prueba de insercion', 15000, 'Activo'
);
SET @id_producto_prueba = (
    SELECT id_producto FROM modo_shake.productos
    WHERE nombre = @nombre_prueba
);
CALL modo_shake.sp_guardar_producto(
    @id_producto_prueba, 1, @nombre_prueba,
    'Prueba de actualizacion y auditoria', 16000, 'Activo'
);
SELECT id_registro, fecha, accion, usuario, descripcion
FROM modo_shake.Historial_Auditoria
WHERE tabla_afectada = 'productos'
  AND id_registro = @id_producto_prueba
ORDER BY id_auditoria DESC;
SELECT
    MAX(CASE WHEN ingrediente_id = 1 THEN cantidad_actual END),
    MAX(CASE WHEN ingrediente_id = 2 THEN cantidad_actual END),
    MAX(CASE WHEN ingrediente_id = 3 THEN cantidad_actual END)
INTO @banano_antes, @pina_antes, @espinaca_antes
FROM modo_shake.inventario
WHERE ingrediente_id IN (1, 2, 3);
CALL modo_shake.sp_registrar_pedido(NULL, 2, 1, 1);
SELECT
    ing.nombre,
    CASE inv.ingrediente_id
        WHEN 1 THEN @banano_antes
        WHEN 2 THEN @pina_antes
        WHEN 3 THEN @espinaca_antes
    END AS cantidad_antes,
    inv.cantidad_actual AS cantidad_despues,
    CASE inv.ingrediente_id
        WHEN 1 THEN @banano_antes
        WHEN 2 THEN @pina_antes
        WHEN 3 THEN @espinaca_antes
    END - inv.cantidad_actual AS cantidad_descontada
FROM modo_shake.inventario inv
INNER JOIN modo_shake.ingredientes ing
    ON ing.id_ingrediente = inv.ingrediente_id
WHERE inv.ingrediente_id IN (1, 2, 3)
ORDER BY inv.ingrediente_id;
SET @pedidos_antes = (SELECT COUNT(*) FROM modo_shake.pedidos);
SET @detalles_antes = (SELECT COUNT(*) FROM modo_shake.detalle_pedido);
CALL modo_shake.sp_registrar_pedido(NULL, 2, @id_producto_prueba, 1);
SELECT
    @pedidos_antes AS pedidos_antes,
    (SELECT COUNT(*) FROM modo_shake.pedidos) AS pedidos_despues,
    @detalles_antes AS detalles_antes,
    (SELECT COUNT(*) FROM modo_shake.detalle_pedido) AS detalles_despues,
    CASE
        WHEN @pedidos_antes = (SELECT COUNT(*) FROM modo_shake.pedidos)
         AND @detalles_antes = (SELECT COUNT(*) FROM modo_shake.detalle_pedido)
        THEN 'ROLLBACK correcto'
        ELSE 'Revisar resultado'
    END AS validacion;
