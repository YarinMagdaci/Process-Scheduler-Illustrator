package com.hit.service;

import com.hit.dao.QueueOfProcessesFileDao;
//import com.hit.myalgorithmspackage.ProcessSchedulerAbstractClass;
import com.yarin.myprocesspackage.Process;
import com.yarin.myalgorithmspackage.ProcessSchedulerAbstractClass;

import java.io.IOException;
import java.util.Queue;

public class FileSystemService {
    public QueueOfProcessesFileDao QPFileDao;

    public FileSystemService() {
        this.QPFileDao = new QueueOfProcessesFileDao();
    }

    public FileSystemService(QueueOfProcessesFileDao i_QPFileDao) {
        this.QPFileDao = i_QPFileDao;
    }

    public Queue<Process> getQueueById(String id) throws IOException, ClassNotFoundException {
        Queue<Process> qProcess = this.QPFileDao.get(id);
        if (qProcess != null) {
            return qProcess;
        }
        System.out.println("Id " + id + " has nothing stored in database.");
        return null;
    }

    public boolean saveQueueById(Queue<Process> newQueue, String id) throws IOException {
        this.QPFileDao.save(newQueue, id);
        return true;
    }
}
