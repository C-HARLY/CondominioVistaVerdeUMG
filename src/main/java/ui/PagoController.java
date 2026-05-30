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
import model.Propietario; 

/**
 * Controlador de la interfaz de Registro de Pagos.
 * Orquesta la logica de transacciones financieras del condominio, aplicando 
 * validaciones estrictas de secuencia, prevencion de duplicados y verificacion 
 * de asignacion de propietarios antes de persistir los datos.
 */
public class PagoController implements Initializable {

    // Nodos de la interfaz grafica inyectados por FXML
    @FXML private ComboBox<String> cmbCasas;
    @FXML private ComboBox<String> cmbMes;
    @FXML private ComboBox<Integer> cmbYear;
    @FXML private TextField txtMonto; 
    @FXML private TextField txtNombrePropietario; 

    /**
     * Metodo de ciclo de vida de JavaFX.
     * Inicializa el estado base del formulario de pagos, cargando dependencias
     * y configurando los listeners reactivos antes de la interaccion del usuario.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarMonto();
        llenarCombosEstaticos();
        cargarCasasOcupadas();
        configurarListenerCasas(); 
    }

    /**
     * Recupera la cuota de mantenimiento vigente desde la base de datos y
     * bloquea el campo de texto para garantizar la integridad del cobro 
     * y evitar alteraciones manuales no autorizadas.
     */
    private void configurarMonto() {
        logic.CuotaDAO dao = new logic.CuotaDAO();
        model.Cuota cuotaVigente = dao.obtenerCuota();
        
        if (cuotaVigente != null) {
            // Formateo visual eliminando decimales para representacion de moneda local
            txtMonto.setText(String.format("Q%,.0f", cuotaVigente.getMontoActual()));        
        } else {
            // Fallback de seguridad en caso de perdida de conexion
            txtMonto.setText("0");
            logic.SweetAlert.showError("Error de Conexion", "No se pudo obtener la cuota actual del sistema central.");
        }
        
        // Restriccion de mutabilidad en la interfaz
        txtMonto.setEditable(false); 
    }
   
    /**
     * Inicializa los selectores de periodos de cobro.
     * Carga el catalogo estatico de meses y asigna el año en curso dinamicamente
     * para facilitar la captura de datos.
     */
    private void llenarCombosEstaticos() {
        cmbMes.getItems().addAll("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
                                 "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre");

        int anioActual = java.time.LocalDate.now().getYear();
        cmbYear.getItems().clear();
        cmbYear.getItems().add(anioActual); 
        cmbYear.getSelectionModel().selectFirst();
    }

    /**
     * Consulta la capa de acceso a datos para poblar el selector unicamente 
     * con inmuebles que poseen un propietario activo. 
     * Esta restriccion previene operaciones de cobro huerfanas.
     */
    private void cargarCasasOcupadas() {
        CasaDAO dao = new CasaDAO();
        List<Integer> ocupadas = dao.obtenerCasasOcupadas();

        cmbCasas.getItems().clear();
        if (ocupadas.isEmpty()) {
            cmbCasas.setPromptText("No hay unidades ocupadas");
        } else {
            for (Integer num : ocupadas) {
                cmbCasas.getItems().add("Casa " + num);
            }
        }
    }
    
    /**
     * Implementa un patron observador sobre el selector de casas.
     * Al detectar un cambio de estado, realiza una busqueda delegada para 
     * auto-completar la informacion del propietario asociado, mejorando la UX.
     */
    private void configurarListenerCasas() {
        cmbCasas.valueProperty().addListener((observable, oldValue, newValue) -> {
            
            // Verificacion de seleccion valida
            if (newValue != null && !newValue.startsWith("No hay")) {
                
                // Saneamiento de string para extraccion del identificador numerico
                int numeroCasa = Integer.parseInt(newValue.replace("Casa ", ""));
                
                CasaDAO casaDao = new CasaDAO();
                Propietario prop = casaDao.obtenerPropietarioPorCasa(numeroCasa);
                
                if (prop != null) {
                    txtNombrePropietario.setText(prop.getNombre());
                } else {
                    txtNombrePropietario.setText("Sin Asignar");
                }
            } else {
                // Limpieza de estado en caso de deseleccion
                if (txtNombrePropietario != null) {
                    txtNombrePropietario.setText("");
                }
            }
        });
    }
    
