package com.sportcourt.modules.branch.view;

import com.sportcourt.common.style.AppFonts;
import com.sportcourt.modules.branch.dto.BranchCreateRequest;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;

final class BranchAdd {
    private static final int INPUT_CORNER_RADIUS = 25;
    private static final Color DIALOG_BG = new Color(248, 249, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BRAND_GREEN = new Color(34, 197, 94);
    private static final Color TEXT_DARK = new Color(30, 41, 59);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);
    private static final Color BUTTON_MUTED = new Color(226, 232, 240);

    private BranchAdd() {
    }

    static BranchCreateRequest show(Component parent, String generatedMaCn) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, "Thêm chi nhánh", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBackground(DIALOG_BG);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        dialog.setContentPane(root);

        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setOpaque(false);
        JLabel title = new JLabel("Thêm chi nhánh mới");
        title.setFont(AppFonts.lexendBold(24f));
        title.setForeground(TEXT_DARK);
        JLabel subtitle = new JLabel("Khởi tạo các thông tin cơ bản cho chi nhánh mới.");
        subtitle.setFont(AppFonts.lexendRegular(13f));
        subtitle.setForeground(TEXT_MUTED);
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        JTextField maCnField = readonly(generatedMaCn);
        JTextField tenChiNhanhField = new JTextField();
        JTextField diaChiField = new JTextField();
        JTextField hotlineField = new JTextField();

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(CARD_BG);
        form.setBorder(new EmptyBorder(18, 18, 18, 18));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 0, 6, 0);

        addField(form, g, 0, "Mã chi nhánh", maCnField);
        addField(form, g, 1, "Tên chi nhánh", tenChiNhanhField);
        addField(form, g, 2, "Địa chỉ", diaChiField);
        addField(form, g, 3, "Hotline", hotlineField);
        root.add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(1, 2, 10, 0));
        actions.setOpaque(false);
        JButton btnCancel = button("Hủy", BUTTON_MUTED, TEXT_DARK);
        JButton btnSave = button("Thêm chi nhánh", BRAND_GREEN, Color.WHITE);
        actions.add(btnCancel);
        actions.add(btnSave);
        root.add(actions, BorderLayout.SOUTH);

        final BranchCreateRequest[] result = new BranchCreateRequest[1];
        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            String ten = tenChiNhanhField.getText().trim();
            String diaChi = diaChiField.getText().trim();
            String hotline = hotlineField.getText().trim();
            if (ten.isBlank() || diaChi.isBlank()) {
                JOptionPane.showMessageDialog(dialog, "Tên chi nhánh và địa chỉ là bắt buộc.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            result[0] = new BranchCreateRequest(generatedMaCn, ten, diaChi, hotline.isEmpty() ? null : hotline);
            dialog.dispose();
        });

        dialog.pack();
        dialog.setSize(Math.max(dialog.getWidth(), 560), dialog.getHeight());
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        return result[0];
    }

    private static JTextField readonly(String value) {
        JTextField field = new JTextField(value == null ? "" : value);
        field.setEditable(false);
        field.setFocusable(false);
        field.setBackground(new Color(241, 245, 249));
        styleTextField(field);
        return field;
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
