package com.hit.model;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;

public class Response {
    public int statusCode;
    public HashMap<String, String> headers;
    public JsonObject body;

    public Response(int statusCode) {
        this.statusCode = statusCode;
        this.headers = new HashMap<>();
        this.body = new JsonObject();
    }

    public Response(int statusCode, HashMap<String, String> i_Headers, JsonObject i_Body) {
        this.statusCode = statusCode;
        this.headers = i_Headers;
        this.body = i_Body;
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public void add(String key, JsonElement value) {
        body.add(key, value);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public JsonObject getBody() {
        return body;
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
