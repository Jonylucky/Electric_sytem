package com.index_electric_server.device_system_electric_server.entity;

import com.index_electric_server.device_system_electric_server.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ats_panels")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AtsPanel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ats_id")
    private Long atsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private ElectricalRoom room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "msb_id")
    private MainSwitchboard mainSwitchboard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generator_id")
    private GeneratorSystem generator;

    @Column(name = "ats_code", nullable = false, unique = true, length = 50)
    private String atsCode;

    @Column(name = "ats_name", nullable = false, length = 255)
    private String atsName;

    @Column(name = "transfer_type", length = 50)
    private String transferType;

    @Column(name = "rated_current_a", precision = 12, scale = 2)
    private BigDecimal ratedCurrentA;

    @Column(name = "rated_voltage", length = 50)
    private String ratedVoltage;

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
