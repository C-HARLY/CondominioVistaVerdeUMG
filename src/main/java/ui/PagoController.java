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

public class PagoController implements Initializable {

    // Estas son las cajitas y listas desplegables que vemos en la pantalla
    @FXML private ComboBox<String> cmbCasas;
    @FXML private ComboBox<String> cmbMes;
    @FXML private ComboBox<Integer> cmbYear;
    @FXML private TextField txtMonto; 
    @FXML private TextField txtNombrePropietario; 

    /*
     * Este método se ejecuta automáticamente apenas abrimos la ventana de Pagos.
     * Sirve para dejar todo listo y cargado antes de que el usuario empiece a hacer clics.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarMonto();
        llenarCombosEstaticos();
        cargarCasasOcupadas();
        configurarListenerCasas(); 
    }

    /*
     * Va a la base de datos a buscar a cómo está la cuota de mantenimiento hoy.
     * Lo pone en la cajita de texto y la bloquea para que el usuario no pueda cambiar el precio.
     */
    private void configurarMonto() {
        logic.CuotaDAO dao = new logic.CuotaDAO();
        model.Cuota cuotaVigente = dao.obtenerCuota();
        
        if (cuotaVigente != null) {
            // Quitamos los decimales para que se vea como número entero
            txtMonto.setText(String.format("Q%,.0f", cuotaVigente.getMontoActual()));        
        } else {
            // Si hay un fallo de conexión, lo dejamos en 0 y mostramos un error
            txtMonto.setText("0");
            logic.SweetAlert.showError("Error de Conexión", "No se pudo obtener la cuota actual de la base de datos.");
        }
        
        txtMonto.setEditable(false); 
    }
   
    /*
     * Llena la lista desplegable de los meses y pone el año actual en la lista de años.
     */
    private void llenarCombosEstaticos() {
        cmbMes.getItems().addAll("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
                                 "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre");

        int anioActual = java.time.LocalDate.now().getYear();
        cmbYear.getItems().clear();
        cmbYear.getItems().add(anioActual); 
        cmbYear.getSelectionModel().selectFirst();
    }

    /*
     * Busca qué casas tienen dueño actualmente y las mete en la lista desplegable.
     * Así evitamos cobrarle mantenimiento a una casa que está vacía.
     */
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
    
    /*
     * Este "Listener" se queda vigilando la lista desplegable de casas.
     * Cuando el usuario elige una casa diferente, este método busca el nombre
     * del dueño y lo rellena automáticamente en la pantalla.
     */
    private void configurarListenerCasas() {
        cmbCasas.valueProperty().addListener((observable, oldValue, newValue) -> {
            // Si el usuario sí eligió una casa válida...
            if (newValue != null && !newValue.startsWith("No hay")) {
                
                // Quitamos la palabra "Casa " para quedarnos solo con el número
                int numeroCasa = Integer.parseInt(newValue.replace("Casa ", ""));
                
                // Buscamos quién es el dueño
                CasaDAO casaDao = new CasaDAO();
                Propietario prop = casaDao.obtenerPropietarioPorCasa(numeroCasa);
                
                if (prop != null) {
                    txtNombrePropietario.setText(prop.getNombre());
                } else {
                    txtNombrePropietario.setText("Sin Asignar");
                }
            } else {
                // Si desmarcan la casa, limpiamos la cajita del nombre
                if (txtNombrePropietario != null) {
                    txtNombrePropietario.setText("");
                }
            }
        });
    }
    
