package com.index_electric_server.dutyschedule.controller;

import com.index_electric_server.dutyschedule.Dto.CreateLeaveRequest;
import com.index_electric_server.dutyschedule.Dto.LeaveRequestResponse;
import com.index_electric_server.dutyschedule.service.LeaveRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leave-requests")
@RequiredArgsConstructor
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    @PostMapping
    public ResponseEntity<LeaveRequestResponse> createLeaveRequest(
            @RequestBody CreateLeaveRequest request
    ) {
        LeaveRequestResponse response = leaveRequestService.createLeaveRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
