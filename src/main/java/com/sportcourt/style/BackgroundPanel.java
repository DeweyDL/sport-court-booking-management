package com.sportcourt.style;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class BackgroundPanel extends JPanel {
    private Image backgroundImage;

    // Constructor nhận vào classpath resource path, ví dụ: /image/bg.png
    public BackgroundPanel(String path) {
        URL imageUrl = getClass().getResource(path);
        if (imageUrl != null) {
            this.backgroundImage = new ImageIcon(imageUrl).getImage();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Vẽ ảnh nền phủ kín toàn bộ diện tích Panel
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}