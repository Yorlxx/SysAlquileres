package pe.edu.upeu.alquileres.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import pe.edu.upeu.alquileres.components.Dialogos;
import pe.edu.upeu.alquileres.enums.EstadoContrato;
import pe.edu.upeu.alquileres.exception.BusinessException;
import pe.edu.upeu.alquileres.model.Contrato;
import pe.edu.upeu.alquileres.model.Departamento;
import pe.edu.upeu.alquileres.model.Inquilino;
import pe.edu.upeu.alquileres.service.IContratoService;
import pe.edu.upeu.alquileres.service.IDepartamentoService;
import pe.edu.upeu.alquileres.service.IInquilinoService;
import pe.edu.upeu.alquileres.util.Formato;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ContratoController {

    private final IContratoService service;
    private final IInquilinoService inquilinoService;
    private final IDepartamentoService departamentoService;

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cbxFiltroEstado;
    @FXML private Label lblConteo;
    @FXML private TableView<Contrato> tblContratos;
    @FXML private TableColumn<Contrato, Long> colId;
    @FXML private TableColumn<Contrato, String> colInquilino;
    @FXML private TableColumn<Contrato, String> colDepartamento;
    @FXML private TableColumn<Contrato, LocalDate> colInicio;
    @FXML private TableColumn<Contrato, LocalDate> colFin;
    @FXML private TableColumn<Contrato, BigDecimal> colMonto;
    @FXML private TableColumn<Contrato, EstadoContrato> colEstado;

    @FXML private Label lblFormTitulo;
    @FXML private ComboBox<Inquilino> cbxInquilino;
    @FXML private ComboBox<Departamento> cbxDepartamento;
    @FXML private DatePicker dpInicio;
    @FXML private DatePicker dpFin;
    @FXML private TextField txtMonto;
    @FXML private TextField txtCantidad;
    @FXML private Label lblInfoDepto;
    @FXML private Label lblMensaje;
    @FXML private Button btnGuardar;

    private final ObservableList<Contrato> datos = FXCollections.observableArrayList();
    private FilteredList<Contrato> filtrados;
    private Long idEditando;
    private Contrato contratoEditando;

    public ContratoController(IContratoService service,
                              IInquilinoService inquilinoService,
                              IDepartamentoService departamentoService) {
        this.service = service;
        this.inquilinoService = inquilinoService;
        this.departamentoService = departamentoService;
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idContrato"));
        colInquilino.setCellValueFactory(new PropertyValueFactory<>("inquilinoNombre"));
        colDepartamento.setCellValueFactory(new PropertyValueFactory<>("departamentoNumero"));
        colInicio.setCellValueFactory(new PropertyValueFactory<>("fechaInicio"));
        colInicio.setCellFactory(c -> celdaFecha());
        colFin.setCellValueFactory(new PropertyValueFactory<>("fechaFin"));
        colFin.setCellFactory(c -> celdaFecha());
        colMonto.setCellValueFactory(new PropertyValueFactory<>("montoMensual"));
        colMonto.setCellFactory(c -> celdaDinero());
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        filtrados = new FilteredList<>(datos, c -> true);
        tblContratos.setItems(filtrados);
        tblContratos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        cbxFiltroEstado.getItems().setAll("Todos", "Vigente", "Finalizado");
        cbxFiltroEstado.getSelectionModel().selectFirst();
        txtBuscar.textProperty().addListener((o, a, b) -> aplicarFiltro());
        cbxFiltroEstado.valueProperty().addListener((o, a, b) -> aplicarFiltro());
        cbxDepartamento.valueProperty().addListener((o, a, b) -> actualizarInfoDepartamento());
        tblContratos.getSelectionModel().selectedItemProperty()
                .addListener((o, a, b) -> {
                    if (b != null) cargarFormulario(b);
                });

        cargar();
        limpiarFormulario();
    }

    private TableCell<Contrato, LocalDate> celdaFecha() {
        return new TableCell<>() {
            @Override
            protected void updateItem(LocalDate fecha, boolean vacio) {
                super.updateItem(fecha, vacio);
                setText(vacio || fecha == null ? null : Formato.fecha(fecha));
            }
        };
    }

    private TableCell<Contrato, BigDecimal> celdaDinero() {
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
        filtrados.setPredicate(c -> {
            boolean coincideTexto = texto.isEmpty()
                    || Formato.normalizar(c.getInquilinoNombre()).contains(texto)
                    || Formato.normalizar(c.getInquilino().getDni()).contains(texto)
                    || Formato.normalizar(c.getDepartamentoNumero()).contains(texto);
            boolean coincideEstado = estado == null || estado.startsWith("Todos")
                    || c.getEstado().getDescripcion().equalsIgnoreCase(estado);
            return coincideTexto && coincideEstado;
        });
        lblConteo.setText(filtrados.size() + " registros");
    }

    private void cargarCombos() {
        cbxInquilino.getItems().setAll(inquilinoService.findAll());
        List<Departamento> disponibles = new ArrayList<>(departamentoService.findDisponibles());
        if (contratoEditando != null) {
            boolean existe = disponibles.stream()
                    .anyMatch(d -> d.getIdDepartamento().equals(contratoEditando.getDepartamento().getIdDepartamento()));
            if (!existe) disponibles.add(contratoEditando.getDepartamento());
        }
        disponibles.sort((a, b) -> a.getNumero().compareToIgnoreCase(b.getNumero()));
        cbxDepartamento.getItems().setAll(disponibles);
    }

    private void cargarFormulario(Contrato contrato) {
        idEditando = contrato.getIdContrato();
        contratoEditando = contrato;
        cargarCombos();

        cbxInquilino.getItems().stream()
                .filter(i -> i.getIdInquilino().equals(contrato.getInquilino().getIdInquilino()))
                .findFirst()
                .ifPresent(i -> cbxInquilino.getSelectionModel().select(i));
        cbxDepartamento.getItems().stream()
                .filter(d -> d.getIdDepartamento().equals(contrato.getDepartamento().getIdDepartamento()))
                .findFirst()
                .ifPresent(d -> cbxDepartamento.getSelectionModel().select(d));

        dpInicio.setValue(contrato.getFechaInicio());
        dpFin.setValue(contrato.getFechaFin());
        txtMonto.setText(contrato.getMontoMensual() == null ? "" : contrato.getMontoMensual().toPlainString());
        txtCantidad.setText(String.valueOf(contrato.getCantidadPersona()));

        boolean finalizado = contrato.getEstado() == EstadoContrato.FINALIZADO;
        btnGuardar.setDisable(finalizado);
        lblFormTitulo.setText(finalizado ? "Contrato finalizado #" + contrato.getIdContrato()
                : "Editar contrato #" + contrato.getIdContrato());
        mensaje("", false);
    }

    @FXML
    private void onNuevo() {
        limpiarFormulario();
        idEditando = null;
        contratoEditando = null;
        cargarCombos();
        dpInicio.setValue(LocalDate.now());
        dpFin.setValue(LocalDate.now().plusYears(1).minusDays(1));
        txtCantidad.setText("1");
        lblInfoDepto.setText("Selecciona un departamento para ver su precio.");
        mensaje("", false);
    }

    @FXML
    private void onGuardar() {
        Contrato contrato = Contrato.builder()
                .fechaInicio(dpInicio.getValue())
                .fechaFin(dpFin.getValue())
                .montoMensual(Formato.decimal(txtMonto.getText()))
                .estado(EstadoContrato.VIGENTE)
                .inquilino(cbxInquilino.getValue())
                .departamento(cbxDepartamento.getValue())
                .cantidadPersona(parseEntero(txtCantidad.getText()))
                .build();
        try {
            if (idEditando == null) {
                service.save(contrato);
                mensaje("Contrato registrado correctamente.", false);
            } else {
                service.update(idEditando, contrato);
                mensaje("Contrato actualizado correctamente.", false);
            }
            cargar();
            limpiarFormulario();
        } catch (BusinessException e) {
            mensaje(e.getMessage(), true);
        } catch (Exception e) {
            e.printStackTrace();
            mensaje("No se pudo guardar el contrato.", true);
        }
    }

    @FXML
    private void onCancelar() {
        limpiarFormulario();
        mensaje("", false);
    }

    @FXML
    private void onFinalizar() {
        Contrato seleccionado = tblContratos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mensaje("Selecciona un contrato de la tabla.", true);
            return;
        }
        if (seleccionado.getEstado() == EstadoContrato.FINALIZADO) {
            mensaje("El contrato ya está finalizado.", true);
            return;
        }
        Dialogos.pedirTexto("Finalizar contrato",
                        "Contrato #" + seleccionado.getIdContrato() + " · " + seleccionado.getInquilinoNombre(),
                        "Fin de plazo")
                .ifPresent(motivo -> {
                    try {
                        service.finalizar(seleccionado.getIdContrato(), motivo);
                        mensaje("Contrato finalizado. El departamento quedó disponible.", false);
                        cargar();
                        limpiarCampos();
                    } catch (BusinessException e) {
                        mensaje(e.getMessage(), true);
                    } catch (Exception e) {
                        e.printStackTrace();
                        mensaje("No se pudo finalizar el contrato.", true);
                    }
                });
    }

    @FXML
    private void onEliminar() {
        Contrato seleccionado = tblContratos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mensaje("Selecciona un contrato de la tabla.", true);
            return;
        }
        boolean confirmado = Dialogos.confirmar("Eliminar contrato",
                "¿Seguro que deseas eliminar el contrato #" + seleccionado.getIdContrato() + "?");
        if (!confirmado) return;

        try {
            service.delete(seleccionado.getIdContrato());
            mensaje("Contrato eliminado.", false);
            cargar();
            limpiarFormulario();
        } catch (BusinessException e) {
            mensaje(e.getMessage(), true);
        } catch (Exception e) {
            e.printStackTrace();
            mensaje("No se pudo eliminar el contrato.", true);
        }
    }

    private void actualizarInfoDepartamento() {
        Departamento departamento = cbxDepartamento.getValue();
        if (departamento == null) {
            lblInfoDepto.setText("Selecciona un departamento para ver su precio.");
            return;
        }
        lblInfoDepto.setText("Precio de referencia: " + Formato.dinero(departamento.getPrecioMensual()));
        if (idEditando == null) {
            txtMonto.setText(departamento.getPrecioMensual() == null ? "" : departamento.getPrecioMensual().toPlainString());
        }
    }

    private void limpiarFormulario() {
        idEditando = null;
        contratoEditando = null;
        tblContratos.getSelectionModel().clearSelection();
        limpiarCampos();
        lblFormTitulo.setText("Nuevo contrato");
        btnGuardar.setDisable(false);
        lblInfoDepto.setText("Selecciona un departamento para ver su precio.");
    }

    private void limpiarCampos() {
        cbxInquilino.getSelectionModel().clearSelection();
        cbxDepartamento.getSelectionModel().clearSelection();
        dpInicio.setValue(null);
        dpFin.setValue(null);
        txtMonto.clear();
        txtCantidad.clear();
        btnGuardar.setDisable(false);
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
