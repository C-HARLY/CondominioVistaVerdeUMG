package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {

        Parent root = loadFXML("login");

        scene = new Scene(root);
        
        stage.setTitle("Login - Sistema Administrativo");

        stage.setScene(scene);

        stage.setResizable(false);

        stage.sizeToScene();

        stage.centerOnScreen();

        // =========================
        // ICONO DE LA APLICACIÓN
        // =========================

        try {

            stage.getIcons().add(
                    new Image(
                            getClass().getResourceAsStream(
                                    "/images/icono.png"
                            )
                    )
            );

        } catch (Exception e) {

            System.err.println(
                    "⚠️ No se encontró el ícono de la aplicación."
            );
        }

        stage.show();
    }

    // =========================
    // CAMBIO DE VISTAS
    // =========================

    public static void setRoot(String fxml) throws IOException {

        scene.setRoot(loadFXML(fxml));
    }

    // =========================
    // CARGAR FXML
    // =========================

    private static Parent loadFXML(String fxml) throws IOException {

        FXMLLoader fxmlLoader =
                new FXMLLoader(
                        App.class.getResource(
                                "/ui/" + fxml + ".fxml"
                        )
                );

        return fxmlLoader.load();
    }

    public static void main(String[] args) {

        launch();
    }
}