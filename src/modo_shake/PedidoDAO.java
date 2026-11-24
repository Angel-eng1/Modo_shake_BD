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
        String precioSql = "SELECT precio FROM productos WHERE id_producto=? AND estado='Activo'";
        String pedidoSql = "INSERT INTO pedidos(id_cliente,id_empleado,fecha,estado) VALUES(?,?,CURRENT_DATE(),'Recibido')";
        String detalleSql = "INSERT INTO detalle_pedido(id_pedido,id_producto,cantidad,precio_unitario) VALUES(?,?,?,?)";
        try (Connection cn = ConexionBD.abrir()) {
            cn.setAutoCommit(false);
            try {
                int idPedido;
                try (PreparedStatement ps = cn.prepareStatement(pedidoSql, Statement.RETURN_GENERATED_KEYS)) {
                    if (clienteId == null || clienteId == 0) ps.setNull(1, java.sql.Types.INTEGER);
                    else ps.setInt(1, clienteId);
                    ps.setInt(2, empleadoId);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("MySQL no devolvió el identificador del pedido.");
                        idPedido = keys.getInt(1);
                    }
                }
                try (PreparedStatement precioPs = cn.prepareStatement(precioSql);
                     PreparedStatement detallePs = cn.prepareStatement(detalleSql)) {
                    for (DetallePedidoNuevo detalle : detalles) {
                        if (detalle.getCantidad() <= 0) throw new SQLException("La cantidad debe ser mayor que cero.");
                        precioPs.setInt(1, detalle.getProductoId());
                        BigDecimal precio;
                        try (ResultSet rs = precioPs.executeQuery()) {
                            if (!rs.next()) throw new SQLException("Uno de los productos ya no existe o está inactivo.");
                            precio = rs.getBigDecimal("precio");
                        }
                        detallePs.setInt(1, idPedido);
                        detallePs.setInt(2, detalle.getProductoId());
                        detallePs.setInt(3, detalle.getCantidad());
                        detallePs.setBigDecimal(4, precio);
                        detallePs.addBatch();
                    }
                    detallePs.executeBatch();
                }
                cn.commit();
                return idPedido;
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally { cn.setAutoCommit(true); }
        }
    }

    public void cambiarEstado(int idPedido, String nuevoEstado) throws SQLException {
        List<String> permitidos = List.of("Recibido", "En preparación", "Entregado", "Cancelado");
        if (!permitidos.contains(nuevoEstado)) throw new SQLException("Estado de pedido no permitido.");
        try (Connection cn = ConexionBD.abrir()) {
            cn.setAutoCommit(false);
            try {
                String actual;
                try (PreparedStatement ps = cn.prepareStatement("SELECT estado FROM pedidos WHERE id_pedido=? FOR UPDATE")) {
                    ps.setInt(1, idPedido);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("No se encontró el pedido seleccionado.");
                        actual = rs.getString(1);
                    }
                }
                if (actual.equals(nuevoEstado)) { cn.commit(); return; }
                boolean descontar = actual.equals("Recibido") && nuevoEstado.equals("En preparación");
                boolean cancelar = (actual.equals("Recibido") || actual.equals("En preparación")) && nuevoEstado.equals("Cancelado");
                boolean entregar = actual.equals("En preparación") && nuevoEstado.equals("Entregado");
                if (!(descontar || cancelar || entregar))
                    throw new SQLException("Transición no permitida: " + actual + " → " + nuevoEstado + ".");
                if (cancelar) {
                    try (PreparedStatement pagos = cn.prepareStatement("SELECT COALESCE(SUM(monto),0) FROM pagos WHERE id_pedido=? AND estado='Registrado'")) {
                        pagos.setInt(1,idPedido);
                        try (ResultSet rs = pagos.executeQuery()) {
                            rs.next();
                            if (rs.getBigDecimal(1).signum() > 0)
                                throw new SQLException("No se puede cancelar un pedido con pagos registrados. Revisa o anula los pagos primero.");
                        }
                    }
                }
                if (descontar) aplicarConsumoIngredientes(cn, idPedido, false);
                if (cancelar && actual.equals("En preparación")) aplicarConsumoIngredientes(cn, idPedido, true);
                try (PreparedStatement ps = cn.prepareStatement("UPDATE pedidos SET estado=? WHERE id_pedido=?")) {
                    ps.setString(1, nuevoEstado); ps.setInt(2, idPedido); ps.executeUpdate();
                }
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally { cn.setAutoCommit(true); }
        }
    }

    private void aplicarConsumoIngredientes(Connection cn, int idPedido, boolean devolver) throws SQLException {
        String sql = """
            SELECT i.id_ingrediente,i.nombre,i.unidad_medida,
                   SUM(dp.cantidad*pi.cantidad_requerida) AS requerido
            FROM detalle_pedido dp
            JOIN producto_ingrediente pi ON pi.id_producto=dp.id_producto
            JOIN ingredientes i ON i.id_ingrediente=pi.id_ingrediente
            WHERE dp.id_pedido=?
            GROUP BY i.id_ingrediente,i.nombre,i.unidad_medida
            """;
        try (PreparedStatement consulta = cn.prepareStatement(sql)) {
            consulta.setInt(1,idPedido);
            try (ResultSet rs = consulta.executeQuery()) {
                while (rs.next()) {
                    int ingredienteId=rs.getInt("id_ingrediente");
                    BigDecimal requerido=rs.getBigDecimal("requerido");
                    String update = devolver
                        ? "UPDATE inventario SET cantidad_actual=cantidad_actual+?,fecha_actualizacion=CURRENT_DATE() WHERE ingrediente_id=?"
                        : "UPDATE inventario SET cantidad_actual=cantidad_actual-?,fecha_actualizacion=CURRENT_DATE() WHERE ingrediente_id=? AND cantidad_actual>=?";
                    try (PreparedStatement ps=cn.prepareStatement(update)) {
                        ps.setBigDecimal(1,requerido); ps.setInt(2,ingredienteId);
                        if (!devolver) ps.setBigDecimal(3,requerido);
                        if (ps.executeUpdate()!=1) {
                            String accion=devolver?"No fue posible devolver al inventario el insumo ":"No hay existencias suficientes del insumo ";
                            throw new SQLException(accion+rs.getString("nombre")+" ("+rs.getString("unidad_medida")+").");
                        }
                    }
                }
            }
        }
    }

    public static class DetallePedidoNuevo {
    private final int productoId;
    private final int cantidad;
    public DetallePedidoNuevo(int productoId, int cantidad) { this.productoId=productoId; this.cantidad=cantidad; }
    public int getProductoId() { return productoId; }
    public int getCantidad() { return cantidad; }
    }
}