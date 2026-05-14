package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;


import java.io.IOException;

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        db.Conexion.conectar();
        scene = new Scene(loadFXML("login"), 600, 500);

        stage.setTitle("Login - Sistema Administrativo");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
        stage.getIcons().add(
    new Image(getClass().getResourceAsStream("/images/icono.png"))
);
    }

    // Permite cambiar de pantalla después (te servirá luego)
    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    // MÉTODO CLAVE (corregido con tu estructura)
    private static Parent loadFXML(String fxml) throws IOException {
       FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/fxml/login.fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}