package com.index_electric_server.dutyschedule.repository;

import com.index_electric_server.dutyschedule.entity.DutySchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DutyScheduleRepository extends JpaRepository<DutySchedule, Long> {

    // Tìm danh sách lịch trực trong khoảng ngày (phục vụ API lấy dữ liệu)
    List<DutySchedule> findByDutyDateBetweenOrderByDutyDateAsc(LocalDate start, LocalDate end);

    // Tìm ca trực cuối cùng gần nhất trước ngày đầu tháng mới để lấy bước nhảy tour
    Optional<DutySchedule> findFirstByDutyDateLessThanOrderByDutyDateDesc(LocalDate date);

    // Lấy danh sách lịch sử ca trực phân bổ thực tế đổ ngược về trước (để check rule nghỉ 3 ngày)
    @Query("SELECT d FROM DutySchedule d WHERE d.dutyDate < :date ORDER BY d.dutyDate DESC")
    List<DutySchedule> findRecentSchedulesBefore(@Param("date") LocalDate date);

    // Xóa toàn bộ lịch trực thuộc tháng chỉ định (Regenerate an toàn)
    @Modifying
    @Query("DELETE FROM DutySchedule d WHERE d.dutyDate BETWEEN :start AND :end")
    void deleteByDutyDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
