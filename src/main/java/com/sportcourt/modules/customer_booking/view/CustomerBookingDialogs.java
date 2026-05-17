package com.sportcourt.modules.customer_booking.view;

import com.sportcourt.modules.payment.dto.PaymentQrInfo;
import com.sportcourt.modules.payment.service.PaymentService;
import com.sportcourt.modules.payment.service.PaymentServiceImpl;
import com.sportcourt.modules.payment.util.QrCodeRenderer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

import static com.sportcourt.modules.customer_booking.view.CustomerBookingViewStyle.*;

final class CustomerBookingDialogs {
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0");

    private CustomerBookingDialogs() {
    }

    static boolean showDepositPaymentDialog(Component parent, BigDecimal deposit) {
        PaymentService service = new PaymentServiceImpl();

        // Tạo link PayOS theo số tiền cọc (booking chưa tạo nên chưa có hóa đơn)
        PaymentQrInfo qr;
        try {
            int amount = deposit == null ? 0 : deposit.intValue();
            qr = service.createPaymentLink(amount, "Coc dat san");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent,
                    "Không tạo được mã thanh toán: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        Window owner = SwingUtilities.getWindowAncestor(parent);
        PaymentDialog dialog = new PaymentDialog(owner, deposit, qr, service);
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

    /** Mã BIN PayOS -> tên ngân hàng (một số ngân hàng phổ biến). */
    private static String bankName(String bin) {
        if (bin == null) {
            return "";
        }
        return switch (bin) {
            case "970436" -> "Vietcombank";
            case "970415" -> "VietinBank";
            case "970418" -> "BIDV";
            case "970405" -> "Agribank";
            case "970422" -> "MB Bank";
            case "970407" -> "Techcombank";
            case "970416" -> "ACB";
            case "970432" -> "VPBank";
            case "970423" -> "TPBank";
            case "970403" -> "Sacombank";
            default -> bin;
        };
    }

    private static final class PaymentDialog extends JDialog {
        private final PaymentQrInfo qr;
        private final PaymentService service;
        private boolean confirmed;
        private SwingWorker<Void, String> poller;

        private PaymentDialog(Window owner, BigDecimal deposit, PaymentQrInfo qr, PaymentService service) {
            super(owner, "Thanh toán đặt cọc", ModalityType.APPLICATION_MODAL);
            this.qr = qr;
            this.service = service;
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setContentPane(buildContent(deposit));
            pack();
            setMinimumSize(new Dimension(s(680), s(520)));
            setLocationRelativeTo(owner);
            startPolling();
            addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    stopPolling();
                }
            });
        }

        private boolean confirmed() {
            return confirmed;
        }

        // ===== POLLING: tự động đóng khi PayOS báo đã thanh toán =====
        private void startPolling() {
            poller = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    while (!isCancelled()) {
                        String st = service.checkStatus(qr.orderCode());
                        publish(st);
                        if ("ĐÃ THANH TOÁN".equals(st) || "ĐÃ HUỶ".equals(st) || "HẾT HẠN".equals(st)) {
                            break;
                        }
                        Thread.sleep(4000);   // 4 giây/lần
                    }
                    return null;
                }

                @Override
                protected void process(List<String> chunks) {
                    String st = chunks.get(chunks.size() - 1);
                    if ("ĐÃ THANH TOÁN".equals(st)) {
                        confirmed = true;
                        dispose();
                    } else if ("HẾT HẠN".equals(st)) {
                        JOptionPane.showMessageDialog(PaymentDialog.this,
                                "Mã thanh toán đã hết hạn.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                        dispose();
                    }
                }
            };
            poller.execute();
        }

        private void stopPolling() {
            if (poller != null) {
                poller.cancel(true);
            }
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
            panel.add(qrView(), gbc);

            gbc.gridx = 1;
            gbc.weightx = 0.58;
            gbc.insets = new Insets(0, 0, 0, 0);
            panel.add(bankInfo(), gbc);
            return panel;
        }

        // QR THẬT từ PayOS (vẽ chuỗi VietQR bằng ZXing)
        private JComponent qrView() {
            int size = s(173);
            JLabel qrLabel = new JLabel(QrCodeRenderer.toIcon(qr.qrCodeData(), size));
            qrLabel.setHorizontalAlignment(SwingConstants.CENTER);
            qrLabel.setPreferredSize(new Dimension(size, size));
            qrLabel.setMinimumSize(new Dimension(size, size));
            return qrLabel;
        }

        private JComponent bankInfo() {
            JPanel info = new JPanel(new GridLayout(3, 2, s(18), s(18)));
            info.setOpaque(false);
            info.add(label("Ngân hàng", regular(20f), TEXT_OLIVE));
            info.add(rightLabel(bankName(qr.bin()), bold(20f), TEXT_DARK));
            info.add(label("Số tài khoản", regular(20f), TEXT_OLIVE));
            info.add(rightLabel(qr.accountNumber(), bold(20f), TEXT_DARK));
            info.add(label("Chủ tài khoản", regular(20f), TEXT_OLIVE));
            info.add(rightLabel(qr.accountName(), bold(20f), TEXT_DARK));
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
            JLabel text = label("<html><div style='text-align:center'>Quét mã QR để chuyển khoản.<br>"
                            + "Hệ thống tự xác nhận khi nhận được tiền.</div></html>",
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
            cancel.addActionListener(e -> {
                stopPolling();
                service.cancel(qr.orderCode());   // hủy link bên PayOS
                dispose();
            });

            JButton paid = roundedAction("Tôi đã chuyển khoản", GREEN, GREEN_DARK);
            paid.addActionListener(e -> {
                // Kiểm tra ngay với PayOS, không tin tưởng thao tác người dùng
                String st = service.checkStatus(qr.orderCode());
                if ("ĐÃ THANH TOÁN".equals(st)) {
                    confirmed = true;
                    stopPolling();
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Chưa nhận được thanh toán. Vui lòng đợi vài giây sau khi chuyển khoản rồi thử lại.",
                            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                }
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
