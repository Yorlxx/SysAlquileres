package pe.edu.upeu.alquileres.util;

import pe.edu.upeu.alquileres.model.Usuario;

/** Guarda el usuario que inició sesión durante la ejecución. */
public final class Sesion {

    private static Usuario actual;

    private Sesion() {
    }

    public static void iniciar(Usuario usuario) {
        actual = usuario;
    }

    public static Usuario actual() {
        return actual;
    }

    public static String nombre() {
        return actual == null ? "" : actual.getNombre();
    }

    public static void cerrar() {
        actual = null;
    }
}
