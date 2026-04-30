package com.sportcourt.modules.product.view;

import com.sportcourt.modules.product.controller.ProductController;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Dimension;

public class ProductModuleRunner {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            ProductPanel panel = new ProductPanel();
            new ProductController(panel);

            JFrame frame = new JFrame("Quản lý sản phẩm");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(panel);
            frame.setMinimumSize(new Dimension(1100, 680));
            frame.setSize(1280, 760);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
