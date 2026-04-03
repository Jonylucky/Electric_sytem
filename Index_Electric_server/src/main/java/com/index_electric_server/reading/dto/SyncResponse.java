package com.index_electric_server.reading.dto;

public class SyncResponse {

    private boolean success;
    private String message;
    private ReadingItem data;
    private SyncResultItem result;

    public SyncResponse() {
    }

    public SyncResponse(boolean success, String message, ReadingItem data, SyncResultItem result) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.result = result;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ReadingItem getData() {
        return data;
    }

    public void setData(ReadingItem data) {
        this.data = data;
    }

    public SyncResultItem getResult() {
        return result;
    }

    public void setResult(SyncResultItem result) {
        this.result = result;
    }
}