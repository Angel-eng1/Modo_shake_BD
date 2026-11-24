package modo_shake;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class LoginView extends JFrame {
    private final PlaceholderTextField campoUsuario = new PlaceholderTextField(" Ingresa tu usuario");
    private final JPasswordField campoContrasena = new JPasswordField();
    private final JButton botonIngresar = Estilo.boton("Iniciar sesión", Estilo.VERDE, Color.WHITE);
    private final JLabel mensaje = new JLabel(" ", SwingConstants.CENTER);
    private final JButton configurarAdmin = new JButton("Configurar administrador inicial");

    public LoginView() {
        setTitle("Modo Shake | Inicio de sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 650)); setSize(1100, 760);
        setLocationRelativeTo(null); getContentPane().setBackground(Estilo.FONDO);
        setLayout(new BorderLayout());

        JPanel centro = new JPanel(new GridBagLayout()); centro.setOpaque(false);
        Estilo.Tarjeta tarjeta = Estilo.tarjeta(new Color(253, 251, 250), 24);
        tarjeta.setLayout(new GridBagLayout());
        tarjeta.setBorder(BorderFactory.createEmptyBorder(34, 48, 30, 48));
        tarjeta.setPreferredSize(new Dimension(510, 650));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx=0; c.gridy=0; c.gridwidth=2; c.anchor=GridBagConstraints.CENTER;
        c.insets=new Insets(0,0,18,0); tarjeta.add(new Estilo.IconoHoja(), c);

        c.gridy++; c.insets=new Insets(0,0,4,0);
        JLabel titulo = new JLabel("Modo Shake"); titulo.setFont(new Font("SansSerif",Font.BOLD,39));
        titulo.setForeground(Estilo.VERDE_OSCURO); tarjeta.add(titulo,c);
        c.gridy++; c.insets=new Insets(0,0,44,0);
        JLabel subtitulo = new JLabel("Sistema de Gestión de Pedidos");
        subtitulo.setFont(new Font("SansSerif",Font.PLAIN,17)); subtitulo.setForeground(Estilo.TEXTO_SUAVE);
        tarjeta.add(subtitulo,c);

        c.gridy++; c.gridwidth=2; c.anchor=GridBagConstraints.WEST; c.insets=new Insets(0,0,7,0);
        tarjeta.add(etiqueta("Usuario"),c);
        c.gridy++; c.fill=GridBagConstraints.HORIZONTAL; c.insets=new Insets(0,0,18,0);
        tarjeta.add(campo(campoUsuario,"♙   Ingresa tu usuario"),c);
        c.gridy++; c.fill=GridBagConstraints.NONE; c.insets=new Insets(0,0,7,0);
        tarjeta.add(etiqueta("Contraseña"),c);
        c.gridy++; c.fill=GridBagConstraints.HORIZONTAL; c.insets=new Insets(0,0,24,0);
        campoContrasena.setFont(new Font("SansSerif",Font.PLAIN,16));
        campoContrasena.setPreferredSize(new Dimension(355,48));
        campoContrasena.setBorder(BorderFactory.createEmptyBorder(8,6,8,8));
        campoContrasena.setBackground(new Color(253,251,250));
        tarjeta.add(contenedorCampo("▣",campoContrasena),c);
        c.gridy++; c.insets=new Insets(0,0,14,0); botonIngresar.setPreferredSize(new Dimension(410,50));
        tarjeta.add(botonIngresar,c);
        c.gridy++; c.insets=new Insets(0,0,0,0); mensaje.setForeground(new Color(170,45,45));
        tarjeta.add(mensaje,c);
        c.gridy++; c.insets=new Insets(10,0,0,0);
        configurarAdmin.setForeground(new Color(117,92,56));
        configurarAdmin.setFont(Estilo.NORMAL);
        configurarAdmin.setBorderPainted(false);
        configurarAdmin.setContentAreaFilled(false);
        configurarAdmin.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        tarjeta.add(configurarAdmin,c);
        centro.add(tarjeta); add(centro,BorderLayout.CENTER);
        JLabel pie = new JLabel("Sahagún, Córdoba",SwingConstants.CENTER);
        pie.setForeground(new Color(145,145,140)); pie.setBorder(BorderFactory.createEmptyBorder(0,0,12,0));
        add(pie,BorderLayout.SOUTH);
    }

    private JLabel etiqueta(String texto) { JLabel l=new JLabel(texto); l.setFont(Estilo.NORMAL); l.setForeground(Estilo.TEXTO); return l; }
    private JPanel campo(JTextField field,String placeholder) {
        field.setFont(new Font("SansSerif",Font.PLAIN,15)); field.setPreferredSize(new Dimension(355,48));
        field.setBorder(BorderFactory.createEmptyBorder(8,6,8,8));
        field.setBackground(new Color(253,251,250));
        return contenedorCampo("♙",field);
    }
    private JPanel contenedorCampo(String icono,JTextField field) {
        JPanel p=new JPanel(new BorderLayout(8,0)); p.setBackground(new Color(253,251,250));
        p.setBorder(BorderFactory.createLineBorder(new Color(205,215,203),1));
        p.setPreferredSize(new Dimension(410,58));
        JLabel i=new JLabel(icono); i.setForeground(new Color(98,115,97)); i.setFont(new Font("SansSerif",Font.PLAIN,17));
        i.setBorder(BorderFactory.createEmptyBorder(0,13,0,3));
        p.add(i,BorderLayout.WEST); p.add(field,BorderLayout.CENTER); return p;
    }
    private static class PlaceholderTextField extends JTextField {
        private final String placeholder;
        PlaceholderTextField(String placeholder){this.placeholder=placeholder;}
        @Override protected void paintComponent(java.awt.Graphics g){
            super.paintComponent(g);
            if(getText().isEmpty()&&!isFocusOwner()){
                g.setColor(new Color(183,196,180));g.setFont(getFont());
                g.drawString(placeholder,getInsets().left+2,getHeight()/2+getFontMetrics(getFont()).getAscent()/2-2);
            }
        }
    }
    public void alIngresar(Runnable accion) { botonIngresar.addActionListener(e -> accion.run()); }
    public void alCrearAdmin(Runnable accion) { configurarAdmin.addActionListener(e -> accion.run()); }
    public String getUsuario() { return campoUsuario.getText().trim(); }
    public char[] getContrasena() { return campoContrasena.getPassword(); }
    public void mostrarMensaje(String texto) { mensaje.setText(texto); }
}
