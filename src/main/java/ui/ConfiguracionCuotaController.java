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

public class ConfiguracionCuotaController implements Initializable {

    @FXML
    private TextField txtCuotaActual;

    @FXML
    private TextField txtNuevaCuota;

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

            int nuevaCuota = Integer.parseInt(txtNuevaCuota.getText());

            CuotaDAO dao = new CuotaDAO();

            boolean actualizado = dao.actualizarCuota(nuevaCuota);

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
}