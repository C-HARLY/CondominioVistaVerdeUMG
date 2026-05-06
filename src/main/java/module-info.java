module umg.vistaverdeumg {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    opens ui to javafx.fxml;
    exports ui;
    opens db to javafx.fxml;
}

