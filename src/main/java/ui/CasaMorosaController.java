package ui;

import java.net.URL;
import java.util.ResourceBundle;

import logic.CasasMorosasDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import javafx.scene.control.cell.PropertyValueFactory;
import model.CasaMorosa;

public class CasaMorosaController implements Initializable {

    @FXML
    private ComboBox<String> comboAnios;

    @FXML
    private ComboBox<String> comboMeses;

    @FXML
    private TableView<CasaMorosa> tablaMorosos;

    @FXML
    private TableColumn<CasaMorosa, String> colNumeroCasa;

    @FXML
    private TableColumn<CasaMorosa, String> colNombre;

    @FXML
    private TableColumn<CasaMorosa, String> colTelefono;

    @FXML
    private TableColumn<CasaMorosa, String> colCorreo;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // Carga los años disponibles en el ComboBox
        comboAnios.getItems().addAll("2025", "2026", "2027", "2028");

        // Define el año seleccionado por defecto
        comboAnios.setValue("2026");

        // Carga los meses disponibles en el ComboBox
        comboMeses.getItems().addAll(
                "Enero", "Febrero", "Marzo", "Abril",
                "Mayo", "Junio", "Julio", "Agosto",
                "Septiembre", "Octubre", "Noviembre", "Diciembre"
        );

        // Define el mes seleccionado por defecto
        comboMeses.setValue("Enero");

        // Vincula las columnas de la tabla con los atributos del modelo CasaMorosa
        colNumeroCasa.setCellValueFactory(new PropertyValueFactory<>("numeroCasa"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colCorreo.setCellValueFactory(new PropertyValueFactory<>("correo"));
        
        tablaMorosos.setPlaceholder(
        new Label("No existen casas morosas para el período seleccionado.")
        );
        
        colNumeroCasa.setStyle("-fx-alignment: CENTER;");
        colTelefono.setStyle("-fx-alignment: CENTER;");

        // Ejecuta la carga de datos cuando cambia el mes seleccionado
        comboMeses.setOnAction(e -> cargarMorosos());

        // Ejecuta la carga de datos cuando cambia el año seleccionado
        comboAnios.setOnAction(e -> cargarMorosos());

        // Carga inicial de casas morosas al abrir la ventana
        cargarMorosos();
    }

    @FXML
    private void cargarMorosos() {

        // Valida que exista un mes y año seleccionado
        if (comboMeses.getValue() == null || comboAnios.getValue() == null) {
            return;
        }

        // Obtiene los valores seleccionados
        String mes = comboMeses.getValue();
        int anio = Integer.parseInt(comboAnios.getValue());

        // Crea una instancia del DAO para consultar la base de datos
        CasasMorosasDAO dao = new CasasMorosasDAO();

        // Obtiene la lista de casas morosas según el mes y año seleccionado
        ObservableList<CasaMorosa> lista = FXCollections.observableArrayList(
                dao.obtenerCasasMorosas(mes, anio)
        );

        // Muestra información en consola para pruebas y depuración
        System.out.println("MES: " + mes);
        System.out.println("AÑO: " + anio);
        System.out.println("TAMAÑO LISTA: " + lista.size());

        // Asigna los datos obtenidos a la tabla
        tablaMorosos.setItems(lista);

         }
}