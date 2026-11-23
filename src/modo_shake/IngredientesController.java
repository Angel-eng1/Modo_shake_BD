package modo_shake;

import java.awt.GridLayout;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class IngredientesController {
    private final VistaTablaModulo vista = new VistaTablaModulo("Gestión de Ingredientes",
            "Administra los insumos y el nivel mínimo de existencias.",
            new String[]{"ID", "INGREDIENTE", "UNIDAD", "STOCK MÍNIMO", "ESTADO"}, true, true, true, "＋  Nuevo ingrediente");
    private final IngredienteDAO dao = new IngredienteDAO();
    private List<Ingrediente> ingredientes = new ArrayList<>();

    public IngredientesController() {
        vista.alNuevo(this::nuevo); vista.alEditar(this::editar);
        vista.alEliminar(this::eliminar); vista.alActualizar(this::refrescar); refrescar();
    }
    public VistaTablaModulo getVista() { return vista; }

    private void refrescar() {
        try {
            ingredientes = dao.listar();
            List<Object[]> filas = new ArrayList<>();
            for (Ingrediente i : ingredientes) filas.add(new Object[]{i.getId(), i.getNombre(), i.getUnidad(),
                    i.getStockMinimo().stripTrailingZeros().toPlainString(), i.isActivo() ? "Activo" : "Inactivo"});
            vista.cargarFilas(filas);
        } catch (SQLException ex) { error(ex); }
    }
    private void nuevo() { formulario(new Ingrediente(0, "", "g", BigDecimal.ZERO, true)); }
    private void editar() {
        int fila = vista.filaSeleccionada();
        if (fila < 0) { aviso("Selecciona un ingrediente."); return; }
        formulario(ingredientes.get(fila));
    }
    private void formulario(Ingrediente original) {
        JTextField nombre = new JTextField(original.getNombre());
        JTextField unidad = new JTextField(original.getUnidad());
        JTextField minimo = new JTextField(original.getStockMinimo().toPlainString());
        JComboBox<String> estado = new JComboBox<>(new String[]{"Activo", "Inactivo"});
        estado.setSelectedItem(original.isActivo() ? "Activo" : "Inactivo");
        FiltrosEntrada.texto(nombre,80); FiltrosEntrada.texto(unidad,20); FiltrosEntrada.decimal(minimo,7,3);
        JPanel panel = new JPanel(new GridLayout(0, 1, 4, 4));
        panel.add(new javax.swing.JLabel("Nombre")); panel.add(nombre);
        panel.add(new javax.swing.JLabel("Unidad (g, ml, unidad, etc.)")); panel.add(unidad);
        panel.add(new javax.swing.JLabel("Stock mínimo")); panel.add(minimo);
        panel.add(new javax.swing.JLabel("Estado")); panel.add(estado);
        int r = JOptionPane.showConfirmDialog(vista, panel, original.getId() == 0 ? "Nuevo ingrediente" : "Editar ingrediente",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) return;
        try {
            if (nombre.getText().isBlank() || unidad.getText().isBlank()) throw new IllegalArgumentException("Completa el nombre y la unidad.");
            BigDecimal cantidad = new BigDecimal(minimo.getText().trim().replace(",", "."));
            if (cantidad.signum() < 0) throw new IllegalArgumentException("El stock mínimo no puede ser negativo.");
            dao.guardar(new Ingrediente(original.getId(), nombre.getText().trim(), unidad.getText().trim(),
                    cantidad, "Activo".equals(estado.getSelectedItem())));
            refrescar();
        } catch (NumberFormatException ex) { aviso("Ingresa un stock mínimo numérico válido."); }
        catch (IllegalArgumentException ex) { aviso(ex.getMessage()); }
        catch (SQLException ex) { error(ex); }
    }
    private void eliminar() {
        int fila = vista.filaSeleccionada();
        if (fila < 0) { aviso("Selecciona un ingrediente."); return; }
        if (JOptionPane.showConfirmDialog(vista, "¿Eliminar este ingrediente? No se podrá si forma parte de una receta.",
                "Confirmar", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try { dao.eliminar(ingredientes.get(fila).getId()); refrescar(); }
        catch (SQLException ex) { error(ex); }
    }
    private void aviso(String s) { JOptionPane.showMessageDialog(vista, s, "Ingredientes", JOptionPane.INFORMATION_MESSAGE); }
    private void error(SQLException ex) { JOptionPane.showMessageDialog(vista, ex.getMessage(), "Ingredientes", JOptionPane.ERROR_MESSAGE); }
}
