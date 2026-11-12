package pe.edu.upeu.alquileres.util;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class Formato {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter MES = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.of("es", "PE"));

    private Formato() {
    }

    public static String dinero(BigDecimal valor) {
        return "S/ " + String.format(Locale.US, "%,.2f", valor == null ? BigDecimal.ZERO : valor);
    }

    public static String fecha(LocalDate fecha) {
        return fecha == null ? "" : FECHA.format(fecha);
    }

    public static String fechaHora(java.time.LocalDateTime fecha) {
        return fecha == null ? "" : FECHA_HORA.format(fecha);
    }

    public static String mes(LocalDate fecha) {
        return fecha == null ? "" : MES.format(fecha);
    }

    public static BigDecimal decimal(String texto) {
        if (texto == null || texto.isBlank()) return BigDecimal.ZERO;
        String limpio = texto.replace("S/", "").replace(",", "").trim();
        try {
            return new BigDecimal(limpio);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    public static String normalizar(String texto) {
        if (texto == null) return "";
        String sinAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sinAcentos.toLowerCase(Locale.ROOT).trim();
    }
}
