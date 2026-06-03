package com.sportcourt.common.ui;

import com.sportcourt.common.style.CrudViewStyle;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ContentPanel extends JPanel {
    private static final Color BG = Color.decode("#F5F7FA");
    private static final int MIN_CONTENT_WIDTH = 860;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);
    private final Map<String, Supplier<JComponent>> factories = new HashMap<>();
    private final Map<String, JScrollPane> loadedViews = new HashMap<>();

    public ContentPanel() {
        setLayout(new BorderLayout());
        setBackground(BG);
        cards.setBackground(BG);
        add(cards, BorderLayout.CENTER);
    }

    public void registerView(String key, JComponent view) {
        registerView(key, () -> view);
    }

    public void registerView(String key, Supplier<JComponent> factory) {
        factories.put(key, factory);
    }

    public void showView(String key) {
        ensureViewCreated(key);
        cardLayout.show(cards, key);
    }

    private void ensureViewCreated(String key) {
        if (loadedViews.containsKey(key)) {
            return;
        }
        Supplier<JComponent> factory = factories.get(key);
        if (factory == null) {
            throw new IllegalArgumentException("View key is not registered: " + key);
        }
        JComponent view = factory.get();

        // Every module view gets ComponentListener-driven font scaling.
        CrudViewStyle.installResponsiveTypography(view);

        // contentHost enforces a minimum rendered width so the JScrollPane can
        // show a horizontal scrollbar when the visible area is too narrow
        // (sidebar visible + small screen). Uses paintComponent to fill its
        // background correctly at any scroll position.
        JPanel contentHost = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                return new Dimension(Math.max(MIN_CONTENT_WIDTH, d.width), d.height);
            }
            @Override
            public Dimension getMinimumSize() {
                return new Dimension(MIN_CONTENT_WIDTH, 0);
            }
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(BG);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        contentHost.setOpaque(false); // we handle painting ourselves above
        contentHost.add(view, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(contentHost);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(BG);
        scrollPane.getViewport().setBackground(BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(20);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        // ScrollPane goes DIRECTLY into the CardLayout — no extra wrapper panel
        // so there is no padding outside the scrollable area that would appear
        // as "redundant grey space" when the user scrolls.
        loadedViews.put(key, scrollPane);
        cards.add(scrollPane, key);
    }
}