    /*
     * ESTE ES EL BOTÓN PRINCIPAL.
     * Se ejecuta cuando el usuario le da clic a "Registrar Pago".
     */
    @FXML
    private void registrarPago(ActionEvent event) {
        
        // 1. Extraemos todo lo que el usuario escribió o seleccionó en la pantalla
        String casaSeleccionada = cmbCasas.getValue();
        String mes = cmbMes.getValue();
        Integer anio = cmbYear.getValue();
        String montoTexto = txtMonto.getText();

        // 2. Si dejó algún espacio en blanco, lo regañamos y detenemos el proceso
        if (casaSeleccionada == null || mes == null || anio == null || montoTexto.isEmpty()) {
            SweetAlert.showWarning("Campos Incompletos", "Por favor, selecciona todos los datos requeridos en el formulario.");
            return; 
        }
        
        // 3. Preparamos los datos numéricos para poder guardarlos
        int numeroCasa = Integer.parseInt(casaSeleccionada.replace("Casa ", ""));
        double monto = Double.parseDouble(montoTexto.replace(",", "."));
        int mesSeleccionadoNum = obtenerNumeroMes(mes);

        // 4. VALIDACIÓN DE FECHA: Revisamos cuándo entró a vivir el dueño
        logic.CasaDAO casaDao = new logic.CasaDAO();
        model.Propietario propietarioActual = casaDao.obtenerPropietarioPorCasa(numeroCasa);
        
        java.time.LocalDate fechaRegistroInquilino = null;
        if (propietarioActual != null) {
            fechaRegistroInquilino = propietarioActual.getFechaRegistro();
        }

        if (fechaRegistroInquilino != null) {
            int mesRegistro = fechaRegistroInquilino.getMonthValue();
            int anioRegistro = fechaRegistroInquilino.getYear();

            // Si intentan pagar un mes que es ANTES de que el dueño llegara, lanzamos error
            if (anio < anioRegistro || (anio == anioRegistro && mesSeleccionadoNum < mesRegistro)) {
                SweetAlert.showError("Periodo Inválido", "No se pueden procesar cobros previos a la fecha de ingreso del inquilino (" + fechaRegistroInquilino + ").");
                return; 
            }
            
            // 5. VALIDACIÓN DE SECUENCIA: Evitamos que dejen meses sin pagar en medio
            List<String> mesesNombres = java.util.Arrays.asList("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre");

            int indexMesActual = mesesNombres.indexOf(mes); 
            int mesAnteriorIndex = indexMesActual - 1;
            int anioAnterior = anio;

            // Si están pagando enero, el mes anterior es diciembre del año pasado
            if (mesAnteriorIndex < 0) {
                mesAnteriorIndex = 11; 
                anioAnterior--; 
            }

            String nombreMesAnterior = mesesNombres.get(mesAnteriorIndex);
            int valorMesAnteriorNumerico = mesAnteriorIndex + 1;

            java.time.LocalDate fechaMesAnterior = java.time.LocalDate.of(anioAnterior, valorMesAnteriorNumerico, 1);
            java.time.LocalDate fechaInicioCobro = fechaRegistroInquilino.withDayOfMonth(1);

            // Si el mes anterior cae después de que se mudó, revisamos si ya lo pagó
            if (!fechaMesAnterior.isBefore(fechaInicioCobro)) {
                logic.PagoDAO pagoDaoAux = new logic.PagoDAO();
                boolean mesAnteriorEstaPagado = pagoDaoAux.verificarPagoExiste(numeroCasa, nombreMesAnterior, anioAnterior);
                
                if (!mesAnteriorEstaPagado) {
                    SweetAlert.showError("Validación de Historial", "No puedes pagar " + mes + " " + anio + " porque está pendiente el mes de " + nombreMesAnterior + " " + anioAnterior + ".");
                    return; 
                }
            }
        }

        // 6. Si no hubo ningún problema, empaquetamos el pago y lo mandamos a la base de datos
        model.Pago pagoParaGuardar = new model.Pago(0, numeroCasa, mes, anio, monto);
        logic.PagoDAO dao = new logic.PagoDAO();
        boolean exito = dao.registrarPago(pagoParaGuardar);
        
        // 7. Si se guardó correctamente, limpiamos la pantalla y enviamos el recibo
        if (exito) {
            cmbCasas.getSelectionModel().clearSelection();
            cmbMes.getSelectionModel().clearSelection();
            cmbYear.getSelectionModel().selectFirst();
            
            if (txtNombrePropietario != null) {
                txtNombrePropietario.setText(""); 
            }

            // Validamos que el dueño sí tenga un correo registrado para enviarle el recibo
            if (propietarioActual != null && propietarioActual.getCorreo() != null && !propietarioActual.getCorreo().trim().isEmpty()) {
                String correoDestino = propietarioActual.getCorreo();
                
                // AQUÍ ESTÁ EL CAMBIO: Le pasamos el nombre del propietario a nuestro servicio de correo
                logic.EmailService.enviarRecibo(correoDestino, propietarioActual.getNombre(), monto, mes, anio, numeroCasa);
                
                SweetAlert.showSuccess("¡Transacción Exitosa!", "El pago se registró y el recibo fue enviado a " + correoDestino);
            } else {
                // Si no tiene correo, lo guardamos pero le avisamos al cajero
                SweetAlert.showWarning("Pago Registrado", "El pago se guardó correctamente, pero el propietario no tiene un correo registrado para enviarle el recibo.");
            }

        } else {
            // Si la base de datos rechazó el pago (posiblemente porque intentaron pagar el mismo mes dos veces)
            SweetAlert.showError("Error de Registro", "La transacción no pudo completarse. Pago Duplicado");
        }
    } 
    
    /*
     * Pequeño método de ayuda para convertir la palabra "Enero" en el número 1, "Febrero" en 2, etc.
     */
    private int obtenerNumeroMes(String mesTexto){
        java.util.List<String> meses = java.util.Arrays.asList(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        );
        return meses.indexOf(mesTexto) + 1;
    }       
}