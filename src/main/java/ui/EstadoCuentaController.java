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

/**
 * Controlador de la interfaz de Estado de Cuenta.
 * Gestiona la visualizacion de pagos realizados y meses pendientes por propietario,
 * asi como la generacion del reporte en formato PDF.
 */
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
        
        // Configuracion de fechas por defecto al inicio y fin del año en curso
        dpInicio.setValue(
            LocalDate.of(
                LocalDate.now().getYear(),
                1,
                1
            )
        );

        dpFin.setValue(
            LocalDate.of(
                LocalDate.now().getYear(),
                12,
                31
            )
        );
        
        cargarCasasOcupadas();
        configurarFiltros();
    }    
    
    /**
     * Establece las restricciones de la interfaz y los eventos de escucha.
     * Esto permite que la informacion se actualice de forma reactiva cada vez que
     * el usuario cambia un parametro, mejorando la experiencia de usuario (UX).
     */
    private void configurarFiltros() {
        // Se desactiva la edicion manual para evitar errores de parseo de fechas ingresadas por teclado
        if (dpInicio != null) dpInicio.setEditable(false);
        if (dpFin != null) dpFin.setEditable(false);
        
        // Asignacion de listeners reactivos
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

    /**
     * Consulta la base de datos para obtener unicamente las casas que tienen un propietario asignado.
     * Esto optimiza la interfaz al no mostrar opciones invalidas al usuario.
     */
    private void cargarCasasOcupadas() {
        String sql = "SELECT c.numero_casa FROM casas c "
                   + "INNER JOIN propietarios p ON c.id = p.id_casa "
                   + "ORDER BY c.numero_casa";
                   
        // Implementacion de try-with-resources para garantizar el cierre de la conexion
        try (Connection con = Conexion.conectar()) {
            
            if (con != null) {
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
                System.err.println("Error: No se pudo obtener conexion del pool.");
            }
            
        } catch (SQLException e) {
            System.err.println("Excepcion al cargar el listado de casas: " + e.getMessage());
        }
    }
   
    /**
     * Metodo principal de procesamiento logico.
     * Calcula los meses pagados y pendientes cruzando el rango de fechas seleccionado
     * con la fecha de registro original del propietario.
     */
    @FXML
    private void buscarEstadoCuenta() {
        if(cmbCasas == null || dpInicio == null || dpFin == null) return;
        
        Integer casaSeleccionada = cmbCasas.getValue();
        LocalDate fechaInicio = dpInicio.getValue();
        LocalDate fechaFin = dpFin.getValue();
        
        if (casaSeleccionada != null && fechaInicio != null && fechaFin != null) {
            
            // Validacion basica de integridad de datos
            if(fechaInicio.isAfter(fechaFin)){
                System.out.println("Advertencia: El rango de fechas es invalido.");
                return;
            }
            
            // Limpieza de estados anteriores en la UI
            lvMesesPagados.getItems().clear();
            lvMesesPendientes.getItems().clear();
            lblTotalPagado.setText("Q0.00");
            
            ObservableList<String> mesesPagados = FXCollections.observableArrayList();
            List<String> mesesPendientesList = new ArrayList<>();
            double sumaTotal = 0.0;

            String sqlInfo = "SELECT p.id, p.nombre, p.fecha_registro FROM propietarios p INNER JOIN casas c ON p.id_casa = c.id WHERE c.numero_casa = ?";
            
            // La consulta transforma los nombres de los meses a valores numericos 
            // para permitir la evaluacion del rango mediante BETWEEN.
            String sqlPagos = "SELECT pa.mes, pa.anio, pa.monto " +
                              "FROM pagos pa " +
                              "WHERE pa.id_propietario = ? " + 
                              "AND (pa.anio * 100 + " +
                              "    CASE pa.mes " +
                              "        WHEN 'Enero' THEN 1 WHEN 'Febrero' THEN 2 WHEN 'Marzo' THEN 3 " +
                              "        WHEN 'Abril' THEN 4 WHEN 'Mayo' THEN 5 WHEN 'Junio' THEN 6 " +
                              "        WHEN 'Julio' THEN 7 WHEN 'Agosto' THEN 8 WHEN 'Septiembre' THEN 9 " +
                              "        WHEN 'Octubre' THEN 10 WHEN 'Noviembre' THEN 11 WHEN 'Diciembre' THEN 12 " +
                              "    END) BETWEEN ? AND ?";

            try (Connection con = Conexion.conectar()) {
                
                int idPropietarioActual = -1;

                // Fase 1: Obtencion de metadatos del propietario
                try (PreparedStatement psInfo = con.prepareStatement(sqlInfo)) {
                    psInfo.setInt(1, casaSeleccionada);
                    try (ResultSet rsInfo = psInfo.executeQuery()) {
                        if (rsInfo.next()) {
                            idPropietarioActual = rsInfo.getInt("id");
                            lblNombrePropietario.setText(rsInfo.getString("nombre"));
                            lblNombrePropietario.setStyle("-fx-text-fill: #000000; -fx-font-weight: bold;");
                            
                            // Se determinan todos los meses cobrables basados en su fecha de registro
                            java.sql.Date fechaSql = rsInfo.getDate("fecha_registro");
                            if(fechaSql != null) {
                                LocalDate fechaRegistro = fechaSql.toLocalDate();
                                mesesPendientesList = calcularMesesValidosEnRango(fechaInicio, fechaFin, fechaRegistro);
                            }
                        }
                    }
                }

                // Fase 2: Mapeo de pagos realizados
                if (idPropietarioActual != -1) {
                    try (PreparedStatement psPagos = con.prepareStatement(sqlPagos)) {
                        psPagos.setInt(1, idPropietarioActual);
                        
                        // Generacion de formato YYYYMM para comparacion numerica
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
                                
                                // Si el mes esta pagado, se descarta de la lista de pendientes
                                mesesPendientesList.remove(etiquetaPago);
                            }
                        }
                    }
                }
                
                // Actualizacion final de la interfaz
                lvMesesPagados.setItems(mesesPagados);
                lvMesesPendientes.setItems(FXCollections.observableArrayList(mesesPendientesList));
                lblTotalPagado.setText("Q" + String.format("%,.0f", sumaTotal));
                
            } catch (SQLException e) {
                System.err.println("Excepcion al procesar el estado de cuenta: " + e.getMessage());
            }
        }
    }
    
    /**
     * Algoritmo auxiliar para calcular la coleccion de meses que el propietario
     * realmente debe pagar, omitiendo el tiempo previo a su mudanza.
     */
    private List<String> calcularMesesValidosEnRango(LocalDate fechaInicioRango, LocalDate fechaFinRango, LocalDate fechaRegistro) {
        List<String> mesesValidos = new ArrayList<>();
        List<String> nombresMeses = Arrays.asList("", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre");

        // Evaluacion de logica de negocio: El cobro inicia a partir del registro o del rango solicitado,
        // lo que ocurra despues.
        LocalDate fechaInicioCobro = fechaInicioRango.isBefore(fechaRegistro) ? fechaRegistro : fechaInicioRango;

        LocalDate fechaActual = fechaInicioCobro.withDayOfMonth(1); 
        LocalDate fechaLimite = fechaFinRango.withDayOfMonth(fechaFinRango.lengthOfMonth()); 

        while (!fechaActual.isAfter(fechaLimite)) {
            String etiquetaMes = nombresMeses.get(fechaActual.getMonthValue()) + " " + fechaActual.getYear();
            mesesValidos.add(etiquetaMes);
            fechaActual = fechaActual.plusMonths(1);
        }

        return mesesValidos;
    }
   
    /**
     * Interaccion con JasperReports para la emision del documento fisico/digital.
     * Pasa los parametros de contexto de la UI y delega la consulta al motor de reportes.
     */
    @FXML
    private void generarReportePDF(ActionEvent event) {
        if(cmbCasas.getValue() == null || dpInicio.getValue() == null || dpFin.getValue() == null) {
            System.out.println("Validacion fallida: Parametros de busqueda incompletos.");
            return; 
        }

        try {
            Integer casaSeleccionada = cmbCasas.getValue();
            LocalDate fechaInicio = dpInicio.getValue();
            LocalDate fechaFin = dpFin.getValue();

            // Transformacion a formato numerico YYYYMM para el SQL interno de Jasper
            int rangoInicio = (fechaInicio.getYear() * 100) + fechaInicio.getMonthValue();
            int rangoFin = (fechaFin.getYear() * 100) + fechaFin.getMonthValue();

            java.util.Map<String, Object> parametros = new java.util.HashMap<>();
            parametros.put("P_NUMERO_CASA", casaSeleccionada);
            parametros.put("P_RANGO_INICIO", rangoInicio);
            parametros.put("P_RANGO_FIN", rangoFin);
            parametros.put("P_FECHA_INICIO_STR", fechaInicio.toString());
            parametros.put("P_FECHA_FIN_STR", fechaFin.toString());

            // Inyeccion de recursos estaticos al reporte
            java.io.InputStream logoStream = getClass().getResourceAsStream("/images/logo.png");
            
            if (logoStream == null) {
                System.out.println("Advertencia: No se detecto el recurso logo.png en el classpath.");
            } else {
                parametros.put("logoEmpresa", logoStream); 
            }

            java.io.InputStream reporteStream = getClass().getResourceAsStream("/reportes/EstadoCuentaIndividual.jasper");
            
            if (reporteStream == null) {
                System.err.println("Error critico: Archivo fuente de reporte (.jasper) no localizado.");
                return;
            }

            // Uso de try-with-resources asegura que Jasper libere la conexion tras renderizar
            try (java.sql.Connection conexion = db.Conexion.conectar()) {
                if (conexion == null) {
                    System.err.println("Error de infraestructura: Imposible conectar al cluster de base de datos.");
                    return;
                }

                net.sf.jasperreports.engine.JasperPrint jasperPrint = net.sf.jasperreports.engine.JasperFillManager.fillReport(reporteStream, parametros, conexion);
                net.sf.jasperreports.view.JasperViewer visor = new net.sf.jasperreports.view.JasperViewer(jasperPrint, false);
                visor.setTitle("Estado de Cuenta - Casa " + casaSeleccionada);
                visor.setVisible(true);
            } 

        } catch (Exception e) {
            System.err.println("Excepcion en el motor de renderizado de JasperReports:");
            e.printStackTrace();
        }
    }
}