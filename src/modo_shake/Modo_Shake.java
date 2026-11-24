package modo_shake;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Modo_Shake {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                // Se conserva el aspecto predeterminado de Swing si el sistema no está disponible.
            }
            new LoginController().iniciar();
        });
    }
}
