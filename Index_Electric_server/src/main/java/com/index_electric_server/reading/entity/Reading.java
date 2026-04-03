package com.index_electric_server.reading.entity;

import com.index_electric_server.common.enums.AlertLevel;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "readings",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_readings_meter_month_type",
                        columnNames = {"meter_id", "month", "reading_type"}
                )
        },
        indexes = {
                @Index(
                        name = "ix_readings_meter_month_type",
                        columnList = "meter_id,month,reading_type"
                ),
                @Index(
                        name = "ix_readings_month_type_alert",
                        columnList = "month,reading_type,alert_level"
                )
        }
)
public class Reading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reading_id")
    private Long readingId;

    @Column(name = "meter_id", nullable = false, length = 64)
    private String meterId;

    @Column(name = "month", nullable = false, length = 7)
    private String month;

    @Column(name = "index_prev_month", nullable = false)
    private Integer indexPrevMonth;

    @Column(name = "index_last_month", nullable = false)
    private Integer indexLastMonth;

    @Column(name = "index_consumption", nullable = false)
    private Integer indexConsumption;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "image_key", length = 128)
    private String imageKey;

    @Column(name = "captured_at")
    private LocalDateTime capturedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "reading_type", nullable = false, length = 16)
    private ReadingType readingType;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_level", length = 16)
    private AlertLevel alertLevel;

    public enum ReadingType {
        OFFICIAL,
        DRAFT
    }

    public Reading() {}

    // ===== Getters / Setters =====

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public ReadingType getReadingType() {
        return readingType;
    }

    public void setReadingType(ReadingType readingType) {
        this.readingType = readingType;
    }

    public AlertLevel getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(AlertLevel alertLevel) {
        this.alertLevel = alertLevel;
    }

    // ===== Hooks =====

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;

        if (readingType == null) {
            readingType = ReadingType.OFFICIAL;
        }

        if (alertLevel == null) {
            alertLevel = AlertLevel.NORMAL;
        }

        if (indexPrevMonth == null) indexPrevMonth = 0;
        if (indexLastMonth == null) indexLastMonth = 0;
        if (indexConsumption == null) indexConsumption = 0;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}