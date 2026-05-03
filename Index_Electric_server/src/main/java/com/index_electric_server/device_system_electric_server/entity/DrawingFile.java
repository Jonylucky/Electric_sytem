package com.index_electric_server.device_system_electric_server.entity;

import com.index_electric_server.device_system_electric_server.enums.DrawingType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "drawing_files")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DrawingFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "drawing_id")
    private Long drawingId;

    // Polymorphic: 'building' | 'panel' | 'site'
    @Column(name = "object_type", nullable = false, length = 50)
    private String objectType;

    @Column(name = "object_id", nullable = false)
    private Long objectId;

    @Enumerated(EnumType.STRING)
    @Column(name = "drawing_type", nullable = false, length = 50)
    private DrawingType drawingType;

    @Column(name = "file_url", nullable = false, length = 1000)
    private String fileUrl;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "drawing_code", length = 100)
    private String drawingCode;

    @Column(name = "drawing_label", length = 255)
    private String drawingLabel;

    @Column(name = "revision", length = 50)
    private String revision;

    @Column(name = "issued_date")
    private LocalDate issuedDate;

    @Column(name = "image_w_px")
    private Integer imageWPx;

    @Column(name = "image_h_px")
    private Integer imageHPx;

    @Column(name = "uploaded_by", length = 255)
    private String uploadedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "drawingFile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DrawingPanelCoord> panelCoords;

    // convenience: link to Building when objectType = 'building'
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "object_id", insertable = false, updatable = false)
    private Building building;
}
