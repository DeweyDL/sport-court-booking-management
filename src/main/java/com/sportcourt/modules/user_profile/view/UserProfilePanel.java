package com.sportcourt.modules.user_profile.view;

import com.formdev.flatlaf.FlatLightLaf;
import com.sportcourt.common.style.AppFonts;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.Locale;

public class UserProfilePanel extends JPanel implements Scrollable {
    private static final Color PAGE_BACKGROUND = new Color(249, 249, 252);
    private static final Color CARD_BACKGROUND = Color.WHITE;
    private static final Color FIELD_BACKGROUND = new Color(239, 255, 236);
    private static final Color TEXT_DARK = new Color(26, 28, 30);
    private static final Color TEXT_MUTED = new Color(60, 75, 53);
    private static final Color BRAND_DARK = new Color(15, 23, 42);
    private static final Color LIME = new Color(163, 230, 53);
    private static final Color LIME_DARK = new Color(16, 110, 0);
    private static final Color BUTTON_TEXT = new Color(11, 72, 0);
    private static final Color DARK_BUTTON = new Color(47, 49, 51);

    private final JLabel displayNameLabel = new JLabel();
    private final JLabel roleLabel = new JLabel();
    private final JLabel fullNameValue = new JLabel();
    private final JLabel phoneValue = new JLabel();
    private final JLabel emailValue = new JLabel();
    private final JLabel birthDateValue = new JLabel();
    private final JTextArea addressValue = new JTextArea();
    private final JLabel rankValue = new JLabel();
    private final AvatarView avatarView = new AvatarView();
    private final JButton editProfileButton;
    private final JButton changePasswordButton;

    public UserProfilePanel() {
        AppFonts.register();
        setLayout(new BorderLayout());
        setBackground(PAGE_BACKGROUND);
        setBorder(new EmptyBorder(34, 50, 56, 50));

        editProfileButton = createActionButton("Chỉnh sửa hồ sơ", new LineIcon(Symbol.EDIT, 18, BUTTON_TEXT));
        changePasswordButton = createActionButton("Đổi mật khẩu", new LineIcon(Symbol.LOCK, 18, BUTTON_TEXT));

        add(createPage(), BorderLayout.CENTER);
        bindProfile(UserProfileData.sample());
    }

    public void bindProfile(UserProfileData profile) {
        displayNameLabel.setText(toDisplayNameHtml(profile.fullName()));
        roleLabel.setText(profile.roleName());
        fullNameValue.setText(profile.fullName());
        phoneValue.setText(profile.phoneNumber());
        emailValue.setText(profile.email());
        birthDateValue.setText(profile.birthDate());
        addressValue.setText(profile.address());
        rankValue.setText(profile.customerRank());
        avatarView.setDisplayName(profile.fullName());
    }

    public void setEditProfileAction(ActionListener listener) {
        replaceAction(editProfileButton, listener);
    }

    public void setChangePasswordAction(ActionListener listener) {
        replaceAction(changePasswordButton, listener);
    }

    private JPanel createPage() {
        JPanel page = new JPanel();
        page.setOpaque(false);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.add(createBrandHeader());
        page.add(Box.createVerticalStrut(28));
        page.add(createHeroSection());
        page.add(Box.createVerticalStrut(30));
        page.add(createInfoCard());
        page.add(Box.createVerticalGlue());
        return page;
    }

