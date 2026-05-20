package com.sportcourt.modules.user_profile.view;

import com.formdev.flatlaf.FlatLightLaf;
import com.sportcourt.common.style.AppFonts;
import com.sportcourt.common.style.CrudViewStyle;
import com.sportcourt.common.style.UIScale;
import com.sportcourt.modules.user_profile.controller.UserProfileController;
import com.sportcourt.modules.user_profile.dto.ChangePasswordRequest;
import com.sportcourt.modules.user_profile.dto.UpdateUserProfileRequest;
import com.sportcourt.modules.user_profile.dto.UserProfileDto;
import com.sportcourt.modules.user_profile.dto.UserProfileResult;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class UserProfilePanel extends JPanel implements Scrollable {
    private static final String PROFILE_CARD = "PROFILE";
    private static final String EDIT_CARD = "EDIT";
    private static final String CHANGE_PASSWORD_CARD = "CHANGE_PASSWORD";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Color PAGE_BACKGROUND = new Color(249, 249, 252);
    private static final Color CARD_BACKGROUND = Color.WHITE;
    private static final Color FIELD_BACKGROUND = new Color(248, 250, 252);
    private static final Color FIELD_BORDER = new Color(203, 213, 225);
    private static final Color TEXT_DARK = new Color(26, 28, 30);
    private static final Color TEXT_MUTED = new Color(60, 75, 53);
    private static final Color LIME = new Color(163, 230, 53);
    private static final Color LIME_DARK = new Color(16, 110, 0);
    private static final Color ACTION_BUTTON_BG = new Color(197, 246, 181);
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
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);
    private final UserProfileController controller = new UserProfileController();
    private final UserProfileEditPanel editPanel = new UserProfileEditPanel();
    private final ChangePasswordPanel changePasswordPanel = new ChangePasswordPanel();

    public UserProfilePanel() {
        AppFonts.register();
        setLayout(new BorderLayout());
        CrudViewStyle.applyPageDefaults(this);

        editProfileButton = createActionButton("Chỉnh sửa hồ sơ", loadIcon("/icon/pencil.png", UIScale.scale(16)));
        changePasswordButton = createActionButton("Đổi mật khẩu", loadIcon("/icon/padlock.png", UIScale.scale(16)));

        cards.setOpaque(false);
        editPanel.setBorder(BorderFactory.createEmptyBorder());
        changePasswordPanel.setBorder(BorderFactory.createEmptyBorder());
        cards.add(createPage(), PROFILE_CARD);
        cards.add(editPanel, EDIT_CARD);
        cards.add(changePasswordPanel, CHANGE_PASSWORD_CARD);
        add(cards, BorderLayout.CENTER);

        installActions();
        bindProfile(UserProfileData.sample());
        loadCurrentProfile();
        CrudViewStyle.installResponsiveTypography(this);
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

    private void installActions() {
        setEditProfileAction(event -> showEditPanel());
        setChangePasswordAction(event -> showChangePasswordPanel());
        editPanel.setCancelAction(event -> showProfilePanel());
        editPanel.setSaveAction(event -> saveProfile());
        changePasswordPanel.setCancelAction(event -> showProfilePanel());
        changePasswordPanel.setSaveAction(event -> savePassword());
    }

    private void loadCurrentProfile() {
        UserProfileResult<UserProfileDto> result = controller.getCurrentProfile();
        if (result.success()) {
            applyProfile(result.data());
        } else {
            showProfileMessage(result);
        }
    }

    private void showProfilePanel() {
        cardLayout.show(cards, PROFILE_CARD);
    }

    private void showEditPanel() {
        UserProfileResult<UserProfileDto> result = controller.getCurrentProfile();
        if (result.success()) {
            editPanel.bindProfile(toEditData(result.data()));
            cardLayout.show(cards, EDIT_CARD);
        } else {
            showProfileMessage(result);
        }
    }

    private void showChangePasswordPanel() {
        cardLayout.show(cards, CHANGE_PASSWORD_CARD);
    }

    private void saveProfile() {
        UserProfileResult<UserProfileDto> result = controller.updateCurrentProfile(
                toUpdateRequest(editPanel.getProfileInput())
        );
        showProfileMessage(result);
        if (result.success()) {
            applyProfile(result.data());
            cardLayout.show(cards, PROFILE_CARD);
        }
    }

    private void savePassword() {
        ChangePasswordPanel.ChangePasswordInput input = changePasswordPanel.getPasswordInput();
        UserProfileResult<Void> result = controller.changePassword(new ChangePasswordRequest(
                input.currentPassword(),
                input.newPassword(),
                input.confirmNewPassword()
        ));
        showProfileMessage(result);
        if (result.success()) {
            changePasswordPanel.clearForm();
            cardLayout.show(cards, PROFILE_CARD);
        }
    }

    private void applyProfile(UserProfileDto profile) {
        bindProfile(toPanelData(profile));
        editPanel.bindProfile(toEditData(profile));
    }

    private UserProfileData toPanelData(UserProfileDto profile) {
        return new UserProfileData(
                emptyIfNull(profile.fullName()),
                emptyIfNull(profile.roleName()),
                emptyIfNull(profile.phoneNumber()),
                emptyIfNull(profile.email()),
                formatDate(profile.birthDate()),
                emptyIfNull(profile.address()),
                emptyIfNull(profile.customerRank())
        );
    }

    private UserProfileEditPanel.UserProfileEditData toEditData(UserProfileDto profile) {
        return new UserProfileEditPanel.UserProfileEditData(
                emptyIfNull(profile.fullName()),
                emptyIfNull(profile.phoneNumber()),
                emptyIfNull(profile.email()),
                formatDate(profile.birthDate()),
                emptyIfNull(profile.address())
        );
    }

    private UpdateUserProfileRequest toUpdateRequest(UserProfileEditPanel.UserProfileEditData input) {
        return new UpdateUserProfileRequest(
                input.fullName(),
                input.phoneNumber(),
                input.email(),
                parseDate(input.birthDate()),
                input.address()
        );
    }

    private static String formatDate(LocalDate value) {
        return value == null ? "" : DATE_FORMATTER.format(value);
    }

    private static LocalDate parseDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim(), DATE_FORMATTER);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private static String emptyIfNull(String value) {
        return value == null ? "" : value;
    }

    private void showProfileMessage(UserProfileResult<?> result) {
        JOptionPane.showMessageDialog(
                this,
                result.message(),
                "Thông báo",
                result.success() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE
        );
    }

    private JPanel createPage() {
        JPanel page = new JPanel(new GridBagLayout());
        page.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, UIScale.scale(24), 0);
        page.add(createHeroSection(), gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        page.add(createInfoCard(), gbc);

        // Push content to top when viewport is taller than content
        gbc.gridy = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        JPanel filler = new JPanel();
        filler.setOpaque(false);
        page.add(filler, gbc);

        return page;
    }

    private JPanel createHeroSection() {
        JPanel hero = new JPanel(new GridBagLayout());
        hero.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, UIScale.scale(28));
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        hero.add(createAvatarBlock(), gbc);

        JPanel nameBlock = createNameBlock();
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        hero.add(nameBlock, gbc);

        return hero;
    }

    private JComponent createAvatarBlock() {
        int layerSz = UIScale.scale(176);
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(layerSz, layerSz));
        layeredPane.setMinimumSize(new Dimension(layerSz, layerSz));
        layeredPane.setMaximumSize(new Dimension(layerSz, layerSz));

        avatarView.setBounds(UIScale.scale(16), UIScale.scale(6), UIScale.scale(144), UIScale.scale(144));
        layeredPane.add(avatarView, JLayeredPane.DEFAULT_LAYER);

        int camSz = UIScale.scale(30);
        CircleIconButton cameraButton = new CircleIconButton(new LineIcon(Symbol.CAMERA, UIScale.scale(15), LIME));
        cameraButton.setBounds(UIScale.scale(130), UIScale.scale(130), camSz, camSz);
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

        JPanel roleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, UIScale.scale(7), 0));
        roleRow.setOpaque(false);
        roleRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel roleIcon = new JLabel(new LineIcon(Symbol.SHIELD_CHECK, UIScale.scale(18), LIME_DARK));
        roleLabel.setFont(AppFonts.lexendRegular(14f));
        roleLabel.setForeground(TEXT_MUTED);
        roleRow.add(roleIcon);
        roleRow.add(roleLabel);

        nameBlock.add(displayNameLabel);
        nameBlock.add(Box.createVerticalStrut(UIScale.scale(6)));
        nameBlock.add(roleRow);
        return nameBlock;
    }

    private JPanel createInfoCard() {
        InfoCardPanel card = new InfoCardPanel();
        card.setLayout(new BorderLayout(0, UIScale.scale(24)));
        card.setBorder(new EmptyBorder(UIScale.scale(28), UIScale.scale(24), UIScale.scale(38), UIScale.scale(24)));

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setOpaque(false);

        JPanel heading = new JPanel(new FlowLayout(FlowLayout.LEFT, UIScale.scale(10), 0));
        heading.setOpaque(false);
        heading.add(new AccentBar());
        JLabel title = new JLabel("Thông tin cá nhân");
        title.setFont(AppFonts.lexendBold(22f));
        title.setForeground(TEXT_DARK);
        heading.add(title);
        northPanel.add(heading, BorderLayout.WEST);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, UIScale.scale(12), 0));
        buttonRow.setOpaque(false);
        buttonRow.add(editProfileButton);
        buttonRow.add(changePasswordButton);
        northPanel.add(buttonRow, BorderLayout.EAST);

        card.add(northPanel, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(0, 0, UIScale.scale(18), UIScale.scale(22));

        gbc.gridx = 0;
        gbc.gridy = 0;
        fields.add(createInfoField("Họ và tên", fullNameValue, UIScale.scale(38)), gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, UIScale.scale(22), UIScale.scale(18), 0);
        fields.add(createInfoField("Số điện thoại", phoneValue, UIScale.scale(38)), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, UIScale.scale(18), UIScale.scale(22));
        fields.add(createInfoField("Địa chỉ email", emailValue, UIScale.scale(38)), gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, UIScale.scale(22), UIScale.scale(18), 0);
        fields.add(createInfoField("Ngày sinh", birthDateValue, UIScale.scale(42)), gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 0, UIScale.scale(22));
        fields.add(createInfoField("Địa chỉ thường trú", addressValue, UIScale.scale(78)), gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, UIScale.scale(22), 0, 0);
        gbc.anchor = GridBagConstraints.NORTH;
        fields.add(createInfoField("Hạng khách hàng", rankValue, UIScale.scale(38)), gbc);

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
        label.setBorder(new EmptyBorder(0, UIScale.scale(8), UIScale.scale(4), UIScale.scale(8)));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        int arc = UIScale.scale(25);
        RoundedPanel valueBox = new RoundedPanel(arc, FIELD_BACKGROUND);
        valueBox.setLayout(new BorderLayout());
        valueBox.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(FIELD_BORDER, arc),
                BorderFactory.createEmptyBorder(UIScale.scale(10), UIScale.scale(12), UIScale.scale(10), UIScale.scale(12))
        ));
        valueBox.setPreferredSize(new Dimension(10, valueHeight));
        valueBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, valueHeight));
        valueBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (valueComponent instanceof JLabel labelValue) {
            float fontSize = labelValue == fullNameValue || labelValue == emailValue ? 13.5f : 14f;
            labelValue.setFont(AppFonts.lexendRegular(fontSize));
            labelValue.setForeground(TEXT_DARK);
            valueBox.add(labelValue, BorderLayout.CENTER);
        } else if (valueComponent instanceof JTextArea textArea) {
            textArea.setFont(AppFonts.lexendRegular(14f));
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

    private static Icon loadIcon(String path, int size) {
        java.net.URL url = UserProfilePanel.class.getResource(path);
        if (url == null) return null;
        return new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
    }

    private JButton createActionButton(String text, Icon icon) {
        JButton button = new JButton(text, icon) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ACTION_BUTTON_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(graphics);
            }
        };
        button.setBorder(new EmptyBorder(UIScale.scale(10), UIScale.scale(20), UIScale.scale(10), UIScale.scale(20)));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setIconTextGap(UIScale.scale(14));
        button.setForeground(Color.BLACK);
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
        return fullName == null ? "" : fullName;
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
                    "15/05/1990",
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
            setPreferredSize(new Dimension(UIScale.scale(7), UIScale.scale(28)));
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

    private static final class RoundedLineBorder extends AbstractBorder {
        private final Color color;
        private final int arc;

        private RoundedLineBorder(Color color, int arc) {
            this.color = color;
            this.arc = arc;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x + 1, y + 1, width - 3, height - 3, arc, arc);
            g2.dispose();
        }
    }
}
