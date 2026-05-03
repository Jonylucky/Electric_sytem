package com.index_electric_server.device_system_electric_server.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "buildings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Building {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "building_id")
    private Long buildingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Column(name = "building_code", nullable = false, length = 50)
    private String buildingCode;

    @Column(name = "building_name", nullable = false, length = 255)
    private String buildingName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "building", fetch = FetchType.LAZY)
    private List<ElectricalRoom> rooms;

    @OneToMany(mappedBy = "building", fetch = FetchType.LAZY)
    private List<Equipment> equipments;

    @OneToMany(mappedBy = "building", fetch = FetchType.LAZY)
    private List<ElectricalPanel> panels;

    @OneToMany(mappedBy = "building", fetch = FetchType.LAZY)
    private List<DrawingFile> drawings;
}
