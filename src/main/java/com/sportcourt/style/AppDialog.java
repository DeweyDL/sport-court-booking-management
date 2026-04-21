package com.sportcourt.style;

import javax.swing.*;
import java.awt.*;

public final class AppDialog {
    private AppDialog() {
    }

    public static void showInfo(Component parent, String message) {
        show(parent, "Thong bao", message, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, String message) {
        show(parent, "Loi", message, JOptionPane.ERROR_MESSAGE);
    }

    private static void show(Component parent, String title, String message, int type) {
        UIManager.put("OptionPane.messageFont", AppFonts.lexendRegular(14f));
        UIManager.put("OptionPane.buttonFont", AppFonts.lexendBold(13f));
        UIManager.put("OptionPane.minimumSize", new Dimension(420, 150));
        JOptionPane.showMessageDialog(parent, message, title, type);
    }
}
