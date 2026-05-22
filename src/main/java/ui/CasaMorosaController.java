package ui;

import java.net.URL;
import java.util.ResourceBundle;

import db.CasasMorosas;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {

       
        comboAnios.getItems().addAll("2025", "2026", "2027", "2028");
        comboAnios.setValue("2026");

       
        comboMeses.getItems().addAll(
                "Enero", "Febrero", "Marzo", "Abril",
                "Mayo", "Junio", "Julio", "Agosto",
                "Septiembre", "Octubre", "Noviembre", "Diciembre"
        );
        comboMeses.setValue("Enero");

        colNumeroCasa.setCellValueFactory(new PropertyValueFactory<>("numeroCasa"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));

        comboMeses.setOnAction(e -> cargarMorosos());
        comboAnios.setOnAction(e -> cargarMorosos());

        
        cargarMorosos();
    }

    @FXML
    private void cargarMorosos() {

        
        if (comboMeses.getValue() == null || comboAnios.getValue() == null) {
            return;
        }

        String mes = comboMeses.getValue();
        int anio = Integer.parseInt(comboAnios.getValue());

        CasasMorosas dao = new CasasMorosas();

        ObservableList<CasaMorosa> lista = FXCollections.observableArrayList(
                dao.obtenerCasasMorosas(mes, anio)
        );

        tablaMorosos.setItems(lista);

        
        if (lista.isEmpty()) {

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Información");
            alert.setHeaderText(null);
            alert.setContentText(
                    "Al día de hoy, todas las casas se encuentran al día con sus pagos"
            );
            alert.showAndWait();
        }
    }
}