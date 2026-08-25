package com.index_electric_server.device_system_electric_server.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ocr_text_blocks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OcrTextBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ocr_text_id")
    private Long ocrTextId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id", nullable = false)
    private DeviceTypeImage deviceTypeImage;

    @Column(name = "text", columnDefinition = "TEXT")
    private String text;

    @Column(name = "x1_percent", precision = 8, scale = 4)
    private BigDecimal x1Percent;

    @Column(name = "y1_percent", precision = 8, scale = 4)
    private BigDecimal y1Percent;

    @Column(name = "x2_percent", precision = 8, scale = 4)
    private BigDecimal x2Percent;

    @Column(name = "y2_percent", precision = 8, scale = 4)
    private BigDecimal y2Percent;

    @Column(name = "confidence", precision = 6, scale = 4)
    private BigDecimal confidence;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}