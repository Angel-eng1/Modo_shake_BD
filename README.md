# Modo Shake

Proyecto Java Swing para la entrega final de Bases de Datos II.

## Requisitos

- JDK 23 y NetBeans con soporte para proyectos Ant.
- MySQL 8.0.16 o posterior.
- Connector/J incluido en `lib`.

## Base de datos existente del avance 2

1. Conserva una copia de seguridad de la base de datos antes de instalar rutinas.
2. Abre `database/03_rutinas_avance_final.sql` en DBeaver. Si ya instalaste el avance 2, ejecuta solamente la nueva definicion `sp_eliminar_producto`; no repitas las rutinas existentes.
3. Ejecuta `database/04_operaciones_aplicacion.sql` por bloques, en orden. Cada procedimiento o trigger se selecciona completo desde `CREATE` hasta `END;` y se ejecuta con Ctrl + Enter. El `DROP TRIGGER` y el `CREATE TRIGGER` que lo sigue se ejecutan consecutivamente, sin registrar pedidos entre ambos.
4. El archivo 04 agrega operaciones para clientes, ingredientes, empleados, pagos, pedidos multiples y cambios de estado. No recrea ni elimina los datos de negocio existentes.
5. Se agrega `consumo_pedido` para guardar el consumo exacto de cada detalle nuevo. El inventario se descuenta solo al registrar el detalle, no al pasar a preparacion. Al cancelar un pedido nuevo sin pagos, se devuelve exactamente ese consumo una sola vez.
6. Los pedidos anteriores a esta migracion no tienen un registro de consumo fiable. La cancelacion de esos pedidos se rechaza y requiere revision manual; no se inventan descuentos historicos.

No vuelvas a cargar los datos iniciales sobre una base que ya tiene los triggers instalados. No ejecutes el antiguo script `02_roles_perfiles.sql` sobre la base actual: es una migracion previa y podria modificar perfiles existentes.

## Instalacion desde cero

Usa el script consolidado de los avances 1 y 2 que acompana la entrega para crear las tablas y los datos, por bloques. Luego sigue las instrucciones anteriores para instalar `sp_eliminar_producto` y el archivo 04. El archivo `01_esquema_mysql.sql` es una alternativa para crear solo el esquema vacio; no contiene el catalogo ni las recetas del avance 1.

## Configuracion en NetBeans

1. Abre esta carpeta como proyecto existente en NetBeans.
2. Selecciona JDK 23 en Propiedades del proyecto si NetBeans solicita resolver la plataforma.
3. Conserva `nbproject/private/db.properties` si tu proyecto ya tiene conexión. Para una instalación nueva, usa `database/db.properties.example` como plantilla para ese archivo, o utiliza `MODO_SHAKE_DB_URL`, `MODO_SHAKE_DB_USER` y `MODO_SHAKE_DB_PASSWORD`.
4. Ejecuta `modo_shake.Modo_Shake`. No hay contrasenas ni configuracion privada incluidas en Git.

## Integracion implementada

- Gestor JDBC Singleton, con conexiones independientes cerradas por cada operacion.
- Login con consulta parametrizada y verificacion PBKDF2.
- Escrituras realizadas mediante CallableStatement y procedimientos almacenados.
- Busqueda de productos por nombre o categoria con LIKE y PreparedStatement, mediante boton Buscar o Enter.
- Reporte de pedidos por fechas usando el procedimiento del avance 2 y JTable.
- Filtros que restringen longitud y formato numerico, incluso al pegar texto.
- Transacciones de pedidos multiples y pagos ejecutadas en MySQL.

## Comprobaciones

Desde la raiz del proyecto, con el JDK en PATH:

```powershell
javac -encoding UTF-8 -cp 'lib/*' -d build/classes src/modo_shake/*.java tests/*.java
java -Djava.awt.headless=true -cp 'build/classes;lib/*' VerificacionLocal
```

La prueba de integracion requiere MySQL accesible y permiso para crear y eliminar una base temporal. El segundo argumento es la carpeta que contiene `nbproject/private/db.properties`. Solo se realizan operaciones de prueba en un esquema temporal propio, eliminado al finalizar:

```powershell
java -cp 'build/classes;lib/*' VerificacionIntegracion "$PWD" "$PWD"
```

La compilación y las pruebas locales pasaron en el proyecto original. La prueba de integración ejecutada en PowerShell con MySQL local verificó el esquema, las rutinas, la auditoría, el descuento de inventario, la cancelación sin devolución duplicada, el rollback, los pagos y el reporte. La prueba usó un esquema temporal. Para habilitar las nuevas operaciones en la base existente, sigue los pasos de instalación de rutinas de esta guía.

## Historial Git

Los commits de `avance-final` se integraron en `main` después de pasar la prueba de integración.
