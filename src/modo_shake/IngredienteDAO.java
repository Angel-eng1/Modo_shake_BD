package modo_shake;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class IngredienteDAO {
    public List<Ingrediente> listar() throws SQLException {
        List<Ingrediente> lista = new ArrayList<>();
        String sql = "SELECT id_ingrediente,nombre,unidad_medida,stock_minimo,estado FROM ingredientes ORDER BY nombre";
        try (Connection cn = ConexionBD.abrir(); PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(new Ingrediente(rs.getInt("id_ingrediente"), rs.getString("nombre"),
                    rs.getString("unidad_medida"), rs.getBigDecimal("stock_minimo"),
                    "Activo".equals(rs.getString("estado"))));
        }
        return lista;
    }

    public List<Ingrediente> listarActivos() throws SQLException {
        List<Ingrediente> todos = listar();
        todos.removeIf(i -> !i.isActivo());
        return todos;
    }

    public void guardar(Ingrediente i) throws SQLException {
        String sql = i.getId() == 0
                ? "INSERT INTO ingredientes(nombre,unidad_medida,stock_minimo,estado) VALUES(?,?,?,?)"
                : "UPDATE ingredientes SET nombre=?,unidad_medida=?,stock_minimo=?,estado=? WHERE id_ingrediente=?";
        try (Connection cn = ConexionBD.abrir(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, i.getNombre());
            ps.setString(2, i.getUnidad());
            ps.setBigDecimal(3, i.getStockMinimo());
            ps.setString(4, i.isActivo() ? "Activo" : "Inactivo");
            if (i.getId() != 0) ps.setInt(5, i.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        try (Connection cn = ConexionBD.abrir();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM ingredientes WHERE id_ingrediente=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
