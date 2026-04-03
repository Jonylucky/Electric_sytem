package com.index_electric_server.reading.service;

import com.index_electric_server.common.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class StorageService {

    @Value("${storage.base-dir:uploads}")
    private String baseDir;

    public String saveImageBytes(byte[] bytes, String month, String fileName) throws IOException {
        if (bytes == null || bytes.length == 0) return null;

        Path dir = Paths.get(baseDir, month, "img");
        Files.createDirectories(dir);

        Path out = dir.resolve(fileName);
        Files.write(out, bytes);

        return "/uploads/" + month + "/img/" + fileName;
    }

    private String getExt(String name) {
        int idx = name.lastIndexOf('.');
        if (idx < 0) return "";
        return name.substring(idx);
    }
}