package modo_shake;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

public class DashboardView extends JFrame {
    private final JPanel contenido=new JPanel(new CardLayout());
    private final Map<String,JButton> botonesMenu=new LinkedHashMap<>();
    private Runnable accionCerrarSesion=()->{};
    private java.util.function.Consumer<String> accionNavegar=s->{};

    public DashboardView(UsuarioSesion sesion,ProductosView productos,DashboardDAO.ResumenDashboard resumen){
        setTitle("Modo Shake | Panel administrativo");setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100,720));setSize(1500,900);setLocationRelativeTo(null);
        setLayout(new BorderLayout());getContentPane().setBackground(Estilo.FONDO);
        add(crearMenu(sesion),BorderLayout.WEST);add(crearArea(sesion),BorderLayout.CENTER);
        contenido.add(crearInicio(sesion,resumen),"Inicio");contenido.add(productos,"Productos");
        mostrarSeccion("Inicio");
    }
    private JPanel crearMenu(UsuarioSesion sesion){
        JPanel menu=new JPanel(new BorderLayout());menu.setPreferredSize(new Dimension(205,0));menu.setBackground(Estilo.LATERAL);
        menu.setBorder(BorderFactory.createMatteBorder(0,0,0,1,new Color(222,220,216)));
        JPanel marca=new JPanel(new BorderLayout(10,0));marca.setOpaque(false);marca.setBorder(BorderFactory.createEmptyBorder(20,16,26,10));
        JLabel circulo=new JLabel("MS",SwingConstants.CENTER);circulo.setOpaque(true);circulo.setBackground(Estilo.VERDE);circulo.setForeground(Color.WHITE);circulo.setFont(Estilo.NEGRITA);circulo.setPreferredSize(new Dimension(38,38));
        JPanel textos=new JPanel(new GridLayout(2,1));textos.setOpaque(false);JLabel nombre=new JLabel("Modo Shake");nombre.setForeground(Estilo.VERDE_OSCURO);nombre.setFont(Estilo.NEGRITA);JLabel subt=new JLabel(sesion.esAdministrador()?"Panel Administrativo":"Panel de Usuario");subt.setFont(new Font("SansSerif",Font.PLAIN,11));subt.setForeground(Estilo.TEXTO_SUAVE);textos.add(nombre);textos.add(subt);
        marca.add(circulo,BorderLayout.WEST);marca.add(textos,BorderLayout.CENTER);menu.add(marca,BorderLayout.NORTH);
        JPanel opciones=new JPanel(new GridLayout(0,1,0,7));opciones.setOpaque(false);opciones.setBorder(BorderFactory.createEmptyBorder(0,10,10,10));
        String[][] items={{"Inicio","▦"},{"Pedidos","◈"},{"Productos","⚒"},{"Ingredientes","♧"},{"Inventario","▣"},{"Clientes","♙"},{"Ventas","▤"},{"Empleados","♟"}};
        for(String[] item:items){String seccion=item[0];
            if(!sesion.esAdministrador()&&(seccion.equals("Inventario")||seccion.equals("Ventas")||seccion.equals("Empleados")))continue;
            JButton b=Estilo.boton(item[1]+"   "+seccion,Estilo.LATERAL,Estilo.TEXTO_SUAVE);b.setHorizontalAlignment(SwingConstants.LEFT);b.setFont(Estilo.NORMAL);b.setPreferredSize(new Dimension(180,42));b.addActionListener(e->accionNavegar.accept(seccion));botonesMenu.put(seccion,b);opciones.add(b);}
        menu.add(opciones,BorderLayout.CENTER);
        JPanel pie=new JPanel(new BorderLayout());pie.setOpaque(false);pie.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1,0,0,0,new Color(218,216,211)),BorderFactory.createEmptyBorder(12,14,13,10)));
        JLabel quien=new JLabel("●  "+sesion.getNombreVisible());quien.setFont(Estilo.NEGRITA);JButton salir=new JButton("Cerrar sesión");salir.setBorderPainted(false);salir.setContentAreaFilled(false);salir.setHorizontalAlignment(SwingConstants.LEFT);salir.setForeground(Estilo.VERDE_OSCURO);salir.addActionListener(e->accionCerrarSesion.run());
        JPanel usuario=new JPanel(new GridLayout(2,1));usuario.setOpaque(false);usuario.add(quien);usuario.add(salir);pie.add(usuario);menu.add(pie,BorderLayout.SOUTH);return menu;
    }
    private JPanel crearArea(UsuarioSesion sesion){
        JPanel area=new JPanel(new BorderLayout());area.setBackground(Estilo.FONDO);
        JPanel barra=new JPanel(new BorderLayout());barra.setBackground(new Color(253,251,250));barra.setBorder(BorderFactory.createMatteBorder(0,0,1,0,Estilo.BORDE));barra.setPreferredSize(new Dimension(0,62));
        JTextField buscar=new JTextField("⌕   Buscar...");buscar.setFont(Estilo.NORMAL);buscar.setForeground(Estilo.TEXTO_SUAVE);buscar.setPreferredSize(new Dimension(250,39));buscar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(218,218,213),1),BorderFactory.createEmptyBorder(6,14,6,12)));
        JPanel izq=new JPanel(new BorderLayout());izq.setOpaque(false);izq.setBorder(BorderFactory.createEmptyBorder(10,18,10,0));izq.add(buscar,BorderLayout.CENTER);barra.add(izq,BorderLayout.WEST);
        JLabel herramientas=new JLabel("♧    ⚙    ?      │    ●  "+sesion.getUsuario());herramientas.setFont(Estilo.NORMAL);herramientas.setForeground(Estilo.TEXTO_SUAVE);herramientas.setBorder(BorderFactory.createEmptyBorder(0,0,0,22));barra.add(herramientas,BorderLayout.EAST);
        area.add(barra,BorderLayout.NORTH);contenido.setOpaque(false);area.add(contenido,BorderLayout.CENTER);return area;
    }
    private JPanel crearInicio(UsuarioSesion sesion,DashboardDAO.ResumenDashboard datos){
        JPanel pagina=new JPanel(new BorderLayout(0,22));pagina.setOpaque(false);pagina.setBorder(BorderFactory.createEmptyBorder(30,34,24,34));
        JPanel title=new JPanel(new BorderLayout());title.setOpaque(false);JLabel h=new JLabel("Panel Principal");h.setFont(Estilo.TITULO);h.setForeground(Estilo.TEXTO);JLabel s=new JLabel("Resumen de operaciones");s.setFont(Estilo.NORMAL);s.setForeground(Estilo.TEXTO_SUAVE);title.add(h,BorderLayout.NORTH);title.add(s,BorderLayout.SOUTH);pagina.add(title,BorderLayout.NORTH);
        JPanel main=new JPanel(new BorderLayout(0,22));main.setOpaque(false);
        boolean admin=sesion.esAdministrador();
        JPanel stats=new JPanel(new GridLayout(1,0,14,0));stats.setOpaque(false);
        stats.add(metrica("Pedidos de hoy",String.valueOf(datos.getPedidosHoy()),"▣",false));
        if(admin)stats.add(metrica("Ventas de hoy",formatoMoneda(datos.getVentasHoy()),"▤",false));
        stats.add(metrica("Productos",String.valueOf(datos.getProductosActivos()),"♙",false));
        if(admin)stats.add(metrica("Stock bajo",datos.getStockBajo()+" ingredientes","⚠",datos.getStockBajo()>0));
        main.add(stats,BorderLayout.NORTH);
        JPanel cuerpo=new JPanel(new GridBagLayout());cuerpo.setOpaque(false);
        GridBagConstraints c=new GridBagConstraints();c.gridy=0;c.fill=GridBagConstraints.BOTH;c.weighty=1;c.gridx=0;
        if(admin){c.insets=new Insets(0,0,0,14);c.weightx=2.2;cuerpo.add(panelPedidos(datos.getPedidosRecientes()),c);c.gridx=1;c.weightx=1;c.insets=new Insets(0,0,0,0);cuerpo.add(panelStock(datos.getAlertasStock()),c);}
        else{c.weightx=1;cuerpo.add(panelPedidos(datos.getPedidosRecientes()),c);}
        main.add(cuerpo,BorderLayout.CENTER);pagina.add(main,BorderLayout.CENTER);
        JLabel nota=new JLabel("Resumen consultado desde la base de datos Modo Shake.");nota.setFont(new Font("SansSerif",Font.ITALIC,11));nota.setForeground(Estilo.TEXTO_SUAVE);pagina.add(nota,BorderLayout.SOUTH);return pagina;
    }
    private JPanel metrica(String titulo,String valor,String icono,boolean alerta){
        Estilo.Tarjeta card=Estilo.tarjeta(alerta?new Color(255,246,245):Color.WHITE,14);card.setLayout(new BorderLayout(0,18));card.setBorder(BorderFactory.createEmptyBorder(15,17,13,17));
        JLabel t=new JLabel(titulo+"      "+icono);t.setFont(Estilo.NORMAL);t.setForeground(alerta?new Color(170,47,42):Estilo.TEXTO_SUAVE);JLabel v=new JLabel(valor);v.setFont(new Font("SansSerif",Font.BOLD,21));v.setForeground(alerta?new Color(170,47,42):Estilo.TEXTO);card.add(t,BorderLayout.NORTH);card.add(v,BorderLayout.CENTER);return card;
    }
    private JPanel panelPedidos(java.util.List<Object[]> datos){
        Estilo.Tarjeta card=Estilo.tarjeta(Color.WHITE,14);card.setLayout(new BorderLayout(0,12));card.setBorder(BorderFactory.createEmptyBorder(16,18,16,18));
        JPanel head=new JPanel(new BorderLayout());head.setOpaque(false);JLabel title=new JLabel("Pedidos recientes");title.setFont(new Font("SansSerif",Font.BOLD,16));JLabel all=new JLabel("Ver todos  →");all.setFont(Estilo.NEGRITA);all.setForeground(Estilo.VERDE_OSCURO);all.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));all.addMouseListener(new java.awt.event.MouseAdapter(){@Override public void mouseClicked(java.awt.event.MouseEvent e){accionNavegar.accept("Pedidos");}});head.add(title,BorderLayout.WEST);head.add(all,BorderLayout.EAST);card.add(head,BorderLayout.NORTH);
        String[] cols={"PEDIDO","CLIENTE","TOTAL","ESTADO"};Object[][] rows=new Object[datos.isEmpty()?1:datos.size()][4];
        if(datos.isEmpty())rows[0]=new Object[]{"—","Sin pedidos registrados","—","—"};
        else for(int i=0;i<datos.size();i++){Object[] d=datos.get(i);rows[i]=new Object[]{String.format("#%03d",d[0]),d[1],formatoMoneda((java.math.BigDecimal)d[2]),d[3]};}
        JTable table=new JTable(new DefaultTableModel(rows,cols){@Override public boolean isCellEditable(int r,int c){return false;}});table.setRowHeight(43);table.setFont(Estilo.NORMAL);table.setShowVerticalLines(false);table.setGridColor(new Color(242,240,237));table.setSelectionBackground(new Color(233,244,233));table.getTableHeader().setFont(new Font("SansSerif",Font.BOLD,10));table.getTableHeader().setForeground(new Color(78,91,78));table.getTableHeader().setBackground(Color.WHITE);table.getTableHeader().setPreferredSize(new Dimension(0,35));
        JScrollPane scroll=new JScrollPane(table);scroll.setBorder(BorderFactory.createEmptyBorder());scroll.getViewport().setBackground(Color.WHITE);card.add(scroll,BorderLayout.CENTER);return card;
    }
    private JPanel panelStock(java.util.List<Object[]> alertas){
        Estilo.Tarjeta card=Estilo.tarjeta(Color.WHITE,14);card.setLayout(new BorderLayout(0,13));card.setBorder(BorderFactory.createEmptyBorder(16,16,15,16));JLabel title=new JLabel("Ingredientes con stock bajo");title.setFont(new Font("SansSerif",Font.BOLD,15));card.add(title,BorderLayout.NORTH);
        JPanel lista=new JPanel(new GridLayout(0,1,0,9));lista.setOpaque(false);
        if(alertas.isEmpty())lista.add(stock("✓","Sin alertas","Existencias normales"));
        else for(Object[] a:alertas)lista.add(stock("⚠",String.valueOf(a[0]),a[1]+" "+a[2]+" restantes"));
        JPanel body=new JPanel(new BorderLayout(0,10));body.setOpaque(false);body.add(lista,BorderLayout.CENTER);JButton manage=Estilo.boton("Gestionar Inventario   →",new Color(239,238,236),Estilo.TEXTO_SUAVE);manage.addActionListener(e->accionNavegar.accept("Inventario"));body.add(manage,BorderLayout.SOUTH);card.add(body,BorderLayout.CENTER);return card;
    }
    private JPanel stock(String icon,String nombre,String qty){JPanel row=new JPanel(new BorderLayout(8,0));row.setBackground(new Color(252,249,248));row.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(246,235,233)),BorderFactory.createEmptyBorder(10,10,10,8)));JLabel i=new JLabel(icon);i.setForeground(Estilo.TEXTO_SUAVE);JLabel n=new JLabel(nombre);n.setFont(Estilo.NEGRITA);JLabel q=new JLabel(qty);q.setFont(new Font("SansSerif",Font.BOLD,10));q.setForeground(new Color(170,45,43));row.add(i,BorderLayout.WEST);row.add(n,BorderLayout.CENTER);row.add(q,BorderLayout.EAST);return row;}
    private String formatoMoneda(java.math.BigDecimal valor){java.text.NumberFormat f=java.text.NumberFormat.getNumberInstance(new java.util.Locale("es","CO"));f.setMaximumFractionDigits(0);return "$ "+f.format(valor);}
    public void definirSeccion(String nombre,java.awt.Component componente){if(!botonesMenu.containsKey(nombre)||nombre.equals("Inicio"))return;contenido.add(componente,nombre);contenido.revalidate();contenido.repaint();}
    public void alNavegar(java.util.function.Consumer<String> accion){accionNavegar=accion;}
    public void alCerrarSesion(Runnable accion){accionCerrarSesion=accion;}
    public void mostrarSeccion(String nombre){
        if(!botonesMenu.containsKey(nombre))return;
        ((CardLayout)contenido.getLayout()).show(contenido,nombre);for(Map.Entry<String,JButton> e:botonesMenu.entrySet()){boolean on=e.getKey().equals(nombre);JButton b=e.getValue();b.setBackground(on?Estilo.VERDE:Estilo.LATERAL);b.setForeground(on?new Color(18,63,24):Estilo.TEXTO_SUAVE);b.setOpaque(true);}}
}
