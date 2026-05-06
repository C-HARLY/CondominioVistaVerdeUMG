/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

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
    
}
