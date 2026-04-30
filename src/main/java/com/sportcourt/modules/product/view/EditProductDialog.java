package com.sportcourt.modules.product.view;

import com.sportcourt.modules.product.dto.ProductResponse;
import com.sportcourt.modules.product.dto.ProductUpdateRequest;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.math.BigDecimal;
import java.text.DecimalFormat;

public class EditProductDialog extends JDialog {
    private static final Color TEXT = new Color(15, 23, 42);
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Color GREEN = new Color(34, 197, 94);
    private static final Color CANCEL = new Color(226, 232, 240);

    private final JTextField nameField = new JTextField();
    private final JTextField categoryField = new JTextField();
    private final JTextField priceField = new JTextField();
    private final JTextField quantityField = new JTextField();

    private final DecimalFormat moneyFormat = new DecimalFormat("0");
    private final ProductResponse product;
    private ProductUpdateRequest result;

    public EditProductDialog(Window owner, ProductResponse product) {
        super(owner, "Cập nhật sản phẩm", ModalityType.APPLICATION_MODAL);

        this.product = product;

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(560, 520);
        setMinimumSize(new Dimension(520, 480));
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

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
        root.setBorder(new EmptyBorder(14, 20, 14, 20));

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(0, 0, 0, 0)
        ));

        JPanel form = new JPanel();
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(28, 34, 26, 34));
        form.setLayout(new javax.swing.BoxLayout(form, javax.swing.BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Cập nhật sản phẩm", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT);
        title.setAlignmentX(CENTER_ALIGNMENT);
        title.setBorder(new EmptyBorder(0, 0, 22, 0));
        form.add(title);

        form.add(createTextInput("Tên sản phẩm", nameField));
        form.add(createTextInput("Danh mục", categoryField));
        form.add(createTextInput("Đơn giá", priceField));
        form.add(createTextInput("Số lượng", quantityField));
        form.add(createStatusHint());
        form.add(createButtons());

        JScrollPane scrollPane = new JScrollPane(form);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(64);
        scrollPane.getVerticalScrollBar().setBlockIncrement(300);

        card.add(scrollPane, BorderLayout.CENTER);
        root.add(card, BorderLayout.CENTER);
        return root;
    }

    private JPanel createTextInput(String labelText, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 14, 0));

        JLabel label = createLabel(labelText);

        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setForeground(TEXT);
        field.setPreferredSize(new Dimension(10, 40));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(0, 14, 0, 14)
        ));

        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStatusHint() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 18, 0));

        JLabel label = createLabel("Trạng thái");
        JLabel hint = new JLabel("Tự động: số lượng = 0 là Hết hàng, số lượng > 0 là Hoạt động.");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        hint.setForeground(new Color(100, 116, 139));
        hint.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(10, 12, 10, 12)
        ));

        panel.add(label, BorderLayout.NORTH);
        panel.add(hint, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createButtons() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 18, 0));
        panel.setOpaque(false);

        JButton cancelButton = new JButton("Hủy");
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        cancelButton.setForeground(TEXT);
        cancelButton.setBackground(CANCEL);
        cancelButton.setPreferredSize(new Dimension(10, 46));
        cancelButton.setFocusPainted(false);
        cancelButton.setBorderPainted(false);
        cancelButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelButton.addActionListener(e -> dispose());

        JButton saveButton = new JButton("Lưu thay đổi");
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        saveButton.setForeground(Color.WHITE);
        saveButton.setBackground(GREEN);
        saveButton.setPreferredSize(new Dimension(10, 46));
        saveButton.setFocusPainted(false);
        saveButton.setBorderPainted(false);
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
        categoryField.setText(nullToEmpty(product.getDanhMuc()));
        priceField.setText(product.getGia() == null ? "" : moneyFormat.format(product.getGia()));
        quantityField.setText(product.getSoLuongTon() == null ? "0" : String.valueOf(product.getSoLuongTon()));
    }

    private void save() {
        if (product == null || product.getMaSp() == null) {
            showInputError("Không tìm thấy sản phẩm cần cập nhật.");
            return;
        }

        String tenSp = text(nameField);
        String danhMuc = text(categoryField);
        String rawPrice = text(priceField);
        String rawQuantity = text(quantityField);

        if (tenSp.isEmpty()) {
            showInputError("Vui lòng nhập tên sản phẩm.");
            nameField.requestFocusInWindow();
            return;
        }

        if (danhMuc.isEmpty()) {
            showInputError("Vui lòng nhập danh mục sản phẩm.");
            categoryField.requestFocusInWindow();
            return;
        }

        BigDecimal gia = parsePrice(rawPrice);
        if (gia == null || gia.compareTo(BigDecimal.ZERO) <= 0) {
            showInputError("Đơn giá phải là số lớn hơn 0.");
            priceField.requestFocusInWindow();
            return;
        }

        Integer soLuong = parseInteger(rawQuantity);
        if (soLuong == null || soLuong < 0) {
            showInputError("Số lượng phải là số nguyên không âm.");
            quantityField.requestFocusInWindow();
            return;
        }

        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setMaSp(product.getMaSp());
        request.setTenSp(tenSp);
        request.setDanhMuc(danhMuc);
        request.setGia(gia);
        request.setSoLuongTon(soLuong);

        result = request;
        dispose();
    }

    private BigDecimal parsePrice(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().replace(".", "").replace(",", "");
        if (!normalized.matches("\\d+(\\.\\d+)?")) {
            return null;
        }
        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInteger(String value) {
        if (value == null || !value.trim().matches("\\d+")) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String text(JTextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private void showInputError(String message) {
        JOptionPane.showMessageDialog(this, message, "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
    }
}
