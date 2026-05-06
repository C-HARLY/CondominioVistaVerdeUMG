/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ui;
import model.Configuracion;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author carlo
 */
public class PagoController implements Initializable {

    /**
     * Initializes the controller class.
     */
    //aqui importamos los componentes de javaFX
    @FXML
    private TextField txtMonto; 
    @FXML
    private ComboBox<String> cbMes; 
    @FXML
    private ComboBox<String> cbYear; 
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1. tomamos el valor desde la clase Configuracion en el paquete model
        double montoActual = Configuracion.cuotaMantenimiento;

        // 2. Lo convertimos a texto y lo ponemos en el TextField
        // Usamos String.format para que siempre muestre dos decimales (ej. 1500.00)
        txtMonto.setText(String.format("%.2f", montoActual));
        
            // 2. Llenar Meses (puedes usar una lista simple)
        cbMes.getItems().addAll("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
                                 "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre");

        // 3. Llenar Años (pon el actual y un par más)
        cbYear.getItems().addAll("2026", "2027", "2028");

        // Opcional: Seleccionar el año actual por defecto
        cbYear.getSelectionModel().selectFirst();
        }    
    
}
