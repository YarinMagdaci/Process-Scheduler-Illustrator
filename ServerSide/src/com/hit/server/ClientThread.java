package com.hit.server;

import com.google.gson.Gson;
import com.hit.controller.ProcessSchedulerController;
import com.yarin.myprocesspackage.Process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import java.util.LinkedList;
import java.util.Queue;

public class ClientThread implements Runnable {
    private Gson gson = new Gson();
    private boolean running = true;
    private Socket currentConnection;

    private LinkedList<Process> m_ListOfProcesses = null;

    private String m_UserName = null;

    public ClientThread(Socket newSocket) {
        this.currentConnection = newSocket;
    }

    @Override
    public void run() {
        while (running) {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(this.currentConnection.getInputStream()));
                String requestString = in.readLine();
                System.out.println("Received request: " + requestString);

                Request req = gson.fromJson(requestString, Request.class);
                HandleRequest handler = new HandleRequest(req);
                Thread handlerThread = new Thread(handler);
                handlerThread.start();
                handlerThread.join();//Wait for my task to end
                running = handler.getWillToRemainConnected();
                switch (handler.getClientThreadTask()) {
                    case 1:
                        this.m_UserName = req.getParameters().get("userName").getAsString();
                        break;
                    case 2:
                        this.m_ListOfProcesses = new LinkedList<>();
                        break;
                    case 3:
                        if (moveProcessesFromQueueToLinkedList(handler.getQueueOfProcesses())) {
                            this.sendProcessesOneByOne(handler);
                        } else {
                            handler.setResponse(new Response(400, null, null));
                        }
                    case 4:
                        this.m_ListOfProcesses.add(handler.getNewProcess());
                        break;
                    case 5:
                        handler.runProcessScheduler(this.m_ListOfProcesses);
                        break;
                    case 6:
                        handler.saveCurrentQueue(this.m_ListOfProcesses, this.m_UserName);
                        break;
                    default:
                        break;
                }
                PrintWriter out = new PrintWriter(this.currentConnection.getOutputStream());
                Response res = handler.getResponse();
                String responseString = gson.toJson(res);
                out.println(responseString);
                out.flush();
                if (!running) {
                    System.out.println("Client " + this.currentConnection.getInetAddress().getHostAddress());
                    printAllAboutCurrentClient();
                    this.currentConnection.close();
                    return;
                }
            } catch (IOException e) {
                System.out.println("There was a problem with client: " + this.currentConnection.getInetAddress().getHostAddress());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void printAllAboutCurrentClient() {
        int index = 0;
        StringBuilder sb = new StringBuilder();
        sb.append(this.m_UserName + " decided to leave.\n");
        System.out.println(sb.toString());
        this.m_ListOfProcesses = null;
    }

    private void sendProcessesOneByOne(HandleRequest handler) throws IOException {
        for (Process currentProcess : this.m_ListOfProcesses) {
            handler.createResponseForThisProcess(currentProcess);
            Response currentRes = handler.getResponse();
            PrintWriter out = new PrintWriter(this.currentConnection.getOutputStream());
            String responseString = gson.toJson(currentRes);
            out.println(responseString);
            out.flush();
        }
        handler.setResponse(null);
    }

    private boolean moveProcessesFromQueueToLinkedList(Queue<Process> i_Queue) {
        this.m_ListOfProcesses = new LinkedList<>();
        if (i_Queue == null) {
            return false;
        }
        while (!i_Queue.isEmpty()) {
            this.m_ListOfProcesses.add(i_Queue.poll());
        }
        System.out.println(this.m_ListOfProcesses.size());
        return true;
    }
}
