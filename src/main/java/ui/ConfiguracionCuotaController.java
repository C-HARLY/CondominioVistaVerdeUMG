/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ui;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import logic.CuotaDAO;
import model.Cuota;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import java.time.LocalDate;
import javafx.scene.control.ButtonType;
import java.util.Optional;

public class ConfiguracionCuotaController implements Initializable {

    @FXML
    private TextField txtCuotaActual;

    @FXML
    private TextField txtNuevaCuota;
    
    @FXML
private DatePicker dpFecha;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        CuotaDAO dao = new CuotaDAO();

        Cuota cuota = dao.obtenerCuota();

        if (cuota != null) {

            txtCuotaActual.setText(String.valueOf(cuota.getMontoActual()));

            txtCuotaActual.setEditable(false);
            
            
        }
    }
     @FXML
    private void guardarCuota(ActionEvent event) {
        
        try {

            LocalDate fechaSeleccionada = dpFecha.getValue();

if (!fechaSeleccionada.equals(LocalDate.now())) {

    Alert alert = new Alert(Alert.AlertType.WARNING);

    alert.setContentText("La fecha no es válida. Debe seleccionar la fecha actual.");

    alert.showAndWait();

    return;
    
    
}
           
            int nuevaCuota = Integer.parseInt(txtNuevaCuota.getText());

            CuotaDAO dao = new CuotaDAO();

            boolean actualizado = dao.actualizarMontoMantenimiento(nuevaCuota);

            if (actualizado) {

                Alert alert = new Alert(Alert.AlertType.INFORMATION);

                alert.setContentText("Cuota actualizada correctamente");

                alert.showAndWait();

                txtCuotaActual.setText(String.valueOf(nuevaCuota));

                txtNuevaCuota.clear();

            } else {

                Alert alert = new Alert(Alert.AlertType.ERROR);

                alert.setContentText("No se pudo actualizar");

                alert.showAndWait();
            }

        } catch (Exception e) {

            Alert alert = new Alert(Alert.AlertType.ERROR);

            alert.setContentText("Ingrese un número válido");

            alert.showAndWait();
        }
    }
    @FXML
private void cancelarCuota(ActionEvent event) {

    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

    alert.setTitle("Confirmación");

    alert.setHeaderText(null);

    alert.setContentText("¿Está seguro que desea cancelar la actualización de la cuota?");

    Optional<ButtonType> resultado = alert.showAndWait();

    if (resultado.isPresent() && resultado.get() == ButtonType.OK) {

        CuotaDAO dao = new CuotaDAO();

Cuota cuota = dao.obtenerCuota();

if (cuota != null) {

    txtCuotaActual.setText(String.valueOf(cuota.getMontoActual()));
}

txtNuevaCuota.clear();

dpFecha.setValue(null);
    }
}
}