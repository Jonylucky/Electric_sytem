package com.index_electric_server.reading.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReadingPullItem {

    private String meterId;
    private String month;

    private Integer indexPrevMonth;
    private Integer indexLastMonth;
    private Integer indexConsumption;

    private LocalDateTime createdAt;

    public ReadingPullItem() {}

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

    public Integer getIndexPrevMonth() {
        return indexPrevMonth;
    }

    public void setIndexPrevMonth(Integer indexPrevMonth) {
        this.indexPrevMonth = indexPrevMonth;
    }

    public Integer getIndexLastMonth() {
        return indexLastMonth;
    }

    public void setIndexLastMonth(Integer indexLastMonth) {
        this.indexLastMonth = indexLastMonth;
    }

    public Integer getIndexConsumption() {
        return indexConsumption;
    }

    public void setIndexConsumption(Integer indexConsumption) {
        this.indexConsumption = indexConsumption;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime capturedAt) {
        this.createdAt = capturedAt;
    }
}