package com.index_electric_server.reading.dto;



import java.util.List;

public class ReadingChartResponseDto {

    private String meterId;
    private String month;
    private List<ReadingChartPointDto> points;

    public ReadingChartResponseDto() {
    }

    public ReadingChartResponseDto(String meterId, String month, List<ReadingChartPointDto> points) {
        this.meterId = meterId;
        this.month = month;
        this.points = points;
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

    public List<ReadingChartPointDto> getPoints() {
        return points;
    }

    public void setPoints(List<ReadingChartPointDto> points) {
        this.points = points;
    }
}