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

public class VentasController {
    private final VistaTablaModulo vista = new VistaTablaModulo("Ventas y Pagos",
            "Consulta los valores de los pedidos y registra pagos o abonos.",
            new String[]{"PEDIDO", "FECHA", "CLIENTE", "TOTAL", "PAGADO", "SALDO", "ESTADO"},
            true, false, false, "＋  Registrar pago");
    private final VentasDAO dao = new VentasDAO();
    private List<Object[]> filas = new ArrayList<>();

    public VentasController() {
        vista.alNuevo(this::registrarPago);
        vista.alActualizar(this::refrescar);
        refrescar();
    }
    public VistaTablaModulo getVista() { return vista; }

    private void refrescar() {
        try { filas = dao.listar(); vista.cargarFilas(filas); }
        catch (SQLException ex) { error(ex); }
    }

    private void registrarPago() {
        int fila = vista.filaSeleccionada();
        if (fila < 0) { aviso("Selecciona el pedido que vas a pagar."); return; }
        BigDecimal saldo = (BigDecimal) filas.get(fila)[5];
        if (saldo.signum() <= 0) { aviso("Este pedido no tiene saldo pendiente."); return; }
        JComboBox<String> metodo = new JComboBox<>(new String[]{"Efectivo", "Tarjeta", "Transferencia"});
        JTextField monto = new JTextField(saldo.toPlainString());
        FiltrosEntrada.decimal(monto,8,2);
        JPanel panel = new JPanel(new GridLayout(0, 1, 4, 4));
        panel.add(new javax.swing.JLabel("Método de pago")); panel.add(metodo);
        panel.add(new javax.swing.JLabel("Monto (saldo: " + saldo.toPlainString() + ")")); panel.add(monto);
        if (JOptionPane.showConfirmDialog(vista, panel, "Registrar pago",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        try {
            BigDecimal valor = new BigDecimal(monto.getText().trim().replace(",", "."));
            dao.registrarPago((Integer) filas.get(fila)[0], (String) metodo.getSelectedItem(), valor);
            refrescar();
        } catch (NumberFormatException ex) { aviso("Ingresa un monto válido."); }
        catch (SQLException ex) { error(ex); }
    }
    private void aviso(String s) { JOptionPane.showMessageDialog(vista, s, "Ventas", JOptionPane.INFORMATION_MESSAGE); }
    private void error(SQLException ex) { JOptionPane.showMessageDialog(vista, ex.getMessage(), "Ventas", JOptionPane.ERROR_MESSAGE); }
}
