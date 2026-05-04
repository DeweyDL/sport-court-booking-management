package com.sportcourt;


import com.formdev.flatlaf.FlatLightLaf;
import com.sportcourt.modules.auth.view.*;

import javax.swing.*;


public class App {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new LoginScreen().setVisible(true));
    }
}
