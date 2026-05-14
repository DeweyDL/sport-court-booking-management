package com.sportcourt.modules.account.view;

import com.sportcourt.common.style.AppFonts;
import com.sportcourt.modules.account.dto.AccountUpsertRequest;
import com.sportcourt.modules.account.dto.RoleGroupOption;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;

final class AccountCreatePanel {
    private static final int INPUT_CORNER_RADIUS = 25;
    private static final Color DIALOG_BG = new Color(248, 249, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BRAND_GREEN = new Color(16, 110, 0);
    private static final Color TEXT_DARK = new Color(30, 41, 59);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);
    private static final Color BUTTON_MUTED = new Color(226, 232, 240);

    private AccountCreatePanel() {
    }

    static AccountUpsertRequest show(Component parent, List<RoleGroupOption> roleGroups, String generatedAccountId) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, "Thêm tài khoản", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBackground(DIALOG_BG);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        dialog.setContentPane(root);

        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setOpaque(false);
        JLabel title = new JLabel("Thêm tài khoản mới");
        title.setFont(AppFonts.lexendBold(24f));
        title.setForeground(TEXT_DARK);
        JLabel subtitle = new JLabel("Điền thông tin account và role group.");
        subtitle.setFont(AppFonts.lexendRegular(13f));
        subtitle.setForeground(TEXT_MUTED);
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        JTextField accountIdField = readonly(generatedAccountId);
        JTextField displayNameField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField usernameField = readonly("");
        JPasswordField passwordField = new JPasswordField();

        phoneField.getDocument().addDocumentListener(new DocumentListener() {
            private void sync() {
                usernameField.setText(phoneField.getText());
            }

            public void insertUpdate(DocumentEvent e) {
                sync();
            }

            public void removeUpdate(DocumentEvent e) {
                sync();
            }

            public void changedUpdate(DocumentEvent e) {
                sync();
            }
        });

        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE"});
        JComboBox<RoleGroupOption> roleGroupCombo = new JComboBox<>(roleGroups.toArray(new RoleGroupOption[0]));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(CARD_BG);
        form.setBorder(new EmptyBorder(18, 18, 18, 18));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 0, 6, 0);

        addField(form, g, 0, "Account ID", accountIdField);
        addField(form, g, 1, "Họ tên", displayNameField);
        addField(form, g, 2, "SĐT", phoneField);
        addField(form, g, 3, "Username", usernameField);
        addField(form, g, 4, "Email", emailField);
        addField(form, g, 5, "Password", passwordField);
        addField(form, g, 6, "Status", statusCombo);
        addField(form, g, 7, "Role group", roleGroupCombo);
        root.add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(1, 2, 10, 0));
        actions.setOpaque(false);
        JButton btnCancel = button("Hủy", BUTTON_MUTED, TEXT_DARK);
        JButton btnConfirm = button("Thêm tài khoản", BRAND_GREEN, Color.WHITE);
        actions.add(btnCancel);
        actions.add(btnConfirm);
        root.add(actions, BorderLayout.SOUTH);

        final AccountUpsertRequest[] result = new AccountUpsertRequest[1];
        btnCancel.addActionListener(e -> dialog.dispose());
        btnConfirm.addActionListener(e -> {
            if (displayNameField.getText().isBlank()
                    || phoneField.getText().isBlank()
                    || new String(passwordField.getPassword()).isBlank()) {
                JOptionPane.showMessageDialog(dialog,
                        "Các trường bắt buộc không được để trống.",
                        "Thông báo",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            RoleGroupOption roleGroup = (RoleGroupOption) roleGroupCombo.getSelectedItem();
            if (roleGroup == null) {
                JOptionPane.showMessageDialog(dialog,
                        "Vui lòng chọn role group.",
                        "Thông báo",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            AccountUpsertRequest request = new AccountUpsertRequest();
            request.setAccountId(accountIdField.getText().trim());
            request.setDisplayName(displayNameField.getText().trim());
            request.setPhone(phoneField.getText().trim());
            request.setEmail(emailField.getText().trim());
            request.setUsername(usernameField.getText().trim());
            request.setPassword(new String(passwordField.getPassword()).trim());
            request.setStatus(String.valueOf(statusCombo.getSelectedItem()));
            request.setRoleGroupId(roleGroup.getGroupId());
            result[0] = request;
            dialog.dispose();
        });

        dialog.pack();
        dialog.setSize(Math.max(dialog.getWidth(), 560), dialog.getHeight());
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        return result[0];
    }

    private static JTextField readonly(String value) {
        JTextField field = new JTextField(value == null ? "" : value) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, INPUT_CORNER_RADIUS, INPUT_CORNER_RADIUS);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        field.setEditable(false);
        field.setFocusable(false);
        field.setOpaque(false);
        field.setBackground(new Color(241, 245, 249));
        styleTextField(field);
        return field;
    }

    private static void addField(JPanel panel, GridBagConstraints g, int row, String label, JComponent field) {
        g.gridy = row * 2;
        JLabel lb = new JLabel(label);
        lb.setFont(AppFonts.lexendBold(12f));
        lb.setForeground(TEXT_DARK);
        panel.add(lb, g);

        g.gridy = row * 2 + 1;
        if (field instanceof JTextField textField) {
            styleTextField(textField);
            if (textField.isEditable()) {
                textField.setBackground(Color.WHITE);
            }
        } else {
            field.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedLineBorder(new Color(203, 213, 225), INPUT_CORNER_RADIUS),
                    BorderFactory.createEmptyBorder(6, 8, 6, 8)
            ));
            field.setBackground(Color.WHITE);
        }
        panel.add(field, g);
    }

    private static void styleTextField(JTextField textField) {
        textField.setFont(textField.isEditable() ? AppFonts.lexendRegular(14f) : AppFonts.lexendBold(14f));
        textField.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(new Color(203, 213, 225), INPUT_CORNER_RADIUS),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
    }

    private static JButton button(String text, Color background, Color foreground) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(background);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btn.setFont(AppFonts.lexendBold(13f));
        btn.setForeground(foreground);
        btn.setBorder(new EmptyBorder(10, 18, 10, 18));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
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
