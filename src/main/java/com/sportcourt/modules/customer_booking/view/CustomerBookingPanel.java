package com.sportcourt.modules.customer_booking.view;

import com.sportcourt.modules.customer_booking.controller.CustomerBookingController;
import com.sportcourt.modules.customer_booking.dto.BranchOption;
import com.sportcourt.modules.customer_booking.dto.CourtSearchResult;
import com.sportcourt.modules.customer_booking.dto.SelectedBookingSlot;
import com.sportcourt.modules.customer_booking.dto.SlotStatus;

import javax.swing.*;
import java.awt.*;
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
        if (currentBranch == null || pendingSlots.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chưa có thông tin đặt sân", "Đặt sân",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            List<SelectedBookingSlot> selectedBookingSlots = pendingSlots.stream()
                    .map(slot -> new SelectedBookingSlot(
                            slot.courtId(),
                            slot.priceBoardId(),
                            slot.bookingDate(),
                            slot.price()
                    ))
                    .collect(Collectors.toList());
            controller.createBooking(currentBranch.branchId(), selectedBookingSlots);
            pendingSlots = List.of();
            homeScreen.refreshCourts();
            CustomerBookingDialogs.showSuccessDialog(this, this::showHome, this::showHome);
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage() == null || e.getMessage().isBlank() ? "Không thể đặt sân" : e.getMessage(),
                    "Đặt sân",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
