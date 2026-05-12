package com.sportcourt.modules.staff.view;

import com.sportcourt.common.style.AppFonts;
import com.sportcourt.modules.staff.dto.StaffResponse;
import com.sportcourt.modules.staff.dto.StaffUpdateRequest;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;

final class EditStaffDialog {
    private static final int INPUT_CORNER_RADIUS = 25;
    private static final Color DIALOG_BG = new Color(248, 249, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BRAND_BLUE = new Color(37, 99, 235);
    private static final Color TEXT_DARK = new Color(30, 41, 59);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);
    private static final Color BUTTON_MUTED = new Color(226, 232, 240);

    private EditStaffDialog() {
    }

    static StaffUpdateRequest show(Component parent, StaffResponse staff) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, "Chỉnh sửa nhân viên", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBackground(DIALOG_BG);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        dialog.setContentPane(root);

        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setOpaque(false);
        JLabel title = new JLabel("Chỉnh sửa nhân viên");
        title.setFont(AppFonts.lexendBold(24f));
        title.setForeground(TEXT_DARK);
        JLabel subtitle = new JLabel("Mã nhân viên: " + staff.getManv());
        subtitle.setFont(AppFonts.lexendRegular(13f));
        subtitle.setForeground(TEXT_MUTED);
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        JTextField txtHoTen = new JTextField(staff.getHoten());
        JTextField txtCCCD = new JTextField(staff.getCccd() == null ? "" : staff.getCccd());
        JComboBox<String> cbChucVu = new JComboBox<>(new String[]{"Nhân viên", "Quản lý"});
        JComboBox<String> cbTrangThai = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE", "ĐÃ NGHỈ"});

        cbChucVu.setSelectedIndex(staff.getIsQl() == 1 ? 1 : 0);
        String currentStatus = staff.getTrangThai();
        if (currentStatus != null) {
            for (int i = 0; i < cbTrangThai.getItemCount(); i++) {
                if (cbTrangThai.getItemAt(i).equalsIgnoreCase(currentStatus)) {
                    cbTrangThai.setSelectedIndex(i);
                    break;
                }
            }
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(CARD_BG);
        form.setBorder(new EmptyBorder(18, 18, 18, 18));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 0, 6, 0);

        addField(form, g, 0, "Họ và tên", txtHoTen);
        addField(form, g, 1, "Căn cước công dân", txtCCCD);
        addField(form, g, 2, "Chức vụ", cbChucVu);
        addField(form, g, 3, "Trạng thái", cbTrangThai);
        root.add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(1, 2, 10, 0));
        actions.setOpaque(false);
        JButton btnCancel = button("Hủy", BUTTON_MUTED, TEXT_DARK);
        JButton btnSave = button("Lưu thay đổi", BRAND_BLUE, Color.WHITE);
        actions.add(btnCancel);
        actions.add(btnSave);
        root.add(actions, BorderLayout.SOUTH);

        final StaffUpdateRequest[] result = new StaffUpdateRequest[1];
        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            String hoten = txtHoTen.getText().trim();
            String cccd = txtCCCD.getText().trim();
            if (hoten.isEmpty() || cccd.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng điền đầy đủ tất cả các trường.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            StaffUpdateRequest req = new StaffUpdateRequest();
            req.setHoten(hoten);
            req.setCccd(cccd);
            req.setIsQl(cbChucVu.getSelectedIndex());
            req.setTrangThai(cbTrangThai.getSelectedItem().toString());
            result[0] = req;
            dialog.dispose();
        });

        dialog.pack();
        dialog.setSize(Math.max(dialog.getWidth(), 560), dialog.getHeight());
        dialog.setLocationRelativeTo(null);
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
        if (field instanceof JTextField textField) {
            styleTextField(textField);
        } else {
            field.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedLineBorder(new Color(203, 213, 225), INPUT_CORNER_RADIUS),
                    BorderFactory.createEmptyBorder(6, 8, 6, 8)
            ));
            field.setBackground(Color.WHITE);
            field.setFont(AppFonts.lexendRegular(14f));
        }
        panel.add(field, g);
    }

    private static void styleTextField(JTextField textField) {
        textField.setFont(AppFonts.lexendRegular(14f));
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
            g2.drawRoundRect(x, y, width - 1, height - 1, arc, arc);
            g2.dispose();
        }
    }
}
