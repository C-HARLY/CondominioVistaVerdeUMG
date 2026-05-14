/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ui;

import db.Conexion;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author carlo
 */
public class EstadoCuentaController implements Initializable {

    @FXML private ComboBox<Integer> cmbCasas;
    @FXML private Label lblNombrePropietario;
    @FXML private ListView<String> lvMesesPagados;
    @FXML private ListView<String> lvMesesPendientes;
    @FXML private Label lblTotalPagado;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cargarCasasOcupadas();
     }    
    
    //METODO PARA TRAER LAS CASAS YA CON DUEÑO 
    private void cargarCasasOcupadas() {
        String sql = "SELECT c.numero_casa FROM casas c "
                   + "INNER JOIN propietarios p ON c.id = p.id_casa "
                   + "ORDER BY c.numero_casa";
                   
        try (Connection con = Conexion.conectar(); // Revisa que este sea tu método de conexión
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            ObservableList<Integer> listaCasas = FXCollections.observableArrayList();
            while (rs.next()) {
                listaCasas.add(rs.getInt("numero_casa"));
            }
            cmbCasas.setItems(listaCasas); 
            
        } catch (SQLException e) {
            System.err.println("Error al cargar las casas ocupadas: " + e.getMessage());
            e.printStackTrace();
        }
    }
   
    // MÉTODO: Se ejecuta al presionar el botón de buscar
    @FXML
    private void buscarEstadoCuenta(ActionEvent event) {
        Integer casaSeleccionada = cmbCasas.getValue();
        
        if (casaSeleccionada != null) {
            // 1. LIMPIAR DATOS ANTERIORES (Por si el usuario cambia de casa)
            lvMesesPagados.getItems().clear();
            lvMesesPendientes.getItems().clear();
            lblTotalPagado.setText("0.00");
            
            // 2. ARREGLO  DE MESES
            java.util.List<String> mesesPendientesList = new java.util.ArrayList<>(java.util.Arrays.asList(
                "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
            ));
            
            ObservableList<String> mesesPagados = FXCollections.observableArrayList();
            double sumaTotal = 0.0;

            // 3. CONSULTAR EL NOMBRE 
            String sqlNombre = "SELECT p.nombre FROM propietarios p INNER JOIN casas c ON p.id_casa = c.id WHERE c.numero_casa = ?";
            
            // 4. CONSULTAR LOS PAGOS 
            String sqlPagos = "SELECT pa.mes, pa.monto FROM pagos pa INNER JOIN casas c ON pa.id_casa = c.id WHERE c.numero_casa = ?";

            try (Connection con = Conexion.conectar()) {
                
                // --- EJECUTAR CONSULTA DE NOMBRE ---
                try (PreparedStatement psNombre = con.prepareStatement(sqlNombre)) {
                    psNombre.setInt(1, casaSeleccionada);
                    try (ResultSet rsNombre = psNombre.executeQuery()) {
                        if (rsNombre.next()) {
                            lblNombrePropietario.setText(rsNombre.getString("nombre"));
                            lblNombrePropietario.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                        }
                    }
                }

                // --- EJECUTAR CONSULTA DE PAGOS ---
                try (PreparedStatement psPagos = con.prepareStatement(sqlPagos)) {
                    psPagos.setInt(1, casaSeleccionada);
                    try (ResultSet rsPagos = psPagos.executeQuery()) {
                        
                        // Recorremos todos los pagos que ha hecho este dueño
                        while (rsPagos.next()) {
                            String mesQuePago = rsPagos.getString("mes");
                            double montoPagado = rsPagos.getDouble("monto");
                            
                            // A) Lo agregamos a la lista visual de Pagados
                            mesesPagados.add(mesQuePago + " (Q" + montoPagado + ")");
                            
                            // B) Sumamos el dinero a la calculadora total
                            sumaTotal += montoPagado;
                            
                            // C) ¡EL TRUCO! Lo borramos de la lista de pendientes
                            mesesPendientesList.remove(mesQuePago);
                        }
                    }
                }
                
                // 5. PINTAR LOS RESULTADOS EN LA PANTALLA
                lvMesesPagados.setItems(mesesPagados);
                
                // Convertimos lo que sobró del ArrayList a ObservableList para la vista
                ObservableList<String> mesesPendientes = FXCollections.observableArrayList(mesesPendientesList);
                lvMesesPendientes.setItems(mesesPendientes);
                
                // Formateamos el total para que se vea como moneda
                lblTotalPagado.setText("Total Pagado: Q" + String.format("%.2f", sumaTotal));
                
            } catch (SQLException e) {
                System.err.println("Error procesando el estado de cuenta: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    
    // METODO VOLVER AL MENU
    @FXML
    private void volverAlMenu(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/MenuPrincipal.fxml")); 
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    } 
    
}
