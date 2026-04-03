package com.index_electric_server.reading.service.export;

import com.index_electric_server.reading.dto.report.ExportReadingRowDto;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.time.LocalDate;
import java.util.List;

@Service
public class ExcelHyperlinkExportService {

    private final ExportReadingService exportReadingService;

    public ExcelHyperlinkExportService(ExportReadingService exportReadingService) {
        this.exportReadingService = exportReadingService;
    }

    public byte[] exportByMonthAndCompanyId( String month,
                                             Long companyId,
                                             List<ExportReadingRowDto> rows) throws Exception {


        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("BaoCao");

            // ===== styles =====
            CellStyle companyStyle = createCompanyInfoStyle(workbook);
            CellStyle nationStyle = createNationStyle(workbook);
            CellStyle mottoStyle = createMottoStyle(workbook);
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle subTitleStyle = createSubTitleStyle(workbook);
            CellStyle headerStyle = createTableHeaderStyle(workbook);
            CellStyle textStyle = createTextCellStyle(workbook);
            CellStyle numberStyle = createNumberCellStyle(workbook);
            CellStyle centerStyle = createCenterCellStyle(workbook);
            CellStyle hyperlinkStyle = createHyperlinkStyle(workbook);
            CellStyle signTitleStyle = createSignTitleStyle(workbook);
            CellStyle signNoteStyle = createSignNoteStyle(workbook);

            int rowIndex = 0;

            // ===== set column widths =====
            sheet.setColumnWidth(0, 6 * 256);   // STT
            sheet.setColumnWidth(1, 18 * 256);  // Meter ID
            sheet.setColumnWidth(2, 26 * 256);  // Meter Name
            sheet.setColumnWidth(3, 14 * 256);  // Month
            sheet.setColumnWidth(4, 16 * 256);  // Prev
            sheet.setColumnWidth(5, 16 * 256);  // Last
            sheet.setColumnWidth(6, 16 * 256);  // Consumption
            sheet.setColumnWidth(7, 14 * 256);  // Location
            sheet.setColumnWidth(8, 18 * 256);  // Image

            // ===== Row 0-2: company left + VN header right =====
            Row r0 = sheet.createRow(rowIndex++);
            r0.setHeightInPoints(22);
            createMergedCell(sheet, r0, 0, 0, 0, 3, "CÔNG TY: " + getCompanyName(rows, companyId), companyStyle);
            createMergedCell(sheet, r0, 0, 0, 5, 8, "CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM", nationStyle);

            Row r1 = sheet.createRow(rowIndex++);
            r1.setHeightInPoints(20);
            createMergedCell(sheet, r1, 1, 1, 0, 3, "Mã công ty: " + companyId, companyStyle);
            createMergedCell(sheet, r1, 1, 1, 5, 8, "Độc lập - Tự do - Hạnh phúc", mottoStyle);

            Row r2 = sheet.createRow(rowIndex++);
            r2.setHeightInPoints(18);
            createMergedCell(sheet, r2, 2, 2, 0, 3, "Tháng báo cáo: " + month, companyStyle);
            createMergedCell(sheet, r2, 2, 2, 5, 8, "--------------------", centerStyle);

            rowIndex++;

            // ===== Title =====
            Row titleRow = sheet.createRow(rowIndex++);
            titleRow.setHeightInPoints(28);
            createMergedCell(sheet, titleRow, titleRow.getRowNum(), titleRow.getRowNum(), 0, 8,
                    "BẢNG TỔNG HỢP CHỈ SỐ ĐIỆN", titleStyle);

            Row subTitleRow = sheet.createRow(rowIndex++);
            subTitleRow.setHeightInPoints(22);
            createMergedCell(sheet, subTitleRow, subTitleRow.getRowNum(), subTitleRow.getRowNum(), 0, 8,
                    "THÁNG " + month, subTitleStyle);

            rowIndex++;

            // ===== table header =====
            Row headerRow = sheet.createRow(rowIndex++);
            headerRow.setHeightInPoints(28);

            String[] headers = {
                    "STT",
                    "Mã đồng hồ",
                    "Tên đồng hồ",
                    "Tháng",
                    "Chỉ số tháng trước",
                    "Chỉ số tháng này",
                    "Sản lượng tiêu thụ",
                    "Tầng",
                    "Hình ảnh"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // ===== data rows =====
            CreationHelper helper = workbook.getCreationHelper();
            int stt = 1;

            for (ExportReadingRowDto item : rows) {
                Row row = sheet.createRow(rowIndex++);
                row.setHeightInPoints(24);

                createCell(row, 0, stt++, centerStyle);
                createCell(row, 1, item.getMeterId(), textStyle);
                createCell(row, 2, item.getMeterName(), textStyle);
                createCell(row, 3, item.getMonth(), centerStyle);
                createCell(row, 4, item.getIndexPrevMonth(), numberStyle);
                createCell(row, 5, item.getIndexLastMonth(), numberStyle);
                createCell(row, 6, item.getIndexConsumption(), numberStyle);
                createCell(row, 7, item.getLocationName(), centerStyle);

                Cell imageCell = row.createCell(8);
                imageCell.setCellStyle(hyperlinkStyle);

                String imagePath = item.getImagePath();

                if (imagePath != null && !imagePath.isBlank()) {

                    Hyperlink link = helper.createHyperlink(HyperlinkType.FILE);
                    link.setAddress(imagePath);

                    imageCell.setCellValue("Xem ảnh");
                    imageCell.setHyperlink(link);
                    imageCell.setCellStyle(hyperlinkStyle);

                } else {
                    imageCell.setCellValue("");
                }
            }

            // ===== total row =====
            Row totalRow = sheet.createRow(rowIndex++);
            totalRow.setHeightInPoints(24);

            createMergedCell(sheet, totalRow, totalRow.getRowNum(), totalRow.getRowNum(), 0, 5,
                    "Tổng cộng", headerStyle);

            int dataStartRow = headerRow.getRowNum() + 2; // Excel row number
            int dataEndRow = totalRow.getRowNum();        // because current row already created, Excel row num = totalRow+1 later in formula context
            int totalExcelRow = totalRow.getRowNum() + 1;

            Cell totalConsumptionCell = totalRow.createCell(6);
            totalConsumptionCell.setCellFormula(String.format("SUM(G%d:G%d)", dataStartRow, dataEndRow));
            totalConsumptionCell.setCellStyle(numberStyle);

            Cell blankLoc = totalRow.createCell(7);
            blankLoc.setCellStyle(headerStyle);

            Cell blankImg = totalRow.createCell(8);
            blankImg.setCellStyle(headerStyle);

            rowIndex += 2;

            // ===== date row =====
            LocalDate now = LocalDate.now();
            Row dateRow = sheet.createRow(rowIndex++);
            createMergedCell(
                    sheet,
                    dateRow,
                    dateRow.getRowNum(),
                    dateRow.getRowNum(),
                    5,
                    8,
                    String.format("Ngày %02d tháng %02d năm %d", now.getDayOfMonth(), now.getMonthValue(), now.getYear()),
                    centerStyle
            );

            rowIndex++;

            // ===== sign titles =====
            Row signTitleRow = sheet.createRow(rowIndex++);
            signTitleRow.setHeightInPoints(22);
            createMergedCell(sheet, signTitleRow, signTitleRow.getRowNum(), signTitleRow.getRowNum(), 0, 2,
                    "Người kiểm tra", signTitleStyle);
            createMergedCell(sheet, signTitleRow, signTitleRow.getRowNum(), signTitleRow.getRowNum(), 3, 5,
                    "Người lập biểu", signTitleStyle);
            createMergedCell(sheet, signTitleRow, signTitleRow.getRowNum(), signTitleRow.getRowNum(), 6, 8,
                    "Đại diện công ty", signTitleStyle);

            Row noteRow = sheet.createRow(rowIndex++);
            createMergedCell(sheet, noteRow, noteRow.getRowNum(), noteRow.getRowNum(), 0, 2,
                    "(Ký, ghi rõ họ tên)", signNoteStyle);
            createMergedCell(sheet, noteRow, noteRow.getRowNum(), noteRow.getRowNum(), 3, 5,
                    "(Ký, ghi rõ họ tên)", signNoteStyle);
            createMergedCell(sheet, noteRow, noteRow.getRowNum(), noteRow.getRowNum(), 6, 8,
                    "(Ký, ghi rõ họ tên)", signNoteStyle);

            // chừa chỗ ký
            for (int i = 0; i < 4; i++) {
                Row blank = sheet.createRow(rowIndex++);
                blank.setHeightInPoints(22);
            }

            Row signNameRow = sheet.createRow(rowIndex++);
            createMergedCell(sheet, signNameRow, signNameRow.getRowNum(), signNameRow.getRowNum(), 0, 2,
                    "", centerStyle);
            createMergedCell(sheet, signNameRow, signNameRow.getRowNum(), signNameRow.getRowNum(), 3, 5,
                    "", centerStyle);
            createMergedCell(sheet, signNameRow, signNameRow.getRowNum(), signNameRow.getRowNum(), 6, 8,
                    "", centerStyle);

            // border cho toàn bộ vùng bảng
            workbook.setForceFormulaRecalculation(true);
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private String getCompanyName(List<ExportReadingRowDto> rows, Long companyId) {

        if (rows != null) {
            for (ExportReadingRowDto r : rows) {
                if (r.getCompanyName() != null && !r.getCompanyName().isBlank()) {
                    return r.getCompanyName();
                }
            }
        }

        return companyId == null ? "" : String.valueOf(companyId);
    }

    private void createMergedCell(
            Sheet sheet,
            Row row,
            int firstRow,
            int lastRow,
            int firstCol,
            int lastCol,
            String value,
            CellStyle style
    ) {
        Cell cell = row.createCell(firstCol);
        cell.setCellValue(value == null ? "" : value);
        cell.setCellStyle(style);

        for (int c = firstCol + 1; c <= lastCol; c++) {
            Cell extra = row.createCell(c);
            extra.setCellStyle(style);
        }

        sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));
    }

