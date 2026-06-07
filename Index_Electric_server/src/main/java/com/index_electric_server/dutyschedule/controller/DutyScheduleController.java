package com.index_electric_server.dutyschedule.controller;

import com.index_electric_server.dutyschedule.entity.DutySchedule;
import com.index_electric_server.dutyschedule.service.DutyScheduleService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/duty-schedules")
public class DutyScheduleController {

    private final DutyScheduleService dutyScheduleService;

    public DutyScheduleController(DutyScheduleService dutyScheduleService) {
        this.dutyScheduleService = dutyScheduleService;
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateSchedule(@RequestParam int year, @RequestParam int month) {
        try {
            List<DutySchedule> schedules = dutyScheduleService.generateMonthlySchedule(year, month);
            return ResponseEntity.status(HttpStatus.CREATED).body(schedules);
        } catch (IllegalArgumentException e) {
            // Trả ra thông báo lỗi nghiệp vụ rõ ràng khi không tìm được người thay thế (Mức ưu tiên 3 thất bại)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Đã xảy ra lỗi hệ thống trong quá trình xử lý: " + e.getMessage());
        }
    }
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportScheduleExcel(@RequestParam int year, @RequestParam int month) {
        try {
            // Gọi tầng service xuất dữ liệu byte[]
            byte[] excelContent = dutyScheduleService.exportScheduleToExcel(year, month);

            // Cấu hình File Name theo chuẩn động mẫu: Lich_Truc_06_2026.xlsx
            String fileName = String.format("Lich_Truc_%02d_%d.xlsx", month, year);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(excelContent);

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(("Lỗi trong quá trình kết xuất file Excel: " + e.getMessage()).getBytes());
        }
    }
    @GetMapping
    public ResponseEntity<List<DutySchedule>> getSchedule(@RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(dutyScheduleService.getMonthlySchedule(year, month));
    }
}