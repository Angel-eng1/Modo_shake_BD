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

    public void guardar(Cliente cliente) throws SQLException {
        String sql = cliente.getId() == 0
                ? "INSERT INTO clientes(nombre,telefono,correo) VALUES(?,?,?)"
                : "UPDATE clientes SET nombre=?,telefono=?,correo=? WHERE id_cliente=?";
        try (Connection cn = ConexionBD.abrir(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getTelefono().isBlank() ? null : cliente.getTelefono());
            ps.setString(3, cliente.getCorreo().isBlank() ? null : cliente.getCorreo());
            if (cliente.getId() != 0) ps.setInt(4, cliente.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        try (Connection cn = ConexionBD.abrir();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM clientes WHERE id_cliente=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
