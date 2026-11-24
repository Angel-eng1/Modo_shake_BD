import modo_shake.*;
import java.nio.file.*;
import java.sql.*;
import java.math.BigDecimal;
import java.util.*;

/** Usa una base temporal propia. Nunca ejecuta migraciones en modo_shake. */
public class VerificacionIntegracion {
    static String esquema;
    static Path raiz;
    static void comprobar(boolean ok, String mensaje) {
        if (!ok) throw new AssertionError(mensaje);
    }
    static long numero(Connection cn, String sql) throws Exception {
        try (Statement st=cn.createStatement(); ResultSet rs=st.executeQuery(sql)) { rs.next(); return rs.getLong(1); }
    }
    static void ejecutar(Connection cn,String sql) throws Exception {
        try (Statement st=cn.createStatement()) { st.execute(sql); }
    }
    static void cargar(Connection cn,Path archivo) throws Exception {
        StringBuilder bloque=new StringBuilder(); boolean rutina=false;
        for(String linea:Files.readAllLines(archivo)) {
            String t=linea.trim();
            if(t.startsWith("--") || t.isEmpty()) continue;
            if(t.startsWith("CREATE DATABASE") || t.startsWith("USE ")) {
                // El esquema se crea antes, con nombre de prueba.
                if(t.startsWith("CREATE DATABASE") && !t.endsWith(";")) bloque.setLength(0);
                continue;
            }
            if(t.startsWith("CHARACTER SET")) continue;
            if(t.startsWith("CREATE PROCEDURE") || t.startsWith("CREATE TRIGGER")) rutina=true;
            bloque.append(linea).append('\n');
            // En estos scripts solo el END de la rutina empieza en la primera columna.
            if ((rutina && linea.stripTrailing().equals("END;")) || (!rutina && t.endsWith(";"))) {
                try {
                    ejecutar(cn, bloque.toString());
                } catch (SQLException ex) {
                    String sentencia = bloque.toString().lines().findFirst().orElse("");
                    throw new SQLException(archivo.getFileName() + " [" + sentencia + "]: " + ex.getMessage(),
                            ex.getSQLState(), ex.getErrorCode(), ex);
                }
                bloque.setLength(0);
                rutina = false;
            }
        }
        comprobar(bloque.toString().isBlank(),"Bloque SQL incompleto en "+archivo);
    }
    static int pedido(Connection cn,String json) throws Exception {
        try(CallableStatement cs=cn.prepareCall("{call sp_registrar_pedido_multiple(?,?,?)}")) {
            cs.setNull(1,Types.INTEGER); cs.setInt(2,1); cs.setString(3,json);
            boolean r=cs.execute(); while(!r && cs.getUpdateCount()!=-1)r=cs.getMoreResults();
            try(ResultSet rs=cs.getResultSet()){rs.next();return rs.getInt("id_pedido");}
        }
    }
    public static void main(String[] args) throws Exception {
        raiz=Path.of(args[0]).toAbsolutePath();
        // La configuracion privada se lee por ConexionBD; no se imprime ni se copia.
        System.setProperty("user.dir",args[1]);
        esquema="modo_shake_prueba_"+System.currentTimeMillis();
        try(Connection cn=ConexionBD.abrir()) {
            ejecutar(cn,"CREATE DATABASE "+esquema+" CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci");
            try {
                cn.setCatalog(esquema);
                cargar(cn,raiz.resolve("database/01_esquema_mysql.sql"));
                cargar(cn,raiz.resolve("database/03_rutinas_avance_final.sql"));
                cargar(cn,raiz.resolve("database/04_operaciones_aplicacion.sql"));
                ejecutar(cn,"INSERT IGNORE INTO roles(id_rol,nombre) VALUES(1,'ADMIN'),(2,'USUARIO')");
                ejecutar(cn,"CALL sp_crear_primer_admin('Prueba','Admin','prueba','PBKDF2$prueba')");
                ejecutar(cn,"INSERT INTO categorias(id_categoria,nombre) VALUES(1,'Batidos')");
                ejecutar(cn,"CALL sp_guardar_producto(0,1,'Batido prueba','',15000,'Activo')");
                ejecutar(cn,"CALL sp_guardar_producto(1,1,'Batido prueba','',16000,'Activo')");
                comprobar(numero(cn,"SELECT COUNT(*) FROM Historial_Auditoria") == 1,"Auditoria");
                ejecutar(cn,"CALL sp_guardar_ingrediente(0,'Banano','g',10,'Activo')");
                ejecutar(cn,"CALL sp_guardar_ingrediente(0,'Leche','ml',10,'Activo')");
                ejecutar(cn,"CALL sp_ajustar_inventario(1,1000)");
                ejecutar(cn,"CALL sp_ajustar_inventario(2,1000)");
                ejecutar(cn,"INSERT INTO producto_ingrediente VALUES(1,1,80),(1,2,120)");
                int id=pedido(cn,"[{\"producto\":1,\"cantidad\":2}]");
                comprobar(numero(cn,"SELECT cantidad_actual FROM inventario WHERE ingrediente_id=1")==840,"Descuento al registrar");
                ejecutar(cn,"CALL sp_cambiar_estado_pedido("+id+",'En preparación')");
                comprobar(numero(cn,"SELECT cantidad_actual FROM inventario WHERE ingrediente_id=1")==840,"No descontar dos veces");
                ejecutar(cn,"CALL sp_cambiar_estado_pedido("+id+",'Cancelado')");
                comprobar(numero(cn,"SELECT cantidad_actual FROM inventario WHERE ingrediente_id=1")==1000,"Devolver consumo");
                ejecutar(cn,"CALL sp_cambiar_estado_pedido("+id+",'Cancelado')");
                comprobar(numero(cn,"SELECT cantidad_actual FROM inventario WHERE ingrediente_id=1")==1000,"No devolver dos veces");
                long antes=numero(cn,"SELECT COUNT(*) FROM pedidos");
                ejecutar(cn,"CALL sp_guardar_producto(0,1,'Sin receta','',100,'Activo')");
                try {pedido(cn,"[{\"producto\":1,\"cantidad\":1},{\"producto\":2,\"cantidad\":1}]");throw new AssertionError("Debio fallar");}
                catch(SQLException esperado) { comprobar("45000".equals(esperado.getSQLState()),"Error esperado de receta"); }
                comprobar(numero(cn,"SELECT COUNT(*) FROM pedidos")==antes,"Rollback pedido");
                comprobar(numero(cn,"SELECT cantidad_actual FROM inventario WHERE ingrediente_id=1")==1000,"Rollback inventario");
                try {pedido(cn,"[{\"producto\":1,\"cantidad\":100}]");throw new AssertionError("Debio fallar stock");}
                catch(SQLException esperado) { comprobar("45000".equals(esperado.getSQLState()),"Error esperado de stock"); }
                comprobar(numero(cn,"SELECT COUNT(*) FROM pedidos")==antes,"Rollback por stock");
                int pago=pedido(cn,"[{\"producto\":1,\"cantidad\":1}]");
                ejecutar(cn,"CALL sp_registrar_pago("+pago+",'Efectivo',1000)");
                try {ejecutar(cn,"CALL sp_registrar_pago("+pago+",'Efectivo',999999)");throw new AssertionError("Debio rechazar exceso");}
                catch(SQLException esperado) {comprobar("45000".equals(esperado.getSQLState()),"Exceso de pago");}
                comprobar(numero(cn,"SELECT COUNT(*) FROM pagos")==1,"Pago atomico");
                try(CallableStatement cs=cn.prepareCall("{call sp_consultar_pedidos(?,?)}")) {
                    cs.setDate(1,java.sql.Date.valueOf("2000-01-01")); cs.setDate(2,java.sql.Date.valueOf("2099-12-31"));
                    try(ResultSet rs=cs.executeQuery()){comprobar(rs.next(),"Reporte");}
                }
                System.out.println("OK: esquema, rutinas, auditoria, inventario, cancelacion, rollback, pagos y reporte.");
            } finally { cn.setCatalog("modo_shake"); ejecutar(cn,"DROP DATABASE "+esquema); }
        }
    }
}
