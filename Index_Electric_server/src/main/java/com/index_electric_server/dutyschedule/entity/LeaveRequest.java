package com.index_electric_server.dutyschedule.entity;
import com.index_electric_server.dutyschedule.status.LeaveStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDateTime startDate; // Ví dụ: 05/06/2026 00:00

    @Column(nullable = false)
    private LocalDateTime endDate;   // Ví dụ: 15/06/2026 00:00

    private String reason;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveStatus status;
}
