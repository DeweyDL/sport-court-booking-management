package com.sportcourt.modules.customer_booking.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;

import static com.sportcourt.modules.customer_booking.view.CustomerBookingViewStyle.*;

final class CustomerBookingDialogs {
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0");

    private CustomerBookingDialogs() {
    }

    static boolean showDepositPaymentDialog(Component parent, BigDecimal deposit) {
        Window owner = SwingUtilities.getWindowAncestor(parent);
        PaymentDialog dialog = new PaymentDialog(owner, deposit);
        dialog.setVisible(true);
        return dialog.confirmed();
    }

    static void showSuccessDialog(Component parent, Runnable onHome, Runnable onHistory) {
        Window owner = SwingUtilities.getWindowAncestor(parent);
        SuccessDialog dialog = new SuccessDialog(owner, onHome, onHistory);
        dialog.setVisible(true);
    }

    private static String money(BigDecimal value) {
        return MONEY_FORMAT.format(value == null ? BigDecimal.ZERO : value) + "đ";
    }

    private static final class PaymentDialog extends JDialog {
        private boolean confirmed;

        private PaymentDialog(Window owner, BigDecimal deposit) {
            super(owner, "Thanh toán đặt cọc", ModalityType.APPLICATION_MODAL);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setContentPane(buildContent(deposit));
            pack();
            setMinimumSize(new Dimension(s(680), s(520)));
            setLocationRelativeTo(owner);
        }

        private boolean confirmed() {
            return confirmed;
        }

        private JComponent buildContent(BigDecimal deposit) {
            JPanel root = new JPanel(new BorderLayout());
            root.setBackground(PAGE_BG);
            root.setBorder(new EmptyBorder(s(18), s(18), s(18), s(18)));

            RoundedPanel card = new RoundedPanel(s(32), SOFT_GRAY, false);
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(new EmptyBorder(s(32), s(32), s(24), s(32)));

            JLabel title = label("CHI TIẾT THANH TOÁN", bold(24f), TEXT_DARK);
            title.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(title);
            card.add(Box.createVerticalStrut(s(22)));
            card.add(paymentRow("Tiền cọc", money(deposit), regular(20f), bold(20f), TEXT_DARK));
            card.add(Box.createVerticalStrut(s(24)));
            card.add(separator());
            card.add(Box.createVerticalStrut(s(22)));
            card.add(paymentRow("TỔNG CỘNG", money(deposit), bold(24f), bold(32f), GREEN_DARK));
            card.add(Box.createVerticalStrut(s(18)));
            card.add(separator());
            card.add(Box.createVerticalStrut(s(24)));
            card.add(bankTransferPanel());
            card.add(Box.createVerticalStrut(s(24)));
            card.add(holdNotice());
            card.add(Box.createVerticalStrut(s(18)));
            card.add(actions());

            root.add(card, BorderLayout.CENTER);
            return root;
        }

