/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ui;
import java.io.IOException;
import model.Configuracion;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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
    
    //----METODO REGRESAR AL MENU PRINCIPAL
    @FXML
    private void volverAlMenu(ActionEvent event) {
        try {
            // 1. Cargar el archivo FXML del Menú Principal
            // Asegúrate de que el nombre del archivo sea exactamente igual a como está en tu proyecto
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/MenuPrincipal.fxml")); 
            Parent root = loader.load();

            // 2. Obtener la ventana (Stage) actual desde el botón que se hizo clic
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // 3. Crear y establecer la nueva escena
            Scene scene = new Scene(root);
            stage.setScene(scene);
            
            // Opcional: Volver a centrar la ventana por si las pantallas tienen distintos tamaños
            stage.centerOnScreen(); 
            stage.show();

        } catch (IOException e) {
            System.err.println("Error al regresar al menú principal: " + e.getMessage());
            e.printStackTrace();
        }
    }
   
}
