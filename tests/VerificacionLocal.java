import modo_shake.*;
import javax.swing.*;
import javax.swing.text.AbstractDocument;

public class VerificacionLocal {
    static void verificar(boolean valor, String mensaje) {
        if (!valor) throw new AssertionError(mensaje);
    }
    public static void main(String[] args) throws Exception {
        verificar(ConexionBD.getInstancia()==ConexionBD.getInstancia(),"Singleton");
        String hash=Contrasenas.generarHash("PruebaLocal2026!".toCharArray());
        verificar(Contrasenas.verificar("PruebaLocal2026!".toCharArray(),hash),"Clave correcta");
        verificar(!Contrasenas.verificar("incorrecta".toCharArray(),hash),"Clave incorrecta");
        SwingUtilities.invokeAndWait(() -> {
            try {
                JTextField precio=new JTextField(); FiltrosEntrada.decimal(precio,8,2);
                precio.setText("123.45"); verificar(precio.getText().equals("123.45"),"Precio decimal");
                precio.setText("abc"); verificar(precio.getText().equals("123.45"),"Bloquear letras pegadas");
                precio.setText("123.456"); verificar(precio.getText().equals("123.45"),"Limitar decimales");
                precio.setText("123456789"); verificar(precio.getText().equals("123.45"),"Limitar precision");
                ((AbstractDocument)precio.getDocument()).replace(0,6,"12,50",null);
                verificar(precio.getText().equals("12,50"),"Reemplazo de seleccion");
                precio.setText(""); verificar(precio.getText().isEmpty(),"Permitir limpiar");
                JTextField nombre=new JTextField(); FiltrosEntrada.texto(nombre,3);
                nombre.setText("abc"); nombre.setText("abcd");
                verificar(nombre.getText().equals("abc"),"Limitar longitud");
            } catch(Exception e) { throw new RuntimeException(e); }
        });
        System.out.println("OK: Singleton, hash de contrasenas y filtros de entrada.");
    }
}
