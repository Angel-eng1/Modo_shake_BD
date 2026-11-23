USE modo_shake;

-- DBeaver: seleccionar cada CREATE completo hasta END; y Ctrl + Enter.
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
END;

CREATE PROCEDURE sp_eliminar_cliente(IN p_id INT)
BEGIN
    DELETE FROM clientes WHERE id_cliente=p_id;
END;

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
END;

CREATE PROCEDURE sp_eliminar_ingrediente(IN p_id INT)
BEGIN
    DELETE FROM ingredientes WHERE id_ingrediente=p_id;
END;

CREATE PROCEDURE sp_ajustar_inventario(IN p_id INT, IN p_cantidad DECIMAL(10,3))
BEGIN
    IF p_cantidad IS NULL OR p_cantidad<0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='La cantidad debe ser no negativa';
    END IF;
    INSERT INTO inventario(ingrediente_id,cantidad_actual,fecha_actualizacion) VALUES(p_id,p_cantidad,CURDATE())
    ON DUPLICATE KEY UPDATE cantidad_actual=p_cantidad,fecha_actualizacion=CURDATE();
END;

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
END;

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
END;

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
END;

-- Fotografias del consumo real para devolver exactamente lo descontado.
CREATE TABLE IF NOT EXISTS consumo_pedido (
    id_detalle INT NOT NULL,
    id_ingrediente INT NOT NULL,
    cantidad DECIMAL(18,3) NOT NULL,
    PRIMARY KEY(id_detalle,id_ingrediente),
    FOREIGN KEY(id_detalle) REFERENCES detalle_pedido(id_detalle),
    FOREIGN KEY(id_ingrediente) REFERENCES ingredientes(id_ingrediente),
    CHECK(cantidad>0)
) ENGINE=InnoDB;

-- Reemplaza exclusivamente el trigger del avance 2 por la version con trazabilidad.
DROP TRIGGER IF EXISTS tr_descontar_inventario;

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
END;

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
END;

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
END;
