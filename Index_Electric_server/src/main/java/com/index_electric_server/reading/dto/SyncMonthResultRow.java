package com.index_electric_server.reading.dto;

public class SyncMonthResultRow {
    private Integer i;
    private String meterId;
    private String month;
    private Boolean success;
    private String status;     // CREATED/UPDATED
    private Long readingId;
    private String error;

    public SyncMonthResultRow() {}

    // getters/setters
    public Integer getI() { return i; }
    public void setI(Integer i) { this.i = i; }

    public String getMeterId() { return meterId; }
    public void setMeterId(String meterId) { this.meterId = meterId; }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getReadingId() { return readingId; }
    public void setReadingId(Long readingId) { this.readingId = readingId; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}