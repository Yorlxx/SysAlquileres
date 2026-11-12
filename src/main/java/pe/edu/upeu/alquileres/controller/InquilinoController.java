package pe.edu.upeu.alquileres.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import pe.edu.upeu.alquileres.components.Dialogos;
import pe.edu.upeu.alquileres.exception.BusinessException;
import pe.edu.upeu.alquileres.model.Inquilino;
import pe.edu.upeu.alquileres.model.TipoPersoneria;
import pe.edu.upeu.alquileres.service.IInquilinoService;
import pe.edu.upeu.alquileres.util.Formato;

public class InquilinoController {

    private final IInquilinoService service;

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cbxFiltroTipo;
    @FXML private Label lblConteo;
    @FXML private TableView<Inquilino> tblInquilinos;
    @FXML private TableColumn<Inquilino, String> colDni;
    @FXML private TableColumn<Inquilino, String> colNombre;
    @FXML private TableColumn<Inquilino, String> colTelefono;
    @FXML private TableColumn<Inquilino, String> colTipo;
    @FXML private TableColumn<Inquilino, String> colDireccion;

    @FXML private Label lblFormTitulo;
    @FXML private TextField txtDni;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtDireccion;
    @FXML private ComboBox<TipoPersoneria> cbxTipoPersoneria;
    @FXML private VBox boxLegal;
    @FXML private TextField txtRepLegal;
    @FXML private TextField txtRuc;
    @FXML private TextField txtDireccionFiscal;
    @FXML private Label lblMensaje;

    private final ObservableList<Inquilino> datos = FXCollections.observableArrayList();
    private FilteredList<Inquilino> filtrados;
    private Long idEditando;

    public InquilinoController(IInquilinoService service) {
        this.service = service;
    }

    @FXML
    public void initialize() {
        colDni.setCellValueFactory(new PropertyValueFactory<>("dni"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipoPersoneriaNombre"));
        colDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));

        filtrados = new FilteredList<>(datos, i -> true);
        tblInquilinos.setItems(filtrados);
        tblInquilinos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        cbxTipoPersoneria.getItems().setAll(service.listarTiposPersoneria());
        cbxFiltroTipo.getItems().add("Todos");
        service.listarTiposPersoneria().forEach(t -> cbxFiltroTipo.getItems().add(t.getNombre()));
        cbxFiltroTipo.getSelectionModel().selectFirst();

        txtBuscar.textProperty().addListener((o, a, b) -> aplicarFiltro());
        cbxFiltroTipo.valueProperty().addListener((o, a, b) -> aplicarFiltro());
        cbxTipoPersoneria.valueProperty().addListener((o, a, b) -> actualizarCamposLegales());
        tblInquilinos.getSelectionModel().selectedItemProperty()
                .addListener((o, a, b) -> {
                    if (b != null) cargarFormulario(b);
                });

        cargar();
        limpiarFormulario();
    }

    private void cargar() {
        datos.setAll(service.findAll());
        aplicarFiltro();
    }

    private void aplicarFiltro() {
        if (filtrados == null) return;
        String texto = Formato.normalizar(txtBuscar.getText());
        String tipo = cbxFiltroTipo.getValue();
        filtrados.setPredicate(i -> {
            boolean coincideTexto = texto.isEmpty()
                    || Formato.normalizar(i.getDni()).contains(texto)
                    || Formato.normalizar(i.getNombre()).contains(texto)
                    || Formato.normalizar(i.getApellido()).contains(texto);
            boolean coincideTipo = tipo == null || tipo.startsWith("Todos")
                    || (i.getTipoPersoneria() != null && tipo.equalsIgnoreCase(i.getTipoPersoneria().getNombre()));
            return coincideTexto && coincideTipo;
        });
        lblConteo.setText(filtrados.size() + " registros");
    }

