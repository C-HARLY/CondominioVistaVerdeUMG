package ui;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import logic.CuotaDAO;
import model.Cuota;

public class ConfiguracionCuotaController implements Initializable {

    @FXML private TextField txtCuotaActual;
    @FXML private TextField txtNuevaCuota;
    @FXML private DatePicker dpFecha;
    @FXML private TableView<Cuota> tablaHistorial;
    @FXML private TableColumn<Cuota, Double> colMonto;
    @FXML private TableColumn<Cuota, String> colFecha;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private int ultimoIdInsertado = -1;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dpFecha.setValue(LocalDate.now());

        colMonto.setCellValueFactory(new PropertyValueFactory<>("montoActual"));
        colFecha.setCellValueFactory(cellData -> {
            if (cellData.getValue().getFechaCambio() != null) {
                return new SimpleStringProperty(
                    cellData.getValue().getFechaCambio().format(formatter)
                );
            }
            return new SimpleStringProperty("");
        });

        cargarDatos();
    }

    private void cargarDatos() {
        CuotaDAO dao = new CuotaDAO();
        Cuota cuota = dao.obtenerCuota();
        if (cuota != null) {
            txtCuotaActual.setText(String.valueOf(cuota.getMontoActual()));
            txtCuotaActual.setEditable(false);
        }
        List<Cuota> historial = dao.obtenerHistorial();
        ObservableList<Cuota> lista = FXCollections.observableArrayList(historial);
        tablaHistorial.setItems(lista);
    }

    @FXML
    private void guardarCuota(ActionEvent event) {
        try {
            LocalDate fechaSeleccionada = dpFecha.getValue();
            if (fechaSeleccionada == null || !fechaSeleccionada.equals(LocalDate.now())) {
                new Alert(Alert.AlertType.WARNING,
                        "La fecha no es válida. Debe seleccionar la fecha actual.")
                        .showAndWait();
                return;
            }
            double nuevaCuota = Double.parseDouble(txtNuevaCuota.getText());
            CuotaDAO dao = new CuotaDAO();
            boolean actualizado = dao.actualizarMontoMantenimiento(nuevaCuota);
            if (actualizado) {
                ultimoIdInsertado = dao.obtenerUltimoId();
                new Alert(Alert.AlertType.INFORMATION, "Cuota actualizada correctamente")
                        .showAndWait();
                txtNuevaCuota.clear();
                dpFecha.setValue(LocalDate.now());
                cargarDatos();
            } else {
                new Alert(Alert.AlertType.ERROR, "No se pudo actualizar").showAndWait();
            }
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Ingrese un número válido").showAndWait();
        }
    }

    @FXML
    private void cancelarCuota(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText(null);
        alert.setContentText("¿Está seguro que desea cancelar la última actualización?");
        Optional<ButtonType> resultado = alert.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            if (ultimoIdInsertado != -1) {
                CuotaDAO dao = new CuotaDAO();
                dao.eliminarCuota(ultimoIdInsertado);
                ultimoIdInsertado = -1;
                new Alert(Alert.AlertType.INFORMATION, "Cambio cancelado correctamente")
                        .showAndWait();
            }
            txtNuevaCuota.clear();
            dpFecha.setValue(LocalDate.now());
            cargarDatos();
        }
    }
}