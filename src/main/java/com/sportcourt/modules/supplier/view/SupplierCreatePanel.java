package com.sportcourt.modules.supplier.view;

import com.sportcourt.common.style.AppFonts;
import com.sportcourt.modules.supplier.controller.SupplierManagementController;
import com.sportcourt.modules.supplier.dto.SupplierCreateRequest;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;

final class SupplierCreatePanel {
    private static final int INPUT_CORNER_RADIUS = 25;
    private static final Color DIALOG_BG   = new Color(248, 249, 252);
    private static final Color CARD_BG     = Color.WHITE;
    private static final Color BRAND_GREEN = new Color(16, 110, 0);
    private static final Color TEXT_DARK   = new Color(30, 41, 59);
    private static final Color TEXT_MUTED  = new Color(100, 116, 139);
    private static final Color BUTTON_MUTED = new Color(226, 232, 240);

    private SupplierCreatePanel() {}

    /**
     * @param controller reuse the injected controller (avoids permission error in demo mode)
     */
    static void show(Component parent, SupplierManagementController controller, Runnable onSuccess) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, "Thêm nhà cung cấp", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBackground(DIALOG_BG);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        dialog.setContentPane(root);

        // ── Header ─────────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setOpaque(false);
        JLabel title = new JLabel("Thêm nhà cung cấp mới");
        title.setFont(AppFonts.lexendBold(24f));
        title.setForeground(TEXT_DARK);
        JLabel subtitle = new JLabel("Điền thông tin nhà cung cấp cần thêm.");
        subtitle.setFont(AppFonts.lexendRegular(13f));
        subtitle.setForeground(TEXT_MUTED);
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        // ── Fields ──────────────────────────────────────────────────────────────
        JTextField manccField  = new JTextField();
        JTextField tennccField = new JTextField();
        JTextField sdtField    = new JTextField();
        JTextField diachiField = new JTextField();
        JTextField emailField  = new JTextField();
        JTextField websiteField = new JTextField();

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(CARD_BG);
        form.setBorder(new EmptyBorder(18, 18, 18, 18));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx   = 0;
        g.weightx = 1;
        g.fill    = GridBagConstraints.HORIZONTAL;
        g.insets  = new Insets(6, 0, 6, 0);

        addField(form, g, 0, "Mã nhà cung cấp (*)",    manccField);
        addField(form, g, 1, "Tên nhà cung cấp (*)",   tennccField);
        addField(form, g, 2, "Số điện thoại (*)",       sdtField);
        addField(form, g, 3, "Địa chỉ (*)",             diachiField);
        addField(form, g, 4, "Email",                   emailField);
        addField(form, g, 5, "Website",                 websiteField);
        root.add(form, BorderLayout.CENTER);

        // ── Actions ─────────────────────────────────────────────────────────────
        JPanel actions = new JPanel(new GridLayout(1, 2, 10, 0));
        actions.setOpaque(false);
        JButton btnCancel  = button("Hủy",               BUTTON_MUTED, TEXT_DARK);
        JButton btnConfirm = button("Thêm nhà cung cấp", BRAND_GREEN,  Color.WHITE);
        actions.add(btnCancel);
        actions.add(btnConfirm);
        root.add(actions, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dialog.dispose());
        btnConfirm.addActionListener(e -> {
            if (manccField.getText().isBlank()
                    || tennccField.getText().isBlank()
                    || sdtField.getText().isBlank()
                    || diachiField.getText().isBlank()) {
                JOptionPane.showMessageDialog(dialog,
                        "Các trường bắt buộc (*) không được để trống.",
                        "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                SupplierCreateRequest request = new SupplierCreateRequest();
                request.setMancc(manccField.getText().trim());
                request.setTenncc(tennccField.getText().trim());
                request.setSdt(sdtField.getText().trim());
                request.setDiachi(diachiField.getText().trim());
                request.setEmail(emailField.getText().trim());
                request.setWebsite(websiteField.getText().trim());
                controller.createSupplier(request);
                JOptionPane.showMessageDialog(dialog,
                        "Đã thêm nhà cung cấp thành công.",
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                if (onSuccess != null) onSuccess.run();
            } catch (Exception exception) {
                JOptionPane.showMessageDialog(
                        dialog,
                        exception.getCause() == null
                                ? exception.getMessage()
                                : exception.getCause().getMessage(),
                        "Lỗi thêm nhà cung cấp",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        dialog.pack();
        dialog.setSize(Math.max(dialog.getWidth(), 480), dialog.getHeight());
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private static JTextField readonly(String placeholder) {
        JTextField field = new JTextField(placeholder);
        field.setEditable(false);
        field.setFocusable(false);
        field.setOpaque(true);
        field.setBackground(new Color(241, 245, 249));
        field.setForeground(new Color(148, 163, 184));
        field.setFont(AppFonts.lexendRegular(13f));
        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(new Color(203, 213, 225), INPUT_CORNER_RADIUS),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        return field;
    }

    private static void addField(JPanel panel, GridBagConstraints g, int row,
                                 String label, JTextField field) {
        g.gridy = row * 2;
        JLabel lb = new JLabel(label);
        lb.setFont(AppFonts.lexendBold(12f));
        lb.setForeground(TEXT_DARK);
        panel.add(lb, g);

        g.gridy = row * 2 + 1;
        if (field.isEditable()) {
            field.setFont(AppFonts.lexendRegular(14f));
            field.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedLineBorder(new Color(203, 213, 225), INPUT_CORNER_RADIUS),
                    BorderFactory.createEmptyBorder(10, 12, 10, 12)
            ));
            field.setBackground(Color.WHITE);
        }
        panel.add(field, g);
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

        RoundedLineBorder(Color color, int arc) {
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
    }
}