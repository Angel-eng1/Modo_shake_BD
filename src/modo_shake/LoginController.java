package modo_shake;

import java.sql.SQLException;
import java.util.Arrays;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

public class LoginController {
    private LoginView vista;

    public void iniciar() {
        vista = new LoginView();
        vista.alIngresar(this::iniciarSesion);
        vista.alCrearAdmin(this::crearAdministrador);
        vista.setVisible(true);
    }

    private void iniciarSesion() {
        String usuario = vista.getUsuario();
        char[] contrasena = vista.getContrasena();
        if (usuario.isBlank() || contrasena.length == 0) {
            Arrays.fill(contrasena, '\0');
            vista.mostrarMensaje("Ingresa el usuario y la contraseña.");
            return;
        }
        try {
            UsuarioSesion sesion = new EmpleadoDAO().autenticar(usuario, contrasena);
            if (sesion == null) {
                vista.mostrarMensaje("Usuario o contraseña incorrectos, o cuenta inactiva.");
                return;
            }
            vista.dispose();
            new DashboardController(sesion).mostrar();
        } catch (SQLException ex) {
            Arrays.fill(contrasena, '\0');
            vista.mostrarMensaje("No fue posible iniciar sesión. Revisa la conexión a MySQL.");
            JOptionPane.showMessageDialog(vista, ex.getMessage(), "Conexión con la base de datos",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void crearAdministrador() {
        try {
            String nombre = JOptionPane.showInputDialog(vista, "Nombre:", "Configurar administrador",
                    JOptionPane.QUESTION_MESSAGE);
            if (nombre == null || nombre.isBlank()) return;
            String apellido = JOptionPane.showInputDialog(vista, "Apellido:", "Configurar administrador",
                    JOptionPane.QUESTION_MESSAGE);
            if (apellido == null || apellido.isBlank()) return;
            String usuario = JOptionPane.showInputDialog(vista, "Nombre de usuario:", "Configurar administrador",
                    JOptionPane.QUESTION_MESSAGE);
            if (usuario == null || usuario.isBlank()) return;
            JPasswordField campo = new JPasswordField();
            int resultado = JOptionPane.showConfirmDialog(vista, campo,
                    "Contraseña (mínimo 8 caracteres)", JOptionPane.OK_CANCEL_OPTION);
            if (resultado != JOptionPane.OK_OPTION) return;
            char[] clave = campo.getPassword();
            if (clave.length < 8) {
                Arrays.fill(clave, '\0');
                vista.mostrarMensaje("La contraseña debe tener al menos 8 caracteres.");
                return;
            }
            JPasswordField confirmar = new JPasswordField();
            resultado = JOptionPane.showConfirmDialog(vista, confirmar,
                    "Confirma la contraseña", JOptionPane.OK_CANCEL_OPTION);
            if (resultado != JOptionPane.OK_OPTION) {
                Arrays.fill(clave, '\0');
                return;
            }
            char[] confirmacion = confirmar.getPassword();
            if (!Arrays.equals(clave, confirmacion)) {
                Arrays.fill(clave, '\0');
                Arrays.fill(confirmacion, '\0');
                vista.mostrarMensaje("Las contraseñas no coinciden.");
                return;
            }
            Arrays.fill(confirmacion, '\0');
            new EmpleadoDAO().crearPrimerAdministrador(nombre.trim(), apellido.trim(), usuario.trim(), clave);
            vista.mostrarMensaje("Administrador creado. Ya puedes iniciar sesión.");
        } catch (SQLException ex) {
            vista.mostrarMensaje("No se pudo configurar el administrador.");
            JOptionPane.showMessageDialog(vista, ex.getMessage(), "Configuración",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
