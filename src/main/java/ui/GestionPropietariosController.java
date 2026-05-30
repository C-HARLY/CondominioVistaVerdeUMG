package ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import logic.PropietarioDAO;
import logic.SweetAlert;
import model.Propietario;

/**
 * Controlador de la interfaz de Gestion de Propietarios.
 * Se encarga de administrar el listado principal de inquilinos, permitiendo
 * visualizar sus datos, procesar bajas del sistema y coordinar la apertura 
 * de la vista de edicion mediante paso de parametros.
 */
public class GestionPropietariosController {

    @FXML private TableView<Propietario> tblPropietarios;
    @FXML private TableColumn<Propietario, Integer> colCasa;
    @FXML private TableColumn<Propietario, String> colNombre;
    @FXML private TableColumn<Propietario, String> colTelefono;
    @FXML private TableColumn<Propietario, String> colCorreo;

    private PropietarioDAO dao = new PropietarioDAO();
    private ObservableList<Propietario> listaPropietarios;

    /**
     * Metodo de ciclo de vida de JavaFX. 
     * Configura el enlace de datos (data binding) entre las columnas de la tabla visual 
     * y las propiedades internas del modelo Propietario.
     */
    @FXML
    public void initialize() {
        // Mapeo de atributos de la clase Propietario mediante reflexion
        colCasa.setCellValueFactory(new PropertyValueFactory<>("numeroCasa")); 
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colCorreo.setCellValueFactory(new PropertyValueFactory<>("correo"));
        
        // Formateo visual para mejorar la legibilidad de datos cortos
        colCasa.setStyle("-fx-alignment: CENTER;");
        colTelefono.setStyle("-fx-alignment: CENTER;");

        cargarTabla();
    }

    /**
     * Sincroniza la tabla de la interfaz gráfica con el estado actual de la base de datos.
     * Utiliza una ObservableList para que cualquier actualizacion se refleje de forma automatica.
     */
    private void cargarTabla() {
        listaPropietarios = FXCollections.observableArrayList();
        listaPropietarios.addAll(dao.obtenerTodos());
        tblPropietarios.setItems(listaPropietarios);
    }

    /**
     * Procesa la solicitud de baja de un inquilino.
     * Valida que exista una seleccion activa, invoca la transaccion de borrado en la capa DAO
     * y actualiza el estado de la interfaz de forma reactiva si la operacion fue exitosa.
     * * @param event Evento disparado por el boton de la interfaz.
     */
    @FXML
    private void removerPropietario(ActionEvent event) {
        Propietario seleccionado = tblPropietarios.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            SweetAlert.showWarning("Seleccion requerida", "Por favor, selecciona un propietario de la tabla para proceder con la baja.");
            return;
        }

        // Ejecucion de la regla de negocio: liberar la casa asociada al propietario
        boolean exito = dao.removerPropietario(seleccionado.getNumeroCasa());

        if (exito) {
            SweetAlert.showSuccess("Baja Exitosa", "El propietario ha sido removido del sistema y la casa ha pasado a estado disponible.");
            cargarTabla(); // Refresco reactivo del listado
        } else {
            SweetAlert.showError("Error de Transaccion", "Ocurrio un problema en la base de datos al intentar dar de baja al propietario.");
        }
    }

    /**
     * Orquesta la apertura de la ventana modal de edicion.
     * Carga el archivo FXML correspondiente, inyecta el objeto seleccionado en el controlador
     * de destino y detiene el flujo actual hasta que la sub-ventana sea cerrada, garantizando
     * que la tabla principal se actualice con los nuevos cambios.
     * * @param event Evento disparado por el boton de edicion.
     */
    @FXML
    private void abrirEdicion(ActionEvent event) {
        Propietario seleccionado = tblPropietarios.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            SweetAlert.showWarning("Seleccion requerida", "Por favor, selecciona un propietario del listado para editar sus datos.");
            return;
        }

        try {
            // Carga de la jerarquia de nodos de la vista de edicion
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/RegistroPropietario.fxml"));
            Parent root = loader.load();

            // Intercomunicacion de controladores: se obtiene la instancia del controlador hijo
            RegistroPropietarioController controladorFormulario = loader.getController();

            // Transferencia de contexto: se pasa la entidad completa para su modificacion
            controladorFormulario.cargarDatosParaEdicion(seleccionado);

            // Configuracion del contenedor de ventana (Stage)
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Mantenimiento de Propietario");
            
            // Restriccion de foco: APPLICATION_MODAL impide la interaccion con la ventana principal 
            // hasta que se resuelva la accion en la ventana actual.
            stage.initModality(Modality.APPLICATION_MODAL); 
            
            // Bloquea el hilo de ejecucion de la UI de esta clase hasta el cierre de la ventana modal
            stage.showAndWait();

            // Tras el cierre del modal, se recarga la informacion para asegurar consistencia de datos
            cargarTabla();

        } catch (Exception e) {
            System.err.println("Excepcion en tiempo de ejecucion al intentar instanciar la vista de edicion:");
            e.printStackTrace();
            SweetAlert.showError("Error de Sistema", "No fue posible cargar el modulo de edicion. Consulte los logs del sistema.");
        }
    }
}