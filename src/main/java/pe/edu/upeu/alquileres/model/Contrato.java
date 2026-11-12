package pe.edu.upeu.alquileres.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upeu.alquileres.enums.EstadoContrato;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contrato {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Long idContrato;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate fechaFin;

    @NotNull(message = "El monto mensual es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto mensual debe ser mayor a cero")
    private BigDecimal montoMensual;

    @NotNull(message = "El estado del contrato es obligatorio")
    private EstadoContrato estado;

    @NotNull(message = "Debe seleccionar un inquilino")
    private Inquilino inquilino;

    @NotNull(message = "Debe seleccionar un departamento")
    private Departamento departamento;

    private LocalDateTime fechaContrato;

    @NotNull(message = "La cantidad de personas es obligatoria")
    @Min(value = 1, message = "Debe registrar al menos una persona")
    private Integer cantidadPersona;

    public String getInquilinoNombre() {
        return inquilino == null ? "" : inquilino.getNombreCompleto();
    }

    public String getDepartamentoNumero() {
        return departamento == null ? "" : departamento.getNumero();
    }

    public String getMontoTexto() {
        return "S/ " + String.format(Locale.US, "%,.2f", montoMensual == null ? BigDecimal.ZERO : montoMensual);
    }

    public String getFechasTexto() {
        if (fechaInicio == null || fechaFin == null) return "";
        return FMT.format(fechaInicio) + " al " + FMT.format(fechaFin);
    }

    public boolean isVigente() {
        return estado == EstadoContrato.VIGENTE;
    }

    @Override
    public String toString() {
        return "Contrato #" + idContrato + " · " + getInquilinoNombre()
                + " · Dpto. " + getDepartamentoNumero()
                + (estado == null ? "" : " · " + estado.getDescripcion());
    }
}
