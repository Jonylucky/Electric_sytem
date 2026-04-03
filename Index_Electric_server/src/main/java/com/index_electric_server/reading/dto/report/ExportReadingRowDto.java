package com.index_electric_server.reading.dto.report;

import com.index_electric_server.common.enums.AlertLevel;

public class ExportReadingRowDto {

    private String meterId;
    private String meterName;
    private String companyName;
    private String month;
    private Integer indexPrevMonth;
    private Integer indexLastMonth;
    private Integer indexConsumption;
    private String locationName;
    private String imagePath;
    private AlertLevel alertLevel;

    // Default constructor (often required by Jackson/Hibernate)
    public ExportReadingRowDto() {}

    // Full constructor
    public ExportReadingRowDto(String meterId, String meterName, String companyName, String month,
                               Integer indexPrevMonth, Integer indexLastMonth, Integer indexConsumption,
                               String locationName, String imagePath, AlertLevel alertLevel) {
        this.meterId = meterId;
        this.meterName = meterName;
        this.companyName = companyName;
        this.month = month;
        this.indexPrevMonth = indexPrevMonth;
        this.indexLastMonth = indexLastMonth;
        this.indexConsumption = indexConsumption;
        this.locationName = locationName;
        this.imagePath = imagePath;
        this.alertLevel = alertLevel;
    }

    // Getters
    public String getMeterId() { return meterId; }
    public String getMeterName() { return meterName; }
    public String getCompanyName() { return companyName; }
    public String getMonth() { return month; }
    public Integer getIndexPrevMonth() { return indexPrevMonth; }
    public Integer getIndexLastMonth() { return indexLastMonth; }
    public Integer getIndexConsumption() { return indexConsumption; }
    public String getLocationName() { return locationName; }
    public String getImagePath() { return imagePath; }
    public AlertLevel getAlertLevel() { return alertLevel; }

    // Setters (Proper void return types)
    public void setMeterId(String meterId) { this.meterId = meterId; }
    public void setMeterName(String meterName) { this.meterName = meterName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public void setMonth(String month) { this.month = month; }
    public void setIndexPrevMonth(Integer indexPrevMonth) { this.indexPrevMonth = indexPrevMonth; }
    public void setIndexLastMonth(Integer indexLastMonth) { this.indexLastMonth = indexLastMonth; }
    public void setIndexConsumption(Integer indexConsumption) { this.indexConsumption = indexConsumption; }
    public void SetlocationName(String locationName) { this.locationName = locationName; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public void setAlertLevel(AlertLevel alertLevel) { this.alertLevel = alertLevel; }
}