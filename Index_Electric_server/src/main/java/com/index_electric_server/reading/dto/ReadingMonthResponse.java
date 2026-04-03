package com.index_electric_server.reading.dto;

public class ReadingMonthResponse {
    private Long readingId;
    private String meterId;
    private String month;

    private Integer indexPrevMonth;   // 👈 thêm
    private Integer indexLastMonth;
    private Integer consum;

    private String imageReading;

    public ReadingMonthResponse(
            Long readingId,
            String meterId,
            String month,
            Integer indexPrevMonth,
            Integer indexLastMonth,
            Integer consum,
            String imageReading
    ) {
        this.readingId = readingId;
        this.meterId = meterId;
        this.month = month;
        this.indexPrevMonth = indexPrevMonth;
        this.indexLastMonth = indexLastMonth;
        this.consum = consum;
        this.imageReading = imageReading;
    }
    public Long getReadingId() {
        return readingId;
    }

    public void setReadingId(Long readingId) {
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

    public Integer getIndexLastMonth() {
        return indexLastMonth;
    }

    public void setIndexLastMonth(Integer indexLastMonth) {
        this.indexLastMonth = indexLastMonth;
    }

    public Integer getConsum() {
        return consum;
    }

    public void setConsum(Integer consum) {
        this.consum = consum;
    }

    public String getImageReading() {
        return imageReading;
    }

    public void setImageReading(String imageReading) {
        this.imageReading = imageReading;
    }
    public Integer getIndexPrevMonth() {
        return indexPrevMonth;
    }
    public void setIndexPrevMonth(Integer indexPrevMonth) {
        this.indexPrevMonth = indexPrevMonth;
    }
}
