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
        Rutinas.ejecutar("{call sp_guardar_ingrediente(?,?,?,?,?)}", i.getId(), i.getNombre(),
                i.getUnidad(), i.getStockMinimo(), i.isActivo() ? "Activo" : "Inactivo");
    }

    public void eliminar(int id) throws SQLException {
        Rutinas.ejecutar("{call sp_eliminar_ingrediente(?)}", id);
    }
}
