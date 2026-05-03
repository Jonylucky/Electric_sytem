package com.index_electric_server.device_system_electric_server.entity;

import com.index_electric_server.device_system_electric_server.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "panel_devices")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PanelDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "panel_device_id")
    private Long panelDeviceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "panel_id", nullable = false)
    private ElectricalPanel panel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id")
    private Equipment equipment;

    @Column(name = "device_name", nullable = false, length = 255)
    private String deviceName;

    @Column(name = "device_role", length = 100)
    private String deviceRole;

    @Column(name = "breaker_type", length = 100)
    private String breakerType;

    @Column(name = "breaker_rating_a", precision = 12, scale = 2)
    private BigDecimal breakerRatingA;

    @Column(name = "position_no", length = 50)
    private String positionNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private Status status = Status.active;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
