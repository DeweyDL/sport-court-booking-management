package com.sportcourt.modules.product.view;

import com.sportcourt.modules.product.dto.ProductResponse;
import com.sportcourt.modules.product.dto.ProductUpdateRequest;

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

public class EditProductDialog extends JDialog {
    private static final Color TEXT   = new Color(15, 23, 42);
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Color GREEN  = new Color(34, 197, 94);
    private static final Color CANCEL = new Color(226, 232, 240);

    private final JTextField nameField  = new JTextField();
    private final JTextField dvtField   = new JTextField();
    private final JTextField priceField = new JTextField();
    private final JTextField stockField = new JTextField();

    private final DecimalFormat moneyFormat;
    private final ProductResponse product;
    private ProductUpdateRequest result;

    public EditProductDialog(Window owner, ProductResponse product) {
        super(owner, "Cap nhat san pham", Dialog.ModalityType.APPLICATION_MODAL);

        this.product = product;

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator('.');
        moneyFormat = new DecimalFormat("#,###", symbols);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(760, 620);
        setMinimumSize(new Dimension(740, 600));
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        installMoneyFormatter(priceField);
        add(createContent(), BorderLayout.CENTER);
        fillData();
    }

    public ProductUpdateRequest showDialog() {
        result = null;
        setVisible(true);
        return result;
    }

    private JPanel createContent() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(248, 250, 252));
        root.setBorder(new EmptyBorder(18, 28, 18, 28));

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(32, 64, 32, 64)
        ));

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new javax.swing.BoxLayout(form, javax.swing.BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Cap nhat san pham", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        titleLabel.setForeground(TEXT);
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);
        titleLabel.setBorder(new EmptyBorder(0, 0, 22, 0));
        form.add(titleLabel);

        form.add(createTextInput("Ten san pham", nameField));
        form.add(createTextInput("Don vi tinh", dvtField));
        form.add(createTextInput("Don gia", priceField));
        form.add(createReadOnlyInput("So luong ton kho", stockField));
        form.add(createButtons());

        card.add(form, BorderLayout.CENTER);
        root.add(card, BorderLayout.CENTER);
        return root;
    }

    private JPanel createTextInput(String labelText, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(0, 7));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 12, 0));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        JLabel label = createLabel(labelText);

        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setForeground(TEXT);
        field.setPreferredSize(new Dimension(10, 48));
        field.setMinimumSize(new Dimension(10, 48));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(0, 14, 0, 14)
        ));

        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createReadOnlyInput(String labelText, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(0, 7));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 12, 0));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        JLabel label = createLabel(labelText);

        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setForeground(new Color(100, 116, 139));
        field.setBackground(new Color(241, 245, 249));
        field.setEditable(false);
        field.setPreferredSize(new Dimension(10, 48));
        field.setMinimumSize(new Dimension(10, 48));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                new EmptyBorder(0, 14, 0, 14)
        ));

        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createButtons() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(18, 0, 0, 0));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 74));

        JButton cancelButton = new RoundedButton("Huy", CANCEL, null, 24);
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        cancelButton.setForeground(TEXT);
        cancelButton.setPreferredSize(new Dimension(10, 55));
        cancelButton.setFocusPainted(false);
        cancelButton.setBorderPainted(false);
        cancelButton.setContentAreaFilled(false);
        cancelButton.setOpaque(false);
        cancelButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelButton.addActionListener(e -> dispose());

        JButton saveButton = new RoundedButton("Luu thay doi", GREEN, null, 24);
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        saveButton.setForeground(Color.WHITE);
        saveButton.setPreferredSize(new Dimension(10, 55));
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

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(TEXT);
        return label;
    }

    private void fillData() {
        if (product == null) {
            return;
        }

        nameField.setText(nullToEmpty(product.getTenSp()));
        dvtField.setText(nullToEmpty(product.getDvt()));
        priceField.setText(product.getGia() == null ? "" : moneyFormat.format(product.getGia()));
        stockField.setText(product.getSlTon() == null ? "0" : String.valueOf(product.getSlTon()));
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private void save() {
        if (product == null || product.getMaSp() == null || product.getMaSp().trim().isEmpty()) {
            showInputError("Khong tim thay san pham can cap nhat.");
            return;
        }

        String tenSp    = text(nameField);
        String dvt      = text(dvtField);
        String rawPrice = text(priceField);

        if (tenSp.isEmpty()) {
            showInputError("Vui long nhap ten san pham.");
            nameField.requestFocusInWindow();
            return;
        }

        if (tenSp.length() > 100) {
            showInputError("Ten san pham toi da 100 ky tu.");
            nameField.requestFocusInWindow();
            return;
        }

        if (dvt.isEmpty()) {
            showInputError("Vui long nhap don vi tinh.");
            dvtField.requestFocusInWindow();
            return;
        }

        if (dvt.length() > 20) {
            showInputError("Don vi tinh toi da 20 ky tu.");
            dvtField.requestFocusInWindow();
            return;
        }

        BigDecimal gia = parsePrice(rawPrice);

        if (gia == null || gia.compareTo(BigDecimal.ZERO) <= 0) {
            showInputError("Don gia phai la so lon hon 0.");
            priceField.requestFocusInWindow();
            return;
        }

        // So luong lay tu DB, khong cho chinh sua tren form
        Integer slTon = product.getSlTon() == null ? 0 : product.getSlTon();

        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setMaSp(product.getMaSp());
        request.setTenSp(tenSp);
        request.setDvt(dvt);
        request.setGia(gia);
        request.setSlTon(slTon);

        result = request;
        dispose();
    }

    private void installMoneyFormatter(JTextField field) {
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new MoneyDocumentFilter());
    }

    private String formatMoneyDigits(String digits) {
        if (digits == null || digits.isEmpty()) {
            return "";
        }

        try {
            return moneyFormat.format(new BigDecimal(digits));
        } catch (NumberFormatException e) {
            return "";
        }
    }

    private BigDecimal parsePrice(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim().replace(".", "").replace(",", "");

        if (normalized.isEmpty() || !normalized.matches("\\d+")) {
            return null;
        }

        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String text(JTextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    private void showInputError(String message) {
        JOptionPane.showMessageDialog(this, message, "Du lieu khong hop le", JOptionPane.WARNING_MESSAGE);
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
