/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logic;

import javafx.scene.control.Alert;
import javafx.stage.StageStyle;
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
        } catch (Exception e) {
            System.out.println("Nota: No se encontró el archivo CSS de alertas, usando estilo por defecto.");
        }

        // 5. Mostramos la alerta y bloqueamos la pantalla hasta que el usuario le de Aceptar
        alerta.showAndWait();
    }
}
