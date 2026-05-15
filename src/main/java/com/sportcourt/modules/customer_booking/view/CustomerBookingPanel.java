package com.sportcourt.modules.customer_booking.view;

import javax.swing.*;
import java.awt.*;

public class CustomerBookingPanel extends JPanel {
    private static final String HOME = "HOME";
    private static final String SCHEDULE = "SCHEDULE";
    private static final String CONFIRM = "CONFIRM";

    private final CardLayout cardLayout = new CardLayout();

    public CustomerBookingPanel() {
        setLayout(cardLayout);
        setBackground(CustomerBookingViewStyle.PAGE_BG);

        CustomerBookingHomeScreen homeScreen = new CustomerBookingHomeScreen(court -> showSchedule());
        CustomerBookingScheduleScreen scheduleScreen = new CustomerBookingScheduleScreen(this::showHome, this::showConfirm);
        CustomerBookingConfirmScreen confirmScreen = new CustomerBookingConfirmScreen(this::showSchedule, this::showSubmittedMessage);

        add(homeScreen, HOME);
        add(scheduleScreen, SCHEDULE);
        add(confirmScreen, CONFIRM);

        showHome();
    }

    public void showHome() {
        cardLayout.show(this, HOME);
        revalidate();
        repaint();
    }

    public void showSchedule() {
        cardLayout.show(this, SCHEDULE);
        revalidate();
        repaint();
    }

    public void showConfirm() {
        cardLayout.show(this, CONFIRM);
        revalidate();
        repaint();
    }

    private void showSubmittedMessage() {
        JOptionPane.showMessageDialog(
                this,
                "Yêu cầu đặt sân đã được ghi nhận.",
                "Đặt sân",
                JOptionPane.INFORMATION_MESSAGE
        );
        showHome();
    }
}
