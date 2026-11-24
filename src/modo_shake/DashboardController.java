package modo_shake;

import java.sql.SQLException;
import javax.swing.JOptionPane;

public class DashboardController {
    private final UsuarioSesion sesion;
    private DashboardView vista;

    public DashboardController(UsuarioSesion sesion) { this.sesion = sesion; }

    public void mostrar() {
        ProductosController productos = new ProductosController();
        PedidosController pedidos = new PedidosController(sesion);
        ClientesController clientes = new ClientesController();
        IngredientesController ingredientes = new IngredientesController();
        InventarioController inventario = sesion.esAdministrador() ? new InventarioController() : null;
        VentasController ventas = sesion.esAdministrador() ? new VentasController() : null;
        EmpleadosController empleados = sesion.esAdministrador() ? new EmpleadosController() : null;
        DashboardDAO.ResumenDashboard resumen;
        try { resumen = new DashboardDAO().cargar(); }
        catch (SQLException ex) {
            resumen = DashboardDAO.ResumenDashboard.vacio();
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Panel principal", JOptionPane.ERROR_MESSAGE);
        }
        vista = new DashboardView(sesion, productos.getVista(), resumen);
        vista.definirSeccion("Pedidos", pedidos.getVista());
        vista.definirSeccion("Clientes", clientes.getVista());
        vista.definirSeccion("Ingredientes", ingredientes.getVista());
        if (sesion.esAdministrador()) {
            vista.definirSeccion("Inventario", inventario.getVista());
            vista.definirSeccion("Ventas", ventas.getVista());
            vista.definirSeccion("Empleados", empleados.getVista());
        }
        vista.alNavegar(this::navegar);
        vista.alCerrarSesion(this::cerrarSesion);
        vista.setVisible(true);
    }

    private void navegar(String seccion) {
        if ((seccion.equals("Inventario") || seccion.equals("Ventas") || seccion.equals("Empleados")) && !sesion.esAdministrador()) {
            JOptionPane.showMessageDialog(vista, "Tu perfil no tiene acceso a este módulo.",
                    "Acceso restringido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        vista.mostrarSeccion(seccion);
    }

    private void cerrarSesion() {
        vista.dispose();
        new LoginController().iniciar();
    }
}
