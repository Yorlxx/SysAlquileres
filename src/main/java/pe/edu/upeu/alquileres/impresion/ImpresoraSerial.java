package pe.edu.upeu.alquileres.impresion;

import com.fazecast.jSerialComm.SerialPort;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;



public final class ImpresoraSerial {

    private ImpresoraSerial() {
    }


    public static List<String> listarPuertos() {
        return Arrays.stream(SerialPort.getCommPorts())
                .map(SerialPort::getSystemPortName)
                .sorted()
                .toList();
    }


    public static void imprimir(String nombrePuerto, int baudios, List<String> lineas) {
        if (nombrePuerto == null || nombrePuerto.isBlank()) {
            throw new IllegalStateException("No se seleccionó ningún puerto COM.");
        }

        SerialPort puerto = SerialPort.getCommPort(nombrePuerto);
        puerto.setComPortParameters(baudios, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        puerto.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);

        if (!puerto.openPort()) {
            throw new IllegalStateException("No se pudo abrir el puerto " + nombrePuerto
                    + ". Verifica que la impresora esté encendida y emparejada.");
        }

        try (OutputStream salida = puerto.getOutputStream()) {
            salida.write(comandos(lineas));
            salida.flush();
        } catch (IOException e) {
            throw new IllegalStateException("Error al enviar los datos a la impresora: " + e.getMessage(), e);
        } finally {
            puerto.closePort();
        }
    }


    private static byte[] comandos(List<String> lineas) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();

        salida.writeBytes(new byte[]{0x1B, 0x40});          // ESC @ (inicializar)
        salida.writeBytes(textoAscii(lineas));
        salida.writeBytes(new byte[]{0x1B, 0x64, 0x04});    // ESC d 4 (avanzar 4 líneas)

        return salida.toByteArray();
    }


    private static byte[] textoAscii(List<String> lineas) {
        String texto = String.join("\n", lineas) + "\n";

        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        String limpio = sinTildes
                .replace('–', '-')
                .replace('—', '-')
                .replace('“', '"')
                .replace('”', '"')
                .replaceAll("[^\\x20-\\x7E\\n]", "?");

        return limpio.getBytes(StandardCharsets.US_ASCII);
    }
}
