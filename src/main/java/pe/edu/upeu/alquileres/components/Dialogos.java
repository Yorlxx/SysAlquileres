package pe.edu.upeu.alquileres.components;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextInputDialog;

import java.util.Optional;

/** Diálogos con el estilo de la aplicación. */
public final class Dialogos {

    private Dialogos() {
    }

    public static boolean confirmar(String titulo, String mensaje) {
        ButtonType aceptar = new ButtonType("Sí, continuar", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.getButtonTypes().setAll(aceptar, cancelar);
        aplicarEstilo(alert.getDialogPane());
        return alert.showAndWait().filter(b -> b == aceptar).isPresent();
    }

    public static Optional<String> pedirTexto(String titulo, String cabecera, String valorInicial) {
        TextInputDialog dialog = new TextInputDialog(valorInicial);
        dialog.setTitle(titulo);
        dialog.setHeaderText(cabecera);
        dialog.setContentText("Motivo:");
        aplicarEstilo(dialog.getDialogPane());
        return dialog.showAndWait();
    }

    public static void informar(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        aplicarEstilo(alert.getDialogPane());
        alert.showAndWait();
    }

    private static void aplicarEstilo(DialogPane pane) {
        try {
            var css = Dialogos.class.getResource("/css/app.css");
            if (css != null) pane.getStylesheets().add(css.toExternalForm());
            pane.getStyleClass().add("app-dialog");
        } catch (Exception ignored) {
        }
    }
}
