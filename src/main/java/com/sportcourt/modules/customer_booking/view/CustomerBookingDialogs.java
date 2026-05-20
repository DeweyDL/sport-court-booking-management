package com.sportcourt.modules.customer_booking.view;

import com.sportcourt.modules.payment.dto.PaymentQrInfo;
import com.sportcourt.modules.payment.service.PaymentService;
import com.sportcourt.modules.payment.service.PaymentServiceImpl;
import com.sportcourt.modules.payment.util.QrCodeRenderer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.sportcourt.modules.customer_booking.view.CustomerBookingViewStyle.*;

public final class CustomerBookingDialogs {
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0");
    private static final int DEPOSIT_HOLD_SECONDS = 300;
    private static final Map<String, DepositPaymentSession> ACTIVE_DEPOSIT_SESSIONS = new ConcurrentHashMap<>();

    private CustomerBookingDialogs() {
    }

    public static void showDepositPaymentDialog(Component parent, String invoiceId, BigDecimal deposit,
                                                Runnable onPaymentConfirmed, Runnable onCancelInvoice) {
        String sessionKey = invoiceId == null || invoiceId.isBlank()
                ? "TRANSIENT-" + System.nanoTime()
                : invoiceId.trim();

        DepositPaymentSession session = ACTIVE_DEPOSIT_SESSIONS.get(sessionKey);
        if (session == null || session.isTerminal()) {
            PaymentService service = new PaymentServiceImpl();
            PaymentQrInfo qr;
            try {
                int amount = deposit == null ? 0 : deposit.intValue();
                String description = invoiceId == null || invoiceId.isBlank()
                        ? "Coc dat san"
                        : "Coc " + invoiceId.trim();
                qr = service.createPaymentLink(amount, description);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(parent,
                        "Không tạo được mã thanh toán: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                runQuietly(onCancelInvoice);
                return;
            }

            session = new DepositPaymentSession(sessionKey, deposit, qr, service, onCancelInvoice);
            ACTIVE_DEPOSIT_SESSIONS.put(sessionKey, session);
            session.start();
        }

        session.setOnPaymentConfirmed(onPaymentConfirmed);

        Window owner = SwingUtilities.getWindowAncestor(parent);
        PaymentDialog dialog = new PaymentDialog(owner, session);
        session.attach(dialog);
        dialog.setVisible(true);
    }

    public static void showDepositPaymentDialog(Component parent, BigDecimal deposit,
                                                Runnable onPaymentConfirmed, Runnable onCancelInvoice) {
        showDepositPaymentDialog(parent, null, deposit, onPaymentConfirmed, onCancelInvoice);
    }

    static void showSuccessDialog(Component parent, Runnable onHome, Runnable onHistory) {
        Window owner = SwingUtilities.getWindowAncestor(parent);
        SuccessDialog dialog = new SuccessDialog(owner, onHome, onHistory);
        dialog.setVisible(true);
    }

    private static String money(BigDecimal value) {
        return MONEY_FORMAT.format(value == null ? BigDecimal.ZERO : value) + "đ";
    }

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

    private static void runQuietly(Runnable action) {
        if (action == null) {
            return;
        }
        try {
            action.run();
        } catch (Exception ignored) {
        }
    }

    private static String normalizedStatus(String status) {
        if (status == null) {
            return "";
        }
        return Normalizer.normalize(status, Normalizer.Form.NFD)
                .replace('Đ', 'D')
                .replace('đ', 'd')
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT);
    }

    private static boolean isPaidStatus(String status) {
        String normalized = normalizedStatus(status);
        return normalized.equals("PAID") || normalized.equals("DA THANH TOAN");
    }

    private static boolean isExpiredStatus(String status) {
        String normalized = normalizedStatus(status);
        return normalized.equals("EXPIRED") || normalized.equals("HET HAN");
    }

    private static boolean isCancelledStatus(String status) {
        String normalized = normalizedStatus(status);
        return normalized.equals("CANCELLED")
                || normalized.equals("CANCELED")
                || normalized.equals("DA HUY");
    }

    private static String formatCountdown(int remainingSeconds) {
        int min = Math.max(0, remainingSeconds) / 60;
        int sec = Math.max(0, remainingSeconds) % 60;
        return String.format("%d:%02d", min, sec);
    }

    private static final class DepositPaymentSession {
        private final String key;
        private final BigDecimal deposit;
        private final PaymentQrInfo qr;
        private final PaymentService service;
        private final Runnable onCancelInvoice;
        private final List<PaymentDialog> dialogs = new CopyOnWriteArrayList<>();

        private Runnable onPaymentConfirmed;
        private SwingWorker<Void, String> poller;
        private Timer countdownTimer;
        private int remainingSeconds = DEPOSIT_HOLD_SECONDS;
        private boolean terminal;

        private DepositPaymentSession(String key, BigDecimal deposit, PaymentQrInfo qr,
                                      PaymentService service, Runnable onCancelInvoice) {
            this.key = key;
            this.deposit = deposit;
            this.qr = qr;
            this.service = service;
            this.onCancelInvoice = onCancelInvoice;
        }

        private synchronized boolean isTerminal() {
            return terminal;
        }

        private synchronized int remainingSeconds() {
            return remainingSeconds;
        }

        private BigDecimal deposit() {
            return deposit;
        }

        private PaymentQrInfo qr() {
            return qr;
        }

        private synchronized void setOnPaymentConfirmed(Runnable onPaymentConfirmed) {
            this.onPaymentConfirmed = onPaymentConfirmed;
        }

