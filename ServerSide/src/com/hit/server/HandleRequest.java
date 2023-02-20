package com.hit.server;

import com.google.gson.JsonObject;
import com.hit.controller.FileSystemController;
import com.hit.controller.ProcessSchedulerController;
import com.yarin.myprocesspackage.Process;

import java.util.LinkedList;
import java.util.Queue;

public class HandleRequest implements Runnable {
    private Response m_Response;
    private Request m_Request;

    private int m_ClientThreadTask = 0;
    private boolean m_WantsToRemainConnected = true;

    private Process newProcess;
    private static FileSystemController fileSystemController = new FileSystemController();
    private ProcessSchedulerController processSchedulerController;
    private Queue<Process> m_QueueOfProcesses;

    public HandleRequest(Request i_NewRequest) {
        this.m_Request = i_NewRequest;
    }

    public void run() {
        String method = this.m_Request.getMethod();
        switch (method) {
            case "GET":
                this.HandleGetMethod();
                break;
            case "POST":
                this.HandlePostMethod();
                break;
            case "PUT":
                this.HandlePutMethod();
        }
    }

    public void HandleGetMethod() {
        String path = this.m_Request.getPath();
        switch (path) {
            case "/quit":
                this.HandleGetMethodQuit();
                break;
            case "/newClient":
                this.HandleGetMethodNewClient();
                break;
            case "/newSetOfProcesses":
                this.HandleGetMethodNewSetOfProcesses();
                break;
            case "/loadSetOfProcesses":
                this.HandleGetMethodLoadSetOfProcesses();
                break;
            default:
                break;
        }
    }

    public void HandlePostMethod() {
        String path = this.m_Request.getPath();
        switch (path) {
            case "/addNewProcess":
                this.HandlePostMethodAddNewProcess();
                break;
            case "/startProcessScheduling":
                this.HandlePostMethodStartProcessScheduling();
                break;
            default:
                break;
        }
    }

    public void HandlePutMethod() {
        String path = this.m_Request.getPath();
        switch (path) {
            case "/saveSetOfProcesses":
                this.HandlePutMethodSaveProcessScheduling();
                break;
            default:
                break;
        }
    }

    public void HandleGetMethodQuit() {
        System.out.println("I am in getMethod Quit.");
        this.m_WantsToRemainConnected = false;
        this.m_ClientThreadTask = 0;
        this.m_Response = new Response(200, null, null);
    }

    public void HandleGetMethodNewClient() {
        this.m_ClientThreadTask = 1; //1 meaning to set new UserName
        this.m_Response = new Response(200, null, null);
    }

    public void HandleGetMethodNewSetOfProcesses() {
        this.m_ClientThreadTask = 2; //2 means to allocate new LinkedList of processes for this current client.
        this.m_Response = new Response(200, null, null);
    }

    public void HandleGetMethodLoadSetOfProcesses() {
        this.m_ClientThreadTask = 3; //3 meaning to load set of processes based by username he provided us in the beginning
        String userName = this.m_Request.getParameters().get("userName").getAsString();
        if (userName == null) {
            this.m_Response = new Response(400, null, null);
            return;
        }
        //Using FileSystemController to load from file:
        this.m_QueueOfProcesses = fileSystemController.loadQueue(userName);
        if (this.m_QueueOfProcesses == null) {
            this.m_Response = new Response(404, null, null);
            return;
        }
        this.m_Response = new Response(200, null, null);
    }

    /*public void HandleGetMethodLoadSetOfProcesses() {
        this.m_ClientThreadTask = 3; //3 meaning to load set of processes based by username he provided us in the beginning
        //TODO
        //Load the data from the database using FileSystemController
        if (this.m_Request.getParameters().get("userName").getAsString() == null) {
            this.m_Response = new Response(400, null, null);
            return;
        }
        String userName = this.m_Request.getParameters().get("userName").getAsString();

        //then use the FileSystemController to load it by userName we gained
        Queue<Process> loadedQueue = fileSystemController.loadQueue(userName);
        if (loadedQueue == null) {
            this.m_Response = new Response(404, null, null);//Not found that queue.
            return;
        }
        JsonObject jsonObject = new JsonObject();
        //need to put all of that queue in the json object.
        jsonObject = createJSONOutOfQueue(loadedQueue);
        this.m_Response = new Response(200, null, jsonObject);
    }*/

