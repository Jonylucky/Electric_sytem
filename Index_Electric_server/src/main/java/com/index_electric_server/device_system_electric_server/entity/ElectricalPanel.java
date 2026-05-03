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
@Table(name = "electrical_panels")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ElectricalPanel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "panel_id")
    private Long panelId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private ElectricalRoom room;

    // Self-reference: panel tree parent → child
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_panel_id")
    private ElectricalPanel parentPanel;

    @OneToMany(mappedBy = "parentPanel", fetch = FetchType.LAZY)
    private List<ElectricalPanel> childPanels;

    // Polymorphic source (msb | ats | panel) — no FK
    @Column(name = "source_type", length = 50)
    private String sourceType;

    @Column(name = "source_id")
    private Long sourceId;

    @Column(name = "panel_code", nullable = false, unique = true, length = 50)
    private String panelCode;

    @Column(name = "panel_name", nullable = false, length = 255)
    private String panelName;

    @Column(name = "panel_type", length = 100)
    private String panelType;

    @Column(name = "rated_current_a", precision = 12, scale = 2)
    private BigDecimal ratedCurrentA;

    @Column(name = "rated_voltage", length = 50)
    private String ratedVoltage;

    @Column(name = "floor_label", length = 50)
    private String floorLabel;

    @Column(name = "area_served", length = 255)
    private String areaServed;

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

    @OneToMany(mappedBy = "panel", fetch = FetchType.LAZY)
    private List<PanelDevice> devices;

    @OneToMany(mappedBy = "panel", fetch = FetchType.LAZY)
    private List<Equipment> equipments;

    @OneToMany(mappedBy = "panel", fetch = FetchType.LAZY)
    private List<DrawingPanelCoord> drawingCoords;
}
