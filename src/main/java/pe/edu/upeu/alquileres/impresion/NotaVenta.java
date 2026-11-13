package pe.edu.upeu.alquileres.impresion;

import pe.edu.upeu.alquileres.model.Contrato;
import pe.edu.upeu.alquileres.model.Inquilino;
import pe.edu.upeu.alquileres.model.Pago;
import pe.edu.upeu.alquileres.util.Formato;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public final class NotaVenta {


    public static final int ANCHO = 32;

    private NotaVenta() {
    }


    public static List<String> lineas(Pago pago) {
        BigDecimal monto = valor(pago.getMonto());
        BigDecimal descuento = valor(pago.getDescuento());
        BigDecimal igv = valor(pago.getIgv());
        BigDecimal total = monto.subtract(descuento).add(igv);

        List<String> lineas = new ArrayList<>();
        lineas.add(centrar(DatosEmpresa.NOMBRE));
        lineas.add(centrar("RUC " + DatosEmpresa.RUC));
        lineas.add(centrar(DatosEmpresa.DIRECCION));
        lineas.add(centrar(DatosEmpresa.TELEFONO));
        lineas.add(separador());

        lineas.add(centrar("NOTA DE VENTA"));
        lineas.add(centrar(numero(pago)));
        lineas.add("Emitido: " + Formato.fechaHora(LocalDateTime.now()));
        lineas.add(separador());

        lineas.add("Cliente: " + recortar(nombreCliente(pago), ANCHO - 9));
        lineas.add("DNI: " + dniCliente(pago));
        lineas.add("Depto.: " + numeroDepartamento(pago));
        lineas.add("Contrato: #" + numeroContrato(pago));
        lineas.add("Periodo: " + pago.getPeriodoTexto());
        lineas.add("Fecha pago: " + pago.getFechaTexto());
        lineas.add(separador());

        lineas.add(fila("Alquiler", Formato.dinero(monto)));
        lineas.add(fila("Descuento", "-" + Formato.dinero(descuento)));
        lineas.add(fila("IGV", Formato.dinero(igv)));
        lineas.add(separador('-'));
        lineas.add(fila("TOTAL", Formato.dinero(total)));
        lineas.add(separador());

        lineas.add("Metodo: " + pago.getMetodoTexto());
        lineas.add(centrar("Gracias por su preferencia"));
        lineas.add(centrar("Documento no fiscal"));
        lineas.add(separador());
        return lineas;
    }


    public static BigDecimal total(Pago pago) {
        return valor(pago.getMonto())
                .subtract(valor(pago.getDescuento()))
                .add(valor(pago.getIgv()));
    }


    public static String numero(Pago pago) {
        long id = pago.getIdPago() == null ? 0 : pago.getIdPago();
        return String.format("NV-%06d", id);
    }

    // ------------------------------------------------------------------
    // Ayudas para que todas las líneas midan exactamente 32 caracteres
    // ------------------------------------------------------------------

    private static String centrar(String texto) {
        String limpio = recortar(texto, ANCHO);
        int espacios = ANCHO - limpio.length();
        int izquierda = espacios / 2;
        return " ".repeat(izquierda) + limpio + " ".repeat(espacios - izquierda);
    }

    private static String fila(String izquierda, String derecha) {
        String der = recortar(derecha, ANCHO - 1);
        String izq = recortar(izquierda, ANCHO - der.length() - 1);
        int espacios = ANCHO - izq.length() - der.length();
        return izq + " ".repeat(Math.max(1, espacios)) + der;
    }

    private static String separador() {
        return separador('-');
    }

    private static String separador(char caracter) {
        return String.valueOf(caracter).repeat(ANCHO);
    }

    private static String recortar(String texto, int maximo) {
        String limpio = texto == null ? "" : texto.trim();
        return limpio.length() <= maximo ? limpio : limpio.substring(0, Math.max(0, maximo));
    }

    private static BigDecimal valor(BigDecimal numero) {
        return numero == null ? BigDecimal.ZERO : numero;
    }

    private static String nombreCliente(Pago pago) {
        Contrato contrato = pago.getContrato();
        Inquilino inquilino = contrato == null ? null : contrato.getInquilino();
        return inquilino == null ? "Cliente varios" : inquilino.getNombreCompleto().trim();
    }

    private static String dniCliente(Pago pago) {
        Contrato contrato = pago.getContrato();
        Inquilino inquilino = contrato == null ? null : contrato.getInquilino();
        return inquilino == null || inquilino.getDni() == null ? "-" : inquilino.getDni();
    }

    private static String numeroDepartamento(Pago pago) {
        return pago.getDepartamentoNumero() == null || pago.getDepartamentoNumero().isBlank()
                ? "-" : pago.getDepartamentoNumero();
    }

    private static String numeroContrato(Pago pago) {
        return pago.getContrato() == null || pago.getContrato().getIdContrato() == null
                ? "-" : String.valueOf(pago.getContrato().getIdContrato());
    }
}
