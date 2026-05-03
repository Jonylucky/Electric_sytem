package com.index_electric_server.device_system_electric_server.entity;

import com.index_electric_server.device_system_electric_server.enums.ObjectType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "maintenance_records")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MaintenanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "maintenance_id")
    private Long maintenanceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "object_type", nullable = false, length = 50)
    private ObjectType objectType;

    @Column(name = "object_id", nullable = false)
    private Long objectId;

    @Column(name = "maintenance_code", nullable = false, unique = true, length = 50)
    private String maintenanceCode;

    @Column(name = "maintenance_type", length = 100)
    private String maintenanceType;

    @Column(name = "scheduled_date")
    private LocalDate scheduledDate;

    @Column(name = "performed_date")
    private LocalDate performedDate;

    @Column(name = "performed_by", length = 255)
    private String performedBy;

    @Column(name = "vendor_name", length = 255)
    private String vendorName;

    @Column(name = "result_status", length = 50)
    private String resultStatus;

    @Column(name = "findings", columnDefinition = "TEXT")
    private String findings;

    @Column(name = "action_taken", columnDefinition = "TEXT")
    private String actionTaken;

    @Column(name = "next_due_date")
    private LocalDate nextDueDate;

    @Column(name = "attachment_url", columnDefinition = "TEXT")
    private String attachmentUrl;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "maintenanceRecord", fetch = FetchType.LAZY)
    private List<EquipmentReplacement> replacements;
}
