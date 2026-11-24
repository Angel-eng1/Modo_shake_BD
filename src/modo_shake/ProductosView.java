package modo_shake;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.function.IntConsumer;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ButtonGroup;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

public class ProductosView extends JPanel {
    private final JTextField campoId=new JTextField("PROD-001");
    private final JTextField campoNombre=new JTextField();
    private final JComboBox<Object> campoCategoria=new JComboBox<>(new Object[]{"Selecciona una categoría"});
    private final JTextField campoPrecio=new JTextField();
    private final JTextArea campoDescripcion=new JTextArea(3,20);
    private final JRadioButton activo=new JRadioButton("Activo",true);
    private final JRadioButton inactivo=new JRadioButton("Inactivo");
    private final JButton guardar=Estilo.boton("▣  GUARDAR",Estilo.VERDE,Color.WHITE);
    private final JButton actualizar=Estilo.boton("↻  ACTUALIZAR",Color.WHITE,Estilo.VERDE_OSCURO);
    private final JButton eliminar=Estilo.boton("▤  ELIMINAR",new Color(255,255,255),new Color(190,55,55));
    private final JButton limpiar=Estilo.boton("LIMPIAR",new Color(246,222,177),new Color(115,91,50));
    private final DefaultTableModel modelo=new DefaultTableModel(new Object[]{"ID","PRODUCTO","CATEGORÍA","PRECIO","ESTADO","ACCIONES"},0) {
        @Override public boolean isCellEditable(int row,int column){return false;}
    };
    private final JTable tabla=new JTable(modelo);

    public ProductosView() {
        setLayout(new BorderLayout(0,20)); setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(25,28,24,28));
        JPanel titulo=new JPanel(new BorderLayout()); titulo.setOpaque(false);
        JLabel h=new JLabel("Gestión de Productos"); h.setFont(Estilo.TITULO); h.setForeground(Estilo.TEXTO);
        JLabel sub=new JLabel("Crea, edita y administra el menú de Modo Shake."); sub.setFont(Estilo.NORMAL); sub.setForeground(Estilo.TEXTO_SUAVE);
        titulo.add(h,BorderLayout.NORTH); titulo.add(sub,BorderLayout.SOUTH); add(titulo,BorderLayout.NORTH);

