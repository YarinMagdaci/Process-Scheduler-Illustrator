package com.hit.controller;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hit.driver.MVCDriver;
import com.hit.model.Client;
import com.hit.model.Request;
import com.hit.model.Response;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class UserEditorController  {
    //client server architecture addition
    private Gson gson = new Gson();
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;
    //End of client server architecture addition
    @FXML
    private TextField usernameField;
    @FXML
    private Button startButton;

    private String m_UserName;
    private static final int MAX_RECONNECTIONS = 5;

    @FXML
    public void initialize(){
        try
        {
            ConnectionThread connectionThread = new ConnectionThread();
            Thread connection = new Thread(connectionThread);
            connection.start();
            connection.join();
            if(!connectionThread.getConnected())
            {
                Platform.exit();
            }
            this.usernameField.setDisable(false);
            this.startButton.setDisable(false);
        } catch (InterruptedException e) {
            System.out.println("There was an error.\n");
        }
    }

    @FXML
    private void startButtonPressed(ActionEvent event) throws IOException {
        if(usernameField.getText().isEmpty())
        {
            drawErrorUserNameIsMissing();
        }
        else
        {
            JsonObject jsonObject = new JsonObject();
            this.m_UserName = this.usernameField.getText();
            jsonObject.addProperty("userName", m_UserName);
            Request newReq = new Request("GET", "/newClient", null, jsonObject);
            out.println(gson.toJson(newReq));
            out.flush();

            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String res = in.readLine();
            Response resObj = gson.fromJson(res, Response.class);
            if(resObj == null || resObj.getStatusCode() != 200)
            {
                this.PopUpServerErrorOccured();
                return;
            }
            DrawMainAppUI(event);
        }
    }

    private void drawErrorUserNameIsMissing()
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Username is missing");
        alert.setContentText("Please enter a username and try again.");
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    private void DrawMainAppUI(ActionEvent event) throws IOException
    {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hit/view/illustratorUI.fxml"));
        Parent root = loader.load();
        IllustratorController controller = loader.getController();
        controller.setUserName(this.m_UserName);
        controller.setWelcomeString(String.format("Welcome %s to the process scheduler program.", usernameField.getText()));
        //client server architecture addition
        controller.setSocket(this.socket);
        //End of client server architecture addition
        controller.setUserName(this.m_UserName);
        Stage stage = (Stage) startButton.getScene().getWindow();
        //client server architecture addition
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                System.out.println("i AM AT handleWindowClose");
                controller.handleWindowClose();
            }
        });
        //End of client server architecture addition
        stage.setScene(new Scene(root));
        stage.getScene().getStylesheets().add(MVCDriver.class.getResource("/com/hit/view/stylesheet.css").toExternalForm());
        stage.show();
    }

    @FXML
    private void cancelButtonPressed(ActionEvent event) {
        // exit the app.
        this.handleWindowClose();
        Platform.exit();
    }

    private void PopUpServerErrorOccured()
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Server Error!");
        alert.setContentText("There has been error, please try again later.");
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }
    //client server architecture addition
    /* Clicking on 'X' of the first JAVAFX Window fires up this function */
    public void handleWindowClose()
    {
        if(!socket.isConnected() || socket.isClosed())
        {
            System.out.println("Socket is already closed.");
            return;
        }
        try {
            //OLD
//            Request newReq = new Request("GET", "/quit", null, null);
//            out.println(gson.toJson(newReq));
//            out.flush();
//
//            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//            String res = in.readLine();
//            Response resObj = gson.fromJson(res, Response.class);
            //NEW
            Response resObj = Client.sendRequestandGetResponse(this.socket, this.out, this.in, "GET", "/quit", null, null);
            if(resObj != null && resObj.getStatusCode() == 200)
            {
                this.socket.close();
                System.out.println("Closed the connection mate.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    //End of client server architecture addition
    private class ConnectionThread implements Runnable{
        private boolean m_Connected = false;
        private int m_CounterReconnections = 0;
        @Override
        public void run() {
            try {
                socket = new Socket("localhost", 2001);
                out = new PrintWriter(socket.getOutputStream());
                if(!socket.isClosed() && socket.isConnected()){
                    this.m_Connected = true;
                }
            } catch (IOException e) {
                System.out.println("ERROR: Connection failure. Retrying to connect in 3 seconds.");
                try {
                    Thread.sleep(3000);
                    this.m_CounterReconnections++;
                    if(this.m_CounterReconnections >= MAX_RECONNECTIONS)
                    {
                        System.out.println("ERROR: There was a failure in connecting to the server. Please try again later.\n");
                    }
                    else{
                        this.run();
                    }
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        public boolean getConnected() {return this.m_Connected;}
    }
}