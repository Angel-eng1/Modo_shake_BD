package modo_shake;

public class Cliente {
    private final int id;
    private final String nombre;
    private final String telefono;
    private final String correo;

    public Cliente(int id, String nombre, String telefono, String correo) {
        this.id = id;
        this.nombre = nombre;
        this.telefono = telefono == null ? "" : telefono;
        this.correo = correo == null ? "" : correo;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getTelefono() { return telefono; }
    public String getCorreo() { return correo; }

    @Override public String toString() { return nombre; }
}
