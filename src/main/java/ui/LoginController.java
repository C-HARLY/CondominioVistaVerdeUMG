/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ui;


import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {


    /**
     * Initializes the controller class.
     */
    @FXML
    private Button btnLogin;
    
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }
    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Label lblMensaje;

    private final String USER = "iusr_vistaverde";
    private final String PASS = "R3sidencial2026%";

    @FXML
    private void handleLogin() {

        String usuario = txtUsuario.getText();
        String password = txtPassword.getText();

        if (usuario.equals(USER) && password.equals(PASS)) {
            lblMensaje.setStyle("-fx-text-fill: green;");
            lblMensaje.setText("Acceso correcto");
        } else {
            lblMensaje.setStyle("-fx-text-fill: red;");
            lblMensaje.setText("Usuario o contraseña incorrectos");
        }
    }    
    
    //METODO PARA MOSTRAR OTRA PANTALLA
    @FXML // IMPORTANTE: Agrega @FXML para que Scene Builder vea el método
    public void login() {
        try {
            // 1. Cargamos el archivo FXML de la pantalla de pagos
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/Pago.fxml"));
            Parent root = loader.load();

            // 2. Creamos una nueva "Escena" con ese archivo
            Scene scene = new Scene(root);

            // 3. Obtenemos la "Ventana" (Stage) actual usando el botón que recibió el clic
            Stage stage = (Stage) btnLogin.getScene().getWindow();

            // 4. Cambiamos la escena de la ventana actual por la de pagos
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            // Si el nombre del archivo está mal, te avisará aquí
            System.err.println("Error al cargar la vista de pagos: " + e.getMessage());
        }
    }
}