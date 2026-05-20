package com.sportcourt.common.style;

import javax.swing.*;
import java.awt.*;

public final class AppDialog {
    private AppDialog() {
    }

    public static void showInfo(Component parent, String message) {
        show(parent, "Thông báo", message, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, String message) {
        show(parent, "Lỗi", message, JOptionPane.ERROR_MESSAGE);
    }

    private static void show(Component parent, String title, String message, int type) {
        UIManager.put("OptionPane.messageFont", AppFonts.lexendRegular(UIScale.scaleFont(14f)));
        UIManager.put("OptionPane.buttonFont",  AppFonts.lexendBold(UIScale.scaleFont(13f)));
        UIManager.put("OptionPane.minimumSize", new Dimension(420, 150));
        JOptionPane.showMessageDialog(parent, message, title, type);
    }
}
