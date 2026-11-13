package pe.edu.upeu.alquileres.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;
import pe.edu.upeu.alquileres.impresion.ImpresoraSerial;
import pe.edu.upeu.alquileres.impresion.ImpresoraWindows;
import pe.edu.upeu.alquileres.impresion.NotaVenta;
import pe.edu.upeu.alquileres.model.Pago;

import javax.print.PrintService;
import java.util.List;

/**
 * Ventana de vista previa e impresión de la nota de venta.
 *
 * Permite dos formas de conexión:
 *  1. Impresora instalada en Windows (USB o Bluetooth emparejado).
 *  2. Puerto COM (impresoras Bluetooth que crean un puerto serial).
 */
public final class NotaVentaDialog {

    private NotaVentaDialog() {
    }

    public static void mostrar(Window duenio, Pago pago) {
        if (pago == null) return;

        List<String> lineas = NotaVenta.lineas(pago);

        TextArea vistaPrevia = new TextArea(String.join("\n", lineas));
        vistaPrevia.setEditable(false);
        vistaPrevia.setWrapText(false);
        vistaPrevia.setPrefColumnCount(34);
        vistaPrevia.setPrefRowCount(24);
        vistaPrevia.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 11px;");
        VBox.setVgrow(vistaPrevia, Priority.ALWAYS);

        // ----- Forma de conexión -----
        ToggleGroup grupo = new ToggleGroup();
        RadioButton rbWindows = new RadioButton("Windows (USB / Bluetooth)");
        RadioButton rbSerial = new RadioButton("Puerto COM (Bluetooth)");
        rbWindows.setToggleGroup(grupo);
        rbSerial.setToggleGroup(grupo);
        rbWindows.setSelected(true);

        // ----- Impresoras de Windows -----
        ComboBox<PrintService> cbxImpresoras = new ComboBox<>();
        cbxImpresoras.getItems().setAll(ImpresoraWindows.listar());
        cbxImpresoras.setMaxWidth(Double.MAX_VALUE);
        cbxImpresoras.setPromptText("No se encontraron impresoras instaladas");
        cbxImpresoras.setConverter(new StringConverter<>() {
            @Override
            public String toString(PrintService servicio) {
                return servicio == null ? "" : servicio.getName();
            }

            @Override
            public PrintService fromString(String texto) {
                return null;
            }
        });
        PrintService predeterminada = ImpresoraWindows.predeterminada();
        if (predeterminada != null) {
            cbxImpresoras.getItems().stream()
                    .filter(p -> p.getName().equals(predeterminada.getName()))
                    .findFirst()
                    .ifPresent(p -> cbxImpresoras.getSelectionModel().select(p));
        }
        if (cbxImpresoras.getValue() == null && !cbxImpresoras.getItems().isEmpty()) {
            cbxImpresoras.getSelectionModel().selectFirst();
        }

        // ----- Puertos COM -----
        ComboBox<String> cbxPuertos = new ComboBox<>();
        cbxPuertos.getItems().setAll(ImpresoraSerial.listarPuertos());
        cbxPuertos.setMaxWidth(Double.MAX_VALUE);
        cbxPuertos.setPromptText("No se encontraron puertos COM");
        if (!cbxPuertos.getItems().isEmpty()) {
            cbxPuertos.getSelectionModel().selectFirst();
        }

        ComboBox<Integer> cbxBaudios = new ComboBox<>();
        cbxBaudios.getItems().setAll(9600, 19200, 38400, 57600, 115200);
        cbxBaudios.getSelectionModel().select(Integer.valueOf(9600));

        Runnable actualizarControles = () -> {
            boolean windows = rbWindows.isSelected();
            cbxImpresoras.setDisable(!windows);
            cbxPuertos.setDisable(windows);
            cbxBaudios.setDisable(windows);
        };
        grupo.selectedToggleProperty().addListener((o, anterior, actual) -> actualizarControles.run());
        actualizarControles.run();

        // ----- Botones y mensajes -----
        Label estado = new Label("Selecciona la conexión y presiona Imprimir.");
        estado.setWrapText(true);

        Stage ventana = new Stage();
        ventana.initModality(Modality.APPLICATION_MODAL);
        if (duenio != null) {
            ventana.initOwner(duenio);
        }
        ventana.setTitle("Nota de venta · 58 mm");

        Button btnImprimir = new Button("Imprimir");
        btnImprimir.getStyleClass().add("boton-primario");
        btnImprimir.setDefaultButton(true);

        Button btnCerrar = new Button("Cerrar");
        btnCerrar.getStyleClass().add("boton-secundario");
        btnCerrar.setOnAction(e -> ventana.close());

        btnImprimir.setOnAction(e -> {
            try {
                if (rbWindows.isSelected()) {
                    PrintService impresora = cbxImpresoras.getValue();
                    if (impresora == null) {
                        throw new IllegalStateException("Selecciona una impresora de Windows.");
                    }
                    ImpresoraWindows.imprimir(impresora, lineas);
                } else {
                    String puerto = cbxPuertos.getValue();
                    if (puerto == null) {
                        throw new IllegalStateException("Selecciona un puerto COM.");
                    }
                    int baudios = cbxBaudios.getValue() == null ? 9600 : cbxBaudios.getValue();
                    ImpresoraSerial.imprimir(puerto, baudios, lineas);
                }
                mostrarEstado(estado, "Ticket enviado a la impresora.", false);
            } catch (Exception ex) {
                mostrarEstado(estado, "No se pudo imprimir: " + ex.getMessage(), true);
            }
        });

        // ----- Distribución de la ventana -----
        Label tituloPrevia = new Label("Vista previa · 58 mm (32 caracteres por línea)");
        tituloPrevia.getStyleClass().add("campo-etiqueta");

        Label tituloConexion = new Label("Forma de conexión");
        tituloConexion.getStyleClass().add("titulo-form");

        VBox campoImpresora = new VBox(3, new Label("Impresora de Windows:"), cbxImpresoras);
        VBox campoPuerto = new VBox(3, new Label("Puerto COM:"), cbxPuertos);
        VBox campoBaudios = new VBox(3, new Label("Velocidad:"), cbxBaudios);

        HBox filaConexion = new HBox(12, campoPuerto, campoBaudios);
        HBox.setHgrow(campoPuerto, Priority.ALWAYS);

        HBox botones = new HBox(10, btnImprimir, btnCerrar);
        botones.setAlignment(Pos.CENTER_RIGHT);

        VBox raiz = new VBox(10,
                tituloPrevia,
                vistaPrevia,
                new Separator(),
                tituloConexion,
                new HBox(16, rbWindows, rbSerial),
                campoImpresora,
                filaConexion,
                estado,
                botones);
        raiz.setPadding(new Insets(14));
        raiz.getStyleClass().add("modulo");

        Scene escena = new Scene(raiz, 500, 680);
        var css = NotaVentaDialog.class.getResource("/css/app.css");
        if (css != null) {
            escena.getStylesheets().add(css.toExternalForm());
        }

        ventana.setScene(escena);
        ventana.setMinWidth(500);
        ventana.setMinHeight(620);
        ventana.showAndWait();
    }

    private static void mostrarEstado(Label etiqueta, String texto, boolean error) {
        etiqueta.setText(texto);
        etiqueta.getStyleClass().removeAll("error", "exito");
        etiqueta.getStyleClass().add(error ? "error" : "exito");
    }
}
