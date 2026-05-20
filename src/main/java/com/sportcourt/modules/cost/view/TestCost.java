package com.sportcourt.modules.cost.view;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;

public class TestCost {
    public static void main(String[] args) {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Test CostManagement");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setContentPane(new CostManagement());
            frame.setVisible(true);
        });
    }
}

