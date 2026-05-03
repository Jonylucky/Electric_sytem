package com.index_electric_server.device_system_electric_server.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "device_type_images")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DeviceTypeImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_type_id", nullable = false)
    private DeviceType deviceType;

    @Column(name = "model_variant", length = 100)
    private String modelVariant;

    // 'main' | 'exploded' | 'diagram' | 'manual'
    @Column(name = "image_role", nullable = false, length = 50)
    private String imageRole = "main";

    @Column(name = "image_url", nullable = false, length = 1000)
    private String imageUrl;

    @Column(name = "image_label", length = 255)
    private String imageLabel;

    @Column(name = "width_px")
    private Integer widthPx;

    @Column(name = "height_px")
    private Integer heightPx;

    @Column(name = "is_default")
    private Boolean isDefault = false;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "uploaded_by", length = 255)
    private String uploadedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "deviceTypeImage", fetch = FetchType.LAZY)
    private List<DeviceImageMarker> markers;
}
