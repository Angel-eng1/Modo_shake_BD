package modo_shake;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class RecetaDAO {
    public List<ComponenteReceta> listar(int idProducto) throws SQLException {
        String sql = """
                SELECT i.id_ingrediente, i.nombre, i.unidad_medida,
                       r.cantidad_requerida, i.estado
                FROM producto_ingrediente r
                INNER JOIN ingredientes i ON i.id_ingrediente = r.id_ingrediente
                WHERE r.id_producto = ?
                ORDER BY i.nombre, i.id_ingrediente
                """;
        List<ComponenteReceta> componentes = new ArrayList<>();
        try (Connection cn = ConexionBD.abrir();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    componentes.add(new ComponenteReceta(
                            rs.getInt("id_ingrediente"), rs.getString("nombre"),
                            rs.getString("unidad_medida"), rs.getBigDecimal("cantidad_requerida"),
                            "Activo".equals(rs.getString("estado"))));
                }
            }
        }
        return componentes;
    }

    public void guardar(int idProducto, List<ComponenteReceta> componentes) throws SQLException {
        StringJoiner json = new StringJoiner(",", "[", "]");
        for (ComponenteReceta componente : componentes) {
            if (componente.cantidad() == null) {
                throw new SQLException("La cantidad es obligatoria.");
            }
            json.add("{\"ingrediente\":" + componente.idIngrediente()
                    + ",\"cantidad\":" + componente.cantidad().toPlainString() + "}");
        }
        Rutinas.ejecutar("{call sp_guardar_receta(?,?)}", idProducto, json.toString());
    }
}
