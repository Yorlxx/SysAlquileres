package pe.edu.upeu.alquileres.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoPersoneria {

    private Integer idTipoPersoneria;
    private String nombre;

    @Override
    public String toString() {
        return nombre;
    }
}
