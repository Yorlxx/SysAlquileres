package pe.edu.upeu.alquileres.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inquilino {

    private Long idInquilino;

    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "^[0-9]{8}$", message = "El DNI debe tener exactamente 8 dígitos")
    private String dni;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 80, message = "El nombre admite como máximo 80 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 80, message = "El apellido admite como máximo 80 caracteres")
    private String apellido;

    @Size(max = 15, message = "El teléfono admite como máximo 15 caracteres")
    private String telefono;

    @Size(max = 150, message = "La dirección admite como máximo 150 caracteres")
    private String direccion;

    @NotNull(message = "El tipo de personería es obligatorio")
    private TipoPersoneria tipoPersoneria;

    @Size(max = 60, message = "El representante legal admite como máximo 60 caracteres")
    private String repLegal;

    @Pattern(regexp = "^$|^[0-9]{10,11}$", message = "El RUC debe tener 10 u 11 dígitos")
    private String ruc;

    @Size(max = 60, message = "La dirección fiscal admite como máximo 60 caracteres")
    private String direccionFiscal;

    public String getNombreCompleto() {
        return (nombre == null ? "" : nombre) + " " + (apellido == null ? "" : apellido);
    }

    public String getTipoPersoneriaNombre() {
        return tipoPersoneria == null ? "" : tipoPersoneria.getNombre();
    }

    @Override
    public String toString() {
        return dni + " · " + getNombreCompleto();
    }
}
