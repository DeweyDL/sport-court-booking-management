package com.sportcourt.common.ui;

import com.sportcourt.common.style.UIScale;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ContentPanel extends JPanel {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);
    private final Map<String, ViewSpec> specs = new HashMap<>();
    private final Map<String, JComponent> loadedViews = new HashMap<>();

    private static final class ViewSpec {
        final Supplier<JComponent> factory;
        final boolean wrapInScrollPane;

        private ViewSpec(Supplier<JComponent> factory, boolean wrapInScrollPane) {
            this.factory = factory;
            this.wrapInScrollPane = wrapInScrollPane;
        }
    }

    public ContentPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.decode("#F5F7FA"));
        cards.setBackground(Color.decode("#F5F7FA"));
        add(cards, BorderLayout.CENTER);
    }

    public void registerView(String key, JComponent view) {
        registerView(key, () -> view, true);
    }

    public void registerView(String key, Supplier<JComponent> factory) {
        registerView(key, factory, true);
    }

    // Some views (e.g. dashboard) manage their own scrolling. Wrapping them again in another
    // JScrollPane often causes nested scrollbars and width calculations that lead to overflow.
    public void registerView(String key, Supplier<JComponent> factory, boolean wrapInScrollPane) {
        specs.put(key, new ViewSpec(factory, wrapInScrollPane));
    }

    public void showView(String key) {
        ensureViewCreated(key);
        cardLayout.show(cards, key);
    }

    private void ensureViewCreated(String key) {
        if (loadedViews.containsKey(key)) {
            return;
        }
        ViewSpec spec = specs.get(key);
        if (spec == null) {
            throw new IllegalArgumentException("View key is not registered: " + key);
        }
        JComponent view = spec.factory.get();

        JComponent content;
        if (spec.wrapInScrollPane) {
            JScrollPane scrollPane = new JScrollPane(view);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.getViewport().setBackground(Color.decode("#F5F7FA"));
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            content = scrollPane;
        } else {
            content = view;
        }

        // Most views expect a consistent outer padding. Some (e.g. dashboard) already manage their
        // own insets; adding another wrapper border can look like "broken" top/bottom edges.
        int pad = spec.wrapInScrollPane ? UIScale.scale(20) : 0;
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.decode("#F5F7FA"));
        wrapper.setBorder(new EmptyBorder(pad, pad, pad, pad));
        wrapper.add(content, BorderLayout.CENTER);

        loadedViews.put(key, content);
        cards.add(wrapper, key);
    }
}
