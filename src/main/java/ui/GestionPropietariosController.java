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

public class GestionPropietariosController {

    @FXML private TableView<Propietario> tblPropietarios;
    @FXML private TableColumn<Propietario, Integer> colCasa;
    @FXML private TableColumn<Propietario, String> colNombre;
    @FXML private TableColumn<Propietario, String> colTelefono;
    @FXML private TableColumn<Propietario, String> colCorreo;

    private PropietarioDAO dao = new PropietarioDAO();
    private ObservableList<Propietario> listaPropietarios;

    @FXML
    public void initialize() {
        // Enlazamos las columnas con el modelo (Asegúrate que los nombres coincidan con tu clase Propietario)
        colCasa.setCellValueFactory(new PropertyValueFactory<>("numeroCasa")); // Cambia a "numCasa" si así está en tu modelo
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colCorreo.setCellValueFactory(new PropertyValueFactory<>("correo"));
        colCasa.setStyle("-fx-alignment: CENTER;");
        colTelefono.setStyle("-fx-alignment: CENTER;");

        cargarTabla();
    }

    private void cargarTabla() {
        listaPropietarios = FXCollections.observableArrayList();
        listaPropietarios.addAll(dao.obtenerTodos());
        tblPropietarios.setItems(listaPropietarios);
    }

    // ==========================================
    // MÉTODO: REMOVER PROPIETARIO
    // ==========================================
    @FXML
    private void removerPropietario(ActionEvent event) {
        Propietario seleccionado = tblPropietarios.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            SweetAlert.showWarning("Selección requerida", "Por favor, selecciona un propietario de la tabla para dar de baja.");
            return;
        }

        boolean exito = dao.removerPropietario(seleccionado.getNumeroCasa());

        if (exito) {
            SweetAlert.showSuccess("Baja Exitosa", "El propietario ha sido removido y la casa ahora está disponible.");
            cargarTabla(); // Recarga reactiva
        } else {
            SweetAlert.showError("Error", "Ocurrió un problema al intentar dar de baja al propietario.");
        }
    }

    // ==========================================
    // MÉTODO: ABRIR EDICIÓN (LA CONEXIÓN MÁGICA)
    // ==========================================
    @FXML
    private void abrirEdicion(ActionEvent event) {
        Propietario seleccionado = tblPropietarios.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            SweetAlert.showWarning("Selección requerida", "Por favor, selecciona un propietario para editar sus datos.");
            return;
        }

        try {
            // 1. Cargamos el FXML del formulario
            // OJO: Si tu FXML se llama diferente, ajusta el nombre aquí
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/RegistroPropietario.fxml"));
            Parent root = loader.load();

            // 2. Atrapamos al controlador del formulario que acabamos de modificar
            RegistroPropietarioController controladorFormulario = loader.getController();

            // 3. Le pasamos el inquilino que el usuario seleccionó en la tabla
            controladorFormulario.cargarDatosParaEdicion(seleccionado);

            // 4. Creamos la ventana modal (flotante)
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Editar Datos de Propietario");
            
            // Bloquea la ventana de atrás para que el usuario no haga desastres
            stage.initModality(Modality.APPLICATION_MODAL); 
            
            // 5. Mostramos la ventana y el código se detiene aquí hasta que la cierren
            stage.showAndWait();

            // 6. Al cerrarse la ventana de edición, actualizamos la tabla por si hubo cambios
            cargarTabla();

        } catch (Exception e) {
            System.err.println("Error al abrir el formulario: " + e.getMessage());
            e.printStackTrace();
            SweetAlert.showError("Error de Sistema", "No se pudo abrir la ventana de edición. Verifica la consola.");
        }
    }
}