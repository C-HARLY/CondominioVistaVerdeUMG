package ui;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import logic.ReporteDAO;
import model.ReporteCasaDTO;

public class ReporteGeneralController implements Initializable {

    /* =========================================================
       COMPONENTES DE LA VISTA (Alineados con el diseño de Axel)
    ========================================================= */
    @FXML private TableView<ReporteCasaDTO> tblReporte;
    @FXML private TableColumn<ReporteCasaDTO, Integer> colCasa;
    @FXML private TableColumn<ReporteCasaDTO, String> colPropietario;
    @FXML private TableColumn<ReporteCasaDTO, String> colEstado;
    @FXML private TableColumn<ReporteCasaDTO, Double> colMontoMes;
    @FXML private TableColumn<ReporteCasaDTO, Double> colTotal;

    @FXML private Label lblTotalEsperado;
    @FXML private Label lblTotalRecaudado;
    
    @FXML private ComboBox<String> cmbMes;

    private final String[] MESES = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};

    /* =========================================================
       INITIALIZE
    ========================================================= */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Ajuste visual para que la tabla de Axel se vea simétrica
        colCasa.setStyle("-fx-alignment: CENTER;");
        colMontoMes.setStyle("-fx-alignment: CENTER;");
        colTotal.setStyle("-fx-alignment: CENTER;");
        
        tblReporte.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        configurarColumnas();
        inicializarComboBox();
        // Al cargar, procesa con el mes actual automáticamente
        ejecutarReporte();
    }

    private void configurarColumnas() {
        colCasa.setCellValueFactory(new PropertyValueFactory<>("numeroCasa"));
        colPropietario.setCellValueFactory(new PropertyValueFactory<>("propietario"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoMes"));
        colMontoMes.setCellValueFactory(new PropertyValueFactory<>("montoMes"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalAnual"));
    }

    private void inicializarComboBox() {
        // Llenamos el combo con los nombres de los meses
        cmbMes.setItems(FXCollections.observableArrayList(MESES));
        
        // Seleccionamos el mes actual del sistema por defecto
        int mesActualDelSistema = LocalDate.now().getMonthValue(); 
        cmbMes.getSelectionModel().select(mesActualDelSistema - 1); 
    }

    /* =========================================================
       LÓGICA DEL REPORTE Y BASE DE DATOS
    ========================================================= */
    @FXML
    private void onCmbMesChange() {
        ejecutarReporte();
    }

    private void ejecutarReporte() {
        String mesSeleccionado = cmbMes.getSelectionModel().getSelectedItem();
        
        if (mesSeleccionado == null) return;

        System.out.println("El ComboBox detectó el mes: " + mesSeleccionado); 

        if (colMontoMes != null) {
            colMontoMes.setText("Monto");
        }

        int anioActual = LocalDate.now().getYear();

        ReporteDAO dao = new ReporteDAO();
        double cuotaVigente = dao.obtenerCuotaActual(); 

        List<ReporteCasaDTO> datos = dao.obtenerReporteGeneral(mesSeleccionado, anioActual);

        ObservableList<ReporteCasaDTO> listaObservable = FXCollections.observableArrayList(datos);
        tblReporte.setItems(listaObservable);

        calcularTotalesFinancieros(datos, cuotaVigente);
    }

    private void calcularTotalesFinancieros(List<ReporteCasaDTO> datos, double cuotaVigente) {
        double totalEsperado = 30 * cuotaVigente; 
        double totalRecaudadoMes = 0.0; 
        
        int contadorCasasPagadas = 0; 

        if (datos != null) {
            for (ReporteCasaDTO casa : datos) {
                String estado = casa.getEstadoMes();
                if (estado != null && estado.trim().equalsIgnoreCase("Pagado")) {
                    totalRecaudadoMes += cuotaVigente;
                    contadorCasasPagadas++;
                }
            }
        }

        System.out.println("--- CÁLCULO DEL MES ---");
        System.out.println("Casas detectadas como pagadas: " + contadorCasasPagadas);
        System.out.println("Total a pintar en el Label: Q. " + totalRecaudadoMes);
        System.out.println("-----------------------");

        lblTotalEsperado.setText(String.format("Q. %,.0f", totalEsperado));
        lblTotalRecaudado.setText(String.format("Q. %,.0f", totalRecaudadoMes));
    }

    /* =========================================================
       JASPER REPORTS
    ========================================================= */
    @FXML
    private void generarReportePDF(ActionEvent event) {
        String mesSeleccionado = cmbMes.getSelectionModel().getSelectedItem();

        if (mesSeleccionado == null || mesSeleccionado.trim().isEmpty()) {
            System.out.println("⚠️ ALERTA: Selecciona un mes primero.");
            return; 
        }

        try {
            System.out.println("Generando PDF para el mes: " + mesSeleccionado);

            java.util.Map<String, Object> parametros = new java.util.HashMap<>();
            parametros.put("MesSeleccionado", mesSeleccionado);

            java.io.InputStream reporteStream = getClass().getResourceAsStream("/reportes/ReporteVistaVerde.jasper");
            
            if (reporteStream == null) {
                System.out.println("❌ ERROR: No se encontró el archivo .jasper en la carpeta de resources.");
                return;
            }

            java.sql.Connection conexion = db.Conexion.conectar(); 

            if (conexion == null) {
                System.out.println(" ERROR: No se pudo establecer conexión con Neon.");
                return;
            }

            net.sf.jasperreports.engine.JasperPrint jasperPrint = net.sf.jasperreports.engine.JasperFillManager.fillReport(reporteStream, parametros, conexion);
            net.sf.jasperreports.view.JasperViewer visor = new net.sf.jasperreports.view.JasperViewer(jasperPrint, false);
            visor.setTitle("Vista Verde - Estado de Cuenta (" + mesSeleccionado + ")");
            visor.setVisible(true);

        } catch (Exception e) {
            System.out.println(" ERROR FATAL al generar el PDF:");
            e.printStackTrace();
        }
    }

    /* =========================================================
       NAVEGACIÓN (Diseño de Axel)
    ========================================================= */
    @FXML
    private void volverAlMenu(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/MenuPrincipal.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.out.println("Error al cargar el Menú Principal:");
            e.printStackTrace();
        }
    }
}