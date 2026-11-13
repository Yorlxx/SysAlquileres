package pe.edu.upeu.alquileres.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import pe.edu.upeu.alquileres.components.Dialogos;
import pe.edu.upeu.alquileres.enums.EstadoDepartamento;
import pe.edu.upeu.alquileres.exception.BusinessException;
import pe.edu.upeu.alquileres.model.Departamento;
import pe.edu.upeu.alquileres.service.IDepartamentoService;
import pe.edu.upeu.alquileres.util.Formato;

import java.math.BigDecimal;

public class DepartamentoController {

    private final IDepartamentoService service;

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cbxFiltroEstado;
    @FXML private Label lblConteo;
    @FXML private TableView<Departamento> tblDepartamentos;
    @FXML private TableColumn<Departamento, String> colNumero;
    @FXML private TableColumn<Departamento, Integer> colPiso;
    @FXML private TableColumn<Departamento, String> colDireccion;
    @FXML private TableColumn<Departamento, BigDecimal> colPrecio;
    @FXML private TableColumn<Departamento, EstadoDepartamento> colEstado;

    @FXML private Label lblFormTitulo;
    @FXML private TextField txtNumero;
    @FXML private TextField txtPiso;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtPrecio;
    @FXML private Label lblMensaje;

    private final ObservableList<Departamento> datos = FXCollections.observableArrayList();//e
    private FilteredList<Departamento> filtrados;
    private Long idEditando;

    public DepartamentoController(IDepartamentoService service) {
        this.service = service;
    }

    @FXML
    public void initialize() {
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colPiso.setCellValueFactory(new PropertyValueFactory<>("piso"));
        colDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioMensual"));
        colPrecio.setCellFactory(c -> celdaDinero());
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        filtrados = new FilteredList<>(datos, d -> true);
        tblDepartamentos.setItems(filtrados);
        tblDepartamentos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        cbxFiltroEstado.getItems().setAll("Todos", "Disponible", "Ocupado");
        cbxFiltroEstado.getSelectionModel().selectFirst();
        txtBuscar.textProperty().addListener((o, a, b) -> aplicarFiltro());
        cbxFiltroEstado.valueProperty().addListener((o, a, b) -> aplicarFiltro());
        tblDepartamentos.getSelectionModel().selectedItemProperty()
                .addListener((o, a, b) -> {
                    if (b != null) cargarFormulario(b);
                });

        cargar();
        limpiarFormulario();
    }

    private TableCell<Departamento, BigDecimal> celdaDinero() {
        return new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal valor, boolean vacio) {
                super.updateItem(valor, vacio);
                setText(vacio || valor == null ? null : Formato.dinero(valor));
            }
        };
    }

    private void cargar() {
        datos.setAll(service.findAll());
        aplicarFiltro();
    }

    private void aplicarFiltro() {
        if (filtrados == null) return;
        String texto = Formato.normalizar(txtBuscar.getText());
        String estado = cbxFiltroEstado.getValue();
        filtrados.setPredicate(d -> {
            boolean coincideTexto = texto.isEmpty()
                    || Formato.normalizar(d.getNumero()).contains(texto)
                    || Formato.normalizar(d.getDireccion()).contains(texto);
            boolean coincideEstado = estado == null || estado.startsWith("Todos")
                    || d.getEstado().getDescripcion().equalsIgnoreCase(estado);
            return coincideTexto && coincideEstado;
        });
        lblConteo.setText(filtrados.size() + " registros");
    }

    private void cargarFormulario(Departamento d) {
        idEditando = d.getIdDepartamento();
        txtNumero.setText(d.getNumero());
        txtPiso.setText(String.valueOf(d.getPiso()));
        txtDireccion.setText(d.getDireccion());
        txtPrecio.setText(d.getPrecioMensual() == null ? "" : d.getPrecioMensual().toPlainString());
        lblFormTitulo.setText("Editar departamento");
        mensaje("", false);
    }

    @FXML
    private void onNuevo() {
        limpiarFormulario();
        mensaje("", false);
    }

    @FXML
    private void onGuardar() {
        Departamento departamento = Departamento.builder()
                .numero(txtNumero.getText())
                .piso(parseEntero(txtPiso.getText()))
                .direccion(txtDireccion.getText())
                .precioMensual(Formato.decimal(txtPrecio.getText()))
                .estado(EstadoDepartamento.DISPONIBLE)
                .build();
        try {
            if (idEditando == null) {
                service.save(departamento);
                mensaje("Departamento registrado correctamente.", false);
            } else {
                service.update(idEditando, departamento);
                mensaje("Departamento actualizado correctamente.", false);
            }
            cargar();
            limpiarFormulario();
        } catch (BusinessException e) {
            mensaje(e.getMessage(), true);
        } catch (Exception e) {
            e.printStackTrace();
            mensaje("No se pudo guardar el departamento.", true);
        }
    }

    @FXML
    private void onCancelar() {
        limpiarFormulario();
        mensaje("", false);
    }

    @FXML
    private void onEliminar() {
        Departamento seleccionado = tblDepartamentos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mensaje("Selecciona un departamento de la tabla.", true);
            return;
        }
        boolean confirmado = Dialogos.confirmar("Eliminar departamento",
                "¿Seguro que deseas eliminar el departamento " + seleccionado.getNumero() + "?");
        if (!confirmado) return;

        try {
            service.delete(seleccionado.getIdDepartamento());
            mensaje("Departamento eliminado.", false);
            cargar();
            limpiarFormulario();
        } catch (BusinessException e) {
            mensaje(e.getMessage(), true);
        } catch (Exception e) {
            e.printStackTrace();
            mensaje("No se pudo eliminar el departamento.", true);
        }
    }

    private void limpiarFormulario() {
        idEditando = null;
        tblDepartamentos.getSelectionModel().clearSelection();
        limpiarCampos();
        lblFormTitulo.setText("Nuevo departamento");
    }

    private void limpiarCampos() {
        txtNumero.clear();
        txtPiso.clear();
        txtDireccion.clear();
        txtPrecio.clear();
    }

    private void mensaje(String texto, boolean error) {
        lblMensaje.setText(texto == null ? "" : texto);
        lblMensaje.getStyleClass().removeAll("error", "exito");
        if (texto != null && !texto.isBlank()) {
            lblMensaje.getStyleClass().add(error ? "error" : "exito");
        }
    }

    private int parseEntero(String valor) {
        try {
            return Integer.parseInt(valor == null ? "" : valor.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
