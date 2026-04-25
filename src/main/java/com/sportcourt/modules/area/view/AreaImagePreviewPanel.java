package com.sportcourt.modules.area.view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.nio.file.Path;

// Component preview anh khu vuc duoc dung chung cho man xem va man sua.
public class AreaImagePreviewPanel extends JPanel {
    private Image image;

    public AreaImagePreviewPanel() {
        setPreferredSize(new Dimension(360, 240));
        setMaximumSize(new Dimension(360, 240));
        setMinimumSize(new Dimension(360, 240));
        setOpaque(false);
        setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    public void setImage(Path imagePath) {
        ImageIcon imageIcon = new ImageIcon(imagePath.toString());
        this.image = imageIcon.getImage();
        repaint();
    }

    public void clearImage() {
        this.image = null;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Shape clipShape = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 36, 36);
        g2.setClip(clipShape);

        if (image != null) {
            drawScaledImage(g2);
        } else {
            drawPlaceholder(g2);
        }
        g2.dispose();
    }

    private void drawScaledImage(Graphics2D g2) {
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        int imageWidth = image.getWidth(this);
        int imageHeight = image.getHeight(this);

        if (imageWidth <= 0 || imageHeight <= 0) {
            drawPlaceholder(g2);
            return;
        }

        double scale = Math.max((double) panelWidth / imageWidth, (double) panelHeight / imageHeight);
        int drawWidth = (int) Math.round(imageWidth * scale);
        int drawHeight = (int) Math.round(imageHeight * scale);
        int x = (panelWidth - drawWidth) / 2;
        int y = (panelHeight - drawHeight) / 2;
        g2.drawImage(image, x, y, drawWidth, drawHeight, this);
    }

    private void drawPlaceholder(Graphics2D g2) {
        GradientPaint gradientPaint = new GradientPaint(
                0, 0, new Color(105, 108, 114),
                0, getHeight(), new Color(46, 52, 64)
        );
        g2.setPaint(gradientPaint);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 36, 36);

        g2.setColor(new Color(255, 255, 255, 30));
        for (int x = -getHeight(); x < getWidth(); x += 26) {
            g2.drawLine(x, 0, x + getHeight(), getHeight());
        }

        g2.setColor(new Color(255, 255, 255, 22));
        g2.fillOval(34, 48, 82, 82);
        g2.fillOval(getWidth() - 118, 48, 82, 82);

        g2.setColor(new Color(255, 255, 255, 50));
        g2.fillRect(58, getHeight() - 56, getWidth() - 116, 10);
        g2.fillRect(58, getHeight() - 36, getWidth() - 116, 10);

        g2.setColor(new Color(255, 255, 255, 190));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
        String text = "CHUA CO ANH KHU VUC";
        FontMetrics metrics = g2.getFontMetrics();
        int textX = (getWidth() - metrics.stringWidth(text)) / 2;
        int textY = getHeight() / 2 + 6;
        g2.drawString(text, textX, textY);
    }
}
