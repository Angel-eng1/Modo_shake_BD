package modo_shake;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DashboardDAO {
    public ResumenDashboard cargar() throws SQLException {
        try (Connection cn = ConexionBD.abrir()) {
            int pedidos = contar(cn, "SELECT COUNT(*) FROM pedidos WHERE fecha=CURRENT_DATE()");
            BigDecimal ventas = decimal(cn, """
                SELECT COALESCE(SUM(monto),0) FROM pagos
                WHERE fecha_pago=CURRENT_DATE() AND estado='Registrado'
                """);
            int productos = contar(cn, "SELECT COUNT(*) FROM productos WHERE estado='Activo'");
            int stockBajo = contar(cn, """
                SELECT COUNT(*) FROM ingredientes i
                LEFT JOIN inventario v ON v.ingrediente_id=i.id_ingrediente
                WHERE i.estado='Activo' AND COALESCE(v.cantidad_actual,0)<=i.stock_minimo
                """);
            return new ResumenDashboard(pedidos, ventas, productos, stockBajo,
                    pedidosRecientes(cn), stockBajo(cn));
        }
    }

    private int contar(Connection cn, String sql) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            rs.next(); return rs.getInt(1);
        }
    }
    private BigDecimal decimal(Connection cn, String sql) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            rs.next(); return rs.getBigDecimal(1);
        }
    }
    private List<Object[]> pedidosRecientes(Connection cn) throws SQLException {
        String sql = """
            SELECT p.id_pedido,COALESCE(c.nombre,'CLIENTE GENERAL') AS cliente,
                   COALESCE(d.total,0) AS total,p.estado
            FROM pedidos p LEFT JOIN clientes c ON c.id_cliente=p.id_cliente
            LEFT JOIN (SELECT id_pedido,SUM(cantidad*precio_unitario) AS total
                       FROM detalle_pedido GROUP BY id_pedido) d ON d.id_pedido=p.id_pedido
            ORDER BY p.fecha DESC,p.id_pedido DESC LIMIT 6
            """;
        List<Object[]> filas = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) filas.add(new Object[]{rs.getInt("id_pedido"), rs.getString("cliente"),
                    rs.getBigDecimal("total"), rs.getString("estado")});
        }
        return filas;
    }
    private List<Object[]> stockBajo(Connection cn) throws SQLException {
        String sql = """
            SELECT i.nombre,i.unidad_medida,COALESCE(v.cantidad_actual,0) AS cantidad
            FROM ingredientes i LEFT JOIN inventario v ON v.ingrediente_id=i.id_ingrediente
            WHERE i.estado='Activo' AND COALESCE(v.cantidad_actual,0)<=i.stock_minimo
            ORDER BY COALESCE(v.cantidad_actual,0),i.nombre LIMIT 5
            """;
        List<Object[]> filas = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) filas.add(new Object[]{rs.getString("nombre"),
                    rs.getBigDecimal("cantidad").stripTrailingZeros().toPlainString(), rs.getString("unidad_medida")});
        }
        return filas;
    }

    public static class ResumenDashboard {
    private final int pedidosHoy;
    private final BigDecimal ventasHoy;
    private final int productosActivos;
    private final int stockBajo;
    private final List<Object[]> pedidosRecientes;
    private final List<Object[]> alertasStock;

    public ResumenDashboard(int pedidosHoy, BigDecimal ventasHoy, int productosActivos, int stockBajo,
                            List<Object[]> pedidosRecientes, List<Object[]> alertasStock) {
        this.pedidosHoy = pedidosHoy;
        this.ventasHoy = ventasHoy;
        this.productosActivos = productosActivos;
        this.stockBajo = stockBajo;
        this.pedidosRecientes = pedidosRecientes;
        this.alertasStock = alertasStock;
    }
    public static ResumenDashboard vacio() {
        return new ResumenDashboard(0, BigDecimal.ZERO, 0, 0, new ArrayList<>(), new ArrayList<>());
    }
    public int getPedidosHoy() { return pedidosHoy; }
    public BigDecimal getVentasHoy() { return ventasHoy; }
    public int getProductosActivos() { return productosActivos; }
    public int getStockBajo() { return stockBajo; }
    public List<Object[]> getPedidosRecientes() { return pedidosRecientes; }
    public List<Object[]> getAlertasStock() { return alertasStock; }
    }
}