-- Avance 2 - Modo Shake
-- Requiere el avance 1. DBeaver: ejecutar cada bloque completo con Ctrl + Enter.

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


-- Ejecutar como una sentencia completa con Ctrl + Enter.
CREATE PROCEDURE sp_eliminar_producto(IN p_id_producto INT)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM productos WHERE id_producto = p_id_producto) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El producto no existe';
    END IF;
    DELETE FROM productos WHERE id_producto = p_id_producto;
END;
