package com.index_electric_server.dutyschedule.service;

import com.index_electric_server.dutyschedule.entity.DutySchedule;
import com.index_electric_server.dutyschedule.entity.Employee;
import com.index_electric_server.dutyschedule.entity.LeaveRequest;
import com.index_electric_server.dutyschedule.repository.DutyScheduleRepository;
import com.index_electric_server.dutyschedule.repository.EmployeeRepository;
import com.index_electric_server.dutyschedule.repository.LeaveRequestRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DutyScheduleService {

    private final EmployeeRepository employeeRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final DutyScheduleRepository dutyScheduleRepository;

    public DutyScheduleService(EmployeeRepository employeeRepository,
                               LeaveRequestRepository leaveRequestRepository,
                               DutyScheduleRepository dutyScheduleRepository) {
        this.employeeRepository = employeeRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.dutyScheduleRepository = dutyScheduleRepository;
    }

    @Transactional
    public List<DutySchedule> generateMonthlySchedule(int year, int month) {
        YearMonth targetYearMonth = YearMonth.of(year, month);
        LocalDate startOfMonth = targetYearMonth.atDay(1);
        LocalDate endOfMonth = targetYearMonth.atEndOfMonth();

        // 1. Lấy danh sách nhân viên gác tour
        List<Employee> activeEmployees = employeeRepository.findByActiveTrueOrderByRotationOrderAsc();
        if (activeEmployees.isEmpty()) {
            throw new IllegalStateException("Hệ thống hiện không có nhân viên trực nào đang hoạt động.");
        }

        // 2. Lấy danh sách nghỉ phép giao cắt tháng (Để tối ưu số lượng câu SQL Query trong loop)
        LocalDateTime startThreshold = startOfMonth.atTime(17, 0);
        LocalDateTime endThreshold = endOfMonth.plusDays(1).atTime(17, 0);
        List<LeaveRequest> leaves = leaveRequestRepository.findActiveLeavesInPeriod(startThreshold, endThreshold);

        // 3. Xóa lịch cũ của tháng (Đảm bảo tính Idempotent - Tái tạo nhiều lần không trùng lặp)
        dutyScheduleRepository.deleteByDutyDateBetween(startOfMonth, endOfMonth);
        dutyScheduleRepository.flush(); // Đồng bộ dữ liệu xuống DB ngay để tránh lỗi Cache thực thể

        // 4. Tìm điểm xuất phát từ tháng trước để giữ nguyên nhịp bước nhảy tour gốc
        Optional<DutySchedule> lastScheduleOpt = dutyScheduleRepository.findFirstByDutyDateLessThanOrderByDutyDateDesc(startOfMonth);

        int currentRotationIndex = 0;
        if (lastScheduleOpt.isPresent()) {
            Employee lastExpectedEmp = lastScheduleOpt.get().getExpectedEmployee();
            int lastIndex = -1;
            for (int i = 0; i < activeEmployees.size(); i++) {
                if (activeEmployees.get(i).getId().equals(lastExpectedEmp.getId())) {
                    lastIndex = i;
                    break;
                }
            }
            if (lastIndex != -1) {
                // Người tiếp theo = (Vị trí người cũ + 1) chia dư cho tổng số lượng nhân viên
                currentRotationIndex = (lastIndex + 1) % activeEmployees.size();
            }
        }

        // Bản đồ theo dõi tần suất phân ca thực tế trong tháng để đảm bảo tính công bằng (Cân bằng tải)
        Map<Long, Integer> monthlyDutyCount = new HashMap<>();
        activeEmployees.forEach(e -> monthlyDutyCount.put(e.getId(), 0));

        List<DutySchedule> newlyGeneratedSchedules = new ArrayList<>();
        int daysInMonth = targetYearMonth.lengthOfMonth(); // Tự động xử lý chính xác 28, 29 (năm nhuận), 30, 31 ngày

        // 5. Vòng lặp phân ca chi tiết từng ngày
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate dutyDate = targetYearMonth.atDay(day);

            // Cấu hình mốc thời gian đặc thù ca trực: 17h ngày hôm nay -> 17h ngày hôm sau
            LocalDateTime shiftStart = dutyDate.atTime(LocalTime.of(17, 0));
            LocalDateTime shiftEnd = dutyDate.plusDays(1).atTime(LocalTime.of(17, 0));

            // Định danh người đáng lẽ phải trực theo cấu trúc tour (Expected Employee)
            Employee expectedEmployee = activeEmployees.get(currentRotationIndex);

            // Thu thập lịch sử phân ca thực tế (Assigned) liền kề trước đó
            List<DutySchedule> history = getRecentHistory(dutyDate, newlyGeneratedSchedules);

            // Thuật toán sàng lọc tìm nhân sự gác ca thay thế tối ưu theo 3 cấp độ ưu tiên
            Employee assignedEmployee = determineAssignedEmployee(
                    expectedEmployee, activeEmployees, leaves, shiftStart, shiftEnd, history, monthlyDutyCount, dutyDate
            );

            boolean isReplaced = !expectedEmployee.getId().equals(assignedEmployee.getId());
            String reason = isReplaced ? "Nhân viên gốc " + expectedEmployee.getFullName() + " bận nghỉ phép. Hệ thống điều động thay thế." : null;

            // Tăng biến đếm số ca của người nhận nhiệm vụ thực tế
            monthlyDutyCount.put(assignedEmployee.getId(), monthlyDutyCount.get(assignedEmployee.getId()) + 1);

            // Đóng gói Object thực thể JPA
            DutySchedule schedule = DutySchedule.builder()
                    .dutyDate(dutyDate)
                    .startTime(shiftStart)
                    .endTime(shiftEnd)
                    .expectedEmployee(expectedEmployee)
                    .assignedEmployee(assignedEmployee)
                    .replaced(isReplaced)
                    .replacementReason(reason)
                    .build();

            newlyGeneratedSchedules.add(schedule);

            // Tiếp tục dịch chuyển bước nhảy tour gốc cho ca tiếp theo
            currentRotationIndex = (currentRotationIndex + 1) % activeEmployees.size();
        }

        // Lưu toàn bộ tập hợp lịch trực mới xuống DB
        return dutyScheduleRepository.saveAll(newlyGeneratedSchedules);
    }

    /**
     * Thuật toán bóc tách và phân cấp ứng viên thay thế gác ca trực
     */
    private Employee determineAssignedEmployee(Employee expected, List<Employee> allEmployees,
                                               List<LeaveRequest> leaves, LocalDateTime shiftStart, LocalDateTime shiftEnd,
                                               List<DutySchedule> history, Map<Long, Integer> dutyCount, LocalDate dutyDate) {

        // Loại bỏ ngay lập tức những người đang có đơn nghỉ phép giao lấn (Overlap) với khung ca trực hiện tại
        List<Employee> availableEmployees = allEmployees.stream()
                .filter(emp -> !isEmployeeOnLeave(emp, leaves, shiftStart, shiftEnd))
                .collect(Collectors.toList());

        // Kịch bản lỗi nghiêm trọng: Tất cả nhân viên đều vướng lịch nghỉ phép
        if (availableEmployees.isEmpty()) {
            throw new IllegalArgumentException("Không tìm được người trực cho ngày " +
                    dutyDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " do tất cả nhân viên đều bận nghỉ phép!");
        }

        // Nếu người thuộc tour gốc không nghỉ phép và đáp ứng được rule nghỉ 3 ngày gác trước đó -> Chọn luôn
        if (availableEmployees.contains(expected) && !hasEmployeeDutiedInDays(expected, history, 3)) {
            return expected;
        }

        // --- MỨC ƯU TIÊN 1 ---
        // Không nghỉ phép + Đảm bảo rule nghỉ 3 ngày + Số ca trực tích lũy thấp nhất
        List<Employee> level1Candidates = availableEmployees.stream()
                .filter(emp -> !hasEmployeeDutiedInDays(emp, history, 3))
                .collect(Collectors.toList());

        if (!level1Candidates.isEmpty()) {
            return level1Candidates.stream()
                    .min(Comparator.comparingInt(emp -> dutyCount.get(emp.getId())))
                    .get();
        }

        // --- MỨC ƯU TIÊN 2 (Hạ điều kiện khi thiếu người do nghỉ dài ngày) ---
        // Không nghỉ phép + Không trực ca ngay ngày hôm trước + Số ca trực thấp nhất
        List<Employee> level2Candidates = availableEmployees.stream()
                .filter(emp -> !hasEmployeeDutiedInDays(emp, history, 1))
                .collect(Collectors.toList());

        if (!level2Candidates.isEmpty()) {
            return level2Candidates.stream()
                    .min(Comparator.comparingInt(emp -> dutyCount.get(emp.getId())))
                    .get();
        }

        // --- MỨC ƯU TIÊN 3 (Tình huống bất khả kháng) ---
        // Không nghỉ phép + Chọn người gác ít ca nhất trong tháng để gánh tải
        return availableEmployees.stream()
                .min(Comparator.comparingInt(emp -> dutyCount.get(emp.getId())))
                .get();
    }

    /**
     * Thuật toán kiểm định overlap thời gian giữa ca gác và đơn nghỉ phép
     */
    private boolean isEmployeeOnLeave(Employee emp, List<LeaveRequest> leaves, LocalDateTime shiftStart, LocalDateTime shiftEnd) {
        return leaves.stream()
                .filter(leave -> leave.getEmployee().getId().equals(emp.getId()))
                .anyMatch(leave -> shiftStart.isBefore(leave.getEndDate()) && shiftEnd.isAfter(leave.getStartDate()));
    }

    /**
     * Kiểm định xem nhân sự đã từng làm việc thực tế (assignedEmployee) trong N ngày trước chưa
     */
    private boolean hasEmployeeDutiedInDays(Employee emp, List<DutySchedule> history, int days) {
        if (history == null || history.isEmpty()) return false;
        int checkLimit = Math.min(days, history.size());
        for (int i = 0; i < checkLimit; i++) {
            DutySchedule pastSchedule = history.get(i); // Lịch sử đã được sắp xếp giảm dần (index 0 là hôm qua)
            if (pastSchedule.getAssignedEmployee().getId().equals(emp.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Hợp nhất mảng lịch sử giữa dữ liệu trong Memory vừa sinh ra và dữ liệu cũ trong Database
     */
    private List<DutySchedule> getRecentHistory(LocalDate date, List<DutySchedule> memorySchedules) {
        List<DutySchedule> history = new ArrayList<>();

        // Đưa dữ liệu vừa sinh ra trong tháng vào (Xếp từ ngày gần nhất ngược về trước)
        for (int i = memorySchedules.size() - 1; i >= 0; i--) {
            history.add(memorySchedules.get(i));
        }

        // Đọc thêm dữ liệu ca trực thực tế của tháng cũ từ database để tránh đứt gãy dữ liệu lịch sử lúc đầu tháng
        List<DutySchedule> dbHistory = dutyScheduleRepository.findRecentSchedulesBefore(date);
        if (dbHistory != null) {
            for (DutySchedule ds : dbHistory) {
                if (ds.getDutyDate().isBefore(date) &&
                        (memorySchedules.isEmpty() || ds.getDutyDate().isBefore(memorySchedules.get(0).getDutyDate()))) {
                    history.add(ds);
                }
                if (history.size() >= 6) break; // Chỉ cần giữ lại khoảng 6 ca gần nhất là đủ phân cấp kiểm tra dữ liệu
            }
        }
        return history;
    }

    public List<DutySchedule> getMonthlySchedule(int year, int month) {
        YearMonth target = YearMonth.of(year, month);
        return dutyScheduleRepository.findByDutyDateBetweenOrderByDutyDateAsc(target.atDay(1), target.atEndOfMonth());
    }
    /**
     * Xuất lịch trực tháng ra file Excel dưới dạng mảng Byte công nghệ cao
     */
    public byte[] exportScheduleToExcel(int year, int month) throws IOException {
        YearMonth target = YearMonth.of(year, month);
        // 1. Lấy dữ liệu từ database thông qua JPA
        List<DutySchedule> schedules = dutyScheduleRepository.findByDutyDateBetweenOrderByDutyDateAsc(
                target.atDay(1), target.atEndOfMonth()
        );

        // 2. Khởi tạo Workbook Excel (.xlsx)
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Lịch trực Thg " + month + "-" + year);

            // Khởi tạo các bộ định dạng Styles (Font, Màu sắc, Border)
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setFontHeightInPoints((short) 12);

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
            setBorder(headerCellStyle);

            // Style cho dòng dữ liệu thông thường
            CellStyle bodyCellStyle = workbook.createCellStyle();
            setBorder(bodyCellStyle);

            // Style đặc biệt bôi vàng cảnh báo cho các ca bị thay thế (Replaced)
            CellStyle replacedCellStyle = workbook.createCellStyle();
            replacedCellStyle.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
            replacedCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            setBorder(replacedCellStyle);

            // 3. Tạo dòng Header
            Row headerRow = sheet.createRow(0);
            String[] columns = {"STT", "Ngày Trực", "Bắt Đầu Ca", "Kết Thúc Ca", "Nhân Viên Gốc", "Nhân Viên Thực Tế", "Trạng Thái", "Lý Do Thay Thế"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            // ĐỊnh dạng Format Date hiển thị trong Excel
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            // 4. Đổ dữ liệu JPA Entities vào từng dòng Excel
            int rowIndex = 1;
            for (DutySchedule schedule : schedules) {
                Row row = sheet.createRow(rowIndex++);

                // Quyết định Style dựa trên việc ca trực có bị đổi người hay không
                CellStyle currentStyle = schedule.isReplaced() ? replacedCellStyle : bodyCellStyle;

                // Cell 0: STT
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(rowIndex - 1);
                cell0.setCellStyle(currentStyle);

                // Cell 1: Ngày trực
                Cell cell1 = row.createCell(1);
                cell1.setCellValue(schedule.getDutyDate().format(dateFormatter));
                cell1.setCellStyle(currentStyle);

                // Cell 2: Khởi đầu ca
                Cell cell2 = row.createCell(2);
                cell2.setCellValue(schedule.getStartTime().format(dateTimeFormatter));
                cell2.setCellStyle(currentStyle);

                // Cell 3: Kết thúc ca
                Cell cell3 = row.createCell(3);
                cell3.setCellValue(schedule.getEndTime().format(dateTimeFormatter));
                cell3.setCellStyle(currentStyle);

                // Cell 4: Nhân viên gốc theo tour
                Cell cell4 = row.createCell(4);
                cell4.setCellValue(schedule.getExpectedEmployee().getFullName());
                cell4.setCellStyle(currentStyle);

                // Cell 5: Nhân viên trực thực tế
                Cell cell5 = row.createCell(5);
                cell5.setCellValue(schedule.getAssignedEmployee().getFullName());
                cell5.setCellStyle(currentStyle);

                // Cell 6: Trạng thái đổi lịch
                Cell cell6 = row.createCell(6);
                cell6.setCellValue(schedule.isReplaced() ? "ĐÃ THAY THẾ" : "ĐÚNG TOUR");
                cell6.setCellStyle(currentStyle);

                // Cell 7: Lý do biến động
                Cell cell7 = row.createCell(7);
                cell7.setCellValue(schedule.getReplacementReason() != null ? schedule.getReplacementReason() : "");
                cell7.setCellStyle(currentStyle);
            }

            // 5. Tự động căn chỉnh độ rộng cột (Auto-size columns) theo nội dung
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Ghi luồng dữ liệu Excel ra bộ nhớ ByteArray
            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * Hàm helper vẽ viền Border ô Excel
     */
    private void setBorder(CellStyle cellStyle) {
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
    }

    // ... các hàm nghiệp vụ generate cũ giữ nguyên
}