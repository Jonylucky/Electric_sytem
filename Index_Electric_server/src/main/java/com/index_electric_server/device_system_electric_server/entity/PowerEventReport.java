package com.index_electric_server.device_system_electric_server.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "power_event_reports")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PowerEventReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ats_id")
    private AtsPanel atsPanel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generator_id")
    private GeneratorSystem generator;

    @Column(name = "report_code", nullable = false, unique = true, length = 50)
    private String reportCode;

    // 'outage' | 'transfer' | 'test'
    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "outage_started_at")
    private LocalDateTime outageStartedAt;

    @Column(name = "generator_started_at")
    private LocalDateTime generatorStartedAt;

    @Column(name = "power_restored_at")
    private LocalDateTime powerRestoredAt;

    @Column(name = "affected_area", columnDefinition = "TEXT")
    private String affectedArea;

    @Column(name = "cause_text", columnDefinition = "TEXT")
    private String causeText;

    @Column(name = "action_taken", columnDefinition = "TEXT")
    private String actionTaken;

    @Column(name = "reported_by", length = 255)
    private String reportedBy;

    @Column(name = "approved_by", length = 255)
    private String approvedBy;

    // 'open' | 'closed'
    @Column(name = "status", length = 50)
    private String status = "open";

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
