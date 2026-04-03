package com.index_electric_server.reading.dto;

public class SyncResultItem {

    private String meterId;
    private String month;
    private String status; // CREATED | UPDATED
    private Long readingId;

    public SyncResultItem() {
    }

    public SyncResultItem(String meterId, String month, String status, Long readingId) {
        this.meterId = meterId;
        this.month = month;
        this.status = status;
        this.readingId = readingId;
    }

    public String getMeterId() {
        return meterId;
    }

    public void setMeterId(String meterId) {
        this.meterId = meterId;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getReadingId() {
        return readingId;
    }

    public void setReadingId(Long readingId) {
        this.readingId = readingId;
    }
}