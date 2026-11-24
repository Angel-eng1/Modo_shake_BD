package modo_shake;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class ProductosController {
    private final ProductosView vista = new ProductosView();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private List<Producto> productos = new ArrayList<>();
    private int seleccionado = -1;

    public ProductosController() {
        conectarEventos();
        cargarDatos();
    }

    public ProductosView getVista() { return vista; }

    private void conectarEventos() {
        vista.alGuardar(this::guardar);
        vista.alActualizar(this::actualizar);
        vista.alEliminar(this::eliminar);
        vista.alLimpiar(this::limpiar);
        vista.alSeleccionar(this::seleccionar);
    }

    private void cargarDatos() {
        try {
            vista.setCategorias(productoDAO.listarCategorias());
            refrescarProductos();
        } catch (SQLException ex) {
            mostrarError(ex);
        }
    }

    private void refrescarProductos() throws SQLException {
        productos = productoDAO.listar();
        mostrarTabla();
    }

    private Producto leerFormulario(int id) {
        if (vista.getNombre().isBlank()) { aviso("Escribe el nombre del producto."); return null; }
        if (vista.getCategoriaId() <= 0) { aviso("Selecciona una categoría."); return null; }
        try {
            BigDecimal precio = new BigDecimal(vista.getPrecio().replace(",", "."));
            if (precio.signum() < 0) throw new NumberFormatException();
            return new Producto(id, vista.getCategoriaId(), vista.getNombre(), vista.getCategoria(),
                    precio, vista.getDescripcion(), vista.isActivo());
        } catch (NumberFormatException ex) {
            aviso("Ingresa un precio válido, por ejemplo 18000 o 18000.50.");
            return null;
        }
    }

    private void guardar() {
        if (seleccionado >= 0) { aviso("Pulsa Limpiar para crear un producto nuevo."); return; }
        Producto producto = leerFormulario(0);
        if (producto == null) return;
        try {
            productoDAO.guardar(producto);
            refrescarProductos();
            limpiar();
        } catch (SQLException ex) { mostrarError(ex); }
    }

    private void actualizar() {
        if (seleccionado < 0 || seleccionado >= productos.size()) {
            aviso("Selecciona un producto de la tabla para actualizarlo."); return;
        }
        Producto producto = leerFormulario(productos.get(seleccionado).getId());
        if (producto == null) return;
        try {
            productoDAO.guardar(producto);
            refrescarProductos();
            limpiar();
        } catch (SQLException ex) { mostrarError(ex); }
    }

    private void eliminar() {
        if (seleccionado < 0 || seleccionado >= productos.size()) {
            aviso("Selecciona un producto de la tabla para eliminarlo."); return;
        }
        int respuesta = JOptionPane.showConfirmDialog(vista, "¿Eliminar el producto seleccionado?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) return;
        try {
            productoDAO.eliminar(productos.get(seleccionado).getId());
            refrescarProductos();
            limpiar();
        } catch (SQLException ex) { mostrarError(ex); }
    }

    private void seleccionar(int fila) {
        if (fila < 0 || fila >= productos.size()) return;
        seleccionado = fila;
        vista.setFormulario(productos.get(fila));
    }

    private void limpiar() {
        seleccionado = -1;
        vista.limpiarFormulario(productos.isEmpty() ? 1 : productos.stream().mapToInt(Producto::getId).max().orElse(0) + 1);
    }

    private void mostrarTabla() {
        vista.vaciarFilas();
        for (Producto p : productos) {
            vista.agregarFila(String.format("PROD-%03d", p.getId()), p.getNombre(), p.getCategoria(),
                    "$ " + p.getPrecio().toPlainString(), p.isActivo() ? "Activo" : "Inactivo", "Editar / Eliminar");
        }
        int siguiente = productos.stream().mapToInt(Producto::getId).max().orElse(0) + 1;
        vista.setIdSiguiente(siguiente);
    }

    private void mostrarError(SQLException ex) {
        JOptionPane.showMessageDialog(vista, ex.getMessage(), "Error de productos",
                JOptionPane.ERROR_MESSAGE);
    }

    private void aviso(String texto) {
        JOptionPane.showMessageDialog(vista, texto, "Productos", JOptionPane.INFORMATION_MESSAGE);
    }
}
