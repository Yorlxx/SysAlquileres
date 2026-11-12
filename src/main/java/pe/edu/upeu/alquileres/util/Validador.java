package pe.edu.upeu.alquileres.util;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import pe.edu.upeu.alquileres.exception.BusinessException;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

/** Validación centralizada con Jakarta Bean Validation. */
public final class Validador {

    private static final Validator VALIDATOR;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        VALIDATOR = factory.getValidator();
    }

    private Validador() {
    }

    public static <T> void validar(T objeto) {
        Set<ConstraintViolation<T>> violaciones = VALIDATOR.validate(objeto);
        if (!violaciones.isEmpty()) {
            String mensaje = violaciones.stream()
                    .sorted(Comparator.comparing(v -> v.getPropertyPath().toString()))
                    .map(ConstraintViolation::getMessage)
                    .distinct()
                    .collect(Collectors.joining("\n"));
            throw new BusinessException(mensaje);
        }
    }

    public static String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
