package modo_shake;

import java.sql.*;

/** Ejecucion parametrizada de las operaciones de escritura del servidor. */
final class Rutinas {
    private Rutinas() { }

    static void ejecutar(String llamada, Object... valores) throws SQLException {
        try (Connection cn = ConexionBD.abrir(); CallableStatement cs = cn.prepareCall(llamada)) {
            enlazar(cs, valores);
            cs.execute();
        }
    }

    static int identificador(String llamada, String columna, Object... valores) throws SQLException {
        try (Connection cn = ConexionBD.abrir(); CallableStatement cs = cn.prepareCall(llamada)) {
            enlazar(cs, valores);
            boolean resultado = cs.execute();
            while (!resultado && cs.getUpdateCount() != -1) resultado = cs.getMoreResults();
            if (resultado) try (ResultSet rs = cs.getResultSet()) {
                if (rs.next()) return rs.getInt(columna);
            }
            throw new SQLException("La operacion no devolvio su identificador.");
        }
    }

    private static void enlazar(CallableStatement cs, Object[] valores) throws SQLException {
        for (int i = 0; i < valores.length; i++) cs.setObject(i + 1, valores[i]);
    }
}
