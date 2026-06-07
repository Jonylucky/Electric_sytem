package com.index_electric_server.dutyschedule.repository;

import com.index_electric_server.dutyschedule.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    // Lấy danh sách nhân viên đang active theo đúng thứ tự xoay vòng tour
    List<Employee> findByActiveTrueOrderByRotationOrderAsc();
}
