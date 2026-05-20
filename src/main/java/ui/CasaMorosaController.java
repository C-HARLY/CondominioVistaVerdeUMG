/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;

import java.net.URL;
import java.util.ResourceBundle;

public class CasaMorosaController implements Initializable {

    @FXML
    private ComboBox<String> comboAnios;
    
    @FXML
private ComboBox<String> comboMeses;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        comboAnios.getItems().addAll(
                "2025",
                "2026",
                "2027",
                "2028"
        );
        comboAnios.setValue("2026");
        comboMeses.getItems().addAll(
                "Enero",
                "Febrero",
                "Marzo",
                "Abril",
                "Mayo",
                "Junio",
                "Julio",
                "Agosto",
                "Septiembre",
                "Octubre",
                "Noviembre",
                "Diciembre"
        );

        comboMeses.setValue("Enero");
        
    }
}
