package com.index_electric_server.device_system_electric_server.entity;

import com.index_electric_server.device_system_electric_server.enums.ReviewStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "detected_devices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetectedDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detected_device_id")
    private Long detectedDeviceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id", nullable = false)
    private DeviceTypeImage deviceTypeImage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_type_id")
    private DeviceType deviceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id")
    private Equipment equipment; // null nếu chưa match với equipment thật

    @Column(name = "detected_label", length = 100)
    private String detectedLabel; // FCU-01, CB 3P/400A

    @Column(name = "ocr_text", length = 500)
    private String ocrText;

    @Column(name = "manufacturer", length = 255)
    private String manufacturer;

    @Column(name = "model", length = 255)
    private String model;

    @Column(name = "rated_current_a", precision = 12, scale = 2)
    private BigDecimal ratedCurrentA;

    @Column(name = "rated_voltage", length = 50)
    private String ratedVoltage;

    @Column(name = "location_text", length = 255)
    private String locationText;

    @Column(name = "x1_percent", precision = 8, scale = 4)
    private BigDecimal x1Percent;

    @Column(name = "y1_percent", precision = 8, scale = 4)
    private BigDecimal y1Percent;

    @Column(name = "x2_percent", precision = 8, scale = 4)
    private BigDecimal x2Percent;

    @Column(name = "y2_percent", precision = 8, scale = 4)
    private BigDecimal y2Percent;

    @Column(name = "x_center_percent", precision = 8, scale = 4)
    private BigDecimal xCenterPercent;

    @Column(name = "y_center_percent", precision = 8, scale = 4)
    private BigDecimal yCenterPercent;

    @Column(name = "yolo_confidence", precision = 6, scale = 4)
    private BigDecimal yoloConfidence;

    @Column(name = "ocr_confidence", precision = 6, scale = 4)
    private BigDecimal ocrConfidence;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_status", length = 50)
    private ReviewStatus reviewStatus = ReviewStatus.NEED_REVIEW;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}