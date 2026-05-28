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

    // =========================================================
    // COMPONENTES
    // =========================================================

    @FXML
    private AnchorPane contentArea;

    @FXML
    private Label lblFecha;

    // BOTONES SIDEBAR

    @FXML
    private Button btnPropietarios;

    @FXML
    private Button btnGestionPropietarios;

    @FXML
    private Button btnPagos;

    @FXML
    private Button btnConfiguracion;

    @FXML
    private Button btnEstadoCuenta;

    @FXML
    private Button btnReporte;

    @FXML
    private Button btnMorosos;

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

            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource(rutaFXML));

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
    // ACTIVAR BOTÓN SIDEBAR
    // =========================================================

    private void activarBoton(Button botonActivo) {

        btnPropietarios.getStyleClass()
                .remove("menu-button-active");

        btnGestionPropietarios.getStyleClass()
                .remove("menu-button-active");

        btnPagos.getStyleClass()
                .remove("menu-button-active");

        btnConfiguracion.getStyleClass()
                .remove("menu-button-active");

        btnEstadoCuenta.getStyleClass()
                .remove("menu-button-active");

        btnReporte.getStyleClass()
                .remove("menu-button-active");

        btnMorosos.getStyleClass()
                .remove("menu-button-active");

        botonActivo.getStyleClass()
                .add("menu-button-active");
    }

    // =========================================================
    // REGISTRO PROPIETARIO
    // =========================================================

    @FXML
    private void abrirPropietario(ActionEvent event) {

        activarBoton(btnPropietarios);

        cargarVista("/ui/RegistroPropietario.fxml");
    }

    // =========================================================
    // GESTIÓN PROPIETARIOS
    // =========================================================

    @FXML
    private void abrirGestionPropietarios(ActionEvent event) {

        activarBoton(btnGestionPropietarios);

        cargarVista("/ui/GestionPropietarios.fxml");
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
    // CONFIGURACIÓN CUOTA
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
    // CERRAR SESIÓN
    // =========================================================

    @FXML
    private void cerrarSesion(MouseEvent event) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource("/ui/login.fxml")
                    );

            Parent root = loader.load();

            Stage stage =
                    (Stage) ((javafx.scene.Node)
                            event.getSource())
                            .getScene()
                            .getWindow();

            Scene scene = new Scene(root);

            stage.setScene(scene);

            stage.setTitle("Login - Sistema Administrativo");

            stage.centerOnScreen();

            stage.show();

            System.out.println("Sesión cerrada correctamente.");

        } catch (IOException e) {

            System.err.println(
                    "Error al regresar al Login: "
                    + e.getMessage()
            );

            e.printStackTrace();
        }
    }
}