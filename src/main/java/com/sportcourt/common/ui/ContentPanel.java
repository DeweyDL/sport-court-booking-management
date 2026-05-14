package com.sportcourt.common.ui;

import com.sportcourt.common.style.CrudViewStyle;
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
    private final Map<String, Supplier<JComponent>> factories = new HashMap<>();
    private final Map<String, JScrollPane> loadedViews = new HashMap<>();

    public ContentPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.decode("#F5F7FA"));
        cards.setBackground(Color.decode("#F5F7FA"));
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
        JScrollPane scrollPane = new JScrollPane(view);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.decode("#F5F7FA"));
        CrudViewStyle.configureScrollPane(scrollPane);

        int pad = UIScale.scale(20);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.decode("#F5F7FA"));
        wrapper.setBorder(new EmptyBorder(pad, pad, pad, pad));
        wrapper.add(scrollPane, BorderLayout.CENTER);

        loadedViews.put(key, scrollPane);
        cards.add(wrapper, key);
    }
}
