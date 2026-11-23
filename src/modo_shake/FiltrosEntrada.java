package modo_shake;

import javax.swing.text.*;

public final class FiltrosEntrada {
    private FiltrosEntrada() { }
    public static void texto(JTextComponent campo, int maximo) { aplicar(campo, maximo, null); }
    public static void decimal(JTextComponent campo, int enteros, int decimales) {
        aplicar(campo, enteros + decimales + 1, "[0-9]{0," + enteros + "}([.,][0-9]{0," + decimales + "})?");
    }
    private static void aplicar(JTextComponent campo, int maximo, String patron) {
        ((AbstractDocument) campo.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override public void insertString(FilterBypass fb, int offset, String texto, AttributeSet atributos)
                    throws BadLocationException { replace(fb, offset, 0, texto, atributos); }
            @Override public void replace(FilterBypass fb, int offset, int largo, String texto, AttributeSet atributos)
                    throws BadLocationException {
                String actual = fb.getDocument().getText(0, fb.getDocument().getLength());
                String nuevo = actual.substring(0, offset) + (texto == null ? "" : texto) + actual.substring(offset + largo);
                if (nuevo.length() <= maximo && (patron == null || nuevo.matches(patron)))
                    fb.replace(offset, largo, texto, atributos);
            }
        });
    }
}
