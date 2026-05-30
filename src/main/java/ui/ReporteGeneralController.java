package ui;

import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import logic.ReporteDAO;
import model.ReporteCasaDTO;

/**
 * Controlador de la interfaz del Reporte General.
 * Se encarga de consolidar y presentar el estado financiero global del condominio,
 * calculando en tiempo real la proyeccion de ingresos (esperado) versus la 
 * recaudacion efectiva, y orquestando la exportacion de estos datos a PDF.
 */
public class ReporteGeneralController implements Initializable {

    // Nodos de la interfaz grafica inyectados por FXML
    @FXML private TableView<ReporteCasaDTO> tblReporte;
    @FXML private TableColumn<ReporteCasaDTO, Integer> colCasa;
    @FXML private TableColumn<ReporteCasaDTO, String> colPropietario;
    @FXML private TableColumn<ReporteCasaDTO, String> colEstado;
    @FXML private TableColumn<ReporteCasaDTO, Double> colMontoMes;
    @FXML private TableColumn<ReporteCasaDTO, Double> colTotal;
    @FXML private Label lblTotalEsperado;
    @FXML private Label lblTotalRecaudado;
    @FXML private ComboBox<String> cmbMes;
    @FXML private ComboBox<Integer> cmbYear;

    // Catalogo estatico en memoria para la estructuracion temporal
    private final String[] MESES = {
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };

