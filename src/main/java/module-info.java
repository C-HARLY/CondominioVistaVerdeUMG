module umg.vistaverdeumg {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.base;
    requires jasperreports;
    requires com.zaxxer.hikari;
    opens ui to javafx.fxml;
    exports ui;
    opens db to javafx.fxml;
    opens model to javafx.base;
}