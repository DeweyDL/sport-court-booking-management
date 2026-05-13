package com.sportcourt.common.style;

import java.awt.*;
import java.io.InputStream;

public final class AppFonts {
    private static Font lexendRegular;
    private static Font lexendBold;
    private static boolean loaded;

    private AppFonts() {
    }

    public static synchronized void register() {
        if (loaded) {
            return;
        }
        lexendRegular = loadFont("/font/Lexend-Regular.ttf");
        lexendBold = loadFont("/font/Lexend-Bold.ttf");
        loaded = true;
    }

    public static Font lexendRegular(float size) {
        register();
        float scaled = UIScale.scaleFont(size);
        if (lexendRegular != null) {
            return lexendRegular.deriveFont(Font.PLAIN, scaled);
        }
        return new Font("SansSerif", Font.PLAIN, Math.round(scaled));
    }

    public static Font lexendBold(float size) {
        register();
        float scaled = UIScale.scaleFont(size);
        if (lexendBold != null) {
            return lexendBold.deriveFont(Font.BOLD, scaled);
        }
        return new Font("SansSerif", Font.BOLD, Math.round(scaled));
    }

    private static Font loadFont(String resourcePath) {
        try (InputStream stream = AppFonts.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                return null;
            }
            Font font = Font.createFont(Font.TRUETYPE_FONT, stream);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
            return font;
        } catch (Exception e) {
            return null;
        }
    }
}
