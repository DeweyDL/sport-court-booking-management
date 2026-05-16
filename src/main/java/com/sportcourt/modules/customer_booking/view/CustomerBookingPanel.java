package com.sportcourt.modules.customer_booking.view;

import com.sportcourt.modules.customer_booking.controller.CustomerBookingController;
import com.sportcourt.modules.customer_booking.dto.BookingPreview;
import com.sportcourt.modules.customer_booking.dto.BranchOption;
import com.sportcourt.modules.customer_booking.dto.CourtSearchResult;
import com.sportcourt.modules.customer_booking.dto.SelectedBookingSlot;
import com.sportcourt.modules.customer_booking.dto.SlotStatus;

import javax.swing.*;
import java.awt.*;
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

    public void showSchedule(BranchOption branch, CourtSearchResult court) {
        currentBranch = branch;
        currentCourt = court;
        pendingSlots = List.of();
        scheduleScreen.showCourt(branch, court);
        cardLayout.show(this, SCHEDULE);
        revalidate();
        repaint();
    }

    private void showScheduleFromCurrentCourt() {
        if (currentBranch == null || currentCourt == null) {
            showHome();
            return;
        }
        scheduleScreen.showCourt(currentBranch, currentCourt);
        cardLayout.show(this, SCHEDULE);
        revalidate();
        repaint();
    }

    private void showConfirm(List<SlotStatus> selectedSlots) {
        if (currentBranch == null || currentCourt == null || selectedSlots == null || selectedSlots.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui long chon san va khung gio.", "Dat san",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        pendingSlots = List.copyOf(selectedSlots);
        confirmScreen.showDraft(currentBranch, currentCourt, pendingSlots);
        cardLayout.show(this, CONFIRM);
        revalidate();
        repaint();
    }

    private void submitBooking() {
        if (currentBranch == null || pendingSlots.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chua co thong tin dat san.", "Dat san",
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
            BookingPreview preview = controller.createBooking(currentBranch.branchId(), selectedBookingSlots);
            JOptionPane.showMessageDialog(
                    this,
                    "Dat san thanh cong. Ma hoa don: " + preview.invoiceId(),
                    "Dat san",
                    JOptionPane.INFORMATION_MESSAGE
            );
            pendingSlots = List.of();
            homeScreen.refreshCourts();
            showHome();
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage() == null || e.getMessage().isBlank() ? "Khong the dat san." : e.getMessage(),
                    "Dat san",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
