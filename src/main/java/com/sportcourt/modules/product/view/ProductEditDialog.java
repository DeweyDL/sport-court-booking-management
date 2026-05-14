package com.sportcourt.modules.product.view;

import com.sportcourt.modules.product.dto.ProductResponse;
import com.sportcourt.modules.product.dto.ProductUpdateRequest;

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

final class ProductEditDialog {
    private static final Color DIALOG_BG = new Color(248, 249, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BRAND_BLUE = new Color(29, 78, 216);
    private static final Color BRAND_BLUE_BG = new Color(239, 246, 255);
    private static final Color TEXT_DARK = new Color(30, 41, 59);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);
    private static final Color BORDER_COLOR = new Color(203, 213, 225);
    private static final Color READONLY_BG = new Color(241, 245, 249);

    private ProductEditDialog() {
    }

    static ProductUpdateRequest show(Component parent, ProductResponse product) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, "Cập nhật sản phẩm", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator('.');
        DecimalFormat moneyFormat = new DecimalFormat("#,###", symbols);

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(DIALOG_BG);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        dialog.setContentPane(root);

        JLabel title = new JLabel("Cập nhật sản phẩm");
        title.setFont(new Font("Lexend", Font.BOLD, 24));
        title.setForeground(TEXT_DARK);

        JLabel subtitle = new JLabel("Chỉnh sửa thông tin cho sản phẩm " + product.getMaSp() + ".");
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

        JTextField txtMaSp = createReadOnlyField(product.getMaSp());
        JTextField txtTenSp = createEditableField(product.getTenSp());
        JTextField txtDvt = createEditableField(product.getDvt());
        JTextField txtGia = createEditableField(product.getGia() == null ? "" : moneyFormat.format(product.getGia()));
        installMoneyFormatter(txtGia, moneyFormat);
        JTextField txtSlTon = createReadOnlyField(product.getSlTon() == null ? "0" : String.valueOf(product.getSlTon()));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(CARD_BG);
        form.setBorder(new EmptyBorder(18, 18, 18, 18));

        form.add(createField("Mã sản phẩm", txtMaSp));
        form.add(Box.createVerticalStrut(14));
        form.add(createField("Tên sản phẩm", txtTenSp));
        form.add(Box.createVerticalStrut(14));
        form.add(createField("Đơn vị tính", txtDvt));
        form.add(Box.createVerticalStrut(14));
        form.add(createField("Đơn giá", txtGia));
        form.add(Box.createVerticalStrut(14));
        form.add(createField("Số lượng tồn kho", txtSlTon));

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(BorderFactory.createEmptyBorder());
        com.sportcourt.common.style.CrudViewStyle.configureScrollPane(formScroll);
        formScroll.getViewport().setBackground(DIALOG_BG);
        root.add(formScroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(1, 2, 12, 0));
        actions.setOpaque(false);

        JButton cancelBtn = createPillButton("Hủy", new Color(226, 232, 240), new Color(30, 41, 59));
        JButton saveBtn = createPillButton("Lưu thay đổi", BRAND_BLUE, Color.WHITE);
        actions.add(cancelBtn);
        actions.add(saveBtn);
        root.add(actions, BorderLayout.SOUTH);

        final ProductUpdateRequest[] result = new ProductUpdateRequest[1];

        cancelBtn.addActionListener(event -> dialog.dispose());
        saveBtn.addActionListener(event -> {
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

            Integer slTon = product.getSlTon() == null ? 0 : product.getSlTon();

            ProductUpdateRequest request = new ProductUpdateRequest();
            request.setMaSp(product.getMaSp());
            request.setTenSp(tenSp);
            request.setDvt(dvt);
            request.setGia(gia);
            request.setSlTon(slTon);

            result[0] = request;
            dialog.dispose();
        });

        dialog.pack();
        applyResponsiveWindowSize(dialog, 0.45, 0.7, 560, 520);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return result[0];
    }

    private static void applyResponsiveWindowSize(JDialog dialog, double widthRatio, double heightRatio, int minWidth, int minHeight) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = Math.max(minWidth, (int) (screenSize.width * widthRatio));
        int height = Math.max(minHeight, (int) (screenSize.height * heightRatio));
        dialog.setSize(Math.min(width, screenSize.width), Math.min(height, screenSize.height));
        dialog.setMinimumSize(new Dimension(minWidth, minHeight));
    }

    private static JTextField createReadOnlyField(String value) {
        JTextField field = createBaseField(value);
        field.setEditable(false);
        field.setFocusable(false);
        field.setRequestFocusEnabled(false);
        field.setCursor(Cursor.getDefaultCursor());
        field.setFont(new Font("Lexend", Font.BOLD, 14));
        field.setBackground(READONLY_BG);
        return field;
    }

    private static JTextField createEditableField(String value) {
        JTextField field = createBaseField(value);
        field.setBackground(Color.WHITE);
        return field;
    }

    private static JTextField createBaseField(String value) {
        JTextField field = new JTextField(value);
        field.setFont(new Font("Lexend", Font.PLAIN, 14));
        field.setForeground(new Color(31, 41, 55));
        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(BORDER_COLOR, 25),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        return field;
    }

    private static JPanel createField(String labelText, JComponent field) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Lexend", Font.BOLD, 12));
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
        btn.setFont(new Font("Lexend", Font.BOLD, 13));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 18, 10, 18));
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
            g2.drawRoundRect(x + 1, y + 1, width - 3, height - 3, arc, arc);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(1, 1, 1, 1);
        }
    }
}
