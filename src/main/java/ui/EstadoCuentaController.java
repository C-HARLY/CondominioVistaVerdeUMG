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
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class EstadoCuentaController implements Initializable {

    @FXML private ComboBox<Integer> cmbCasas;
    @FXML private DatePicker dpInicio;
    @FXML private DatePicker dpFin;
    @FXML private Label lblNombrePropietario;
    @FXML private ListView<String> lvMesesPagados;
    @FXML private ListView<String> lvMesesPendientes;
    @FXML private Label lblTotalPagado;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cargarCasasOcupadas();
        configurarFiltros();
    }    
    
    private void configurarFiltros() {
        // Bloqueamos la edición manual de texto en los calendarios (solo clic)
        if (dpInicio != null) dpInicio.setEditable(false);
        if (dpFin != null) dpFin.setEditable(false);
        
        // Listeners: Si cambia la casa, o alguna fecha, actualiza la pantalla automáticamente
        if(cmbCasas != null) {
            cmbCasas.valueProperty().addListener((obs, oldVal, newVal) -> buscarEstadoCuenta());
        }
        if(dpInicio != null) {
            dpInicio.valueProperty().addListener((obs, oldVal, newVal) -> buscarEstadoCuenta());
        }
        if(dpFin != null) {
            dpFin.valueProperty().addListener((obs, oldVal, newVal) -> buscarEstadoCuenta());
        }
    }

    private void cargarCasasOcupadas() {
        String sql = "SELECT c.numero_casa FROM casas c "
                   + "INNER JOIN propietarios p ON c.id = p.id_casa "
                   + "ORDER BY c.numero_casa";
                   
        // 1. Primero abrimos SOLO la conexión
        try (Connection con = Conexion.conectar()) {
            
            // 2. Validamos que Hikari sí nos haya dado una conexión (que no sea null)
            if (con != null) {
                // 3. Ahora sí, hacemos el statement seguros de que no explotará
                try (PreparedStatement ps = con.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                     
                    ObservableList<Integer> listaCasas = FXCollections.observableArrayList();
                    while (rs.next()) {
                        listaCasas.add(rs.getInt("numero_casa"));
                    }
                    if(cmbCasas != null) {
                       cmbCasas.setItems(listaCasas); 
                    }
                }
            } else {
                System.err.println("⚠️ No se pudo obtener conexión (Timeout o base de datos inalcanzable).");
            }
            
        } catch (SQLException e) {
            System.err.println("Error al cargar las casas ocupadas: " + e.getMessage());
        }
    }
   
    @FXML
    private void buscarEstadoCuenta() {
        // Esta validación asegura que el usuario no escriba texto en el combo si no está el FXML
        if(cmbCasas == null || dpInicio == null || dpFin == null) return;
        
        Integer casaSeleccionada = cmbCasas.getValue();
        LocalDate fechaInicio = dpInicio.getValue();
        LocalDate fechaFin = dpFin.getValue();
        
        // Validamos que los tres campos tengan datos
        if (casaSeleccionada != null && fechaInicio != null && fechaFin != null) {
            
            // Validación lógica: La fecha inicio no puede ser después de la fecha fin
            if(fechaInicio.isAfter(fechaFin)){
                System.out.println("La fecha de inicio no puede ser mayor a la fecha de fin");
                return;
            }
            
            lvMesesPagados.getItems().clear();
            lvMesesPendientes.getItems().clear();
            lblTotalPagado.setText("Q0.00");
            
            ObservableList<String> mesesPagados = FXCollections.observableArrayList();
            List<String> mesesPendientesList = new ArrayList<>();
            double sumaTotal = 0.0;

            String sqlInfo = "SELECT p.nombre, p.fecha_registro FROM propietarios p INNER JOIN casas c ON p.id_casa = c.id WHERE c.numero_casa = ?";
            
            // EL TRUCO DEL RANGO SIN TOCAR LA BASE DE DATOS:
            String sqlPagos = "SELECT pa.mes, pa.anio, pa.monto " +
                              "FROM pagos pa " +
                              "INNER JOIN casas c ON pa.id_casa = c.id " +
                              "WHERE c.numero_casa = ? " +
                              "AND (pa.anio * 100 + " +
                              "    CASE pa.mes " +
                              "        WHEN 'Enero' THEN 1 WHEN 'Febrero' THEN 2 WHEN 'Marzo' THEN 3 " +
                              "        WHEN 'Abril' THEN 4 WHEN 'Mayo' THEN 5 WHEN 'Junio' THEN 6 " +
                              "        WHEN 'Julio' THEN 7 WHEN 'Agosto' THEN 8 WHEN 'Septiembre' THEN 9 " +
                              "        WHEN 'Octubre' THEN 10 WHEN 'Noviembre' THEN 11 WHEN 'Diciembre' THEN 12 " +
                              "    END) BETWEEN ? AND ?";

            try (Connection con = Conexion.conectar()) {
                
                // --- EJECUTAR CONSULTA DE INFO (Nombre y Fecha) ---
                try (PreparedStatement psInfo = con.prepareStatement(sqlInfo)) {
                    psInfo.setInt(1, casaSeleccionada);
                    try (ResultSet rsInfo = psInfo.executeQuery()) {
                        if (rsInfo.next()) {
                            lblNombrePropietario.setText(rsInfo.getString("nombre"));
                            lblNombrePropietario.setStyle("-fx-text-fill: #000000; -fx-font-weight: bold;");
                            
                            java.sql.Date fechaSql = rsInfo.getDate("fecha_registro");
                            if(fechaSql != null) {
                                LocalDate fechaRegistro = fechaSql.toLocalDate();
                                mesesPendientesList = calcularMesesValidosEnRango(fechaInicio, fechaFin, fechaRegistro);
                            }
                        }
                    }
                }

                // --- EJECUTAR CONSULTA DE PAGOS EN EL RANGO ---
                try (PreparedStatement psPagos = con.prepareStatement(sqlPagos)) {
                    psPagos.setInt(1, casaSeleccionada);
                    
                    // Convertimos las fechas a nuestro formato numérico (Ej: 202601)
                    int rangoInicio = (fechaInicio.getYear() * 100) + fechaInicio.getMonthValue();
                    int rangoFin = (fechaFin.getYear() * 100) + fechaFin.getMonthValue();
                    
                    psPagos.setInt(2, rangoInicio);
                    psPagos.setInt(3, rangoFin);
                    
                    try (ResultSet rsPagos = psPagos.executeQuery()) {
                        while (rsPagos.next()) {
                            String mesQuePago = rsPagos.getString("mes");
                            int anioPago = rsPagos.getInt("anio");
                            double montoPagado = rsPagos.getDouble("monto");
                            
                            String etiquetaPago = mesQuePago + " " + anioPago;
                            mesesPagados.add(etiquetaPago + " (Q" + String.format("%,.0f", montoPagado) + ")");
                            sumaTotal += montoPagado;
                            
                            mesesPendientesList.remove(etiquetaPago);
                        }
                    }
                }
                
                // PINTAR LOS RESULTADOS
                lvMesesPagados.setItems(mesesPagados);
                ObservableList<String> mesesPendientes = FXCollections.observableArrayList(mesesPendientesList);
                lvMesesPendientes.setItems(mesesPendientes);
                
                lblTotalPagado.setText("Q" + String.format("%,.0f", sumaTotal));
                
            } catch (SQLException e) {
                System.err.println("Error procesando el estado de cuenta: " + e.getMessage());
            }
        } else {
            System.out.println("Esperando selección de casa y fechas.");
        }
    }
    
    // MÉTODO AUXILIAR ADAPTADO AL RANGO DE FECHAS
    private List<String> calcularMesesValidosEnRango(LocalDate fechaInicioRango, LocalDate fechaFinRango, LocalDate fechaRegistro) {
        List<String> mesesValidos = new ArrayList<>();
        List<String> nombresMeses = Arrays.asList("", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre");

        // Decidimos desde cuándo empezar a cobrar
        // Si el inquilino llegó DESPUÉS de la fecha de inicio del filtro, empezamos a cobrar desde que llegó
        LocalDate fechaInicioCobro = fechaInicioRango.isBefore(fechaRegistro) ? fechaRegistro : fechaInicioRango;

        // Iteramos mes a mes desde el inicio del cobro hasta la fecha fin del rango
        LocalDate fechaActual = fechaInicioCobro.withDayOfMonth(1); 
        LocalDate fechaLimite = fechaFinRango.withDayOfMonth(fechaFinRango.lengthOfMonth()); 

        while (!fechaActual.isAfter(fechaLimite)) {
            String etiquetaMes = nombresMeses.get(fechaActual.getMonthValue()) + " " + fechaActual.getYear();
            mesesValidos.add(etiquetaMes);
            fechaActual = fechaActual.plusMonths(1);
        }

        return mesesValidos;
    }
   
    @FXML
    private void generarReportePDF(ActionEvent event) {
        // 1. Validar que tengamos todo seleccionado
        if(cmbCasas.getValue() == null || dpInicio.getValue() == null || dpFin.getValue() == null) {
            System.out.println(" ALERTA: Selecciona la casa y el rango de fechas primero.");
            return; 
        }

        try {
            Integer casaSeleccionada = cmbCasas.getValue();
            LocalDate fechaInicio = dpInicio.getValue();
            LocalDate fechaFin = dpFin.getValue();

            // 2. Convertir fechas a nuestro formato matemático (Ej: Mayo 2026 -> 202605)
            int rangoInicio = (fechaInicio.getYear() * 100) + fechaInicio.getMonthValue();
            int rangoFin = (fechaFin.getYear() * 100) + fechaFin.getMonthValue();

            System.out.println("Generando Estado de Cuenta PDF para la casa: " + casaSeleccionada);

            // 3. Empaquetar los parámetros para Jasper
            java.util.Map<String, Object> parametros = new java.util.HashMap<>();
            parametros.put("P_NUMERO_CASA", casaSeleccionada);
            parametros.put("P_RANGO_INICIO", rangoInicio);
            parametros.put("P_RANGO_FIN", rangoFin);
            
            // Textos bonitos para que los pongas en el diseño del PDF
            parametros.put("P_FECHA_INICIO_STR", fechaInicio.toString());
            parametros.put("P_FECHA_FIN_STR", fechaFin.toString());

            // 4. Cargar el reporte
            java.io.InputStream reporteStream = getClass().getResourceAsStream("/reportes/EstadoCuentaIndividual.jasper");
            
            if (reporteStream == null) {
                System.out.println("❌ ERROR: No se encontró el archivo EstadoCuentaIndividual.jasper en resources/reportes");
                return;
            }

            // AQUI ESTA LA CORRECCIÓN: El try asegura que la conexión regrese al pool de Hikari
            try (java.sql.Connection conexion = db.Conexion.conectar()) {
                if (conexion == null) {
                    System.out.println(" ERROR: No se pudo conectar a Neon DB.");
                    return;
                }

                // 5. Generar y mostrar
                net.sf.jasperreports.engine.JasperPrint jasperPrint = net.sf.jasperreports.engine.JasperFillManager.fillReport(reporteStream, parametros, conexion);
                net.sf.jasperreports.view.JasperViewer visor = new net.sf.jasperreports.view.JasperViewer(jasperPrint, false);
                visor.setTitle("Estado de Cuenta - Casa " + casaSeleccionada);
                visor.setVisible(true);
            } 

        } catch (Exception e) {
            System.out.println(" ERROR FATAL al generar el PDF:");
            e.printStackTrace();
        }
    }   
}