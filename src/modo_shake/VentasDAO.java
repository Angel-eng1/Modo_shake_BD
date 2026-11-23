package modo_shake;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class VentasDAO {
    public List<Object[]> listar() throws SQLException {
        String sql = """
            SELECT p.id_pedido,p.fecha,COALESCE(c.nombre,'CLIENTE GENERAL') AS cliente,
                   COALESCE(d.total,0) AS total,COALESCE(pg.pagado,0) AS pagado,
                   GREATEST(COALESCE(d.total,0)-COALESCE(pg.pagado,0),0) AS saldo,
                   CASE WHEN COALESCE(d.total,0)=0 THEN 'Sin detalle'
                        WHEN COALESCE(pg.pagado,0)>=d.total THEN 'Pagado'
                        WHEN COALESCE(pg.pagado,0)>0 THEN 'Abono parcial'
                        ELSE 'Pendiente' END AS estado
            FROM pedidos p
            LEFT JOIN clientes c ON c.id_cliente=p.id_cliente
            LEFT JOIN (SELECT id_pedido,SUM(cantidad*precio_unitario) AS total
                       FROM detalle_pedido GROUP BY id_pedido) d ON d.id_pedido=p.id_pedido
            LEFT JOIN (SELECT id_pedido,SUM(monto) AS pagado
                       FROM pagos WHERE estado='Registrado' GROUP BY id_pedido) pg ON pg.id_pedido=p.id_pedido
            WHERE p.estado <> 'Cancelado'
            ORDER BY p.fecha DESC,p.id_pedido DESC
            """;
        List<Object[]> filas = new ArrayList<>();
        try (Connection cn = ConexionBD.abrir(); PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) filas.add(new Object[]{rs.getInt("id_pedido"), rs.getDate("fecha"),
                    rs.getString("cliente"), rs.getBigDecimal("total"), rs.getBigDecimal("pagado"),
                    rs.getBigDecimal("saldo"), rs.getString("estado")});
        }
        return filas;
    }

    public void registrarPago(int pedidoId, String metodo, BigDecimal monto) throws SQLException {
        Rutinas.ejecutar("{call sp_registrar_pago(?,?,?)}", pedidoId, metodo, monto);
    }
}
