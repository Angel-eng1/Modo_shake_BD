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
        if (monto == null || monto.signum() <= 0) throw new SQLException("El monto debe ser mayor que cero.");
        try (Connection cn = ConexionBD.abrir()) {
            cn.setAutoCommit(false);
            try {
                try (PreparedStatement lock = cn.prepareStatement("SELECT estado FROM pedidos WHERE id_pedido=? FOR UPDATE")) {
                    lock.setInt(1,pedidoId);
                    try (ResultSet rs = lock.executeQuery()) {
                        if (!rs.next()) throw new SQLException("No se encontró el pedido.");
                        if ("Cancelado".equals(rs.getString("estado"))) throw new SQLException("No se pueden registrar pagos para un pedido cancelado.");
                    }
                }
                BigDecimal total = BigDecimal.ZERO;
                String totalSql = "SELECT COALESCE(SUM(cantidad*precio_unitario),0) FROM detalle_pedido WHERE id_pedido=?";
                try (PreparedStatement ps = cn.prepareStatement(totalSql)) {
                    ps.setInt(1, pedidoId);
                    try (ResultSet rs = ps.executeQuery()) { rs.next(); total = rs.getBigDecimal(1); }
                }
                BigDecimal pagado = BigDecimal.ZERO;
                String pagosSql = "SELECT COALESCE(SUM(monto),0) FROM pagos WHERE id_pedido=? AND estado='Registrado'";
                try (PreparedStatement ps = cn.prepareStatement(pagosSql)) {
                    ps.setInt(1, pedidoId);
                    try (ResultSet rs = ps.executeQuery()) { rs.next(); pagado = rs.getBigDecimal(1); }
                }
                if (total.signum() == 0) throw new SQLException("El pedido no tiene productos registrados.");
                if (monto.compareTo(total.subtract(pagado)) > 0)
                    throw new SQLException("El monto excede el saldo pendiente de " + total.subtract(pagado) + ".");
                String insert = "INSERT INTO pagos(id_pedido,fecha_pago,metodo_pago,monto,estado) VALUES(?,CURRENT_DATE(),?,?,'Registrado')";
                try (PreparedStatement ps = cn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, pedidoId);
                    ps.setString(2, metodo);
                    ps.setBigDecimal(3, monto);
                    ps.executeUpdate();
                }
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally { cn.setAutoCommit(true); }
        }
    }
}
