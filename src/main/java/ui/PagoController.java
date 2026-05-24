package ui;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import logic.CasaDAO;
import logic.SweetAlert;

public class PagoController implements Initializable {

    @FXML private ComboBox<String> cmbCasas;
    @FXML private ComboBox<String> cmbMes;
    @FXML private ComboBox<Integer> cmbYear;
    @FXML private TextField txtMonto; 

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarMonto();
        llenarCombosEstaticos();
        cargarCasasOcupadas();
    }

    private void configurarMonto() {
        logic.ReporteDAO dao = new logic.ReporteDAO();
        double montoActual = dao.obtenerCuotaActual();
        txtMonto.setText(String.format("%.2f", montoActual));
        txtMonto.setEditable(false); 
    }

    private void llenarCombosEstaticos() {
        cmbMes.getItems().addAll("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
                                 "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre");

        int anioActual = java.time.LocalDate.now().getYear();
        cmbYear.getItems().clear();
        cmbYear.getItems().add(anioActual); 
        cmbYear.getSelectionModel().selectFirst();
    }

    private void cargarCasasOcupadas() {
        CasaDAO dao = new CasaDAO();
        List<Integer> ocupadas = dao.obtenerCasasOcupadas();

        cmbCasas.getItems().clear();
        if (ocupadas.isEmpty()) {
            cmbCasas.setPromptText("No hay casas ocupadas");
        } else {
            for (Integer num : ocupadas) {
                cmbCasas.getItems().add("Casa " + num);
            }
        }
    }
    
    @FXML
    private void registrarPago(ActionEvent event) {
        // 1. Extraer los datos de la pantalla
        String casaSeleccionada = cmbCasas.getValue();
        String mes = cmbMes.getValue();
        Integer anio = cmbYear.getValue();
        String montoTexto = txtMonto.getText();

        // 2. Validar que no haya campos vacíos
        if (casaSeleccionada == null || mes == null || anio == null || montoTexto.isEmpty()) {
            SweetAlert.showWarning("Campos Incompletos", "Por favor, selecciona todos los datos requeridos en el formulario.");
            return; 
        }
        
        // 3. Limpiar los datos para poder hacer validaciones numéricas
        int numeroCasa = Integer.parseInt(casaSeleccionada.replace("Casa ", ""));
        double monto = Double.parseDouble(montoTexto.replace(",", "."));
        int mesSeleccionadoNum = obtenerNumeroMes(mes);

        // Validar fecha de registro del propietario
        logic.CasaDAO casaDao = new logic.CasaDAO();
        java.time.LocalDate fechaRegistroInquilino = casaDao.obtenerFechaRegistroPropietario(numeroCasa);

        if (fechaRegistroInquilino != null) {
            int mesRegistro = fechaRegistroInquilino.getMonthValue();
            int anioRegistro = fechaRegistroInquilino.getYear();

            if (anio < anioRegistro || (anio == anioRegistro && mesSeleccionadoNum < mesRegistro)) {
                SweetAlert.showError("Periodo Inválido", "No se pueden procesar cobros previos a la fecha de ingreso del inquilino (" + fechaRegistroInquilino + ").");
                return; 
            }
            
            // --- VALIDACIÓN DE SECUENCIA DE MESES ---
            List<String> mesesNombres = java.util.Arrays.asList("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre");

            int indexMesActual = mesesNombres.indexOf(mes); 
            int mesAnteriorIndex = indexMesActual - 1;
            int anioAnterior = anio;

            if (mesAnteriorIndex < 0) {
                mesAnteriorIndex = 11; 
                anioAnterior--; 
            }

            String nombreMesAnterior = mesesNombres.get(mesAnteriorIndex);
            int valorMesAnteriorNumerico = mesAnteriorIndex + 1;

            java.time.LocalDate fechaMesAnterior = java.time.LocalDate.of(anioAnterior, valorMesAnteriorNumerico, 1);
            java.time.LocalDate fechaInicioCobro = fechaRegistroInquilino.withDayOfMonth(1);

            if (!fechaMesAnterior.isBefore(fechaInicioCobro)) {
                logic.PagoDAO pagoDaoAux = new logic.PagoDAO();
                boolean mesAnteriorEstaPagado = pagoDaoAux.verificarPagoExiste(numeroCasa, nombreMesAnterior, anioAnterior);
                
                if (!mesAnteriorEstaPagado) {
                    SweetAlert.showError("Validación de Historial", "No puedes pagar " + mes + " " + anio + " porque está pendiente el mes de " + nombreMesAnterior + " " + anioAnterior + ".");
                    return; 
                }
            }
        }

        // 4. Si pasó todas las validaciones, creamos el objeto Pago
        model.Pago pagoParaGuardar = new model.Pago(0, numeroCasa, mes, anio, monto);

        // 5. Mandar a guardar usando nuestro DAO
        logic.PagoDAO dao = new logic.PagoDAO();
        boolean exito = dao.registrarPago(pagoParaGuardar);
        
        // 6. Confirmación final con tu estilo SweetAlert
        if (exito) {
            // Limpiamos la pantalla
            cmbCasas.getSelectionModel().clearSelection();
            cmbMes.getSelectionModel().clearSelection();
            cmbYear.getSelectionModel().selectFirst();

            SweetAlert.showSuccess("¡Transacción Exitosa!", "El pago de la Casa " + numeroCasa + " se registró correctamente.");
        } else {
            SweetAlert.showError("Error de Registro", "La transacción no pudo completarse. Es muy probable que este mes ya esté pagado.");
        }
    }
    
    private int obtenerNumeroMes(String mesTexto) {
        java.util.List<String> meses = java.util.Arrays.asList(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        );
        return meses.indexOf(mesTexto) + 1;
    }
}