package com.sportcourt.modules.area.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

// Utility nho de area module doc query SQL tu file resources thay vi hard-code trong DAO.
public final class AreaSqlLoader {
    private AreaSqlLoader() {
    }

    public static String load(String resourcePath) {
        try (InputStream inputStream = AreaSqlLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalStateException("Khong tim thay file SQL: " + resourcePath);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Khong the doc file SQL: " + resourcePath, exception);
        }
    }
}
