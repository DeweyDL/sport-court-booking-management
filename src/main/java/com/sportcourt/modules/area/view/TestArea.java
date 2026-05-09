package com.sportcourt.modules.area.view;

import javax.swing.*;
import java.awt.*;

public class TestArea {
    public static void main(String[] args) {
        // Set Look and Feel to system default for a better native appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Ensure UI updates are done on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Area Management Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1400, 800);
            frame.setLocationRelativeTo(null); // Center the frame

            // Create an instance of AreaManagement and add it to the frame
            AreaManagement areaManagementPanel = new AreaManagement();
            frame.add(areaManagementPanel);

            // Make the frame visible
            frame.setVisible(true);
        });
    }
}
