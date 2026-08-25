package com.index_electric_server.device_system_electric_server.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "device_image_markers",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"image_id", "equipment_id", "check_definition_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceImageMarker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "marker_id")
    private Long markerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id", nullable = false)
    private DeviceTypeImage deviceTypeImage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id")
    private Equipment equipment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "check_definition_id", nullable = false)
    private DeviceCheckDefinition checkDefinition;

    @Column(name = "x_percent", nullable = false, precision = 8, scale = 4)
    private BigDecimal xPercent;

    @Column(name = "y_percent", nullable = false, precision = 8, scale = 4)
    private BigDecimal yPercent;

    @Column(name = "x1_percent", precision = 8, scale = 4)
    private BigDecimal x1Percent;

    @Column(name = "y1_percent", precision = 8, scale = 4)
    private BigDecimal y1Percent;

    @Column(name = "x2_percent", precision = 8, scale = 4)
    private BigDecimal x2Percent;

    @Column(name = "y2_percent", precision = 8, scale = 4)
    private BigDecimal y2Percent;

    @Column(name = "source", length = 30)
    private String source; // MANUAL, YOLO, YOLO_OCR, REVIEWED

    @Column(name = "confidence", precision = 6, scale = 4)
    private BigDecimal confidence;

    @Column(name = "review_status", length = 30)
    private String reviewStatus; // NEED_REVIEW, CONFIRMED, REJECTED

    @Column(name = "label_position", length = 20)
    private String labelPosition = "right";

    @Column(name = "notes", length = 255)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}