package com.index_electric_server.common.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class ZipFileUtil {

    private ZipFileUtil() {
    }

    public record ZipImageItem(String sourcePath, String zipFileName) {}

    public static byte[] buildZip(
            String excelFileName,
            byte[] excelBytes,
            List<ZipImageItem> images
    ) throws Exception {

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(out)) {

            addBytesEntry(zos, excelFileName, excelBytes);

            if (images != null && !images.isEmpty()) {
                addFolderEntry(zos, "images");

                Set<String> usedNames = new LinkedHashSet<>();

                for (ZipImageItem item : images) {
                    if (item == null) continue;
                    if (item.sourcePath() == null || item.sourcePath().isBlank()) continue;
                    if (item.zipFileName() == null || item.zipFileName().isBlank()) continue;

                    File file = new File(item.sourcePath());
                    if (!file.exists() || !file.isFile()) {
                        continue;
                    }

                    String safeName = uniqueFileName(item.zipFileName(), usedNames);
                    String zipImagePath = "images/" + safeName;

                    addFileEntry(zos, zipImagePath, file);
                }
            }

            zos.finish();
            return out.toByteArray();
        }
    }

    public static void addBytesEntry(
            ZipOutputStream zos,
            String entryName,
            byte[] data
    ) throws Exception {
        if (entryName == null || entryName.isBlank()) {
            throw new IllegalArgumentException("entryName must not be blank");
        }
        if (data == null) {
            throw new IllegalArgumentException("data must not be null");
        }

        ZipEntry entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);
        zos.write(data);
        zos.closeEntry();
    }

    public static void addFileEntry(
            ZipOutputStream zos,
            String entryName,
            File file
    ) throws Exception {
        if (entryName == null || entryName.isBlank()) {
            throw new IllegalArgumentException("entryName must not be blank");
        }
        if (file == null || !file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("file is invalid: " + file);
        }

        ZipEntry entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);

        try (InputStream is = new FileInputStream(file)) {
            is.transferTo(zos);
        }

        zos.closeEntry();
    }

    public static void addFolderEntry(ZipOutputStream zos, String folderName) throws Exception {
        if (folderName == null || folderName.isBlank()) {
            throw new IllegalArgumentException("folderName must not be blank");
        }

        String normalized = folderName.endsWith("/") ? folderName : folderName + "/";
        ZipEntry entry = new ZipEntry(normalized);
        zos.putNextEntry(entry);
        zos.closeEntry();
    }

    public static String uniqueFileName(String originalName, Set<String> usedNames) {
        if (originalName == null || originalName.isBlank()) {
            originalName = "file";
        }

        String sanitized = sanitizeFileName(originalName);

        if (!usedNames.contains(sanitized)) {
            usedNames.add(sanitized);
            return sanitized;
        }

        String base = sanitized;
        String ext = "";

        int dotIndex = sanitized.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < sanitized.length() - 1) {
            base = sanitized.substring(0, dotIndex);
            ext = sanitized.substring(dotIndex);
        }

        int counter = 1;
        while (true) {
            String candidate = base + "_" + counter + ext;
            if (!usedNames.contains(candidate)) {
                usedNames.add(candidate);
                return candidate;
            }
            counter++;
        }
    }
    public static byte[] zipDirectory(Path sourceDir) throws Exception {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(out)) {

            Files.walk(sourceDir)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        String entryName = sourceDir.relativize(path).toString().replace("\\", "/");
                        try (InputStream is = Files.newInputStream(path)) {
                            zos.putNextEntry(new ZipEntry(entryName));
                            is.transferTo(zos);
                            zos.closeEntry();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });

            zos.finish();
            return out.toByteArray();
        }
    }
    public static void deleteDirectory(Path dir) {
        if (dir == null || !Files.exists(dir)) return;

        try {
            Files.walk(dir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static String sanitizeFileName(String fileName) {
        return fileName.replace("\\", "_")
                .replace("/", "_")
                .replace(":", "_")
                .replace("*", "_")
                .replace("?", "_")
                .replace("\"", "_")
                .replace("<", "_")
                .replace(">", "_")
                .replace("|", "_");
    }

    public static byte[] readFileBytes(String path) throws Exception {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("path must not be blank");
        }
        return Files.readAllBytes(new File(path).toPath());
    }

}