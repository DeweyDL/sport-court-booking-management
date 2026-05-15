package com.sportcourt.modules.customer_booking.view;

import com.sportcourt.common.style.AppFonts;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static com.sportcourt.modules.customer_booking.view.CustomerBookingViewStyle.*;

public class CustomerBookingConfirmScreen extends JPanel {
    private final Runnable onBack;
    private final Runnable onSubmit;

    public CustomerBookingConfirmScreen(Runnable onBack, Runnable onSubmit) {
        this.onBack = onBack;
        this.onSubmit = onSubmit;
        AppFonts.register();
        setLayout(new BorderLayout());
        setBackground(PAGE_BG);
        setBorder(new EmptyBorder(s(24), s(24), s(40), s(24)));
        add(buildContent(), BorderLayout.NORTH);
    }

    private JComponent buildContent() {
        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;

        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, s(16), 0);
        content.add(buildTopLine(), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, s(32), 0);
        content.add(new HeroPanel("XÁC NHẬN ĐẶT SÂN"), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 0, 0);
        content.add(buildCardsRow(), gbc);
        return content;
    }

    private JComponent buildTopLine() {
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JButton back = new JButton("← Quay lại");
        back.setFont(bold(14f));
        back.setForeground(GREEN_DARK);
        back.setContentAreaFilled(false);
        back.setBorderPainted(false);
        back.setFocusPainted(false);
        back.setCursor(new Cursor(Cursor.HAND_CURSOR));
        back.addActionListener(e -> onBack.run());
        top.add(back, BorderLayout.WEST);

        JLabel brand = label("RENTSTA", bold(34f), BRAND_DARK);
        top.add(brand, BorderLayout.EAST);
        return top;
    }

    private JComponent buildCardsRow() {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTH;

        gbc.gridx = 0;
        gbc.weightx = 0.62;
        gbc.insets = new Insets(0, 0, 0, s(24));
        row.add(buildBookingInfoCard(), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.38;
        gbc.insets = new Insets(0, 0, 0, 0);
        row.add(buildPaymentSummaryCard(), gbc);
        return row;
    }

    private JComponent buildBookingInfoCard() {
        RoundedPanel card = new RoundedPanel(s(32), PANEL_BG, false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(s(24), s(28), s(28), s(28)));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(label("▣ THÔNG TIN ĐẶT SÂN", bold(20f), TEXT_DARK), BorderLayout.WEST);
        JButton addCourt = pillButton("Thêm sân", GREEN_DARK, Color.WHITE);
        addCourt.setFont(bold(13f));
        header.add(addCourt, BorderLayout.EAST);
        card.add(header);
        card.add(Box.createVerticalStrut(s(22)));

        card.add(infoRow("Khách hàng", "Nguyễn Văn A"));
        card.add(Box.createVerticalStrut(s(16)));
        card.add(infoRow("Số điện thoại", "0123456789"));
        card.add(Box.createVerticalStrut(s(16)));
        card.add(infoRow("Mã đơn", "xxxxxxxxx"));
        card.add(Box.createVerticalStrut(s(18)));
        card.add(line());
        card.add(Box.createVerticalStrut(s(14)));
        card.add(infoRow("Chi nhánh", "KINETIC Central Park"));
        card.add(Box.createVerticalStrut(s(16)));
        card.add(infoRow("Địa chỉ", "Quận 1, TP. Hồ Chí Minh"));
        card.add(Box.createVerticalStrut(s(18)));
        card.add(courtLine("Mã sân 1", "18:00 - 20:00 | 29/12/2026", "250.000đ/giờ"));
        card.add(Box.createVerticalStrut(s(10)));
        card.add(courtLine("Mã sân 2", "18:00 - 20:00 | 29/12/2026", "250.000đ/giờ"));
        card.add(Box.createVerticalStrut(s(18)));
        card.add(line());
        card.add(Box.createVerticalStrut(s(16)));
        card.add(totalRow("Tổng tiền thuê sân", "500.000đ", TEXT_DARK));
        return card;
    }

    private JComponent buildPaymentSummaryCard() {
        RoundedPanel card = new RoundedPanel(s(32), SOFT_GRAY, false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(s(24), s(28), s(28), s(28)));

        JLabel title = label("CHI TIẾT THANH TOÁN", bold(20f), TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(s(22)));
        card.add(infoRow("Tiền thuê sân", "500.000đ"));
        card.add(Box.createVerticalStrut(s(22)));
        card.add(line());
        card.add(Box.createVerticalStrut(s(18)));
        card.add(totalRow("TỔNG CỘNG", "500.000đ", GREEN_DARK));
        card.add(Box.createVerticalStrut(s(26)));

        JButton submit = pillButton("ĐẶT NGAY", GREEN, GREEN_DARK);
        submit.setFont(bold(28f));
        submit.setAlignmentX(Component.LEFT_ALIGNMENT);
        submit.setMaximumSize(new Dimension(Integer.MAX_VALUE, s(64)));
        submit.addActionListener(e -> onSubmit.run());
        card.add(submit);
        return card;
    }

    private JComponent infoRow(String leftText, String rightText) {
        JPanel row = new JPanel(new BorderLayout(s(16), 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, s(30)));
        row.add(label(leftText, regular(16f), TEXT_OLIVE), BorderLayout.WEST);
        JLabel value = label(rightText, bold(16f), TEXT_DARK);
        value.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(value, BorderLayout.EAST);
        return row;
    }

    private JComponent totalRow(String leftText, String rightText, Color valueColor) {
        JPanel row = new JPanel(new BorderLayout(s(16), 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, s(42)));
        row.add(label(leftText, bold(17f), TEXT_DARK), BorderLayout.WEST);
        JLabel value = label(rightText, bold(28f), valueColor);
        value.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(value, BorderLayout.EAST);
        return row;
    }

    private JComponent courtLine(String courtCode, String time, String price) {
        RoundedPanel row = new RoundedPanel(s(14), SURFACE_BG);
        row.setLayout(new GridBagLayout());
        row.setBorder(new EmptyBorder(s(8), s(12), s(8), s(12)));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, s(42)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        gbc.gridx = 0;
        gbc.weightx = 0.22;
        row.add(label(courtCode, regular(15f), TEXT_OLIVE), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.52;
        row.add(label(time, bold(15f), GREEN_DARK), gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.26;
        JLabel priceLabel = label(price, bold(15f), GREEN_DARK);
        priceLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(priceLabel, gbc);
        return row;
    }

    private JComponent line() {
        JPanel line = new JPanel();
        line.setOpaque(true);
        line.setBackground(new Color(113, 113, 122, 130));
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        line.setPreferredSize(new Dimension(0, 1));
        line.setAlignmentX(Component.LEFT_ALIGNMENT);
        return line;
    }
}
