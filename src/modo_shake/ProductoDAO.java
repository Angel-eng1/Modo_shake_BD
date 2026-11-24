package modo_shake;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modo_shake.Producto;

public class ProductoDAO {
    private static final String CONSULTA = """
        SELECT p.id_producto, p.id_categoria, c.nombre AS categoria,
               p.nombre, p.precio, p.descripcion, p.estado
        FROM productos p
        INNER JOIN categorias c ON c.id_categoria = p.id_categoria
        """;

    public List<Categoria> listarCategorias() throws SQLException {
        String sql = "SELECT id_categoria, nombre FROM categorias ORDER BY nombre";
        List<Categoria> categorias = new ArrayList<>();
        try (Connection cn = ConexionBD.abrir();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) categorias.add(new Categoria(rs.getInt("id_categoria"), rs.getString("nombre")));
        }
        return categorias;
    }

    public List<Producto> listar() throws SQLException {
        List<Producto> productos = new ArrayList<>();
        try (Connection cn = ConexionBD.abrir();
             PreparedStatement ps = cn.prepareStatement(CONSULTA + " ORDER BY p.id_producto");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) productos.add(leer(rs));
        }
        return productos;
    }

    public List<Producto> listarActivos() throws SQLException {
        List<Producto> productos = new ArrayList<>();
        try (Connection cn = ConexionBD.abrir();
             PreparedStatement ps = cn.prepareStatement(CONSULTA + " WHERE p.estado = 'Activo' ORDER BY p.nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) productos.add(leer(rs));
        }
        return productos;
    }

    public int guardar(Producto producto) throws SQLException {
        if (producto.getId() == 0) {
            String sql = "INSERT INTO productos (id_categoria,nombre,descripcion,precio,estado) VALUES (?,?,?,?,?)";
            try (Connection cn = ConexionBD.abrir();
                 PreparedStatement ps = cn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                enlazar(ps, producto);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
                throw new SQLException("MySQL no devolvió el identificador del producto creado.");
            }
        }
        String sql = "UPDATE productos SET id_categoria=?, nombre=?, descripcion=?, precio=?, estado=? WHERE id_producto=?";
        try (Connection cn = ConexionBD.abrir(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, producto.getCategoriaId());
            ps.setString(2, producto.getNombre());
            ps.setString(3, producto.getDescripcion());
            ps.setBigDecimal(4, producto.getPrecio());
            ps.setString(5, producto.isActivo() ? "Activo" : "Inactivo");
            ps.setInt(6, producto.getId());
            ps.executeUpdate();
        }
        return producto.getId();
    }

    public void eliminar(int id) throws SQLException {
        try (Connection cn = ConexionBD.abrir();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM productos WHERE id_producto=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private void enlazar(PreparedStatement ps, Producto p) throws SQLException {
        ps.setInt(1, p.getCategoriaId());
        ps.setString(2, p.getNombre());
        ps.setString(3, p.getDescripcion());
        ps.setBigDecimal(4, p.getPrecio());
        ps.setString(5, p.isActivo() ? "Activo" : "Inactivo");
    }

    private Producto leer(ResultSet rs) throws SQLException {
        return new Producto(rs.getInt("id_producto"), rs.getInt("id_categoria"),
                rs.getString("nombre"), rs.getString("categoria"), rs.getBigDecimal("precio"),
                rs.getString("descripcion"), "Activo".equals(rs.getString("estado")));
    }
}

