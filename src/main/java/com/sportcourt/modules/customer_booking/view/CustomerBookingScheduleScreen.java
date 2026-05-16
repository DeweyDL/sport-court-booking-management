package com.sportcourt.modules.customer_booking.view;

import com.sportcourt.common.style.AppFonts;
import com.sportcourt.modules.customer_booking.controller.CustomerBookingController;
import com.sportcourt.modules.customer_booking.dto.BookingSlotStatus;
import com.sportcourt.modules.customer_booking.dto.BranchOption;
import com.sportcourt.modules.customer_booking.dto.CourtSearchResult;
import com.sportcourt.modules.customer_booking.dto.SlotStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import static com.sportcourt.modules.customer_booking.view.CustomerBookingViewStyle.*;

public class CustomerBookingScheduleScreen extends JPanel {
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0");

    private final CustomerBookingController controller;
    private final Runnable onBack;
    private final Consumer<List<SlotStatus>> onConfirm;

    private final JLabel titleLabel = label("DAT SAN", bold(28f), Color.WHITE);
    private final JLabel courtInfoLabel = label("--", bold(13f), TEXT_DARK);
    private final JLabel selectedSlotLabel = label("Chua chon khung gio", bold(14f), GREEN_DARK);
    private final JLabel totalLabel = label("Tong tien: 0 VND", bold(14f), GREEN_DARK);
    private final JSpinner dateSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH));

    private JPanel slotGrid;
    private BranchOption currentBranch;
    private CourtSearchResult currentCourt;
    private LocalDate bookingDate = LocalDate.now();
    private List<SlotStatus> currentSlots = List.of();
    private SlotStatus selectedSlot;
    private boolean adjustingDate;

    public CustomerBookingScheduleScreen(CustomerBookingController controller,
                                         Runnable onBack,
                                         Consumer<List<SlotStatus>> onConfirm) {
        this.controller = controller;
        this.onBack = onBack;
        this.onConfirm = onConfirm;
        AppFonts.register();
        setLayout(new BorderLayout());
        setBackground(PAGE_BG);
        setPreferredSize(new Dimension(s(1240), s(820)));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildMain(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    public void showCourt(BranchOption branch, CourtSearchResult court) {
        currentBranch = branch;
        currentCourt = court;
        selectedSlot = null;
        bookingDate = LocalDate.now();
        adjustingDate = true;
        dateSpinner.setValue(Date.from(bookingDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        adjustingDate = false;
        titleLabel.setText("DAT SAN - " + court.courtId());
        courtInfoLabel.setText(branch.branchName() + " | " + court.sportTypeName() + " | " + court.branchAddress());
        updateFooter();
        loadSlots();
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(GREEN_DARK);
        header.setBorder(new EmptyBorder(s(16), s(24), s(16), s(24)));
        header.add(titleLabel, BorderLayout.WEST);
        return header;
    }

    private JComponent buildMain() {
        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);
        main.add(buildFilterStrip(), BorderLayout.NORTH);
        main.add(buildGridScroll(), BorderLayout.CENTER);
        return main;
    }

    private JComponent buildFilterStrip() {
        JPanel strip = new JPanel(new BorderLayout(s(24), 0));
        strip.setBackground(new Color(243, 243, 246));
        strip.setBorder(new EmptyBorder(s(18), s(24), s(18), s(24)));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(label("THONG TIN SAN", bold(12f), TEXT_OLIVE));
        left.add(Box.createVerticalStrut(s(8)));
        left.add(courtInfoLabel);
        strip.add(left, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, s(12), 0));
        right.setOpaque(false);
        right.add(buildLegend());
        right.add(label("Ngay dat", bold(12f), TEXT_OLIVE));
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy"));
        dateSpinner.setPreferredSize(new Dimension(s(140), s(36)));
        dateSpinner.addChangeListener(e -> {
            if (!adjustingDate) {
                bookingDate = selectedDate();
                selectedSlot = null;
                loadSlots();
            }
        });
        right.add(dateSpinner);
        JButton reload = pillButton("Tai lai", Color.WHITE, GREEN_DARK);
        reload.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(GREEN_DARK, s(999)),
                new EmptyBorder(s(7), s(16), s(7), s(16))
        ));
        reload.addActionListener(e -> loadSlots());
        right.add(reload);
        strip.add(right, BorderLayout.EAST);
        return strip;
    }

    private JComponent buildLegend() {
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, s(10), 0));
        legend.setBackground(new Color(232, 232, 234, 128));
        legend.setBorder(new EmptyBorder(s(8), s(12), s(8), s(12)));
        legend.add(legendDot(Color.WHITE, true));
        legend.add(label("Trong", regular(12f), TEXT_DARK));
        legend.add(Box.createHorizontalStrut(s(4)));
        legend.add(legendDot(new Color(59, 130, 246), false));
        legend.add(label("Dang chon", regular(12f), TEXT_DARK));
        legend.add(Box.createHorizontalStrut(s(4)));
        legend.add(legendDot(new Color(148, 163, 184), false));
        legend.add(label("Da het", regular(12f), TEXT_DARK));
        return legend;
    }

    private JLabel legendDot(Color fill, boolean bordered) {
        JLabel dot = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(fill);
                g2.fillOval(0, 0, getWidth(), getHeight());
                if (bordered) {
                    g2.setColor(new Color(186, 204, 176, 128));
                    g2.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
                }
                g2.dispose();
            }
        };
        dot.setPreferredSize(new Dimension(s(12), s(12)));
        dot.setOpaque(false);
        return dot;
    }

    private JComponent buildGridScroll() {
        slotGrid = new JPanel(new GridLayout(0, 4, s(16), s(16)));
        slotGrid.setOpaque(false);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(s(18), s(18), s(18), s(18)));
        wrapper.add(slotGrid, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(PAGE_BG);
        scroll.setBackground(PAGE_BG);
        scroll.getVerticalScrollBar().setUnitIncrement(s(20));
        return scroll;
    }

    private JComponent buildFooter() {
        JPanel footer = new JPanel(new BorderLayout(s(32), 0));
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(s(18), s(24), s(18), s(24)));

        JPanel summary = new JPanel(new FlowLayout(FlowLayout.LEFT, s(36), 0));
        summary.setOpaque(false);
        summary.add(selectedSlotLabel);
        summary.add(totalLabel);
        footer.add(summary, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, s(18), 0));
        actions.setOpaque(false);
        JButton reset = pillButton("Chọn lại", Color.WHITE, GREEN_DARK);
        reset.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(GREEN_DARK, s(999)),
                new EmptyBorder(s(8), s(28), s(8), s(28))
        ));
        reset.addActionListener(e -> onBack.run());

        JButton book = pillButton("Đặt lịch", GREEN, GREEN_DARK);
        book.addActionListener(e -> confirmSelection());
        actions.add(reset);
        actions.add(book);
        footer.add(actions, BorderLayout.EAST);
        return footer;
    }

    private void loadSlots() {
        if (currentCourt == null) {
            renderEmpty("Chua chon san.");
            return;
        }

        try {
            currentSlots = controller.loadSlots(currentCourt.courtId(), bookingDate);
            renderSlots();
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, errorMessage(e), "Dat san", JOptionPane.ERROR_MESSAGE);
            renderEmpty("Khong the tai lich san.");
        }
    }

    private void renderSlots() {
        slotGrid.removeAll();
        if (currentSlots.isEmpty()) {
            renderEmpty("San nay chua co bang gia hoac khung gio.");
            return;
        }

        slotGrid.setLayout(new GridLayout(0, 4, s(16), s(16)));
        for (SlotStatus slot : currentSlots) {
            slotGrid.add(slotButton(slot));
        }
        slotGrid.revalidate();
        slotGrid.repaint();
    }

    private void renderEmpty(String message) {
        slotGrid.removeAll();
        slotGrid.setLayout(new BorderLayout());
        RoundedPanel empty = new RoundedPanel(s(18), SURFACE_BG);
        empty.setLayout(new BorderLayout());
        empty.setBorder(new EmptyBorder(s(34), s(24), s(34), s(24)));
        JLabel label = label(message, bold(15f), TEXT_MUTED);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        empty.add(label, BorderLayout.CENTER);
        slotGrid.add(empty, BorderLayout.NORTH);
        slotGrid.revalidate();
        slotGrid.repaint();
    }

    private JButton slotButton(SlotStatus slot) {
        boolean available = slot.status() == BookingSlotStatus.AVAILABLE;
        boolean selected = selectedSlot != null
                && selectedSlot.priceBoardId().equals(slot.priceBoardId())
                && selectedSlot.bookingDate().equals(slot.bookingDate());

        Color bg = selected ? new Color(59, 130, 246)
                : slot.status() == BookingSlotStatus.AVAILABLE ? Color.WHITE
                : slot.status() == BookingSlotStatus.BOOKED ? new Color(148, 163, 184)
                : new Color(254, 226, 226);
        Color fg = selected || slot.status() == BookingSlotStatus.BOOKED ? Color.WHITE : TEXT_DARK;

        JButton button = new JButton("<html><center>"
                + formatHour(slot.startHour()) + " - " + formatHour(slot.endHour())
                + "<br>" + money(slot.price())
                + "</center></html>") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), s(14), s(14));
                g2.setColor(selected ? new Color(59, 130, 246, 80) : BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, s(14), s(14));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(bold(13f));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(s(200), s(90)));
        button.setCursor(available ? new Cursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
        button.setEnabled(available || selected);
        button.setForeground(fg);
        if (available) {
            button.addActionListener(e -> {
                selectedSlot = slot;
                updateFooter();
                renderSlots();
            });
        }
        return button;
    }

    private void confirmSelection() {
        if (selectedSlot == null) {
            JOptionPane.showMessageDialog(this, "Vui long chon mot khung gio.", "Dat san",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        onConfirm.accept(List.of(selectedSlot));
    }

    private void updateFooter() {
        if (selectedSlot == null) {
            selectedSlotLabel.setText("Chua chon khung gio");
            totalLabel.setText("Tong tien: 0 VND");
            return;
        }

        selectedSlotLabel.setText(formatHour(selectedSlot.startHour())
                + " - " + formatHour(selectedSlot.endHour())
                + " | " + selectedSlot.bookingDate());
        totalLabel.setText("Tong tien: " + money(selectedSlot.price()));
    }

    private LocalDate selectedDate() {
        Date date = (Date) dateSpinner.getValue();
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private String formatHour(int hour) {
        return String.format("%02d:00", hour);
    }

    private String money(BigDecimal value) {
        return MONEY_FORMAT.format(value == null ? BigDecimal.ZERO : value) + " VND";
    }

    private String errorMessage(RuntimeException e) {
        return e.getMessage() == null || e.getMessage().isBlank() ? "Co loi xay ra." : e.getMessage();
    }
}
