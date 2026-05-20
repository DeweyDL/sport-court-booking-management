package com.sportcourt.modules.cost.view;

import com.sportcourt.modules.cost.view.CostMockData.AreaOption;
import com.sportcourt.modules.cost.view.CostMockData.CostItem;
import com.sportcourt.modules.cost.view.CostMockData.Store;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.function.Consumer;

public class CostAdd extends JPanel {
    private static final int MIN_HOUR = 0;
    private static final int MAX_HOUR = 23;
    private static final int DEFAULT_START_HOUR = 7;
    private static final int DEFAULT_END_START_HOUR = 10;

    private final Store store;
    private final Consumer<String> onSaved;

    private final JTextField maBgField = createDisplayField();
    private final JComboBox<AreaOption> areaComboBox = new JComboBox<>();
    private final HourRangeSlider hourRangeSlider =
            new HourRangeSlider(MIN_HOUR, MAX_HOUR, DEFAULT_START_HOUR, DEFAULT_END_START_HOUR);
    private final JLabel selectedRangeLabel = new JLabel();
    private final JTextField startHourField = createDisplayField();
    private final JTextField endHourField = createDisplayField();
    private final JTextField giaField = createEditField();

    private JDialog dialog;
    private String generatedMaBg;

    public CostAdd(Store store, Consumer<String> onSaved) {
        this.store = store;
        this.onSaved = onSaved;

        setOpaque(true);
        setBackground(new Color(248, 249, 252));
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));

        hourRangeSlider.setOnRangeChanged(this::updateHoursFromSlider);
        add(createContent(), BorderLayout.CENTER);
    }

    public void showCreator(Component parent) {
        prepareCreateForm();
        JDialog popup = ensureDialog(parent);
        popup.pack();
        popup.setLocationRelativeTo(parent);
        popup.setVisible(true);
    }

    private void prepareCreateForm() {
        generatedMaBg = store.generateNextMaBg();
        maBgField.setText(generatedMaBg);
        giaField.setText("");

        bindAreaOptions();
        hourRangeSlider.setRange(DEFAULT_START_HOUR, DEFAULT_END_START_HOUR);
        updateHoursFromSlider();
    }

    private void bindAreaOptions() {
        DefaultComboBoxModel<AreaOption> model = new DefaultComboBoxModel<>();
        for (AreaOption opt : store.getAreaOptions()) {
            model.addElement(opt);
        }
        areaComboBox.setModel(model);
        styleComboBox(areaComboBox);
        if (model.getSize() > 0) {
            areaComboBox.setSelectedIndex(0);
        }
    }

    private void updateHoursFromSlider() {
        int start = hourRangeSlider.getLowerValue();
        int endStart = hourRangeSlider.getUpperValue();
        int slotCount = endStart - start + 1;

        startHourField.setText(formatHour(start));
        endHourField.setText(formatHour(endStart));
        selectedRangeLabel.setText(
                "Sẽ tạo " + slotCount + " khung giờ, từ "
                        + formatHour(start) + "-" + formatHour(start + 1)
                        + " đến " + formatHour(endStart) + "-" + formatHour(endStart + 1)
        );
    }

    private JDialog ensureDialog(Component parent) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        if (dialog == null || dialog.getOwner() != owner) {
            if (dialog != null) {
                dialog.dispose();
            }
            dialog = new JDialog(owner, "Thêm bảng giá", Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
            dialog.setContentPane(this);
            dialog.setResizable(false);
            dialog.pack();
        }
        return dialog;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setOpaque(false);
        content.add(createHeader(), BorderLayout.NORTH);
        content.add(createForm(), BorderLayout.CENTER);
        content.add(createActions(), BorderLayout.SOUTH);
        return content;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Thêm bảng giá");
        titleLabel.setFont(new Font("Lexend", Font.BOLD, 24));
        titleLabel.setForeground(new Color(30, 41, 59));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Chọn khoảng giờ, hệ thống sẽ tự tạo từng khung 1 giờ cùng một giá.");
        subtitleLabel.setFont(new Font("Lexend", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(100, 116, 139));
        subtitleLabel.setBorder(new EmptyBorder(6, 0, 0, 0));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(titleLabel);
        header.add(subtitleLabel);
        return header;
    }

    private JPanel createForm() {
        JPanel form = new JPanel();
        form.setOpaque(true);
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(18, 18, 18, 18));
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.setMaximumSize(new Dimension(460, Integer.MAX_VALUE));

        form.add(createReadOnlyField("Mã bảng giá bắt đầu", maBgField));
        form.add(Box.createVerticalStrut(17));
        form.add(createComboField("Khu vực", areaComboBox));
        form.add(Box.createVerticalStrut(17));
        form.add(createRangeField("Khoảng giờ", hourRangeSlider));
        form.add(Box.createVerticalStrut(17));
        form.add(createReadOnlyField("Giờ bắt đầu", startHourField));
        form.add(Box.createVerticalStrut(17));
        form.add(createReadOnlyField("Giờ bắt đầu cuối", endHourField));
        form.add(Box.createVerticalStrut(17));
        form.add(createEditField("Giá (VNĐ)", giaField));
        return form;
    }

    private JPanel createActions() {
        JPanel actions = new JPanel(new GridLayout(1, 2, 10, 0));
        actions.setOpaque(false);

        JButton cancel = createPillButton("Hủy", new Color(226, 232, 240), new Color(30, 41, 59), true);
        cancel.addActionListener(e -> cancelCreate());

        JButton save = createPillButton("Thêm bảng giá", new Color(16, 110, 0), Color.WHITE, true);
        save.addActionListener(e -> saveNewBangGia());

        actions.add(cancel);
        actions.add(save);
        return actions;
    }

    private JPanel createReadOnlyField(String labelText, JTextField field) {
        JLabel label = createFieldLabel(labelText);

        JPanel wrapper = createReadOnlyInputWrapper();
        wrapper.add(field, BorderLayout.CENTER);

        JPanel container = createFieldContainer();
        container.add(label);
        container.add(Box.createVerticalStrut(6));
        container.add(wrapper);
        return container;
    }

    private JPanel createEditField(String labelText, JTextField field) {
        JLabel label = createFieldLabel(labelText);

        JPanel wrapper = createInputWrapper();
        wrapper.add(field, BorderLayout.CENTER);

        JPanel container = createFieldContainer();
        container.add(label);
        container.add(Box.createVerticalStrut(6));
        container.add(wrapper);
        return container;
    }

    private JPanel createComboField(String labelText, JComponent component) {
        JLabel label = createFieldLabel(labelText);

        JPanel wrapper = createInputWrapper();
        wrapper.add(component, BorderLayout.CENTER);

        JPanel container = createFieldContainer();
        container.add(label);
        container.add(Box.createVerticalStrut(6));
        container.add(wrapper);
        return container;
    }

    private JPanel createRangeField(String labelText, JComponent component) {
        JLabel label = createFieldLabel(labelText);

        selectedRangeLabel.setFont(new Font("Lexend", Font.PLAIN, 12));
        selectedRangeLabel.setForeground(new Color(71, 85, 105));
        selectedRangeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel sliderWrapper = new JPanel(new BorderLayout());
        sliderWrapper.setOpaque(false);
        sliderWrapper.setPreferredSize(new Dimension(420, 58));
        sliderWrapper.setMinimumSize(new Dimension(420, 58));
        sliderWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        sliderWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        sliderWrapper.add(component, BorderLayout.CENTER);

        JPanel container = createFieldContainer();
        container.add(label);
        container.add(Box.createVerticalStrut(6));
        container.add(sliderWrapper);
        container.add(Box.createVerticalStrut(4));
        container.add(selectedRangeLabel);
        return container;
    }

    private JLabel createFieldLabel(String labelText) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Lexend", Font.BOLD, 12));
        label.setForeground(new Color(30, 41, 59));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JPanel createFieldContainer() {
        JPanel container = new JPanel();
        container.setOpaque(false);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setAlignmentX(Component.LEFT_ALIGNMENT);
        return container;
    }

    private JPanel createInputWrapper() {
        return createInputWrapper(Color.WHITE);
    }

    private JPanel createReadOnlyInputWrapper() {
        return createInputWrapper(new Color(241, 245, 249));
    }

    private JPanel createInputWrapper(Color background) {
        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(background);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2.setColor(new Color(203, 213, 225));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 25, 25);
                g2.dispose();
            }
        };
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(0, 12, 0, 12));
        wrapper.setPreferredSize(new Dimension(420, 40));
        wrapper.setMinimumSize(new Dimension(420, 40));
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        return wrapper;
    }

    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setBorder(BorderFactory.createEmptyBorder());
        comboBox.setBackground(new Color(249, 250, 251));
        comboBox.setOpaque(false);
        comboBox.setFocusable(false);

        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setBorder(new EmptyBorder(0, 2, 0, 2));
                return label;
            }
        });

        comboBox.setUI(new BasicComboBoxUI() {
            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            }

            @Override
            public Insets getInsets() {
                return new Insets(0, 0, 0, 0);
            }

            @Override
            protected JButton createArrowButton() {
                JButton button = new JButton() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(new Color(107, 114, 128));
                        int w = getWidth();
                        int h = getHeight();
                        int size = 8;
                        int cx = w / 2;
                        int cy = h / 2 + 1;
                        Polygon p = new Polygon();
                        p.addPoint(cx - size / 2, cy - 2);
                        p.addPoint(cx + size / 2, cy - 2);
                        p.addPoint(cx, cy + size / 2);
                        g2.fillPolygon(p);
                        g2.dispose();
                    }
                };
                button.setBorder(new EmptyBorder(0, 6, 0, 6));
                button.setContentAreaFilled(false);
                button.setFocusPainted(false);
                button.setOpaque(false);
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
                button.setPreferredSize(new Dimension(34, 40));
                return button;
            }
        });
    }

    private void saveNewBangGia() {
        AreaOption selectedArea = (AreaOption) areaComboBox.getSelectedItem();
        if (selectedArea == null) {
            JOptionPane.showMessageDialog(this, "Hãy chọn khu vực.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int gioBatDau = hourRangeSlider.getLowerValue();
        int gioBatDauCuoi = hourRangeSlider.getUpperValue();
        int slotCount = gioBatDauCuoi - gioBatDau + 1;

        BigDecimal gia;
        try {
            gia = new BigDecimal(giaField.getText().trim().replace(",", ""));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Giá không hợp lệ.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (gia.compareTo(BigDecimal.ZERO) <= 0) {
            JOptionPane.showMessageDialog(this, "Giá phải lớn hơn 0.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Tạo " + slotCount + " khung giờ từ "
                        + formatHour(gioBatDau) + "-" + formatHour(gioBatDau + 1)
                        + " đến " + formatHour(gioBatDauCuoi) + "-" + formatHour(gioBatDauCuoi + 1)
                        + " cho khu vực " + selectedArea.maKv() + "?",
                "Xác nhận tạo",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            store.create(new CostItem(
                    generatedMaBg,
                    selectedArea.maKv(),
                    null,
                    gioBatDau,
                    gioBatDauCuoi,
                    gia,
                    false,
                    java.time.LocalDateTime.now()
            ));
            JOptionPane.showMessageDialog(this, "Đã thêm bảng giá mới.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            if (dialog != null) {
                dialog.setVisible(false);
            }
            onSaved.accept(generatedMaBg);
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getCause() == null ? exception.getMessage() : exception.getCause().getMessage(),
                    "Lỗi thêm bảng giá",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void cancelCreate() {
        if (dialog != null) {
            dialog.setVisible(false);
        }
    }

    private JTextField createDisplayField() {
        JTextField textField = new JTextField();
        textField.setEditable(false);
        textField.setBorder(null);
        textField.setOpaque(false);
        textField.setFocusable(false);
        textField.setFont(new Font("Lexend", Font.BOLD, 14));
        textField.setForeground(new Color(31, 41, 55));
        return textField;
    }

    private JTextField createEditField() {
        JTextField textField = new JTextField();
        textField.setEditable(true);
        textField.setBorder(null);
        textField.setOpaque(false);
        textField.setFont(new Font("Lexend", Font.PLAIN, 14));
        textField.setForeground(new Color(31, 41, 55));
        return textField;
    }

    private String formatHour(int hour) {
        return "%02d:00".formatted(hour);
    }

    private JButton createPillButton(String text, Color bg, Color fg, boolean bold) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                super.paintComponent(g);
                g2.dispose();
            }
        };
        button.setForeground(fg);
        button.setFont(new Font("Lexend", bold ? Font.BOLD : Font.PLAIN, 13));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 18, 10, 18));
        return button;
    }

    private static final class HourRangeSlider extends JComponent {
        private static final int LOWER_THUMB = 1;
        private static final int UPPER_THUMB = 2;

        private final int minHour;
        private final int maxHour;
        private final int thumbRadius = 9;
        private final int trackHeight = 6;
        private int lowerValue;
        private int upperValue;
        private int activeThumb;
        private Runnable onRangeChanged;

        private HourRangeSlider(int minHour, int maxHour, int lowerValue, int upperValue) {
            this.minHour = minHour;
            this.maxHour = maxHour;
            setRange(lowerValue, upperValue);
            setPreferredSize(new Dimension(420, 58));
            setMinimumSize(new Dimension(260, 58));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            MouseAdapter mouseHandler = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    activeThumb = nearestThumb(e.getX());
                    updateActiveThumb(e.getX());
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    updateActiveThumb(e.getX());
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    activeThumb = 0;
                }
            };
            addMouseListener(mouseHandler);
            addMouseMotionListener(mouseHandler);
        }

        private int getLowerValue() {
            return lowerValue;
        }

        private int getUpperValue() {
            return upperValue;
        }

        private void setOnRangeChanged(Runnable onRangeChanged) {
            this.onRangeChanged = onRangeChanged;
        }

        private void setRange(int lowerValue, int upperValue) {
            int lower = clamp(lowerValue);
            int upper = clamp(upperValue);
            if (lower > upper) {
                int temp = lower;
                lower = upper;
                upper = temp;
            }
            this.lowerValue = lower;
            this.upperValue = upper;
            fireRangeChanged();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int startX = trackStartX();
            int endX = trackEndX();
            int centerY = 23;
            int lowerX = valueToX(lowerValue);
            int upperX = valueToX(upperValue);

            g2.setStroke(new BasicStroke(trackHeight, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(226, 232, 240));
            g2.drawLine(startX, centerY, endX, centerY);
            g2.setColor(new Color(22, 163, 74));
            g2.drawLine(lowerX, centerY, upperX, centerY);

            paintThumb(g2, lowerX, centerY, activeThumb == LOWER_THUMB);
            paintThumb(g2, upperX, centerY, activeThumb == UPPER_THUMB);

            g2.setFont(new Font("Lexend", Font.PLAIN, 11));
            g2.setColor(new Color(100, 116, 139));
            g2.drawString("%02d:00".formatted(minHour), startX - 2, 50);
            String maxText = "%02d:00".formatted(maxHour + 1);
            int maxWidth = g2.getFontMetrics().stringWidth(maxText);
            g2.drawString(maxText, endX - maxWidth + 2, 50);
            g2.dispose();
        }

        private void paintThumb(Graphics2D g2, int x, int y, boolean active) {
            int radius = active ? thumbRadius + 1 : thumbRadius;
            g2.setColor(new Color(255, 255, 255));
            g2.fillOval(x - radius, y - radius, radius * 2, radius * 2);
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(active ? new Color(21, 128, 61) : new Color(22, 163, 74));
            g2.drawOval(x - radius, y - radius, radius * 2, radius * 2);
        }

        private int nearestThumb(int x) {
            int lowerDistance = Math.abs(x - valueToX(lowerValue));
            int upperDistance = Math.abs(x - valueToX(upperValue));
            return lowerDistance <= upperDistance ? LOWER_THUMB : UPPER_THUMB;
        }

        private void updateActiveThumb(int x) {
            int value = xToValue(x);
            if (activeThumb == LOWER_THUMB) {
                lowerValue = Math.min(value, upperValue);
            } else if (activeThumb == UPPER_THUMB) {
                upperValue = Math.max(value, lowerValue);
            }
            fireRangeChanged();
            repaint();
        }

        private int valueToX(int value) {
            int width = Math.max(1, trackEndX() - trackStartX());
            double ratio = (double) (value - minHour) / (double) (maxHour - minHour);
            return trackStartX() + (int) Math.round(ratio * width);
        }

        private int xToValue(int x) {
            int start = trackStartX();
            int width = Math.max(1, trackEndX() - start);
            double ratio = (double) (x - start) / (double) width;
            int value = minHour + (int) Math.round(ratio * (maxHour - minHour));
            return clamp(value);
        }

        private int trackStartX() {
            return thumbRadius + 4;
        }

        private int trackEndX() {
            return Math.max(trackStartX() + 1, getWidth() - thumbRadius - 4);
        }

        private int clamp(int value) {
            return Math.max(minHour, Math.min(maxHour, value));
        }

        private void fireRangeChanged() {
            if (onRangeChanged != null) {
                onRangeChanged.run();
            }
        }
    }
}
