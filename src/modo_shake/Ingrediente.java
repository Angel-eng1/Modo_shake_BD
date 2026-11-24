package modo_shake;

import java.math.BigDecimal;

public class Ingrediente {
    private final int id;
    private final String nombre;
    private final String unidad;
    private final BigDecimal stockMinimo;
    private final boolean activo;

    public Ingrediente(int id, String nombre, String unidad, BigDecimal stockMinimo, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.unidad = unidad;
        this.stockMinimo = stockMinimo;
        this.activo = activo;
    }
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getUnidad() { return unidad; }
    public BigDecimal getStockMinimo() { return stockMinimo; }
    public boolean isActivo() { return activo; }
    @Override public String toString() { return nombre; }
}
