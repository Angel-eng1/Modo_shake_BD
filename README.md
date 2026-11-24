# Modo Shake

Aplicación de escritorio para gestionar productos, recetas, clientes, empleados, pedidos, inventario y pagos. Desarrollada en Java Swing con JDBC para la entrega final de Bases de Datos II.

## Requisitos

- JDK 23.
- NetBeans con soporte para proyectos Java Ant, o el ejecutable JAR incluido en la entrega.
- MySQL 8.0.16 o posterior.
- MySQL Connector/J incluido en la carpeta lib.

## Instalación nueva

El archivo [database/instalacion_completa.sql](database/instalacion_completa.sql) crea la base modo_shake, el modelo aprobado, los datos de prueba, los procedimientos, los triggers y la gestión de recetas. Debe ejecutarse sobre un servidor que todavía no tenga esa base. Su primera sentencia de creación rechaza una base ya existente.

Desde la raíz del proyecto, con el cliente mysql disponible en PATH:

~~~powershell
mysql --default-character-set=utf8mb4 -u root -p -e "source database/instalacion_completa.sql"
~~~

También se puede usar la ruta completa del ejecutable mysql. La contraseña se introduce en el aviso del cliente.

## Base existente

Para actualizar una instalación del Avance 2:

1. Guardar un respaldo.
2. Ejecutar solo sp_eliminar_producto de [03_rutinas_avance_final.sql](database/03_rutinas_avance_final.sql), si todavía no existe.
3. Ejecutar [04_operaciones_aplicacion.sql](database/04_operaciones_aplicacion.sql) en orden.
4. Ejecutar sp_guardar_receta de [05_recetas.sql](database/05_recetas.sql), si todavía no existe.

En DBeaver, cada procedimiento o trigger de los archivos 03, 04 y 05 se selecciona completo desde CREATE hasta END; y se ejecuta con Ctrl + Enter. El DROP TRIGGER del archivo 04 y el CREATE TRIGGER siguiente se ejecutan consecutivamente.

Las recetas de pedidos nuevos guardan su consumo en consumo_pedido. Al cancelar se devuelve ese consumo una sola vez, aunque la receta haya cambiado después. Los pedidos anteriores a esta migración requieren revisión manual del inventario antes de cancelarse.

## Conexión y ejecución

Copiar [database/db.properties.example](database/db.properties.example) a nbproject/private/db.properties y completar las credenciales de MySQL. Ese archivo está excluido de Git y del paquete de entrega. También se admiten MODO_SHAKE_DB_URL, MODO_SHAKE_DB_USER y MODO_SHAKE_DB_PASSWORD.

### NetBeans

Abrir la carpeta del proyecto, seleccionar JDK 23 y ejecutar modo_shake.Modo_Shake con F6.

### Ejecutable JAR

Desde la raíz del proyecto, una vez construido el ejecutable:

~~~powershell
java -jar dist/Modo_Shake.jar
~~~

Para usar la configuración privada del proyecto, el comando se ejecuta desde esta carpeta. También puede configurarse un archivo en la carpeta personal .modo-shake/db.properties.

La cuenta de inicio de sesión pertenece a la aplicación. En una instalación nueva, el botón Configurar administrador inicial permite crearla. Los registros de demostración con DEMO_NO_VALIDO no son contraseñas utilizables.

## Módulos

| Módulo | Función |
| --- | --- |
| Inicio | Resumen de operaciones que se actualiza al volver a esta sección. |
| Productos | Registro, actualización, eliminación y búsqueda por nombre o categoría. |
| Recetas | Composición de cada producto e ingredientes necesarios por unidad. |
| Ingredientes | Gestión de insumos y unidades de medida. |
| Clientes | Gestión de clientes. |
| Pedidos | Pedidos de varios productos, cambios de estado y reporte por fechas. |
| Inventario | Existencias y stock mínimo. Disponible para administradores. |
| Ventas | Registro de pagos y consulta de saldos. Disponible para administradores. |
| Empleados | Gestión de cuentas y perfiles. Disponible para administradores. |

## Uso de Recetas

1. Seleccionar el producto. La tabla carga su receta guardada.
2. Seleccionar un ingrediente activo e introducir la cantidad necesaria para una unidad del producto, en la unidad indicada.
3. Pulsar Agregar / actualizar. Si el ingrediente ya está en la tabla, se actualiza su cantidad en el borrador.
4. Para cambiar una fila, seleccionarla, editar la cantidad y pulsar Agregar / actualizar.
5. Quitar seleccionado retira el ingrediente del borrador.
6. Guardar receta reemplaza la composición completa mediante una transacción en MySQL.

Las cantidades admiten hasta tres decimales y deben estar entre 0,001 y 9999999,999. La receta requiere al menos un ingrediente activo. Los cambios se confirman antes de descartar el borrador al cambiar de producto, recargar, salir del módulo, cerrar sesión o cerrar la ventana.

Actualizar catálogo incorpora productos e ingredientes creados durante la sesión. Recargar receta recupera su composición guardada. Guardar la receta no descuenta inventario; el descuento ocurre al registrar un pedido.

## Recorrido funcional para la presentación

1. Iniciar sesión como administrador y mostrar los módulos.
2. Crear un producto en una categoría existente.
3. Abrir Recetas, actualizar el catálogo y asignarle dos ingredientes activos con cantidades pequeñas.
4. Guardar y recargar la receta para comprobar su persistencia.
5. Revisar las existencias de esos ingredientes en Inventario.
6. Crear un pedido de dos unidades del producto. Comprobar el descuento del doble de cada cantidad de la receta.
7. Pasarlo a En preparación y comprobar que el inventario no se descuenta otra vez.
8. Cancelar ese pedido sin pagos y comprobar la devolución del consumo.
9. Crear otro pedido, registrar un pago y consultar su saldo.
10. Buscar productos, consultar el reporte por fechas y mostrar la validación de un campo numérico o de un formulario incompleto.

Los datos iniciales corresponden a octubre de 2026. El rango del reporte debe incluir las fechas de los registros que se desean mostrar.

## Arquitectura y seguridad

- ConexionBD aplica Singleton como gestor de conexiones JDBC.
- Las modificaciones se ejecutan con CallableStatement y procedimientos almacenados.
- Las búsquedas y la autenticación usan PreparedStatement y parámetros.
- Las contraseñas de la aplicación se verifican mediante PBKDF2.
- Los filtros visuales limitan longitudes y formatos numéricos.
- Las reglas de inventario, pagos y cancelación se ejecutan en MySQL con transacciones.

## Comprobaciones

Desde la raíz del proyecto, con el JDK en PATH:

~~~powershell
javac -encoding UTF-8 -cp 'lib/*' -d build/classes src/modo_shake/*.java tests/*.java
java -Djava.awt.headless=true -cp 'build/classes;lib/*' VerificacionLocal
java -cp 'build/classes;lib/*' VerificacionIntegracion "$PWD" "$PWD"
~~~

La integración utiliza un esquema temporal propio, eliminado al finalizar. Verifica las recetas, sus validaciones y persistencia, el consumo histórico, la auditoría, inventario, cancelaciones, rollback, pagos y reportes.

El archivo [00_avances_1_y_2.sql](database/00_avances_1_y_2.sql) conserva el desarrollo histórico y sus demostraciones. La instalación nueva usa instalacion_completa.sql para cargar los componentes finales sin ejecutar las pruebas históricas.
