-- Instalacion completa de Modo Shake. Ejecutar solo en un servidor sin la base modo_shake.
-- Incluye el esquema y los datos aprobados, las rutinas finales y la gestion de recetas.
SET NAMES utf8mb4;

CREATE DATABASE modo_shake
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


DELIMITER $$
CREATE TABLE IF NOT EXISTS Historial_Auditoria (
    id_auditoria INT AUTO_INCREMENT PRIMARY KEY,
    tabla_afectada VARCHAR(60) NOT NULL,
    id_registro INT NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    accion VARCHAR(20) NOT NULL,
    usuario VARCHAR(100) NOT NULL,
    descripcion TEXT
) ENGINE = InnoDB$$

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
END$$

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
END$$

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
END$$

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
END$$

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
END$$

CREATE PROCEDURE sp_eliminar_producto(IN p_id_producto INT)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM productos WHERE id_producto = p_id_producto) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El producto no existe';
    END IF;
    DELETE FROM productos WHERE id_producto = p_id_producto;
END$$

CREATE PROCEDURE sp_guardar_cliente(IN p_id INT, IN p_nombre VARCHAR(60), IN p_telefono VARCHAR(20), IN p_correo VARCHAR(120))
BEGIN
    IF p_nombre IS NULL OR TRIM(p_nombre) = '' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El nombre es obligatorio';
    END IF;
    IF COALESCE(p_id,0) = 0 THEN
        INSERT INTO clientes(nombre,telefono,correo) VALUES(TRIM(p_nombre),p_telefono,p_correo);
    ELSE
        IF NOT EXISTS(SELECT 1 FROM clientes WHERE id_cliente=p_id) THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El cliente no existe';
        END IF;
        UPDATE clientes SET nombre=TRIM(p_nombre),telefono=p_telefono,correo=p_correo WHERE id_cliente=p_id;
    END IF;
END$$

CREATE PROCEDURE sp_eliminar_cliente(IN p_id INT)
BEGIN
    DELETE FROM clientes WHERE id_cliente=p_id;
END$$

CREATE PROCEDURE sp_guardar_ingrediente(IN p_id INT, IN p_nombre VARCHAR(80), IN p_unidad VARCHAR(20), IN p_minimo DECIMAL(10,3), IN p_estado VARCHAR(15))
BEGIN
    IF p_nombre IS NULL OR TRIM(p_nombre)='' OR p_unidad IS NULL OR TRIM(p_unidad)='' OR p_minimo IS NULL OR p_minimo<0 OR p_estado IS NULL OR p_estado NOT IN ('Activo','Inactivo') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Datos de ingrediente invalidos';
    END IF;
    IF COALESCE(p_id,0)=0 THEN
        INSERT INTO ingredientes(nombre,unidad_medida,stock_minimo,estado) VALUES(TRIM(p_nombre),TRIM(p_unidad),p_minimo,p_estado);
    ELSE
        IF NOT EXISTS(SELECT 1 FROM ingredientes WHERE id_ingrediente=p_id) THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='El ingrediente no existe';
        END IF;
        UPDATE ingredientes SET nombre=TRIM(p_nombre),unidad_medida=TRIM(p_unidad),stock_minimo=p_minimo,estado=p_estado WHERE id_ingrediente=p_id;
    END IF;
END$$

CREATE PROCEDURE sp_eliminar_ingrediente(IN p_id INT)
BEGIN
    DELETE FROM ingredientes WHERE id_ingrediente=p_id;
END$$

CREATE PROCEDURE sp_ajustar_inventario(IN p_id INT, IN p_cantidad DECIMAL(10,3))
BEGIN
    IF p_cantidad IS NULL OR p_cantidad<0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='La cantidad debe ser no negativa';
    END IF;
    INSERT INTO inventario(ingrediente_id,cantidad_actual,fecha_actualizacion) VALUES(p_id,p_cantidad,CURDATE())
    ON DUPLICATE KEY UPDATE cantidad_actual=p_cantidad,fecha_actualizacion=CURDATE();
END$$

