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

public class ReporteGeneralController implements Initializable {

    /* =========================================================
       COMPONENTES DE LA VISTA
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
    @FXML private ComboBox<Integer> cmbYear;

    // =========================
    // MESES
    // =========================

    private final String[] MESES = {
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
    };

    // =========================
    // FORMATO MONEDA
    // =========================

    private final NumberFormat formatoQ =
            NumberFormat.getCurrencyInstance(new Locale("es", "GT"));

    /* =========================================================
       INITIALIZE
    ========================================================= */

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // =========================
        // ALINEACIONES
        // =========================

        colCasa.setStyle("-fx-alignment: CENTER;");
        colMontoMes.setStyle("-fx-alignment: CENTER;");
        colTotal.setStyle("-fx-alignment: CENTER;");
        colEstado.setStyle("-fx-alignment: CENTER;");

        tblReporte.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        configurarColumnas();
        configurarFormatoMoneda();
        inicializarComboBox();

        ejecutarReporte();
    }

    // =========================================================
    // CONFIGURAR COLUMNAS
    // =========================================================

    private void configurarColumnas() {

        colCasa.setCellValueFactory(
                new PropertyValueFactory<>("numeroCasa")
        );

        colPropietario.setCellValueFactory(
                new PropertyValueFactory<>("propietario")
        );

        colEstado.setCellValueFactory(
                new PropertyValueFactory<>("estadoMes")
        );

        colMontoMes.setCellValueFactory(
                new PropertyValueFactory<>("montoMes")
        );

        colTotal.setCellValueFactory(
                new PropertyValueFactory<>("totalAnual")
        );
    }

    // =========================================================
    // FORMATO MONEDA EN TABLA
    // =========================================================

    private void configurarFormatoMoneda() {

        colMontoMes.setCellFactory(col -> new TableCell<>() {

            @Override
            protected void updateItem(Double monto, boolean empty) {

                super.updateItem(monto, empty);

                if (empty || monto == null) {

                    setText(null);

                } else {

                    setText(
                        "Q" + String.format("%,.0f", monto)
                    );
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

                    setText(
                        "Q" + String.format("%,.0f", total)
                    );
                }
            }
        });
    }

    // =========================================================
    // COMBOBOX
    // =========================================================

    private void inicializarComboBox() {

        cmbMes.setItems(
                FXCollections.observableArrayList(MESES)
        );

        int mesActual = LocalDate.now().getMonthValue();

        cmbMes.getSelectionModel().select(mesActual - 1);

        int anioActual = LocalDate.now().getYear();

        cmbYear.getItems().addAll(
                anioActual - 1,
                anioActual,
                anioActual + 1
        );

        cmbYear.getSelectionModel().select(
                Integer.valueOf(anioActual)
        );
    }

    /* =========================================================
       EVENTOS
    ========================================================= */

    @FXML
    private void onCmbMesChange() {

        ejecutarReporte();
    }

    /* =========================================================
       EJECUTAR REPORTE
    ========================================================= */

    private void ejecutarReporte() {

        String mesSeleccionado =
                cmbMes.getSelectionModel().getSelectedItem();

        Integer anioSeleccionado =
                cmbYear.getSelectionModel().getSelectedItem();

        if (mesSeleccionado == null ||
            anioSeleccionado == null) {

            return;
        }

        logic.CuotaDAO cuotaDao = new logic.CuotaDAO();

        model.Cuota cuota = cuotaDao.obtenerCuota();

        double cuotaVigente =
                (cuota != null)
                ? cuota.getMontoActual()
                : 1500.00;

        ReporteDAO dao = new ReporteDAO();

        List<ReporteCasaDTO> datos =
                dao.obtenerReporteGeneral(
                        mesSeleccionado,
                        anioSeleccionado
                );

        ObservableList<ReporteCasaDTO> listaObservable =
                FXCollections.observableArrayList(datos);

        tblReporte.setItems(listaObservable);

        calcularTotalesFinancieros(datos, cuotaVigente);
    }

    /* =========================================================
       CALCULAR TOTALES
    ========================================================= */

    private void calcularTotalesFinancieros(
            List<ReporteCasaDTO> datos,
            double cuotaVigente
    ) {

        double totalRecaudadoMes = 0.0;
        double totalEsperado = 0.0;

        if (datos != null) {

            for (ReporteCasaDTO casa : datos) {

                if (casa.getPropietario()
                        .equalsIgnoreCase("Sin Asignar")) {

                    continue;
                }

                if (casa.getEstadoMes() != null &&
                    casa.getEstadoMes()
                        .trim()
                        .equalsIgnoreCase("Pagado")) {

                    totalRecaudadoMes += casa.getMontoMes();
                    totalEsperado += casa.getMontoMes();

                } else {

                    totalEsperado += cuotaVigente;
                }
            }
        }

        lblTotalEsperado.setText(
                "Q" + String.format("%,.0f", totalEsperado)
        );

        lblTotalRecaudado.setText(
                "Q" + String.format("%,.0f", totalRecaudadoMes)
        );
    }

    /* =========================================================
       GENERAR PDF
    ========================================================= */

    @FXML
    private void generarReportePDF(ActionEvent event) {

        String mesSeleccionado =
                cmbMes.getSelectionModel().getSelectedItem();

        Integer anioSeleccionado =
                cmbYear.getSelectionModel().getSelectedItem();

        if (mesSeleccionado == null ||
            anioSeleccionado == null) {

            System.out.println(
                    "Selecciona un mes y año."
            );

            return;
        }

        try {

            java.util.Map<String, Object> parametros =
                    new java.util.HashMap<>();

            parametros.put(
                    "MesSeleccionado",
                    mesSeleccionado
            );

            java.io.InputStream reporteStream =
                    getClass().getResourceAsStream(
                            "/reportes/ReporteVistaVerde.jasper"
                    );

            if (reporteStream == null) {

                System.out.println(
                        "No se encontró el archivo Jasper."
                );

                return;
            }

            try (java.sql.Connection conexion =
                         db.Conexion.conectar()) {

                if (conexion == null) {

                    System.out.println(
                            "No se pudo conectar a la BD."
                    );

                    return;
                }

                net.sf.jasperreports.engine.JasperPrint jasperPrint =
                        net.sf.jasperreports.engine.JasperFillManager
                                .fillReport(
                                        reporteStream,
                                        parametros,
                                        conexion
                                );

                net.sf.jasperreports.view.JasperViewer visor =
                        new net.sf.jasperreports.view.JasperViewer(
                                jasperPrint,
                                false
                        );

                visor.setTitle(
                        "Vista Verde - Reporte General"
                );

                visor.setVisible(true);
            }

        } catch (Exception e) {

            System.out.println(
                    "Error al generar PDF:"
            );

            e.printStackTrace();
        }
    }
}