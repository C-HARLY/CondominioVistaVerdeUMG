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
import model.Configuracion;
import logic.CasaDAO;

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
        double montoActual = Configuracion.cuotaMantenimiento;
        txtMonto.setText(String.format("%.2f", montoActual));
        txtMonto.setEditable(false); // Recomendado para que no alteren el precio
    }

    private void llenarCombosEstaticos() {
        // 1. Llenar Meses 
        cmbMes.getItems().addAll("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
                                 "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre");

        // 2. Obtener el año real del sistema
        int anioActual = java.time.LocalDate.now().getYear();

        // 3. Limpiar y agregar SOLO el año actual
        cmbYear.getItems().clear();
        cmbYear.getItems().add(anioActual); 

        // 4. Dejarlo seleccionado por defecto
        cmbYear.getSelectionModel().selectFirst();
    }

    private void cargarCasasOcupadas() {
        // Aquí conectamos con la base de datos a través del DAO
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
    private void volverAlMenu(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/MenuPrincipal.fxml")); 
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen(); 
            stage.show();
        } catch (IOException e) {
            System.err.println("Error al regresar: " + e.getMessage());
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
            System.out.println("Error: Faltan datos por seleccionar.");
            return; 
        }
        
        // 3. Limpiar los datos para poder hacer validaciones numéricas
        int numeroCasa = Integer.parseInt(casaSeleccionada.replace("Casa ", ""));
        double monto = Double.parseDouble(montoTexto.replace(",", "."));
        int mesSeleccionadoNum = obtenerNumeroMes(mes);


        //. Validar que no sea un mes pasado respecto a la fecha actual
        java.time.LocalDate hoy = java.time.LocalDate.now();
        int mesActual = hoy.getMonthValue();
        int anioActual = hoy.getYear();

        if (anio < anioActual || (anio == anioActual && mesSeleccionadoNum < mesActual)) {
            System.out.println("Error: No puedes realizar pagos de meses que ya pasaron.");
            return; // Detenemos la ejecución antes de guardar
        }

        //  Validar fecha de registro del propietario
        logic.CasaDAO casaDao = new logic.CasaDAO();
        java.time.LocalDate fechaRegistroInquilino = casaDao.obtenerFechaRegistroPropietario(numeroCasa);

        if (fechaRegistroInquilino != null) {
            int mesRegistro = fechaRegistroInquilino.getMonthValue();
            int anioRegistro = fechaRegistroInquilino.getYear();

            if (anio < anioRegistro || (anio == anioRegistro && mesSeleccionadoNum < mesRegistro)) {
                System.out.println("Error: No puedes cobrar meses anteriores a la llegada del propietario (" + fechaRegistroInquilino + ").");
                return; // Detenemos la ejecución antes de guardar
            }
        }

        // 4. Si pasó todas las validaciones, creamos el objeto Pago
        model.Pago pagoParaGuardar = new model.Pago(0, numeroCasa, mes, anio, monto);

        // 5. Mandar a guardar usando nuestro DAO
        logic.PagoDAO dao = new logic.PagoDAO();
        boolean exito = dao.registrarPago(pagoParaGuardar);
        
        // 6. Confirmación final
        if (exito) {
            System.out.println("¡Transacción exitosa! El pago se ha guardado.");
            // Limpiamos la pantalla
            cmbCasas.getSelectionModel().clearSelection();
            cmbMes.getSelectionModel().clearSelection();
            cmbYear.getSelectionModel().selectFirst();
        } else {
            System.out.println("Error: El pago no pudo ser procesado. Es probable que ya esté pagado.");
        }
    }
    
    
    private int obtenerNumeroMes(String mesTexto) {
        java.util.List<String> meses = java.util.Arrays.asList(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        );
        return meses.indexOf(mesTexto) + 1; // Enero será 1, Febrero 2, etc.
    }
 }