    private void createCell(Row row, int colIndex, String value, CellStyle style) {
        Cell cell = row.createCell(colIndex);
        cell.setCellValue(value == null ? "" : value);
        cell.setCellStyle(style);
    }

    private void createCell(Row row, int colIndex, Integer value, CellStyle style) {
        Cell cell = row.createCell(colIndex);
        if (value != null) {
            cell.setCellValue(value);
        } else {
            cell.setCellValue("");
        }
        cell.setCellStyle(style);
    }

    private void createCell(Row row, int colIndex, Long value, CellStyle style) {
        Cell cell = row.createCell(colIndex);
        if (value != null) {
            cell.setCellValue(value);
        } else {
            cell.setCellValue("");
        }
        cell.setCellStyle(style);
    }

    private CellStyle createCompanyInfoStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createNationStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createMottoStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setItalic(true);
        font.setFontHeightInPoints((short) 11);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createSubTitleStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createTableHeaderStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        addBorder(style);
        return style;
    }

    private CellStyle createTextCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setWrapText(true);
        addBorder(style);
        return style;
    }

    private CellStyle createCenterCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setWrapText(true);
        addBorder(style);
        return style;
    }

    private CellStyle createNumberCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setAlignment(HorizontalAlignment.RIGHT);
        addBorder(style);

        DataFormat dataFormat = workbook.createDataFormat();
        style.setDataFormat(dataFormat.getFormat("#,##0"));
        return style;
    }

    private CellStyle createHyperlinkStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setUnderline(Font.U_SINGLE);
        font.setColor(IndexedColors.BLUE.getIndex());

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        addBorder(style);
        return style;
    }

    private CellStyle createSignTitleStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createSignNoteStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setItalic(true);
        font.setFontHeightInPoints((short) 10);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private void addBorder(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}