package com.sportcourt.modules.bill.view;

import com.sportcourt.common.style.AppDialog;
import com.sportcourt.modules.bill.controller.ManageBillController;
import com.sportcourt.modules.bill.dto.BillResult;
import com.sportcourt.modules.customer_booking.controller.CustomerBookingController;
import com.sportcourt.modules.customer_booking.dto.BranchOption;
import com.sportcourt.modules.customer_booking.dto.CourtSearchResult;
import com.sportcourt.modules.customer_booking.dto.SelectedBookingSlot;
import com.sportcourt.modules.customer_booking.dto.SlotStatus;
import com.sportcourt.modules.customer_booking.view.CustomerBookingScheduleScreen;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class AddCourtScreen extends JPanel {

    private static final String HOME = "HOME";
    private static final String SCHEDULE = "SCHEDULE";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);
    private final CustomerBookingController controller = new CustomerBookingController();
    private final ManageBillController billController = new ManageBillController();

    private final String maHD;
    private final Runnable onBack;
    private final boolean advanceBooking;

    private final BillBookingHomeScreen homeScreen;
    private final CustomerBookingScheduleScreen scheduleScreen;
    private boolean firstShow = true;

    public AddCourtScreen(String maHD, Runnable onBack) {
        this(maHD, onBack, false);
    }

    public AddCourtScreen(String maHD, Runnable onBack, boolean advanceBooking) {
        this.maHD = maHD;
        this.onBack = onBack;
        this.advanceBooking = advanceBooking;
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        homeScreen = new BillBookingHomeScreen(controller, this::showSchedule);
        scheduleScreen = new CustomerBookingScheduleScreen(controller, this::showHome, this::confirmSlots);

        cardPanel.setOpaque(false);
        cardPanel.add(homeScreen, HOME);
        cardPanel.add(scheduleScreen, SCHEDULE);

        add(buildTopBar(), BorderLayout.NORTH);
        add(cardPanel, BorderLayout.CENTER);
        showHome();
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(new EmptyBorder(14, 24, 12, 24));

        JButton backBtn = new JButton("< Quay lại hóa đơn");
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        backBtn.setForeground(new Color(107, 114, 128));
        backBtn.setBorderPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> { if (onBack != null) onBack.run(); });

        JLabel title = new JLabel(advanceBooking ? "Thêm sân đặt trước" : "Thêm sân chơi ngay");
        title.setFont(new Font("Lexend", Font.BOLD, 16));
        title.setForeground(new Color(17, 24, 39));

        bar.add(backBtn, BorderLayout.WEST);
        bar.add(title, BorderLayout.CENTER);
        return bar;
    }

    private void showHome() {
        cardLayout.show(cardPanel, HOME);
        if (firstShow) {
            firstShow = false;
            SwingUtilities.invokeLater(homeScreen::triggerFilterDialog);
        }
        revalidate();
        repaint();
    }

    private void showSchedule(BranchOption branch, CourtSearchResult court, LocalDate bookingDate) {
        scheduleScreen.showCourt(branch, court, bookingDate == null ? LocalDate.now() : bookingDate);
        cardLayout.show(cardPanel, SCHEDULE);
        revalidate();
        repaint();
    }

    private void confirmSlots(List<SlotStatus> selectedSlots) {
        if (selectedSlots == null || selectedSlots.isEmpty()) {
            AppDialog.showError(this, "Vui lòng chọn khung giờ.");
            return;
        }
        try {
            List<SelectedBookingSlot> slots = selectedSlots.stream()
                    .map(s -> new SelectedBookingSlot(
                            s.courtId(), s.priceBoardId(), s.bookingDate(), s.price()))
                    .collect(Collectors.toList());
            BillResult<Void> result = billController.addCourtBookingDetails(maHD, slots, advanceBooking);
            if (!result.success()) {
                AppDialog.showError(this, result.message() == null ? "Không thể thêm sân." : result.message());
                return;
            }
            AppDialog.showInfo(this, advanceBooking
                    ? "Đã thêm sân đặt trước. Bấm Thanh toán cọc để hoàn tất đặt trước."
                    : "Đã thêm sân chơi ngay vào hóa đơn.");
            if (onBack != null) onBack.run();
        } catch (RuntimeException e) {
            AppDialog.showError(this, e.getMessage() == null || e.getMessage().isBlank()
                    ? "Không thể thêm sân." : e.getMessage());
        }
    }
}
