package com.index_electric_server.reading.service.export;

import com.index_electric_server.common.util.ZipFileUtil;
import com.index_electric_server.reading.dto.report.ExportReadingRowDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service

public class ZipExportService {

    private final ExportReadingService exportReadingService;
    private final ExcelHyperlinkExportService excelHyperlinkExportService;

    public ZipExportService(
            ExportReadingService exportReadingService,
            ExcelHyperlinkExportService excelHyperlinkExportService
    ) {
        this.exportReadingService = exportReadingService;
        this.excelHyperlinkExportService = excelHyperlinkExportService;
    }
    @Value("${app.upload-dir}")
    private String uploadDir;
    public byte[] exportZipByMonthAndCompanyId(String month, Long companyId) throws Exception {
        List<ExportReadingRowDto> rows =
                exportReadingService.getExportRowsByMonthAndCompanyId(month, companyId);

        Path tempDir = Files.createTempDirectory("export_");
        Path imageDir = Files.createDirectories(tempDir.resolve("images"));

        try {
            Set<String> usedNames = new LinkedHashSet<>();

            for (ExportReadingRowDto row : rows) {
                String imagePath = row.getImagePath();
                if (imagePath == null || imagePath.isBlank()) {
                    continue;
                }

                System.out.println("db imagePath = " + imagePath);

                // /uploads/2026-02/img/YL_1_2026-02.jpg
                // -> 2026-02/img/YL_1_2026-02.jpg
                String relativePath = imagePath.replaceFirst("^/uploads/", "");

                // D:/Javafpt/Index_Electric_server/uploads + relativePath
                Path srcPath = Paths.get(uploadDir, relativePath).normalize();

                System.out.println("real srcPath = " + srcPath.toAbsolutePath());
                System.out.println("exists = " + Files.exists(srcPath));

                if (!Files.exists(srcPath) || !Files.isRegularFile(srcPath)) {
                    System.out.println("File not found: " + srcPath.toAbsolutePath());
                    continue;
                }

                String targetName = ZipFileUtil.uniqueFileName(
                        srcPath.getFileName().toString(),
                        usedNames
                );

                Path targetPath = imageDir.resolve(targetName);

                Files.copy(srcPath, targetPath, StandardCopyOption.REPLACE_EXISTING);

                // path tương đối trong file zip để Excel tạo hyperlink
                row.setImagePath("images/" + targetName);
            }

            byte[] excelBytes =
                    excelHyperlinkExportService.exportByMonthAndCompanyId(month, companyId, rows);

            String excelFileName = "bao_cao_chi_so_dien_" + companyId + "_" + month + ".xlsx";
            Files.write(tempDir.resolve(excelFileName), excelBytes);

            return ZipFileUtil.zipDirectory(tempDir);

        } finally {
            ZipFileUtil.deleteDirectory(tempDir);
        }
    }

    private String getExtension(String path) {
        int dot = path.lastIndexOf('.');
        if (dot >= 0) {
            return path.substring(dot);
        }
        return ".jpg";
    }
}