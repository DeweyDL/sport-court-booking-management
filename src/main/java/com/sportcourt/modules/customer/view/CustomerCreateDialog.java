package com.sportcourt.modules.customer.view;

import com.sportcourt.modules.customer.dto.CreateCustomerRequest;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;

final class CustomerCreateDialog {

    private static final Color DIALOG_BG = new Color(248, 249, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BRAND_GREEN = new Color(22, 101, 52);
    private static final Color BRAND_GREEN_BG = new Color(220, 252, 231);
    private static final Color TEXT_DARK = new Color(30, 41, 59);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);
    private static final Color BORDER_COLOR = new Color(203, 213, 225);

    private CustomerCreateDialog() {
    }

    static CreateCustomerRequest show(Component parent) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, "Thêm khách hàng", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(DIALOG_BG);
        root.setBorder(new EmptyBorder(22, 22, 22, 22));
        dialog.setContentPane(root);

        JLabel title = new JLabel("Thêm khách hàng mới");
        title.setFont(new Font("Lexend", Font.BOLD, 22));
        title.setForeground(TEXT_DARK);
        title.setHorizontalAlignment(SwingConstants.LEFT);

        JLabel subtitle = new JLabel("Nhập thông tin cơ bản cho khách hàng mới.");
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

        JTextField txtHoTen = new JTextField();
        JTextField txtSdt = new JTextField();

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(CARD_BG);
        form.setBorder(new EmptyBorder(18, 18, 18, 18));
        form.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(createField("Họ tên", txtHoTen));
        form.add(Box.createVerticalStrut(14));
        form.add(createField("Số điện thoại", txtSdt));
        root.add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(1, 2, 12, 0));
        actions.setOpaque(false);

        JButton cancelBtn = createPillButton("Hủy", new Color(229, 231, 235), new Color(31, 41, 55));
        JButton saveBtn = createPillButton("Thêm khách hàng", BRAND_GREEN_BG, BRAND_GREEN);
        actions.add(cancelBtn);
        actions.add(saveBtn);
        root.add(actions, BorderLayout.SOUTH);

        final CreateCustomerRequest[] result = new CreateCustomerRequest[1];

        cancelBtn.addActionListener(event -> dialog.dispose());
        saveBtn.addActionListener(event -> {
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
        dialog.setSize(Math.max(dialog.getWidth(), 480), dialog.getHeight());
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return result[0];
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
        public Insets getBorderInsets(Component c) {
            return new Insets(1, 1, 1, 1);
        }
    }
}
