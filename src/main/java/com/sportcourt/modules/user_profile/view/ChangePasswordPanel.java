package com.sportcourt.modules.user_profile.view;

import com.formdev.flatlaf.FlatLightLaf;
import com.sportcourt.common.style.AppFonts;
import com.sportcourt.common.style.CrudViewStyle;
import com.sportcourt.common.style.UIScale;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Locale;

public class ChangePasswordPanel extends JPanel implements Scrollable {
    private static final Color PAGE_BACKGROUND = new Color(249, 249, 252);
    private static final Color CARD_BACKGROUND = Color.WHITE;
    private static final Color TEXT_DARK = new Color(26, 28, 30);
    private static final Color TEXT_MUTED = new Color(60, 75, 53);
    private static final Color FIELD_BORDER = new Color(203, 213, 225);
    private static final Color GREEN = new Color(16, 110, 0);
    private static final Color LIME = new Color(163, 230, 53);
    private static final Color CANCEL_BACKGROUND = new Color(228, 228, 231);
    private static final Color SELECTION = new Color(197, 246, 181);

    private final JPasswordField currentPasswordField = createPasswordField();
    private final JPasswordField newPasswordField = createPasswordField();
    private final JPasswordField confirmNewPasswordField = createPasswordField();
    private final JButton saveButton = createPillButton("Lưu thay đổi", GREEN, Color.WHITE);
    private final JButton cancelButton = createPillButton("Hủy thay đổi", CANCEL_BACKGROUND, new Color(24, 24, 27));

    public ChangePasswordPanel() {
        AppFonts.register();
        setLayout(new BorderLayout());
        setBackground(PAGE_BACKGROUND);
        setBorder(new EmptyBorder(UIScale.scale(28), UIScale.scale(28), UIScale.scale(28), UIScale.scale(28)));

        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        add(content, BorderLayout.NORTH);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        content.add(createPasswordCard(), gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(UIScale.scale(24), UIScale.scale(32), 0, UIScale.scale(32));
        content.add(createActionRow(), gbc);

        CrudViewStyle.installResponsiveTypography(this);
    }

    public ChangePasswordInput getPasswordInput() {
        return new ChangePasswordInput(
                passwordText(currentPasswordField),
                passwordText(newPasswordField),
                passwordText(confirmNewPasswordField)
        );
    }

    public void clearForm() {
        currentPasswordField.setText("");
        newPasswordField.setText("");
        confirmNewPasswordField.setText("");
    }

    public void setSaveAction(ActionListener listener) {
        replaceAction(saveButton, listener);
    }

    public void setCancelAction(ActionListener listener) {
        replaceAction(cancelButton, listener);
    }

    private JPanel createPasswordCard() {
        ProfileCard card = new ProfileCard();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(UIScale.scale(28), UIScale.scale(32), UIScale.scale(24), UIScale.scale(32)));

        JPanel heading = new JPanel(new FlowLayout(FlowLayout.LEFT, UIScale.scale(10), 0));
        heading.setOpaque(false);
        heading.add(new AccentBar());

        JLabel title = new JLabel("Đổi mật khẩu");
        title.setFont(AppFonts.lexendBold(22f));
        title.setForeground(TEXT_DARK);
        heading.add(title);
        card.add(heading, BorderLayout.NORTH);

        JPanel body = createFieldsPanel();
        body.setBorder(new EmptyBorder(UIScale.scale(24), 0, 0, 0));
        card.add(body, BorderLayout.CENTER);
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
        fields.add(createField("Mật khẩu hiện tại", currentPasswordField), gbc);

        gbc.gridy = 1;
        fields.add(createField("Mật khẩu mới", newPasswordField), gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        fields.add(createField("Xác nhận mật khẩu mới", confirmNewPasswordField), gbc);

        return fields;
    }

    private JPanel createField(String labelText, JComponent input) {
        JPanel field = new JPanel();
        field.setOpaque(false);
        field.setLayout(new BoxLayout(field, BoxLayout.Y_AXIS));

        JLabel label = new JLabel(labelText.toUpperCase(Locale.forLanguageTag("vi-VN")));
        label.setFont(AppFonts.lexendBold(11f));
        label.setForeground(TEXT_MUTED);
        label.setBorder(new EmptyBorder(0, UIScale.scale(10), UIScale.scale(4), UIScale.scale(10)));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        RoundedInputPanel inputWrapper = new RoundedInputPanel();
        inputWrapper.setLayout(new BorderLayout());
        inputWrapper.setBorder(new EmptyBorder(UIScale.scale(5), UIScale.scale(12), UIScale.scale(5), UIScale.scale(12)));
        inputWrapper.setPreferredSize(new Dimension(UIScale.scale(260), UIScale.scale(40)));
        inputWrapper.setMinimumSize(new Dimension(UIScale.scale(220), UIScale.scale(40)));
        inputWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIScale.scale(40)));
        inputWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputWrapper.add(input, BorderLayout.CENTER);

        field.add(label);
        field.add(inputWrapper);
        return field;
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

    private static JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(AppFonts.lexendRegular(14f));
        field.setForeground(TEXT_DARK);
        field.setCaretColor(GREEN);
        field.setOpaque(false);
        field.setBorder(BorderFactory.createEmptyBorder());
        field.setSelectionColor(SELECTION);
        return field;
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

    private static String passwordText(JPasswordField field) {
        return new String(field.getPassword()).trim();
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
            JFrame frame = new JFrame("Đổi mật khẩu");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            JScrollPane scrollPane = new JScrollPane(new ChangePasswordPanel());
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.getViewport().setBackground(PAGE_BACKGROUND);
            frame.setContentPane(scrollPane);
            frame.setSize(1040, 520);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    public record ChangePasswordInput(
            String currentPassword,
            String newPassword,
            String confirmNewPassword
    ) {
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
        private RoundedInputPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int arc = UIScale.scale(15);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, arc, arc);
            g2.setColor(FIELD_BORDER);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arc, arc);
            g2.dispose();
            super.paintComponent(graphics);
        }
    }
}
