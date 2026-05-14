package com.sportcourt.modules.staff.view;

import com.sportcourt.modules.staff.dto.StaffResponse;
import com.sportcourt.modules.staff.dto.StaffUpdateRequest;
import com.sportcourt.modules.staff.service.StaffService;
import com.sportcourt.modules.staff.service.StaffServiceImpl;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class EditStaffDialog extends JDialog {

    private static final Color DIALOG_BG    = new Color(248, 249, 252);
    private static final Color CARD_BG      = Color.WHITE;
    private static final Color BRAND_COLOR  = new Color(29, 78, 216);
    private static final Color TEXT_DARK    = new Color(30, 41, 59);
    private static final Color TEXT_MUTED   = new Color(100, 116, 139);
    private static final Color BORDER_COLOR = new Color(203, 213, 225);
    private static final Color READONLY_BG  = new Color(241, 245, 249);

    private final StaffService staffService = new StaffServiceImpl();
    private final StaffPanel   parentPanel;

    public EditStaffDialog(JFrame parent, StaffPanel parentPanel, StaffResponse staff, boolean isOwner) {
        super(parent, "Chỉnh sửa nhân viên", ModalityType.APPLICATION_MODAL);
        this.parentPanel = parentPanel;

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBackground(DIALOG_BG);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(root);

        JLabel title = new JLabel("Chỉnh sửa: " + staff.getHoten());
        title.setFont(new Font("Lexend", Font.BOLD, 24));
        title.setForeground(TEXT_DARK);

        JLabel subtitle = new JLabel("Mã nhân viên: " + staff.getManv());
        subtitle.setFont(new Font("Lexend", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_MUTED);
        subtitle.setBorder(new EmptyBorder(4, 0, 0, 0));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(title);
        header.add(subtitle);
        root.add(header, BorderLayout.NORTH);

        JTextField txtHoTen   = new JTextField(staff.getHoten());
        JTextField txtPhone   = new JTextField(staff.getSdt() == null ? "" : staff.getSdt());
        JTextField txtDiaChi  = new JTextField(staff.getDiaChi() == null ? "" : staff.getDiaChi());
        JTextField txtCCCD    = new JTextField(staff.getCccd() == null ? "" : staff.getCccd());
        JTextField txtMaCn    = new JTextField(staff.getMaCn() == null ? "" : staff.getMaCn());
        JComboBox<String> cbChucVu    = new JComboBox<>(new String[]{"Nhân viên", "Quản lý"});
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

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(CARD_BG);
        form.setBorder(new EmptyBorder(18, 18, 18, 18));
        form.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(createReadOnlyField("Mã nhân viên", staff.getManv()));
        form.add(Box.createVerticalStrut(14));
        form.add(createField("Họ và tên", txtHoTen));
        form.add(Box.createVerticalStrut(14));
        form.add(createField("Số điện thoại", txtPhone));
        form.add(Box.createVerticalStrut(14));
        form.add(createField("Địa chỉ", txtDiaChi));
        form.add(Box.createVerticalStrut(14));
        form.add(createField("Căn cước công dân", txtCCCD));
        form.add(Box.createVerticalStrut(14));
        if (isOwner) {
            form.add(createField("Mã chi nhánh", txtMaCn));
            form.add(Box.createVerticalStrut(14));
        }

        JPanel splitPanel = new JPanel(new GridLayout(1, 2, 14, 0));
        splitPanel.setOpaque(false);
        splitPanel.add(createField("Chức vụ", cbChucVu));
        splitPanel.add(createField("Trạng thái", cbTrangThai));
        splitPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(splitPanel);

        root.add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(1, 2, 10, 0));
        actions.setOpaque(false);

        JButton cancelBtn = createPillButton("Hủy", new Color(226, 232, 240), new Color(30, 41, 59));
        JButton saveBtn   = createPillButton("Lưu thay đổi", BRAND_COLOR, Color.WHITE);

        cancelBtn.addActionListener(e -> dispose());
        saveBtn.addActionListener(e -> {
            try {
                String phone = txtPhone.getText().trim();
                if (!phone.isEmpty() && !phone.matches("^0[0-9]{9}$")) {
                    JOptionPane.showMessageDialog(this, "Số điện thoại phải gồm 10 chữ số bắt đầu bằng 0.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                StaffUpdateRequest req = new StaffUpdateRequest();
                req.setHoten(txtHoTen.getText().trim());
                req.setSdt(phone.isEmpty() ? null : phone);
                req.setDiaChi(txtDiaChi.getText().trim());
                req.setCccd(txtCCCD.getText().trim());
                req.setIsQl(cbChucVu.getSelectedIndex());
                req.setTrangThai(cbTrangThai.getSelectedItem().toString());
                req.setMaCn(txtMaCn.getText().trim());

                staffService.updateStaff(staff.getManv(), req);
                JOptionPane.showMessageDialog(this, "Đã cập nhật nhân viên thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                parentPanel.loadData();
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi cập nhật", JOptionPane.ERROR_MESSAGE);
            }
        });

        actions.add(cancelBtn);
        actions.add(saveBtn);
        root.add(actions, BorderLayout.SOUTH);

        pack();
        setSize(Math.max(getWidth(), 560), getHeight());
        setLocationRelativeTo(parent);
    }

    private JPanel createReadOnlyField(String labelText, String value) {
        JTextField field = new JTextField(value == null ? "" : value) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 25, 25);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        field.setEditable(false);
        field.setFocusable(false);
        field.setRequestFocusEnabled(false);
        field.setCursor(Cursor.getDefaultCursor());
        field.setOpaque(false);
        field.setFont(new Font("Lexend", Font.BOLD, 14));
        field.setForeground(new Color(31, 41, 55));
        field.setBackground(READONLY_BG);
        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(BORDER_COLOR, 25),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Lexend", Font.BOLD, 12));
        label.setForeground(TEXT_DARK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(label);
        panel.add(Box.createVerticalStrut(6));
        panel.add(field);
        return panel;
    }

    private JPanel createField(String labelText, JComponent field) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Lexend", Font.BOLD, 12));
        label.setForeground(TEXT_DARK);

        field.setFont(new Font("Lexend", Font.PLAIN, 14));
        field.setForeground(new Color(31, 41, 55));
        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(BORDER_COLOR, 25),
                BorderFactory.createEmptyBorder(field instanceof JTextField ? 10 : 6, 12, field instanceof JTextField ? 10 : 6, 12)
        ));
        field.setBackground(Color.WHITE);

        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        panel.add(label);
        panel.add(Box.createVerticalStrut(6));
        panel.add(field);
        return panel;
    }

    private JButton createPillButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btn.setForeground(fg);
        btn.setFont(new Font("Lexend", Font.BOLD, 13));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 18, 10, 18));
        return btn;
    }

    private static final class RoundedLineBorder extends AbstractBorder {
        private final Color color;
        private final int   arc;

        private RoundedLineBorder(Color color, int arc) {
            this.color = color;
            this.arc   = arc;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x + 1, y + 1, width - 3, height - 3, arc, arc);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) { return new Insets(1, 1, 1, 1); }
    }
}
