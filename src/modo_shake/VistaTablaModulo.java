package modo_shake;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class VistaTablaModulo extends JPanel {
    private final DefaultTableModel modelo;
    private final JTable tabla;
    private final JButton nuevo = Estilo.boton("＋  Nuevo", Estilo.VERDE, Color.WHITE);
    private final JButton editar = Estilo.boton("Editar", Color.WHITE, Estilo.VERDE_OSCURO);
    private final JButton eliminar = Estilo.boton("Eliminar", Color.WHITE, new Color(180, 55, 48));
    private final JButton actualizar = Estilo.boton("Actualizar", new Color(239, 238, 236), Estilo.TEXTO_SUAVE);

    public VistaTablaModulo(String titulo, String subtitulo, String[] columnas,
                            boolean permiteCrear, boolean permiteEditar,
                            boolean permiteEliminar, String accionCrear) {
        setLayout(new BorderLayout(0, 20));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 24, 32));
        JPanel encabezado = new JPanel(new BorderLayout(0, 4));
        encabezado.setOpaque(false);
        JLabel h = new JLabel(titulo);
        h.setFont(Estilo.TITULO);
        h.setForeground(Estilo.TEXTO);
        JLabel sub = new JLabel(subtitulo);
        sub.setFont(Estilo.NORMAL);
        sub.setForeground(Estilo.TEXTO_SUAVE);
        encabezado.add(h, BorderLayout.NORTH);
        encabezado.add(sub, BorderLayout.SOUTH);
        add(encabezado, BorderLayout.NORTH);

        modelo = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int fila, int columna) { return false; }
        };
        tabla = new JTable(modelo);
        tabla.setRowHeight(38);
        tabla.setFont(Estilo.NORMAL);
        tabla.setShowVerticalLines(false);
        tabla.setGridColor(new Color(240, 240, 237));
        tabla.setSelectionBackground(new Color(230, 243, 230));
        tabla.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 10));
        tabla.getTableHeader().setForeground(new Color(65, 77, 66));
        tabla.getTableHeader().setBackground(new Color(248, 247, 245));
        tabla.getTableHeader().setPreferredSize(new Dimension(0, 36));

        Estilo.Tarjeta tarjeta = Estilo.tarjeta(Color.WHITE, 14);
        tarjeta.setLayout(new BorderLayout(0, 14));
        tarjeta.setBorder(BorderFactory.createEmptyBorder(15, 17, 15, 17));
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        acciones.setOpaque(false);
        nuevo.setText(accionCrear);
        nuevo.setVisible(permiteCrear);
        editar.setVisible(permiteEditar);
        eliminar.setVisible(permiteEliminar);
        acciones.add(nuevo);
        acciones.add(editar);
        acciones.add(eliminar);
        acciones.add(actualizar);
        tarjeta.add(acciones, BorderLayout.NORTH);
        tarjeta.add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(tarjeta, BorderLayout.CENTER);
    }

    public void cargarFilas(List<Object[]> filas) {
        modelo.setRowCount(0);
        for (Object[] fila : filas) modelo.addRow(fila);
    }
    public int filaSeleccionada() { return tabla.getSelectedRow() < 0 ? -1 : tabla.convertRowIndexToModel(tabla.getSelectedRow()); }
    public Object valor(int fila, int columna) { return modelo.getValueAt(fila, columna); }
    public void agregarAccion(String titulo, Runnable accion) {
        JButton boton = new JButton(titulo);
        boton.addActionListener(e -> accion.run());
        nuevo.getParent().add(boton);
    }
    public void alNuevo(Runnable a) { nuevo.addActionListener(e -> a.run()); }
    public void alEditar(Runnable a) { editar.addActionListener(e -> a.run()); }
    public void alEliminar(Runnable a) { eliminar.addActionListener(e -> a.run()); }
    public void alActualizar(Runnable a) { actualizar.addActionListener(e -> a.run()); }
}
