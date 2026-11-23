package modo_shake;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAO {
    public List<Object[]> listar() throws SQLException {
        String sql = """
            SELECT p.id_pedido, p.fecha, COALESCE(c.nombre,'CLIENTE GENERAL') AS cliente,
                   CONCAT(e.nombre,' ',e.apellido) AS empleado,
                   COALESCE(d.total,0) AS total, p.estado
            FROM pedidos p
            LEFT JOIN clientes c ON c.id_cliente=p.id_cliente
            INNER JOIN empleados e ON e.id_empleado=p.id_empleado
            LEFT JOIN (
                SELECT id_pedido,SUM(cantidad*precio_unitario) AS total
                FROM detalle_pedido GROUP BY id_pedido
            ) d ON d.id_pedido=p.id_pedido
            ORDER BY p.fecha DESC,p.id_pedido DESC
            """;
        List<Object[]> filas = new ArrayList<>();
        try (Connection cn = ConexionBD.abrir(); PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) filas.add(new Object[]{rs.getInt("id_pedido"), rs.getDate("fecha"),
                    rs.getString("cliente"), rs.getString("empleado"), rs.getBigDecimal("total"), rs.getString("estado")});
        }
        return filas;
    }

    public int crear(Integer clienteId, int empleadoId, List<DetallePedidoNuevo> detalles) throws SQLException {
        if (detalles == null || detalles.isEmpty()) throw new SQLException("Agrega al menos un producto al pedido.");
        StringBuilder json = new StringBuilder("[");
        for (DetallePedidoNuevo d : detalles) {
            if (d.getCantidad() <= 0) throw new SQLException("La cantidad debe ser positiva.");
            if (json.length() > 1) json.append(',');
            json.append("{\"producto\":").append(d.getProductoId())
                .append(",\"cantidad\":").append(d.getCantidad()).append('}');
        }
        json.append(']');
        return Rutinas.identificador("{call sp_registrar_pedido_multiple(?,?,?)}", "id_pedido",
                clienteId == null || clienteId == 0 ? null : clienteId, empleadoId, json.toString());
    }

    public void cambiarEstado(int idPedido, String nuevoEstado) throws SQLException {
        Rutinas.ejecutar("{call sp_cambiar_estado_pedido(?,?)}", idPedido, nuevoEstado);
    }

    // El consumo y la devolucion pertenecen exclusivamente al motor SQL.


    public static class DetallePedidoNuevo {
    private final int productoId;
    private final int cantidad;
    public DetallePedidoNuevo(int productoId, int cantidad) { this.productoId=productoId; this.cantidad=cantidad; }
    public int getProductoId() { return productoId; }
    public int getCantidad() { return cantidad; }
    }
}