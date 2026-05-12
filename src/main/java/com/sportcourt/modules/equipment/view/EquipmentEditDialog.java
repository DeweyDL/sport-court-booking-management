package com.sportcourt.modules.equipment.view;

import com.sportcourt.common.style.AppFonts;
import com.sportcourt.modules.equipment.view.EquipmentMockData.EquipmentItem;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;

final class EquipmentEditDialog {
    private static final int INPUT_CORNER_RADIUS = 25;
    private static final Color DIALOG_BG = new Color(248, 249, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BRAND_BLUE = new Color(37, 99, 235);
    private static final Color TEXT_DARK = new Color(30, 41, 59);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);
    private static final Color BUTTON_MUTED = new Color(226, 232, 240);

    private EquipmentEditDialog() {
    }

    static void show(Component parent, EquipmentItem item) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, "Chỉnh sửa dụng cụ", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBackground(DIALOG_BG);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        dialog.setContentPane(root);

        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setOpaque(false);
        JLabel title = new JLabel("Chỉnh sửa dụng cụ");
        title.setFont(AppFonts.lexendBold(24f));
        title.setForeground(TEXT_DARK);
        JLabel subtitle = new JLabel("Mã dụng cụ: " + item.maDc());
        subtitle.setFont(AppFonts.lexendRegular(13f));
        subtitle.setForeground(TEXT_MUTED);
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        JTextField txtMaDc = readonly(item.maDc());
        JTextField txtTenDc = new JTextField(item.tenDc());
        JTextField txtDvt = new JTextField(item.dvt());
        JTextField txtGia = new JTextField(item.gia().toPlainString());
        JTextField txtSlTon = readonly(String.valueOf(item.slTon()));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(CARD_BG);
        form.setBorder(new EmptyBorder(18, 18, 18, 18));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 0, 6, 0);

        addField(form, g, 0, "Mã dụng cụ", txtMaDc);
        addField(form, g, 1, "Tên dụng cụ", txtTenDc);
        addField(form, g, 2, "Đơn vị tính", txtDvt);
        addField(form, g, 3, "Giá (VNĐ)", txtGia);
        addField(form, g, 4, "Số lượng tồn", txtSlTon);
        root.add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(1, 2, 10, 0));
        actions.setOpaque(false);
        JButton btnCancel = button("Hủy", BUTTON_MUTED, TEXT_DARK);
        JButton btnSave = button("Lưu thay đổi", BRAND_BLUE, Color.WHITE);
        actions.add(btnCancel);
        actions.add(btnSave);
        root.add(actions, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            String tenDc = txtTenDc.getText().trim();
            String dvt = txtDvt.getText().trim();
            String giaText = txtGia.getText().trim();

            if (tenDc.isEmpty() || dvt.isEmpty() || giaText.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng điền đầy đủ tất cả các trường.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                double gia = Double.parseDouble(giaText);
                if (gia <= 0) {
                    JOptionPane.showMessageDialog(dialog, "Giá phải lớn hơn 0.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                System.out.println("[Equipment Edit] Mã: " + item.maDc() + ", Tên: " + tenDc + ", ĐVT: " + dvt + ", Giá: " + gia);
                JOptionPane.showMessageDialog(dialog, "Đã ghi nhận (mock). Thay đổi sẽ được lưu khi có BE.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Giá phải là số hợp lệ.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            }
        });

        dialog.pack();
        dialog.setSize(Math.max(dialog.getWidth(), 560), dialog.getHeight());
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    private static JTextField readonly(String value) {
        JTextField f = new JTextField(value);
        f.setEditable(false);
        f.setFocusable(false);
        f.setBackground(new Color(241, 245, 249));
        return f;
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
