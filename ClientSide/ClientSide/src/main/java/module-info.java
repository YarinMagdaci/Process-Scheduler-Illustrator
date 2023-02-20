module com.example.clientside {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;

    exports com.hit.model;
    opens com.hit.model to javafx.fxml;
    exports com.hit.driver;
    opens com.hit.driver to javafx.fxml;
    exports com.hit.controller;
    opens com.hit.controller to javafx.fxml;
}