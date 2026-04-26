package com.sportcourt.modules.customer.view;

import com.sportcourt.common.style.AppFonts;
import com.sportcourt.modules.customer.dto.CustomerProfile;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

final class CustomerProfileDialog {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    enum Action {
        UPDATE
    }

    private static final Color DIALOG_BG = new Color(248, 249, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(30, 41, 59);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);
    private static final Color BRAND_BLUE = new Color(37, 99, 235);
    private static final Color FIELD_BG = new Color(241, 245, 249);

    private CustomerProfileDialog() {
    }

    static Action show(Component parent, CustomerProfile profile) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, "Hồ sơ khách hàng", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBackground(DIALOG_BG);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        dialog.setContentPane(root);

        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setOpaque(false);
        JLabel title = new JLabel("Hồ sơ khách hàng");
        title.setFont(AppFonts.lexendBold(24f));
        title.setForeground(TEXT_DARK);
        JLabel subtitle = new JLabel("Xem nhanh thông tin chi tiết trước khi cập nhật.");
        subtitle.setFont(AppFonts.lexendRegular(13f));
        subtitle.setForeground(TEXT_MUTED);
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD_BG);
        card.setBorder(new EmptyBorder(18, 18, 18, 18));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 0, 6, 0);

        addField(card, g, 0, "Mã khách hàng", profile.maKhachHang());
        addField(card, g, 1, "Họ tên", profile.hoTen());
        addField(card, g, 2, "Số điện thoại", profile.sdt());
        addField(card, g, 3, "Email hệ thống", safeEmailText(profile.emailHeThong()));
        addField(card, g, 4, "Tên đăng nhập", safeText(profile.username()));
        addField(card, g, 5, "Địa chỉ", emptyIfMissing(profile.diaChi()));
        addField(card, g, 6, "Ngày sinh", formatDate(profile.ngaySinh()));
        addField(card, g, 7, "Trạng thái", safeText(profile.trangThai()));
        addField(card, g, 8, "Mã hạng", safeText(profile.maHang()));
        addField(card, g, 9, "Doanh thu", profile.doanhThu() == null ? "0 VNĐ" : profile.doanhThu() + " VNĐ");

        JScrollPane scrollPane = new JScrollPane(card);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.getViewport().setBackground(DIALOG_BG);
        scrollPane.setPreferredSize(new Dimension(480, 360));
        root.add(scrollPane, BorderLayout.CENTER);

        final Action[] result = new Action[1];
        JPanel actions = new JPanel(new GridLayout(1, 1, 0, 0));
        JButton btnUpdate = actionButton("Cập nhật thông tin", BRAND_BLUE, Color.WHITE);
        actions.add(btnUpdate);
        actions.setOpaque(false);
        root.add(actions, BorderLayout.SOUTH);
        btnUpdate.addActionListener(e -> {
            result[0] = Action.UPDATE;
            dialog.dispose();
        });

        dialog.pack();
        dialog.setSize(560, 560);
        dialog.setMinimumSize(new Dimension(560, 560));
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return result[0];
    }

    private static void addField(JPanel panel, GridBagConstraints g, int row, String label, String value) {
        g.gridy = row * 2;
        JLabel lb = new JLabel(label);
        lb.setFont(AppFonts.lexendBold(12f));
        lb.setForeground(TEXT_DARK);
        panel.add(lb, g);

        g.gridy = row * 2 + 1;
        JTextField field = new JTextField(value);
        field.setEditable(false);
        field.setFocusable(false);
        field.setRequestFocusEnabled(false);
        field.setCursor(Cursor.getDefaultCursor());
        field.setFont(AppFonts.lexendRegular(14f));
        field.setBackground(FIELD_BG);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        panel.add(field, g);
    }

    private static JButton actionButton(String text, Color background, Color foreground) {
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
}

