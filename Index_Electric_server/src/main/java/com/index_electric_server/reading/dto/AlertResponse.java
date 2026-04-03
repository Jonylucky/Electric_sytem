package com.index_electric_server.reading.dto;

import java.util.List;

public class AlertResponse {

    private String month;
    private boolean hasAlert;
    private int totalAlerts;
    private int totalWarning;
    private int totalDanger;
    private List<AlertReadingDto> readings;

    // Default Constructor
    public AlertResponse() {
    }

    // Getters and Setters
    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public boolean isHasAlert() {
        return hasAlert;
    }

    public void setHasAlert(boolean hasAlert) {
        this.hasAlert = hasAlert;
    }

    public int getTotalAlerts() {
        return totalAlerts;
    }

    public void setTotalAlerts(int totalAlerts) {
        this.totalAlerts = totalAlerts;
    }

    public int getTotalWarning() {
        return totalWarning;
    }

    public void setTotalWarning(int totalWarning) {
        this.totalWarning = totalWarning;
    }

    public int getTotalDanger() {
        return totalDanger;
    }

    public void setTotalDanger(int totalDanger) {
        this.totalDanger = totalDanger;
    }

    public List<AlertReadingDto> getReadings() {
        return readings;
    }

    public void setReadings(List<AlertReadingDto> readings) {
        this.readings = readings;
    }
}