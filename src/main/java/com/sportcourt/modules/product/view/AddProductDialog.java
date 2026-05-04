package com.sportcourt.modules.product.view;

import com.sportcourt.modules.product.dto.ProductCreateRequest;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Window;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class AddProductDialog extends JDialog {
    private static final Color TEXT   = new Color(15, 23, 42);
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Color GREEN  = new Color(34, 197, 94);
    private static final Color CANCEL = new Color(226, 232, 240);

    private final JTextField codeField  = new JTextField();
    private final JTextField nameField  = new JTextField();
    private final JTextField dvtField   = new JTextField();
    private final JTextField priceField = new JTextField();

    private final DecimalFormat moneyFormat;
    private ProductCreateRequest result;

    public AddProductDialog(Window owner) {
        super(owner, "Thêm sản phẩm", Dialog.ModalityType.APPLICATION_MODAL);

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator('.');
        moneyFormat = new DecimalFormat("#,###", symbols);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(600, 520);
        setMinimumSize(new Dimension(560, 500));
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        installMoneyFormatter(priceField);
        add(createContent(), BorderLayout.CENTER);
    }

    public ProductCreateRequest showDialog() {
        result = null;
        setVisible(true);
        return result;
    }

    private JPanel createContent() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(248, 250, 252));
        root.setBorder(new EmptyBorder(16, 24, 16, 24));

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(28, 48, 28, 48)
        ));

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new javax.swing.BoxLayout(form, javax.swing.BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Thêm sản phẩm mới", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(TEXT);
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        form.add(titleLabel);

        form.add(createField("Mã sản phẩm", codeField));
        form.add(createField("Tên sản phẩm", nameField));
        form.add(createField("Đơn vị tính", dvtField));
        form.add(createField("Đơn giá", priceField));
        form.add(createButtons());

        card.add(form, BorderLayout.CENTER);
        root.add(card, BorderLayout.CENTER);
        return root;
    }

    private JPanel createField(String labelText, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 10, 0));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(TEXT);

        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setForeground(TEXT);
        field.setPreferredSize(new Dimension(10, 44));
        field.setMinimumSize(new Dimension(10, 44));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(0, 12, 0, 12)
        ));

        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createButtons() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 16, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(14, 0, 0, 0));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));

        JButton cancelButton = new RoundedButton("Hủy", CANCEL, null, 22);
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        cancelButton.setForeground(TEXT);
        cancelButton.setPreferredSize(new Dimension(10, 50));
        cancelButton.setFocusPainted(false);
        cancelButton.setBorderPainted(false);
        cancelButton.setContentAreaFilled(false);
        cancelButton.setOpaque(false);
        cancelButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelButton.addActionListener(e -> dispose());

        JButton saveButton = new RoundedButton("Tạo sản phẩm", GREEN, null, 22);
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        saveButton.setForeground(Color.WHITE);
        saveButton.setPreferredSize(new Dimension(10, 50));
        saveButton.setFocusPainted(false);
        saveButton.setBorderPainted(false);
        saveButton.setContentAreaFilled(false);
        saveButton.setOpaque(false);
        saveButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveButton.addActionListener(e -> save());

        panel.add(cancelButton);
        panel.add(saveButton);
        return panel;
    }

    private void save() {
        String maSp     = text(codeField);
        String tenSp    = text(nameField);
        String dvt      = text(dvtField);
        String rawPrice = text(priceField);

        if (maSp.isEmpty()) {
            showError("Vui lòng nhập mã sản phẩm.");
            codeField.requestFocusInWindow();
            return;
        }
        if (maSp.length() > 20) {
            showError("Mã sản phẩm tối đa 20 ký tự.");
            codeField.requestFocusInWindow();
            return;
        }
        if (tenSp.isEmpty()) {
            showError("Vui lòng nhập tên sản phẩm.");
            nameField.requestFocusInWindow();
            return;
        }
        if (tenSp.length() > 100) {
            showError("Tên sản phẩm tối đa 100 ký tự.");
            nameField.requestFocusInWindow();
            return;
        }
        if (dvt.isEmpty()) {
            showError("Vui lòng nhập đơn vị tính.");
            dvtField.requestFocusInWindow();
            return;
        }
        if (dvt.length() > 20) {
            showError("Đơn vị tính tối đa 20 ký tự.");
            dvtField.requestFocusInWindow();
            return;
        }

        BigDecimal gia = parsePrice(rawPrice);
        if (gia == null || gia.compareTo(BigDecimal.ZERO) <= 0) {
            showError("Đơn giá phải là số lớn hơn 0.");
            priceField.requestFocusInWindow();
            return;
        }

        ProductCreateRequest request = new ProductCreateRequest();
        request.setMaSp(maSp);
        request.setTenSp(tenSp);
        request.setDvt(dvt);
        request.setGia(gia);

        result = request;
        dispose();
    }

    private void installMoneyFormatter(JTextField field) {
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new MoneyDocumentFilter());
    }

    private String formatMoneyDigits(String digits) {
        if (digits == null || digits.isEmpty()) return "";
        try {
            return moneyFormat.format(new BigDecimal(digits));
        } catch (NumberFormatException e) {
            return "";
        }
    }

    private BigDecimal parsePrice(String value) {
        if (value == null) return null;
        String normalized = value.trim().replace(".", "").replace(",", "");
        if (normalized.isEmpty() || !normalized.matches("\\d+")) return null;
        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String text(JTextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
    }

    private class MoneyDocumentFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            replace(fb, offset, 0, string, attr);
        }
        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            String oldValue = fb.getDocument().getText(0, fb.getDocument().getLength());
            StringBuilder builder = new StringBuilder(oldValue);
            builder.replace(offset, offset + length, text == null ? "" : text);
            String digits    = builder.toString().replaceAll("\\D", "");
            String formatted = formatMoneyDigits(digits);
            fb.replace(0, fb.getDocument().getLength(), formatted, attrs);
        }
        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            replace(fb, offset, length, "", null);
        }
    }

    private static class RoundedButton extends JButton {
        private final Color backgroundColor;
        private final Color borderColor;
        private final int   radius;
        RoundedButton(String text, Color backgroundColor, Color borderColor, int radius) {
            super(text);
            this.backgroundColor = backgroundColor;
            this.borderColor     = borderColor;
            this.radius          = radius;
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}