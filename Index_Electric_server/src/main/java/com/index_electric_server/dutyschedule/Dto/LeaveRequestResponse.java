package com.index_electric_server.dutyschedule.Dto;

import com.index_electric_server.dutyschedule.status.LeaveStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class LeaveRequestResponse {

    private Long id;

    private Long employeeId;

    private String employeeName;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private String reason;

    private LeaveStatus status;
}
