-- Esquema inicial de Modo Shake. Compatible con MySQL 8.0.16+.
-- Ejecutar desde DBeaver. No contiene datos reales ni credenciales de conexión.
CREATE DATABASE IF NOT EXISTS modo_shake
  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE modo_shake;

CREATE TABLE IF NOT EXISTS categorias (
  id_categoria INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(60) NOT NULL UNIQUE,
  descripcion VARCHAR(180)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS roles (
  id_rol TINYINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(20) NOT NULL UNIQUE,
  descripcion VARCHAR(150),
  CONSTRAINT ck_roles_nombre CHECK (nombre IN ('ADMIN','USUARIO'))
) ENGINE=InnoDB;

INSERT IGNORE INTO roles (nombre, descripcion) VALUES
  ('ADMIN', 'Administra el sistema y consulta ventas e inventario'),
  ('USUARIO', 'Gestiona operaciones autorizadas sin acceso a ventas e inventario');

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
  CONSTRAINT ck_empleados_estado CHECK (estado IN ('Activo','Inactivo')),
  CONSTRAINT fk_empleados_rol FOREIGN KEY (id_rol) REFERENCES roles(id_rol)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS clientes (
  id_cliente INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(60) NOT NULL,
  telefono VARCHAR(20),
  correo VARCHAR(120) UNIQUE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS productos (
  id_producto INT AUTO_INCREMENT PRIMARY KEY,
  id_categoria INT NOT NULL,
  nombre VARCHAR(90) NOT NULL UNIQUE,
  descripcion VARCHAR(220),
  precio DECIMAL(10,2) NOT NULL,
  estado VARCHAR(15) NOT NULL DEFAULT 'Activo',
  CONSTRAINT ck_productos_precio CHECK (precio >= 0),
  CONSTRAINT ck_productos_estado CHECK (estado IN ('Activo','Inactivo')),
  CONSTRAINT fk_productos_categoria FOREIGN KEY (id_categoria)
    REFERENCES categorias(id_categoria)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS ingredientes (
  id_ingrediente INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(80) NOT NULL UNIQUE,
  unidad_medida VARCHAR(20) NOT NULL,
  stock_minimo DECIMAL(10,3) NOT NULL DEFAULT 0,
  estado VARCHAR(15) NOT NULL DEFAULT 'Activo',
  CONSTRAINT ck_ingredientes_minimo CHECK (stock_minimo >= 0),
  CONSTRAINT ck_ingredientes_estado CHECK (estado IN ('Activo','Inactivo'))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS producto_ingrediente (
  id_producto INT NOT NULL,
  id_ingrediente INT NOT NULL,
  cantidad_requerida DECIMAL(10,3) NOT NULL,
  PRIMARY KEY (id_producto, id_ingrediente),
  CONSTRAINT ck_receta_cantidad CHECK (cantidad_requerida > 0),
  CONSTRAINT fk_receta_producto FOREIGN KEY (id_producto)
    REFERENCES productos(id_producto) ON DELETE CASCADE,
  CONSTRAINT fk_receta_ingrediente FOREIGN KEY (id_ingrediente)
    REFERENCES ingredientes(id_ingrediente)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS inventario (
  ingrediente_id INT PRIMARY KEY,
  cantidad_actual DECIMAL(10,3) NOT NULL DEFAULT 0,
  fecha_actualizacion DATE NOT NULL,
  CONSTRAINT ck_inventario_cantidad CHECK (cantidad_actual >= 0),
  CONSTRAINT fk_inventario_ingrediente FOREIGN KEY (ingrediente_id)
    REFERENCES ingredientes(id_ingrediente)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS pedidos (
  id_pedido INT AUTO_INCREMENT PRIMARY KEY,
  id_cliente INT NULL,
  id_empleado INT NOT NULL,
  fecha DATE NOT NULL,
  estado VARCHAR(20) NOT NULL DEFAULT 'Recibido',
  CONSTRAINT ck_pedidos_estado CHECK (estado IN ('Recibido','En preparación','Entregado','Cancelado')),
  CONSTRAINT fk_pedidos_cliente FOREIGN KEY (id_cliente)
    REFERENCES clientes(id_cliente) ON DELETE SET NULL,
  CONSTRAINT fk_pedidos_empleado FOREIGN KEY (id_empleado)
    REFERENCES empleados(id_empleado)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS detalle_pedido (
  id_detalle INT AUTO_INCREMENT PRIMARY KEY,
  id_pedido INT NOT NULL,
  id_producto INT NOT NULL,
  cantidad INT NOT NULL,
  precio_unitario DECIMAL(10,2) NOT NULL,
  CONSTRAINT uq_detalle_pedido_producto UNIQUE (id_pedido, id_producto),
  CONSTRAINT ck_detalle_cantidad CHECK (cantidad > 0),
  CONSTRAINT ck_detalle_precio CHECK (precio_unitario >= 0),
  CONSTRAINT fk_detalle_pedido FOREIGN KEY (id_pedido)
    REFERENCES pedidos(id_pedido) ON DELETE CASCADE,
  CONSTRAINT fk_detalle_producto FOREIGN KEY (id_producto)
    REFERENCES productos(id_producto)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS pagos (
  id_pago INT AUTO_INCREMENT PRIMARY KEY,
  id_pedido INT NOT NULL,
  fecha_pago DATE NOT NULL,
  metodo_pago VARCHAR(25) NOT NULL,
  monto DECIMAL(10,2) NOT NULL,
  estado VARCHAR(15) NOT NULL DEFAULT 'Registrado',
  CONSTRAINT ck_pagos_monto CHECK (monto > 0),
  CONSTRAINT ck_pagos_estado CHECK (estado IN ('Registrado','Anulado')),
  CONSTRAINT fk_pagos_pedido FOREIGN KEY (id_pedido)
    REFERENCES pedidos(id_pedido) ON DELETE CASCADE
) ENGINE=InnoDB;

