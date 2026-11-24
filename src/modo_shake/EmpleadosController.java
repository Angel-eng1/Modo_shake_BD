package modo_shake;

import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class EmpleadosController {
    private final VistaTablaModulo vista = new VistaTablaModulo("Usuarios y Perfiles",
            "Crea cuentas y asigna los perfiles ADMIN o USUARIO.",
            new String[]{"ID", "EMPLEADO", "USUARIO", "PERFIL", "PUESTO", "ESTADO"},
            true, true, false, "＋  Nuevo usuario");
    private final EmpleadoDAO dao = new EmpleadoDAO();
    private List<Empleado> empleados = new ArrayList<>();

    public EmpleadosController() {
        vista.alNuevo(this::nuevo); vista.alEditar(this::editar); vista.alActualizar(this::refrescar); refrescar();
    }
    public VistaTablaModulo getVista() { return vista; }

    private void refrescar() {
        try {
            empleados = dao.listar();
            List<Object[]> filas = new ArrayList<>();
            for (Empleado e : empleados) filas.add(new Object[]{e.getId(),e.getNombre()+" "+e.getApellido(),
                    e.getUsuario(),e.getPerfil(),e.getPuesto(),e.getEstado()});
            vista.cargarFilas(filas);
        } catch (SQLException ex) { error(ex); }
    }
    private void nuevo() {
        formulario(new Empleado(0,"","","","", "", "USUARIO","Activo"));
    }
    private void editar() {
        int fila = vista.filaSeleccionada();
        if (fila < 0) { aviso("Selecciona un usuario."); return; }
        formulario(empleados.get(fila));
    }

    private void formulario(Empleado e) {
        JTextField nombre = new JTextField(e.getNombre());
        JTextField apellido = new JTextField(e.getApellido());
        JTextField telefono = new JTextField(e.getTelefono());
        JTextField puesto = new JTextField(e.getPuesto());
        JTextField usuario = new JTextField(e.getUsuario());
        JComboBox<String> perfil = new JComboBox<>(new String[]{"ADMIN","USUARIO"});
        perfil.setSelectedItem(e.getPerfil());
        JComboBox<String> estado = new JComboBox<>(new String[]{"Activo","Inactivo"});
        estado.setSelectedItem(e.getEstado());
        JPasswordField clave = new JPasswordField();
        JPanel panel = new JPanel(new GridLayout(0,1,4,4));
        panel.add(new javax.swing.JLabel("Nombre")); panel.add(nombre);
        panel.add(new javax.swing.JLabel("Apellido")); panel.add(apellido);
        panel.add(new javax.swing.JLabel("Teléfono (opcional)")); panel.add(telefono);
        panel.add(new javax.swing.JLabel("Puesto")); panel.add(puesto);
        panel.add(new javax.swing.JLabel("Usuario")); panel.add(usuario);
        panel.add(new javax.swing.JLabel("Perfil")); panel.add(perfil);
        panel.add(new javax.swing.JLabel("Estado")); panel.add(estado);
        panel.add(new javax.swing.JLabel(e.getId()==0?"Contraseña (mínimo 8 caracteres)":"Nueva contraseña (dejar vacía para conservar)"));
        panel.add(clave);
        int r=JOptionPane.showConfirmDialog(vista,panel,e.getId()==0?"Crear usuario":"Editar usuario",
                JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);
        if(r!=JOptionPane.OK_OPTION)return;
        char[] password=clave.getPassword();
        if(nombre.getText().isBlank()||apellido.getText().isBlank()||puesto.getText().isBlank()||usuario.getText().isBlank()){
            java.util.Arrays.fill(password,'\0');aviso("Completa nombre, apellido, puesto y usuario.");return;
        }
        if(e.getId()==0&&password.length<8){java.util.Arrays.fill(password,'\0');aviso("La contraseña debe tener al menos 8 caracteres.");return;}
        try{
            Empleado actualizado=new Empleado(e.getId(),nombre.getText().trim(),apellido.getText().trim(),
                    telefono.getText().trim(),puesto.getText().trim(),usuario.getText().trim(),
                    (String)perfil.getSelectedItem(),(String)estado.getSelectedItem());
            dao.guardar(actualizado,password);
            refrescar();
        }catch(SQLException ex){error(ex);}
    }
    private void aviso(String s){JOptionPane.showMessageDialog(vista,s,"Usuarios",JOptionPane.INFORMATION_MESSAGE);}
    private void error(SQLException ex){JOptionPane.showMessageDialog(vista,ex.getMessage(),"Usuarios",JOptionPane.ERROR_MESSAGE);}
}

