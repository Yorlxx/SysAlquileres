package pe.edu.upeu.alquileres.exception;

/** Error de regla de negocio que se muestra amigablemente al usuario. */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
