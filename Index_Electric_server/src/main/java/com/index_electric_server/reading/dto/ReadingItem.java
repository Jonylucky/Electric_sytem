package com.index_electric_server.reading.dto;

import java.time.LocalDateTime;

public class ReadingItem {

    private Long readingId;

    private String meterId;
    private String month;

    private Integer indexPrevMonth;
    private Integer indexLastMonth;
    private Integer indexConsumption;

    private String imageUrl;

    /**
     * dùng cho phase2 upload ảnh
     * ví dụ: VT_7_T7_CT1_2026-02
     */
    private String imageKey;

    private LocalDateTime capturedAt;

    private String readingType; // OFFICIAL | DRAFT

    public ReadingItem() {
    }

    public ReadingItem(
            Long readingId,
            String meterId,
            String month,
            Integer indexPrevMonth,
            Integer indexLastMonth,
            Integer indexConsumption,
            String imageUrl,
            String imageKey,
            LocalDateTime capturedAt,
            String readingType
    ) {
        this.readingId = readingId;
        this.meterId = meterId;
        this.month = month;
        this.indexPrevMonth = indexPrevMonth;
        this.indexLastMonth = indexLastMonth;
        this.indexConsumption = indexConsumption;
        this.imageUrl = imageUrl;
        this.imageKey = imageKey;
        this.capturedAt = capturedAt;
        this.readingType = readingType;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageKey() {
        return imageKey;
    }

    public void setImageKey(String imageKey) {
        this.imageKey = imageKey;
    }

    public LocalDateTime getCapturedAt() {
        return capturedAt;
    }

    public void setCapturedAt(LocalDateTime capturedAt) {
        this.capturedAt = capturedAt;
    }

    public String getReadingType() {
        return readingType;
    }

    public void setReadingType(String readingType) {
        this.readingType = readingType;
    }
}