package com.sportcourt.modules.customer_booking.view;

import com.sportcourt.modules.customer_booking.controller.CustomerBookingController;
import com.sportcourt.modules.customer_booking.dto.BookingPreview;
import com.sportcourt.modules.customer_booking.dto.BranchOption;
import com.sportcourt.modules.customer_booking.dto.CourtSearchResult;
import com.sportcourt.modules.customer_booking.dto.SelectedBookingSlot;
import com.sportcourt.modules.customer_booking.dto.SlotStatus;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class CustomerBookingPanel extends JPanel {
    private static final String HOME = "HOME";
    private static final String SCHEDULE = "SCHEDULE";
    private static final String CONFIRM = "CONFIRM";

    private final CardLayout cardLayout = new CardLayout();
    private final CustomerBookingController controller = new CustomerBookingController();
    private final CustomerBookingHomeScreen homeScreen;
    private final CustomerBookingScheduleScreen scheduleScreen;
    private final CustomerBookingConfirmScreen confirmScreen;

    private BranchOption currentBranch;
    private CourtSearchResult currentCourt;
    private LocalDate currentBookingDate = LocalDate.now();
    private List<SlotStatus> pendingSlots = List.of();
    private String pendingDepositInvoiceId;
    private BigDecimal pendingDepositAmount;
    private boolean firstShow = true;

    public CustomerBookingPanel() {
        setLayout(cardLayout);
        setBackground(CustomerBookingViewStyle.PAGE_BG);

        homeScreen = new CustomerBookingHomeScreen(controller, this::showSchedule);
        scheduleScreen = new CustomerBookingScheduleScreen(controller, this::showHome, this::showConfirm);
        confirmScreen = new CustomerBookingConfirmScreen(this::showScheduleFromCurrentCourt, this::submitBooking);

        add(homeScreen, HOME);
        add(scheduleScreen, SCHEDULE);
        add(confirmScreen, CONFIRM);

        showHome();
    }

    public void showHome() {
        cardLayout.show(this, HOME);
        if (firstShow) {
            firstShow = false;
            SwingUtilities.invokeLater(homeScreen::triggerFilterDialog);
        }
        revalidate();
        repaint();
    }

    public void showSchedule(BranchOption branch, CourtSearchResult court, LocalDate bookingDate) {
        currentBranch = branch;
        currentCourt = court;
        currentBookingDate = bookingDate == null ? LocalDate.now() : bookingDate;
        pendingSlots = List.of();
        pendingDepositInvoiceId = null;
        pendingDepositAmount = null;
        scheduleScreen.showCourt(branch, court, currentBookingDate);
        cardLayout.show(this, SCHEDULE);
        revalidate();
        repaint();
    }

    private void showScheduleFromCurrentCourt() {
        if (currentBranch == null || currentCourt == null) {
            showHome();
            return;
        }
        scheduleScreen.showCourt(currentBranch, currentCourt, currentBookingDate);
        cardLayout.show(this, SCHEDULE);
        revalidate();
        repaint();
    }

    private void showConfirm(List<SlotStatus> selectedSlots) {
        if (currentBranch == null || selectedSlots == null || selectedSlots.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sân và khung giờ", "Đặt sân",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        pendingSlots = List.copyOf(selectedSlots);
        pendingDepositInvoiceId = null;
        pendingDepositAmount = null;
        currentBookingDate = pendingSlots.get(0).bookingDate();
        String slotCourtId = pendingSlots.get(0).courtId();
        if (currentCourt == null || !currentCourt.courtId().equals(slotCourtId)) {
            controller.findCourt(slotCourtId).ifPresent(c -> currentCourt = c);
        }
        confirmScreen.showDraft(currentBranch, currentCourt, pendingSlots);
        cardLayout.show(this, CONFIRM);
        revalidate();
        repaint();
    }

    private void submitBooking() {
        if ((pendingSlots == null || pendingSlots.isEmpty()) && pendingDepositInvoiceId != null) {
            showCurrentDepositDialog();
            return;
        }

        if (currentBranch == null || pendingSlots.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chưa có thông tin đặt sân", "Đặt sân",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 1. Tạo hóa đơn trước (trạng thái ĐÃ ĐẶT CHỜ CỌC) để giữ chỗ
        BookingPreview preview;
        try {
            List<SelectedBookingSlot> slots = pendingSlots.stream()
                    .map(s -> new SelectedBookingSlot(s.courtId(), s.priceBoardId(), s.bookingDate(), s.price()))
                    .collect(Collectors.toList());
            preview = controller.createBooking(currentBranch.branchId(), slots);
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this,
                    e.getMessage() == null || e.getMessage().isBlank() ? "Không thể đặt sân" : e.getMessage(),
                    "Đặt sân", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String invoiceId = preview.invoiceId();
        BigDecimal deposit = computeDeposit();
        pendingDepositInvoiceId = invoiceId;
        pendingDepositAmount = deposit;

        // 2. Hiển thị dialog QR — nếu thanh toán thành công → cập nhật ĐÃ CỌC CHỜ XÁC NHẬN + thông báo
        //                         nếu timeout/đóng   → huỷ hóa đơn
        showCurrentDepositDialog();
        pendingSlots = List.of();
    }

    private void showCurrentDepositDialog() {
        String invoiceId = pendingDepositInvoiceId;
        BigDecimal deposit = pendingDepositAmount;
        if (invoiceId == null || invoiceId.isBlank() || deposit == null) {
            JOptionPane.showMessageDialog(this, "Chưa có thông tin đặt sân", "Đặt sân",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        CustomerBookingDialogs.showDepositPaymentDialog(this, invoiceId, deposit,
                () -> handleDepositPaid(invoiceId),
                () -> handleDepositExpired(invoiceId)
        );
    }

    private void handleDepositPaid(String invoiceId) {
        try {
            controller.markBookingAsDeposited(invoiceId);
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this,
                    e.getMessage() == null || e.getMessage().isBlank()
                            ? "Không thể cập nhật trạng thái đặt cọc."
                            : e.getMessage(),
                    "Đặt sân", JOptionPane.ERROR_MESSAGE);
            return;
        }

        pendingSlots = List.of();
        pendingDepositInvoiceId = null;
        pendingDepositAmount = null;
        homeScreen.refreshCourts();
        scheduleScreen.refreshCurrentSchedule();
        CustomerBookingDialogs.showSuccessDialog(this, this::showHome, this::showHome);
    }

    private void handleDepositExpired(String invoiceId) {
        Thread worker = new Thread(() -> {
            RuntimeException failure = null;
            try {
                controller.cancelPendingBooking(invoiceId);
            } catch (RuntimeException e) {
                failure = e;
            }

            RuntimeException cancelFailure = failure;
            SwingUtilities.invokeLater(() -> {
                pendingSlots = List.of();
                pendingDepositInvoiceId = null;
                pendingDepositAmount = null;
                homeScreen.refreshCourts();
                scheduleScreen.refreshCurrentSchedule();

                if (cancelFailure != null) {
                    JOptionPane.showMessageDialog(this,
                            cancelFailure.getMessage() == null || cancelFailure.getMessage().isBlank()
                                    ? "Không thể hủy đặt sân quá hạn."
                                    : cancelFailure.getMessage(),
                            "Đặt sân", JOptionPane.ERROR_MESSAGE);
                }
            });
        }, "customer-booking-expire-cancel");
        worker.setDaemon(true);
        worker.start();
    }

    private BigDecimal computeDeposit() {
        BigDecimal total = pendingSlots.stream()
                .map(SlotStatus::price)
                .filter(p -> p != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.multiply(new BigDecimal("0.70")).setScale(2, RoundingMode.HALF_UP);
    }
}