    public void HandlePostMethodAddNewProcess() {
        this.m_ClientThreadTask = 4; //4 meaning to add new client's process to the client's linked list of processes.
        int cpuTimeNeeded = this.m_Request.getParameters().get("CPUTimeNeeded").getAsInt();
        //this.newProcess = fileSystemController.getNewProcess(cpuTimeNeeded);
        System.out.println("cpuTimeNeeded: " + cpuTimeNeeded);
        this.newProcess = new Process(cpuTimeNeeded);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", newProcess.GetId());
        this.m_Response = new Response(200, null, jsonObject);
    }

    public void HandlePostMethodStartProcessScheduling() {
        this.m_ClientThreadTask = 5; //5 meaning to run process scheduler
        //this.m_Response = new Response(200, null, null);
    }

    public void HandlePutMethodSaveProcessScheduling() {
        this.m_ClientThreadTask = 6; //6 meaning to save current linkedlist.
        this.m_Response = new Response(200, null, null);
    }


    //ClientThread will execute this function when the Client Thread Task is 1. V
    public void setUserName(String i_UserName) {
        i_UserName = this.getNewUserName();
    }


    //ClientThread will execute this function when the Client Thread Task is 2.
    public static void allocateNewListOfProcesses(LinkedList<Process> i_NewList) {
        i_NewList = new LinkedList<>();
    }

    //ClientThread will execute this function when the Client Thread Task is 3.
    public void createResponseForThisProcess(Process newProcess) {
        this.m_Response = new Response(200, null, newPackedProcessInJsonObject(newProcess));
    }

    //ClientThread will execute this function when the Client Thread Task is 4.
    //No need to do nothing the Handler already takes care of that and the response is well.

    //ClientThread will execute this function when the Client Thread Task is 5.

    public void runProcessScheduler(LinkedList<Process> listOfProcesses) throws InterruptedException {
        if (listOfProcesses == null) {
            this.m_Response = new Response(400, null, null);
            return;
        }
        Queue<Process> queueToSend = new LinkedList<>();
        for (Process currentProcess : listOfProcesses) {
            queueToSend.add(currentProcess);
        }
        ProcessSchedulerController controller = new ProcessSchedulerController(queueToSend);
        controller.run();
        boolean FIFOChosen = controller.getFIFOChosen();
        if (FIFOChosen) {
            this.m_Response = new Response(201, null, null);
        } else {
            this.m_Response = new Response(202, null, null);
        }
    }

    //ClientThread will execute this function when the Client Thread Task is 6.
    public static boolean saveCurrentQueue(LinkedList<Process> i_CurrentLinkedList, String i_UserName) {
        System.out.println("I am at saveCurrentQueue.");
        Queue<Process> queue = new LinkedList<>(i_CurrentLinkedList);
        if (queue != null) {
            return fileSystemController.saveQueue(queue, i_UserName);
        }
        return false;
    }

    private JsonObject createJSONOutOfQueue(Queue<Process> i_Queue) {
        JsonObject newJsonObjectForResponse = new JsonObject();
        while (!i_Queue.isEmpty()) {
            Process newProcess = i_Queue.poll();
            JsonObject newPackedProcessInJSON = newPackedProcessInJsonObject(newProcess);
            newJsonObjectForResponse.add("P" + newProcess.GetId(), newPackedProcessInJSON);
        }
        return newJsonObjectForResponse;
    }

    private JsonObject newPackedProcessInJsonObject(Process newProcess) {
        JsonObject newProcessPackedInJsonObject = new JsonObject();
        newProcessPackedInJsonObject.addProperty("id", String.format("%d", newProcess.GetId()));
        newProcessPackedInJsonObject.addProperty("CPUTimeNeeded", String.format("%d", newProcess.GetCPUTimeNeeded()));
        newProcessPackedInJsonObject.addProperty("CPUTimeNeededFromBeginning", String.format("%d", newProcess.GetCPUTimeNeededFromBeginning()));
        newProcessPackedInJsonObject.addProperty("ProgressIndicatorVal", String.format("%f", newProcess.getValueForProgressIndicator()));
        return newProcessPackedInJsonObject;
    }

    public void setResponse(Response newRes) {
        this.m_Response = newRes;
    }

    public Queue<Process> getQueueOfProcesses() {
        return this.m_QueueOfProcesses;
    }

    public Response getResponse() {
        return this.m_Response;
    }

    public String getNewUserName() {
        return this.m_Request.getParameters().get("userName").getAsString();
    }

    public Process getNewProcess() {
        return this.newProcess;
    }

    public int getClientThreadTask() {
        return this.m_ClientThreadTask;
    }

    public boolean getWillToRemainConnected() {
        return this.m_WantsToRemainConnected;
    }

}
