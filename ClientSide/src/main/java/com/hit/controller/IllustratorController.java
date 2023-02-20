package com.hit.controller;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hit.model.Client;
import com.hit.model.Pair;
import com.hit.model.Request;
import com.hit.model.Response;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class IllustratorController {
    private Gson gson = new Gson();
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;
    private boolean firstTimePressedAdd = false;
    @FXML
    private Button startButton;
    @FXML
    private Button newButton;
    @FXML
    private Button loadButton;
    @FXML
    private Button addButton;
    @FXML
    private Button saveButton;
    @FXML
    private Label enterCPUTimeLabel;
    @FXML
    private TextField addTextField;
    @FXML
    private Label welcomeLabel;
    private int m_NumOfCols;
    private final int MAX_NUM_OF_COLS = 6;
    @FXML
    private ProgressIndicator colOneProgressIndicator;
    @FXML
    private ProgressIndicator colTwoProgressIndicator;

    @FXML
    private ProgressIndicator colThreeProgressIndicator;

    @FXML
    private ProgressIndicator colFourProgressIndicator;

    @FXML
    private ProgressIndicator colFiveProgressIndicator;

    @FXML
    private ProgressIndicator colSixProgressIndicator;

    @FXML
    private Separator firstSeparator;
    @FXML
    private Separator secondSeparator;
    @FXML
    private Separator thirdSeparator;
    @FXML
    private Separator fourthSeparator;
    @FXML
    private Separator fifthSeparator;
    @FXML
    private Separator sixthSeparator;
    @FXML
    private Label colOneLabel;
    @FXML
    private Label colTwoLabel;

    @FXML
    private Label colThreeLabel;

    @FXML
    private Label colFourLabel;

    @FXML
    private Label colFiveLabel;

    @FXML
    private Label colSixLabel;

    @FXML
    private LinkedList<ProgressIndicator> progressIndicators;

    @FXML
    private LinkedList<Label> colLabels;

    @FXML
    private LinkedList<Separator> verticalSeparators;

    @FXML
    private ListView listView;

    @FXML
    private Separator horizontalSeperator;
    @FXML
    private Label instructionsLabel;

    private LinkedList<Integer> listOfCPUTimeNeeded;

    private String m_UserName;

    private int progressIndex = 0;
    private int quantom = 2;
    private LinkedList<Pair> listOfProgresses;

    private ProgressIndicator currentProgressIndicator;

    public void setSocket(Socket newSocket)
    {
        this.socket = newSocket;
        try {
            this.out = new PrintWriter(this.socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void initialize()
    {
        this.SetToolTips();
    }

    private void SetToolTips()
    {
        Tooltip newButtonToolTip = new Tooltip("Create new set of processes to run on the illustrator.");
        Tooltip startButtonToolTip = new Tooltip("Start running the process scheduler illustrator.");
        Tooltip loadButtonToolTip = new Tooltip("Load old set of processes you created already to run on the illustrator.");
        Tooltip.install(this.newButton, newButtonToolTip);
        Tooltip.install(this.startButton, startButtonToolTip);
        Tooltip.install(this.loadButton, loadButtonToolTip);
    }
    //Start button of the illustrator application:
    @FXML
    private void OnStartButtonOfIllustratorPressed(ActionEvent event) throws IOException {
        Response resObj = Client.sendRequestandGetResponse(this.socket, this.out, this.in, "POST", "/startProcessScheduling",null, null);
        if(resObj == null || (resObj.getStatusCode() != 201 && resObj.getStatusCode() != 202))
        {
            this.PopUpServerErrorOccured();
            return;
        }
        this.saveButton.setDisable(true);
        this.disableOrEnableButtons(true);
        if(resObj.statusCode == 201)
        {
            this.welcomeLabel.setText("Running the processes using FIFO Process Scheduler.");
            this.welcomeLabel.setTextFill(Color.WHITE);
            animateFIFOProgressIndicator(0);
        }
        else
        {
            this.welcomeLabel.setText("Running the processes using Round Robin Process Scheduler.");
            this.welcomeLabel.setTextFill(Color.WHITE);
            startAnimationRoundRobin();
        }
    }

    private void disableOrEnableButtons(boolean disOrEn)
    {
        this.newButton.setDisable(disOrEn);
        this.addButton.setDisable(disOrEn);
        this.startButton.setDisable(disOrEn);
        this.loadButton.setDisable(disOrEn);
    }

    // Define the animation as a method so we can call it recursively
    private void animateFIFOProgressIndicator(int index) {
        this.colLabels.get(index).setTextFill(Color.RED);
        ProgressIndicator currentProgressIndicator = this.progressIndicators.get(index);
        int currentCPUTimeNeeded = this.listOfCPUTimeNeeded.get(index);
        // Create a timeline to animate the progress value
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(currentProgressIndicator.progressProperty(), 0.0)),
                new KeyFrame(Duration.millis(currentCPUTimeNeeded*1000), new KeyValue(currentProgressIndicator.progressProperty(), 1.0))
        );
        // Set the callback to start the next animation when this one is finished
        timeline.setOnFinished(event -> {
            // Increment the index and check if we're done
            this.colLabels.get(progressIndex).setTextFill(Color.GREEN);
            progressIndex++;
            if (progressIndex < this.m_NumOfCols) {
                // If we're not done, start the next animation
                disableOrEnableButtons(true);
                animateFIFOProgressIndicator(progressIndex);
            }
            else{
                disableOrEnableButtons(false);
            }
        });
        // Start the animation
        timeline.play();
    }

    private void startAnimationRoundRobin() {
        if (this.listOfProgresses.isEmpty()) {
            disableOrEnableButtons(false);
            return;
        }
        this.progressIndex = this.listOfProgresses.get(0).getIndex();
        this.colLabels.get(progressIndex).setTextFill(Color.RED);
        currentProgressIndicator = this.progressIndicators.get(progressIndex);
        int runTime = Math.min(quantom, this.listOfProgresses.get(0).getTimeNeeded());
        double progressValAfterFilling = Math.min((double)runTime/(double)this.listOfProgresses.get(0).getTimeNeeded(), 1.0);
        //Creating TimeLine:
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(currentProgressIndicator.progressProperty(), currentProgressIndicator.getProgress())),
                new KeyFrame(Duration.millis(runTime*1000), new KeyValue(currentProgressIndicator.progressProperty(), progressValAfterFilling))
        );
        timeline.setOnFinished(event -> {
            if(currentProgressIndicator.getProgress() >= 1.0)//DONE
            {
                this.listOfCPUTimeNeeded.remove(0);
                this.colLabels.get(progressIndex).setTextFill(Color.GREEN);
                this.listOfProgresses.remove(0);
            }
            else//NOT DONE YET
            {
                this.colLabels.get(progressIndex).setTextFill(Color.ORANGE);
                Pair newPair = this.listOfProgresses.remove(0);
                newPair.setTimeNeeded(newPair.getTimeNeeded() - runTime);
                this.listOfProgresses.add(newPair);
            }
            disableOrEnableButtons(true);
            // Start the next animation
            startAnimationRoundRobin();
        });
        timeline.play();
    }

    @FXML
    private void onSaveButtonPressed(ActionEvent event) throws IOException{
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("userName", this.m_UserName);
        Response resObj = Client.sendRequestandGetResponse(this.socket, this.out, this.in, "PUT", "/saveSetOfProcesses",null, jsonObject);
        if(resObj == null || resObj.getStatusCode() != 200)
        {
            this.instructionsLabel.setText(String.format("Dear %s, there was a failure in saving your queue of processes.", this.m_UserName));
            this.instructionsLabel.setTextFill(Color.RED);
        }
        else
        {
            this.instructionsLabel.setTextFill(Color.GREEN);
            this.instructionsLabel.setText(String.format("Dear %s, your queue of processes has been saved!", this.m_UserName));
        }
    }

    @FXML
    private void OnLoadButtonPressed(ActionEvent event) throws IOException {
        this.instructionsLabel.setText("");
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("userName", this.m_UserName);
        Request newReq = new Request("GET", "/loadSetOfProcesses", null, jsonObject);
        out.println(gson.toJson(newReq));
        out.flush();

        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String res = in.readLine();
        Response resObj = gson.fromJson(res, Response.class);
        while(resObj!=null && res!=null)
        {
            if(resObj.getStatusCode() == 404 || resObj.getStatusCode() == 400)
            {
                this.instructionsLabel.setTextFill(Color.RED);
                this.instructionsLabel.setText(String.format("Dear %s, we have not found any queue of processes under your name in our database.", this.m_UserName));
                return;
            }
            //meaning read the process
            //if it enters the if it's the first time and it's initializing the JAVAFX objects to see on the UI
            //regarding all the processes.

            if(m_NumOfCols == MAX_NUM_OF_COLS - 1)
            {
                this.addButton.setDisable(true);
                this.addTextField.setDisable(true);
                this.enterCPUTimeLabel.setDisable(true);
            }

            if(firstTimePressedAdd == false)
            {
                this.m_NumOfCols = 0;
                this.firstTimePressedAdd = true;
                this.startButton.setDisable(false);
                this.initializeListOfProgressIndicators();
                this.initializeListOfColLabels();
                this.initializeListOfSeparators();
                this.listView.setVisible(true);
                this.horizontalSeperator.setVisible(true);
                this.saveButton.setDisable(false);
                this.listOfProgresses = new LinkedList<>();
                this.listOfCPUTimeNeeded = new LinkedList<>();
            }
            this.listOfCPUTimeNeeded.add(resObj.getBody().get("CPUTimeNeeded").getAsInt());
            this.listOfProgresses.add(new Pair(m_NumOfCols, resObj.getBody().get("CPUTimeNeeded").getAsInt()));
            AddNewColToTable("P" + resObj.getBody().get("id").getAsInt());
            System.out.println("Adding P" + resObj.getBody().get("id").getAsInt() + " with: " + resObj.getBody().get("CPUTimeNeeded").getAsInt() + " time requested.");
            res = in.readLine();
            resObj = gson.fromJson(res, Response.class);
        }

    }

    @FXML
    private void OnNewButtonPressed(ActionEvent event) throws IOException {
        listOfCPUTimeNeeded = new LinkedList<>();
        this.welcomeLabel.setText("");
        this.instructionsLabel.setText("");
        this.listOfProgresses = new LinkedList<>();
        Response resObj = Client.sendRequestandGetResponse(this.socket, this.out, this.in, "GET", "/newSetOfProcesses", null, null);
        if(resObj == null || resObj.getStatusCode() != 200)
        {
            this.PopUpServerErrorOccured();
            return;
        }

        this.addButton.setVisible(true);
        this.addButton.setDisable(false);
        this.enterCPUTimeLabel.setVisible(true);
        this.enterCPUTimeLabel.setDisable(false);
        this.addTextField.setVisible(true);
        this.addTextField.setDisable(false);
        this.horizontalSeperator.setPrefWidth(58);
        this.horizontalSeperator.setVisible(false);
        this.listView.setPrefWidth(58);
        this.listView.setVisible(false);
        this.firstTimePressedAdd = false;
        if(this.verticalSeparators != null && this.verticalSeparators.size() != 0)
        {
            for (Separator currentVerticalSeparator: this.verticalSeparators) {
                currentVerticalSeparator.setVisible(false);
            }
        }
        if(this.colLabels != null && this.colLabels.size() != 0)
        {
            for (Label currentColLabel: this.colLabels) {
                currentColLabel.setVisible(false);
            }
        }
        if(this.progressIndicators != null && this.progressIndicators.size() != 0)
        {
            for (ProgressIndicator currentProgressIndicator: this.progressIndicators) {
                currentProgressIndicator.setVisible(false);
            }
        }
        this.m_NumOfCols = 0;
    }

    @FXML
    private void OnAddButtonPressed(ActionEvent event) throws IOException {
        int currentCPUTimeRequestedInteger;
        String currentCPUTimeRequested = this.addTextField.getText();
        if(currentCPUTimeRequested.isEmpty() || currentCPUTimeRequested.length() > 1 || !Character.isDigit(currentCPUTimeRequested.toCharArray()[0]) || currentCPUTimeRequested.toCharArray()[0] == '0')
        {
            PopAddButtonError();
            return;
        }
        if(m_NumOfCols == MAX_NUM_OF_COLS - 1)
        {
            this.addButton.setDisable(true);
            this.addTextField.setDisable(true);
            this.enterCPUTimeLabel.setDisable(true);
        }
        if(firstTimePressedAdd == false)
        {
            this.firstTimePressedAdd = true;
            this.startButton.setDisable(false);
            this.initializeListOfProgressIndicators();
            this.initializeListOfColLabels();
            this.initializeListOfSeparators();
            this.listView.setVisible(true);
            this.horizontalSeperator.setVisible(true);
            this.saveButton.setDisable(false);
        }
        currentCPUTimeRequestedInteger = Integer.parseInt(currentCPUTimeRequested);
        this.listOfCPUTimeNeeded.add(currentCPUTimeRequestedInteger);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("CPUTimeNeeded", currentCPUTimeRequestedInteger);
        Response resObj = Client.sendRequestandGetResponse(this.socket, this.out, this.in, "POST", "/addNewProcess", null, jsonObject);
        if(resObj == null || resObj.statusCode != 200){
            PopUpServerErrorOccured();
            return;
        }

        this.listOfProgresses.add(new Pair(m_NumOfCols, currentCPUTimeRequestedInteger));
        this.addTextField.setText("");
        AddNewColToTable("P" + resObj.getBody().get("id").getAsInt());
    }

    private void AddNewColToTable(String newProcessId)
    {
        ProgressIndicator currentProgressIndicator = this.progressIndicators.get(this.m_NumOfCols);
        currentProgressIndicator.setVisible(true);
        Label currentColLabel = this.colLabels.get(this.m_NumOfCols);
        currentColLabel.setText(newProcessId);
        currentColLabel.setVisible(true);
        Separator currentSeparator = this.verticalSeparators.get(m_NumOfCols);
        currentSeparator.setVisible(true);
        this.m_NumOfCols++;
        if(m_NumOfCols != 1)
        {
            horizontalSeperator.setPrefWidth(horizontalSeperator.getPrefWidth() + 63);
            listView.setPrefWidth(listView.getPrefWidth() + 63);
        }
    }

    private void initializeListOfProgressIndicators()
    {
        this.progressIndicators = new LinkedList<>();
        this.progressIndicators.add(colOneProgressIndicator);
        this.progressIndicators.add(colTwoProgressIndicator);
        this.progressIndicators.add(colThreeProgressIndicator);
        this.progressIndicators.add(colFourProgressIndicator);
        this.progressIndicators.add(colFiveProgressIndicator);
        this.progressIndicators.add(colSixProgressIndicator);
    }

    private void initializeListOfColLabels()
    {
        this.colLabels = new LinkedList<>();
        this.colLabels.add(colOneLabel);
        this.colLabels.add(colTwoLabel);
        this.colLabels.add(colThreeLabel);
        this.colLabels.add(colFourLabel);
        this.colLabels.add(colFiveLabel);
        this.colLabels.add(colSixLabel);
    }

    private void initializeListOfSeparators()
    {
        this.verticalSeparators = new LinkedList<>();
        this.verticalSeparators.add(firstSeparator);
        this.verticalSeparators.add(secondSeparator);
        this.verticalSeparators.add(thirdSeparator);
        this.verticalSeparators.add(fourthSeparator);
        this.verticalSeparators.add(fifthSeparator);
        this.verticalSeparators.add(sixthSeparator);
    }
    private void PopAddButtonError()
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Digits Only!");
        alert.setContentText("Please enter a digit between 1-9 represents the CPU Time requested for this process.");
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
        alert.showAndWait();
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

    public void setUserName(String i_UserName)
    {
        this.m_UserName = i_UserName;
    }

    public void setWelcomeString(String i_WelcomeString)
    {
        this.welcomeLabel.setText(i_WelcomeString);
    }

    public boolean isThisClientStillConnectedToServer(){
        return (socket.isConnected() && !socket.isClosed());
    }


    /* Clicking on 'X' of the first JAVAFX Window fires up this function */
    public void handleWindowClose()
    {
        if(!this.isThisClientStillConnectedToServer())
        {
            System.out.println("Socket is already closed.");
            return;
        }
        try {
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

}