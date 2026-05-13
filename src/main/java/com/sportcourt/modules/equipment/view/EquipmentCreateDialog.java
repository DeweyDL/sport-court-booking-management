package com.sportcourt.modules.equipment.view;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Dialog thêm dụng cụ thể thao mới.
 * Chưa lưu DB — chỉ in kết quả ra console.
 */
final class EquipmentCreateDialog {

    private static final Color DIALOG_BG = new Color(248, 249, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BRAND_GREEN = new Color(16, 110, 0);
    private static final Color BRAND_GREEN_BG = new Color(228, 250, 226);
    private static final Color TEXT_DARK = new Color(30, 41, 59);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);
    private static final Color BORDER_COLOR = new Color(203, 213, 225);

    private EquipmentCreateDialog() {
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

    static void show(Component parent) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, "Thêm dụng cụ", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBackground(DIALOG_BG);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        dialog.setContentPane(root);

        // Header
        JLabel title = new JLabel("Thêm dụng cụ thể thao");
        title.setFont(new Font("Lexend", Font.BOLD, 24));
        title.setForeground(TEXT_DARK);
        title.setHorizontalAlignment(SwingConstants.LEFT);

        JLabel subtitle = new JLabel("Nhập thông tin cơ bản cho dụng cụ mới.");
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

        // Form fields
        JTextField txtTenDc = new JTextField();
        JTextField txtDvt = new JTextField();
        JTextField txtGia = new JTextField();
        JTextField txtSlTon = new JTextField();

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(CARD_BG);
        form.setBorder(new EmptyBorder(18, 18, 18, 18));
        form.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(createField("Tên dụng cụ", txtTenDc));
        form.add(Box.createVerticalStrut(14));
        form.add(createField("Đơn vị tính", txtDvt));
        form.add(Box.createVerticalStrut(14));
        form.add(createField("Giá (VNĐ)", txtGia));
        form.add(Box.createVerticalStrut(14));
        form.add(createField("Số lượng tồn", txtSlTon));

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(BorderFactory.createEmptyBorder());
        formScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);
        formScroll.getViewport().setBackground(DIALOG_BG);
        root.add(formScroll, BorderLayout.CENTER);

        // Actions
        JPanel actions = new JPanel(new GridLayout(1, 2, 10, 0));
        actions.setOpaque(false);

        JButton cancelBtn = createPillButton("Hủy", new Color(226, 232, 240), new Color(30, 41, 59));
        JButton saveBtn = createPillButton("Thêm dụng cụ", BRAND_GREEN, Color.WHITE);

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

                com.sportcourt.modules.equipment.dto.EquipmentCreateRequest req = new com.sportcourt.modules.equipment.dto.EquipmentCreateRequest();
                req.setTenDc(tenDc);
                req.setDvt(dvt);
                req.setGia(java.math.BigDecimal.valueOf(gia));
                req.setSlTon(slTon);
                
                new com.sportcourt.modules.equipment.controller.EquipmentController().createEquipment(req);
                
                JOptionPane.showMessageDialog(dialog, "Thêm dụng cụ thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                if (parent instanceof EquipmentManagement) {
                    ((EquipmentManagement) parent).refresh();
                }
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Giá và số lượng tồn phải là số hợp lệ.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Lỗi khi thêm: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
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

    private static JPanel createField(String labelText, JTextField field) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Lexend", Font.BOLD, 12));
        label.setForeground(TEXT_DARK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setFont(new Font("Lexend", Font.PLAIN, 14));
        field.setForeground(new Color(31, 41, 55));
        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(BORDER_COLOR, 25),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        field.setBackground(Color.WHITE);
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
