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
import model.Propietario; // Importante agregar tu modelo

public class PagoController implements Initializable {

    @FXML private ComboBox<String> cmbCasas;
    @FXML private ComboBox<String> cmbMes;
    @FXML private ComboBox<Integer> cmbYear;
    @FXML private TextField txtMonto; 
    
    // 🌟 NUEVO: El campo de texto de solo lectura para el nombre
    @FXML private TextField txtNombrePropietario; 

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarMonto();
        llenarCombosEstaticos();
        cargarCasasOcupadas();
        configurarListenerCasas(); // 🌟 NUEVO: Iniciamos el "escucha" del ComboBox
    }

    private void configurarMonto() {
        logic.CuotaDAO dao = new logic.CuotaDAO();
        model.Cuota cuotaVigente = dao.obtenerCuota();
        
        // Verificamos que la BD sí nos haya devuelto algo
        if (cuotaVigente != null) {
            txtMonto.setText(String.format("%.2f", cuotaVigente.getMontoActual()));
        } else {
            // Si no hay internet o falló la consulta, lo dejamos en 0.00 y avisamos al usuario
            txtMonto.setText("0.00");
            logic.SweetAlert.showError("Error de Conexión", "No se pudo obtener la cuota actual de la base de datos.");
        }
        
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
    
    // 🌟 NUEVO MÉTODO: Se encarga de llenar el nombre del propietario automáticamente
    private void configurarListenerCasas() {
        cmbCasas.valueProperty().addListener((observable, oldValue, newValue) -> {
            // Verificamos que hayan seleccionado algo válido
            if (newValue != null && !newValue.startsWith("No hay")) {
                
                // Limpiamos el texto para que "Casa 5" se vuelva un int 5
                int numeroCasa = Integer.parseInt(newValue.replace("Casa ", ""));
                
                CasaDAO casaDao = new CasaDAO();
                Propietario prop = casaDao.obtenerPropietarioPorCasa(numeroCasa);
                
                if (prop != null) {
                    txtNombrePropietario.setText(prop.getNombre());
                } else {
                    txtNombrePropietario.setText("Sin Asignar");
                }
            } else {
                // Si limpian la selección, limpiamos el campo
                if (txtNombrePropietario != null) {
                    txtNombrePropietario.setText("");
                }
            }
        });
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

        // 🌟 BUENAS PRÁCTICAS: Validar fecha de registro extrayéndola del modelo Propietario
        logic.CasaDAO casaDao = new logic.CasaDAO();
        model.Propietario propietarioActual = casaDao.obtenerPropietarioPorCasa(numeroCasa);
        
        java.time.LocalDate fechaRegistroInquilino = null;
        if (propietarioActual != null) {
            fechaRegistroInquilino = propietarioActual.getFechaRegistro();
        }

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
        
        // 6. Confirmación final y ENVÍO DE CORREO 📧
        if (exito) {
            // Limpiamos la pantalla
            cmbCasas.getSelectionModel().clearSelection();
            cmbMes.getSelectionModel().clearSelection();
            cmbYear.getSelectionModel().selectFirst();
            
            if (txtNombrePropietario != null) {
                txtNombrePropietario.setText(""); // Limpiamos el nombre
            }

            //  MAGIA DEL CORREO: Validamos que el propietario tenga email y enviamos
            if (propietarioActual != null && propietarioActual.getCorreo() != null && !propietarioActual.getCorreo().trim().isEmpty()) {
                String correoDestino = propietarioActual.getCorreo();
                
                // Llamamos a la clase estática que construimos
                logic.EmailService.enviarRecibo(correoDestino, monto, mes, anio, numeroCasa);
                
                SweetAlert.showSuccess("¡Transacción Exitosa!", "El pago se registró y el recibo fue enviado a " + correoDestino);
            } else {
                // Si el inquilino no tiene correo, igual registramos el pago pero avisamos al usuario
                SweetAlert.showWarning("Pago Registrado", "El pago se guardó correctamente, pero el propietario no tiene un correo registrado para enviarle el recibo.");
            }

        } else {
            SweetAlert.showError("Error de Registro", "La transacción no pudo completarse. Es muy probable que este mes ya esté pagado.");
        };
        
        

    } 
    
    private int obtenerNumeroMes(String mesTexto){
        java.util.List<String> meses = java.util.Arrays.asList(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        );
        return meses.indexOf(mesTexto) + 1;
    }
        
       
}