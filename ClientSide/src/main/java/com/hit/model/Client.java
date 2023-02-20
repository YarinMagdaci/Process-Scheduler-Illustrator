package com.hit.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

public class Client {
    private static Gson gson = new Gson();
    public static Response sendRequestandGetResponse(Socket socket, PrintWriter out, BufferedReader in, String method, String path, HashMap<String, String> headers, JsonObject params)
    {

        try {
            Request newReq = new Request(method, path, headers, params);
            out.println(gson.toJson(newReq));
            out.flush();
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String res = null;
            res = in.readLine();
            Response resObj = gson.fromJson(res, Response.class);
            return resObj;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
