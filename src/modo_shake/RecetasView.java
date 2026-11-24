package modo_shake;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class RecetasView extends JPanel {
    private final JComboBox<Producto> productos = new JComboBox<>();
    private final JComboBox<Ingrediente> ingredientes = new JComboBox<>();
    private final JTextField cantidad = new JTextField(9);
    private final JLabel unidad = new JLabel(" ");
    private final JLabel estado = new JLabel("Selecciona un producto.");
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"INGREDIENTE", "CANTIDAD POR PRODUCTO", "UNIDAD", "ESTADO"}, 0) {
        @Override public boolean isCellEditable(int fila, int columna) { return false; }
    };
    private final JTable tabla = new JTable(modelo);
    private final JButton agregar = Estilo.boton("Agregar / actualizar", Estilo.VERDE, Color.WHITE);
    private final JButton quitar = new JButton("Quitar seleccionado");
    private final JButton guardar = Estilo.boton("Guardar receta", Estilo.VERDE, Color.WHITE);
    private final JButton recargar = new JButton("Recargar receta");
    private final JButton actualizar = new JButton("Actualizar catálogo");

    public RecetasView() {
        setLayout(new BorderLayout(0, 18));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 24, 32));
        JPanel encabezado = new JPanel(new BorderLayout(0, 8));
        encabezado.setOpaque(false);
        JLabel titulo = new JLabel("Recetas de productos");
        titulo.setFont(Estilo.TITULO);
        JLabel descripcion = new JLabel("Define los ingredientes y cantidades necesarios para una unidad del producto.");
        descripcion.setFont(Estilo.NORMAL);
        encabezado.add(titulo, BorderLayout.NORTH);
        encabezado.add(descripcion, BorderLayout.CENTER);
        JPanel selector = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        selector.setOpaque(false);
        JLabel producto = new JLabel("Producto");
        producto.setLabelFor(productos);
        productos.setPrototypeDisplayValue(new Producto(0, 0,
                "Selecciona el producto para configurar su receta", "", java.math.BigDecimal.ZERO, "", true));
        selector.add(producto);
        selector.add(productos);
        selector.add(actualizar);
        encabezado.add(selector, BorderLayout.SOUTH);
        add(encabezado, BorderLayout.NORTH);

        Estilo.Tarjeta tarjeta = Estilo.tarjeta(Color.WHITE, 14);
        tarjeta.setLayout(new BorderLayout(0, 15));
        tarjeta.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JPanel editor = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        editor.setOpaque(false);
        JLabel ingrediente = new JLabel("Ingrediente");
        ingrediente.setLabelFor(ingredientes);
        ingredientes.setPrototypeDisplayValue(new Ingrediente(0,
                "Selecciona un ingrediente", "", java.math.BigDecimal.ZERO, true));
        JLabel cantidadLabel = new JLabel("Cantidad");
        cantidadLabel.setLabelFor(cantidad);
        cantidad.setToolTipText("Cantidad por una unidad del producto, con hasta tres decimales.");
        FiltrosEntrada.decimal(cantidad, 7, 3);
        editor.add(ingrediente);
        editor.add(ingredientes);
        editor.add(cantidadLabel);
        editor.add(cantidad);
        editor.add(unidad);
        editor.add(agregar);
        tarjeta.add(editor, BorderLayout.NORTH);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(38);
        tabla.setFont(Estilo.NORMAL);
        tabla.getAccessibleContext().setAccessibleName("Ingredientes de la receta");
        tarjeta.add(new JScrollPane(tabla), BorderLayout.CENTER);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        acciones.setOpaque(false);
        acciones.add(guardar);
        acciones.add(quitar);
        acciones.add(recargar);
        tarjeta.add(acciones, BorderLayout.SOUTH);
        add(tarjeta, BorderLayout.CENTER);
        estado.setFont(Estilo.NORMAL);
        add(estado, BorderLayout.SOUTH);
    }

    public void setProductos(List<Producto> lista, Integer seleccionado) {
        productos.removeAllItems();
        for (Producto p : lista) {
            productos.addItem(p);
            if (seleccionado != null && p.getId() == seleccionado) productos.setSelectedItem(p);
        }
    }
    public void setIngredientes(List<Ingrediente> lista) {
        ingredientes.removeAllItems();
        for (Ingrediente ingrediente : lista) ingredientes.addItem(ingrediente);
    }
    public Producto producto() { return (Producto) productos.getSelectedItem(); }
    public void seleccionarProducto(Producto producto) {
        if (producto != null) {
            for (int i = 0; i < productos.getItemCount(); i++) {
                if (productos.getItemAt(i).getId() == producto.getId()) {
                    productos.setSelectedIndex(i);
                    return;
                }
            }
        }
        productos.setSelectedIndex(-1);
    }
    public Ingrediente ingrediente() { return (Ingrediente) ingredientes.getSelectedItem(); }
    public String cantidad() { return cantidad.getText().trim(); }
    public void mostrarUnidad() {
        Ingrediente ingrediente = ingrediente();
        unidad.setText(ingrediente == null ? " " : ingrediente.getUnidad());
    }
    public void mostrarCantidad(String valor) { cantidad.setText(valor); }
    public void seleccionarIngrediente(int id) {
        for (int i = 0; i < ingredientes.getItemCount(); i++) {
            if (ingredientes.getItemAt(i).getId() == id) {
                ingredientes.setSelectedIndex(i);
                return;
            }
        }
    }
    public int filaSeleccionada() {
        return tabla.getSelectedRow() < 0 ? -1 : tabla.convertRowIndexToModel(tabla.getSelectedRow());
    }
    public void mostrar(List<ComponenteReceta> lista, boolean cambios) {
        modelo.setRowCount(0);
        for (ComponenteReceta c : lista) {
            modelo.addRow(new Object[]{c.nombre(), c.cantidad().stripTrailingZeros().toPlainString(),
                    c.unidad(), c.activo() ? "Activo" : "Inactivo"});
        }
        Producto p = producto();
        estado.setText(p == null ? "No hay productos. Registra uno en Productos y actualiza el catálogo."
                : p.getNombre() + " · " + lista.size() + " ingredientes · "
                + (cambios ? "Cambios sin guardar." : lista.isEmpty()
                ? "Sin receta. Agrega ingredientes para poder registrar pedidos."
                : "Receta guardada. Las cantidades corresponden a una unidad del producto."));
        agregar.setEnabled(p != null && ingrediente() != null);
        guardar.setEnabled(p != null && cambios);
        quitar.setEnabled(p != null && !lista.isEmpty());
        recargar.setEnabled(p != null);
    }
    public void alProducto(Runnable a) { productos.addActionListener(e -> a.run()); }
    public void alIngrediente(Runnable a) { ingredientes.addActionListener(e -> a.run()); }
    public void alAgregar(Runnable a) { agregar.addActionListener(e -> a.run()); }
    public void alQuitar(Runnable a) { quitar.addActionListener(e -> a.run()); }
    public void alGuardar(Runnable a) { guardar.addActionListener(e -> a.run()); }
    public void alRecargar(Runnable a) { recargar.addActionListener(e -> a.run()); }
    public void alActualizar(Runnable a) { actualizar.addActionListener(e -> a.run()); }
    public void alFila(Runnable a) {
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && filaSeleccionada() >= 0) a.run();
        });
    }
}