    /**
     * Punto de entrada principal para el registro transaccional de un pago.
     * Ejecuta una cadena de validaciones de reglas de negocio antes de delegar
     * la persistencia al DAO y emitir la notificacion por correo.
     * * @param event Evento disparado por la accion del usuario en la UI.
     */
    @FXML
    private void registrarPago(ActionEvent event) {
        
        // 1. Extraccion de estado de los componentes de la vista
        String casaSeleccionada = cmbCasas.getValue();
        String mes = cmbMes.getValue();
        Integer anio = cmbYear.getValue();
        String montoTexto = txtMonto.getText();

        // 2. Validacion de completitud de formulario
        if (casaSeleccionada == null || mes == null || anio == null || montoTexto.isEmpty()) {
            SweetAlert.showWarning("Campos Incompletos", "Es necesario completar todos los parametros requeridos para procesar el pago.");
            return; 
        }
        
        // 3. Transformacion y saneamiento de datos para procesamiento logico
        int numeroCasa = Integer.parseInt(casaSeleccionada.replace("Casa ", ""));

        // Limpieza de caracteres de formato monetario para parseo a double
        String montoLimpio = montoTexto.replace("Q", "").replace(",", "").trim();
        double monto = Double.parseDouble(montoLimpio); 
        int mesSeleccionadoNum = obtenerNumeroMes(mes);
        
        // 4. Validacion de Integridad Relacional: Existencia de propietario y fecha de ingreso
        logic.CasaDAO casaDao = new logic.CasaDAO();
        model.Propietario propietarioActual = casaDao.obtenerPropietarioPorCasa(numeroCasa);
        
        if (propietarioActual == null) {
            SweetAlert.showError("Inconsistencia de Datos", "Imposible procesar: La unidad seleccionada no posee un titular activo.");
            return;
        }

        java.time.LocalDate fechaRegistroInquilino = propietarioActual.getFechaRegistro();
        int mesRegistro = fechaRegistroInquilino.getMonthValue();
        int anioRegistro = fechaRegistroInquilino.getYear();

        // Regla de Negocio: Restriccion de cobros retroactivos previos al alta del inquilino
        if (anio < anioRegistro || (anio == anioRegistro && mesSeleccionadoNum < mesRegistro)) {
            SweetAlert.showError("Periodo Invalido", "Restriccion del sistema: No se permiten cobros correspondientes a periodos anteriores a la fecha de registro del titular (" + fechaRegistroInquilino + ").");
            return; 
        }
            
        // 5. Evaluacion de Reglas de Negocio sobre Historial Transaccional
        logic.PagoDAO pagoDaoAux = new logic.PagoDAO();
        List<model.Pago> pagosDelDueñoActual = pagoDaoAux.obtenerPagosValidos(numeroCasa, propietarioActual.getId());

        // 5.1 Algoritmo de prevencion de duplicidad de cobros
        for (model.Pago p : pagosDelDueñoActual) {
            if (p.getMes().equalsIgnoreCase(mes) && p.getYear() == anio) {
                SweetAlert.showError("Transaccion Duplicada", "El titular actual ya posee un registro de solvencia para el periodo " + mes + " " + anio + ".");
                return; 
            }
        }

        // 5.2 Algoritmo de validacion secuencial (Prevencion de saltos en meses de pago)
        List<String> mesesNombres = java.util.Arrays.asList("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre");

        int indexMesActual = mesesNombres.indexOf(mes); 
        int mesAnteriorIndex = indexMesActual - 1;
        int anioAnterior = anio;

        // Calculo del periodo inmediato anterior considerando el cambio de año (Rollback a Diciembre)
        if (mesAnteriorIndex < 0) {
            mesAnteriorIndex = 11; 
            anioAnterior--; 
        }

        String nombreMesAnterior = mesesNombres.get(mesAnteriorIndex);
        int valorMesAnteriorNumerico = mesAnteriorIndex + 1;

        java.time.LocalDate fechaMesAnterior = java.time.LocalDate.of(anioAnterior, valorMesAnteriorNumerico, 1);
        java.time.LocalDate fechaInicioCobro = fechaRegistroInquilino.withDayOfMonth(1);

        // Verificamos solvencia del mes anterior, unicamente si dicho mes recae dentro de su periodo de actividad
        if (!fechaMesAnterior.isBefore(fechaInicioCobro)) {
            boolean mesAnteriorEstaPagado = false;

            for (model.Pago p : pagosDelDueñoActual) {
                if (p.getMes().equalsIgnoreCase(nombreMesAnterior) && p.getYear() == anioAnterior) {
                    mesAnteriorEstaPagado = true;
                    break;
                }
            }
                
            if (!mesAnteriorEstaPagado) {
                SweetAlert.showError("Infraccion de Secuencia", "Operacion denegada: Existe un saldo pendiente correspondiente al periodo de " + nombreMesAnterior + " " + anioAnterior + ".");
                return; 
            }
        }
        
        // 6. Construccion del modelo y delegacion de la persistencia
        model.Pago pagoParaGuardar = new model.Pago(0, numeroCasa, propietarioActual.getId(), mes, anio, monto);
        
        logic.PagoDAO dao = new logic.PagoDAO();
        boolean exito = dao.registrarPago(pagoParaGuardar);
        
        // 7. Manejo de respuesta de transaccion y notificaciones asincronas
        if (exito) {
            // Limpieza de estado de la vista
            cmbCasas.getSelectionModel().clearSelection();
            cmbMes.getSelectionModel().clearSelection();
            cmbYear.getSelectionModel().selectFirst();
            
            if (txtNombrePropietario != null) {
                txtNombrePropietario.setText(""); 
            }

            // Integracion con servicio de mensajeria externa (SMTP)
            if (propietarioActual.getCorreo() != null && !propietarioActual.getCorreo().trim().isEmpty()) {
                String correoDestino = propietarioActual.getCorreo();
                
                logic.EmailService.enviarRecibo(correoDestino, propietarioActual.getNombre(), monto, mes, anio, numeroCasa);
                
                SweetAlert.showSuccess("Transaccion Procesada", "El pago ha sido registrado en el sistema y el comprobante digital fue despachado a " + correoDestino);
            } else {
                SweetAlert.showWarning("Registro Parcial", "Transaccion en base de datos exitosa. Sin embargo, no se pudo despachar el comprobante debido a la ausencia de un correo electronico valido.");
            }

        } else {
            SweetAlert.showError("Fallo de Persistencia", "Se produjo una interrupcion al intentar confirmar la transaccion en el servidor de base de datos.");
        }
    }
    
    /**
     * Metodo utilitario para resolver la equivalencia numerica de un mes textual.
     * * @param mesTexto Nombre del mes en formato String.
     * @return Representacion entera del mes (1-12).
     */
    private int obtenerNumeroMes(String mesTexto){
        java.util.List<String> meses = java.util.Arrays.asList(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        );
        return meses.indexOf(mesTexto) + 1;
    }       
}