    /**
     * Metodo de ciclo de vida de JavaFX.
     * Configura las politicas de renderizado de la tabla y prepara el estado inicial
     * del formulario antes de despachar la primera consulta a la base de datos.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Configuracion de alineacion visual para columnas numericas y de estado
        colCasa.setStyle("-fx-alignment: CENTER;");
        colMontoMes.setStyle("-fx-alignment: CENTER;");
        colTotal.setStyle("-fx-alignment: CENTER;");
        colEstado.setStyle("-fx-alignment: CENTER;");

        // Politica de redimensionamiento fluido para evitar espacios vacios en la UI
        tblReporte.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        configurarColumnas();
        configurarFormatoMoneda();
        inicializarComboBox();

        // Carga de datos inicial con el periodo actual
        ejecutarReporte();
    }

    /**
     * Enlaza las columnas de la vista con los atributos del DTO (Data Transfer Object).
     * Utiliza reflexion interna de JavaFX para inyectar los datos del modelo en la tabla.
     */
    private void configurarColumnas() {
        colCasa.setCellValueFactory(new PropertyValueFactory<>("numeroCasa"));
        colPropietario.setCellValueFactory(new PropertyValueFactory<>("propietario"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoMes"));
        colMontoMes.setCellValueFactory(new PropertyValueFactory<>("montoMes"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalAnual"));
    }

    /**
     * Sobrescribe el renderizado por defecto de las celdas (CellFactory).
     * Esto permite inyectar una mascara de formato de moneda local (Quetzales) 
     * unicamente en la capa de presentacion, manteniendo el tipo de dato numerico (Double) intacto en el modelo.
     */
    private void configurarFormatoMoneda() {
        colMontoMes.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double monto, boolean empty) {
                super.updateItem(monto, empty);
                if (empty || monto == null) {
                    setText(null);
                } else {
                    setText("Q" + String.format("%,.0f", monto));
                }
            }
        });

        colTotal.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double total, boolean empty) {
                super.updateItem(total, empty);
                if (empty || total == null) {
                    setText(null);
                } else {
                    setText("Q" + String.format("%,.0f", total));
                }
            }
        });
    }

    /**
     * Pobla los selectores de tiempo (mes y año) inicializandolos dinamicamente
     * en el periodo actual del sistema para facilitar la interaccion inmediata del usuario.
     */
    private void inicializarComboBox() {
        cmbMes.setItems(FXCollections.observableArrayList(MESES));

        int mesActual = LocalDate.now().getMonthValue();
        cmbMes.getSelectionModel().select(mesActual - 1);

        int anioActual = LocalDate.now().getYear();
        cmbYear.getItems().addAll(anioActual - 1, anioActual, anioActual + 1);
        cmbYear.getSelectionModel().select(Integer.valueOf(anioActual));
    }

    /**
     * Listener disparado por la UI cuando el usuario altera el selector de mes.
     * Desencadena una recarga reactiva de los datos financieros.
     */
    @FXML
    private void onCmbMesChange() {
        ejecutarReporte();
    }

    /**
     * Construye y despacha la consulta financiera global delegando al DAO.
     * Recupera el listado de Data Transfer Objects (DTOs) y lo inyecta en la 
     * coleccion observable que alimenta la tabla de la interfaz.
     */
    private void ejecutarReporte() {
        String mesSeleccionado = cmbMes.getSelectionModel().getSelectedItem();
        Integer anioSeleccionado = cmbYear.getSelectionModel().getSelectedItem();

        if (mesSeleccionado == null || anioSeleccionado == null) {
            return;
        }

        // Obtencion de la politica de cobro actual para el calculo de proyecciones
        logic.CuotaDAO cuotaDao = new logic.CuotaDAO();
        model.Cuota cuota = cuotaDao.obtenerCuota();
        double cuotaVigente = (cuota != null) ? cuota.getMontoActual() : 1500.00;

        ReporteDAO dao = new ReporteDAO();
        List<ReporteCasaDTO> datos = dao.obtenerReporteGeneral(mesSeleccionado, anioSeleccionado);

        ObservableList<ReporteCasaDTO> listaObservable = FXCollections.observableArrayList(datos);
        tblReporte.setItems(listaObservable);

        calcularTotalesFinancieros(datos, cuotaVigente);
    }

    /**
     * Algoritmo de conciliacion financiera. Itera sobre el set de datos recuperado
     * para calcular el flujo de caja real versus el flujo de caja esperado, 
     * omitiendo del calculo esperado a aquellas unidades que no poseen un titular asignado.
     */
    private void calcularTotalesFinancieros(List<ReporteCasaDTO> datos, double cuotaVigente) {
        double totalRecaudadoMes = 0.0;
        double totalEsperado = 0.0;

        if (datos != null) {
            for (ReporteCasaDTO casa : datos) {
                // Las propiedades deshabitadas no generan expectativa de cobro
                if (casa.getPropietario().equalsIgnoreCase("Sin Asignar")) {
                    continue;
                }

                // Conciliacion de estado de cuenta
                if (casa.getEstadoMes() != null && casa.getEstadoMes().trim().equalsIgnoreCase("Pagado")) {
                    totalRecaudadoMes += casa.getMontoMes();
                    totalEsperado += casa.getMontoMes();
                } else {
                    totalEsperado += cuotaVigente;
                }
            }
        }

        // Actualizacion en UI con formato monetario
        lblTotalEsperado.setText("Q" + String.format("%,.0f", totalEsperado));
        lblTotalRecaudado.setText("Q" + String.format("%,.0f", totalRecaudadoMes));
    }

    /**
     * Invoca el motor de JasperReports para la generacion del documento formal.
     * Transfiere el contexto temporal (mes/año) al reporte y renderiza el visualizador embebido.
     */
    @FXML
    private void generarReportePDF(ActionEvent event) {
        String mesSeleccionado = cmbMes.getSelectionModel().getSelectedItem();
        Integer anioSeleccionado = cmbYear.getSelectionModel().getSelectedItem();

        if (mesSeleccionado == null || anioSeleccionado == null) {
            System.err.println("Validacion fallida: Parametros temporales incompletos para la emision del reporte.");
            return;
        }

        try {
            java.util.Map<String, Object> parametros = new java.util.HashMap<>();
            parametros.put("MesSeleccionado", mesSeleccionado);

            // Inyeccion de recurso binario (Logotipo) en el contexto de Jasper
            java.io.InputStream logoStream = getClass().getResourceAsStream("/images/logo.png");
            if (logoStream != null) {
                parametros.put("logoEmpresa", logoStream); 
            } else {
                System.err.println("Advertencia de recurso: Archivo logo.png no localizado en el classpath (/images/).");
            }

            java.io.InputStream reporteStream = getClass().getResourceAsStream("/reportes/ReporteVistaVerde.jasper");
            if (reporteStream == null) {
                System.err.println("Error critico: Imposible localizar el binario del reporte (.jasper).");
                return;
            }

            // Conexion efimera gestionada por try-with-resources
            try (java.sql.Connection conexion = db.Conexion.conectar()) {
                if (conexion == null) {
                    System.err.println("Error de infraestructura: Pool de conexiones no disponible.");
                    return;
                }

                net.sf.jasperreports.engine.JasperPrint jasperPrint = net.sf.jasperreports.engine.JasperFillManager.fillReport(reporteStream, parametros, conexion);
                net.sf.jasperreports.view.JasperViewer visor = new net.sf.jasperreports.view.JasperViewer(jasperPrint, false);
                
                visor.setTitle("Vista Verde - Reporte General Financiero");
                visor.setVisible(true);
            }

        } catch (Exception e) {
            System.err.println("Excepcion capturada durante la canalizacion del motor JasperReports:");
            e.printStackTrace();
        }
    }
}