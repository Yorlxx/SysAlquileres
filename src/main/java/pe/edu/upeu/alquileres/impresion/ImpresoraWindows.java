package pe.edu.upeu.alquileres.impresion;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.List;


public final class ImpresoraWindows {

    private ImpresoraWindows() {
    }


    public static PrintService[] listar() {
        return PrintServiceLookup.lookupPrintServices(null, null);
    }


    public static PrintService predeterminada() {
        return PrintServiceLookup.lookupDefaultPrintService();
    }


    public static void imprimir(PrintService impresora, List<String> lineas) throws PrinterException {
        if (impresora == null) {
            throw new PrinterException("No se seleccionó ninguna impresora de Windows.");
        }

        PrinterJob trabajo = PrinterJob.getPrinterJob();
        trabajo.setPrintService(impresora);
        trabajo.setJobName("Nota de venta 58 mm");
        trabajo.setPrintable(new TicketPrintable(lineas), crearPagina(lineas.size()));
        trabajo.print();
    }

    private static PageFormat crearPagina(int cantidadLineas) {
        double mm = 72.0 / 25.4;                 // 1 mm = 2.8346 puntos
        double ancho = 58 * mm;                  // papel térmico de 58 mm
        double margen = 2.5 * mm;
        double altoLinea = 3.7 * mm;
        double alto = Math.max(40 * mm, cantidadLineas * altoLinea + 10 * mm);

        Paper papel = new Paper();
        papel.setSize(ancho, alto);
        papel.setImageableArea(margen, margen, ancho - margen * 2, alto - margen * 2);

        PageFormat formato = new PageFormat();
        formato.setPaper(papel);
        formato.setOrientation(PageFormat.PORTRAIT);
        return formato;
    }


    private static final class TicketPrintable implements Printable {

        private final List<String> lineas;

        private TicketPrintable(List<String> lineas) {
            this.lineas = lineas;
        }

        @Override
        public int print(Graphics grafico, PageFormat formato, int pagina) {
            if (pagina > 0) {
                return Printable.NO_SUCH_PAGE;
            }

            Graphics2D g2 = (Graphics2D) grafico;
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(Color.BLACK);

            float anchoDisponible = (float) formato.getImageableWidth();
            Font fuente = fuenteQueCabe(g2, anchoDisponible);
            g2.setFont(fuente);

            FontMetrics medidas = g2.getFontMetrics();
            float x = (float) formato.getImageableX();
            float y = (float) formato.getImageableY() + medidas.getAscent();
            float altoLinea = Math.max(medidas.getHeight(), (float) (3.7 * 72.0 / 25.4));

            for (String linea : lineas) {
                g2.drawString(linea, x, y);
                y += altoLinea;
            }
            return Printable.PAGE_EXISTS;
        }

        /** Busca el tamaño de letra más grande con el que caben 32 caracteres. */
        private Font fuenteQueCabe(Graphics2D g2, float ancho) {
            String prueba = "M".repeat(NotaVenta.ANCHO);
            for (int tamano = 9; tamano >= 5; tamano--) {
                Font fuente = new Font(Font.MONOSPACED, Font.PLAIN, tamano);
                if (g2.getFontMetrics(fuente).stringWidth(prueba) <= ancho) {
                    return fuente;
                }
            }
            return new Font(Font.MONOSPACED, Font.PLAIN, 5);
        }
    }
}
