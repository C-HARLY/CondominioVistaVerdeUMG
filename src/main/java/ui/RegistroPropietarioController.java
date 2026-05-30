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

/**
 * Controlador de la interfaz de Registro y Edicion de Propietarios.
 * Implementa un patron de formulario de doble proposito (Dual-Purpose Form),
 * permitiendo tanto la creacion de nuevos registros como la modificacion de datos
 * de contacto existentes, gestionando el estado interno mediante banderas booleanas.
 */
public class RegistroPropietarioController implements Initializable {

    // Nodos de la interfaz grafica inyectados por FXML
    @FXML private TextField txtNombre;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtCorreo;
    @FXML private ComboBox<String> cmbCasas;
    @FXML private Button btnRegistrar;

    // Variables de estado para el control de contexto del formulario
    private boolean modoEdicion = false;
    private int numeroCasaEdicion = 0;

    /**
     * Metodo de ciclo de vida de JavaFX.
     * Prepara el entorno inicializando los selectores de datos dinamicos.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cargarCasasDesdeDB();
    }

    /**
     * Consulta la base de datos para obtener unicamente las casas cuyo estado es 'Disponible'.
     * Esta regla de negocio a nivel de interfaz previene errores de concurrencia o
     * asignaciones invalidas al restringir las opciones del usuario desde el inicio.
     */
    private void cargarCasasDesdeDB() {
        CasaDAO dao = new CasaDAO();
        List<Integer> disponibles = dao.obtenerCasasDisponibles();
        
        cmbCasas.getItems().clear();
        for (Integer numero : disponibles) {
            cmbCasas.getItems().add("Casa " + numero);
        }
    }

    /**
     * Transforma el formulario de un estado de "Creacion" a "Edicion".
     * Aplica reglas de inmutabilidad parcial: bloquea la edicion del nombre y 
     * el numero de casa, ya que alterar la identidad del inquilino o moverlo de casa
     * requeriria una transaccion de traslado completo, no una simple actualizacion de contacto.
     * * @param p Objeto Propietario con los datos a cargar en la vista.
     */
    public void cargarDatosParaEdicion(Propietario p) {
        this.modoEdicion = true;
        this.numeroCasaEdicion = p.getNumeroCasa();
        
        txtNombre.setText(p.getNombre());
        txtTelefono.setText(p.getTelefono());
        txtCorreo.setText(p.getCorreo());
        
        // Aplicacion de inmutabilidad visual
        txtNombre.setDisable(true);
        cmbCasas.getItems().clear();
        cmbCasas.getItems().add("Casa " + numeroCasaEdicion);
        cmbCasas.setValue("Casa " + numeroCasaEdicion);
        cmbCasas.setDisable(true);

        if (btnRegistrar != null) {
            btnRegistrar.setText("Actualizar Datos");
        }
    }

    /**
     * Orquesta el flujo de validacion y persistencia de datos.
     * Actua como punto de entrada para el boton principal del formulario, ejecutando
     * sanitizacion de entradas, validacion de expresiones regulares y delegacion al DAO.
     */
    @FXML
    private void registrarPropietario() {
        // 1. Extraccion y limpieza de espacios en blanco (Trim)
        String nombre = txtNombre.getText().trim();
        String tel = txtTelefono.getText().trim();
        String mail = txtCorreo.getText().trim();
        String casaSeleccionada = cmbCasas.getValue();

        // 2. Validacion de completitud de estructura
        if (nombre.isEmpty() || tel.isEmpty() || mail.isEmpty() || casaSeleccionada == null) {
            SweetAlert.showWarning("Campos incompletos", "Es necesario completar todos los campos obligatorios del formulario.");
            return;
        }

        // 3. Sanitizacion y Validacion de Integridad mediante Expresiones Regulares (Regex)
        
        // Verifica que el nombre solo contenga caracteres alfabeticos y espacios, con longitud minima
        if (!nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]{3,}$")) {
            SweetAlert.showWarning("Nombre invalido", "El campo nombre no admite caracteres especiales ni numeros, y debe tener al menos 3 caracteres.");
            return;
        }

        // Validacion estandarizada para numeracion movil/fija en Guatemala (8 digitos, prefijos validos)
        if (!tel.matches("^[2-7]\\d{7}$")) {
            SweetAlert.showWarning("Telefono invalido", "La estructura del numero no coincide con el estandar nacional (8 digitos).");
            return;
        }

       // Validacion de dominios de correo electronico segun el estandar RFC 5322 simplificado
        if (!mail.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[a-z]{2,}$")) {
            SweetAlert.showWarning("Correo invalido", "La direccion ingresada no cumple con la estructura de un correo electronico valido.");
            return;
        }

        // 4. Enrutamiento de Transaccion (Creacion vs. Actualizacion)
        PropietarioDAO dao = new PropietarioDAO();

        if (modoEdicion) {
            // Flujo de Actualizacion (UPDATE)
            if (dao.actualizarContacto(numeroCasaEdicion, tel, mail)) {
                SweetAlert.showSuccess("Actualizacion exitosa", "Los metadatos de contacto fueron sincronizados correctamente.");
                cerrarVentana();
            } else {
                SweetAlert.showError("Error de Persistencia", "Se produjo una anomalia al intentar actualizar los registros en el servidor.");
            }
        } else {
            // Flujo de Insercion (INSERT)
            int numCasa = Integer.parseInt(casaSeleccionada.replace("Casa ", ""));
            Propietario nuevo = new Propietario(nombre, numCasa, tel, mail);

            if (dao.registrar(nuevo)) {
                SweetAlert.showSuccess("Registro exitoso", "El perfil del titular fue aprovisionado y vinculado a la unidad seleccionada.");
                limpiarCampos();
                cargarCasasDesdeDB(); // Se recarga el selector para evitar conflictos en asignaciones subsecuentes
            } else {
                SweetAlert.showWarning("Conflicto de Asignacion", "La unidad seleccionada ya no se encuentra disponible. Refresque el listado e intente nuevamente.");
            }
        }
    }

    /**
     * Restablece el estado de los componentes visuales de entrada de datos,
     * preparando el formulario para una nueva interaccion sin necesidad de recargar la vista.
     */
    private void limpiarCampos() {
        txtNombre.clear();
        txtTelefono.clear();
        txtCorreo.clear();
        cmbCasas.getSelectionModel().clearSelection();
    }

    /**
     * Administra el ciclo de vida de la ventana modal, procediendo a su destruccion
     * una vez que el flujo de trabajo (como una edicion exitosa) ha concluido.
     */
    private void cerrarVentana() {
        Stage stage = (Stage) txtNombre.getScene().getWindow();
        stage.close();
    }
}