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

        CuotaDAO dao = new CuotaDAO();

        Cuota cuota = dao.obtenerCuota();

        if (cuota != null) {

            txtCuotaActual.setText(
                "Q" + String.format("%,.0f", cuota.getMontoActual())
            );

            txtCuotaActual.setEditable(false);
        }

        // Fecha actual por defecto
        dpFecha.setValue(LocalDate.now());
    }

    @FXML
    private void guardarCuota(ActionEvent event) {

        try {

            LocalDate fechaSeleccionada = dpFecha.getValue();

            if (fechaSeleccionada == null ||
                !fechaSeleccionada.equals(LocalDate.now())) {

                Alert alert = new Alert(Alert.AlertType.WARNING);

                alert.setTitle("Fecha inválida");
                alert.setHeaderText(null);
                alert.setContentText(
                    "La fecha no es válida. Debe seleccionar la fecha actual."
                );

                alert.showAndWait();

                return;
            }

            // Limpiamos Q y comas por si el usuario las escribe
            String texto = txtNuevaCuota.getText()
                    .replace("Q", "")
                    .replace(",", "")
                    .trim();

            int nuevaCuota = Integer.parseInt(texto);

            CuotaDAO dao = new CuotaDAO();

            boolean actualizado =
                    dao.actualizarMontoMantenimiento(nuevaCuota);

            if (actualizado) {

                Alert alert = new Alert(Alert.AlertType.INFORMATION);

                alert.setTitle("Actualización exitosa");
                alert.setHeaderText(null);
                alert.setContentText(
                    "Cuota actualizada correctamente"
                );

                alert.showAndWait();

                txtCuotaActual.setText(
                    "Q" + String.format("%,.0f", nuevaCuota)
                );

                txtNuevaCuota.setText(
                    "Q" + String.format("%,.0f", nuevaCuota)
                );

            } else {

                Alert alert = new Alert(Alert.AlertType.ERROR);

                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText(
                    "No se pudo actualizar"
                );

                alert.showAndWait();
            }

        } catch (Exception e) {

            Alert alert = new Alert(Alert.AlertType.ERROR);

            alert.setTitle("Dato inválido");
            alert.setHeaderText(null);
            alert.setContentText(
                "Ingrese un número válido"
            );

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