package com.sportcourt.modules.customer_booking.view;

import com.sportcourt.common.style.AppFonts;
import com.sportcourt.common.style.UIScale;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;

final class CustomerBookingViewStyle {
    static final Color PAGE_BG = new Color(248, 249, 252);
    static final Color SURFACE_BG = Color.WHITE;
    static final Color PANEL_BG = new Color(243, 243, 246);
    static final Color PANEL_DARK = new Color(60, 75, 53);
    static final Color BORDER = new Color(228, 228, 231);
    static final Color TEXT_DARK = new Color(26, 28, 30);
    static final Color BRAND_DARK = new Color(15, 23, 42);
    static final Color TEXT_MUTED = new Color(113, 113, 122);
    static final Color TEXT_OLIVE = new Color(60, 75, 53);
    static final Color GREEN = new Color(57, 255, 20);
    static final Color GREEN_DARK = new Color(16, 110, 0);
    static final Color TAG_BLUE_BG = new Color(192, 235, 255, 90);
    static final Color TAG_BLUE_TEXT = new Color(0, 97, 171);
    static final Color SOFT_GRAY = new Color(232, 232, 234);

    private CustomerBookingViewStyle() {
    }

    static int s(int value) {
        return UIScale.scale(value);
    }

    static Font regular(float size) {
        return AppFonts.lexendRegular(size);
    }

    static Font bold(float size) {
        return AppFonts.lexendBold(size);
    }

    static void prepareText(JComponent component, Font font, Color color) {
        component.setFont(font);
        component.setForeground(color);
    }

    static JLabel label(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        prepareText(label, font, color);
        return label;
    }

    static JButton pillButton(String text, Color background, Color foreground) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(background);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(graphics);
            }
        };
        button.setFont(bold(13f));
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(s(8), s(22), s(8), s(22)));
        return button;
    }

    static Icon icon(String resourcePath, int width, int height) {
        URL url = CustomerBookingViewStyle.class.getResource(resourcePath);
        if (url == null) {
            return null;
        }
        Image image = new ImageIcon(url).getImage()
                .getScaledInstance(s(width), s(height), Image.SCALE_SMOOTH);
        return new ImageIcon(image);
    }

    static Image loadImage(String... resourcePaths) {
        for (String path : resourcePaths) {
            URL url = CustomerBookingViewStyle.class.getResource(path);
            if (url != null) {
                return new ImageIcon(url).getImage();
            }
        }
        return null;
    }

    static JScrollPane cleanScrollPane(Component view) {
        JScrollPane scrollPane = new JScrollPane(view);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(PAGE_BG);
        scrollPane.setBackground(PAGE_BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(s(18));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return scrollPane;
    }

    static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color background;
        private final boolean shadow;

        RoundedPanel(int radius, Color background) {
            this(radius, background, false);
        }

        RoundedPanel(int radius, Color background, boolean shadow) {
            this.radius = radius;
            this.background = background;
            this.shadow = shadow;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int shadowOffset = shadow ? s(6) : 0;
            int height = Math.max(0, getHeight() - shadowOffset);
            if (shadow) {
                g2.setColor(new Color(0, 0, 0, 12));
                g2.fillRoundRect(0, shadowOffset / 2, getWidth(), height, radius, radius);
            }
            g2.setColor(background);
            g2.fillRoundRect(0, 0, getWidth(), height, radius, radius);
            g2.dispose();
            super.paintComponent(graphics);
        }
    }

    static final class RoundedBorder extends AbstractBorder {
        private final Color color;
        private final int radius;

        RoundedBorder(Color color, int radius) {
            this.color = color;
            this.radius = radius;
        }

        @Override
        public Insets getBorderInsets(Component component) {
            return new Insets(s(1), s(1), s(1), s(1));
        }

        @Override
        public void paintBorder(Component component, Graphics graphics, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x + 1, y + 1, width - 3, height - 3, radius, radius);
            g2.dispose();
        }
    }

    static final class HeroPanel extends JPanel {
        private final Image image;
        private final String title;

        HeroPanel(String title) {
            this.title = title;
            this.image = loadImage("/image/court.png", "/image/court2.png", "/image/BgDashboard.png");
            setOpaque(false);
            setPreferredSize(new Dimension(0, s(320)));
            setMinimumSize(new Dimension(0, s(260)));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int arc = s(32);
            Shape clip = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc);
            g2.setClip(clip);
            if (image != null) {
                g2.drawImage(image, 0, 0, getWidth(), getHeight(), this);
            } else {
                g2.setColor(new Color(90, 130, 80));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
            int bandHeight = Math.max(s(105), getHeight() / 3);
            g2.setColor(PANEL_DARK);
            g2.fillRect(0, getHeight() - bandHeight, getWidth(), bandHeight);

            g2.setClip(null);
            g2.setFont(bold(34f));
            g2.setColor(Color.WHITE);
            FontMetrics metrics = g2.getFontMetrics();
            int x = (getWidth() - metrics.stringWidth(title)) / 2;
            int y = getHeight() - bandHeight / 2 + metrics.getAscent() / 2 - s(4);
            g2.drawString(title, Math.max(s(24), x), y);
            g2.dispose();
        }
    }
}
