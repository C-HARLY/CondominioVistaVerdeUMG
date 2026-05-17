/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ui;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

public class DashboardController implements Initializable {

    @FXML
    private Label lblFecha;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        LocalDate fechaLocal = LocalDate.now();

        DateTimeFormatter formatoElegante =
                DateTimeFormatter.ofPattern(
                        "EEEE, dd 'de' MMMM 'de' yyyy",
                        new Locale("es", "ES")
                );

        String fechaFormateada =
                fechaLocal.format(formatoElegante);

        lblFecha.setText(
                "📅 "
                + fechaFormateada.substring(0, 1).toUpperCase()
                + fechaFormateada.substring(1)
        );
    }
}