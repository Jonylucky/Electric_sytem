package com.index_electric_server.reading.dto;


public class ReadingChartPointDto {

    private String month;
    private Integer indexPrevMonth;
    private Integer indexLastMonth;
    private Integer indexConsumption;
    private String alertLevel;

    public ReadingChartPointDto() {
    }

    public ReadingChartPointDto(
            String month,
            Integer indexPrevMonth,
            Integer indexLastMonth,
            Integer indexConsumption,
            String alertLevel
    ) {
        this.month = month;
        this.indexPrevMonth = indexPrevMonth;
        this.indexLastMonth = indexLastMonth;
        this.indexConsumption = indexConsumption;
        this.alertLevel = alertLevel;
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

    public String getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(String alertLevel) {
        this.alertLevel = alertLevel;
    }
}
