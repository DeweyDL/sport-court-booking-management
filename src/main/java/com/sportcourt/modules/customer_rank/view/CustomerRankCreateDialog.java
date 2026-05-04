package com.sportcourt.modules.customer_rank.view;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;

final class CustomerRankCreateDialog {

    private static final Color DIALOG_BG   = new Color(248, 249, 252);
    private static final Color CARD_BG     = Color.WHITE;
    private static final Color BRAND_COLOR = new Color(22, 101, 52);
    private static final Color BRAND_BG    = new Color(220, 252, 231);
    private static final Color TEXT_DARK   = new Color(30, 41, 59);
    private static final Color TEXT_MUTED  = new Color(100, 116, 139);
    private static final Color BORDER_COLOR = new Color(203, 213, 225);

    private CustomerRankCreateDialog() {
    }

    static void show(Component parent) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, "Thêm hạng khách hàng", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(DIALOG_BG);
        root.setBorder(new EmptyBorder(22, 22, 22, 22));
        dialog.setContentPane(root);

        // Header
        JLabel title = new JLabel("Thêm hạng khách hàng");
        title.setFont(new Font("Lexend", Font.BOLD, 22));
        title.setForeground(TEXT_DARK);
        title.setHorizontalAlignment(SwingConstants.LEFT);

        JLabel subtitle = new JLabel("Nhập mã hạng và thông tin cho hạng khách hàng mới.");
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

        // Form fields
        JTextField txtMaHang    = new JTextField();
        JTextField txtTenHang   = new JTextField();
        JTextField txtChietKhau = new JTextField();
        JTextField txtMucTien   = new JTextField();

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(CARD_BG);
        form.setBorder(new EmptyBorder(18, 18, 18, 18));
        form.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(createField("Mã hạng", txtMaHang));
        form.add(Box.createVerticalStrut(14));
        form.add(createField("Tên hạng", txtTenHang));
        form.add(Box.createVerticalStrut(14));
        form.add(createField("Chiết khấu (%)", txtChietKhau));
        form.add(Box.createVerticalStrut(14));
        form.add(createField("Mức tiền (VNĐ)", txtMucTien)); // Cập nhật tên nhãn
        root.add(form, BorderLayout.CENTER);

        // Actions
        JPanel actions = new JPanel(new GridLayout(1, 2, 12, 0));
        actions.setOpaque(false);

        JButton cancelBtn = createPillButton("Hủy", new Color(229, 231, 235), new Color(31, 41, 55));
        JButton saveBtn   = createPillButton("Thêm hạng", BRAND_BG, BRAND_COLOR);
        cancelBtn.addActionListener(event -> dialog.dispose());
        saveBtn.addActionListener(event -> {
            String maHang       = txtMaHang.getText().trim();
            String tenHang      = txtTenHang.getText().trim();
            String chietKhauStr = txtChietKhau.getText().trim();
            String mucTienStr   = txtMucTien.getText().trim();

            if (maHang.isEmpty() || tenHang.isEmpty() || chietKhauStr.isEmpty() || mucTienStr.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng điền đầy đủ tất cả các trường.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                double chietKhau = Double.parseDouble(chietKhauStr);
                double mucTien   = Double.parseDouble(mucTienStr);

                if (chietKhau < 0 || chietKhau > 100) {
                    JOptionPane.showMessageDialog(dialog, "Chiết khấu phải từ 0 đến 100.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (mucTien < 0) {
                    JOptionPane.showMessageDialog(dialog, "Mức tiền không được âm.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                System.out.println("[CustomerRank Create] Mã: " + maHang
                        + ", Tên: " + tenHang
                        + ", Chiết khấu: " + chietKhau + "%"
                        + ", Mức tiền: " + mucTien);
                JOptionPane.showMessageDialog(dialog, "Đã ghi nhận (mock).", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Chiết khấu và mức tiền phải là số hợp lệ.", "Lỗi định dạng", JOptionPane.WARNING_MESSAGE);
            }
        });
        actions.add(cancelBtn);
        actions.add(saveBtn);
        root.add(actions, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setSize(Math.max(dialog.getWidth(), 480), dialog.getHeight());
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private static JPanel createField(String labelText, JTextField field) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(75, 85, 99));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setForeground(new Color(31, 41, 55));
        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(BORDER_COLOR, 18),
                BorderFactory.createEmptyBorder(9, 14, 9, 14)
        ));
        field.setBackground(new Color(249, 250, 251));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        panel.add(label);
        panel.add(Box.createVerticalStrut(6));
        panel.add(field);
        return panel;
    }

    private static JButton createPillButton(String text, Color bg, Color fg) {
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
            this.arc   = arc;
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
        public Insets getBorderInsets(Component c) {
            return new Insets(1, 1, 1, 1);
        }
    }
}