    private JPanel createBrandHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        JLabel brand = new JLabel("RENTSTA");
        brand.setFont(AppFonts.lexendBold(42f));
        brand.setForeground(BRAND_DARK);
        header.add(brand, BorderLayout.WEST);
        return header;
    }

    private JPanel createHeroSection() {
        JPanel hero = new JPanel(new GridBagLayout());
        hero.setOpaque(false);
        hero.setAlignmentX(Component.LEFT_ALIGNMENT);
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 28);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        hero.add(createAvatarBlock(), gbc);

        JPanel nameBlock = createNameBlock();
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        hero.add(nameBlock, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 18, 0, 0);
        hero.add(createButtonStack(), gbc);

        return hero;
    }

    private JComponent createAvatarBlock() {
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(176, 176));
        layeredPane.setMinimumSize(new Dimension(176, 176));
        layeredPane.setMaximumSize(new Dimension(176, 176));

        avatarView.setBounds(16, 16, 144, 144);
        layeredPane.add(avatarView, JLayeredPane.DEFAULT_LAYER);

        CircleIconButton cameraButton = new CircleIconButton(new LineIcon(Symbol.CAMERA, 15, LIME));
        cameraButton.setBounds(130, 130, 30, 30);
        cameraButton.setToolTipText("Đổi ảnh đại diện");
        layeredPane.add(cameraButton, JLayeredPane.PALETTE_LAYER);
        return layeredPane;
    }

    private JPanel createNameBlock() {
        JPanel nameBlock = new JPanel();
        nameBlock.setOpaque(false);
        nameBlock.setLayout(new BoxLayout(nameBlock, BoxLayout.Y_AXIS));

        displayNameLabel.setFont(AppFonts.lexendBold(34f));
        displayNameLabel.setForeground(TEXT_DARK);
        displayNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel roleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        roleRow.setOpaque(false);
        roleRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel roleIcon = new JLabel(new LineIcon(Symbol.SHIELD_CHECK, 18, LIME_DARK));
        roleLabel.setFont(AppFonts.lexendRegular(14f));
        roleLabel.setForeground(TEXT_MUTED);
        roleRow.add(roleIcon);
        roleRow.add(roleLabel);

        nameBlock.add(displayNameLabel);
        nameBlock.add(Box.createVerticalStrut(6));
        nameBlock.add(roleRow);
        return nameBlock;
    }

    private JPanel createButtonStack() {
        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        editProfileButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        changePasswordButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttons.add(editProfileButton);
        buttons.add(Box.createVerticalStrut(14));
        buttons.add(changePasswordButton);
        return buttons;
    }

    private JPanel createInfoCard() {
        InfoCardPanel card = new InfoCardPanel();
        card.setLayout(new BorderLayout(0, 28));
        card.setBorder(new EmptyBorder(32, 32, 38, 32));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(900, 420));

        JPanel heading = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        heading.setOpaque(false);
        heading.add(new AccentBar());

        JLabel title = new JLabel("Thông tin cá nhân");
        title.setFont(AppFonts.lexendBold(22f));
        title.setForeground(TEXT_DARK);
        heading.add(title);
        card.add(heading, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(0, 0, 18, 22);

        gbc.gridx = 0;
        gbc.gridy = 0;
        fields.add(createInfoField("Họ và tên", fullNameValue, 38), gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 22, 18, 0);
        fields.add(createInfoField("Số điện thoại", phoneValue, 38), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 18, 22);
        fields.add(createInfoField("Địa chỉ email", emailValue, 38), gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 22, 18, 0);
        fields.add(createInfoField("Ngày sinh", birthDateValue, 38), gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 0, 22);
        fields.add(createInfoField("Địa chỉ thường trú", addressValue, 78), gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 22, 0, 0);
        gbc.anchor = GridBagConstraints.NORTH;
        fields.add(createInfoField("Hạng khách hàng", rankValue, 38), gbc);

        card.add(fields, BorderLayout.CENTER);
        return card;
    }

    private JPanel createInfoField(String labelText, JComponent valueComponent, int valueHeight) {
        JPanel field = new JPanel();
        field.setOpaque(false);
        field.setLayout(new BoxLayout(field, BoxLayout.Y_AXIS));

        JLabel label = new JLabel(labelText.toUpperCase(Locale.forLanguageTag("vi-VN")));
        label.setFont(AppFonts.lexendBold(11f));
        label.setForeground(TEXT_MUTED);
        label.setBorder(new EmptyBorder(0, 8, 4, 8));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        RoundedPanel valueBox = new RoundedPanel(15, FIELD_BACKGROUND);
        valueBox.setLayout(new BorderLayout());
        valueBox.setBorder(new EmptyBorder(6, 10, 6, 10));
        valueBox.setPreferredSize(new Dimension(10, valueHeight));
        valueBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, valueHeight));
        valueBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (valueComponent instanceof JLabel labelValue) {
            labelValue.setFont(AppFonts.lexendRegular(15f));
            labelValue.setForeground(TEXT_DARK);
            valueBox.add(labelValue, BorderLayout.CENTER);
        } else if (valueComponent instanceof JTextArea textArea) {
            textArea.setFont(AppFonts.lexendRegular(15f));
            textArea.setForeground(TEXT_DARK);
            textArea.setOpaque(false);
            textArea.setEditable(false);
            textArea.setFocusable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setBorder(BorderFactory.createEmptyBorder());
            valueBox.add(textArea, BorderLayout.CENTER);
        }

        field.add(label);
        field.add(valueBox);
        return field;
    }

    private JButton createActionButton(String text, Icon icon) {
        JButton button = new JButton(text, icon) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(LIME);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(graphics);
            }
        };
        button.setPreferredSize(new Dimension(252, 48));
        button.setMaximumSize(new Dimension(252, 48));
        button.setMinimumSize(new Dimension(252, 48));
        button.setBorder(new EmptyBorder(0, 20, 0, 22));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setIconTextGap(14);
        button.setForeground(BUTTON_TEXT);
        button.setFont(AppFonts.lexendBold(16f));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private static void replaceAction(JButton button, ActionListener listener) {
        for (ActionListener actionListener : button.getActionListeners()) {
            button.removeActionListener(actionListener);
        }
        if (listener != null) {
            button.addActionListener(listener);
        }
    }

    private static String toDisplayNameHtml(String fullName) {
        String[] parts = fullName == null ? new String[0] : fullName.trim().split("\\s+");
        if (parts.length <= 2) {
            return "<html><div style='line-height:1.24'>" + escapeHtml(fullName) + "</div></html>";
        }
        StringBuilder builder = new StringBuilder("<html><div style='line-height:1.24'>");
        builder.append(escapeHtml(parts[0]));
        for (int index = 1; index < parts.length; index++) {
            builder.append("<br>").append(escapeHtml(parts[index]));
        }
        builder.append("</div></html>");
        return builder.toString();
    }

    private static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 16;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 96;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    public static void main(String[] args) {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("RentSta - Trang cá nhân");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setContentPane(new UserProfilePanel());
            frame.setSize(1024, 768);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    public record UserProfileData(
            String fullName,
            String roleName,
            String phoneNumber,
            String email,
            String birthDate,
            String address,
            String customerRank
    ) {
        public static UserProfileData sample() {
            return new UserProfileData(
                    "Marcus Alexander Thorne",
                    "Khách hàng",
                    "+84 901 234 567",
                    "marcus.thorne@rentsta.vn",
                    "15 tháng 05, 1990",
                    "Tầng 42, Tòa nhà Landmark 81, 720A Điện Biên Phủ, Phường 22, Bình Thạnh, TP. Hồ Chí Minh",
                    "Thân thiết"
            );
        }
    }

    private static class InfoCardPanel extends RoundedPanel {
        private InfoCardPanel() {
            super(32, CARD_BACKGROUND, true);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(57, 255, 20, 13));
            g2.fillOval(getWidth() - 120, -72, 192, 192);
            g2.dispose();
        }
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color background;
        private final boolean shadow;

        private RoundedPanel(int radius, Color background) {
            this(radius, background, false);
        }

        private RoundedPanel(int radius, Color background, boolean shadow) {
            this.radius = radius;
            this.background = background;
            this.shadow = shadow;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int contentHeight = shadow ? getHeight() - 8 : getHeight();
            if (shadow) {
                g2.setColor(new Color(26, 28, 30, 12));
                g2.fillRoundRect(0, 8, getWidth(), getHeight() - 8, radius, radius);
            }
            g2.setColor(background);
            g2.fillRoundRect(0, 0, getWidth(), contentHeight, radius, radius);
            g2.dispose();
            super.paintComponent(graphics);
        }
    }

    private static class AccentBar extends JComponent {
        private AccentBar() {
            setPreferredSize(new Dimension(7, 28));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(LIME_DARK);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getWidth(), getWidth());
            g2.dispose();
        }
    }

    private static class AvatarView extends JComponent {
        private String displayName = "";

        private AvatarView() {
            setOpaque(false);
        }

        private void setDisplayName(String displayName) {
            this.displayName = displayName == null ? "" : displayName;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int size = Math.min(getWidth(), getHeight()) - 8;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;

            g2.setColor(new Color(57, 255, 20, 55));
            g2.fillOval(x - 5, y - 5, size + 10, size + 10);
            g2.setColor(Color.WHITE);
            g2.fillOval(x, y, size, size);
            g2.setClip(new Ellipse2D.Float(x, y, size, size));

            g2.setColor(new Color(236, 253, 245));
            g2.fillOval(x, y, size, size);
            drawPortrait(g2, x, y, size);

            g2.setClip(null);
            g2.setStroke(new BasicStroke(4f));
            g2.setColor(new Color(249, 249, 252));
            g2.drawOval(x + 2, y + 2, size - 4, size - 4);
            g2.dispose();
        }

        private void drawPortrait(Graphics2D g2, int x, int y, int size) {
            int centerX = x + size / 2;
            int top = y + size / 5;

            g2.setColor(new Color(30, 41, 59));
            g2.fillOval(centerX - size / 4, top - 3, size / 2, size / 2);

            g2.setColor(new Color(251, 206, 177));
            g2.fillOval(centerX - size / 6, top + size / 7, size / 3, size / 3);

            g2.setColor(new Color(30, 41, 59));
            g2.fillArc(centerX - size / 5, top + 2, size / 2, size / 3, 90, 180);

            g2.setColor(new Color(15, 118, 110));
            Path2D jacket = new Path2D.Double();
            jacket.moveTo(centerX - size / 3.2, y + size * 0.76);
            jacket.lineTo(centerX + size / 3.2, y + size * 0.76);
            jacket.lineTo(centerX + size / 2.2, y + size);
            jacket.lineTo(centerX - size / 2.2, y + size);
            jacket.closePath();
            g2.fill(jacket);

            g2.setColor(Color.WHITE);
            Path2D shirt = new Path2D.Double();
            shirt.moveTo(centerX - size / 8.0, y + size * 0.75);
            shirt.lineTo(centerX + size / 8.0, y + size * 0.75);
            shirt.lineTo(centerX, y + size * 0.98);
            shirt.closePath();
            g2.fill(shirt);

            g2.setColor(new Color(239, 68, 68));
            Path2D tie = new Path2D.Double();
            tie.moveTo(centerX, y + size * 0.80);
            tie.lineTo(centerX + size / 13.0, y + size * 0.98);
            tie.lineTo(centerX - size / 13.0, y + size * 0.98);
            tie.closePath();
            g2.fill(tie);

            g2.setColor(TEXT_DARK);
            g2.setFont(AppFonts.lexendBold(10f));
            String initials = initials(displayName);
            FontMetrics metrics = g2.getFontMetrics();
            g2.drawString(initials, centerX - metrics.stringWidth(initials) / 2, y + size - 10);
        }

        private static String initials(String value) {
            if (value == null || value.isBlank()) {
                return "";
            }
            String[] parts = value.trim().split("\\s+");
            String first = parts[0].substring(0, 1);
            String last = parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "";
            return (first + last).toUpperCase(Locale.ROOT);
        }
    }

    private static class CircleIconButton extends JButton {
        private CircleIconButton(Icon icon) {
            super(icon);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(DARK_BUTTON);
            g2.fillOval(0, 0, getWidth(), getHeight());
            g2.dispose();
            super.paintComponent(graphics);
        }
    }

    private enum Symbol {
        EDIT,
        LOCK,
        SHIELD_CHECK,
        CAMERA
    }

    private static class LineIcon implements Icon {
        private final Symbol symbol;
        private final int size;
        private final Color color;

        private LineIcon(Symbol symbol, int size, Color color) {
            this.symbol = symbol;
            this.size = size;
            this.color = color;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(Math.max(1.8f, size / 9f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            switch (symbol) {
                case EDIT -> paintEdit(g2, x, y);
                case LOCK -> paintLock(g2, x, y);
                case SHIELD_CHECK -> paintShieldCheck(g2, x, y);
                case CAMERA -> paintCamera(g2, x, y);
            }
            g2.dispose();
        }

        private void paintEdit(Graphics2D g2, int x, int y) {
            g2.drawLine(x + size / 4, y + size * 3 / 4, x + size * 3 / 4, y + size / 4);
            g2.drawLine(x + size / 5, y + size * 4 / 5, x + size / 3, y + size * 3 / 4);
            g2.drawLine(x + size * 2 / 3, y + size / 5, x + size * 4 / 5, y + size / 3);
        }

        private void paintLock(Graphics2D g2, int x, int y) {
            g2.drawRoundRect(x + size / 5, y + size / 2, size * 3 / 5, size * 2 / 5, 4, 4);
            g2.drawArc(x + size / 3, y + size / 5, size / 3, size / 2, 0, 180);
            g2.fillOval(x + size / 2 - 1, y + size * 2 / 3, 3, 3);
        }

        private void paintShieldCheck(Graphics2D g2, int x, int y) {
            Path2D shield = new Path2D.Double();
            shield.moveTo(x + size / 2.0, y + 1);
            shield.lineTo(x + size - 2, y + size / 4.0);
            shield.lineTo(x + size * 0.82, y + size * 0.76);
            shield.lineTo(x + size / 2.0, y + size - 1);
            shield.lineTo(x + size * 0.18, y + size * 0.76);
            shield.lineTo(x + 2, y + size / 4.0);
            shield.closePath();
            g2.fill(shield);
            g2.setColor(Color.WHITE);
            g2.drawLine(x + size / 3, y + size / 2, x + size * 5 / 12, y + size * 2 / 3);
            g2.drawLine(x + size * 5 / 12, y + size * 2 / 3, x + size * 2 / 3, y + size / 3);
        }

        private void paintCamera(Graphics2D g2, int x, int y) {
            g2.drawRoundRect(x + size / 6, y + size / 3, size * 2 / 3, size / 2, 3, 3);
            g2.drawLine(x + size / 3, y + size / 3, x + size * 2 / 3, y + size / 3);
            g2.drawOval(x + size / 2 - size / 8, y + size / 2 - size / 8, size / 4, size / 4);
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }
}
