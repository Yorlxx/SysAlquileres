package pe.edu.upeu.alquileres.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import pe.edu.upeu.alquileres.App;
import pe.edu.upeu.alquileres.components.Dialogos;
import pe.edu.upeu.alquileres.config.AppContext;
import pe.edu.upeu.alquileres.util.Sesion;

import java.util.Map;

public class MainController {

    @FXML private TabPane tabPane;

    private record Vista(String archivo, String titulo) {
    }

    private static final Map<String, Vista> VISTAS = Map.of(
            "departamentos", new Vista("departamentos.fxml", "Departamentos"),
            "inquilinos", new Vista("inquilinos.fxml", "Inquilinos"),
            "contratos", new Vista("contratos.fxml", "Contratos"),
            "pagos", new Vista("pagos.fxml", "Pagos")
    );

    @FXML
    public void initialize() {
        abrirTab("departamentos");
    }

    @FXML
    private void irDepartamentos() {
        abrirTab("departamentos");
    }

    @FXML
    private void irInquilinos() {
        abrirTab("inquilinos");
    }

    @FXML
    private void irContratos() {
        abrirTab("contratos");
    }

    @FXML
    private void irPagos() {
        abrirTab("pagos");
    }

    private void abrirTab(String clave) {
        for (Tab tab : tabPane.getTabs()) {
            if (clave.equals(tab.getUserData())) {
                tabPane.getSelectionModel().select(tab);
                return;
            }
        }

        Vista vista = VISTAS.get(clave);
        if (vista == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/" + vista.archivo()));
            loader.setControllerFactory(AppContext.getInstance()::getBean);
            Parent root = loader.load();

            Tab tab = new Tab(vista.titulo(), root);
            tab.setUserData(clave);
            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);
        } catch (Exception e) {
            e.printStackTrace();
            Dialogos.informar("No se pudo abrir el módulo",
                    "Ocurrió un problema al cargar la pantalla: " + e.getMessage());
        }
    }

    @FXML
    private void onCerrarSesion() {
        boolean confirmado = Dialogos.confirmar("Cerrar sesión",
                "¿Deseas cerrar la sesión y volver al login?");
        if (!confirmado) return;
        Sesion.cerrar();
        App.mostrarLogin();
    }

    @FXML
    private void onSalir() {
        Platform.exit();
    }

    @FXML
    private void onAcercaDe() {
        Dialogos.informar("Acerca de SysAlquileres",
                "Sistema de gestión de alquileres de departamentos.\n"
                        + "Módulos: Departamentos, Inquilinos, Contratos y Pagos.\n"
                        + "POO 2026 · UPeU Juliaca");
    }
}
