package ui;

import db.Conexion;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

public class EstadoCuentaController implements Initializable {

    @FXML private ComboBox<Integer> cmbCasas;
   
    @FXML private ComboBox<Integer> cmbAnio; 
    @FXML private Label lblNombrePropietario;
    @FXML private ListView<String> lvMesesPagados;
    @FXML private ListView<String> lvMesesPendientes;
    @FXML private Label lblTotalPagado;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cargarCasasOcupadas();
        cargarAnios(); 
    }    
    
    // MÉTODO PARA LLENAR EL COMBOBOX DE AÑOS
    private void cargarAnios() {
        int anioActual = LocalDate.now().getYear();
        cmbAnio.getItems().addAll(anioActual - 1, anioActual, anioActual + 1, anioActual + 2);
        cmbAnio.getSelectionModel().select(Integer.valueOf(anioActual)); // Selecciona el actual por defecto
    }

    private void cargarCasasOcupadas() {
        String sql = "SELECT c.numero_casa FROM casas c "
                   + "INNER JOIN propietarios p ON c.id = p.id_casa "
                   + "ORDER BY c.numero_casa";
                   
        try (Connection con = Conexion.conectar(); 
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            ObservableList<Integer> listaCasas = FXCollections.observableArrayList();
            while (rs.next()) {
                listaCasas.add(rs.getInt("numero_casa"));
            }
            cmbCasas.setItems(listaCasas); 
            
        } catch (SQLException e) {
            System.err.println("Error al cargar las casas ocupadas: " + e.getMessage());
        }
    }
   
    @FXML
    private void buscarEstadoCuenta(ActionEvent event) {
        Integer casaSeleccionada = cmbCasas.getValue();
        Integer anioSeleccionado = cmbAnio.getValue(); // OBTENEMOS EL AÑO
        
        // Validamos que hayan seleccionado casa y año
        if (casaSeleccionada != null && anioSeleccionado != null) {
            
            lvMesesPagados.getItems().clear();
            lvMesesPendientes.getItems().clear();
            lblTotalPagado.setText("0.00");
            
            ObservableList<String> mesesPagados = FXCollections.observableArrayList();
            List<String> mesesPendientesList = new ArrayList<>();
            double sumaTotal = 0.0;

            // 3. CONSULTA MEJORADA: Traemos nombre Y fecha_registro
            String sqlInfo = "SELECT p.nombre, p.fecha_registro FROM propietarios p INNER JOIN casas c ON p.id_casa = c.id WHERE c.numero_casa = ?";
            
            // 4. CONSULTA MEJORADA: Filtramos los pagos por la casa Y EL AÑO seleccionado
            String sqlPagos = "SELECT pa.mes, pa.monto FROM pagos pa INNER JOIN casas c ON pa.id_casa = c.id WHERE c.numero_casa = ? AND pa.anio = ?";

            try (Connection con = Conexion.conectar()) {
                
                // --- EJECUTAR CONSULTA DE INFO (Nombre y Fecha) ---
                try (PreparedStatement psInfo = con.prepareStatement(sqlInfo)) {
                    psInfo.setInt(1, casaSeleccionada);
                    try (ResultSet rsInfo = psInfo.executeQuery()) {
                        if (rsInfo.next()) {
                            lblNombrePropietario.setText(rsInfo.getString("nombre"));
                            lblNombrePropietario.setStyle("-fx-text-fill: #000000; -fx-font-weight: bold;");
                            
                            // EXTRAEMOS LA FECHA DE REGISTRO
                            java.sql.Date fechaSql = rsInfo.getDate("fecha_registro");
                            if(fechaSql != null) {
                                LocalDate fechaRegistro = fechaSql.toLocalDate();
                                // CONSTRUIMOS LA LISTA DE MESES VÁLIDOS
                                mesesPendientesList = calcularMesesValidos(anioSeleccionado, fechaRegistro);
                            }
                        }
                    }
                }

                // --- EJECUTAR CONSULTA DE PAGOS ---
                try (PreparedStatement psPagos = con.prepareStatement(sqlPagos)) {
                    psPagos.setInt(1, casaSeleccionada);
                    psPagos.setInt(2, anioSeleccionado); // LE PASAMOS EL AÑO AL QUERY
                    
                    try (ResultSet rsPagos = psPagos.executeQuery()) {
                        while (rsPagos.next()) {
                            String mesQuePago = rsPagos.getString("mes");
                            double montoPagado = rsPagos.getDouble("monto");
                            
                            mesesPagados.add(mesQuePago + " (Q" + String.format("%,.0f", montoPagado) + ")");
                            sumaTotal += montoPagado;
                            
                            // EL TRUCO SIGUE FUNCIONANDO: Borramos de los pendientes solo si existe en la lista
                            mesesPendientesList.remove(mesQuePago);
                        }
                    }
                }
                
                // 5. PINTAR LOS RESULTADOS EN LA PANTALLA
                lvMesesPagados.setItems(mesesPagados);
                ObservableList<String> mesesPendientes = FXCollections.observableArrayList(mesesPendientesList);
                lvMesesPendientes.setItems(mesesPendientes);
                
                lblTotalPagado.setText("Q" + String.format("%,.0f", sumaTotal));
                
            } catch (SQLException e) {
                System.err.println("Error procesando el estado de cuenta: " + e.getMessage());
            }
        } else {
            System.out.println("Por favor selecciona una casa y un año.");
        }
    }
    
    // MÉTODO AUXILIAR: Calcula qué meses realmente debe el inquilino según cuándo llegó
    private List<String> calcularMesesValidos(int anioSeleccionado, LocalDate fechaRegistro) {
        List<String> todosLosMeses = Arrays.asList(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        );
        
        List<String> mesesValidos = new ArrayList<>();
        int anioRegistro = fechaRegistro.getYear();
        int mesRegistro = fechaRegistro.getMonthValue();

        if (anioSeleccionado < anioRegistro) {
            // Si el año consultado es antes de que llegara, no debe nada. Devuelve lista vacía.
            return mesesValidos; 
        } else if (anioSeleccionado == anioRegistro) {
            // Si es el año en que llegó, empieza a deber desde ese mes en adelante
            for (int i = mesRegistro - 1; i < 12; i++) {
                mesesValidos.add(todosLosMeses.get(i));
            }
        } else {
            // Si es un año posterior, debe los 12 meses
            mesesValidos.addAll(todosLosMeses);
        }
        
        return mesesValidos;
    }
   
     
}
 

