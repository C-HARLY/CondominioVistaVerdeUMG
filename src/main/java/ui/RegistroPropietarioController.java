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

public class RegistroPropietarioController
        implements Initializable {

    // =====================================================
    // COMPONENTES
    // =====================================================

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtTelefono;

    @FXML
    private TextField txtCorreo;

    @FXML
    private ComboBox<String> cmbCasas;

    @FXML
    private Button btnRegistrar;

    // =====================================================
    // VARIABLES MODO EDICIÓN
    // =====================================================

    private boolean modoEdicion = false;

    private int numeroCasaEdicion = 0;

    // =====================================================
    // INITIALIZE
    // =====================================================

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        cargarCasasDesdeDB();
    }

    // =====================================================
    // CARGAR CASAS DISPONIBLES
    // =====================================================

    private void cargarCasasDesdeDB() {

        CasaDAO dao = new CasaDAO();

        List<Integer> disponibles =
                dao.obtenerCasasDisponibles();

        cmbCasas.getItems().clear();

        for (Integer numero : disponibles) {

            cmbCasas.getItems()
                    .add("Casa " + numero);
        }
    }

    // =====================================================
    // CARGAR DATOS PARA EDICIÓN
    // =====================================================

    public void cargarDatosParaEdicion(
            Propietario p
    ) {

        this.modoEdicion = true;

        this.numeroCasaEdicion =
                p.getNumeroCasa();

        // NOMBRE

        txtNombre.setText(p.getNombre());

        // TELÉFONO

        txtTelefono.setText(
                p.getTelefono()
        );

        // CORREO

        txtCorreo.setText(
                p.getCorreo()
        );

        // BLOQUEAR NOMBRE

        txtNombre.setDisable(true);

        // BLOQUEAR CASA

        cmbCasas.getItems().clear();

        cmbCasas.getItems()
                .add("Casa " + numeroCasaEdicion);

        cmbCasas.setValue(
                "Casa " + numeroCasaEdicion
        );

        cmbCasas.setDisable(true);

        // CAMBIAR TEXTO BOTÓN

        if (btnRegistrar != null) {

            btnRegistrar.setText(
                    "Actualizar Datos"
            );
        }
    }

    // =====================================================
    // REGISTRAR / ACTUALIZAR
    // =====================================================

    @FXML
    private void registrarPropietario() {

        // ==============================================
        // CAPTURAR DATOS
        // ==============================================

        String nombre =
                txtNombre.getText().trim();

        String tel =
                txtTelefono.getText().trim();

        String mail =
                txtCorreo.getText().trim();

        String casaSeleccionada =
                cmbCasas.getValue();

        // ==============================================
        // VALIDAR VACÍOS
        // ==============================================

        if (nombre.isEmpty()
                || tel.isEmpty()
                || mail.isEmpty()
                || casaSeleccionada == null) {

            SweetAlert.showWarning(
                    "Campos incompletos",
                    "Por favor completa todos los campos."
            );

            return;
        }

        // ==============================================
        // VALIDAR NOMBRE
        // ==============================================

        if (!nombre.matches(
                "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$"
        )) {

            SweetAlert.showWarning(
                    "Nombre inválido",
                    "El nombre solo puede contener letras."
            );

            return;
        }

        // ==============================================
        // VALIDAR TELÉFONO
        // ==============================================

        if (!tel.matches("^\\d{8}$")) {

            SweetAlert.showWarning(
                    "Teléfono inválido",
                    "El teléfono debe contener exactamente 8 dígitos."
            );

            return;
        }

        // ==============================================
        // VALIDAR CORREO
        // ==============================================

        if (!mail.matches(
                "^[A-Za-z0-9._%+-]+@(gmail\\.com|hotmail\\.com|outlook\\.com|.+\\.edu)$"
        )) {

            SweetAlert.showWarning(
                    "Correo inválido",
                    "Ingresa un correo válido."
            );

            return;
        }

        PropietarioDAO dao =
                new PropietarioDAO();

        // =====================================================
        // MODO EDICIÓN
        // =====================================================

        if (modoEdicion) {

            boolean actualizado =
                    dao.actualizarContacto(
                            numeroCasaEdicion,
                            tel,
                            mail
                    );

            if (actualizado) {

                SweetAlert.showSuccess(
                        "Actualización exitosa",
                        "Los datos fueron actualizados correctamente."
                );

                Stage stage =
                        (Stage) txtNombre
                                .getScene()
                                .getWindow();

                stage.close();

            } else {

                SweetAlert.showError(
                        "Error",
                        "No se pudieron actualizar los datos."
                );
            }

        } else {

            // =====================================================
            // REGISTRO NORMAL
            // =====================================================

            int numCasa = Integer.parseInt(
                    casaSeleccionada.replace("Casa ", "")
            );

            Propietario nuevo =
                    new Propietario(
                            nombre,
                            numCasa,
                            tel,
                            mail
                    );

            boolean registrado =
                    dao.registrar(nuevo);

            if (registrado) {

                SweetAlert.showSuccess(
                        "Registro exitoso",
                        "El propietario fue asignado correctamente."
                );

                limpiarCampos();

                cargarCasasDesdeDB();

            } else {

                SweetAlert.showWarning(
                        "Casa ocupada",
                        "La casa seleccionada ya tiene un propietario activo."
                );
            }
        }
    }

    // =====================================================
    // LIMPIAR CAMPOS
    // =====================================================

    private void limpiarCampos() {

        txtNombre.clear();

        txtTelefono.clear();

        txtCorreo.clear();

        cmbCasas.getSelectionModel()
                .clearSelection();
    }
}