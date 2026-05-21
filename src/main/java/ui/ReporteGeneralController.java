/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
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

/**
 * FXML Controller class
 *
 * @author carlo
 */
public class ReporteGeneralController implements Initializable {

    @FXML private TableView<ReporteCasaDTO> tblReporte;
    @FXML private TableColumn<ReporteCasaDTO, Integer> colCasa;
    @FXML private TableColumn<ReporteCasaDTO, String> colPropietario;
    @FXML private TableColumn<ReporteCasaDTO, String> colEstado;
    @FXML private TableColumn<ReporteCasaDTO, Double> colTotal;
    @FXML private TableColumn<ReporteCasaDTO, Double> colMontoMes;
    @FXML private Label lblTotalEsperado;
    @FXML private Label lblTotalRecaudado;
    
    // El nuevo ComboBox para filtrar
    @FXML private ComboBox<String> cmbMes;

    private final String[] MESES = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarColumnas();
        inicializarComboBox();
        // Al cargar, procesa con el mes actual automáticamente
        ejecutarReporte();
    }

    private void configurarColumnas() {
        colCasa.setCellValueFactory(new PropertyValueFactory<>("numeroCasa"));
        colPropietario.setCellValueFactory(new PropertyValueFactory<>("propietario"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoMes"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalAnual"));
        colMontoMes.setCellValueFactory(new PropertyValueFactory<>("montoMes"));
    }

    private void inicializarComboBox() {
        // Llenamos el combo con los nombres de los meses
        cmbMes.setItems(FXCollections.observableArrayList(MESES));
        
        // Seleccionamos el mes actual del sistema por defecto
        int mesActualDelSistema = LocalDate.now().getMonthValue(); // Ejemplo: 5 para Mayo
        cmbMes.getSelectionModel().select(mesActualDelSistema - 1); // -1 porque el array empieza en 0
    }

    // Este es el evento que se dispara cuando cambias el mes en el ComboBox
    @FXML
    private void onCmbMesChange() {
        ejecutarReporte();
    }

    private void ejecutarReporte() {
    // 1. Capturamos el mes que el usuario seleccionó
    String mesSeleccionado = cmbMes.getSelectionModel().getSelectedItem();
    
    // Si la pantalla apenas está cargando y no hay mes, detenemos el método
    if (mesSeleccionado == null) return;

    // Debug en consola para confirmar que sí lee tu clic
    System.out.println("El ComboBox detectó el mes: " + mesSeleccionado); 

   
    if (colMontoMes != null) {
        colMontoMes.setText("Pagado en " + mesSeleccionado);
    }

    int anioActual = LocalDate.now().getYear();

    // 2. Instanciamos el DAO y traemos la cuota real de la base de datos
    ReporteDAO dao = new ReporteDAO();
    double cuotaVigente = dao.obtenerCuotaActual(); 

    // 3. Vamos a la base de datos en Neon a traer las 30 casas
    List<ReporteCasaDTO> datos = dao.obtenerReporteGeneral(mesSeleccionado, anioActual);

    // 4. Llenamos la tabla de JavaFX
    ObservableList<ReporteCasaDTO> listaObservable = FXCollections.observableArrayList(datos);
    tblReporte.setItems(listaObservable);

    // 5. Calculamos el dinero esperado y recaudado para los Labels de abajo
    calcularTotalesFinancieros(datos, cuotaVigente);
}

   private void calcularTotalesFinancieros(List<ReporteCasaDTO> datos, double cuotaVigente) {
    double totalEsperado = 30 * cuotaVigente; 
    double totalRecaudadoMes = 0.0; // Empezamos en cero limpio
    
    int contadorCasasPagadas = 0; // Para ver en consola

    if (datos != null) {
        for (ReporteCasaDTO casa : datos) {
            String estado = casa.getEstadoMes();
            // .trim() quita espacios fantasma y equalsIgnoreCase ignora mayúsculas/minúsculas
            if (estado != null && estado.trim().equalsIgnoreCase("Pagado")) {
                totalRecaudadoMes += cuotaVigente;
                contadorCasasPagadas++;
            }
        }
    }

    // 🕵️‍♂️ EL CHIVATO: Revisa la consola de NetBeans cuando cambies de mes
    System.out.println("--- CÁLCULO DEL MES ---");
    System.out.println("Casas detectadas como pagadas: " + contadorCasasPagadas);
    System.out.println("Total a pintar en el Label: Q. " + totalRecaudadoMes);
    System.out.println("-----------------------");

    // Mandamos el dinero a la pantalla
    lblTotalEsperado.setText(String.format("Q. %,.2f", totalEsperado));
    lblTotalRecaudado.setText(String.format("Q. %,.2f", totalRecaudadoMes));
}
  // --- NUEVO MÉTODO PARA EL BOTÓN DE JASPER ---
    @FXML
    private void generarReportePDF(ActionEvent event) {
        // 1. Reutilizamos tu ComboBox para saber qué mes quiere el usuario
        String mesSeleccionado = cmbMes.getSelectionModel().getSelectedItem();

        if (mesSeleccionado == null || mesSeleccionado.trim().isEmpty()) {
            System.out.println("⚠️ ALERTA: Selecciona un mes primero.");
            return; // Detiene la ejecución si no hay mes
        }

        try {
            System.out.println("Generando PDF para el mes: " + mesSeleccionado);

            // 2. Empaquetar el parámetro exacto que pusiste en Jaspersoft
            java.util.Map<String, Object> parametros = new java.util.HashMap<>();
            parametros.put("MesSeleccionado", mesSeleccionado);

            // 3. Buscar el archivo compilado en los recursos del proyecto
            java.io.InputStream reporteStream = getClass().getResourceAsStream("/reportes/ReporteVistaVerde.jasper");
            
            if (reporteStream == null) {
                System.out.println("❌ ERROR: No se encontró el archivo .jasper en la carpeta de resources.");
                return;
            }

            // 4. Obtener la conexión a tu base de datos Neon usando tu clase exacta
            java.sql.Connection conexion = db.Conexion.conectar(); 

            if (conexion == null) {
                System.out.println(" ERROR: No se pudo establecer conexión con Neon.");
                return;
            }

            // 5. Unir la plantilla visual con los datos
            net.sf.jasperreports.engine.JasperPrint jasperPrint = net.sf.jasperreports.engine.JasperFillManager.fillReport(reporteStream, parametros, conexion);

            // 6. Lanzar el visor en una ventana independiente
            net.sf.jasperreports.view.JasperViewer visor = new net.sf.jasperreports.view.JasperViewer(jasperPrint, false);
            visor.setTitle("Vista Verde - Estado de Cuenta (" + mesSeleccionado + ")");
            visor.setVisible(true);

        } catch (Exception e) {
            System.out.println(" ERROR FATAL al generar el PDF:");
            e.printStackTrace();
        }
    }
}