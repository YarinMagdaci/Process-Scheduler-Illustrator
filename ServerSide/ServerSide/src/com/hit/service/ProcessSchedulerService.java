package com.hit.service;

import com.yarin.myalgorithmspackage.FIFOProcessScheduler;
import com.yarin.myalgorithmspackage.ProcessSchedulerAbstractClass;
import com.yarin.myalgorithmspackage.RoundRobinProcessScheduler;
import com.yarin.myprocesspackage.Process;

import java.util.Queue;

public class ProcessSchedulerService {
    public ProcessSchedulerAbstractClass m_Scheduler;

    public ProcessSchedulerService(int i_RandomChooseWhatSchedulerToSet, Queue<Process> i_Queue) {
        if (i_RandomChooseWhatSchedulerToSet == 0) {
            this.m_Scheduler = new FIFOProcessScheduler();
        } else {
            this.m_Scheduler = new RoundRobinProcessScheduler(2);
        }
        this.m_Scheduler.SetReadyQueue(i_Queue);
    }

    public void RunScheduler() throws InterruptedException {
        while (!this.m_Scheduler.GetReadyQueue().isEmpty()) {
            this.m_Scheduler.Run();
        }
    }
}
