package com.hit.controller;

import com.hit.service.ProcessSchedulerService;
import com.yarin.myprocesspackage.Process;

import java.util.Queue;
import java.util.Random;

public class ProcessSchedulerController {
    public ProcessSchedulerService service;
    private boolean m_FIFOChosen = false;

    public ProcessSchedulerController(Queue<Process> i_Queue) {
        Random rand = new Random();
        int randomNum = rand.nextInt(2);
        if (randomNum == 0) {
            m_FIFOChosen = true;
        }//else it's RoundRobin
        this.service = new ProcessSchedulerService(randomNum, i_Queue);
    }

    public void run() throws InterruptedException {
        this.service.RunScheduler();
    }

    public boolean getFIFOChosen() {
        return this.m_FIFOChosen;
    }
}