        private void start() {
            countdownTimer = new Timer(1000, e -> {
                boolean shouldExpire;
                synchronized (this) {
                    if (terminal) {
                        return;
                    }
                    remainingSeconds = Math.max(0, remainingSeconds - 1);
                    shouldExpire = remainingSeconds == 0;
                }
                updateDialogs();
                if (shouldExpire) {
                    expire();
                }
            });
            countdownTimer.start();
            startPolling();
        }

        private void attach(PaymentDialog dialog) {
            if (isTerminal()) {
                dialog.dispose();
                return;
            }
            for (PaymentDialog existing : dialogs) {
                if (existing.isDisplayable()) {
                    existing.toFront();
                    existing.requestFocus();
                    dialog.dispose();
                    return;
                }
            }
            dialogs.add(dialog);
            dialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    dialogs.remove(dialog);
                }
            });
            dialog.updateCountdown(remainingSeconds());
        }

        private void updateDialogs() {
            int remaining = remainingSeconds();
            for (PaymentDialog dialog : dialogs) {
                dialog.updateCountdown(remaining);
            }
        }

        private void startPolling() {
            poller = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    while (!isCancelled() && !isTerminal()) {
                        String status = service.checkStatus(qr.orderCode());
                        publish(status);
                        if (isPaidStatus(status) || isExpiredStatus(status) || isCancelledStatus(status)) {
                            break;
                        }
                        Thread.sleep(4000);
                    }
                    return null;
                }

                @Override
                protected void process(List<String> chunks) {
                    if (chunks == null || chunks.isEmpty() || isTerminal()) {
                        return;
                    }
                    String status = chunks.get(chunks.size() - 1);
                    if (isPaidStatus(status)) {
                        finishPaid();
                    } else if (isExpiredStatus(status) || isCancelledStatus(status)) {
                        expire();
                    }
                }
            };
            poller.execute();
        }

        private void finishPaid() {
            Runnable callback;
            synchronized (this) {
                if (terminal) {
                    return;
                }
                terminal = true;
                callback = onPaymentConfirmed;
                stopInternal();
            }
            ACTIVE_DEPOSIT_SESSIONS.remove(key, this);
            disposeDialogs();
            SwingUtilities.invokeLater(() -> runQuietly(callback));
        }

        private void expire() {
            synchronized (this) {
                if (terminal) {
                    return;
                }
                terminal = true;
                remainingSeconds = 0;
                stopInternal();
            }
            ACTIVE_DEPOSIT_SESSIONS.remove(key, this);
            updateDialogs();
            disposeDialogs();

            Thread worker = new Thread(() -> {
                try {
                    service.cancel(qr.orderCode());
                } finally {
                    runQuietly(onCancelInvoice);
                }
            }, "deposit-payment-expire");
            worker.setDaemon(true);
            worker.start();
        }

        private void stopInternal() {
            if (countdownTimer != null) {
                countdownTimer.stop();
            }
            if (poller != null) {
                poller.cancel(true);
            }
        }

        private void disposeDialogs() {
            for (PaymentDialog dialog : dialogs) {
                SwingUtilities.invokeLater(dialog::dispose);
            }
            dialogs.clear();
        }
    }

    private static final class PaymentDialog extends JDialog {
        private final DepositPaymentSession session;
        private JLabel countdownLabel;

        private PaymentDialog(Window owner, DepositPaymentSession session) {
            super(owner, "Thanh toán đặt cọc", ModalityType.MODELESS);
            this.session = session;
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setContentPane(buildContent());
            pack();
            setMinimumSize(new Dimension(s(680), s(520)));
            setLocationRelativeTo(owner);
        }

        private void updateCountdown(int remainingSeconds) {
            if (countdownLabel != null) {
                countdownLabel.setText("Sân sẽ được giữ chỗ trong vòng " + formatCountdown(remainingSeconds));
            }
        }

        private JComponent buildContent() {
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
            card.add(paymentRow("Tiền cọc", money(session.deposit()), regular(20f), bold(20f), TEXT_DARK));
            card.add(Box.createVerticalStrut(s(24)));
            card.add(separator());
            card.add(Box.createVerticalStrut(s(22)));
            card.add(paymentRow("TỔNG CỘNG", money(session.deposit()), bold(24f), bold(32f), GREEN_DARK));
            card.add(Box.createVerticalStrut(s(18)));
            card.add(separator());
            card.add(Box.createVerticalStrut(s(24)));
            card.add(bankTransferPanel());
            card.add(Box.createVerticalStrut(s(24)));
            card.add(holdNotice());

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

        private JComponent qrView() {
            int size = s(173);
            JLabel qrLabel = new JLabel(QrCodeRenderer.toIcon(session.qr().qrCodeData(), size));
            qrLabel.setHorizontalAlignment(SwingConstants.CENTER);
            qrLabel.setPreferredSize(new Dimension(size, size));
            qrLabel.setMinimumSize(new Dimension(size, size));
            return qrLabel;
        }

        private JComponent bankInfo() {
            PaymentQrInfo qr = session.qr();
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
            notice.setBorder(new EmptyBorder(s(12), s(16), s(12), s(16)));
            notice.setAlignmentX(Component.LEFT_ALIGNMENT);
            notice.setMaximumSize(new Dimension(Integer.MAX_VALUE, s(76)));

            countdownLabel = label("", bold(20f), GREEN_DARK);
            countdownLabel.setHorizontalAlignment(SwingConstants.CENTER);
            countdownLabel.setVerticalAlignment(SwingConstants.CENTER);
            notice.add(countdownLabel, BorderLayout.CENTER);
            updateCountdown(session.remainingSeconds());
            return notice;
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
