package com.index_electric_server.device_system_electric_server.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "equipment_check_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EquipmentCheckItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "check_item_id")
    private Long checkItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "check_id", nullable = false)
    private EquipmentCheck equipmentCheck;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "check_definition_id", nullable = false)
    private DeviceCheckDefinition checkDefinition;

    // 'ok' | 'warn' | 'fail'
    @Column(name = "status_code", length = 50)
    private String statusCode;

    @Column(name = "text_value", length = 255)
    private String textValue;

    @Column(name = "numeric_value", precision = 12, scale = 2)
    private BigDecimal numericValue;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
