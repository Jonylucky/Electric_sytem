package com.index_electric_server.dutyschedule.service;

import com.index_electric_server.dutyschedule.Dto.CreateLeaveRequest;
import com.index_electric_server.dutyschedule.Dto.LeaveRequestResponse;
import com.index_electric_server.dutyschedule.entity.Employee;
import com.index_electric_server.dutyschedule.entity.LeaveRequest;
import com.index_electric_server.dutyschedule.repository.EmployeeRepository;
import com.index_electric_server.dutyschedule.repository.LeaveRequestRepository;
import com.index_electric_server.dutyschedule.status.LeaveStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public LeaveRequestResponse createLeaveRequest(CreateLeaveRequest request) {

        if (request.getEmployeeId() == null) {
            throw new IllegalArgumentException("employeeId không được để trống");
        }

        if (request.getStartDate() == null) {
            throw new IllegalArgumentException("startDate không được để trống");
        }

        if (request.getEndDate() == null) {
            throw new IllegalArgumentException("endDate không được để trống");
        }

        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw new IllegalArgumentException("endDate phải lớn hơn startDate");
        }

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy nhân viên với id = " + request.getEmployeeId()
                ));

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(employee)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reason(request.getReason())
                .status(LeaveStatus.APPROVED)
                .build();

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);

        return LeaveRequestResponse.builder()
                .id(saved.getId())
                .employeeId(saved.getEmployee().getId())
                .employeeName(saved.getEmployee().getFullName())
                .startDate(saved.getStartDate())
                .endDate(saved.getEndDate())
                .reason(saved.getReason())
                .status(saved.getStatus())
                .build();
    }
}