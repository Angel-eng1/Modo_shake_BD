package modo_shake;

public class UsuarioSesion {
    public enum Perfil { ADMIN, USUARIO }

    private final int idEmpleado;
    private final String usuario;
    private final String nombreVisible;
    private final Perfil perfil;

    public UsuarioSesion(int idEmpleado, String usuario, String nombreVisible, Perfil perfil) {
        this.idEmpleado = idEmpleado;
        this.usuario = usuario;
        this.nombreVisible = nombreVisible;
        this.perfil = perfil;
    }

    public UsuarioSesion(String usuario, String nombreVisible, Perfil perfil) {
        this(0, usuario, nombreVisible, perfil);
    }

    public int getIdEmpleado() { return idEmpleado; }
    public String getUsuario() { return usuario; }
    public String getNombreVisible() { return nombreVisible; }
    public Perfil getPerfil() { return perfil; }
    public boolean esAdministrador() { return perfil == Perfil.ADMIN; }
}
