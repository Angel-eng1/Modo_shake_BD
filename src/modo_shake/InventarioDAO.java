package modo_shake;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InventarioDAO {
    public List<Object[]> listar() throws SQLException {
        String sql = """
            SELECT i.id_ingrediente, i.nombre, i.unidad_medida,
                   COALESCE(v.cantidad_actual, 0) AS cantidad_actual,
                   i.stock_minimo, v.fecha_actualizacion,
                   CASE WHEN v.ingrediente_id IS NULL THEN 'Sin registro'
                        WHEN v.cantidad_actual <= i.stock_minimo THEN 'Stock bajo'
                        ELSE 'Normal' END AS situacion
            FROM ingredientes i
            LEFT JOIN inventario v ON v.ingrediente_id = i.id_ingrediente
            ORDER BY (v.ingrediente_id IS NULL) DESC,
                     (v.cantidad_actual <= i.stock_minimo) DESC, i.nombre
            """;
        List<Object[]> filas = new ArrayList<>();
        try (Connection cn = ConexionBD.abrir(); PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Object fecha = rs.getDate("fecha_actualizacion");
                filas.add(new Object[]{rs.getInt("id_ingrediente"), rs.getString("nombre"),
                    rs.getString("unidad_medida"), rs.getBigDecimal("cantidad_actual").stripTrailingZeros().toPlainString(),
                    rs.getBigDecimal("stock_minimo").stripTrailingZeros().toPlainString(),
                    fecha == null ? "—" : fecha.toString(), rs.getString("situacion")});
            }
        }
        return filas;
    }

    public void ajustar(int ingredienteId, BigDecimal cantidad) throws SQLException {
        Rutinas.ejecutar("{call sp_ajustar_inventario(?,?)}", ingredienteId, cantidad);
    }
}
