package com.sportcourt.modules.staff.view;

import com.sportcourt.modules.staff.dto.StaffCreateRequest;
import com.sportcourt.modules.staff.service.StaffService;
import com.sportcourt.modules.staff.service.StaffServiceImpl;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AddStaffDialog extends JDialog {

    private static final Color DIALOG_BG    = new Color(248, 249, 252);
    private static final Color CARD_BG      = Color.WHITE;
    private static final Color BRAND_COLOR  = new Color(22, 101, 52);
    private static final Color BRAND_BG     = new Color(220, 252, 231);
    private static final Color TEXT_DARK    = new Color(30, 41, 59);
    private static final Color TEXT_MUTED   = new Color(100, 116, 139);
    private static final Color BORDER_COLOR = new Color(203, 213, 225);

    private final StaffService staffService = new StaffServiceImpl();
    private final StaffPanel parentPanel;

    public AddStaffDialog(JFrame parent, StaffPanel parentPanel) {
        super(parent, "Thêm nhân viên", ModalityType.APPLICATION_MODAL);
        this.parentPanel = parentPanel;

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(DIALOG_BG);
        root.setBorder(new EmptyBorder(22, 22, 22, 22));
        setContentPane(root);

        // Header
        JLabel title = new JLabel("Thêm nhân viên mới");
        title.setFont(new Font("Lexend", Font.BOLD, 22));
        title.setForeground(TEXT_DARK);

        JLabel subtitle = new JLabel("Điền thông tin cơ bản để tạo hồ sơ nhân viên.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
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

        // Form Fields (Đã bỏ Chi nhánh và Loại nhân viên)
        JTextField txtMaNV = new JTextField();
        JTextField txtHoTen = new JTextField();
        JTextField txtCCCD = new JTextField();
        JComboBox<String> cbChucVu = new JComboBox<>(new String[]{"Nhân viên", "Quản lý"});
        JComboBox<String> cbTrangThai = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE", "ĐÃ NGHỈ"});

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(CARD_BG);
        form.setBorder(new EmptyBorder(18, 18, 18, 18));
        form.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(createField("Mã nhân viên", txtMaNV));
        form.add(Box.createVerticalStrut(14));
        form.add(createField("Họ và tên", txtHoTen));
        form.add(Box.createVerticalStrut(14));
        form.add(createField("Căn cước công dân", txtCCCD));
        form.add(Box.createVerticalStrut(14));

        // Chia 2 cột cho Chức vụ và Trạng thái
        JPanel splitPanel = new JPanel(new GridLayout(1, 2, 14, 0));
        splitPanel.setOpaque(false);
        splitPanel.add(createField("Chức vụ", cbChucVu));
        splitPanel.add(createField("Trạng thái", cbTrangThai));
        splitPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(splitPanel);

        root.add(form, BorderLayout.CENTER);

        // Actions
        JPanel actions = new JPanel(new GridLayout(1, 2, 12, 0));
        actions.setOpaque(false);

        JButton cancelBtn = createPillButton("Hủy", new Color(229, 231, 235), new Color(31, 41, 55));
        JButton saveBtn   = createPillButton("Lưu nhân viên", BRAND_BG, BRAND_COLOR);

        cancelBtn.addActionListener(e -> dispose());
        saveBtn.addActionListener(e -> {
            try {
                StaffCreateRequest req = new StaffCreateRequest();
                req.setManv(txtMaNV.getText().trim());
                req.setHoten(txtHoTen.getText().trim());
                req.setCccd(txtCCCD.getText().trim());
                req.setIsQl(cbChucVu.getSelectedIndex()); // 0: Nhân viên, 1: Quản lý
                req.setTrangThai(cbTrangThai.getSelectedItem().toString());

                staffService.createStaff(req);
                JOptionPane.showMessageDialog(this, "Đã thêm nhân viên thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                parentPanel.loadData();
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            }
        });

        actions.add(cancelBtn);
        actions.add(saveBtn);
        root.add(actions, BorderLayout.SOUTH);

        pack();
        setSize(Math.max(getWidth(), 450), getHeight());
        setLocationRelativeTo(parent);
    }

    private JPanel createField(String labelText, JComponent field) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(75, 85, 99));

        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(new Color(31, 41, 55));
        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(BORDER_COLOR, 12),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)
        ));
        field.setBackground(new Color(249, 250, 251));

        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

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
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 18, 10, 18));
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
        @Override
        public Insets getBorderInsets(Component c) { return new Insets(1, 1, 1, 1); }
    }
}