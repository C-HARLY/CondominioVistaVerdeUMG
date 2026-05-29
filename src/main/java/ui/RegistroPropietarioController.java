package ui;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import logic.CasaDAO;
import logic.PropietarioDAO;
import logic.SweetAlert;
import model.Propietario;

public class RegistroPropietarioController implements Initializable {

    // =====================================================
    // COMPONENTES
    // =====================================================
    @FXML private TextField txtNombre;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtCorreo;
    @FXML private ComboBox<String> cmbCasas;
    @FXML private Button btnRegistrar;

    private boolean modoEdicion = false;
    private int numeroCasaEdicion = 0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cargarCasasDesdeDB();
    }

    private void cargarCasasDesdeDB() {
        CasaDAO dao = new CasaDAO();
        List<Integer> disponibles = dao.obtenerCasasDisponibles();
        cmbCasas.getItems().clear();
        for (Integer numero : disponibles) {
            cmbCasas.getItems().add("Casa " + numero);
        }
    }

    public void cargarDatosParaEdicion(Propietario p) {
        this.modoEdicion = true;
        this.numeroCasaEdicion = p.getNumeroCasa();
        txtNombre.setText(p.getNombre());
        txtTelefono.setText(p.getTelefono());
        txtCorreo.setText(p.getCorreo());
        
        txtNombre.setDisable(true);
        cmbCasas.getItems().clear();
        cmbCasas.getItems().add("Casa " + numeroCasaEdicion);
        cmbCasas.setValue("Casa " + numeroCasaEdicion);
        cmbCasas.setDisable(true);

        if (btnRegistrar != null) {
            btnRegistrar.setText("Actualizar Datos");
        }
    }

    @FXML
    private void registrarPropietario() {
        // 1. CAPTURAR DATOS
        String nombre = txtNombre.getText().trim();
        String tel = txtTelefono.getText().trim();
        String mail = txtCorreo.getText().trim();
        String casaSeleccionada = cmbCasas.getValue();

        // 2. VALIDAR VACÍOS
        if (nombre.isEmpty() || tel.isEmpty() || mail.isEmpty() || casaSeleccionada == null) {
            SweetAlert.showWarning("Campos incompletos", "Por favor completa todos los campos.");
            return;
        }

        // 3. VALIDACIONES MEDIANTE REGEX
        // Nombre: Solo letras y espacios
        if (!nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]{3,}$")) {
            SweetAlert.showWarning("Nombre inválido", "El nombre debe contener solo letras y ser mayor a 3 caracteres.");
            return;
        }

        // Teléfono: Exactamente 8 dígitos (Guatemala)
        if (!tel.matches("^[2-7]\\d{7}$")) {
            SweetAlert.showWarning("Teléfono inválido", "El número debe tener 8 dígitos y ser un número guatemalteco válido.");
            return;
        }

       // Correo: Permite cualquier estructura de correo estándar, incluyendo .edu.gt, .edu, etc.
        if (!mail.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[a-z]{2,}$")) {
            SweetAlert.showWarning("Correo inválido", "Por favor, ingresa una dirección de correo electrónico con formato válido.");
            return;
        }

        // 4. LÓGICA DE Registro
        PropietarioDAO dao = new PropietarioDAO();

        if (modoEdicion) {
            if (dao.actualizarContacto(numeroCasaEdicion, tel, mail)) {
                SweetAlert.showSuccess("Actualización exitosa", "Los datos fueron actualizados correctamente.");
                cerrarVentana();
            } else {
                SweetAlert.showError("Error", "No se pudieron actualizar los datos.");
            }
        } else {
            int numCasa = Integer.parseInt(casaSeleccionada.replace("Casa ", ""));
            Propietario nuevo = new Propietario(nombre, numCasa, tel, mail);

            if (dao.registrar(nuevo)) {
                SweetAlert.showSuccess("Registro exitoso", "El propietario fue asignado correctamente.");
                limpiarCampos();
                cargarCasasDesdeDB();
            } else {
                SweetAlert.showWarning("Casa ocupada", "La casa seleccionada ya tiene un propietario activo.");
            }
        }
    }

    private void limpiarCampos() {
        txtNombre.clear();
        txtTelefono.clear();
        txtCorreo.clear();
        cmbCasas.getSelectionModel().clearSelection();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) txtNombre.getScene().getWindow();
        stage.close();
    }
}