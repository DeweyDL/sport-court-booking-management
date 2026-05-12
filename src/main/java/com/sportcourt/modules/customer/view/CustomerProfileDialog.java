package com.sportcourt.modules.customer.view;

import com.sportcourt.modules.customer.dto.CustomerProfile;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

final class CustomerProfileDialog {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    enum Action {
        UPDATE
    }

    private static final Color DIALOG_BG = new Color(248, 249, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BRAND_BLUE = new Color(29, 78, 216);
    private static final Color BRAND_BLUE_BG = new Color(239, 246, 255);
    private static final Color TEXT_DARK = new Color(30, 41, 59);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);
    private static final Color BORDER_COLOR = new Color(203, 213, 225);
    private static final Color READONLY_BG = new Color(241, 245, 249);

    private CustomerProfileDialog() {
    }

    static Action show(Component parent, CustomerProfile profile) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, "Hồ sơ khách hàng", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(DIALOG_BG);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        dialog.setContentPane(root);

        JLabel title = new JLabel("Hồ sơ khách hàng");
        title.setFont(new Font("Lexend", Font.BOLD, 24));
        title.setForeground(TEXT_DARK);

        JLabel subtitle = new JLabel("Xem nhanh thông tin chi tiết của khách hàng " + profile.maKhachHang() + ".");
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

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(CARD_BG);
        form.setBorder(new EmptyBorder(18, 18, 18, 18));

        form.add(createField("Mã khách hàng", profile.maKhachHang()));
        form.add(Box.createVerticalStrut(14));
        form.add(createField("Họ tên", profile.hoTen()));
        form.add(Box.createVerticalStrut(14));
        form.add(createField("Số điện thoại", profile.sdt()));
        form.add(Box.createVerticalStrut(14));
        form.add(createField("Email hệ thống", safeEmailText(profile.emailHeThong())));
        form.add(Box.createVerticalStrut(14));
        form.add(createField("Tên đăng nhập", safeText(profile.username())));
        form.add(Box.createVerticalStrut(14));
        form.add(createField("Địa chỉ", emptyIfMissing(profile.diaChi())));
        form.add(Box.createVerticalStrut(14));
        form.add(createField("Ngày sinh", formatDate(profile.ngaySinh())));
        form.add(Box.createVerticalStrut(14));
        form.add(createField("Trạng thái", safeText(profile.trangThai())));
        form.add(Box.createVerticalStrut(14));
        form.add(createField("Mã hạng", safeText(profile.maHang())));
        form.add(Box.createVerticalStrut(14));
        form.add(createField("Doanh thu", formatCurrency(profile.doanhThu())));

        JScrollPane scrollPane = new JScrollPane(form);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.getViewport().setBackground(DIALOG_BG);
        root.add(scrollPane, BorderLayout.CENTER);

        final Action[] result = new Action[1];
        JPanel actions = new JPanel(new GridLayout(1, 1, 0, 0));
        actions.setOpaque(false);

        JButton updateBtn = createPillButton("Cập nhật thông tin", BRAND_BLUE, Color.WHITE);
        actions.add(updateBtn);
        root.add(actions, BorderLayout.SOUTH);
        updateBtn.addActionListener(event -> {
            result[0] = Action.UPDATE;
            dialog.dispose();
        });

        dialog.pack();
        applyResponsiveWindowSize(dialog, 0.4, 0.7, 560, 560);
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


    private static JPanel createField(String labelText, String value) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Lexend", Font.BOLD, 12));
        label.setForeground(new Color(75, 85, 99));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField field = new JTextField(value);
        field.setEditable(false);
        field.setFocusable(false);
        field.setRequestFocusEnabled(false);
        field.setCursor(Cursor.getDefaultCursor());
        field.setFont(new Font("Segoe UI", Font.BOLD, 15));
        field.setForeground(new Color(31, 41, 55));
        field.setBackground(READONLY_BG);
        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(BORDER_COLOR, 25),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
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

    private static String safeText(String value) {
        return value == null || value.isBlank() ? "Chưa có" : value;
    }

    private static String safeEmailText(String value) {
        return value == null ? "" : value.trim();
    }

    private static String emptyIfMissing(String value) {
        return value == null ? "" : value.trim();
    }

    private static String formatDate(LocalDate date) {
        return date == null ? "" : DATE_FORMAT.format(date);
    }

    private static String formatCurrency(BigDecimal value) {
        if (value == null) {
            return "0 VNĐ";
        }
        return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(value) + " VNĐ";
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
