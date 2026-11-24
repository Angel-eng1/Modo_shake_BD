package modo_shake;

import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ClientesController {
    private final VistaTablaModulo vista = new VistaTablaModulo("Gestión de Clientes",
            "Registra y actualiza los datos de los clientes.",
            new String[]{"ID", "CLIENTE", "TELÉFONO", "CORREO"}, true, true, true, "＋  Nuevo cliente");
    private final ClienteDAO dao = new ClienteDAO();
    private List<Cliente> clientes = new ArrayList<>();

    public ClientesController() {
        vista.alNuevo(this::nuevo);
        vista.alEditar(this::editar);
        vista.alEliminar(this::eliminar);
        vista.alActualizar(this::refrescar);
        refrescar();
    }
    public VistaTablaModulo getVista() { return vista; }

    private void refrescar() {
        try {
            clientes = dao.listar();
            List<Object[]> filas = new ArrayList<>();
            for (Cliente c : clientes) filas.add(new Object[]{c.getId(), c.getNombre(), c.getTelefono(), c.getCorreo()});
            vista.cargarFilas(filas);
        } catch (SQLException ex) { error(ex); }
    }

    private void nuevo() { formulario(new Cliente(0, "", "", "")); }
    private void editar() {
        int fila = vista.filaSeleccionada();
        if (fila < 0) { aviso("Selecciona un cliente de la tabla."); return; }
        formulario(clientes.get(fila));
    }

    private void formulario(Cliente original) {
        JTextField nombre = new JTextField(original.getNombre());
        JTextField telefono = new JTextField(original.getTelefono());
        JTextField correo = new JTextField(original.getCorreo());
        JPanel panel = new JPanel(new GridLayout(0, 1, 4, 4));
        panel.add(new javax.swing.JLabel("Nombre")); panel.add(nombre);
        panel.add(new javax.swing.JLabel("Teléfono")); panel.add(telefono);
        panel.add(new javax.swing.JLabel("Correo (opcional)")); panel.add(correo);
        int r = JOptionPane.showConfirmDialog(vista, panel, original.getId() == 0 ? "Nuevo cliente" : "Editar cliente",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) return;
        if (nombre.getText().isBlank()) { aviso("El nombre es obligatorio."); return; }
        try {
            dao.guardar(new Cliente(original.getId(), nombre.getText().trim(), telefono.getText().trim(), correo.getText().trim()));
            refrescar();
        } catch (SQLException ex) { error(ex); }
    }

    private void eliminar() {
        int fila = vista.filaSeleccionada();
        if (fila < 0) { aviso("Selecciona un cliente de la tabla."); return; }
        if (JOptionPane.showConfirmDialog(vista, "Se eliminará el cliente; sus pedidos conservarán el registro sin cliente.",
                "Confirmar", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try { dao.eliminar(clientes.get(fila).getId()); refrescar(); }
        catch (SQLException ex) { error(ex); }
    }
    private void aviso(String s) { JOptionPane.showMessageDialog(vista, s, "Clientes", JOptionPane.INFORMATION_MESSAGE); }
    private void error(SQLException ex) { JOptionPane.showMessageDialog(vista, ex.getMessage(), "Clientes", JOptionPane.ERROR_MESSAGE); }
}
