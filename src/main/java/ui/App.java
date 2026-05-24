package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.IOException;
import java.sql.Connection;

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        
        // Al usar try-with-resources, garantizamos que la conexión se devuelva INMEDIATAMENTE
        try (Connection conn = db.Conexion.conectar()) {
            if (conn != null) {
                System.out.println(" Base de datos lista en el arranque.");
            }
        } catch (Exception e) {
            System.err.println("⚠Advertencia: No se pudo conectar a la base de datos en el arranque.");
        }

        scene = new Scene(loadFXML("login"), 600, 500);

        stage.setTitle("Login - Sistema Administrativo");
        stage.setScene(scene);
        stage.setResizable(false);
        
        // Carga segura del ícono (evita que la app truene si alguien borra la imagen)
        try {
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icono.png")));
        } catch (Exception e) {
            System.err.println("⚠️ No se encontró el ícono de la aplicación.");
        }
        
        stage.show();
    }

    // Permite cambiar de pantalla después
    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/fxml/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}