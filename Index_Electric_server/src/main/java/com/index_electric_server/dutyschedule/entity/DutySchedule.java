package com.index_electric_server.dutyschedule.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "duty_schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DutySchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate dutyDate; // Ngày đại diện ca trực (Key chính để bóc tách theo tháng)

    @Column(nullable = false)
    private LocalDateTime startTime; // Khởi đầu ca: 17:00 ngày hiện tại

    @Column(nullable = false)
    private LocalDateTime endTime;   // Kết thúc ca: 17:00 ngày hôm sau

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expected_employee_id", nullable = false)
    private Employee expectedEmployee; // Người đáng lẽ trực theo tour gốc

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_employee_id", nullable = false)
    private Employee assignedEmployee; // Người thực tế trực sau khi xử lý off

    private boolean replaced;

    @Column(length = 500)
    private String replacementReason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }
    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}