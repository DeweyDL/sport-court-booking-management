package com.sportcourt.modules.area.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.Properties;

// Quan ly viec luu anh khu vuc tren may nguoi dung. Backend logic duoc tach ra khoi Swing view.
public final class AreaImageStorage {
    private static final Path STORAGE_DIR = Paths.get(System.getProperty("user.home"), ".rentsta", "area-images");
    private static final Path INDEX_FILE = STORAGE_DIR.resolve("area-image-index.properties");

    private AreaImageStorage() {
    }

    public static Optional<Path> findImagePath(String maKv) {
        Properties properties = loadIndex();
        String storedPath = properties.getProperty(maKv);
        if (storedPath == null || storedPath.isBlank()) {
            return Optional.empty();
        }

        Path path = Paths.get(storedPath);
        return Files.exists(path) ? Optional.of(path) : Optional.empty();
    }

    public static Path saveImage(String maKv, Path sourceFile) {
        try {
            Files.createDirectories(STORAGE_DIR);

            String originalName = sourceFile.getFileName().toString();
            String extension = "";
            int extensionIndex = originalName.lastIndexOf('.');
            if (extensionIndex >= 0) {
                extension = originalName.substring(extensionIndex);
            }

            Path targetFile = STORAGE_DIR.resolve(maKv + extension);
            Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);

            Properties properties = loadIndex();
            properties.setProperty(maKv, targetFile.toString());
            saveIndex(properties);
            return targetFile;
        } catch (IOException exception) {
            throw new IllegalStateException("Khong the luu anh khu vuc: " + exception.getMessage(), exception);
        }
    }

    private static Properties loadIndex() {
        Properties properties = new Properties();
        if (!Files.exists(INDEX_FILE)) {
            return properties;
        }

        try (InputStream inputStream = Files.newInputStream(INDEX_FILE)) {
            properties.load(inputStream);
            return properties;
        } catch (IOException exception) {
            throw new IllegalStateException("Khong the doc thong tin anh khu vuc: " + exception.getMessage(), exception);
        }
    }

    private static void saveIndex(Properties properties) {
        try (OutputStream outputStream = Files.newOutputStream(INDEX_FILE)) {
            properties.store(outputStream, "RentSta area image index");
        } catch (IOException exception) {
            throw new IllegalStateException("Khong the cap nhat thong tin anh khu vuc: " + exception.getMessage(), exception);
        }
    }
}
