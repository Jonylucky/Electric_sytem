package com.index_electric_server.device_system_electric_server.entity;

import com.index_electric_server.device_system_electric_server.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "equipment")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "equipment_id")
    private Long equipmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "panel_id")
    private ElectricalPanel panel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_type_id", nullable = false)
    private DeviceType deviceType;

    @Column(name = "equipment_code", nullable = false, unique = true, length = 50)
    private String equipmentCode;

    @Column(name = "equipment_name", nullable = false, length = 255)
    private String equipmentName;

    @Column(name = "equipment_group", length = 100)
    private String equipmentGroup;

    @Column(name = "manufacturer", length = 255)
    private String manufacturer;

    @Column(name = "model", length = 255)
    private String model;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Column(name = "rated_power_kw", precision = 12, scale = 2)
    private BigDecimal ratedPowerKw;

    @Column(name = "rated_current_a", precision = 12, scale = 2)
    private BigDecimal ratedCurrentA;

    @Column(name = "rated_voltage", length = 50)
    private String ratedVoltage;

    @Column(name = "location_text", length = 255)
    private String locationText;

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

    @OneToMany(mappedBy = "equipment", fetch = FetchType.LAZY)
    private List<EquipmentComponent> components;

    @OneToMany(mappedBy = "equipment", fetch = FetchType.LAZY)
    private List<EquipmentCheck> checks;
}
