package ui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ComboBox;

import javafx.stage.Stage;

public class ReporteGeneralController implements Initializable {

    /* =========================================================
       TABLE VIEW
    ========================================================= */

    @FXML
    private TableView<?> tablaReporte;

    @FXML
    private TableColumn<?, ?> colCasa;

    @FXML
    private TableColumn<?, ?> colPropietario;

    @FXML
    private TableColumn<?, ?> colMesActual;

    @FXML
    private TableColumn<?, ?> colTotalAnio;

    /* =========================================================
       LABELS RESUMEN
    ========================================================= */

    @FXML
    private Label lblTotalEsperado;

    @FXML
    private Label lblTotalRecaudado;

     /* =========================================================
       INITIALIZE
    ========================================================= */
    
    @FXML
    private ComboBox<String> cmbMes;

    @FXML
    private TableColumn<?, ?> colEstado;

    @FXML
    private TableColumn<?, ?> colMontoMes;
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Valores temporales visuales
        tablaReporte.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        lblTotalEsperado.setText("Q45,000");

        lblTotalRecaudado.setText("Q38,500");
    
        cmbMes.getItems().addAll(
            "Enero",
            "Febrero",
            "Marzo",
            "Abril",
            "Mayo",
            "Junio",
            "Julio",
            "Agosto",
            "Septiembre",
            "Octubre",
            "Noviembre",
            "Diciembre"
        );

        cmbMes.setValue("Mayo");
    }

    /* =========================================================
       VOLVER AL MENU
    ========================================================= */

    @FXML
    private void volverAlMenu(ActionEvent event) {

        try {

            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/ui/MenuPrincipal.fxml")
            );

            Parent root = loader.load();

            Stage stage = (Stage)
                ((Node) event.getSource())
                .getScene()
                .getWindow();

            stage.setScene(new Scene(root));

            stage.show();

        } catch (IOException e) {

            e.printStackTrace();
        }
    }
}