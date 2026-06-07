package com.index_electric_server.dutyschedule.repository;
import com.index_electric_server.dutyschedule.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    // JPQL tìm kiếm đơn nghỉ phép có khoảng giao (Overlap) với khoảng thời gian của tháng trực
    // Công thức: leave.startDate < :endOfMonth AND leave.endDate > :startOfMonth
    @Query("SELECT l FROM LeaveRequest l JOIN FETCH l.employee e " +
            "WHERE l.status = 'APPROVED' AND e.active = true " +
            "AND l.startDate < :endOfMonth AND l.endDate > :startOfMonth")
    List<LeaveRequest> findActiveLeavesInPeriod(
            @Param("startOfMonth") LocalDateTime startOfMonth,
            @Param("endOfMonth") LocalDateTime endOfMonth);
}