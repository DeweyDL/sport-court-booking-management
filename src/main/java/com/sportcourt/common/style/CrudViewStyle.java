package com.sportcourt.common.style;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;

public final class CrudViewStyle {
    public static final Color PAGE_BACKGROUND = new Color(245, 247, 250);
    public static final Color TABLE_HEADER_BACKGROUND = new Color(248, 249, 250);
    public static final Color ALTERNATE_ROW_BACKGROUND = new Color(248, 250, 252);
    public static final Color BORDER = new Color(229, 231, 235);
    public static final Color ROW_BORDER = new Color(243, 244, 246);
    public static final Color TEXT = new Color(17, 24, 39);
    public static final Color TEXT_DARK = new Color(30, 31, 36);
    public static final Color MUTED = new Color(107, 114, 128);
    public static final Color SUCCESS_TEXT = new Color(16, 110, 0);
    public static final Color SUCCESS_BG = new Color(228, 250, 226);
    public static final Color DANGER_TEXT = new Color(185, 28, 28);
    public static final Color DANGER_BG = new Color(254, 226, 226);
    public static final Color EDIT_TEXT = new Color(29, 78, 216);
    public static final Color EDIT_BG = new Color(239, 246, 255);

    private static final String BASE_FONT_PROPERTY = "crud.baseFont";
    private static final String TYPOGRAPHY_LISTENER_PROPERTY = "crud.typographyListenerInstalled";
    private static final String CONTAINER_LISTENER_PROPERTY = "crud.containerListenerInstalled";

    private CrudViewStyle() {
    }

    public static void applyPageDefaults(JPanel panel) {
        panel.setBackground(PAGE_BACKGROUND);
        installResponsivePageBorder(panel);
    }

    public static void installResponsiveTypography(JComponent root) {
        if (Boolean.TRUE.equals(root.getClientProperty(TYPOGRAPHY_LISTENER_PROPERTY))) {
            return;
        }
        root.putClientProperty(TYPOGRAPHY_LISTENER_PROPERTY, true);
        root.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                applyResponsiveTypography(root);
            }
        });
        SwingUtilities.invokeLater(() -> applyResponsiveTypography(root));
    }

    public static void applyResponsiveTypography(JComponent root) {
        int width = root.getWidth() > 0 ? root.getWidth() : Toolkit.getDefaultToolkit().getScreenSize().width;
        float scale = resolveTextScale(width);
        scaleComponentTree(root, root, scale);
        root.revalidate();
        root.repaint();
    }

    private static void installResponsivePageBorder(JComponent component) {
        updatePageBorder(component);
        component.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                updatePageBorder(component);
            }
        });
    }

    private static void updatePageBorder(JComponent component) {
        int width = component.getWidth() > 0 ? component.getWidth() : Toolkit.getDefaultToolkit().getScreenSize().width;
        int horizontal = clamp(Math.round(width * 0.055f), 24, 70);
        int top = clamp(Math.round(width * 0.070f), 48, 100);
        int bottom = clamp(Math.round(width * 0.040f), 32, 50);
        component.setBorder(new EmptyBorder(top, horizontal, bottom, horizontal));
    }

    private static void scaleComponentTree(Component component, JComponent root, float scale) {
        if (component instanceof JComponent swingComponent) {
            rememberBaseFont(swingComponent);
            Font baseFont = (Font) swingComponent.getClientProperty(BASE_FONT_PROPERTY);
            if (baseFont != null) {
                float scaledSize = clamp(baseFont.getSize2D() * scale, 11f, Math.max(11f, baseFont.getSize2D() + 4f));
                swingComponent.setFont(baseFont.deriveFont(scaledSize));
            }
        }

        if (component instanceof Container container) {
            installContainerListener(container, root);
            for (Component child : container.getComponents()) {
                scaleComponentTree(child, root, scale);
            }
        }
    }

    private static void rememberBaseFont(JComponent component) {
        if (component.getClientProperty(BASE_FONT_PROPERTY) == null && component.getFont() != null) {
            component.putClientProperty(BASE_FONT_PROPERTY, component.getFont());
        }
    }

    private static void installContainerListener(Container container, JComponent root) {
        if (!(container instanceof JComponent swingContainer)) {
            return;
        }
        if (Boolean.TRUE.equals(swingContainer.getClientProperty(CONTAINER_LISTENER_PROPERTY))) {
            return;
        }
        swingContainer.putClientProperty(CONTAINER_LISTENER_PROPERTY, true);
        container.addContainerListener(new ContainerAdapter() {
            @Override
            public void componentAdded(ContainerEvent event) {
                SwingUtilities.invokeLater(() -> applyResponsiveTypography(root));
            }
        });
    }

    private static float resolveTextScale(int width) {
        if (width < 900) {
            return 0.88f;
        }
        if (width < 1200) {
            return 0.94f;
        }
        if (width > 1700) {
            return 1.05f;
        }
        return 1.0f;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
