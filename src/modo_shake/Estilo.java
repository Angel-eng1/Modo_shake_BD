package modo_shake;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

public final class Estilo {
    public static final Color VERDE = new Color(76, 175, 80);
    public static final Color VERDE_OSCURO = new Color(8, 112, 39);
    public static final Color FONDO = new Color(250, 248, 247);
    public static final Color LATERAL = new Color(246, 243, 241);
    public static final Color TEXTO = new Color(34, 38, 35);
    public static final Color TEXTO_SUAVE = new Color(94, 103, 94);
    public static final Color BORDE = new Color(231, 230, 226);
    public static final Font TITULO = new Font("SansSerif", Font.BOLD, 27);
    public static final Font SUBTITULO = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font NORMAL = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font NEGRITA = new Font("SansSerif", Font.BOLD, 13);

    private Estilo() { }

    public static Tarjeta tarjeta(Color color, int radio) { return new Tarjeta(color, radio); }

    public static JButton boton(String texto, Color fondo, Color letra) {
        return new BotonRedondeado(texto, fondo, letra);
    }

    public static class Tarjeta extends JPanel {
        private final Color color;
        private final int radio;
        public Tarjeta(Color color, int radio) {
            this.color = color; this.radio = radio; setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 0, 0, 10));
            g2.fillRoundRect(2, 3, getWidth()-4, getHeight()-5, radio, radio);
            g2.setColor(color);
            g2.fillRoundRect(0, 0, getWidth()-4, getHeight()-6, radio, radio);
            g2.setColor(BORDE);
            g2.drawRoundRect(0, 0, getWidth()-5, getHeight()-7, radio, radio);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class BotonRedondeado extends JButton {
        private final Color fondo;
        private final Color letra;
        BotonRedondeado(String texto, Color fondo, Color letra) {
            super(texto); this.fondo = fondo; this.letra = letra;
            setForeground(letra); setFont(NEGRITA); setFocusPainted(false);
            setBorderPainted(false); setContentAreaFilled(false); setOpaque(false);
            setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
            setMargin(new java.awt.Insets(9, 16, 9, 16));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color actual = getModel().isPressed() ? fondo.darker() : fondo;
            g2.setColor(actual);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            if (fondo.equals(Color.WHITE)) {
                g2.setColor(letra); g2.setStroke(new java.awt.BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class IconoHoja extends JComponent {
        public IconoHoja() { setPreferredSize(new java.awt.Dimension(58,58)); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(246, 239, 226)); g2.fillOval(3,3,52,52);
            g2.setColor(new Color(104, 85, 45)); g2.setStroke(new java.awt.BasicStroke(2.2f));
            Path2D leaf = new Path2D.Double();
            leaf.moveTo(19,35); leaf.curveTo(17,20,27,14,41,16); leaf.curveTo(42,29,35,39,23,38);
            g2.draw(leaf); g2.drawLine(20,37,34,23);
            g2.dispose();
        }
    }
}
