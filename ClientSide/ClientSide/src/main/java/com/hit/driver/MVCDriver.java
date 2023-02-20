package com.hit.driver;

import com.hit.controller.UserEditorController;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

public class MVCDriver extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MVCDriver.class.getResource("/com/hit/view/hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        UserEditorController controller = fxmlLoader.getController();
        scene.getStylesheets().add(MVCDriver.class.getResource("/com/hit/view/stylesheet.css").toExternalForm());
        //If a client leaves just in the first window, it verifies it's connection is closed.
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                System.out.println("I am at handleWindowClose");
                controller.handleWindowClose();
            }
        });
        stage.setScene(scene);
        scene.setFill(Color.valueOf("#F5F5F5"));
        stage.setResizable(false);
        stage.setTitle("Process Scheduler Illustrator");
        stage.setWidth(600);
        stage.setHeight(400);
        stage.show();
    }
    public static void main(String[] args) throws IOException {
        Application.launch(MVCDriver.class);
    }
}