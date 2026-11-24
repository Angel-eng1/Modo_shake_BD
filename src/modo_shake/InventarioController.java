package modo_shake;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class InventarioController {
    private final VistaTablaModulo vista = new VistaTablaModulo("Control de Inventario",
            "Consulta existencias, niveles mínimos y registra ajustes de stock.",
            new String[]{"ID", "INGREDIENTE", "UNIDAD", "CANTIDAD ACTUAL", "MÍNIMO", "ACTUALIZADO", "ESTADO"},
            true, false, false, "Ajustar stock");
    private final InventarioDAO dao = new InventarioDAO();
    private List<Object[]> filas;

    public InventarioController() {
        vista.alNuevo(this::ajustar);
        vista.alActualizar(this::refrescar);
        refrescar();
    }
    public VistaTablaModulo getVista() { return vista; }

    private void refrescar() {
        try { filas = dao.listar(); vista.cargarFilas(filas); }
        catch (SQLException ex) { error(ex); }
    }

    private void ajustar() {
        int fila = vista.filaSeleccionada();
        if (fila < 0) { aviso("Selecciona un ingrediente de la tabla."); return; }
        JTextField campo = new JTextField(String.valueOf(filas.get(fila)[3]));
        int respuesta = JOptionPane.showConfirmDialog(vista, campo,
                "Nueva cantidad para " + filas.get(fila)[1] + " (" + filas.get(fila)[2] + ")",
                JOptionPane.OK_CANCEL_OPTION);
        if (respuesta != JOptionPane.OK_OPTION) return;
        try {
            BigDecimal cantidad = new BigDecimal(campo.getText().trim().replace(",", "."));
            if (cantidad.signum() < 0) { aviso("La cantidad no puede ser negativa."); return; }
            dao.ajustar((Integer) filas.get(fila)[0], cantidad);
            refrescar();
        } catch (NumberFormatException ex) { aviso("Ingresa una cantidad numérica válida."); }
        catch (SQLException ex) { error(ex); }
    }
    private void aviso(String s) { JOptionPane.showMessageDialog(vista, s, "Inventario", JOptionPane.INFORMATION_MESSAGE); }
    private void error(SQLException ex) { JOptionPane.showMessageDialog(vista, ex.getMessage(), "Inventario", JOptionPane.ERROR_MESSAGE); }
}
