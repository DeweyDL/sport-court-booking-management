package com.sportcourt.modules.user_profile.view;

import com.formdev.flatlaf.FlatLightLaf;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.components.DatePicker;
import com.sportcourt.common.style.AppFonts;
import com.sportcourt.common.style.CrudViewStyle;
import com.sportcourt.common.style.UIScale;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class UserProfileEditPanel extends JPanel implements Scrollable {
    private static final Color PAGE_BACKGROUND = new Color(249, 249, 252);
    private static final Color CARD_BACKGROUND = Color.WHITE;
    private static final Color TEXT_DARK = new Color(26, 28, 30);
    private static final Color TEXT_MUTED = new Color(60, 75, 53);
    private static final Color FIELD_BORDER = new Color(203, 213, 225);
    private static final Color GREEN = new Color(16, 110, 0);
    private static final Color LIME = new Color(163, 230, 53);
    private static final Color DARK_BUTTON = new Color(47, 49, 51);
    private static final Color CANCEL_BACKGROUND = new Color(228, 228, 231);
    private static final Color READONLY_BACKGROUND = new Color(241, 245, 249);
    private static final Color READONLY_BORDER = new Color(203, 213, 225);
    private static final Color SELECTION = new Color(197, 246, 181);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String READONLY_FIELD_PROPERTY = "userProfile.readonlyField";

    private final JTextField fullNameField = createTextField();
    private final JTextField emailField = createTextField();
    private final JTextField phoneField = createTextField();
    private final DatePicker birthDatePicker = createDatePicker();
    private final JTextArea addressArea = createTextArea();
    private final AvatarView avatarView = new AvatarView();
    private final JButton avatarButton = new CircleIconButton(new CameraIcon(UIScale.scale(12), LIME));
    private final JButton saveButton = createPillButton("Lưu thay đổi", GREEN, Color.WHITE);
    private final JButton cancelButton = createPillButton("Hủy thay đổi", CANCEL_BACKGROUND, new Color(24, 24, 27));

    private UserProfileEditData originalData;

    public UserProfileEditPanel() {
        AppFonts.register();
        setLayout(new BorderLayout());
        setBackground(PAGE_BACKGROUND);
        setBorder(new EmptyBorder(UIScale.scale(28), UIScale.scale(28), UIScale.scale(28), UIScale.scale(28)));
        applyReadonlyStyle(emailField);
        applyReadonlyStyle(phoneField);

        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        add(content, BorderLayout.NORTH);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        content.add(createProfileCard(), gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(UIScale.scale(24), UIScale.scale(32), 0, UIScale.scale(32));
        content.add(createActionRow(), gbc);

        bindProfile(UserProfileEditData.sample());
        CrudViewStyle.installResponsiveTypography(this);
    }

    public void bindProfile(UserProfileEditData profile) {
        UserProfileEditData resolved = profile == null ? UserProfileEditData.empty() : profile;
        originalData = resolved;
        fullNameField.setText(resolved.fullName());
        emailField.setText(resolved.email());
        phoneField.setText(resolved.phoneNumber());
        setBirthDateText(resolved.birthDate());
        addressArea.setText(resolved.address());
        avatarView.setDisplayName(resolved.fullName());
    }

    public UserProfileEditData getProfileInput() {
        return new UserProfileEditData(
                fullNameField.getText().trim(),
                phoneField.getText().trim(),
                emailField.getText().trim(),
                getBirthDateText(),
                addressArea.getText().trim()
        );
    }

    public void resetForm() {
        bindProfile(originalData);
    }

    public void setSaveAction(ActionListener listener) {
        replaceAction(saveButton, listener);
    }

    public void setCancelAction(ActionListener listener) {
        replaceAction(cancelButton, listener);
    }

    public void setAvatarAction(ActionListener listener) {
        replaceAction(avatarButton, listener);
    }

    private JPanel createProfileCard() {
        ProfileCard card = new ProfileCard();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(UIScale.scale(28), UIScale.scale(32), UIScale.scale(24), UIScale.scale(32)));

        JPanel heading = new JPanel(new FlowLayout(FlowLayout.LEFT, UIScale.scale(10), 0));
        heading.setOpaque(false);
        heading.add(new AccentBar());

        JLabel title = new JLabel("Cập nhật thông tin cá nhân");
        title.setFont(AppFonts.lexendBold(22f));
        title.setForeground(TEXT_DARK);
        heading.add(title);
        card.add(heading, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(UIScale.scale(24), 0, 0, 0));
        card.add(body, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        body.add(createFieldsPanel(), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, UIScale.scale(36), 0, 0);
        body.add(createAvatarBlock(), gbc);

        return card;
    }

    private JPanel createFieldsPanel() {
        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(0, 0, UIScale.scale(16), 0);

        gbc.gridy = 0;
        fields.add(createField("Họ và tên", fullNameField, UIScale.scale(40)), gbc);

        gbc.gridy = 1;
        fields.add(createField("Địa chỉ email", emailField, UIScale.scale(40)), gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, UIScale.scale(18), 0);
        fields.add(createTwoColumnRow(
                createField("Số điện thoại", phoneField, UIScale.scale(40)),
                createField("Ngày sinh", birthDatePicker, UIScale.scale(40))
        ), gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 0, 0);
        fields.add(createField("Địa chỉ thường trú", addressArea, UIScale.scale(70)), gbc);

        return fields;
    }

    private JPanel createTwoColumnRow(JComponent left, JComponent right) {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 0, UIScale.scale(18));
        row.add(left, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, UIScale.scale(18), 0, 0);
        row.add(right, gbc);

        return row;
    }

    private JPanel createField(String labelText, JComponent input, int inputHeight) {
        JPanel field = new JPanel();
        field.setOpaque(false);
        field.setLayout(new BoxLayout(field, BoxLayout.Y_AXIS));

        JLabel label = new JLabel(labelText.toUpperCase(Locale.forLanguageTag("vi-VN")));
        label.setFont(AppFonts.lexendBold(11f));
        label.setForeground(TEXT_MUTED);
        label.setBorder(new EmptyBorder(0, UIScale.scale(10), UIScale.scale(4), UIScale.scale(10)));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        boolean readonly = Boolean.TRUE.equals(input.getClientProperty(READONLY_FIELD_PROPERTY));
        RoundedInputPanel inputWrapper = new RoundedInputPanel(readonly);
        inputWrapper.setLayout(new BorderLayout());
        inputWrapper.setBorder(new EmptyBorder(UIScale.scale(5), UIScale.scale(12), UIScale.scale(5), UIScale.scale(12)));
        inputWrapper.setPreferredSize(new Dimension(UIScale.scale(260), inputHeight));
        inputWrapper.setMinimumSize(new Dimension(UIScale.scale(220), inputHeight));
        inputWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, inputHeight));
        inputWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputWrapper.add(input, BorderLayout.CENTER);

        field.add(label);
        field.add(inputWrapper);
        return field;
    }

    private JComponent createAvatarBlock() {
        int width = UIScale.scale(180);
        int height = UIScale.scale(160);
        int avatarSize = UIScale.scale(122);
        int buttonSize = UIScale.scale(24);

        JLayeredPane layeredPane = new JLayeredPane();
        Dimension size = new Dimension(width, height);
        layeredPane.setPreferredSize(size);
        layeredPane.setMinimumSize(size);
        layeredPane.setMaximumSize(size);

        avatarView.setBounds((width - avatarSize) / 2, UIScale.scale(8), avatarSize, avatarSize);
        layeredPane.add(avatarView, JLayeredPane.DEFAULT_LAYER);

        avatarButton.setBounds(
                (width + avatarSize) / 2 - buttonSize - UIScale.scale(4),
                UIScale.scale(104),
                buttonSize,
                buttonSize
        );
        avatarButton.setToolTipText("Đổi ảnh đại diện");
        layeredPane.add(avatarButton, JLayeredPane.PALETTE_LAYER);

        return layeredPane;
    }

    private JPanel createActionRow() {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 0, UIScale.scale(24));
        row.add(saveButton, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, UIScale.scale(24), 0, 0);
        row.add(cancelButton, gbc);

        return row;
    }

    private static JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(AppFonts.lexendRegular(14f));
        field.setForeground(TEXT_DARK);
        field.setCaretColor(GREEN);
        field.setOpaque(false);
        field.setBorder(BorderFactory.createEmptyBorder());
        field.setSelectionColor(SELECTION);
        return field;
    }

    private static void applyReadonlyStyle(JTextField field) {
        field.setEditable(false);
        field.setFocusable(false);
        field.setForeground(TEXT_DARK);
        field.putClientProperty(READONLY_FIELD_PROPERTY, true);
        field.setCursor(Cursor.getDefaultCursor());
    }

    private static JTextArea createTextArea() {
        JTextArea area = new JTextArea();
        area.setFont(AppFonts.lexendRegular(14f));
        area.setForeground(TEXT_DARK);
        area.setCaretColor(GREEN);
        area.setOpaque(false);
        area.setBorder(BorderFactory.createEmptyBorder());
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setSelectionColor(SELECTION);
        return area;
    }

    private static DatePicker createDatePicker() {
        DatePickerSettings dateSettings = new DatePickerSettings(Locale.forLanguageTag("vi-VN"));
        dateSettings.setFormatForDatesCommonEra(DATE_FORMATTER);
        dateSettings.setAllowKeyboardEditing(false);
        dateSettings.setAllowEmptyDates(false);
        dateSettings.setFontValidDate(AppFonts.lexendRegular(14f));
        dateSettings.setFontInvalidDate(AppFonts.lexendRegular(14f));
        dateSettings.setFontVetoedDate(AppFonts.lexendRegular(14f));
        dateSettings.setSizeTextFieldMinimumWidth(UIScale.scale(118));

        DatePicker picker = new DatePicker(dateSettings);
        picker.setOpaque(false);
        picker.getComponentDateTextField().setFont(AppFonts.lexendRegular(14f));
        picker.getComponentDateTextField().setBorder(BorderFactory.createEmptyBorder());
        picker.getComponentDateTextField().setOpaque(false);
        return picker;
    }

    private void setBirthDateText(String value) {
        if (value == null || value.isBlank()) {
            birthDatePicker.setDate(LocalDate.of(1990, 5, 15));
            return;
        }

        LocalDate parsed = parseDate(value.trim());
        if (parsed != null) {
            birthDatePicker.setDate(parsed);
        } else {
            birthDatePicker.setText(value.trim());
        }
    }

    private String getBirthDateText() {
        LocalDate value = birthDatePicker.getDate();
        return value == null ? birthDatePicker.getText().trim() : DATE_FORMATTER.format(value);
    }

    private static LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value, DATE_FORMATTER);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private static JButton createPillButton(String text, Color background, Color foreground) {
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
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setForeground(foreground);
        button.setFont(AppFonts.lexendBold(16f));
        button.setBorder(new EmptyBorder(UIScale.scale(10), UIScale.scale(24), UIScale.scale(10), UIScale.scale(24)));
        button.setPreferredSize(new Dimension(UIScale.scale(260), UIScale.scale(48)));
        button.setMinimumSize(new Dimension(UIScale.scale(210), UIScale.scale(48)));
        button.setHorizontalAlignment(SwingConstants.CENTER);
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

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return UIScale.scale(16);
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return UIScale.scale(96);
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
        UIScale.init();
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Cập nhật thông tin cá nhân");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            JScrollPane scrollPane = new JScrollPane(new UserProfileEditPanel());
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.getViewport().setBackground(PAGE_BACKGROUND);
            frame.setContentPane(scrollPane);
            frame.setSize(1040, 720);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    public record UserProfileEditData(
            String fullName,
            String phoneNumber,
            String email,
            String birthDate,
            String address
    ) {
        public static UserProfileEditData sample() {
            return new UserProfileEditData(
                    "Marcus Alexander Thorne",
                    "0123456789",
                    "MarcusAlexanderThorne@gmail.com",
                    "15/05/1990",
                    "Tầng 42, Tòa nhà Landmark 81, 720A Điện Biên Phủ, Phường 22, Bình Thạnh, TP. Hồ Chí Minh"
            );
        }

        public static UserProfileEditData empty() {
            return new UserProfileEditData("", "", "", "", "");
        }
    }

    private static final class ProfileCard extends JPanel {
        private ProfileCard() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int shadowOffset = UIScale.scale(10);
            int radius = UIScale.scale(28);
            g2.setColor(new Color(26, 28, 30, 12));
            g2.fillRoundRect(0, shadowOffset, getWidth(), getHeight() - shadowOffset, radius, radius);

            g2.setColor(CARD_BACKGROUND);
            g2.fillRoundRect(0, 0, getWidth(), getHeight() - UIScale.scale(2), radius, radius);

            g2.setColor(new Color(57, 255, 20, 13));
            int oval = UIScale.scale(174);
            g2.fillOval(getWidth() - UIScale.scale(104), -UIScale.scale(88), oval, oval);
            g2.dispose();
            super.paintComponent(graphics);
        }
    }

    private static final class AccentBar extends JComponent {
        private AccentBar() {
            setPreferredSize(new Dimension(UIScale.scale(6), UIScale.scale(28)));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(GREEN);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getWidth(), getWidth());
            g2.dispose();
        }
    }

    private static final class RoundedInputPanel extends JPanel {
        private final boolean readonly;

        private RoundedInputPanel(boolean readonly) {
            this.readonly = readonly;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int arc = UIScale.scale(15);
            g2.setColor(readonly ? READONLY_BACKGROUND : Color.WHITE);
            g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, arc, arc);
            g2.setColor(readonly ? READONLY_BORDER : FIELD_BORDER);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arc, arc);
            g2.dispose();
            super.paintComponent(graphics);
        }
    }

    private static final class AvatarView extends JComponent {
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

            int size = Math.min(getWidth(), getHeight()) - UIScale.scale(12);
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;

            g2.setColor(new Color(57, 255, 20, 55));
            g2.fillOval(x - UIScale.scale(6), y - UIScale.scale(6),
                    size + UIScale.scale(12), size + UIScale.scale(12));
            g2.setColor(Color.WHITE);
            g2.fillOval(x, y, size, size);
            g2.setClip(new Ellipse2D.Float(x, y, size, size));

            g2.setColor(new Color(236, 253, 245));
            g2.fillOval(x, y, size, size);
            drawPortrait(g2, x, y, size);

            g2.setClip(null);
            g2.setStroke(new BasicStroke(UIScale.scale(4)));
            g2.setColor(PAGE_BACKGROUND);
            g2.drawOval(x + UIScale.scale(2), y + UIScale.scale(2),
                    size - UIScale.scale(4), size - UIScale.scale(4));
            g2.dispose();
        }

        private void drawPortrait(Graphics2D g2, int x, int y, int size) {
            int centerX = x + size / 2;
            int hairTop = y + size / 7;

            g2.setColor(new Color(17, 24, 39));
            g2.fillRoundRect(centerX - size / 4, hairTop, size / 2, size * 7 / 10,
                    size / 4, size / 4);

            g2.setColor(new Color(251, 207, 178));
            g2.fillOval(centerX - size / 6, y + size / 4, size / 3, size / 3);

            g2.setColor(new Color(17, 24, 39));
            g2.fillArc(centerX - size / 5, y + size / 5, size * 2 / 5, size / 4, 0, 180);

            g2.setColor(new Color(15, 118, 110));
            Path2D jacket = new Path2D.Double();
            jacket.moveTo(centerX - size * 0.34, y + size * 0.72);
            jacket.lineTo(centerX + size * 0.34, y + size * 0.72);
            jacket.lineTo(centerX + size * 0.48, y + size);
            jacket.lineTo(centerX - size * 0.48, y + size);
            jacket.closePath();
            g2.fill(jacket);

            g2.setColor(Color.WHITE);
            Path2D shirt = new Path2D.Double();
            shirt.moveTo(centerX - size * 0.12, y + size * 0.72);
            shirt.lineTo(centerX + size * 0.12, y + size * 0.72);
            shirt.lineTo(centerX, y + size * 0.98);
            shirt.closePath();
            g2.fill(shirt);

            g2.setColor(new Color(220, 38, 38));
            Path2D tie = new Path2D.Double();
            tie.moveTo(centerX, y + size * 0.79);
            tie.lineTo(centerX + size * 0.06, y + size * 0.99);
            tie.lineTo(centerX - size * 0.06, y + size * 0.99);
            tie.closePath();
            g2.fill(tie);

            g2.setColor(TEXT_DARK);
            g2.setFont(AppFonts.lexendBold(Math.max(9f, size / 12f)));
            String initials = initials(displayName);
            FontMetrics metrics = g2.getFontMetrics();
            g2.drawString(initials, centerX - metrics.stringWidth(initials) / 2, y + size - UIScale.scale(10));
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

    private static final class CircleIconButton extends JButton {
        private CircleIconButton(Icon icon) {
            super(icon);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(0, 0, 0, 0));
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

    private static final class CameraIcon implements Icon {
        private final int size;
        private final Color color;

        private CameraIcon(int size, Color color) {
            this.size = size;
            this.color = color;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(Math.max(1.5f, size / 8f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawRoundRect(x + size / 7, y + size / 3, size * 5 / 7, size / 2, 3, 3);
            g2.drawLine(x + size / 3, y + size / 3, x + size * 2 / 3, y + size / 3);
            g2.drawOval(x + size / 2 - size / 8, y + size / 2 - size / 8, size / 4, size / 4);
            g2.dispose();
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
