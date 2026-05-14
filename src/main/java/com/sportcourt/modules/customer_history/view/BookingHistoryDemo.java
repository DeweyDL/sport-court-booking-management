package com.sportcourt.modules.customer_history.view;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;

public class BookingHistoryDemo {
    public static void main(String[] args) {
        try { 
            UIManager.setLookAndFeel(new FlatLightLaf()); 
        } catch (Exception ignored) {
        }
        
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("RENTSTA – Lịch Sử Đặt Sân (Demo)");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 900);
            frame.setLocationRelativeTo(null);
            
            // Khởi tạo panel hiển thị
            frame.add(new BookingHistoryPanel());
            
            frame.setVisible(true);
        });
    }
}
