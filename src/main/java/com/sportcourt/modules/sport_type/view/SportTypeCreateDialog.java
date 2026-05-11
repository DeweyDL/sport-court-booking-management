package com.sportcourt.modules.sport_type.view;

import com.sportcourt.modules.sport_type.dto.SportTypeForm;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;

final class SportTypeCreateDialog {

    private static final Color DIALOG_BG = new Color(248, 249, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BRAND_GREEN = new Color(16, 110, 0);
    private static final Color BRAND_GREEN_BG = new Color(220, 252, 231);
    private static final Color TEXT_DARK = new Color(30, 41, 59);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);
    private static final Color BORDER_COLOR = new Color(203, 213, 225);

    private SportTypeCreateDialog() {
    }

    static SportTypeForm show(Component parent) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, "Thêm loại thể thao", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(DIALOG_BG);
        root.setBorder(new EmptyBorder(22, 22, 22, 22));
        dialog.setContentPane(root);

        JLabel title = new JLabel("Thêm loại thể thao mới");
        title.setFont(new Font("Lexend", Font.BOLD, 22));
        title.setForeground(TEXT_DARK);

        JLabel subtitle = new JLabel("Nhập tên và mô tả cho loại thể thao mới.");
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

        JTextField txtName = new JTextField();
        JTextArea txtDescription = new JTextArea(3, 20);
        txtDescription.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtDescription.setForeground(new Color(31, 41, 55));
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setBackground(new Color(249, 250, 251));

        JScrollPane descScroll = new JScrollPane(txtDescription);
        descScroll.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(BORDER_COLOR, 12),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));
        descScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(CARD_BG);
        form.setBorder(new EmptyBorder(18, 18, 18, 18));
        form.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(createTextField("Tên loại thể thao", txtName));
        form.add(Box.createVerticalStrut(14));
        form.add(createAreaField("Mô tả", descScroll));
        root.add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(1, 2, 12, 0));
        actions.setOpaque(false);

        JButton cancelBtn = createPillButton("Hủy", new Color(229, 231, 235), new Color(31, 41, 55));
        JButton saveBtn = createPillButton("Thêm loại thể thao", BRAND_GREEN_BG, BRAND_GREEN);
        actions.add(cancelBtn);
        actions.add(saveBtn);
        root.add(actions, BorderLayout.SOUTH);

        final SportTypeForm[] result = new SportTypeForm[1];

        cancelBtn.addActionListener(e -> dialog.dispose());
        saveBtn.addActionListener(e -> {
            String name = txtName.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Tên loại thể thao không được để trống.",
                        "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String description = txtDescription.getText().trim();
            result[0] = new SportTypeForm(null, name, description.isEmpty() ? null : description);
            dialog.dispose();
        });

        dialog.pack();
        dialog.setSize(Math.max(dialog.getWidth(), 480), dialog.getHeight());
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return result[0];
    }

    private static JPanel createTextField(String labelText, JTextField field) {
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

    private static JPanel createAreaField(String labelText, JComponent field) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(75, 85, 99));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setAlignmentX(Component.LEFT_ALIGNMENT);

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