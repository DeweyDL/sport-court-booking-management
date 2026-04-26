package com.sportcourt.modules.court.test;

import com.formdev.flatlaf.FlatLightLaf;
import com.sportcourt.modules.court.view.CourtManagementPanel;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class CourtTest {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(new FlatLightLaf());
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            JFrame frame = new JFrame("Court Management Panel Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1180, 760);
            frame.setLocationRelativeTo(null);

            CourtManagementPanel panel = new CourtManagementPanel();
            panel.setCurrentBranchId("CN_TEST_01");

            frame.setContentPane(panel);
            frame.setVisible(true);
        });
    }
}