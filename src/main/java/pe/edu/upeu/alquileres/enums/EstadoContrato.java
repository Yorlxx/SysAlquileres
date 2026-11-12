package pe.edu.upeu.alquileres.enums;

import java.util.Arrays;

public enum EstadoContrato {
    VIGENTE("Vigente"),
    FINALIZADO("Finalizado");

    private final String descripcion;

    EstadoContrato(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }

    public static EstadoContrato desde(String valor) {
        return Arrays.stream(values())
                .filter(e -> e.descripcion.equalsIgnoreCase(valor) || e.name().equalsIgnoreCase(valor))
                .findFirst()
                .orElse(VIGENTE);
    }
}
