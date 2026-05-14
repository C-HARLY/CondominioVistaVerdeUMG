/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author carlo
 */
public class MenuPrincipalController implements Initializable {
    @FXML
    private Label lblFecha;
    /**
     * Initializes the controller class.
     */
    
    

    @FXML
    private void cerrarSesion(MouseEvent event) {
        try {
            // 1. Cargamos de vuelta el archivo  Login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml")); 
            Parent root = loader.load();

            // 2. Obtenemos la ventana actual a través del clic del mouse
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

            // 3. Cambiamos la escena de regreso al Login
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Login - Sistema Administrativo");
            stage.centerOnScreen();
            stage.show();

            System.out.println("Sesión cerrada correctamente.");

        } catch (IOException e) {
            System.err.println("Error al regresar al Login: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        LocalDate fechaLocal = LocalDate.now();
    // Formato de fecha
    DateTimeFormatter formatoElegante = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
    String fechaFormateada = fechaLocal.format(formatoElegante);

    // aqui se puede cargar una imagen
    lblFecha.setText("📅 " + fechaFormateada.substring(0, 1).toUpperCase() + fechaFormateada.substring(1));
    }    
    
}
