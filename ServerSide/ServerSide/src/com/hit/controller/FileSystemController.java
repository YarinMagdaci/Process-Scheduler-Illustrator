package com.hit.controller;

import com.yarin.myprocesspackage.Process;
import com.hit.service.FileSystemService;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class FileSystemController {
    private FileSystemService services;

    public FileSystemController() {
        this.services = new FileSystemService();
    }

    public boolean saveQueue(Queue<Process> i_QueueToBeSaved, String i_ID) {
        if (i_QueueToBeSaved == null || i_QueueToBeSaved.isEmpty()) {
            return false;
        }
        try {
            this.services.QPFileDao.save(i_QueueToBeSaved, i_ID);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public Queue<Process> loadQueue(String i_ID) {
        try {
            Queue<Process> loadedQueue = this.services.QPFileDao.get(i_ID);
            return loadedQueue;

        } catch (IOException e) {
            return null;
        }
    }

    public Process getNewProcess(int i_NewCPUTimeNeeded) {
        return new Process(i_NewCPUTimeNeeded);
    }

    public void allocateNewQueueOfProcesses(Queue<Process> i_NewQueue) {
        i_NewQueue = new LinkedList<>();
    }

    public void startProcessScheduler() {

    }
}
