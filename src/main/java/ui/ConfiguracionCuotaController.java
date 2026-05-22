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

}