        private JComponent paymentRow(String left, String right, Font leftFont, Font rightFont, Color rightColor) {
            JPanel row = new JPanel(new BorderLayout(s(20), 0));
            row.setOpaque(false);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, s(44)));
            row.add(label(left, leftFont, TEXT_OLIVE), BorderLayout.WEST);
            JLabel value = label(right, rightFont, rightColor);
            value.setHorizontalAlignment(SwingConstants.RIGHT);
            row.add(value, BorderLayout.EAST);
            return row;
        }

        private JComponent separator() {
            JPanel line = new JPanel();
            line.setOpaque(true);
            line.setBackground(new Color(60, 75, 53, 120));
            line.setPreferredSize(new Dimension(0, 1));
            line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            line.setAlignmentX(Component.LEFT_ALIGNMENT);
            return line;
        }

        private JComponent bankTransferPanel() {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setOpaque(false);
            panel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, s(180)));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridy = 0;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.anchor = GridBagConstraints.CENTER;

            gbc.gridx = 0;
            gbc.weightx = 0.42;
            gbc.insets = new Insets(0, 0, 0, s(34));
            panel.add(new QrPanel(), gbc);

            gbc.gridx = 1;
            gbc.weightx = 0.58;
            gbc.insets = new Insets(0, 0, 0, 0);
            panel.add(bankInfo(), gbc);
            return panel;
        }

        private JComponent bankInfo() {
            JPanel info = new JPanel(new GridLayout(3, 2, s(18), s(18)));
            info.setOpaque(false);
            info.add(label("Ngân hàng", regular(20f), TEXT_OLIVE));
            info.add(rightLabel("Vietcombank", bold(20f), TEXT_DARK));
            info.add(label("Số tài khoản", regular(20f), TEXT_OLIVE));
            info.add(rightLabel("0123456789", bold(20f), TEXT_DARK));
            info.add(label("Chủ tài khoản", regular(20f), TEXT_OLIVE));
            info.add(rightLabel("Nguyễn Văn A", bold(20f), TEXT_DARK));
            return info;
        }

        private JLabel rightLabel(String text, Font font, Color color) {
            JLabel value = label(text, font, color);
            value.setHorizontalAlignment(SwingConstants.RIGHT);
            return value;
        }

        private JComponent holdNotice() {
            RoundedPanel notice = new RoundedPanel(s(15), Color.WHITE, false);
            notice.setLayout(new BorderLayout());
            notice.setBorder(new EmptyBorder(s(10), s(16), s(10), s(16)));
            notice.setMaximumSize(new Dimension(Integer.MAX_VALUE, s(76)));
            JLabel text = label("<html><div style='text-align:center'>Sân sẽ được giữ chỗ trong vòng<br>05:00 phút</div></html>",
                    bold(20f), GREEN_DARK);
            text.setHorizontalAlignment(SwingConstants.CENTER);
            notice.add(text, BorderLayout.CENTER);
            return notice;
        }

        private JComponent actions() {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, s(12), 0));
            row.setOpaque(false);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, s(48)));

            JButton cancel = roundedAction("Hủy", Color.WHITE, TEXT_DARK);
            cancel.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(BORDER, s(999)),
                    new EmptyBorder(s(8), s(22), s(8), s(22))
            ));
            cancel.addActionListener(e -> dispose());

            JButton paid = roundedAction("Tôi đã chuyển khoản", GREEN, GREEN_DARK);
            paid.addActionListener(e -> {
                confirmed = true;
                dispose();
            });

            row.add(cancel);
            row.add(paid);
            return row;
        }
    }

    private static final class SuccessDialog extends JDialog {
        private final Runnable onHome;
        private final Runnable onHistory;

        private SuccessDialog(Window owner, Runnable onHome, Runnable onHistory) {
            super(owner, "Đặt sân thành công", ModalityType.APPLICATION_MODAL);
            this.onHome = onHome;
            this.onHistory = onHistory;
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setContentPane(buildContent());
            pack();
            setMinimumSize(new Dimension(s(780), s(520)));
            setLocationRelativeTo(owner);
        }

        private JComponent buildContent() {
            SuccessPanel root = new SuccessPanel();
            root.setLayout(new GridBagLayout());
            root.setBorder(new EmptyBorder(s(64), s(48), s(64), s(48)));

            JPanel content = new JPanel();
            content.setOpaque(false);
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

            CheckBadge badge = new CheckBadge();
            badge.setAlignmentX(Component.CENTER_ALIGNMENT);
            content.add(badge);
            content.add(Box.createVerticalStrut(s(30)));

            JLabel title = label("ĐẶT SÂN THÀNH CÔNG!", bold(44f), TEXT_DARK);
            title.setAlignmentX(Component.CENTER_ALIGNMENT);
            title.setHorizontalAlignment(SwingConstants.CENTER);
            content.add(title);
            content.add(Box.createVerticalStrut(s(18)));

            JLabel desc = label("<html><div style='text-align:center'>Cảm ơn bạn đã tin tưởng dịch vụ của RentSta.<br>"
                    + "Thông tin đặt chỗ đã được gửi về email của bạn.</div></html>", regular(18f), TEXT_OLIVE);
            desc.setAlignmentX(Component.CENTER_ALIGNMENT);
            content.add(desc);
            content.add(Box.createVerticalStrut(s(34)));

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, s(16), 0));
            actions.setOpaque(false);
            JButton home = roundedAction("Trở về trang chủ", GREEN, GREEN_DARK);
            home.addActionListener(e -> {
                dispose();
                if (onHome != null) {
                    onHome.run();
                }
            });
            JButton history = roundedAction("Xem lịch sử đặt chỗ", SOFT_GRAY, TEXT_DARK);
            history.addActionListener(e -> {
                dispose();
                if (onHistory != null) {
                    onHistory.run();
                }
            });
            actions.add(home);
            actions.add(history);
            content.add(actions);

            root.add(content);
            return root;
        }
    }

    private static JButton roundedAction(String text, Color background, Color foreground) {
        JButton button = pillButton(text, background, foreground);
        button.setFont(bold(16f));
        button.setBorder(new EmptyBorder(s(12), s(34), s(12), s(34)));
        return button;
    }

    private static final class QrPanel extends JPanel {
        private QrPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(s(173), s(164)));
            setMinimumSize(new Dimension(s(173), s(164)));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int outer = Math.min(getWidth(), getHeight()) - s(8);
            int x = (getWidth() - outer) / 2;
            int y = (getHeight() - outer) / 2;
            g2.setColor(new Color(37, 48, 48));
            g2.fillRoundRect(x, y, outer, outer, s(8), s(8));
            int pad = s(18);
            g2.setColor(Color.WHITE);
            g2.fillRect(x + pad, y + pad, outer - pad * 2, outer - pad * 2);

            int qrX = x + pad + s(17);
            int qrY = y + pad + s(30);
            int cell = Math.max(2, s(4));
            g2.setColor(new Color(37, 48, 48));
            drawFinder(g2, qrX, qrY, cell);
            drawFinder(g2, qrX + cell * 18, qrY, cell);
            drawFinder(g2, qrX, qrY + cell * 18, cell);
            for (int row = 0; row < 27; row++) {
                for (int col = 0; col < 27; col++) {
                    boolean finderArea = (row < 7 && col < 7)
                            || (row < 7 && col > 17)
                            || (row > 17 && col < 7);
                    if (!finderArea && ((row * 13 + col * 7 + row * col) % 5 == 0)) {
                        g2.fillRect(qrX + col * cell, qrY + row * cell, cell, cell);
                    }
                }
            }
            g2.setFont(regular(8f));
            g2.drawString("safe wonk", qrX + s(22), y + pad + s(18));
            g2.drawString("Safe work", qrX + s(20), y + outer - pad - s(4));
            g2.dispose();
        }

        private void drawFinder(Graphics2D g2, int x, int y, int cell) {
            g2.fillRect(x, y, cell * 7, cell * 7);
            g2.setColor(Color.WHITE);
            g2.fillRect(x + cell, y + cell, cell * 5, cell * 5);
            g2.setColor(new Color(37, 48, 48));
            g2.fillRect(x + cell * 2, y + cell * 2, cell * 3, cell * 3);
        }
    }

    private static final class CheckBadge extends JComponent {
        private CheckBadge() {
            setPreferredSize(new Dimension(s(96), s(96)));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int size = Math.min(getWidth(), getHeight()) - s(8);
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;
            g2.setColor(new Color(57, 255, 20, 32));
            g2.fillOval(x - s(8), y - s(8), size + s(16), size + s(16));
            g2.setColor(GREEN);
            g2.fillOval(x, y, size, size);
            g2.setStroke(new BasicStroke(s(4), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(GREEN_DARK);
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            g2.drawLine(cx - s(18), cy, cx - s(6), cy + s(12));
            g2.drawLine(cx - s(6), cy + s(12), cx + s(20), cy - s(16));
            g2.dispose();
        }
    }

    private static final class SuccessPanel extends JPanel {
        private SuccessPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(PAGE_BG);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(new Color(57, 255, 20, 28));
            g2.fillOval(getWidth() - s(170), -s(120), s(260), s(260));
            g2.setColor(new Color(16, 110, 0, 14));
            g2.fillOval(-s(120), getHeight() - s(160), s(260), s(220));
            g2.dispose();
        }
    }
}
