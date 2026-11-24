USE modo_shake;

-- Ejecutar el CREATE completo hasta END; como una sola sentencia.
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
END;
