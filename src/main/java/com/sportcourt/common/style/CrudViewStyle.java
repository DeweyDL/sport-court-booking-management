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

    // Dimension constants scaled via dimFactor (1920 baseline).
    // Note: the status pill itself no longer uses these for fixed sizing — it
    // auto-sizes to its content. These values remain as layout hints for panels
    // that need a minimum column width for status columns.
    public static final int STATUS_PILL_WIDTH  = UIScale.scaleFontInt(120f);
    public static final int STATUS_PILL_HEIGHT = UIScale.scaleFontInt(28f);
    public static final int TOOLBAR_CONTROL_HEIGHT = UIScale.scale(41);
    public static final int TOOLBAR_SEARCH_WIDTH = UIScale.scale(270);
    public static final int TOOLBAR_SORT_WIDTH = UIScale.scale(214);
    public static final int ROW_HEIGHT = UIScale.scale(72);

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
        int width = root.getWidth() > 0 ? root.getWidth() : UIScale.getScreenWidth();
        float scale = resolveTextScale(width);
        scaleComponentTree(root, root, scale);
        root.revalidate();
        root.repaint();
    }

    public static JPanel createToolbarActionsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(0, UIScale.scale(6), 0, 0));
        return panel;
    }

    public static void applyToolbarButtonHeight(AbstractButton button) {
        button.putClientProperty("crud.toolbarFixedHeight", TOOLBAR_CONTROL_HEIGHT);
        Dimension preferred = button.getPreferredSize();
        button.setPreferredSize(new Dimension(preferred.width, TOOLBAR_CONTROL_HEIGHT));
        button.setMinimumSize(new Dimension(preferred.width, TOOLBAR_CONTROL_HEIGHT));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, TOOLBAR_CONTROL_HEIGHT));
    }

    public static JPanel createSearchFieldWithIcon(JPanel wrapper, JTextField searchField, Icon icon) {
        wrapper.removeAll();
        wrapper.setOpaque(false);
        Dimension size = new Dimension(TOOLBAR_SEARCH_WIDTH, TOOLBAR_CONTROL_HEIGHT);
        wrapper.setPreferredSize(size);
        wrapper.setMinimumSize(size);
        wrapper.setMaximumSize(size);

        searchField.setFont(new Font("Segoe UI", Font.PLAIN, UIScale.scale(14)));
        searchField.setPreferredSize(size);
        searchField.putClientProperty("JTextField.padding", new Insets(5, 8, 5, 10));
        searchField.putClientProperty("JComponent.roundRect", true);
        searchField.setBorder(null);
        searchField.setOpaque(false);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setBorder(new EmptyBorder(0, 0, 0, UIScale.scale(8)));

        int arc = UIScale.scale(28);
        JPanel innerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                g2.setColor(BORDER);
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arc, arc);
                g2.dispose();
            }
        };
        innerPanel.setOpaque(false);
        innerPanel.setPreferredSize(size);
        innerPanel.setMaximumSize(size);
        innerPanel.setBorder(new EmptyBorder(0, UIScale.scale(12), 0, UIScale.scale(12)));
        innerPanel.add(iconLabel, BorderLayout.WEST);
        innerPanel.add(searchField, BorderLayout.CENTER);

        wrapper.add(innerPanel, BorderLayout.CENTER);
        return wrapper;
    }

    public static JPanel createSortWrapper(JComboBox<String> sortBox, JButton directionButton) {
        sortBox.setFont(new Font("Segoe UI", Font.PLAIN, UIScale.scale(14)));
        sortBox.setFocusable(false);
        sortBox.setBorder(BorderFactory.createEmptyBorder(0, UIScale.scale(12), 0, UIScale.scale(12)));
        sortBox.setOpaque(false);
        sortBox.setBackground(Color.WHITE);
        sortBox.putClientProperty("JComponent.roundRect", true);
        sortBox.putClientProperty("JComponent.arc", 999);
        sortBox.putClientProperty("JComboBox.buttonStyle", "button");
        sortBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                Object display = index < 0 ? "Sắp xếp: " + value : value;
                JLabel label = (JLabel) super.getListCellRendererComponent(list, display, index, isSelected, cellHasFocus);
                label.setBorder(new EmptyBorder(UIScale.scale(6), UIScale.scale(10), UIScale.scale(6), UIScale.scale(10)));
                return label;
            }
        });

        directionButton.setFont(new Font("Segoe UI Symbol", Font.BOLD, UIScale.scale(11)));
        directionButton.setForeground(new Color(75, 85, 99));
        directionButton.setBorder(new EmptyBorder(0, 0, 0, UIScale.scale(12)));
        directionButton.setContentAreaFilled(false);
        directionButton.setBorderPainted(false);
        directionButton.setFocusPainted(false);
        directionButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        int sortArc = UIScale.scale(28);
        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), sortArc, sortArc);
                g2.dispose();
            }

            @Override
            public void paint(Graphics graphics) {
                super.paint(graphics);
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BORDER);
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, sortArc, sortArc);
                g2.dispose();
            }
        };
        Dimension size = new Dimension(TOOLBAR_SORT_WIDTH, TOOLBAR_CONTROL_HEIGHT);
        wrapper.setOpaque(false);
        wrapper.setPreferredSize(size);
        wrapper.setMinimumSize(size);
        wrapper.setMaximumSize(size);
        wrapper.add(sortBox, BorderLayout.CENTER);
        wrapper.add(directionButton, BorderLayout.EAST);
        return wrapper;
    }

    public static void updateSortDirectionButton(JButton directionButton, boolean sortAscending) {
        directionButton.setText(sortAscending ? "\u25B2" : "\u25BC");
        directionButton.setToolTipText(sortAscending
                ? "Đang sắp xếp tăng dần"
                : "Đang sắp xếp giảm dần");
    }

    public static JPanel createStatusPill(String text, Color background, Color foreground) {
        JLabel textLabel = new JLabel(text == null || text.isBlank() ? "--" : text);
        // Raw size — installResponsiveTypography scales it via fontFactor; no fixed width needed.
        textLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        textLabel.setForeground(foreground);

        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);

        GridBagConstraints dotConstraints = new GridBagConstraints();
        dotConstraints.gridx = 0;
        dotConstraints.gridy = 0;
        dotConstraints.insets = new Insets(UIScale.scale(2), 0, 0, UIScale.scale(6));
        content.add(createStatusDot(foreground), dotConstraints);

        GridBagConstraints textConstraints = new GridBagConstraints();
        textConstraints.gridx = 1;
        textConstraints.gridy = 0;
        content.add(textLabel, textConstraints);

        // Pill sizes itself to its content; EmptyBorder provides the visual padding.
        // No fixed min/max/preferred — the scaled font fits without clipping.
        int hPad = UIScale.scale(10);
        int vPad = UIScale.scale(5);
        JPanel pill = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(background);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
            }
        };
        pill.setOpaque(false);
        pill.setBorder(new EmptyBorder(vPad, hPad, vPad, hPad));
        pill.add(content);

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.add(pill);
        return wrapper;
    }

    private static JPanel createStatusDot(Color color) {
        JPanel dot = new JPanel() {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        // Dot sized to match the scaled font baseline.
        int dotSize = UIScale.scaleFontInt(6f);
        Dimension size = new Dimension(dotSize, dotSize);
        dot.setOpaque(false);
        dot.setPreferredSize(size);
        dot.setMinimumSize(size);
        dot.setMaximumSize(size);
        return dot;
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
        int width = component.getWidth() > 0 ? component.getWidth() : UIScale.getScreenWidth();
        int horizontal = clamp(Math.round(width * 0.055f), UIScale.scale(24), UIScale.scale(70));
        int top = clamp(Math.round(width * 0.045f), UIScale.scale(28), UIScale.scale(60));
        int bottom = clamp(Math.round(width * 0.035f), UIScale.scale(28), UIScale.scale(46));
        component.setBorder(new EmptyBorder(top, horizontal, bottom, horizontal));
    }

    private static void scaleComponentTree(Component component, JComponent root, float scale) {
        if (component instanceof JComponent swingComponent) {
            rememberBaseFont(swingComponent);
            Font baseFont = (Font) swingComponent.getClientProperty(BASE_FONT_PROPERTY);
            if (baseFont != null) {
                // Minimum readable size scales with the global factor; no artificial upper cap.
                float minSize = UIScale.scaleFont(10f);
                float scaledSize = Math.max(minSize, baseFont.getSize2D() * scale);
                swingComponent.setFont(baseFont.deriveFont(scaledSize));
                Object fixedHeight = swingComponent.getClientProperty("crud.toolbarFixedHeight");
                if (fixedHeight instanceof Integer h) {
                    swingComponent.setPreferredSize(null);
                    Dimension natural = swingComponent.getPreferredSize();
                    swingComponent.setPreferredSize(new Dimension(natural.width, h));
                    swingComponent.setMinimumSize(new Dimension(natural.width, h));
                }
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

    /**
     * Returns the font scale factor for a panel.
     *
     * Uses the screen-based font factor only — no width-based reduction.
     * When a panel's content is too wide for the current window, the layout
     * scrolls rather than shrinking text.
     */
    private static float resolveTextScale(int width) {
        return UIScale.getFactor(); // fontFactor: larger on big screens, no width penalty
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
