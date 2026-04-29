module umg.vistaverdeumg {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;

   // Esto permite que Scene Builder y JavaFX vean tus controladores
    opens ui to javafx.fxml; 
    
    // Esto permite que el sistema ejecute la clase App
    exports ui;
}
