package com.sportcourt.modules.product.view;

import com.sportcourt.common.style.AppFonts;
import com.sportcourt.modules.product.dto.ProductCreateRequest;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

final class ProductCreateDialog {
    private static final int INPUT_CORNER_RADIUS = 25;
    private static final Color DIALOG_BG = new Color(248, 249, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BRAND_GREEN = new Color(34, 197, 94);
    private static final Color TEXT_DARK = new Color(30, 41, 59);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);
    private static final Color BUTTON_MUTED = new Color(226, 232, 240);

    private ProductCreateDialog() {
    }

    static ProductCreateRequest show(Component parent, String generatedProductId) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator('.');
        DecimalFormat moneyFormat = new DecimalFormat("#,###", symbols);

        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, "Thêm sản phẩm", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBackground(DIALOG_BG);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        dialog.setContentPane(root);

        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setOpaque(false);
        JLabel title = new JLabel("Thêm sản phẩm mới");
        title.setFont(AppFonts.lexendBold(24f));
        title.setForeground(TEXT_DARK);
        JLabel subtitle = new JLabel("Nhập thông tin cơ bản cho sản phẩm mới.");
        subtitle.setFont(AppFonts.lexendRegular(13f));
        subtitle.setForeground(TEXT_MUTED);
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        JTextField txtMaSp = readonly(generatedProductId == null || generatedProductId.isBlank() ? "SP-1" : generatedProductId.trim());
        JTextField txtTenSp = new JTextField();
        JTextField txtDvt = new JTextField();
        JTextField txtGia = new JTextField();
        installMoneyFormatter(txtGia, moneyFormat);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(CARD_BG);
        form.setBorder(new EmptyBorder(18, 18, 18, 18));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 0, 6, 0);

        addField(form, g, 0, "Mã sản phẩm", txtMaSp);
        addField(form, g, 1, "Tên sản phẩm", txtTenSp);
        addField(form, g, 2, "Đơn vị tính", txtDvt);
        addField(form, g, 3, "Đơn giá", txtGia);
        root.add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(1, 2, 10, 0));
        actions.setOpaque(false);
        JButton btnCancel = button("Hủy", BUTTON_MUTED, TEXT_DARK);
        JButton btnSave = button("Thêm sản phẩm", BRAND_GREEN, Color.WHITE);
        actions.add(btnCancel);
        actions.add(btnSave);
        root.add(actions, BorderLayout.SOUTH);

        final ProductCreateRequest[] result = new ProductCreateRequest[1];
        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            String tenSp = txtTenSp.getText().trim();
            String dvt = txtDvt.getText().trim();
            String rawGia = txtGia.getText().trim();

            if (tenSp.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Tên sản phẩm không được để trống.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (tenSp.length() > 100) {
                JOptionPane.showMessageDialog(dialog, "Tên sản phẩm tối đa 100 ký tự.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (dvt.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Đơn vị tính không được để trống.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (dvt.length() > 20) {
                JOptionPane.showMessageDialog(dialog, "Đơn vị tính tối đa 20 ký tự.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            BigDecimal gia = parsePrice(rawGia);
            if (gia == null || gia.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(dialog, "Đơn giá phải là số lớn hơn 0.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            ProductCreateRequest req = new ProductCreateRequest();
            req.setMaSp(txtMaSp.getText().trim());
            req.setTenSp(tenSp);
            req.setDvt(dvt);
            req.setGia(gia);
            result[0] = req;
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
        lb.setForeground(new Color(30, 41, 59));
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

    private static void installMoneyFormatter(JTextField field, DecimalFormat moneyFormat) {
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                replace(fb, offset, 0, string, attr);
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                String oldValue = fb.getDocument().getText(0, fb.getDocument().getLength());
                StringBuilder builder = new StringBuilder(oldValue);
                builder.replace(offset, offset + length, text == null ? "" : text);
                String digits = builder.toString().replaceAll("\\D", "");
                String formatted = digits.isEmpty() ? "" : moneyFormat.format(new BigDecimal(digits));
                fb.replace(0, fb.getDocument().getLength(), formatted, attrs);
            }

            @Override
            public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
                replace(fb, offset, length, "", null);
            }
        });
    }

    private static BigDecimal parsePrice(String value) {
        if (value == null) return null;
        String normalized = value.trim().replace(".", "").replace(",", "");
        if (normalized.isEmpty() || !normalized.matches("\\d+")) return null;
        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException e) {
            return null;
        }
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
