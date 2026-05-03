package com.index_electric_server.device_system_electric_server.entity;

import com.index_electric_server.device_system_electric_server.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "power_stations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PowerStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "station_id")
    private Long stationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private ElectricalRoom room;

    @Column(name = "station_code", nullable = false, unique = true, length = 50)
    private String stationCode;

    @Column(name = "station_name", nullable = false, length = 255)
    private String stationName;

    @Column(name = "station_type", length = 100)
    private String stationType;

    @Column(name = "voltage_level", length = 50)
    private String voltageLevel;

    @Column(name = "capacity_kva", precision = 12, scale = 2)
    private BigDecimal capacityKva;

    @Column(name = "manufacturer", length = 255)
    private String manufacturer;

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

    @OneToMany(mappedBy = "station", fetch = FetchType.LAZY)
    private List<MainSwitchboard> switchboards;
}
