package com.sportcourt.common.style;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * Central scale utility: maps screen resolution to a global scale factor
 * relative to a 1920×1080 baseline.
 *
 * Call UIScale.init() once in App.main() before any UI is constructed.
 * All font sizes, pixel dimensions, and icon sizes in the codebase should
 * go through the helpers here so the UI fits any monitor size automatically.
 */
public final class UIScale {

    private static final float BASE_WIDTH = 1920f;
    private static final float BASE_HEIGHT = 1080f;

    private static float factor = 1.0f;
    private static int screenWidth = 1920;
    private static int screenHeight = 1080;

    private UIScale() {}

    /** Must be called once from App.main() before any UI is constructed. */
    public static void init() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        screenWidth = screen.width;
        screenHeight = screen.height;

        // Use the smaller dimension ratio so nothing overflows
        float scaleX = screenWidth / BASE_WIDTH;
        float scaleY = screenHeight / BASE_HEIGHT;
        factor = Math.min(scaleX, scaleY);

        // Clamp: never below 70 % (very small screens) or above 150 % (large 4K)
        factor = Math.max(0.70f, Math.min(1.50f, factor));
    }

    public static float getFactor() {
        return factor;
    }

    public static int getScreenWidth() {
        return screenWidth;
    }

    public static int getScreenHeight() {
        return screenHeight;
    }

    /** Scale an integer pixel dimension. */
    public static int scale(int base) {
        return Math.round(base * factor);
    }

    /** Scale a float pixel dimension. */
    public static float scale(float base) {
        return base * factor;
    }

    /** Scale a font size. */
    public static float scaleFont(float base) {
        return base * factor;
    }

    /**
     * Create a scaled ImageIcon from a classpath URL.
     * Pass the base (design) dimensions; they will be scaled automatically.
     */
    public static ImageIcon scaleIcon(URL url, int baseW, int baseH) {
        if (url == null) return new ImageIcon();
        Image img = new ImageIcon(url).getImage()
                .getScaledInstance(scale(baseW), scale(baseH), Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }
}
