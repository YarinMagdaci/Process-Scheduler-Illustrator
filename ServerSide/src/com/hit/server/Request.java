package com.hit.server;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Request {
    private String method;
    private String path;
    private HashMap<String, String> headers;
    private JsonObject parameters;

    public Request(String method, String path, HashMap<String, String> headers, JsonObject parameters) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.parameters = parameters;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public JsonObject getParameters() {
        return parameters;
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static Request fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Request.class);
    }
}
