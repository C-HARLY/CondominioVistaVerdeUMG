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
            
            abrirMenuPrincipal();
        } else {
            lblMensaje.setStyle("-fx-text-fill: red;");
            lblMensaje.setText("Usuario o contraseña incorrectos");
        }
    }    
    
    private void abrirMenuPrincipal() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/MenuPrincipal.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        Stage stage = (Stage) btnLogin.getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Menú Principal");
        stage.show();

    } catch (IOException e) {
       e.printStackTrace();
    }
}

    
}