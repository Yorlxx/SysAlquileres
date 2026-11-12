package pe.edu.upeu.alquileres.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upeu.alquileres.enums.EstadoDepartamento;

import java.math.BigDecimal;
import java.util.Locale;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Departamento {

    private Long idDepartamento;

    @NotBlank(message = "El número de departamento es obligatorio")
    @Size(max = 10, message = "El número admite como máximo 10 caracteres")
    private String numero;

    @NotNull(message = "El piso es obligatorio")
    @Min(value = 1, message = "El piso debe ser mayor o igual a 1")
    private Integer piso;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 150, message = "La dirección admite como máximo 150 caracteres")
    private String direccion;

    @NotNull(message = "El precio mensual es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio mensual debe ser mayor a cero")
    private BigDecimal precioMensual;

    @NotNull(message = "El estado del departamento es obligatorio")
    private EstadoDepartamento estado;

    public String getPrecioTexto() {
        return "S/ " + String.format(Locale.US, "%,.2f", precioMensual == null ? BigDecimal.ZERO : precioMensual);
    }

    public String getDescripcionCorta() {
        return "Dpto. " + numero + " · Piso " + piso;
    }

    @Override
    public String toString() {
        return "Dpto. " + numero + " · Piso " + piso + " · " + getPrecioTexto();
    }
}
