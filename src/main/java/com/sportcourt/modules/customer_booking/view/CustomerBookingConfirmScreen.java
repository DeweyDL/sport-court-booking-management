package com.sportcourt.modules.customer_booking.view;

import com.sportcourt.common.style.AppFonts;
import com.sportcourt.modules.customer_booking.dto.BranchOption;
import com.sportcourt.modules.customer_booking.dto.CourtSearchResult;
import com.sportcourt.modules.customer_booking.dto.SlotStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

import static com.sportcourt.modules.customer_booking.view.CustomerBookingViewStyle.*;

public class CustomerBookingConfirmScreen extends JPanel {
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0");
    private static final BigDecimal DEPOSIT_RATE = new BigDecimal("0.70");

    private final Runnable onBack;
    private final Runnable onSubmit;
    private final JPanel content = new JPanel(new GridBagLayout());

    private BranchOption branch;
    private CourtSearchResult court;
    private List<SlotStatus> slots = List.of();

    public CustomerBookingConfirmScreen(Runnable onBack, Runnable onSubmit) {
        this.onBack = onBack;
        this.onSubmit = onSubmit;
        AppFonts.register();
        setLayout(new BorderLayout());
        setBackground(PAGE_BG);
        setBorder(new EmptyBorder(s(24), s(24), s(40), s(24)));
        content.setOpaque(false);
        add(content, BorderLayout.NORTH);
        renderDraft();
    }

    public void showDraft(BranchOption branch, CourtSearchResult court, List<SlotStatus> slots) {
        this.branch = branch;
        this.court = court;
        this.slots = slots == null ? List.of() : List.copyOf(slots);
        renderDraft();
    }

    private void renderDraft() {
        content.removeAll();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;

        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, s(16), 0);
        content.add(buildTopLine(), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, s(28), 0);
        content.add(new HeroPanel("Xác nhận đặt sân"), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 0, 0);
        content.add(buildCardsRow(), gbc);

