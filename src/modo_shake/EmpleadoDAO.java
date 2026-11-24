package modo_shake;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class EmpleadoDAO {
    public UsuarioSesion autenticar(String usuario, char[] contrasena) throws SQLException {
        String sql = """
            SELECT e.id_empleado, e.nombre, e.apellido, e.contrasena_hash, r.nombre AS perfil
            FROM empleados e
            INNER JOIN roles r ON r.id_rol = e.id_rol
            WHERE e.usuario = ? AND e.estado = 'Activo'
            """;
        try (Connection cn = ConexionBD.abrir(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                String hash = rs.getString("contrasena_hash");
                if (!Contrasenas.verificar(contrasena, hash)) return null;
                UsuarioSesion.Perfil perfil = UsuarioSesion.Perfil.valueOf(rs.getString("perfil").toUpperCase());
                String nombre = rs.getString("nombre") + " " + rs.getString("apellido");
                return new UsuarioSesion(rs.getInt("id_empleado"), usuario, nombre, perfil);
            }
        } catch (IllegalArgumentException ex) {
            throw new SQLException("El perfil asignado a este empleado no es válido.", ex);
        } finally {
            Arrays.fill(contrasena, '\0');
        }
    }

    public void crearPrimerAdministrador(String nombre, String apellido, String usuario,
                                         char[] contrasena) throws SQLException {
        String comprobar = """
            SELECT COUNT(*)
            FROM empleados e JOIN roles r ON r.id_rol = e.id_rol
            WHERE r.nombre = 'ADMIN' AND e.estado = 'Activo'
              AND e.contrasena_hash LIKE 'PBKDF2$%'
            """;
        String insertar = """
            INSERT INTO empleados
                (nombre, apellido, puesto, id_rol, usuario, contrasena_hash, estado)
            SELECT ?, ?, 'Administradora', id_rol, ?, ?, 'Activo'
            FROM roles WHERE nombre = 'ADMIN'
            """;
        String hash = Contrasenas.generarHash(contrasena);
        Arrays.fill(contrasena, '\0');
        try (Connection cn = ConexionBD.abrir()) {
            cn.setAutoCommit(false);
            try (Statement st = cn.createStatement(); ResultSet rs = st.executeQuery(comprobar)) {
                rs.next();
                if (rs.getInt(1) > 0) {
                    cn.rollback();
                    throw new SQLException("Ya existe un administrador activo configurado.");
                }
            }
            try (PreparedStatement ps = cn.prepareStatement(insertar)) {
                ps.setString(1, nombre);
                ps.setString(2, apellido);
                ps.setString(3, usuario);
                ps.setString(4, hash);
                if (ps.executeUpdate() != 1) {
                    cn.rollback();
                    throw new SQLException("No se encontró el perfil ADMIN. Ejecuta primero el script de roles.");
                }
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            }
            cn.commit();
        } finally {
            Arrays.fill(contrasena, '\0');
        }
    }

    public List<Empleado> listar() throws SQLException {
        String sql = """
            SELECT e.id_empleado,e.nombre,e.apellido,e.telefono,e.puesto,e.usuario,
                   r.nombre AS perfil,e.estado
            FROM empleados e JOIN roles r ON r.id_rol=e.id_rol
            ORDER BY e.id_empleado
            """;
        List<Empleado> empleados = new ArrayList<>();
        try (Connection cn = ConexionBD.abrir(); PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) empleados.add(new Empleado(rs.getInt("id_empleado"),rs.getString("nombre"),
                    rs.getString("apellido"),rs.getString("telefono"),rs.getString("puesto"),
                    rs.getString("usuario"),rs.getString("perfil"),rs.getString("estado")));
        }
        return empleados;
    }

    public void guardar(Empleado empleado, char[] contrasena) throws SQLException {
        boolean nuevo = empleado.getId() == 0;
        if (nuevo && (contrasena == null || contrasena.length < 8))
            throw new SQLException("La contraseña para un empleado nuevo debe tener al menos 8 caracteres.");
        String hash = contrasena == null || contrasena.length == 0 ? null : Contrasenas.generarHash(contrasena);
        if (contrasena != null) Arrays.fill(contrasena, '\0');
        String sql = nuevo
            ? """
              INSERT INTO empleados(nombre,apellido,telefono,puesto,id_rol,usuario,contrasena_hash,estado)
              SELECT ?,?,?,?,r.id_rol,?,?,? FROM roles r WHERE r.nombre=?
              """
            : hash == null
              ? """
                UPDATE empleados e JOIN roles r ON r.nombre=?
                SET e.nombre=?,e.apellido=?,e.telefono=?,e.puesto=?,e.id_rol=r.id_rol,
                    e.usuario=?,e.estado=? WHERE e.id_empleado=?
                """
              : """
                UPDATE empleados e JOIN roles r ON r.nombre=?
                SET e.nombre=?,e.apellido=?,e.telefono=?,e.puesto=?,e.id_rol=r.id_rol,
                    e.usuario=?,e.contrasena_hash=?,e.estado=? WHERE e.id_empleado=?
                """;
        try (Connection cn = ConexionBD.abrir(); PreparedStatement ps = cn.prepareStatement(sql)) {
            if (nuevo) {
                ps.setString(1,empleado.getNombre()); ps.setString(2,empleado.getApellido());
                ps.setString(3,empleado.getTelefono().isBlank()?null:empleado.getTelefono());
                ps.setString(4,empleado.getPuesto()); ps.setString(5,empleado.getUsuario());
                ps.setString(6,hash); ps.setString(7,empleado.getEstado()); ps.setString(8,empleado.getPerfil());
            } else if (hash == null) {
                ps.setString(1,empleado.getPerfil()); ps.setString(2,empleado.getNombre());
                ps.setString(3,empleado.getApellido()); ps.setString(4,empleado.getTelefono().isBlank()?null:empleado.getTelefono());
                ps.setString(5,empleado.getPuesto()); ps.setString(6,empleado.getUsuario());
                ps.setString(7,empleado.getEstado()); ps.setInt(8,empleado.getId());
            } else {
                ps.setString(1,empleado.getPerfil()); ps.setString(2,empleado.getNombre());
                ps.setString(3,empleado.getApellido()); ps.setString(4,empleado.getTelefono().isBlank()?null:empleado.getTelefono());
                ps.setString(5,empleado.getPuesto()); ps.setString(6,empleado.getUsuario());
                ps.setString(7,hash); ps.setString(8,empleado.getEstado()); ps.setInt(9,empleado.getId());
            }
            if (ps.executeUpdate() != 1) throw new SQLException("No se encontró el perfil o empleado indicado.");
        } finally {
            if (contrasena != null) Arrays.fill(contrasena, '\0');
        }
    }

}
