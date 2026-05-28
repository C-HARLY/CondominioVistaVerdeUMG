package logic;

import javafx.scene.control.Alert;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.Stage;

/**
 * Clase utilitaria para la gestión estandarizada de cuadros de diálogo (Alertas) en JavaFX.
 * 
 * a lo largo de la aplicación. Centraliza la inyección de estilos CSS, la remoción 
 * de decoraciones del sistema operativo (OS) y el cálculo de posicionamiento en pantalla, 
 * garantizando una experiencia de usuario (UX) uniforme en todo el sistema.
 *
 */
public class SweetAlert {
    
    /**
     * Despliega un modal informativo de éxito.
     * Utilizado para confirmar transacciones completadas (ej. Pago registrado, Propietario guardado).
     *
     * @param titulo  El encabezado principal de la alerta.
     * @param mensaje El texto descriptivo con el detalle de la operación exitosa.
     */
    public static void showSuccess(String titulo, String mensaje) {
        crearAlerta(Alert.AlertType.INFORMATION, titulo, mensaje);
    }

    /**
     * Despliega un modal crítico de error.
     * Utilizado para notificar excepciones, fallos de red o violaciones a las reglas de negocio 
     * (ej. Casa ya ocupada, Base de datos desconectada).
     *
     * @param titulo  El encabezado principal del error.
     * @param mensaje El detalle técnico o funcional de la falla.
     */
    public static void showError(String titulo, String mensaje) {
        crearAlerta(Alert.AlertType.ERROR, titulo, mensaje);
    }

    /**
     * Despliega un modal preventivo de advertencia.
     * Utilizado para situaciones no críticas que requieren la atención del usuario.
     *
     * @param titulo  El encabezado principal de la advertencia.
     * @param mensaje El texto descriptivo de la situación.
     */
    public static void showWarning(String titulo, String mensaje) {
        crearAlerta(Alert.AlertType.WARNING, titulo, mensaje);
    }

    /**
     * Motor interno de renderizado de alertas.
     * Construye, estiliza y bloquea el hilo principal (Thread) de la interfaz gráfica 
     * hasta que el usuario interaccione con el modal.
     * 
     * Se utiliza un listener sobre el evento {@code setOnShown} 
     * para forzar el centrado de la ventana. Esto mitiga un comportamiento nativo de JavaFX 
     * donde las dimensiones finales de un nodo modificado por CSS no se calculan correctamente 
     * antes de ser mostrado en pantalla.
     * 
     * @param tipo    El enumerador nativo de JavaFX que define el icono base de la alerta.
     * @param titulo  El texto de la cabecera.
     * @param mensaje El contenido del cuerpo.
     */
    
    
    private static void crearAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        
        // Limpieza visual: Remueve el header nativo por defecto para diseños más limpios
        alerta.setHeaderText(null); 
        alerta.setContentText(mensaje);
        
        // Se aplica un estilo utilitario para ocultar los controles nativos de ventana (Min/Max/Close de Windows/Mac)
        alerta.initStyle(StageStyle.UTILITY); 
        
        // Inyección dinámica de hojas de estilo (CSS)
        try {
            alerta.getDialogPane().getStylesheets().add(
                Alert.class.getResource("/css/alertas.css").toExternalForm()
            );
            alerta.getDialogPane().getStyleClass().add("condo-alert"); 
        } catch (Exception e) {
            System.err.println(" Advertencia de UI (SweetAlert): No se pudo inyectar alertas.css. Renderizando con estilo por defecto.");
        }
        
        // Corrección de posicionamiento post-renderizado
        Window window = alerta.getDialogPane().getScene().getWindow();
        if (window instanceof Stage) {
            Stage stage = (Stage) window;
            stage.setOnShown(event -> {
                stage.centerOnScreen();
            });
        }

        // Bloquea la interacción con la ventana padre hasta que esta alerta sea descartada
        alerta.showAndWait();
    }
}