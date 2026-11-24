# Configurar la aplicación con MySQL

1. Abre `nbproject/private/db.properties`.
2. Escribe el usuario y la contraseña de MySQL que usas en DBeaver en `db.user` y `db.password`. El archivo está excluido de Git para no publicar la contraseña.
3. Confirma que DBeaver tenga la base `modo_shake` creada y que hayas ejecutado el esquema SQL. La app se conecta a `localhost:3306`.
4. Abre el proyecto en NetBeans y ejecútalo. En el inicio de sesión, usa **Configurar administrador inicial** para crear la primera cuenta con contraseña segura.
5. Inicia sesión como ADMIN y crea el resto de cuentas desde **Empleados**; asigna a cada persona el perfil ADMIN o USUARIO.

La contraseña se almacena con PBKDF2. Los registros de prueba con `DEMO_NO_VALIDO` no sirven para iniciar sesión; configura la cuenta ADMIN desde el botón inicial y luego actualiza las demás cuentas.

La aplicación incluye `lib/mysql-connector-j-8.2.0.jar`; no es necesario instalar un controlador adicional en NetBeans.

Para una instalación real, crea un usuario MySQL exclusivo para la aplicación, con permisos limitados a `modo_shake`; evita conectarla como `root`.
