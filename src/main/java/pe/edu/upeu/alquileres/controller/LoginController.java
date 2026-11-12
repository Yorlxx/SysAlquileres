package pe.edu.upeu.alquileres.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import pe.edu.upeu.alquileres.App;
import pe.edu.upeu.alquileres.exception.BusinessException;
import pe.edu.upeu.alquileres.model.Usuario;
import pe.edu.upeu.alquileres.service.IUsuarioService;
import pe.edu.upeu.alquileres.util.Sesion;

import java.util.Optional;

public class LoginController {

    private final IUsuarioService service;

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtClave;
    @FXML private Label lblError;

    public LoginController(IUsuarioService service) {
        this.service = service;
    }

    @FXML
    public void initialize() {
        lblError.setText("");
        lblError.getStyleClass().removeAll("error", "exito");
        Platform.runLater(txtUsuario::requestFocus);
    }

    @FXML
    private void onIngresar() {
        lblError.setText("");
        try {
            Optional<Usuario> usuario = service.login(txtUsuario.getText(), txtClave.getText());
            if (usuario.isEmpty()) {
                lblError.setText("Usuario o clave incorrectos.");
                txtClave.clear();
                txtClave.requestFocus();
                return;
            }
            Sesion.iniciar(usuario.get());
            App.mostrarPrincipal();
        } catch (BusinessException e) {
            lblError.setText(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            lblError.setText("No se pudo conectar con la base de datos.");
        }
    }

    @FXML
    private void onSalir() {
        Platform.exit();
    }
}
