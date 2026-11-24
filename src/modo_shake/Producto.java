package modo_shake;

import java.math.BigDecimal;

public class Producto {
    private final int id;
    private final int categoriaId;
    private final String nombre;
    private final String categoria;
    private final BigDecimal precio;
    private final String descripcion;
    private final boolean activo;

    public Producto(int id, int categoriaId, String nombre, String categoria,
                    BigDecimal precio, String descripcion, boolean activo) {
        this.id = id;
        this.categoriaId = categoriaId;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.descripcion = descripcion == null ? "" : descripcion;
        this.activo = activo;
    }

    public int getId() { return id; }
    public int getCategoriaId() { return categoriaId; }
    public String getNombre() { return nombre; }
    public String getCategoria() { return categoria; }
    public BigDecimal getPrecio() { return precio; }
    public String getDescripcion() { return descripcion; }
    public boolean isActivo() { return activo; }
    @Override public String toString() { return nombre + " — $" + precio.toPlainString(); }
}
