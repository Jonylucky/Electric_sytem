package com.index_electric_server.reading.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.index_electric_server.reading.dto.ReadingPullItem;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PullMonthResponse {

    private boolean success;
    private String month;
    private Integer count;
    private List<ReadingPullItem> items;

    public PullMonthResponse() {}

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public List<ReadingPullItem> getItems() {
        return items;
    }

    public void setItems(List<ReadingPullItem> items) {
        this.items = items;
    }
}