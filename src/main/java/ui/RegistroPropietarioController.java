/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ui;

import java.io.IOException;
import java.net.URL;
import java.util.List;
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
import logic.CasaDAO;
import logic.PropietarioDAO;
import model.Propietario;

/**
 * FXML Controller class
 *
 * @author carlo
 */
public class RegistroPropietarioController implements Initializable {

    /**
     * Initializes the controller class.
     */
    @FXML private TextField txtNombre;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtCorreo;
    @FXML private ComboBox<String> cmbCasas;
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cargarCasasDesdeDB();
    }    
    private void cargarCasasDesdeDB(){
        CasaDAO dao = new CasaDAO();
        List<Integer> disponibles = dao.obtenerCasasDisponibles();

        // Limpiamos el ComboBox por si tiene datos viejos
        cmbCasas.getItems().clear();

        // Llenamos el ComboBox con los resultados de la DB
        for (Integer numero : disponibles) {
            cmbCasas.getItems().add("Casa " + numero);
        }
    }
    
    //  El método del botón Registrar
    @FXML
    private void registrarPropietario() {
        // 1. Capturar datos
        String nombre = txtNombre.getText();
        String tel = txtTelefono.getText();
        String mail = txtCorreo.getText();
        String casaSeleccionada = cmbCasas.getValue(); // Trae "Casa X"

        // 2. Validar que no haya vacíos
        if (nombre.isEmpty() || tel.isEmpty() || casaSeleccionada == null) {
            // Aquí puedes usar tu método mostrarAlerta que hicimos antes
            System.out.println("Faltan datos");
            return;
        }

        // 3. Extraer solo el número de la casa (ej: "Casa 5" -> 5)
        int numCasa = Integer.parseInt(casaSeleccionada.replace("Casa ", ""));

        // 4. Ejecutar el registro
        Propietario nuevo = new Propietario(nombre, numCasa, tel, mail);
        PropietarioDAO dao = new PropietarioDAO();

        if (dao.registrar(nuevo)) {
            System.out.println("¡Propietario registrado exitosamente!");
            limpiarCampos();
            cargarCasasDesdeDB(); // Recargamos el ComboBox para que la casa ya no aparezca
        } else {
            System.out.println("Error al registrar.");
        }
    }

    private void limpiarCampos() {
        txtNombre.clear();
        txtTelefono.clear();
        txtCorreo.clear();
        cmbCasas.getSelectionModel().clearSelection();
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
