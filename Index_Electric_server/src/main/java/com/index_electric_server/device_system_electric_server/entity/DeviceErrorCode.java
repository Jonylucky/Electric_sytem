package com.index_electric_server.device_system_electric_server.entity;

import com.index_electric_server.device_system_electric_server.enums.SeverityLevel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "device_error_codes",
    uniqueConstraints = @UniqueConstraint(columnNames = {"device_type_id", "error_code"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DeviceErrorCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "error_code_id")
    private Long errorCodeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_type_id", nullable = false)
    private DeviceType deviceType;

    @Column(name = "error_code", nullable = false, length = 50)
    private String errorCode;

    @Column(name = "error_name", nullable = false, length = 255)
    private String errorName;

    @Column(name = "error_category", length = 100)
    private String errorCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity_level", nullable = false, length = 20)
    private SeverityLevel severityLevel = SeverityLevel.warning;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "possible_cause", columnDefinition = "TEXT")
    private String possibleCause;

    @Column(name = "operator_action", columnDefinition = "TEXT")
    private String operatorAction;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
