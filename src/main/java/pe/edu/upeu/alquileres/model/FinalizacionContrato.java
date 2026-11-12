package pe.edu.upeu.alquileres.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalizacionContrato {

    private Long idFinalizacion;
    private Contrato contrato;
    private LocalDateTime fecha;
    private String motivo;

    @Override
    public String toString() {
        return motivo;
    }
}
