package pe.edu.upeu.alquileres.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import pe.edu.upeu.alquileres.components.Dialogos;
import pe.edu.upeu.alquileres.components.NotaVentaDialog;
import pe.edu.upeu.alquileres.enums.MetodoPago;
import pe.edu.upeu.alquileres.exception.BusinessException;
import pe.edu.upeu.alquileres.model.Contrato;
import pe.edu.upeu.alquileres.model.Pago;
import pe.edu.upeu.alquileres.service.IContratoService;
import pe.edu.upeu.alquileres.service.IPagoService;
import pe.edu.upeu.alquileres.util.Formato;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PagoController {

    private final IPagoService service;
    private final IContratoService contratoService;

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cbxFiltroMetodo;
    @FXML private Label lblConteo;
    @FXML private TableView<Pago> tblPagos;
    @FXML private TableColumn<Pago, Long> colId;
    @FXML private TableColumn<Pago, LocalDate> colFecha;
    @FXML private TableColumn<Pago, LocalDate> colPeriodo;
    @FXML private TableColumn<Pago, String> colInquilino;
    @FXML private TableColumn<Pago, String> colDepartamento;
    @FXML private TableColumn<Pago, BigDecimal> colMonto;
    @FXML private TableColumn<Pago, MetodoPago> colMetodo;

    @FXML private Label lblFormTitulo;
    @FXML private ComboBox<Contrato> cbxContrato;
    @FXML private DatePicker dpFechaPago;
    @FXML private DatePicker dpPeriodo;
    @FXML private TextField txtMonto;
    @FXML private ComboBox<MetodoPago> cbxMetodo;
    @FXML private TextField txtDescuento;
    @FXML private TextField txtIgv;
    @FXML private Label lblInfoContrato;
    @FXML private Label lblMensaje;

    private final ObservableList<Pago> datos = FXCollections.observableArrayList();
    private FilteredList<Pago> filtrados;
    private Long idEditando;

    public PagoController(IPagoService service, IContratoService contratoService) {
        this.service = service;
        this.contratoService = contratoService;
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idPago"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaPago"));
        colFecha.setCellFactory(c -> celdaFecha(true));
        colPeriodo.setCellValueFactory(new PropertyValueFactory<>("periodo"));
        colPeriodo.setCellFactory(c -> celdaFecha(false));
        colInquilino.setCellValueFactory(new PropertyValueFactory<>("inquilinoNombre"));
        colDepartamento.setCellValueFactory(new PropertyValueFactory<>("departamentoNumero"));
        colMonto.setCellValueFactory(new PropertyValueFactory<>("monto"));
        colMonto.setCellFactory(c -> celdaDinero());
        colMetodo.setCellValueFactory(new PropertyValueFactory<>("metodo"));

        filtrados = new FilteredList<>(datos, p -> true);
        tblPagos.setItems(filtrados);
        tblPagos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        cbxFiltroMetodo.getItems().add("Todos");
        for (MetodoPago metodo : MetodoPago.values()) {
            cbxFiltroMetodo.getItems().add(metodo.getDescripcion());
        }
        cbxFiltroMetodo.getSelectionModel().selectFirst();
        cbxMetodo.getItems().setAll(MetodoPago.values());

        txtBuscar.textProperty().addListener((o, a, b) -> aplicarFiltro());
        cbxFiltroMetodo.valueProperty().addListener((o, a, b) -> aplicarFiltro());
        cbxContrato.valueProperty().addListener((o, a, b) -> actualizarInfoContrato());
        tblPagos.getSelectionModel().selectedItemProperty()
                .addListener((o, a, b) -> {
                    if (b != null) cargarFormulario(b);
                });

        cargar();
        limpiarFormulario();
    }

    private TableCell<Pago, LocalDate> celdaFecha(boolean fechaCompleta) {
        return new TableCell<>() {
            @Override
            protected void updateItem(LocalDate fecha, boolean vacio) {
                super.updateItem(fecha, vacio);
                if (vacio || fecha == null) {
                    setText(null);
                } else {
                    setText(fechaCompleta ? Formato.fecha(fecha) : Formato.mes(fecha));
                }
            }
        };
    }

    private TableCell<Pago, BigDecimal> celdaDinero() {
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
        String metodo = cbxFiltroMetodo.getValue();
        filtrados.setPredicate(p -> {
            boolean coincideTexto = texto.isEmpty()
                    || Formato.normalizar(p.getInquilinoNombre()).contains(texto)
                    || Formato.normalizar(p.getDepartamentoNumero()).contains(texto)
                    || Formato.normalizar(p.getPeriodoTexto()).contains(texto)
                    || Formato.normalizar(p.getMetodoTexto()).contains(texto);
            boolean coincideMetodo = metodo == null || metodo.startsWith("Todos")
                    || (p.getMetodo() != null && p.getMetodo().getDescripcion().equalsIgnoreCase(metodo));
            return coincideTexto && coincideMetodo;
        });
        lblConteo.setText(filtrados.size() + " registros");
    }

    private void cargarCombos() {
        cbxContrato.getItems().setAll(contratoService.findAll());
    }

    private void cargarFormulario(Pago pago) {
        idEditando = pago.getIdPago();
        cargarCombos();
        cbxContrato.getItems().stream()
                .filter(c -> c.getIdContrato().equals(pago.getContrato().getIdContrato()))
                .findFirst()
                .ifPresent(c -> cbxContrato.getSelectionModel().select(c));
        dpFechaPago.setValue(pago.getFechaPago());
        dpPeriodo.setValue(pago.getPeriodo());
        txtMonto.setText(pago.getMonto() == null ? "" : pago.getMonto().toPlainString());
        cbxMetodo.getSelectionModel().select(pago.getMetodo());
        txtDescuento.setText(pago.getDescuento().toPlainString());
        txtIgv.setText(pago.getIgv().toPlainString());
        lblFormTitulo.setText("Editar pago #" + pago.getIdPago());
        mensaje("", false);
    }

    @FXML
    private void onNuevo() {
        limpiarFormulario();
        cargarCombos();
        dpFechaPago.setValue(LocalDate.now());
        dpPeriodo.setValue(LocalDate.now().withDayOfMonth(1));
        txtDescuento.setText("0.00");
        txtIgv.setText("0.00");
        cbxMetodo.getSelectionModel().select(MetodoPago.EFECTIVO);
        lblInfoContrato.setText("Selecciona un contrato para ver el monto.");
        mensaje("", false);
    }

    @FXML
    private void onGuardar() {
        Pago pago = Pago.builder()
                .fechaPago(dpFechaPago.getValue())
                .periodo(dpPeriodo.getValue())
                .monto(Formato.decimal(txtMonto.getText()))
                .metodo(cbxMetodo.getValue())
                .contrato(cbxContrato.getValue())
                .descuento(Formato.decimal(txtDescuento.getText()))
                .igv(Formato.decimal(txtIgv.getText()))
                .build();
        try {
            if (idEditando == null) {
                service.save(pago);
                mensaje("Pago registrado correctamente.", false);
            } else {
                service.update(idEditando, pago);
                mensaje("Pago actualizado correctamente.", false);
            }
            cargar();
            limpiarFormulario();
        } catch (BusinessException e) {
            mensaje(e.getMessage(), true);
        } catch (Exception e) {
            e.printStackTrace();
            mensaje("No se pudo guardar el pago.", true);
        }
    }

    @FXML
    private void onCancelar() {
        limpiarFormulario();
        mensaje("", false);
    }

    @FXML
    private void onEliminar() {
        Pago seleccionado = tblPagos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mensaje("Selecciona un pago de la tabla.", true);
            return;
        }
        boolean confirmado = Dialogos.confirmar("Eliminar pago",
                "¿Seguro que deseas eliminar el pago #" + seleccionado.getIdPago()
                        + " del periodo " + seleccionado.getPeriodoTexto() + "?");
        if (!confirmado) return;

        try {
            service.delete(seleccionado.getIdPago());
            mensaje("Pago eliminado.", false);
            cargar();
            limpiarFormulario();
        } catch (BusinessException e) {
            mensaje(e.getMessage(), true);
        } catch (Exception e) {
            e.printStackTrace();
            mensaje("No se pudo eliminar el pago.", true);
        }
    }

    @FXML
    private void onImprimirNota() {
        Pago seleccionado = tblPagos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mensaje("Selecciona un pago para emitir la nota de venta.", true);
            return;
        }
        NotaVentaDialog.mostrar(
                tblPagos.getScene() == null ? null : tblPagos.getScene().getWindow(),
                seleccionado);
    }

    private void actualizarInfoContrato() {
        Contrato contrato = cbxContrato.getValue();
        if (contrato == null) {
            lblInfoContrato.setText("Selecciona un contrato para ver el monto.");
            return;
        }
        lblInfoContrato.setText(contrato.getInquilinoNombre() + " · Dpto. " + contrato.getDepartamentoNumero()
                + " · " + Formato.dinero(contrato.getMontoMensual()) + " / mes");
        if (idEditando == null) {
            txtMonto.setText(contrato.getMontoMensual() == null ? "" : contrato.getMontoMensual().toPlainString());
        }
    }

    private void limpiarFormulario() {
        idEditando = null;
        tblPagos.getSelectionModel().clearSelection();
        limpiarCampos();
        lblFormTitulo.setText("Registrar pago");
        lblInfoContrato.setText("Selecciona un contrato para ver el monto.");
    }

    private void limpiarCampos() {
        cbxContrato.getSelectionModel().clearSelection();
        dpFechaPago.setValue(null);
        dpPeriodo.setValue(null);
        txtMonto.clear();
        cbxMetodo.getSelectionModel().clearSelection();
        txtDescuento.clear();
        txtIgv.clear();
    }

    private void mensaje(String texto, boolean error) {
        lblMensaje.setText(texto == null ? "" : texto);
        lblMensaje.getStyleClass().removeAll("error", "exito");
        if (texto != null && !texto.isBlank()) {
            lblMensaje.getStyleClass().add(error ? "error" : "exito");
        }
    }
}
