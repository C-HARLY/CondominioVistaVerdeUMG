package logic;

import javafx.scene.control.Alert;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.Stage;

/**
 *
 * @author carlo
 */
public class SweetAlert {
    // Método para alertas verdes/exitosas
    public static void showSuccess(String titulo, String mensaje) {
        crearAlerta(Alert.AlertType.INFORMATION, titulo, mensaje);
    }

    // Método para alertas rojas/errores
    public static void showError(String titulo, String mensaje) {
        crearAlerta(Alert.AlertType.ERROR, titulo, mensaje);
    }

    // Método para alertas amarillas/advertencias
    public static void showWarning(String titulo, String mensaje) {
        crearAlerta(Alert.AlertType.WARNING, titulo, mensaje);
    }

    // ⚙️ EL MOTOR PRIVADO: Aquí ocurre la magia
    private static void crearAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        
        // 1. Esto elimina la horrible franja gris de arriba
        alerta.setHeaderText(null); 
        
        // 2. Ponemos tu mensaje
        alerta.setContentText(mensaje);
        
        // 3. Esto quita los botones de minimizar/maximizar y cerrar de Windows
        alerta.initStyle(StageStyle.UTILITY); 
        
        // 4. Conectamos el CSS para que se vea moderno
        try {
            alerta.getDialogPane().getStylesheets().add(
                Alert.class.getResource("/css/alertas.css").toExternalForm()
            );
            //Inyectamos la clase de tu CSS
            alerta.getDialogPane().getStyleClass().add("condo-alert"); 
        } catch (Exception e) {
            System.out.println("Nota: No se encontró el archivo CSS de alertas, usando estilo por defecto.");
        }
        
        //forzamos que salga en el centro de la pantalla
        Window window = alerta.getDialogPane().getScene().getWindow();
        if (window instanceof Stage) {
            Stage stage = (Stage) window;
            // Para que el centro se calcule bien después de aplicar el CSS
            stage.setOnShown(event -> {
                stage.centerOnScreen();
            });
        }

        // 5. Mostramos la alerta y bloqueamos la pantalla hasta que el usuario le de Aceptar
        alerta.showAndWait();
    }
}