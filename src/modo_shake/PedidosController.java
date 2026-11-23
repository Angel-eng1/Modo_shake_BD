package modo_shake;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class PedidosController {
    private final VistaTablaModulo vista = new VistaTablaModulo("Gestión de Pedidos",
            "Registra pedidos y consulta su estado y valor.",
            new String[]{"PEDIDO", "FECHA", "CLIENTE", "EMPLEADO", "TOTAL", "ESTADO"},
            true, true, false, "＋  Nuevo pedido");
    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final UsuarioSesion sesion;
    private List<Object[]> pedidos = new ArrayList<>();

    public PedidosController(UsuarioSesion sesion) {
        this.sesion = sesion;
        vista.alNuevo(this::nuevo);
        vista.alEditar(this::cambiarEstado);
        vista.alActualizar(this::refrescar);
        vista.agregarAccion("Reporte por fechas", this::reporte);
        refrescar();
    }
    public VistaTablaModulo getVista() { return vista; }

    private void refrescar() {
        try { pedidos = pedidoDAO.listar(); vista.cargarFilas(pedidos); }
        catch (SQLException ex) { error(ex); }
    }

    private void reporte() {
        javax.swing.JTextField inicio = new javax.swing.JTextField(java.time.LocalDate.now().withDayOfMonth(1).toString());
        javax.swing.JTextField fin = new javax.swing.JTextField(java.time.LocalDate.now().toString());
        FiltrosEntrada.texto(inicio, 10); FiltrosEntrada.texto(fin, 10);
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new javax.swing.JLabel("Fecha inicial (AAAA-MM-DD)")); panel.add(inicio);
        panel.add(new javax.swing.JLabel("Fecha final (AAAA-MM-DD)")); panel.add(fin);
        if (JOptionPane.showConfirmDialog(vista, panel, "Consultar pedidos", JOptionPane.OK_CANCEL_OPTION)
                != JOptionPane.OK_OPTION) return;
        try {
            java.time.LocalDate desde = java.time.LocalDate.parse(inicio.getText().trim());
            java.time.LocalDate hasta = java.time.LocalDate.parse(fin.getText().trim());
            DefaultTableModel modelo = new DefaultTableModel(new String[]{"Pedido", "Fecha", "Cliente", "Empleado",
                "Categoria", "Producto", "Cantidad", "Precio", "Subtotal", "Estado"}, 0) {
                @Override public boolean isCellEditable(int fila, int columna) { return false; }
            };
            for (Object[] fila : pedidoDAO.reporte(desde, hasta)) modelo.addRow(fila);
            JScrollPane scroll = new JScrollPane(new JTable(modelo));
            scroll.setPreferredSize(new java.awt.Dimension(1000, 400));
            JOptionPane.showMessageDialog(vista, scroll, "Reporte de pedidos", JOptionPane.PLAIN_MESSAGE);
        } catch (java.time.format.DateTimeParseException ex) {
            JOptionPane.showMessageDialog(vista, "Ingresa fechas validas con formato AAAA-MM-DD.");
        } catch (SQLException ex) { error(ex); }
    }

    private void nuevo() {
        try {
            List<Cliente> clientes = new ArrayList<>();
            clientes.add(new Cliente(0, "CLIENTE GENERAL", "", ""));
            clientes.addAll(clienteDAO.listar());
            List<Producto> productos = productoDAO.listarActivos();
            if (productos.isEmpty()) { aviso("No hay productos activos para agregar al pedido."); return; }
            JComboBox<Cliente> cliente = new JComboBox<>(clientes.toArray(Cliente[]::new));
            JComboBox<Producto> producto = new JComboBox<>(productos.toArray(Producto[]::new));
            JSpinner cantidad = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
            DefaultTableModel modelo = new DefaultTableModel(new Object[]{"PRODUCTO", "CANTIDAD", "PRECIO", "SUBTOTAL"}, 0) {
                @Override public boolean isCellEditable(int fila, int columna) { return false; }
            };
            JTable tabla = new JTable(modelo);
            JButton agregar = new JButton("Agregar producto");
            JButton quitar = new JButton("Quitar seleccionado");
            JPanel linea = new JPanel(new GridLayout(0, 1, 4, 4));
            linea.add(new javax.swing.JLabel("Cliente")); linea.add(cliente);
            linea.add(new javax.swing.JLabel("Producto")); linea.add(producto);
            linea.add(new javax.swing.JLabel("Cantidad")); linea.add(cantidad);
            JPanel acciones = new JPanel(); acciones.add(agregar); acciones.add(quitar);
            JPanel panel = new JPanel(new BorderLayout(0, 8));
            panel.add(linea, BorderLayout.NORTH);
            panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
            panel.add(acciones, BorderLayout.SOUTH);
            List<PedidoDAO.DetallePedidoNuevo> detalles = new ArrayList<>();
            agregar.addActionListener(e -> {
                Producto seleccionado = (Producto) producto.getSelectedItem();
                if (seleccionado == null) return;
                int unidades = (Integer) cantidad.getValue();
                int existente = -1;
                for (int i = 0; i < detalles.size(); i++) if (detalles.get(i).getProductoId() == seleccionado.getId()) existente = i;
                if (existente >= 0) {
                    PedidoDAO.DetallePedidoNuevo anterior = detalles.get(existente);
                    int nuevaCantidad = anterior.getCantidad() + unidades;
                    detalles.set(existente, new PedidoDAO.DetallePedidoNuevo(seleccionado.getId(), nuevaCantidad));
                    modelo.setValueAt(nuevaCantidad, existente, 1);
                    modelo.setValueAt("$ " + seleccionado.getPrecio().multiply(java.math.BigDecimal.valueOf(nuevaCantidad)).toPlainString(), existente, 3);
                } else {
                    detalles.add(new PedidoDAO.DetallePedidoNuevo(seleccionado.getId(), unidades));
                    modelo.addRow(new Object[]{seleccionado.getNombre(), unidades,
                        "$ " + seleccionado.getPrecio().toPlainString(),
                        "$ " + seleccionado.getPrecio().multiply(java.math.BigDecimal.valueOf(unidades)).toPlainString()});
                }
            });
            quitar.addActionListener(e -> {
                int f = tabla.getSelectedRow();
                if (f >= 0) { detalles.remove(tabla.convertRowIndexToModel(f)); modelo.removeRow(tabla.convertRowIndexToModel(f)); }
            });
            int r = JOptionPane.showConfirmDialog(vista, panel, "Crear pedido",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (r != JOptionPane.OK_OPTION) return;
            if (detalles.isEmpty()) { aviso("Agrega al menos un producto al pedido."); return; }
            Cliente c = (Cliente) cliente.getSelectedItem();
            int id = pedidoDAO.crear(c == null || c.getId() == 0 ? null : c.getId(),
                    sesion.getIdEmpleado(), detalles);
            refrescar();
            aviso("Pedido #" + id + " registrado con " + detalles.size() + " producto(s).");
        } catch (SQLException ex) { error(ex); }
    }

    private void cambiarEstado() {
        int fila = vista.filaSeleccionada();
        if (fila < 0) { aviso("Selecciona un pedido."); return; }
        String[] estados = {"Recibido", "En preparación", "Entregado", "Cancelado"};
        JComboBox<String> estado = new JComboBox<>(estados);
        estado.setSelectedItem(pedidos.get(fila)[5]);
        if (JOptionPane.showConfirmDialog(vista, estado, "Cambiar estado del pedido #" + pedidos.get(fila)[0],
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        try {
            pedidoDAO.cambiarEstado((Integer) pedidos.get(fila)[0], (String) estado.getSelectedItem());
            refrescar();
        } catch (SQLException ex) { error(ex); }
    }
    private void aviso(String s) { JOptionPane.showMessageDialog(vista, s, "Pedidos", JOptionPane.INFORMATION_MESSAGE); }
    private void error(SQLException ex) { JOptionPane.showMessageDialog(vista, ex.getMessage(), "Pedidos", JOptionPane.ERROR_MESSAGE); }
}

