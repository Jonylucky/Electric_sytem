package com.index_electric_server.reading.dto;

import java.util.List;

public class SyncMonthResult {
    private Boolean success;
    private Integer count;
    private List<SyncMonthResultRow> results;

    public SyncMonthResult() {}

    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }

    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }

    public List<SyncMonthResultRow> getResults() { return results; }
    public void setResults(List<SyncMonthResultRow> results) { this.results = results; }
}