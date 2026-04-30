package com.sportcourt.modules.staff.view;

import com.formdev.flatlaf.FlatLightLaf;
import com.sportcourt.modules.staff.controller.StaffController;

import javax.swing.*;
import java.awt.*;

public class StaffModuleRunner {
    private StaffModuleRunner() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                FlatLightLaf.setup();
            } catch (Exception ignored) {
                // Chạy được kể cả khi FlatLaf chưa được cấu hình.
            }

            JFrame frame = new JFrame("Quản lý nhân viên");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setSize(1366, 768);
            frame.setLocationRelativeTo(null);

            StaffPanel panel = new StaffPanel();
            panel.setCurrentBranchId("CN01");
            panel.setStaffTypeIds("LNV01", "LNV02");
            new StaffController(panel);

            frame.setLayout(new BorderLayout());
            frame.add(panel, BorderLayout.CENTER);
            frame.setVisible(true);
        });
    }
}
