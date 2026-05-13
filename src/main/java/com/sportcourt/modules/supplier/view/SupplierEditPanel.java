package com.sportcourt.modules.supplier.view;

import com.sportcourt.common.style.AppFonts;
import com.sportcourt.modules.supplier.controller.SupplierManagementController;
import com.sportcourt.modules.supplier.dto.SupplierUpdateRequest;
import com.sportcourt.modules.supplier.entity.Supplier;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;

final class SupplierEditPanel {
    private static final int INPUT_CORNER_RADIUS = 25;
    private static final Color DIALOG_BG   = new Color(248, 249, 252);
    private static final Color CARD_BG     = Color.WHITE;
    private static final Color BRAND_BLUE  = new Color(29, 78, 216);
    private static final Color TEXT_DARK   = new Color(30, 41, 59);
    private static final Color TEXT_MUTED  = new Color(100, 116, 139);
    private static final Color BUTTON_MUTED = new Color(226, 232, 240);

    private SupplierEditPanel() {}

    static void show(Component parent, Supplier current, SupplierManagementController controller, Runnable onSuccess) {
        if (current == null) return;

        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, "Cập nhật nhà cung cấp", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBackground(DIALOG_BG);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        dialog.setContentPane(root);

        // ── Header ─────────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setOpaque(false);
        JLabel title = new JLabel("Cập nhật nhà cung cấp");
        title.setFont(AppFonts.lexendBold(24f));
        title.setForeground(TEXT_DARK);
        JLabel subtitle = new JLabel("Chỉnh sửa thông tin nhà cung cấp.");
        subtitle.setFont(AppFonts.lexendRegular(13f));
        subtitle.setForeground(TEXT_MUTED);
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        // ── Fields ──────────────────────────────────────────────────────────────
        JTextField manccField  = readonly(current.getMancc());
        JTextField tennccField = new JTextField(current.getTenncc());
        JTextField sdtField    = new JTextField(current.getSdt());
        JTextField diachiField = new JTextField(current.getDiachi()   != null ? current.getDiachi()   : "");
        JTextField emailField  = new JTextField(current.getEmail()    != null ? current.getEmail()    : "");
        JTextField websiteField = new JTextField(current.getWebsite() != null ? current.getWebsite()  : "");

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(CARD_BG);
        form.setBorder(new EmptyBorder(18, 18, 18, 18));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx   = 0;
        g.weightx = 1;
        g.fill    = GridBagConstraints.HORIZONTAL;
        g.insets  = new Insets(6, 0, 6, 0);

        addField(form, g, 0, "Mã nhà cung cấp",       manccField);
        addField(form, g, 1, "Tên nhà cung cấp (*)",   tennccField);
        addField(form, g, 2, "Số điện thoại (*)",       sdtField);
        addField(form, g, 3, "Địa chỉ (*)",             diachiField);
        addField(form, g, 4, "Email",                   emailField);
        addField(form, g, 5, "Website",                 websiteField);
        root.add(form, BorderLayout.CENTER);

        // ── Actions ─────────────────────────────────────────────────────────────
        JPanel actions = new JPanel(new GridLayout(1, 2, 10, 0));
        actions.setOpaque(false);
        JButton btnCancel = button("Hủy",          BUTTON_MUTED, TEXT_DARK);
        JButton btnSave   = button("Lưu thay đổi", BRAND_BLUE,   Color.WHITE);
        actions.add(btnCancel);
        actions.add(btnSave);
        root.add(actions, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            if (tennccField.getText().isBlank()
                    || sdtField.getText().isBlank()
                    || diachiField.getText().isBlank()) {
                JOptionPane.showMessageDialog(dialog,
                        "Các trường bắt buộc (*) không được để trống.",
                        "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                SupplierUpdateRequest request = new SupplierUpdateRequest();
                request.setMancc(current.getMancc());
                request.setTenncc(tennccField.getText().trim());
                request.setSdt(sdtField.getText().trim());
                request.setDiachi(diachiField.getText().trim());
                request.setEmail(emailField.getText().trim());
                request.setWebsite(websiteField.getText().trim());
                controller.updateSupplier(request);
                // Sync entity so DetailPanel refreshes correctly
                current.setTenncc(request.getTenncc());
                current.setSdt(request.getSdt());
                current.setDiachi(request.getDiachi());
                current.setEmail(request.getEmail());
                current.setWebsite(request.getWebsite());
                JOptionPane.showMessageDialog(dialog,
                        "Đã cập nhật nhà cung cấp thành công.",
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                if (onSuccess != null) onSuccess.run();
            } catch (Exception exception) {
                JOptionPane.showMessageDialog(
                        dialog,
                        exception.getCause() == null
                                ? exception.getMessage()
                                : exception.getCause().getMessage(),
                        "Lỗi cập nhật nhà cung cấp",
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

    private static JTextField readonly(String value) {
        JTextField field = new JTextField(value == null ? "" : value) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2,
                        INPUT_CORNER_RADIUS, INPUT_CORNER_RADIUS);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        field.setEditable(false);
        field.setFocusable(false);
        field.setOpaque(false);
        field.setBackground(new Color(241, 245, 249));
        styleTextField(field);
        return field;
    }

    private static void addField(JPanel panel, GridBagConstraints g, int row,
                                 String label, JTextField field) {
        g.gridy = row * 2;
        JLabel lb = new JLabel(label);
        lb.setFont(AppFonts.lexendBold(12f));
        lb.setForeground(new Color(30, 41, 59));
        panel.add(lb, g);

        g.gridy = row * 2 + 1;
        styleTextField(field);
        panel.add(field, g);
    }

    private static void styleTextField(JTextField field) {
        field.setFont(AppFonts.lexendRegular(14f));
        field.setBorder(BorderFactory.createCompoundBorder(
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