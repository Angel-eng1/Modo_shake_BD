package modo_shake;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {
    public List<Cliente> listar() throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.abrir();
             PreparedStatement ps = cn.prepareStatement("SELECT id_cliente,nombre,telefono,correo FROM clientes ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(new Cliente(rs.getInt("id_cliente"), rs.getString("nombre"),
                    rs.getString("telefono"), rs.getString("correo")));
        }
        return lista;
    }

    public void guardar(Cliente c) throws SQLException {
        Rutinas.ejecutar("{call sp_guardar_cliente(?,?,?,?)}", c.getId(), c.getNombre(),
                c.getTelefono().isBlank() ? null : c.getTelefono(), c.getCorreo().isBlank() ? null : c.getCorreo());
    }

    public void eliminar(int id) throws SQLException {
        Rutinas.ejecutar("{call sp_eliminar_cliente(?)}", id);
    }
}