CREATE PROCEDURE sp_guardar_empleado(IN p_id INT, IN p_nombre VARCHAR(60), IN p_apellido VARCHAR(60), IN p_telefono VARCHAR(20), IN p_puesto VARCHAR(40), IN p_usuario VARCHAR(40), IN p_perfil VARCHAR(20), IN p_estado VARCHAR(15), IN p_hash VARCHAR(255))
BEGIN
    DECLARE v_rol INT;
    IF p_nombre IS NULL OR TRIM(p_nombre)='' OR p_apellido IS NULL OR TRIM(p_apellido)='' OR p_usuario IS NULL OR TRIM(p_usuario)='' OR p_puesto IS NULL OR TRIM(p_puesto)='' OR p_estado IS NULL OR p_estado NOT IN ('Activo','Inactivo') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Datos de empleado invalidos';
    END IF;
    SET v_rol=(SELECT id_rol FROM roles WHERE nombre=p_perfil);
    IF v_rol IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='El perfil no existe';
    END IF;
    IF p_hash IS NOT NULL AND p_hash NOT LIKE 'PBKDF2$%' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Formato de clave invalido';
    END IF;
    IF COALESCE(p_id,0)=0 THEN
        IF p_hash IS NULL THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='La clave es obligatoria';
        END IF;
        INSERT INTO empleados(nombre,apellido,telefono,puesto,id_rol,usuario,contrasena_hash,estado)
        VALUES(TRIM(p_nombre),TRIM(p_apellido),p_telefono,p_puesto,v_rol,TRIM(p_usuario),p_hash,p_estado);
    ELSE
        IF NOT EXISTS(SELECT 1 FROM empleados WHERE id_empleado=p_id) THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='El empleado no existe';
        END IF;
        UPDATE empleados SET nombre=TRIM(p_nombre),apellido=TRIM(p_apellido),telefono=p_telefono,puesto=p_puesto,
            id_rol=v_rol,usuario=TRIM(p_usuario),contrasena_hash=COALESCE(p_hash,contrasena_hash),estado=p_estado WHERE id_empleado=p_id;
    END IF;
END$$

CREATE PROCEDURE sp_crear_primer_admin(IN p_nombre VARCHAR(60), IN p_apellido VARCHAR(60), IN p_usuario VARCHAR(40), IN p_hash VARCHAR(255))
BEGIN
    DECLARE v_rol INT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;
    START TRANSACTION;
    SELECT id_rol INTO v_rol FROM roles WHERE nombre='ADMIN' FOR UPDATE;
    IF v_rol IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Falta el perfil ADMIN';
    END IF;
    IF EXISTS(SELECT 1 FROM empleados WHERE id_rol=v_rol AND estado='Activo' AND contrasena_hash LIKE 'PBKDF2$%') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Ya existe un administrador configurado';
    END IF;
    CALL sp_guardar_empleado(0,p_nombre,p_apellido,NULL,'Administrador',p_usuario,'ADMIN','Activo',p_hash);
    COMMIT;
END$$

CREATE PROCEDURE sp_registrar_pago(IN p_pedido INT, IN p_metodo VARCHAR(25), IN p_monto DECIMAL(10,2))
BEGIN
    DECLARE v_estado VARCHAR(20);
    DECLARE v_total DECIMAL(14,2);
    DECLARE v_pagado DECIMAL(14,2);
    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;
    START TRANSACTION;
    SELECT estado INTO v_estado FROM pedidos WHERE id_pedido=p_pedido FOR UPDATE;
    IF v_estado IS NULL OR v_estado='Cancelado' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Pedido inexistente o cancelado';
    END IF;
    IF p_monto IS NULL OR p_monto<=0 OR p_metodo IS NULL OR p_metodo NOT IN ('Efectivo','Tarjeta','Transferencia') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Datos de pago invalidos';
    END IF;
    SELECT COALESCE(SUM(cantidad*precio_unitario),0) INTO v_total FROM detalle_pedido WHERE id_pedido=p_pedido;
    SELECT COALESCE(SUM(monto),0) INTO v_pagado FROM pagos WHERE id_pedido=p_pedido AND estado='Registrado';
    IF v_total=0 OR p_monto>v_total-v_pagado THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='El monto excede el saldo del pedido';
    END IF;
    INSERT INTO pagos(id_pedido,fecha_pago,metodo_pago,monto,estado) VALUES(p_pedido,CURDATE(),p_metodo,p_monto,'Registrado');
    COMMIT;
END$$

CREATE TABLE IF NOT EXISTS consumo_pedido (
    id_detalle INT NOT NULL,
    id_ingrediente INT NOT NULL,
    cantidad DECIMAL(18,3) NOT NULL,
    PRIMARY KEY(id_detalle,id_ingrediente),
    FOREIGN KEY(id_detalle) REFERENCES detalle_pedido(id_detalle),
    FOREIGN KEY(id_ingrediente) REFERENCES ingredientes(id_ingrediente),
    CHECK(cantidad>0)
) ENGINE=InnoDB$$

DROP TRIGGER IF EXISTS tr_descontar_inventario$$

