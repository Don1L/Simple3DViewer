module com.cgvsu {
    requires javafx.controls;
    requires javafx.fxml;
    requires vecmath;
    requires java.desktop;


    opens com.cgvsu to javafx.fxml;
    exports com.cgvsu;
    exports com.cgvsu.objwriter;
    opens com.cgvsu.objwriter to javafx.fxml;
    exports com.cgvsu.model;
    opens com.cgvsu.model to javafx.fxml;
}