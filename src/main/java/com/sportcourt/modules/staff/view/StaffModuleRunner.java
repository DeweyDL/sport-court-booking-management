package com.sportcourt.modules.staff.view;

import com.formdev.flatlaf.FlatLightLaf;
import com.sportcourt.modules.staff.controller.StaffController;

import javax.swing.*;
import java.awt.*;

public class StaffModuleRunner {
    private StaffModuleRunner() {
    }

    public static void main(String[] args) {
        // Cài đặt Look and Feel hệ thống (Windows) để các cửa sổ hiển thị đẹp mắt
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Test UI - Quản Lý Nhân Viên");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Khởi tạo Panel Nhân viên
            StaffPanel mainPanel = new StaffPanel();

            frame.add(mainPanel);

            // Set size và hiển thị giữa màn hình
            frame.setSize(1200, 800);
            frame.setMinimumSize(new Dimension(900, 600));
            frame.setLocationRelativeTo(null);

            // Đặt màu nền chung
            frame.getContentPane().setBackground(new Color(245, 247, 250));

            frame.setVisible(true);
        });
    }
}

