package pe.edu.upeu.alquileres.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upeu.alquileres.enums.MetodoPago;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pago {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_MES = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.of("es", "PE"));

    private Long idPago;

    @NotNull(message = "La fecha de pago es obligatoria")
    private LocalDate fechaPago;

    @NotNull(message = "El periodo es obligatorio")
    private LocalDate periodo;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    private BigDecimal monto;

    @NotNull(message = "El método de pago es obligatorio")
    private MetodoPago metodo;

    @NotNull(message = "Debe seleccionar un contrato")
    private Contrato contrato;

    private BigDecimal descuento;
    private BigDecimal igv;

    public String getInquilinoNombre() {
        return contrato == null || contrato.getInquilino() == null ? "" : contrato.getInquilino().getNombreCompleto();
    }

    public String getDepartamentoNumero() {
        return contrato == null || contrato.getDepartamento() == null ? "" : contrato.getDepartamento().getNumero();
    }

    public String getMontoTexto() {
        return "S/ " + String.format(Locale.US, "%,.2f", monto == null ? BigDecimal.ZERO : monto);
    }

    public String getFechaTexto() {
        return fechaPago == null ? "" : FMT.format(fechaPago);
    }

    public String getPeriodoTexto() {
        return periodo == null ? "" : FMT_MES.format(periodo);
    }

    public String getMetodoTexto() {
        return metodo == null ? "" : metodo.getDescripcion();
    }

    public BigDecimal getDescuento() {
        return descuento == null ? BigDecimal.ZERO : descuento;
    }

    public BigDecimal getIgv() {
        return igv == null ? BigDecimal.ZERO : igv;
    }

    @Override
    public String toString() {
        return "Pago #" + idPago + " · " + getPeriodoTexto() + " · " + getMontoTexto();
    }
}
