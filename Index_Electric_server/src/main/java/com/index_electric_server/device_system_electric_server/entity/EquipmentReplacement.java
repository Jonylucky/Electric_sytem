package com.index_electric_server.device_system_electric_server.entity;

import com.index_electric_server.device_system_electric_server.enums.ObjectType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "equipment_replacements")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EquipmentReplacement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "replacement_id")
    private Long replacementId;

    @Enumerated(EnumType.STRING)
    @Column(name = "object_type", nullable = false, length = 50)
    private ObjectType objectType;

    @Column(name = "object_id", nullable = false)
    private Long objectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maintenance_id")
    private MaintenanceRecord maintenanceRecord;

    @Column(name = "replacement_code", nullable = false, unique = true, length = 50)
    private String replacementCode;

    @Column(name = "replacement_type", length = 100)
    private String replacementType = "component_replacement";

    @Column(name = "replaced_part_name", nullable = false, length = 255)
    private String replacedPartName;

    @Column(name = "old_item_code", length = 100)
    private String oldItemCode;

    @Column(name = "old_serial_number", length = 100)
    private String oldSerialNumber;

    @Column(name = "new_part_name", length = 255)
    private String newPartName;

    @Column(name = "new_item_code", length = 100)
    private String newItemCode;

    @Column(name = "new_serial_number", length = 100)
    private String newSerialNumber;

    @Column(name = "reason_text", columnDefinition = "TEXT")
    private String reasonText;

    @Column(name = "replacement_date", nullable = false)
    private LocalDateTime replacementDate;

    @Column(name = "replaced_by", length = 255)
    private String replacedBy;

    @Column(name = "vendor_name", length = 255)
    private String vendorName;

    @Column(name = "cost_amount", precision = 14, scale = 2)
    private BigDecimal costAmount;

    @Column(name = "warranty_until")
    private LocalDate warrantyUntil;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
