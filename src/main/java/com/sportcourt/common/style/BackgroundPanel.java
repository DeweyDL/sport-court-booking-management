package com.sportcourt.common.style;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.net.URL;

public class BackgroundPanel extends JPanel {
    public enum ScaleMode {
        COVER,
        CONTAIN
    }

    private final Image backgroundImage;
    private final ScaleMode scaleMode;

    public BackgroundPanel(String path) {
        this(path, ScaleMode.COVER);
    }

    public BackgroundPanel(String path, ScaleMode scaleMode) {
        URL resource = getClass().getResource(path);
        this.backgroundImage = resource == null ? null : new ImageIcon(resource).getImage();
        this.scaleMode = scaleMode == null ? ScaleMode.COVER : scaleMode;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            int panelWidth = getWidth();
            int panelHeight = getHeight();
            int imageWidth = backgroundImage.getWidth(this);
            int imageHeight = backgroundImage.getHeight(this);
            if (panelWidth <= 0 || panelHeight <= 0 || imageWidth <= 0 || imageHeight <= 0) {
                return;
            }

            // Keep image ratio (no distortion), then either crop (COVER) or fit fully (CONTAIN).
            double widthRatio = (double) panelWidth / imageWidth;
            double heightRatio = (double) panelHeight / imageHeight;
            double scale = scaleMode == ScaleMode.CONTAIN
                    ? Math.min(widthRatio, heightRatio)
                    : Math.max(widthRatio, heightRatio);
            int drawWidth = (int) Math.round(imageWidth * scale);
            int drawHeight = (int) Math.round(imageHeight * scale);
            int drawX = (panelWidth - drawWidth) / 2;
            int drawY = (panelHeight - drawHeight) / 2;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.drawImage(backgroundImage, drawX, drawY, drawWidth, drawHeight, this);
            g2.dispose();
        }
    }
}
