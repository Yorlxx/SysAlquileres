package pe.edu.upeu.alquileres.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Usuario que puede iniciar sesión en el sistema. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    private Long idUsuario;
    private String usuario;
    private String clave;
    private String nombre;
    private String rol;
    private String estado;

    public boolean isActivo() {
        return "Activo".equalsIgnoreCase(estado);
    }

    @Override
    public String toString() {
        return nombre == null ? usuario : nombre;
    }
}
