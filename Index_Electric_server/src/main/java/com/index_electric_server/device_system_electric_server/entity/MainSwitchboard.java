package com.index_electric_server.device_system_electric_server.entity;

import com.index_electric_server.device_system_electric_server.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "main_switchboards")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MainSwitchboard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "msb_id")
    private Long msbId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private PowerStation station;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private ElectricalRoom room;

    @Column(name = "msb_code", nullable = false, unique = true, length = 50)
    private String msbCode;

    @Column(name = "msb_name", nullable = false, length = 255)
    private String msbName;

    @Column(name = "rated_current_a", precision = 12, scale = 2)
    private BigDecimal ratedCurrentA;

    @Column(name = "rated_voltage", length = 50)
    private String ratedVoltage;

    @Column(name = "busbar_material", length = 50)
    private String busbarMaterial;

    @Column(name = "incomer_type", length = 100)
    private String incomerType;

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
