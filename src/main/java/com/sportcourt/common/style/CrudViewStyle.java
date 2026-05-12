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
    public static final int STATUS_PILL_WIDTH = 112;
    public static final int STATUS_PILL_HEIGHT = 24;
    public static final int TOOLBAR_CONTROL_HEIGHT = 41;
    public static final int TOOLBAR_SEARCH_WIDTH = 270;
    public static final int TOOLBAR_SORT_WIDTH = 214;

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

    public static JPanel createToolbarActionsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(0, 6, 0, 0));
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

        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setPreferredSize(size);
        searchField.putClientProperty("JTextField.padding", new Insets(5, 8, 5, 10));
        searchField.putClientProperty("JComponent.roundRect", true);
        searchField.setBorder(null);
        searchField.setOpaque(false);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setBorder(new EmptyBorder(0, 0, 0, 8));

        JPanel innerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 28, 28);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 28, 28);
                g2.dispose();
            }
        };
        innerPanel.setOpaque(false);
        innerPanel.setPreferredSize(size);
        innerPanel.setMaximumSize(size);
        innerPanel.setBorder(new EmptyBorder(0, 12, 0, 12));
        innerPanel.add(iconLabel, BorderLayout.WEST);
        innerPanel.add(searchField, BorderLayout.CENTER);

        wrapper.add(innerPanel, BorderLayout.CENTER);
        return wrapper;
    }

    public static JPanel createSortWrapper(JComboBox<String> sortBox, JButton directionButton) {
        sortBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sortBox.setFocusable(false);
        sortBox.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
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
                label.setBorder(new EmptyBorder(6, 10, 6, 10));
                return label;
            }
        });

        directionButton.setFont(new Font("Segoe UI Symbol", Font.BOLD, 11));
        directionButton.setForeground(new Color(75, 85, 99));
        directionButton.setBorder(new EmptyBorder(0, 0, 0, 12));
        directionButton.setContentAreaFilled(false);
        directionButton.setBorderPainted(false);
        directionButton.setFocusPainted(false);
        directionButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 28, 28);
                g2.dispose();
            }

            @Override
            public void paint(Graphics graphics) {
                super.paint(graphics);
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BORDER);
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 28, 28);
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
        Dimension size = new Dimension(STATUS_PILL_WIDTH, STATUS_PILL_HEIGHT);
        pill.setPreferredSize(size);
        pill.setMinimumSize(size);
        pill.setMaximumSize(size);

        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);

        GridBagConstraints dotConstraints = new GridBagConstraints();
        dotConstraints.gridx = 0;
        dotConstraints.gridy = 0;
        dotConstraints.insets = new Insets(2, 0, 0, 7);
        content.add(createStatusDot(foreground), dotConstraints);

        JLabel textLabel = new JLabel(text == null || text.isBlank() ? "--" : text);
        textLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        textLabel.setForeground(foreground);

        GridBagConstraints textConstraints = new GridBagConstraints();
        textConstraints.gridx = 1;
        textConstraints.gridy = 0;
        content.add(textLabel, textConstraints);

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
        Dimension size = new Dimension(5, 5);
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
        int width = component.getWidth() > 0 ? component.getWidth() : Toolkit.getDefaultToolkit().getScreenSize().width;
        int horizontal = clamp(Math.round(width * 0.055f), 24, 70);
        int top = clamp(Math.round(width * 0.045f), 28, 60);
        int bottom = clamp(Math.round(width * 0.035f), 28, 46);
        component.setBorder(new EmptyBorder(top, horizontal, bottom, horizontal));
    }

    private static void scaleComponentTree(Component component, JComponent root, float scale) {
        if (component instanceof JComponent swingComponent) {
            rememberBaseFont(swingComponent);
            Font baseFont = (Font) swingComponent.getClientProperty(BASE_FONT_PROPERTY);
            if (baseFont != null) {
                float scaledSize = clamp(baseFont.getSize2D() * scale, 11f, Math.max(11f, baseFont.getSize2D() + 4f));
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
