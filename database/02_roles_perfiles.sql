-- Perfiles ADMIN y USUARIO para una base modo_shake ya creada.
-- Ejecutar desde DBeaver conectado a MySQL; los empleados existentes pasan a USUARIO.
USE modo_shake;

CREATE TABLE IF NOT EXISTS roles (
  id_rol TINYINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(20) NOT NULL UNIQUE,
  descripcion VARCHAR(150),
  CONSTRAINT ck_roles_nombre CHECK (nombre IN ('ADMIN','USUARIO'))
) ENGINE=InnoDB;

INSERT IGNORE INTO roles (nombre, descripcion) VALUES
  ('ADMIN', 'Administra el sistema y consulta ventas e inventario'),
  ('USUARIO', 'Gestiona operaciones autorizadas sin acceso a ventas e inventario');

SET @columna_rol = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'empleados' AND COLUMN_NAME = 'id_rol'
);
SET @sql_rol = IF(@columna_rol = 0,
  'ALTER TABLE empleados ADD COLUMN id_rol TINYINT UNSIGNED NULL AFTER puesto',
  'SELECT ''La columna id_rol ya existe'''
);
PREPARE stmt_rol FROM @sql_rol;
EXECUTE stmt_rol;
DEALLOCATE PREPARE stmt_rol;

UPDATE empleados e
JOIN roles r ON r.nombre = 'USUARIO'
SET e.id_rol = r.id_rol
WHERE e.id_rol IS NULL;

ALTER TABLE empleados MODIFY id_rol TINYINT UNSIGNED NOT NULL;

SET @fk_rol = (
  SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'empleados'
    AND CONSTRAINT_NAME = 'fk_empleados_rol' AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);
SET @sql_fk = IF(@fk_rol = 0,
  'ALTER TABLE empleados ADD CONSTRAINT fk_empleados_rol FOREIGN KEY (id_rol) REFERENCES roles(id_rol)',
  'SELECT ''La llave foránea fk_empleados_rol ya existe'''
);
PREPARE stmt_fk FROM @sql_fk;
EXECUTE stmt_fk;
DEALLOCATE PREPARE stmt_fk;
