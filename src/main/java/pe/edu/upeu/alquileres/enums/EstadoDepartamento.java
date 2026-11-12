package pe.edu.upeu.alquileres.enums;

import java.util.Arrays;

public enum EstadoDepartamento {
    DISPONIBLE("Disponible"),
    OCUPADO("Ocupado");

    private final String descripcion;

    EstadoDepartamento(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }

    public static EstadoDepartamento desde(String valor) {
        return Arrays.stream(values())
                .filter(e -> e.descripcion.equalsIgnoreCase(valor) || e.name().equalsIgnoreCase(valor))
                .findFirst()
                .orElse(DISPONIBLE);
    }
}
