package modo_shake;

public class Empleado {
    private final int id;
    private final String nombre;
    private final String apellido;
    private final String telefono;
    private final String puesto;
    private final String usuario;
    private final String perfil;
    private final String estado;

    public Empleado(int id, String nombre, String apellido, String telefono,
                    String puesto, String usuario, String perfil, String estado) {
        this.id = id; this.nombre = nombre; this.apellido = apellido;
        this.telefono = telefono == null ? "" : telefono; this.puesto = puesto;
        this.usuario = usuario; this.perfil = perfil; this.estado = estado;
    }
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getTelefono() { return telefono; }
    public String getPuesto() { return puesto; }
    public String getUsuario() { return usuario; }
    public String getPerfil() { return perfil; }
    public String getEstado() { return estado; }
}
