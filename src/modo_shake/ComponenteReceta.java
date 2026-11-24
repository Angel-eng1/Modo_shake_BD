package modo_shake;

import java.math.BigDecimal;

public record ComponenteReceta(int idIngrediente, String nombre, String unidad,
                               BigDecimal cantidad, boolean activo) {
}
