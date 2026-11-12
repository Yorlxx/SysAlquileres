package pe.edu.upeu.alquileres.enums;

import java.util.Arrays;

public enum MetodoPago {
    EFECTIVO("Efectivo"),
    TRANSFERENCIA("Transferencia"),
    YAPE("Yape"),
    PLIN("Plin"),
    TARJETA("Tarjeta");

    private final String descripcion;

    MetodoPago(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }

    public static MetodoPago desde(String valor) {
        return Arrays.stream(values())
                .filter(e -> e.descripcion.equalsIgnoreCase(valor) || e.name().equalsIgnoreCase(valor))
                .findFirst()
                .orElse(EFECTIVO);
    }
}