CREATE TRIGGER tr_descontar_inventario
AFTER INSERT ON detalle_pedido FOR EACH ROW
BEGIN
    DECLARE v_ingredientes INT;
    DECLARE v_actualizados INT;
    SELECT COUNT(*) INTO v_ingredientes FROM producto_ingrediente WHERE id_producto=NEW.id_producto;
    IF v_ingredientes=0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='El producto no tiene una receta registrada';
    END IF;
    INSERT INTO consumo_pedido(id_detalle,id_ingrediente,cantidad)
        SELECT NEW.id_detalle,id_ingrediente,cantidad_requerida*NEW.cantidad
        FROM producto_ingrediente WHERE id_producto=NEW.id_producto;
    UPDATE inventario inv JOIN consumo_pedido c ON c.id_ingrediente=inv.ingrediente_id
    SET inv.cantidad_actual=inv.cantidad_actual-c.cantidad, inv.fecha_actualizacion=CURDATE()
    WHERE c.id_detalle=NEW.id_detalle AND inv.cantidad_actual>=c.cantidad;
    SET v_actualizados=ROW_COUNT();
    IF v_actualizados<>v_ingredientes THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Inventario insuficiente o ingrediente sin inventario';
    END IF;
END$$

CREATE PROCEDURE sp_registrar_pedido_multiple(IN p_cliente INT, IN p_empleado INT, IN p_detalles JSON)
BEGIN
    DECLARE v_pedido INT;
    DECLARE v_producto INT;
    DECLARE v_cantidad INT;
    DECLARE v_precio DECIMAL(10,2);
    DECLARE v_estado VARCHAR(15);
    DECLARE v_i INT DEFAULT 0;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;
    START TRANSACTION;
    IF p_detalles IS NULL OR JSON_TYPE(p_detalles)<>'ARRAY' OR JSON_LENGTH(p_detalles)=0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Agrega productos al pedido';
    END IF;
    SELECT estado INTO v_estado FROM empleados WHERE id_empleado=p_empleado FOR UPDATE;
    IF v_estado IS NULL OR v_estado<>'Activo' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Empleado inexistente o inactivo';
    END IF;
    INSERT INTO pedidos(id_cliente,id_empleado,fecha,estado) VALUES(p_cliente,p_empleado,CURDATE(),'Recibido');
    SET v_pedido=LAST_INSERT_ID();
    WHILE v_i<JSON_LENGTH(p_detalles) DO
        IF COALESCE(JSON_TYPE(JSON_EXTRACT(p_detalles,CONCAT('$[',v_i,'].producto'))),'NULL')<>'INTEGER'
           OR COALESCE(JSON_TYPE(JSON_EXTRACT(p_detalles,CONCAT('$[',v_i,'].cantidad'))),'NULL')<>'INTEGER' THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Producto y cantidad deben ser enteros';
        END IF;
        SET v_producto=JSON_UNQUOTE(JSON_EXTRACT(p_detalles,CONCAT('$[',v_i,'].producto')));
        SET v_cantidad=JSON_UNQUOTE(JSON_EXTRACT(p_detalles,CONCAT('$[',v_i,'].cantidad')));
        IF v_producto<=0 OR v_cantidad<=0 THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Producto o cantidad invalidos';
        END IF;
        SET v_precio=NULL;
        SELECT precio INTO v_precio FROM productos WHERE id_producto=v_producto AND estado='Activo' FOR UPDATE;
        IF v_precio IS NULL THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Producto inexistente o inactivo';
        END IF;
        INSERT INTO detalle_pedido(id_pedido,id_producto,cantidad,precio_unitario) VALUES(v_pedido,v_producto,v_cantidad,v_precio);
        SET v_i=v_i+1;
    END WHILE;
    COMMIT;
    SELECT v_pedido AS id_pedido;
END$$

CREATE PROCEDURE sp_cambiar_estado_pedido(IN p_pedido INT, IN p_estado VARCHAR(20))
BEGIN
    DECLARE v_anterior VARCHAR(20);
    DECLARE v_esperados INT;
    DECLARE v_actualizados INT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;
    START TRANSACTION;
    SELECT estado INTO v_anterior FROM pedidos WHERE id_pedido=p_pedido FOR UPDATE;
    IF v_anterior IS NULL OR p_estado IS NULL OR p_estado NOT IN ('Recibido','En preparación','Entregado','Cancelado') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Pedido o estado invalido';
    END IF;
    IF v_anterior<>p_estado THEN
        IF NOT ((v_anterior='Recibido' AND p_estado IN ('En preparación','Cancelado')) OR
                (v_anterior='En preparación' AND p_estado IN ('Entregado','Cancelado'))) THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Transicion de estado no permitida';
        END IF;
        IF p_estado='Cancelado' THEN
            IF EXISTS(SELECT 1 FROM pagos WHERE id_pedido=p_pedido AND estado='Registrado') THEN
                SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='El pedido tiene pagos registrados';
            END IF;
            IF EXISTS(SELECT 1 FROM detalle_pedido d WHERE d.id_pedido=p_pedido
                AND NOT EXISTS(SELECT 1 FROM consumo_pedido c WHERE c.id_detalle=d.id_detalle)) THEN
                SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Pedido anterior a la migracion: revisar su inventario antes de cancelar';
            END IF;
            SELECT COUNT(DISTINCT c.id_ingrediente) INTO v_esperados
            FROM consumo_pedido c JOIN detalle_pedido d ON d.id_detalle=c.id_detalle WHERE d.id_pedido=p_pedido;
            UPDATE inventario inv JOIN (
                SELECT c.id_ingrediente,SUM(c.cantidad) AS devolver FROM consumo_pedido c
                JOIN detalle_pedido d ON d.id_detalle=c.id_detalle WHERE d.id_pedido=p_pedido GROUP BY c.id_ingrediente
            ) tot ON tot.id_ingrediente=inv.ingrediente_id
            SET inv.cantidad_actual=inv.cantidad_actual+tot.devolver,inv.fecha_actualizacion=CURDATE();
            SET v_actualizados=ROW_COUNT();
            IF v_actualizados<>v_esperados THEN
                SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Falta inventario para devolver el consumo';
            END IF;
        END IF;
        UPDATE pedidos SET estado=p_estado WHERE id_pedido=p_pedido;
    END IF;
    COMMIT;
