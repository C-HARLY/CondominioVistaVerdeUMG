/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;

import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.control.Button;
import javafx.scene.control.Label;

import javafx.scene.input.MouseEvent;

import javafx.scene.layout.AnchorPane;

import javafx.stage.Stage;

public class MenuPrincipalController implements Initializable {

    @FXML
    private AnchorPane contentArea;

    @FXML
    private Label lblFecha;

    @FXML
    private Button btnPropietarios;

    @FXML
    private Button btnPagos;

    @FXML
    private Button btnEstadoCuenta;

    @FXML
    private Button btnReporte;

    @FXML
    private Button btnMorosos;

    @FXML
    private Button btnConfiguracion;

    // =========================================================
    // INITIALIZE
    // =========================================================

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        cargarVista("/ui/Dashboard.fxml");

      
    }

    // =========================================================
    // CARGAR VISTAS
    // =========================================================

    private void cargarVista(String rutaFXML) {

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFXML));

            Parent vista = loader.load();

            contentArea.getChildren().clear();

            contentArea.getChildren().add(vista);

            AnchorPane.setTopAnchor(vista, 0.0);
            AnchorPane.setBottomAnchor(vista, 0.0);
            AnchorPane.setLeftAnchor(vista, 0.0);
            AnchorPane.setRightAnchor(vista, 0.0);

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    // =========================================================
    // ACTIVAR BOTONES SIDEBAR
    // =========================================================

    private void activarBoton(Button botonActivo) {

        btnPropietarios.getStyleClass().remove("menu-button-active");

        btnPagos.getStyleClass().remove("menu-button-active");

        btnConfiguracion.getStyleClass().remove("menu-button-active");

        btnEstadoCuenta.getStyleClass().remove("menu-button-active");

        btnReporte.getStyleClass().remove("menu-button-active");

        btnMorosos.getStyleClass().remove("menu-button-active");

        botonActivo.getStyleClass().add("menu-button-active");
    }

    // =========================================================
    // PROPIETARIOS
    // =========================================================

    @FXML
    private void abrirPropietario(ActionEvent event) {

        activarBoton(btnPropietarios);

        cargarVista("/ui/RegistroPropietario.fxml");
    }

    // =========================================================
    // PAGOS
    // =========================================================

    @FXML
    private void abrirPagos(ActionEvent event) {

        activarBoton(btnPagos);

        cargarVista("/ui/Pago.fxml");
    }

    // =========================================================
    // CONFIGURACION CUOTA
    // =========================================================

    @FXML
    private void abrirConfiguracionCuota(ActionEvent event) {

        activarBoton(btnConfiguracion);

        cargarVista("/ui/ConfiguracionCuota.fxml");
    }

    // =========================================================
    // ESTADO DE CUENTA
    // =========================================================

    @FXML
    private void abrirEstadoCuenta(ActionEvent event) {

        activarBoton(btnEstadoCuenta);

        cargarVista("/ui/EstadoCuenta.fxml");
    }

    // =========================================================
    // REPORTE GENERAL
    // =========================================================

    @FXML
    private void abrirReporteGeneral(ActionEvent event) {

        activarBoton(btnReporte);

        cargarVista("/ui/ReporteGeneral.fxml");
    }

    // =========================================================
    // CASAS MOROSAS
    // =========================================================

    @FXML
    private void abrirCasasMorosas(ActionEvent event) {

        activarBoton(btnMorosos);

        cargarVista("/ui/CasasMorosas.fxml");
    }

    // =========================================================
    // CERRAR SESION
    // =========================================================

    @FXML
    private void cerrarSesion(MouseEvent event) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/Login.fxml")
            );

            Parent root = loader.load();

            Stage stage = (Stage) ((javafx.scene.Node)
                    event.getSource()).getScene().getWindow();

            Scene scene = new Scene(root);

            stage.setScene(scene);

            stage.setTitle("Login - Sistema Administrativo");

            stage.centerOnScreen();

            stage.show();

            System.out.println("Sesión cerrada correctamente.");

        } catch (IOException e) {

            System.err.println(
                    "Error al regresar al Login: " + e.getMessage()
            );

            e.printStackTrace();
        }
    }
}