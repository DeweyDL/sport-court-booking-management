package com.sportcourt.modules.managecustomer.view;

import com.sportcourt.common.style.AppFonts;
import com.sportcourt.modules.managecustomer.dto.CreateCustomerRequest;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

final class CustomerCreateDialog {
    private static final Color DIALOG_BG = new Color(248, 249, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BRAND_BLUE = new Color(37, 99, 235);
    private static final Color TEXT_DARK = new Color(30, 41, 59);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);
    private static final Color BUTTON_MUTED = new Color(226, 232, 240);

    private CustomerCreateDialog() {
    }

    static CreateCustomerRequest show(Component parent) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, "Thêm khách hàng", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBackground(DIALOG_BG);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        dialog.setContentPane(root);

        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setOpaque(false);
        JLabel title = new JLabel("Thêm khách hàng mới");
        title.setFont(AppFonts.lexendBold(22f));
        title.setForeground(TEXT_DARK);
        JLabel subtitle = new JLabel("Tạo nhanh hồ sơ khách hàng để sử dụng trong hệ thống.");
        subtitle.setFont(AppFonts.lexendRegular(13f));
        subtitle.setForeground(TEXT_MUTED);
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        JTextField txtHoTen = new JTextField();
        JTextField txtSdt = new JTextField();

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(CARD_BG);
        form.setBorder(new EmptyBorder(18, 18, 18, 18));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 0, 6, 0);

        addField(form, g, 0, "Họ tên", txtHoTen);
        addField(form, g, 1, "Số điện thoại", txtSdt);
        root.add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(1, 2, 10, 0));
        actions.setOpaque(false);
        JButton btnCancel = secondaryButton("Hủy");
        JButton btnConfirm = brandButton("Tạo khách hàng");
        actions.add(btnCancel);
        actions.add(btnConfirm);
        root.add(actions, BorderLayout.SOUTH);

        final CreateCustomerRequest[] result = new CreateCustomerRequest[1];

        btnCancel.addActionListener(e -> dialog.dispose());
        btnConfirm.addActionListener(e -> {
            String hoTen = txtHoTen.getText().trim();
            String sdt = txtSdt.getText().trim();
            if (hoTen.isEmpty() || sdt.isEmpty()) {
                JOptionPane.showMessageDialog(
                        dialog,
                        "Họ tên và số điện thoại không được để trống.",
                        "Thông báo",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            result[0] = new CreateCustomerRequest(hoTen, sdt);
            dialog.dispose();
        });

        dialog.pack();
        dialog.setSize(Math.max(dialog.getWidth(), 460), dialog.getHeight());
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return result[0];
    }

    private static void addField(JPanel form, GridBagConstraints g, int row, String label, JComponent field) {
        g.gridy = row * 2;
        JLabel lb = new JLabel(label);
        lb.setFont(AppFonts.lexendBold(12f));
        lb.setForeground(TEXT_DARK);
        form.add(lb, g);

        g.gridy = row * 2 + 1;
        field.setFont(AppFonts.lexendRegular(14f));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        form.add(field, g);
    }

    private static JButton brandButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(AppFonts.lexendBold(13f));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBackground(BRAND_BLUE);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(29, 78, 216), 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        btn.setBorderPainted(true);
        btn.setContentAreaFilled(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private static JButton secondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(AppFonts.lexendBold(13f));
        btn.setForeground(TEXT_DARK);
        btn.setFocusPainted(false);
        btn.setBackground(BUTTON_MUTED);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(148, 163, 184), 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        btn.setBorderPainted(true);
        btn.setContentAreaFilled(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
