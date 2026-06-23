package com.sys.designer.framework.web.mcp;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonRpcRequest {
    private String jsonrpc;
    private JsonNode id;
    private String method;
    private JsonNode params;
    private String models;

    public JsonNode id(){
        return id;
    }

    public String method(){
        return method;
    }

    public JsonNode params(){
        return params;
    }

    public String jsonrpc(){
        return jsonrpc;
    }

    public String models(){
        return models;
    }

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public JsonNode getId() {
        return id;
    }

    public void setId(JsonNode id) {
        this.id = id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public JsonNode getParams() {
        return params;
    }

    public void setParams(JsonNode params) {
        this.params = params;
    }

    public String getModels() {
        return models;
    }

    public void setModels(String models) {
        this.models = models;
    }
}