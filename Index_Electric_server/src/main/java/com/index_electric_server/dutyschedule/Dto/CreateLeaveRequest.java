package com.index_electric_server.dutyschedule.Dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateLeaveRequest {

    private Long employeeId;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private String reason;
}
