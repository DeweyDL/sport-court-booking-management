package com.sportcourt.common.style;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * Central scale utility.  Two independent scale factors:
 *
 *   dimFactor  — 1920×1080 baseline, used for pixel dimensions (toolbar heights,
 *                sidebar widths, icon sizes, border padding, etc.)
 *
 *   fontFactor — 1440×900 baseline, used for all font sizes.  A 1920×1080
 *                monitor therefore gets fontFactor ≈ 1.2, making fonts naturally
 *                larger without inflating UI chrome.
 *
 * Call UIScale.init() once from App.main() before any UI is constructed.
 */
public final class UIScale {

    private static final float DIM_BASE_W  = 1920f;
    private static final float DIM_BASE_H  = 1080f;
    private static final float FONT_BASE_W = 1440f;
    private static final float FONT_BASE_H =  900f;

    private static float dimFactor  = 1.0f;
    private static float fontFactor = 1.0f;

    private static int screenWidth  = 1920;
    private static int screenHeight = 1080;

    private UIScale() {}

    public static void init() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        screenWidth  = screen.width;
        screenHeight = screen.height;

        float dx = screenWidth  / DIM_BASE_W;
        float dy = screenHeight / DIM_BASE_H;
        dimFactor = Math.max(0.70f, Math.min(1.50f, Math.min(dx, dy)));

        float fx = screenWidth  / FONT_BASE_W;
        float fy = screenHeight / FONT_BASE_H;
        fontFactor = Math.max(0.70f, Math.min(1.50f, Math.min(fx, fy)));
    }

    /** Font scale factor (1440×900 baseline). Used by resolveTextScale. */
    public static float getFactor() { return fontFactor; }

    public static int  getScreenWidth()  { return screenWidth;  }
    public static int  getScreenHeight() { return screenHeight; }

    /** Scale an integer pixel dimension (toolbar heights, icon sizes, etc.). */
    public static int scale(int base) { return Math.round(base * dimFactor); }

    /** Scale a float pixel dimension. */
    public static float scale(float base) { return base * dimFactor; }

    /**
     * Scale a font size.
     * Uses fontFactor so fonts are naturally larger on big monitors.
     */
    public static float scaleFont(float base) { return base * fontFactor; }

    /**
     * Scale a font size and return the nearest int (for new Font(..., size) calls).
     */
    public static int scaleFontInt(float base) { return Math.round(base * fontFactor); }

    /** Create a scaled ImageIcon. baseW/baseH are design-size pixels. */
    public static ImageIcon scaleIcon(URL url, int baseW, int baseH) {
        if (url == null) return new ImageIcon();
        Image img = new ImageIcon(url).getImage()
                .getScaledInstance(scale(baseW), scale(baseH), Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }
}
