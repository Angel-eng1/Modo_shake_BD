# Configurar la aplicación con MySQL

1. Copia `database/db.properties.example` a `nbproject/private/db.properties`. Crea las carpetas de destino si no existen.
2. Escribe el usuario y la contraseña de MySQL que usas en DBeaver en `db.user` y `db.password`. El archivo está excluido de Git para no publicar la contraseña.
3. Para una base nueva, ejecuta `database/instalacion_completa.sql` según el README. Si ya existe, sigue las migraciones indicadas allí. La conexión predeterminada es `localhost:3306`.
4. Abre el proyecto en NetBeans y ejecútalo. En el inicio de sesión, usa **Configurar administrador inicial** para crear la primera cuenta con contraseña segura.
5. Inicia sesión como ADMIN y crea el resto de cuentas desde **Empleados**; asigna a cada persona el perfil ADMIN o USUARIO.

La contraseña se almacena con PBKDF2. Los registros de prueba con `DEMO_NO_VALIDO` no sirven para iniciar sesión; configura la cuenta ADMIN desde el botón inicial y luego actualiza las demás cuentas.

La aplicación incluye `lib/mysql-connector-j-8.2.0.jar`; no es necesario instalar un controlador adicional en NetBeans.

Para una instalación real, crea un usuario MySQL exclusivo para la aplicación, con permisos limitados a `modo_shake`; evita conectarla como `root`.
