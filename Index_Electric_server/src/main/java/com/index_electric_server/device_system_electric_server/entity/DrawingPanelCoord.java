package com.index_electric_server.device_system_electric_server.entity;

import com.index_electric_server.device_system_electric_server.enums.ExtractMethod;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "drawing_panel_coords",
    uniqueConstraints = @UniqueConstraint(columnNames = {"drawing_id", "panel_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DrawingPanelCoord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coord_id")
    private Long coordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drawing_id", nullable = false)
    private DrawingFile drawingFile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "panel_id", nullable = false)
    private ElectricalPanel panel;

    @Column(name = "x_percent", nullable = false, precision = 8, scale = 4)
    private BigDecimal xPercent;

    @Column(name = "y_percent", nullable = false, precision = 8, scale = 4)
    private BigDecimal yPercent;

    // 'top' | 'bottom' | 'left' | 'right'
    @Column(name = "label_anchor", length = 20)
    private String labelAnchor = "top";

    @Enumerated(EnumType.STRING)
    @Column(name = "extract_method", length = 50)
    private ExtractMethod extractMethod = ExtractMethod.manual;

    @Column(name = "confidence", precision = 5, scale = 4)
    private BigDecimal confidence;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