END$$

CREATE PROCEDURE sp_guardar_receta(IN p_producto INT, IN p_componentes JSON)
BEGIN
    DECLARE v_producto INT DEFAULT NULL;
    DECLARE v_ingrediente BIGINT;
    DECLARE v_estado VARCHAR(15);
    DECLARE v_cantidad VARCHAR(100);
    DECLARE v_distintos INT;
    DECLARE v_i INT DEFAULT 0;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;
    START TRANSACTION;
    SELECT id_producto INTO v_producto FROM productos
    WHERE id_producto = p_producto FOR UPDATE;
    IF v_producto IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El producto no existe';
    END IF;
    IF p_componentes IS NULL OR JSON_TYPE(p_componentes) <> 'ARRAY'
       OR JSON_LENGTH(p_componentes) = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La receta requiere al menos un ingrediente';
    END IF;
    SELECT COUNT(DISTINCT ingrediente) INTO v_distintos
    FROM JSON_TABLE(p_componentes, '$[*]' COLUMNS (
        ingrediente INT PATH '$.ingrediente' ERROR ON ERROR
    )) AS componentes;
    IF v_distintos <> JSON_LENGTH(p_componentes) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Hay ingredientes repetidos o sin identificador';
    END IF;
    WHILE v_i < JSON_LENGTH(p_componentes) DO
        IF COALESCE(JSON_TYPE(JSON_EXTRACT(p_componentes, CONCAT('$[', v_i, '].ingrediente'))), 'NULL') <> 'INTEGER'
           OR COALESCE(JSON_TYPE(JSON_EXTRACT(p_componentes, CONCAT('$[', v_i, '].cantidad'))), 'NULL')
              NOT IN ('INTEGER', 'DOUBLE', 'DECIMAL') THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Ingrediente y cantidad deben ser valores numericos';
        END IF;
        SET v_ingrediente = JSON_UNQUOTE(JSON_EXTRACT(p_componentes, CONCAT('$[', v_i, '].ingrediente')));
        SET v_cantidad = JSON_UNQUOTE(JSON_EXTRACT(p_componentes, CONCAT('$[', v_i, '].cantidad')));
        IF v_ingrediente <= 0 OR v_ingrediente > 2147483647
           OR v_cantidad NOT REGEXP '^[0-9]+([.][0-9]{1,3})?$' THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Usa cantidades positivas con hasta tres decimales';
        END IF;
        IF CAST(v_cantidad AS DECIMAL(20,3)) <= 0
           OR CAST(v_cantidad AS DECIMAL(20,3)) > 9999999.999 THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La cantidad debe estar entre 0.001 y 9999999.999';
        END IF;
        SET v_estado = NULL;
        SELECT estado INTO v_estado FROM ingredientes
        WHERE id_ingrediente = v_ingrediente FOR SHARE;
        IF v_estado IS NULL OR v_estado <> 'Activo' THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Un ingrediente no existe o esta inactivo';
        END IF;
        SET v_i = v_i + 1;
    END WHILE;
    DELETE FROM producto_ingrediente WHERE id_producto = p_producto;
    INSERT INTO producto_ingrediente(id_producto, id_ingrediente, cantidad_requerida)
    SELECT p_producto, ingrediente, cantidad
    FROM JSON_TABLE(p_componentes, '$[*]' COLUMNS (
        ingrediente INT PATH '$.ingrediente',
        cantidad DECIMAL(10,3) PATH '$.cantidad'
    )) AS componentes;
    COMMIT;
END$$

DELIMITER ;
