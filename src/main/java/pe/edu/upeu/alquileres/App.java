package pe.edu.upeu.alquileres;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pe.edu.upeu.alquileres.config.AppContext;
import pe.edu.upeu.alquileres.config.DatabaseConfig;


public class App {

    private static Stage ventana;


    public static class Ventana extends Application {

        @Override
        public void start(Stage stage) {
            ventana = stage;
            AppContext.getInstance();
            mostrarLogin();
        }

        @Override
        public void stop() {
            DatabaseConfig.shutdown();
        }
    }

    public static void main(String[] args) {
        Application.launch(Ventana.class, args);
    }

    public static void mostrarLogin() {
        cargar("/view/login.fxml", "SysAlquileres · Iniciar sesión", 430, 430, false);
    }

    public static void mostrarPrincipal() {
        cargar("/view/main.fxml", "SysAlquileres · Gestión de Departamentos", 1100, 700, true);
    }

    private static void cargar(String vista, String titulo, double ancho, double alto, boolean redimensionable) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(vista));
            loader.setControllerFactory(AppContext.getInstance()::getBean);
            Parent root = loader.load();

            Scene scene = new Scene(root, ancho, alto);
            scene.getStylesheets().add(App.class.getResource("/css/app.css").toExternalForm());

            ventana.setTitle(titulo);
            ventana.setScene(scene);
            ventana.setResizable(redimensionable);
            ventana.setMinWidth(redimensionable ? 900 : 0);
            ventana.setMinHeight(redimensionable ? 600 : 0);
            ventana.centerOnScreen();
            ventana.show();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo abrir la vista " + vista, e);
        }
    }
}
