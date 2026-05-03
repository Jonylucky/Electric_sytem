package com.index_electric_server.device_system_electric_server.entity;

import com.index_electric_server.device_system_electric_server.enums.CheckInputType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "device_check_definitions",
    uniqueConstraints = @UniqueConstraint(columnNames = {"device_type_id", "item_code"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DeviceCheckDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "check_definition_id")
    private Long checkDefinitionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_type_id", nullable = false)
    private DeviceType deviceType;

    @Column(name = "item_code", nullable = false, length = 100)
    private String itemCode;

    @Column(name = "item_name", nullable = false, length = 255)
    private String itemName;

    @Column(name = "item_group", length = 100)
    private String itemGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "check_input_type", length = 50)
    private CheckInputType checkInputType;

    @Column(name = "unit_name", length = 50)
    private String unitName;

    @Column(name = "min_value", precision = 12, scale = 4)
    private BigDecimal minValue;

    @Column(name = "max_value", precision = 12, scale = 4)
    private BigDecimal maxValue;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "is_required")
    private Boolean isRequired = true;

    @Column(name = "default_x_percent", precision = 8, scale = 4)
    private BigDecimal defaultXPercent;

    @Column(name = "default_y_percent", precision = 8, scale = 4)
    private BigDecimal defaultYPercent;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "checkDefinition", fetch = FetchType.LAZY)
    private List<EquipmentCheckItem> checkItems;

    @OneToMany(mappedBy = "checkDefinition", fetch = FetchType.LAZY)
    private List<DeviceImageMarker> imageMarkers;
}
