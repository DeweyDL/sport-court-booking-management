package com.sportcourt.modules.staff_type.view;

import com.sportcourt.modules.staff_type.dto.StaffTypeForm;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;

final class StaffTypeCreateDialog {

    private static final Color DIALOG_BG = new Color(248, 249, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BRAND_GREEN = new Color(16, 110, 0);
    private static final Color TEXT_DARK = new Color(30, 41, 59);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);
    private static final Color BORDER_COLOR = new Color(203, 213, 225);
    private static final Color READONLY_BG = new Color(241, 245, 249);

    private StaffTypeCreateDialog() {
    }

    static StaffTypeForm show(Component parent, String generatedId) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, "Thêm loại nhân viên", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBackground(DIALOG_BG);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        dialog.setContentPane(root);

        JLabel title = new JLabel("Thêm loại nhân viên mới");
        title.setFont(new Font("Lexend", Font.BOLD, 24));
        title.setForeground(TEXT_DARK);

        JLabel subtitle = new JLabel("Nhập vị trí và mức lương cho loại nhân viên mới.");
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

        JTextField txtId = createReadOnlyField(generatedId);
        JTextField txtPosition = new JTextField();
        JTextField txtSalary = new JTextField();

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(CARD_BG);
        form.setBorder(new EmptyBorder(18, 18, 18, 18));
        form.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(createTextField("Mã loại nhân viên", txtId));
        form.add(Box.createVerticalStrut(14));
        form.add(createTextField("Vị trí", txtPosition));
        form.add(Box.createVerticalStrut(14));
        form.add(createTextField("Mức lương (VNĐ)", txtSalary));
        root.add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(1, 2, 10, 0));
        actions.setOpaque(false);

        JButton cancelBtn = createPillButton("Hủy", new Color(226, 232, 240), new Color(30, 41, 59));
        JButton saveBtn = createPillButton("Thêm loại nhân viên", BRAND_GREEN, Color.WHITE);
        actions.add(cancelBtn);
        actions.add(saveBtn);
        root.add(actions, BorderLayout.SOUTH);

        final StaffTypeForm[] result = new StaffTypeForm[1];

        cancelBtn.addActionListener(e -> dialog.dispose());
        saveBtn.addActionListener(e -> {
            String position = txtPosition.getText().trim();
            if (position.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vị trí không được để trống.",
                        "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String salaryText = txtSalary.getText().trim();
            if (salaryText.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Mức lương không được để trống.",
                        "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            BigDecimal salary;
            try {
                salary = new BigDecimal(salaryText.replace(",", ""));
                if (salary.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Mức lương phải là số dương hợp lệ.",
                        "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            result[0] = new StaffTypeForm(txtId.getText().trim(), position, salary);
            dialog.dispose();
        });

        dialog.pack();
        dialog.setSize(Math.max(dialog.getWidth(), 560), dialog.getHeight());
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return result[0];
    }

    private static JTextField createReadOnlyField(String value) {
        JTextField field = new JTextField(value == null ? "" : value.trim()) {
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
        return field;
    }

    private static JPanel createTextField(String labelText, JTextField field) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Lexend", Font.BOLD, 12));
        label.setForeground(TEXT_DARK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        boolean editable = field.isEditable();
        field.setFont(new Font(editable ? "Lexend" : "Segoe UI", editable ? Font.PLAIN : Font.BOLD, editable ? 14 : 15));
        field.setForeground(new Color(31, 41, 55));
        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(BORDER_COLOR, 25),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        field.setBackground(editable ? Color.WHITE : READONLY_BG);
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
