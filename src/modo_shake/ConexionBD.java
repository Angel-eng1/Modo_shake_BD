package modo_shake;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Abre conexiones JDBC usando credenciales locales, fuera del código fuente. */
public final class ConexionBD {
    private static final String URL_PREDETERMINADA =
            "jdbc:mysql://localhost:3306/modo_shake?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota&connectTimeout=5000&socketTimeout=10000";

    private ConexionBD() { }

    public static Connection abrir() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            throw new SQLException("Falta MySQL Connector/J. Revisa la biblioteca del proyecto en NetBeans.", ex);
        }
        Properties propiedades = cargarPropiedades();
        String url = valor("MODO_SHAKE_DB_URL", propiedades, "db.url", URL_PREDETERMINADA);
        String usuario = valor("MODO_SHAKE_DB_USER", propiedades, "db.user", "root");
        String contrasena = valor("MODO_SHAKE_DB_PASSWORD", propiedades, "db.password", "");
        return DriverManager.getConnection(url, usuario, contrasena);
    }

    private static Properties cargarPropiedades() throws SQLException {
        Properties p = new Properties();
        Path proyecto = Path.of(System.getProperty("user.dir"), "nbproject", "private", "db.properties");
        Path usuario = Path.of(System.getProperty("user.home"), ".modo-shake", "db.properties");
        Path archivo = Files.exists(proyecto) ? proyecto : usuario;
        if (Files.exists(archivo)) {
            try (InputStream entrada = Files.newInputStream(archivo)) {
                p.load(entrada);
            } catch (IOException ex) {
                throw new SQLException("No se pudo leer la configuración local de MySQL: " + archivo, ex);
            }
        }
        return p;
    }

    private static String valor(String variable, Properties p, String clave, String defecto) {
        String entorno = System.getenv(variable);
        if (entorno != null && !entorno.isBlank()) return entorno;
        return p.getProperty(clave, defecto).trim();
    }
}
