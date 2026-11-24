package modo_shake;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class RecetasController {
    private final RecetasView vista = new RecetasView();
    private final RecetaDAO dao = new RecetaDAO();
    private List<ComponenteReceta> componentes = new ArrayList<>();
    private Producto productoActual;
    private boolean cambios;
    private boolean actualizando;

    public RecetasController() {
        vista.alProducto(this::cambiarProducto);
        vista.alIngrediente(vista::mostrarUnidad);
        vista.alAgregar(this::agregar);
        vista.alQuitar(this::quitar);
        vista.alGuardar(this::guardar);
        vista.alActualizar(this::actualizarCatalogo);
        vista.alRecargar(() -> {
            if (confirmarDescarte()) cargar(vista.producto());
        });
        vista.alFila(this::seleccionarFila);
        actualizarCatalogo();
    }

    public RecetasView getVista() { return vista; }

    public boolean permitirSalida() {
        if (!cambios) return true;
        return confirmarDescarte() && cargar(productoActual);
    }

    private void actualizarCatalogo() {
        if (!confirmarDescarte()) return;
        try {
            List<Producto> productos = new ProductoDAO().listar();
            List<Ingrediente> ingredientes = new IngredienteDAO().listar();
            Integer id = productoActual == null ? null : productoActual.getId();
            actualizando = true;
            vista.setProductos(productos, id);
            vista.setIngredientes(ingredientes);
            actualizando = false;
            vista.mostrarUnidad();
            cargar(vista.producto());
        } catch (SQLException ex) {
            actualizando = false;
            error(ex);
        }
    }

    private void cambiarProducto() {
        if (actualizando) return;
        if (!confirmarDescarte()) {
            actualizando = true;
            vista.seleccionarProducto(productoActual);
            actualizando = false;
            return;
        }
        cargar(vista.producto());
    }

    private boolean cargar(Producto producto) {
        try {
            List<ComponenteReceta> receta = producto == null ? List.of() : dao.listar(producto.getId());
            componentes = new ArrayList<>(receta);
            productoActual = producto;
            cambios = false;
            vista.mostrarCantidad("");
            vista.mostrar(componentes, cambios);
            return true;
        } catch (SQLException ex) {
            actualizando = true;
            vista.seleccionarProducto(productoActual);
            actualizando = false;
            error(ex);
            return false;
        }
    }

    private void agregar() {
        Ingrediente ingrediente = vista.ingrediente();
        if (productoActual == null || ingrediente == null) return;
        if (!ingrediente.isActivo()) {
            aviso("Selecciona un ingrediente activo.");
            return;
        }
        try {
            BigDecimal cantidad = new BigDecimal(vista.cantidad().replace(",", "."));
            if (cantidad.signum() <= 0 || cantidad.compareTo(new BigDecimal("9999999.999")) > 0
                    || cantidad.scale() > 3) {
                aviso("Usa una cantidad entre 0,001 y 9999999,999, con hasta tres decimales.");
                return;
            }
            ComponenteReceta componente = new ComponenteReceta(ingrediente.getId(),
                    ingrediente.getNombre(), ingrediente.getUnidad(), cantidad, true);
            int posicion = -1;
            for (int i = 0; i < componentes.size(); i++) {
                if (componentes.get(i).idIngrediente() == ingrediente.getId()) posicion = i;
            }
            if (posicion >= 0) componentes.set(posicion, componente);
            else componentes.add(componente);
            cambios = true;
            vista.mostrar(componentes, cambios);
        } catch (NumberFormatException ex) {
            aviso("Ingresa la cantidad necesaria para una unidad del producto.");
        }
    }

    private void seleccionarFila() {
        int fila = vista.filaSeleccionada();
        if (fila < 0 || fila >= componentes.size()) return;
        ComponenteReceta componente = componentes.get(fila);
        vista.seleccionarIngrediente(componente.idIngrediente());
        vista.mostrarCantidad(componente.cantidad().toPlainString());
    }

    private void quitar() {
        int fila = vista.filaSeleccionada();
        if (fila < 0) {
            aviso("Selecciona el ingrediente que deseas quitar de la receta.");
            return;
        }
        componentes.remove(fila);
        cambios = true;
        vista.mostrar(componentes, cambios);
    }

    private void guardar() {
        if (productoActual == null) return;
        if (componentes.isEmpty()) {
            aviso("Agrega al menos un ingrediente antes de guardar la receta.");
            return;
        }
        try {
            dao.guardar(productoActual.getId(), componentes);
            cambios = false;
            if (cargar(productoActual)) aviso("Receta guardada correctamente.");
        } catch (SQLException ex) { error(ex); }
    }

    private boolean confirmarDescarte() {
        return !cambios || JOptionPane.showConfirmDialog(vista,
                "Hay cambios sin guardar. ¿Descartarlos y cargar la receta?",
                "Cambios pendientes", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
    private void aviso(String mensaje) {
        JOptionPane.showMessageDialog(vista, mensaje, "Recetas", JOptionPane.INFORMATION_MESSAGE);
    }
    private void error(SQLException ex) {
        JOptionPane.showMessageDialog(vista, "No fue posible completar la operación de recetas.\n"
                + ex.getMessage(), "Recetas", JOptionPane.ERROR_MESSAGE);
    }
}
