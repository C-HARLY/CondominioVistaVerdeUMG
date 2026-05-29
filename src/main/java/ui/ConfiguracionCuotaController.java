/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ui;

import java.net.URL;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

import logic.CuotaDAO;
import model.Cuota;

public class ConfiguracionCuotaController implements Initializable {

    @FXML
    private TextField txtCuotaActual;

    @FXML
    private TextField txtNuevaCuota;

    @FXML
    private DatePicker dpFecha;

   @Override
public void initialize(URL url, ResourceBundle rb) {
    // Configuración de fecha: Bloqueada y automática
    dpFecha.setValue(LocalDate.now());
    dpFecha.setEditable(false);
    dpFecha.setDisable(true); // El usuario no la toca

    cargarDatosCuota(); // Método privado para reutilizar
}

private void cargarDatosCuota() {
    CuotaDAO dao = new CuotaDAO();
    Cuota cuota = dao.obtenerCuota();
    if (cuota != null) {
        txtCuotaActual.setText("Q" + String.format("%,.0f", cuota.getMontoActual()));
        txtCuotaActual.setEditable(false);
    }
}

@FXML
private void guardarCuota(ActionEvent event) {
    try {
        // 1. Limpieza de datos
        String texto = txtNuevaCuota.getText().replaceAll("[^0-9]", "");
        if (texto.isEmpty()) throw new Exception("Campo vacío");
        
        int nuevaCuota = Integer.parseInt(texto);

        // 2. Operación DAO
        CuotaDAO dao = new CuotaDAO();
        if (dao.actualizarMontoMantenimiento(nuevaCuota)) {
            
            // 3. Éxito: Solo una alerta
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Actualización exitosa");
            alert.setHeaderText(null);
            alert.setContentText("Cuota actualizada correctamente.");
            alert.showAndWait();

            // 4. EL REFRESCO REAL: Pedimos la cuota de nuevo a la base de datos
            // Esto garantiza que si la BD insertó el dato, el DAO lo traerá con el ORDER BY DESC
            Cuota cuotaRecargada = dao.obtenerCuota();
            if (cuotaRecargada != null) {
                txtCuotaActual.setText("Q" + String.format("%,.0f", cuotaRecargada.getMontoActual()));
            }
            
            txtNuevaCuota.clear();

        } else {
            // Solo entra aquí si el INSERT falló (sin excepciones extra)
            throw new Exception("Error al insertar en la base de datos.");
        }

    } catch (Exception e) {
        // Todas las validaciones y errores terminan aquí en una sola alerta
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(e.getMessage()); // Muestra el mensaje del error específico
        alert.showAndWait();
    }
}
    @FXML
    private void cancelarCuota(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText(null);
        alert.setContentText(
            "¿Está seguro que desea cancelar la actualización de la cuota?"
        );
        Optional<ButtonType> resultado = alert.showAndWait();
        if (resultado.isPresent() &&
            resultado.get() == ButtonType.OK) {
            CuotaDAO dao = new CuotaDAO();
            Cuota cuota = dao.obtenerCuota();
            if (cuota != null) {

                txtCuotaActual.setText(
                    "Q" + String.format("%,.0f", cuota.getMontoActual())
                );
            }
            txtNuevaCuota.clear();

            dpFecha.setValue(LocalDate.now());
        }
    }
}