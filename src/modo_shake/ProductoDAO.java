package modo_shake;

import java.sql.Connection;
import java.sql.CallableStatement;
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
        try (Connection cn = ConexionBD.abrir();
             CallableStatement cs = cn.prepareCall("{call sp_guardar_producto(?,?,?,?,?,?)}")) {
            cs.setInt(1, producto.getId());
            cs.setInt(2, producto.getCategoriaId());
            cs.setString(3, producto.getNombre());
            cs.setString(4, producto.getDescripcion());
            cs.setBigDecimal(5, producto.getPrecio());
            cs.setString(6, producto.isActivo() ? "Activo" : "Inactivo");
            boolean resultado = cs.execute();
            while (!resultado && cs.getUpdateCount() != -1) resultado = cs.getMoreResults();
            if (resultado) {
                try (ResultSet rs = cs.getResultSet()) {
                    if (rs.next()) return rs.getInt("id_producto");
                }
            }
            throw new SQLException("El procedimiento no devolvio el identificador del producto.");
        }
    }

    public void eliminar(int id) throws SQLException {
        try (Connection cn = ConexionBD.abrir();
             CallableStatement cs = cn.prepareCall("{call sp_eliminar_producto(?)}")) {
            cs.setInt(1, id);
            cs.execute();
        }
    }

    public List<Producto> buscar(String texto) throws SQLException {
        List<Producto> productos = new ArrayList<>();
        String sql = CONSULTA + " WHERE p.nombre LIKE ? OR c.nombre LIKE ? ORDER BY p.id_producto";
        try (Connection cn = ConexionBD.abrir(); PreparedStatement ps = cn.prepareStatement(sql)) {
            String filtro = "%" + texto.trim() + "%";
            ps.setString(1, filtro);
            ps.setString(2, filtro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) productos.add(leer(rs));
            }
        }
        return productos;
    }

    private Producto leer(ResultSet rs) throws SQLException {
        return new Producto(rs.getInt("id_producto"), rs.getInt("id_categoria"),
                rs.getString("nombre"), rs.getString("categoria"), rs.getBigDecimal("precio"),
                rs.getString("descripcion"), "Activo".equals(rs.getString("estado")));
    }
}

