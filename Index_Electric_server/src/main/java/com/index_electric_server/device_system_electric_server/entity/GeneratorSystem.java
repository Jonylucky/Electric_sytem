package com.index_electric_server.device_system_electric_server.entity;

import com.index_electric_server.device_system_electric_server.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "generator_systems")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GeneratorSystem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "generator_id")
    private Long generatorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private ElectricalRoom room;

    @Column(name = "generator_code", nullable = false, unique = true, length = 50)
    private String generatorCode;

    @Column(name = "generator_name", nullable = false, length = 255)
    private String generatorName;

    @Column(name = "fuel_type", length = 50)
    private String fuelType;

    @Column(name = "rated_power_kva", precision = 12, scale = 2)
    private BigDecimal ratedPowerKva;

    @Column(name = "rated_voltage", length = 50)
    private String ratedVoltage;

    @Column(name = "phase_type", length = 20)
    private String phaseType;

    @Column(name = "manufacturer", length = 255)
    private String manufacturer;

    @Column(name = "model", length = 255)
    private String model;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Column(name = "install_date")
    private LocalDate installDate;

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
