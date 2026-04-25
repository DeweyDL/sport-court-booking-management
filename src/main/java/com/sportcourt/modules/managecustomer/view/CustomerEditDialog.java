package com.sportcourt.modules.managecustomer.view;

import com.sportcourt.common.style.AppFonts;
import com.sportcourt.modules.managecustomer.dto.CustomerProfile;
import com.sportcourt.modules.managecustomer.dto.UpdateCustomerRequest;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

final class CustomerEditDialog {
    private static final Color DIALOG_BG = new Color(248, 249, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(30, 41, 59);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);
    private static final Color BRAND_BLUE = new Color(37, 99, 235);
    private static final Color BUTTON_MUTED = new Color(226, 232, 240);

    private CustomerEditDialog() {
    }

    static UpdateCustomerRequest show(Component parent, CustomerProfile profile) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, "Cập nhật khách hàng", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBackground(DIALOG_BG);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        dialog.setContentPane(root);

        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setOpaque(false);
        JLabel title = new JLabel("Cập nhật khách hàng");
        title.setFont(AppFonts.lexendBold(24f));
        title.setForeground(TEXT_DARK);
        JLabel subtitle = new JLabel("Chỉnh sửa các thông tin cơ bản của khách hàng.");
        subtitle.setFont(AppFonts.lexendRegular(13f));
        subtitle.setForeground(TEXT_MUTED);
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        JTextField txtMaKh = readonlyField(profile.maKhachHang());
        JTextField txtHoTen = editableField(profile.hoTen());
        JTextField txtSdt = editableField(profile.sdt());
        JComboBox<String> cbTrangThai = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE"});
        cbTrangThai.setSelectedItem(profile.trangThai());
        cbTrangThai.setFont(AppFonts.lexendRegular(14f));
        cbTrangThai.setEnabled(false);
        cbTrangThai.setFocusable(false);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD_BG);
        card.setBorder(new EmptyBorder(18, 18, 18, 18));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 0, 6, 0);

        addField(card, g, 0, "Mã khách hàng", txtMaKh);
        addField(card, g, 1, "Họ tên", txtHoTen);
        addField(card, g, 2, "Số điện thoại", txtSdt);
        addField(card, g, 3, "Trạng thái", cbTrangThai);
        root.add(card, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(1, 2, 12, 0));
        actions.setOpaque(false);
        JButton btnCancel = button("Hủy", BUTTON_MUTED, TEXT_DARK);
        JButton btnSave = button("Lưu cập nhật", BRAND_BLUE, Color.WHITE);
        actions.add(btnCancel);
        actions.add(btnSave);
        root.add(actions, BorderLayout.SOUTH);

        final UpdateCustomerRequest[] result = new UpdateCustomerRequest[1];
        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            String hoTen = txtHoTen.getText().trim();
            String sdt = txtSdt.getText().trim();
            String trangThai = profile.trangThai();

            if (hoTen.isEmpty() || sdt.isEmpty()) {
                JOptionPane.showMessageDialog(
                        dialog,
                        "Họ tên và số điện thoại không được để trống.",
                        "Thông báo",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            result[0] = new UpdateCustomerRequest(hoTen, sdt, trangThai);
            dialog.dispose();
        });

        dialog.pack();
        dialog.setSize(Math.max(dialog.getWidth(), 520), dialog.getHeight());
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return result[0];
    }

    private static void addField(JPanel panel, GridBagConstraints g, int row, String label, JComponent field) {
        g.gridy = row * 2;
        JLabel lb = new JLabel(label);
        lb.setFont(AppFonts.lexendBold(12f));
        lb.setForeground(TEXT_DARK);
        panel.add(lb, g);

        g.gridy = row * 2 + 1;
        panel.add(field, g);
    }

    private static JTextField readonlyField(String value) {
        JTextField field = baseField(value);
        field.setEditable(false);
        field.setBackground(new Color(241, 245, 249));
        return field;
    }

    private static JTextField editableField(String value) {
        return baseField(value);
    }

    private static JTextField baseField(String value) {
        JTextField field = new JTextField(value);
        field.setFont(AppFonts.lexendRegular(14f));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        return field;
    }

    private static JButton button(String text, Color background, Color foreground) {
        JButton btn = new JButton(text);
        btn.setFont(AppFonts.lexendBold(13f));
        btn.setForeground(foreground);
        btn.setBackground(background);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        Color borderColor = Color.WHITE.equals(foreground) ? new Color(29, 78, 216) : new Color(148, 163, 184);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        btn.setBorderPainted(true);
        btn.setContentAreaFilled(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