        JPanel centro=new JPanel(new BorderLayout(0,20)); centro.setOpaque(false);
        centro.add(crearFormulario(),BorderLayout.NORTH);
        JPanel tablaCard=Estilo.tarjeta(Color.WHITE,14); tablaCard.setLayout(new BorderLayout(0,12));
        tablaCard.setBorder(BorderFactory.createEmptyBorder(16,17,12,17));
        JLabel etiquetaTabla=new JLabel("Productos Registrados"); etiquetaTabla.setFont(new Font("SansSerif",Font.BOLD,18));
        tablaCard.add(etiquetaTabla,BorderLayout.NORTH); configurarTabla();
        tablaCard.add(new JScrollPane(tabla),BorderLayout.CENTER); centro.add(tablaCard,BorderLayout.CENTER);
        add(centro,BorderLayout.CENTER);
    }

    private JPanel crearFormulario() {
        JPanel tarjeta=Estilo.tarjeta(Color.WHITE,14); tarjeta.setLayout(new BorderLayout(0,18));
        tarjeta.setBorder(BorderFactory.createEmptyBorder(20,22,18,22));
        JLabel encabezado=new JLabel("Detalles del Producto"); encabezado.setFont(new Font("SansSerif",Font.BOLD,18));
        tarjeta.add(encabezado,BorderLayout.NORTH);
        JPanel campos=new JPanel(new GridBagLayout()); campos.setOpaque(false);
        GridBagConstraints c=new GridBagConstraints(); c.insets=new Insets(0,0,11,16); c.fill=GridBagConstraints.HORIZONTAL; c.weightx=1;
        agregarCampo(campos,c,0,0,"ID DEL PRODUCTO",campoId); campoId.setEditable(false); campoId.setBackground(new Color(247,246,244));
        agregarCampo(campos,c,1,0,"NOMBRE DEL PRODUCTO",campoNombre);
        agregarCampo(campos,c,0,1,"CATEGORÍA",campoCategoria); agregarCampo(campos,c,1,1,"PRECIO",campoPrecio);
        c.gridx=0;c.gridy=2;c.gridwidth=2;c.weightx=1;c.insets=new Insets(0,0,10,16);
        JPanel desc=new JPanel(new BorderLayout(0,5));desc.setOpaque(false);desc.add(etiquetaCampo("DESCRIPCIÓN"),BorderLayout.NORTH);
        campoDescripcion.setFont(Estilo.NORMAL); campoDescripcion.setLineWrap(true);campoDescripcion.setWrapStyleWord(true);
        campoDescripcion.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Estilo.BORDE),BorderFactory.createEmptyBorder(9,10,9,10)));
        JScrollPane scroll=new JScrollPane(campoDescripcion); scroll.setPreferredSize(new Dimension(300,75));desc.add(scroll,BorderLayout.CENTER);campos.add(desc,c);
        c.gridy=3;c.gridwidth=2;c.insets=new Insets(0,0,6,16);
        JPanel estado=new JPanel(new BorderLayout());estado.setOpaque(false);estado.add(etiquetaCampo("ESTADO"),BorderLayout.NORTH);
        ButtonGroup grupo=new ButtonGroup();grupo.add(activo);grupo.add(inactivo);
        JPanel radios=new JPanel(new GridLayout(1,2,10,0));radios.setOpaque(false);activo.setOpaque(false);inactivo.setOpaque(false);activo.setForeground(Estilo.TEXTO);inactivo.setForeground(Estilo.TEXTO);radios.add(activo);radios.add(inactivo);estado.add(radios,BorderLayout.CENTER);campos.add(estado,c);
        tarjeta.add(campos,BorderLayout.CENTER);
        JPanel acciones=new JPanel(new BorderLayout());acciones.setOpaque(false);acciones.setBorder(BorderFactory.createMatteBorder(1,0,0,0,Estilo.BORDE));
        JPanel izquierda=new JPanel(new GridLayout(1,2,10,0));izquierda.setOpaque(false);izquierda.add(guardar);izquierda.add(actualizar);
        JPanel derecha=new JPanel(new GridLayout(1,2,10,0));derecha.setOpaque(false);derecha.add(eliminar);derecha.add(limpiar);
        acciones.add(izquierda,BorderLayout.WEST);acciones.add(derecha,BorderLayout.EAST);tarjeta.add(acciones,BorderLayout.SOUTH);
        return tarjeta;
    }

    private void agregarCampo(JPanel panel,GridBagConstraints base,int columna,int fila,String texto,java.awt.Component campo){
        GridBagConstraints c=(GridBagConstraints)base.clone();c.gridx=columna;c.gridy=fila;c.gridwidth=1;
        JPanel envoltura=new JPanel(new BorderLayout(0,5));envoltura.setOpaque(false);envoltura.add(etiquetaCampo(texto),BorderLayout.NORTH);
        campo.setFont(Estilo.NORMAL);campo.setPreferredSize(new Dimension(240,39));
        if(campo instanceof JTextField){((JTextField)campo).setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(207,216,204)),BorderFactory.createEmptyBorder(7,10,7,10)));((JTextField)campo).setBackground(Color.WHITE);}
        if(campo instanceof JComboBox){((JComboBox<?>)campo).setBackground(Color.WHITE);}
        envoltura.add(campo,BorderLayout.CENTER);panel.add(envoltura,c);
    }
    private JLabel etiquetaCampo(String texto){JLabel l=new JLabel(texto);l.setFont(new Font("SansSerif",Font.BOLD,10));l.setForeground(new Color(75,88,76));return l;}
    private void configurarTabla(){
        tabla.setRowHeight(36);tabla.setFont(Estilo.NORMAL);tabla.setForeground(Estilo.TEXTO);tabla.setBackground(Color.WHITE);
        tabla.setShowVerticalLines(false);tabla.setGridColor(new Color(240,240,237));tabla.setSelectionBackground(new Color(230,243,230));
        tabla.getTableHeader().setFont(new Font("SansSerif",Font.BOLD,10));tabla.getTableHeader().setForeground(new Color(65,77,66));
        tabla.getTableHeader().setBackground(new Color(248,247,245));tabla.getTableHeader().setPreferredSize(new Dimension(0,34));
    }
    public void agregarFila(Object... datos){modelo.addRow(datos);}
    public void vaciarFilas(){modelo.setRowCount(0);}
    public int getFilaSeleccionada(){return tabla.getSelectedRow();}
    public String getNombre(){return campoNombre.getText().trim();}
    public String getCategoria(){Object item=campoCategoria.getSelectedItem();return item instanceof Categoria?((Categoria)item).getNombre():"";}
    public int getCategoriaId(){Object item=campoCategoria.getSelectedItem();return item instanceof Categoria?((Categoria)item).getId():0;}
    public void setCategorias(List<Categoria> categorias){campoCategoria.removeAllItems();campoCategoria.addItem("Selecciona una categoría");for(Categoria categoria:categorias)campoCategoria.addItem(categoria);}
    public String getPrecio(){return campoPrecio.getText().trim();}
    public String getDescripcion(){return campoDescripcion.getText().trim();}
    public boolean isActivo(){return activo.isSelected();}
    public void setFormulario(Producto p){campoId.setText(String.format("PROD-%03d",p.getId()));campoNombre.setText(p.getNombre());for(int i=0;i<campoCategoria.getItemCount();i++){Object item=campoCategoria.getItemAt(i);if(item instanceof Categoria&&((Categoria)item).getId()==p.getCategoriaId()){campoCategoria.setSelectedIndex(i);break;}}campoPrecio.setText(p.getPrecio().toPlainString());campoDescripcion.setText(p.getDescripcion());activo.setSelected(p.isActivo());inactivo.setSelected(!p.isActivo());}
    public void setIdSiguiente(int id){campoId.setText(String.format("PROD-%03d",id));}
    public void limpiarFormulario(int siguiente){campoId.setText(String.format("PROD-%03d",siguiente));campoNombre.setText("");campoCategoria.setSelectedIndex(0);campoPrecio.setText("");campoDescripcion.setText("");activo.setSelected(true);tabla.clearSelection();}
    public void alGuardar(Runnable a){guardar.addActionListener(e->a.run());}
    public void alActualizar(Runnable a){actualizar.addActionListener(e->a.run());}
    public void alEliminar(Runnable a){eliminar.addActionListener(e->a.run());}
    public void alLimpiar(Runnable a){limpiar.addActionListener(e->a.run());}
    public void alSeleccionar(IntConsumer a){tabla.getSelectionModel().addListSelectionListener(e->{if(!e.getValueIsAdjusting()&&tabla.getSelectedRow()>=0)a.accept(tabla.convertRowIndexToModel(tabla.getSelectedRow()));});}
}
