package com.sportcourt.modules.equipment.view;

import com.sportcourt.modules.equipment.view.EquipmentMockData.EquipmentItem;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;

/**
 * Dialog chỉnh sửa dụng cụ thể thao.
 * Chưa lưu DB — chỉ in kết quả ra console.
 */
final class EquipmentEditDialog {

    private static final Color DIALOG_BG = new Color(248, 249, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BRAND_BLUE = new Color(29, 78, 216);
    private static final Color BRAND_BLUE_BG = new Color(239, 246, 255);
    private static final Color TEXT_DARK = new Color(30, 41, 59);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);
    private static final Color BORDER_COLOR = new Color(203, 213, 225);
    private static final Color READONLY_BG = new Color(241, 245, 249);

    private EquipmentEditDialog() {
    }

    private static void applyResponsiveWindowSize(JDialog dialog, int baseWidth, int baseHeight) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double widthRatio = screenSize.getWidth() / 1920.0;
        double heightRatio = screenSize.getHeight() / 1080.0;
        double ratio = Math.min(widthRatio, heightRatio);
        if (ratio < 0.8) ratio = 0.8;

        int width = (int) (baseWidth * ratio);
        int height = (int) (baseHeight * ratio);
        dialog.setSize(width, height);
    }

    static void show(Component parent, EquipmentItem item) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, "Chỉnh sửa dụng cụ", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(DIALOG_BG);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        dialog.setContentPane(root);

        // Header
        JLabel title = new JLabel("Chỉnh sửa dụng cụ");
        title.setFont(new Font("Lexend", Font.BOLD, 24));
        title.setForeground(TEXT_DARK);

        JLabel subtitle = new JLabel("Cập nhật thông tin cho dụng cụ " + item.maDc() + ".");
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
        JTextField txtMaDc = createReadOnlyField(item.maDc());
        JTextField txtTenDc = new JTextField(item.tenDc());
        JTextField txtDvt = new JTextField(item.dvt());
        JTextField txtGia = new JTextField(item.gia().toPlainString());
        JTextField txtSlTon = new JTextField(String.valueOf(item.slTon()));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(CARD_BG);
        form.setBorder(new EmptyBorder(18, 18, 18, 18));

        form.add(createField("Mã dụng cụ", txtMaDc, true));
        form.add(Box.createVerticalStrut(14));
        form.add(createField("Tên dụng cụ", txtTenDc, false));
        form.add(Box.createVerticalStrut(14));
        form.add(createField("Đơn vị tính", txtDvt, false));
        form.add(Box.createVerticalStrut(14));
        form.add(createField("Giá (VNĐ)", txtGia, false));
        form.add(Box.createVerticalStrut(14));
        form.add(createField("Số lượng tồn", txtSlTon, false));
        root.add(form, BorderLayout.CENTER);

        // Actions
        JPanel actions = new JPanel(new GridLayout(1, 2, 12, 0));
        actions.setOpaque(false);

        JButton cancelBtn = createPillButton("Hủy", new Color(226, 232, 240), new Color(30, 41, 59));
        JButton saveBtn = createPillButton("Lưu thay đổi", BRAND_BLUE, Color.WHITE);

        cancelBtn.addActionListener(event -> dialog.dispose());
        saveBtn.addActionListener(event -> {
            String tenDc = txtTenDc.getText().trim();
            String dvt = txtDvt.getText().trim();
            String giaText = txtGia.getText().trim();
            String slText = txtSlTon.getText().trim();

            if (tenDc.isEmpty() || dvt.isEmpty() || giaText.isEmpty() || slText.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng điền đầy đủ tất cả các trường.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                double gia = Double.parseDouble(giaText);
                int slTon = Integer.parseInt(slText);
                if (gia <= 0) {
                    JOptionPane.showMessageDialog(dialog, "Giá phải lớn hơn 0.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (slTon < 0) {
                    JOptionPane.showMessageDialog(dialog, "Số lượng tồn không được âm.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                com.sportcourt.modules.equipment.dto.EquipmentUpdateRequest req = new com.sportcourt.modules.equipment.dto.EquipmentUpdateRequest();
                req.setMaDc(item.maDc());
                req.setTenDc(tenDc);
                req.setDvt(dvt);
                req.setGia(java.math.BigDecimal.valueOf(gia));
                req.setSlTon(slTon);
                
                new com.sportcourt.modules.equipment.controller.EquipmentController().updateEquipment(req);
                
                JOptionPane.showMessageDialog(dialog, "Cập nhật dụng cụ thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                if (parent instanceof EquipmentManagement) {
                    ((EquipmentManagement) parent).refresh();
                }
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Giá và số lượng tồn phải là số hợp lệ.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Lỗi khi sửa: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        actions.add(cancelBtn);
        actions.add(saveBtn);
        root.add(actions, BorderLayout.SOUTH);

        dialog.pack();
        applyResponsiveWindowSize(dialog, 560, dialog.getHeight());
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private static JTextField createReadOnlyField(String value) {
        JTextField field = new JTextField(value);
        field.setEditable(false);
        field.setFocusable(false);
        field.setFont(new Font("Segoe UI", Font.BOLD, 15));
        field.setForeground(new Color(31, 41, 55));
        field.setBackground(READONLY_BG);
        return field;
    }

    private static JPanel createField(String labelText, JTextField field, boolean readOnly) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Lexend", Font.BOLD, 12));
        label.setForeground(new Color(75, 85, 99));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (!readOnly) {
            field.setFont(new Font("Lexend", Font.PLAIN, 14));
            field.setForeground(new Color(31, 41, 55));
            field.setBackground(Color.WHITE);
        }
        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(BORDER_COLOR, 25),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
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

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(1, 1, 1, 1);
        }
    }
}
