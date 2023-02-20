package com.hit.dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yarin.myprocesspackage.Process;

import java.io.*;
import java.util.Queue;

public class QueueOfProcessesFileDao implements Dao<String, Queue<Process>> {
    static String path = "C:\\Users\\yarin\\Desktop\\JAVA Project\\ServerSide\\ServerSide\\queues.txt";
    Gson gson;

    public QueueOfProcessesFileDao() {
        gson = new GsonBuilder().create();
    }

    @Override
    public void save(Queue<Process> q, String id) throws IOException {
        ProcessMap map = null;
        try {
            map = gson.fromJson(ReadObjectFromFile(), ProcessMap.class);
        } catch (FileNotFoundException e) {
            map = new ProcessMap();
        }
        if (map == null) {
            map = new ProcessMap();
        }
        map.put(id, q);
        WriteObjectToFile(gson.toJson(map));
    }

    @Override
    public Queue<Process> get(String id) throws IOException {
        ProcessMap db = gson.fromJson(ReadObjectFromFile(), ProcessMap.class);
        if (db == null) {
            return null;
        }
        return db.get(id);
    }

    @Override
    public void delete(String id) throws IOException {
        ProcessMap map = gson.fromJson(ReadObjectFromFile(), ProcessMap.class);
        if (map == null) {
            return;
        }
        if (!map.containsKey(id)) {
            return;
        }
        map.remove(id);
        WriteObjectToFile(gson.toJson(map));
    }

    public static void WriteObjectToFile(String json) throws IOException {
        FileWriter fileWriter = new FileWriter(path);
        fileWriter.write(json);
        fileWriter.flush();
        fileWriter.close();
    }

    public static String ReadObjectFromFile() throws IOException {
        FileReader fileReader = new FileReader(path);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String json = bufferedReader.readLine();
        bufferedReader.close();
        return json;
    }

}