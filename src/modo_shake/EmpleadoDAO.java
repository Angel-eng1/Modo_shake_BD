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
        try {
            if (contrasena == null || contrasena.length < 8) throw new SQLException("La clave requiere al menos 8 caracteres.");
            Rutinas.ejecutar("{call sp_crear_primer_admin(?,?,?,?)}", nombre, apellido, usuario,
                    Contrasenas.generarHash(contrasena));
        } finally { if (contrasena != null) Arrays.fill(contrasena, '\0'); }
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

    public void guardar(Empleado e, char[] contrasena) throws SQLException {
        try {
            boolean cambiar = contrasena != null && contrasena.length > 0;
            if ((e.getId() == 0 || cambiar) && (contrasena == null || contrasena.length < 8))
                throw new SQLException("La clave requiere al menos 8 caracteres.");
            String hash = cambiar ? Contrasenas.generarHash(contrasena) : null;
            Rutinas.ejecutar("{call sp_guardar_empleado(?,?,?,?,?,?,?,?,?)}", e.getId(), e.getNombre(),
                    e.getApellido(), e.getTelefono(), e.getPuesto(), e.getUsuario(), e.getPerfil(), e.getEstado(), hash);
        } finally { if (contrasena != null) Arrays.fill(contrasena, '\0'); }
    }

}