        content.revalidate();
        content.repaint();
    }

    private JComponent buildTopLine() {
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JButton back = new JButton("< Quay lại");
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
        gbc.insets = new Insets(0, 0, 0, s(22));
        row.add(buildBookingInfoCard(), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.38;
        gbc.insets = new Insets(0, 0, 0, 0);
        row.add(buildPaymentSummaryCard(), gbc);
        return row;
    }

    private JComponent buildBookingInfoCard() {
        RoundedPanel card = new RoundedPanel(s(24), PANEL_BG, false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(s(24), s(28), s(28), s(28)));

        JLabel title = label("THÔNG TIN ĐẶT SÂN", bold(20f), TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(s(18)));

        card.add(infoRow("Chi nhánh", branch == null ? "--" : branch.branchName()));
        card.add(Box.createVerticalStrut(s(12)));
        card.add(infoRow("Địa chỉ", branch == null ? "--" : branch.address()));
        card.add(Box.createVerticalStrut(s(12)));
        card.add(infoRow("Mã sân", selectedCourtText()));
        card.add(Box.createVerticalStrut(s(12)));
        card.add(infoRow("Loại thể thao", court == null ? "--" : court.sportTypeName()));
        card.add(Box.createVerticalStrut(s(16)));
        card.add(line());
        card.add(Box.createVerticalStrut(s(14)));

        if (slots.isEmpty()) {
            card.add(infoRow("Khung giờ", "--"));
        } else {
            for (SlotStatus slot : slots) {
                card.add(slotLine(slot));
                card.add(Box.createVerticalStrut(s(10)));
            }
        }
        card.add(Box.createVerticalStrut(s(8)));
        card.add(line());
        card.add(Box.createVerticalStrut(s(14)));
        card.add(totalRow("Tổng tiền thuê sân", money(totalPrice()), TEXT_DARK));
        return card;
    }

    private JComponent buildPaymentSummaryCard() {
        RoundedPanel card = new RoundedPanel(s(24), SOFT_GRAY, false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(s(24), s(28), s(28), s(28)));

        JLabel title = label("CHI TIẾT THANH TOÁN", bold(20f), TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(s(22)));
        card.add(infoRow("Tổng tiền thuê sân", money(totalPrice())));
        card.add(Box.createVerticalStrut(s(14)));
        card.add(infoRow("Tiền cọc (70%)", money(deposit())));
        card.add(Box.createVerticalStrut(s(22)));
        card.add(line());
        card.add(Box.createVerticalStrut(s(18)));
        card.add(totalRow("TỔNG CỌC PHẢI TRẢ", money(deposit()), GREEN_DARK));
        card.add(Box.createVerticalStrut(s(26)));

        JButton submit = pillButton("Xác nhận và đặt cọc", GREEN, GREEN_DARK);
        submit.setFont(bold(24f));
        submit.setAlignmentX(Component.LEFT_ALIGNMENT);
        submit.setMaximumSize(new Dimension(Integer.MAX_VALUE, s(58)));
        submit.addActionListener(e -> showDepositDialog());
        card.add(submit);
        return card;
    }

    private String selectedCourtText() {
        if (slots.isEmpty()) {
            return court == null ? "--" : court.courtId();
        }

        java.util.LinkedHashSet<String> courtIds = new java.util.LinkedHashSet<>();
        for (SlotStatus slot : slots) {
            if (slot.courtId() != null && !slot.courtId().isBlank()) {
                courtIds.add(slot.courtId());
            }
        }
        return courtIds.isEmpty() ? "--" : String.join(", ", courtIds);
    }

    private JComponent infoRow(String leftText, String rightText) {
        JPanel row = new JPanel(new BorderLayout(s(16), 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, s(32)));
        row.add(label(leftText, regular(15f), TEXT_OLIVE), BorderLayout.WEST);
        JLabel value = label(rightText == null ? "--" : rightText, bold(15f), TEXT_DARK);
        value.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(value, BorderLayout.EAST);
        return row;
    }

    private JComponent totalRow(String leftText, String rightText, Color valueColor) {
        JPanel row = new JPanel(new BorderLayout(s(16), 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, s(42)));
        row.add(label(leftText, bold(16f), TEXT_DARK), BorderLayout.WEST);
        JLabel value = label(rightText, bold(24f), valueColor);
        value.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(value, BorderLayout.EAST);
        return row;
    }

    private JComponent slotLine(SlotStatus slot) {
        RoundedPanel row = new RoundedPanel(s(12), SURFACE_BG);
        row.setLayout(new GridBagLayout());
        row.setBorder(new EmptyBorder(s(8), s(12), s(8), s(12)));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, s(46)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        gbc.gridx = 0;
        gbc.weightx = 0.26;
        row.add(label(slot.courtId(), regular(14f), TEXT_OLIVE), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.48;
        row.add(label(slot.bookingDate() + " | " + formatHour(slot.startHour()) + " - " + formatHour(slot.endHour()),
                bold(14f), GREEN_DARK), gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.26;
        JLabel priceLabel = label(money(slot.price()), bold(14f), GREEN_DARK);
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

    private BigDecimal totalPrice() {
        BigDecimal total = BigDecimal.ZERO;
        for (SlotStatus slot : slots) {
            if (slot.price() != null) {
                total = total.add(slot.price());
            }
        }
        return total;
    }

    private BigDecimal deposit() {
        return depositOf(totalPrice());
    }

    private BigDecimal depositOf(BigDecimal amount) {
        return (amount == null ? BigDecimal.ZERO : amount)
                .multiply(DEPOSIT_RATE)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private String formatHour(int hour) {
        return String.format("%02d:00", hour);
    }

    private String money(BigDecimal value) {
        return MONEY_FORMAT.format(value == null ? BigDecimal.ZERO : value) + " VND";
    }

    private void showDepositDialog() {
        onSubmit.run();
    }

    private final class DepositDialog extends JDialog {
        private boolean confirmed = false;

        DepositDialog(Window owner) {
            super(owner, "Xác nhận đặt cọc", ModalityType.APPLICATION_MODAL);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setContentPane(buildContent());
            pack();
            setMinimumSize(new Dimension(s(480), s(360)));
            setLocationRelativeTo(owner);
        }

        boolean isConfirmed() {
            return confirmed;
        }

        private JComponent buildContent() {
            JPanel root = new JPanel(new BorderLayout(0, s(20)));
            root.setBackground(PAGE_BG);
            root.setBorder(new EmptyBorder(s(28), s(32), s(24), s(32)));

            JLabel title = label("CHI TIẾT ĐẶT CỌC", bold(20f), TEXT_DARK);
            root.add(title, BorderLayout.NORTH);

            JPanel body = new JPanel();
            body.setOpaque(false);
            body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

            for (SlotStatus slot : slots) {
                body.add(depositSlotRow(slot));
                body.add(Box.createVerticalStrut(s(10)));
            }
            body.add(Box.createVerticalStrut(s(6)));
            body.add(dividerLine());
            body.add(Box.createVerticalStrut(s(14)));
            body.add(depositTotalRow("TỔNG CỌC PHẢI TRẢ", money(deposit()), GREEN_DARK));

            root.add(body, BorderLayout.CENTER);

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, s(12), 0));
            actions.setOpaque(false);
            JButton cancel = pillButton("Hủy", Color.WHITE, TEXT_DARK);
            cancel.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(BORDER, s(999)),
                    new EmptyBorder(s(8), s(22), s(8), s(22))
            ));
            cancel.addActionListener(e -> dispose());
            JButton confirm = pillButton("Xác nhận đặt cọc", GREEN, GREEN_DARK);
            confirm.addActionListener(e -> {
                confirmed = true;
                dispose();
            });
            actions.add(cancel);
            actions.add(confirm);
            root.add(actions, BorderLayout.SOUTH);

            return root;
        }

        private JComponent depositSlotRow(SlotStatus slot) {
            RoundedPanel row = new RoundedPanel(s(12), SURFACE_BG);
            row.setLayout(new BorderLayout(s(16), 0));
            row.setBorder(new EmptyBorder(s(10), s(14), s(10), s(14)));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, s(56)));

            JPanel left = new JPanel();
            left.setOpaque(false);
            left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
            left.add(label("Tiền cọc", regular(13f), TEXT_OLIVE));
            left.add(label(formatHour(slot.startHour()) + " - " + formatHour(slot.endHour())
                    + "  |  " + slot.bookingDate(), bold(13f), TEXT_DARK));
            row.add(left, BorderLayout.WEST);

            BigDecimal slotDeposit = depositOf(slot.price());
            JLabel amount = label(money(slotDeposit), bold(15f), GREEN_DARK);
            amount.setHorizontalAlignment(SwingConstants.RIGHT);
            row.add(amount, BorderLayout.EAST);

            return row;
        }

        private JComponent dividerLine() {
            JPanel line = new JPanel();
            line.setOpaque(true);
            line.setBackground(new Color(113, 113, 122, 130));
            line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            line.setPreferredSize(new Dimension(0, 1));
            line.setAlignmentX(Component.LEFT_ALIGNMENT);
            return line;
        }

        private JComponent depositTotalRow(String leftText, String rightText, Color valueColor) {
            JPanel row = new JPanel(new BorderLayout(s(16), 0));
            row.setOpaque(false);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, s(42)));
            row.add(label(leftText, bold(16f), TEXT_DARK), BorderLayout.WEST);
            JLabel value = label(rightText, bold(24f), valueColor);
            value.setHorizontalAlignment(SwingConstants.RIGHT);
            row.add(value, BorderLayout.EAST);
            return row;
        }
    }
}
