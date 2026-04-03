package com.index_electric_server.reading.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SyncPayload {

    /**
     * index của item trong list
     * dùng để map multipart image_{i}
     */
    private Integer i;

    private String meterId;
    private String month;              // YYYY-MM

    // ✅ thêm
    private Integer indexPrevMonth;

    private Integer indexLastMonth;

    private LocalDateTime capturedAt;

    // optional
    private Long timestampMillis;

    // optional billing rule
    private Integer cutoffDay;

    /**
     * dùng cho phase2 zip mapping
     * ví dụ: VT_7_T7_CT1_2026-02
     */
    private String imageKey;

    public SyncPayload() {}

    public SyncPayload(
            Integer i,
            String meterId,
            String month,
            Integer indexPrevMonth,
            Integer indexLastMonth,
            LocalDateTime capturedAt,
            Long timestampMillis,
            Integer cutoffDay,
            String imageKey
    ) {
        this.i = i;
        this.meterId = meterId;
        this.month = month;
        this.indexPrevMonth = indexPrevMonth;
        this.indexLastMonth = indexLastMonth;
        this.capturedAt = capturedAt;
        this.timestampMillis = timestampMillis;
        this.cutoffDay = cutoffDay;
        this.imageKey = imageKey;
    }

    // ===== getters setters =====

    public Integer getI() {
        return i;
    }

    public void setI(Integer i) {
        this.i = i;
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

    public LocalDateTime getCapturedAt() {
        return capturedAt;
    }

    public void setCapturedAt(LocalDateTime capturedAt) {
        this.capturedAt = capturedAt;
    }

    public Long getTimestampMillis() {
        return timestampMillis;
    }

    public void setTimestampMillis(Long timestampMillis) {
        this.timestampMillis = timestampMillis;
    }

    public Integer getCutoffDay() {
        return cutoffDay;
    }

    public void setCutoffDay(Integer cutoffDay) {
        this.cutoffDay = cutoffDay;
    }

    public String getImageKey() {
        return imageKey;
    }

    public void setImageKey(String imageKey) {
        this.imageKey = imageKey;
    }

    /**
     * helper: nếu client không gửi imageKey
     */
    public String resolveImageKey() {
        if (imageKey != null && !imageKey.isBlank()) {
            return imageKey;
        }
        if (meterId == null || month == null) return null;
        return (meterId + "_" + month).replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    @Override
    public String toString() {
        return "SyncPayload{" +
                "i=" + i +
                ", meterId='" + meterId + '\'' +
                ", month='" + month + '\'' +
                ", indexPrevMonth=" + indexPrevMonth +
                ", indexLastMonth=" + indexLastMonth +
                ", capturedAt=" + capturedAt +
                ", timestampMillis=" + timestampMillis +
                ", cutoffDay=" + cutoffDay +
                ", imageKey='" + imageKey + '\'' +
                '}';
    }
}