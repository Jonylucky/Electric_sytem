package com.index_electric_server.device_system_electric_server.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "device_types")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DeviceType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "device_type_id")
    private Long deviceTypeId;

    @Column(name = "type_code", nullable = false, unique = true, length = 50)
    private String typeCode;

    @Column(name = "type_name", nullable = false, length = 255)
    private String typeName;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "deviceType", fetch = FetchType.LAZY)
    private List<DeviceCheckDefinition> checkDefinitions;

    @OneToMany(mappedBy = "deviceType", fetch = FetchType.LAZY)
    private List<DeviceErrorCode> errorCodes;

    @OneToMany(mappedBy = "deviceType", fetch = FetchType.LAZY)
    private List<Equipment> equipments;

    @OneToMany(mappedBy = "deviceType", fetch = FetchType.LAZY)
    private List<DeviceTypeImage> images;
}
