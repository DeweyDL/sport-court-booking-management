package com.sportcourt.modules.staff.view;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Font;

public final class StaffTheme {
    public static final Color BACKGROUND = new Color(245, 247, 251);
    public static final Color CARD = Color.WHITE;
    public static final Color BORDER = new Color(229, 231, 235);
    public static final Color TEXT = new Color(31, 41, 55);
    public static final Color PRIMARY = new Color(37, 99, 235);
    public static final Color DANGER = new Color(220, 38, 38);

    private StaffTheme() {
    }

    public static Font fontPlain(int size) {
        return new Font("Segoe UI", Font.PLAIN, size);
    }

    public static Font fontBold(int size) {
        return new Font("Segoe UI", Font.BOLD, size);
    }

    public static void applyCard(JComponent component) {
        component.setBackground(CARD);
        component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
    }

    public static JButton primaryButton(String text) {
        JButton button = baseButton(text);
        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        return button;
    }

    public static JButton secondaryButton(String text) {
        JButton button = baseButton(text);
        button.setBackground(new Color(229, 231, 235));
        button.setForeground(TEXT);
        return button;
    }

    public static JButton dangerButton(String text) {
        JButton button = baseButton(text);
        button.setBackground(DANGER);
        button.setForeground(Color.WHITE);
        return button;
    }

    private static JButton baseButton(String text) {
        JButton button = new JButton(text);
        button.setFont(fontBold(13));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        return button;
    }
}
