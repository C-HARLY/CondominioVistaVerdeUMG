module umg.vistaverdeumg {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.base;
    opens ui to javafx.fxml;
    exports ui;
    opens db to javafx.fxml;
}

