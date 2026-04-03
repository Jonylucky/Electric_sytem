package com.index_electric_server.reading.dto;

public class AlertReadingDto {

    private Long readingId;
    private Long companyId;
    private String companyName;
    private String meterId;
    private String meterName;
    private String month;
    private Integer indexConsumption;
    private String alertLevel;
    private String imageUrl;

    // Default Constructor
    public AlertReadingDto() {
    }

    // Getters and Setters
    public Long getReadingId() {
        return readingId;
    }

    public void setReadingId(Long readingId) {
        this.readingId = readingId;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getMeterId() {
        return meterId;
    }

    public void setMeterId(String meterId) {
        this.meterId = meterId;
    }

    public String getMeterName() {
        return meterName;
    }

    public void setMeterName(String meterName) {
        this.meterName = meterName;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}