    private void cargarFormulario(Inquilino i) {
        idEditando = i.getIdInquilino();
        txtDni.setText(i.getDni());
        txtNombre.setText(i.getNombre());
        txtApellido.setText(i.getApellido());
        txtTelefono.setText(i.getTelefono());
        txtDireccion.setText(i.getDireccion());
        if (i.getTipoPersoneria() != null) {
            cbxTipoPersoneria.getItems().stream()
                    .filter(t -> t.getIdTipoPersoneria().equals(i.getTipoPersoneria().getIdTipoPersoneria()))
                    .findFirst()
                    .ifPresent(t -> cbxTipoPersoneria.getSelectionModel().select(t));
        } else {
            cbxTipoPersoneria.getSelectionModel().clearSelection();
        }
        txtRepLegal.setText(i.getRepLegal());
        txtRuc.setText(i.getRuc());
        txtDireccionFiscal.setText(i.getDireccionFiscal());
        lblFormTitulo.setText("Editar inquilino");
        mensaje("", false);
    }

    @FXML
    private void onNuevo() {
        limpiarFormulario();
        mensaje("", false);
    }

    @FXML
    private void onGuardar() {
        Inquilino inquilino = Inquilino.builder()
                .dni(txtDni.getText())
                .nombre(txtNombre.getText())
                .apellido(txtApellido.getText())
                .telefono(txtTelefono.getText())
                .direccion(txtDireccion.getText())
                .tipoPersoneria(cbxTipoPersoneria.getValue())
                .repLegal(txtRepLegal.getText())
                .ruc(txtRuc.getText())
                .direccionFiscal(txtDireccionFiscal.getText())
                .build();
        try {
            if (idEditando == null) {
                service.save(inquilino);
                mensaje("Inquilino registrado correctamente.", false);
            } else {
                service.update(idEditando, inquilino);
                mensaje("Inquilino actualizado correctamente.", false);
            }
            cargar();
            limpiarFormulario();
        } catch (BusinessException e) {
            mensaje(e.getMessage(), true);
        } catch (Exception e) {
            e.printStackTrace();
            mensaje("No se pudo guardar el inquilino.", true);
        }
    }

    @FXML
    private void onCancelar() {
        limpiarFormulario();
        mensaje("", false);
    }

    @FXML
    private void onEliminar() {
        Inquilino seleccionado = tblInquilinos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mensaje("Selecciona un inquilino de la tabla.", true);
            return;
        }
        boolean confirmado = Dialogos.confirmar("Eliminar inquilino",
                "¿Seguro que deseas eliminar a " + seleccionado.getNombreCompleto() + "?");
        if (!confirmado) return;

        try {
            service.delete(seleccionado.getIdInquilino());
            mensaje("Inquilino eliminado.", false);
            cargar();
            limpiarFormulario();
        } catch (BusinessException e) {
            mensaje(e.getMessage(), true);
        } catch (Exception e) {
            e.printStackTrace();
            mensaje("No se pudo eliminar el inquilino.", true);
        }
    }

    private void actualizarCamposLegales() {
        TipoPersoneria tipo = cbxTipoPersoneria.getValue();
        boolean esJuridica = tipo != null && "Jurídica".equalsIgnoreCase(tipo.getNombre());
        boxLegal.setVisible(esJuridica);
        boxLegal.setManaged(esJuridica);
        if (!esJuridica) {
            txtRepLegal.clear();
            txtRuc.clear();
            txtDireccionFiscal.clear();
        }
    }

    private void limpiarFormulario() {
        idEditando = null;
        tblInquilinos.getSelectionModel().clearSelection();
        limpiarCampos();
        lblFormTitulo.setText("Nuevo inquilino");
    }

    private void limpiarCampos() {
        txtDni.clear();
        txtNombre.clear();
        txtApellido.clear();
        txtTelefono.clear();
        txtDireccion.clear();
        cbxTipoPersoneria.getSelectionModel().clearSelection();
        txtRepLegal.clear();
        txtRuc.clear();
        txtDireccionFiscal.clear();
        boxLegal.setVisible(false);
        boxLegal.setManaged(false);
    }

    private void mensaje(String texto, boolean error) {
        lblMensaje.setText(texto == null ? "" : texto);
        lblMensaje.getStyleClass().removeAll("error", "exito");
        if (texto != null && !texto.isBlank()) {
            lblMensaje.getStyleClass().add(error ? "error" : "exito");
        }
    }
}
