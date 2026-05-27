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
import logic.SweetAlert; //  IMPORTACIÓN CLAVE

/**
 * FXML Controller class
 *
 * @author eluzai
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
        String casaSeleccionada = cmbCasas.getValue(); 

        //Validar Nombre del propietario//
        if (nombre.isEmpty() || tel.isEmpty() || casaSeleccionada == null) {
            SweetAlert.showWarning("Campos Incompletos", "Por favor, llena todos los campos obligatorios para registrar al Propietario.");
            return;
        }
        
        // Validar nombre (solo letras y espacios)//
        if (!nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")) {
             SweetAlert.showWarning("Nombre inválido","El nombre solo puede contener letras.");
            return;
        }
 
        // Validar que el Numero de telefono corresponda a Guatemala//
        if (!tel.matches("^\\d{8}$")) {
            SweetAlert.showWarning("Teléfono inválido","El teléfono debe contener exactamente 8 dígitos.");
            return;
        }
          
        // Validar correo
        if (!mail.matches("^[A-Za-z0-9._%+-]+@(gmail\\.com|hotmail\\.com|outlook\\.com|.+\\.edu)$")) {
        SweetAlert.showWarning("Su Correo es inválido","Ingresa un correo válido para su Respistro.");
           return;
        }
        // Extraer solo el número de la casa 
        int numCasa = Integer.parseInt(casaSeleccionada.replace("Casa ", ""));

        // 4. Ejecutar el registro
        Propietario nuevo = new Propietario(nombre, numCasa, tel, mail);
        PropietarioDAO dao = new PropietarioDAO();

        if (dao.registrar(nuevo)) {
            SweetAlert.showSuccess("¡Registro Exitoso!", "El propietario " + nombre + " ha sido asignado correctamente a la Casa " + numCasa + ".");
            limpiarCampos();
            cargarCasasDesdeDB(); // Recargamos el ComboBox para que la casa ya no aparezca
        } else {
            SweetAlert.showError("Error de Registro", "Hubo un problema de conexión. No se pudo guardar el propietario.");
        }
    }

    private void limpiarCampos() {
        txtNombre.clear();
        txtTelefono.clear();
        txtCorreo.clear();
        cmbCasas.getSelectionModel().clearSelection();
    }
}