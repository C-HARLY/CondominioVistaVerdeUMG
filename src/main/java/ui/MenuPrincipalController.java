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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.scene.control.Button;



/**
 * FXML Controller class
 *
 * @author carlo
 */
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


    
    
    
    /**
     * Initializes the controller class.
     */
    
    
    // ------------CERRAR SESION
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
    
    //----METODO DEL CALENDARIO EN EL MENU
    @Override
   public void initialize(URL url, ResourceBundle rb) {

    cargarVista("/ui/Dashboard.fxml");
}   
    
   
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
   
   
   private void activarBoton(Button botonActivo) {

    btnPropietarios.getStyleClass().remove("menu-button-active");
    btnPagos.getStyleClass().remove("menu-button-active");
    btnEstadoCuenta.getStyleClass().remove("menu-button-active");
    btnReporte.getStyleClass().remove("menu-button-active");
    btnMorosos.getStyleClass().remove("menu-button-active");

    botonActivo.getStyleClass().add("menu-button-active");
}
    
    // -------ABRIR VENTANA PAGOS
     @FXML
    private void abrirPagos(ActionEvent event) {

    activarBoton(btnPagos);

    cargarVista("/ui/Pago.fxml");
}
    
        // -------ABRIR VENTANA PAGOS
     @FXML
    private void abrirPropietario(ActionEvent event) {

    activarBoton(btnPropietarios);

    cargarVista("/ui/RegistroPropietario.fxml");
}
    
    
    // -------ABRIR VENTANA ESTADO DE CUENTAS
     @FXML
    private void abrirEstadoCuenta(ActionEvent event) {

    activarBoton(btnEstadoCuenta);

    cargarVista("/ui/EstadoCuenta.fxml");
}
    
    
     // -------ABRIR VENTANA REPORTE GENERAL
     @FXML
    private void abrirReporteGeneral(ActionEvent event) {

    activarBoton(btnReporte);

    cargarVista("/ui/ReporteGeneral.fxml");
}
    //abrir ventana configuracion cuota
    
    @FXML
    private void abrirConfiguracionCuota(ActionEvent event) {

    activarBoton(btnEstadoCuenta);

    cargarVista("/ui/ConfiguracionCuota.fxml");
}
    
}
