package com.index_electric_server.device_system_electric_server.entity;

import com.index_electric_server.device_system_electric_server.enums.OverallCondition;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "equipment_checks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EquipmentCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "check_id")
    private Long checkId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Column(name = "check_date", nullable = false)
    private LocalDateTime checkDate;

    @Column(name = "checked_by", length = 255)
    private String checkedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "overall_condition", length = 50)
    private OverallCondition overallCondition;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "equipmentCheck", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EquipmentCheckItem> items;
}
