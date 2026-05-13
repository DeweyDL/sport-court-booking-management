package com.sportcourt.modules.cost.view;

import com.sportcourt.modules.cost.view.CostMockData.AreaOption;
import com.sportcourt.modules.cost.view.CostMockData.CostItem;
import com.sportcourt.modules.cost.view.CostMockData.KhungGioOption;
import com.sportcourt.modules.cost.view.CostMockData.Store;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.math.BigDecimal;
import java.util.function.Consumer;

public class CostChange extends JPanel {
    private final Store store;
    private final Consumer<String> onSaved;

    private final JTextField maBgField = createDisplayField();
    private final JComboBox<AreaOption> areaComboBox = new JComboBox<>();
    private final JComboBox<KhungGioOption> khungGioComboBox = new JComboBox<>();
    private final JTextField startHourField = createDisplayField();
    private final JTextField endHourField = createDisplayField();
    private final JTextField giaField = createEditField();

    private String currentMaBg;
    private JDialog dialog;

    public CostChange(Store store, Consumer<String> onSaved) {
        this.store = store;
        this.onSaved = onSaved;

        setOpaque(true);
        setBackground(new Color(248, 249, 252));
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));
        add(createContent(), BorderLayout.CENTER);
    }

    public void showEditor(Component parent, String maBg) {
        currentMaBg = maBg;
        bindData(maBg);

        JDialog popup = ensureDialog(parent);
        popup.setLocationRelativeTo(parent);
        popup.setVisible(true);
    }

    private void bindData(String maBg) {
        CostItem detail = store.getDetail(maBg);
        if (detail == null) {
            JOptionPane.showMessageDialog(this, "KhÃ´ng tÃ¬m tháº¥y báº£ng giÃ¡.", "ThÃ´ng bÃ¡o", JOptionPane.WARNING_MESSAGE);
            return;
        }
        maBgField.setText(detail.maBg());
        giaField.setText(detail.gia() == null ? "" : detail.gia().toPlainString());

        bindAreaOptions(detail.maKv());
        bindKhungGio(detail);
        updateHoursFromKhungGio();
    }

    private void bindAreaOptions(String selectedMaKv) {
        DefaultComboBoxModel<AreaOption> model = new DefaultComboBoxModel<>();
        AreaOption selected = null;
        for (AreaOption opt : store.getAreaOptions()) {
            model.addElement(opt);
            if (opt != null && opt.maKv() != null && opt.maKv().equals(selectedMaKv)) {
                selected = opt;
            }
        }
        areaComboBox.setModel(model);
        styleComboBox(areaComboBox);

        if (selected != null) {
            areaComboBox.setSelectedItem(selected);
        } else if (model.getSize() > 0) {
            areaComboBox.setSelectedIndex(0);
        }
    }

    private void bindKhungGio(CostItem detail) {
        DefaultComboBoxModel<KhungGioOption> model = new DefaultComboBoxModel<>();
        java.util.List<KhungGioOption> options = store.getKhungGioOptions();
        KhungGioOption selected = null;

        if (options == null || options.isEmpty()) {
            for (int h = 0; h <= 23; h++) {
                KhungGioOption opt = new KhungGioOption(null, h, h + 1);
                model.addElement(opt);
                if (h == detail.gioBatDau()) {
                    selected = opt;
                }
            }
        } else {
            for (KhungGioOption opt : options) {
                model.addElement(opt);
                if (detail != null && opt != null) {
                    if (opt.gioBatDau() == detail.gioBatDau() && opt.gioKetThuc() == detail.gioKetThuc()) {
                        selected = opt;
                    }
                }
            }
        }

        khungGioComboBox.setModel(model);
        styleComboBox(khungGioComboBox);
        if (selected != null) {
            khungGioComboBox.setSelectedItem(selected);
        } else if (model.getSize() > 0) {
            khungGioComboBox.setSelectedIndex(0);
        }
        khungGioComboBox.addActionListener(e -> updateHoursFromKhungGio());
    }

    private void updateHoursFromKhungGio() {
        KhungGioOption selected = (KhungGioOption) khungGioComboBox.getSelectedItem();
        int start = selected == null ? 0 : selected.gioBatDau();
        int end = selected == null ? 1 : selected.gioKetThuc();
        startHourField.setText("%02d:00".formatted(start));
        endHourField.setText("%02d:00".formatted(end));
    }

    private JDialog ensureDialog(Component parent) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        if (dialog == null || dialog.getOwner() != owner) {
            if (dialog != null) {
                dialog.dispose();
            }
            dialog = new JDialog(owner, "Chỉnh sửa bảng giá", Dialog.ModalityType.APPLICATION_MODAL);
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

        JLabel titleLabel = new JLabel("Cập nhật bảng giá");
        titleLabel.setFont(new Font("Lexend", Font.BOLD, 24));
        titleLabel.setForeground(new Color(30, 41, 59));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Chỉnh sửa thông tin khung giờ và giá.");
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
        form.setMaximumSize(new Dimension(420, Integer.MAX_VALUE));

        form.add(createReadOnlyField("Mã bảng giá", maBgField));
        form.add(Box.createVerticalStrut(17));
        form.add(createComboField("Khu vực", areaComboBox));
        form.add(Box.createVerticalStrut(17));
        form.add(createComboField("Khung giờ", khungGioComboBox));
        form.add(Box.createVerticalStrut(17));
        form.add(createReadOnlyField("Giờ bắt đầu", startHourField));
        form.add(Box.createVerticalStrut(17));
        form.add(createReadOnlyField("Giờ kết thúc", endHourField));
        form.add(Box.createVerticalStrut(17));
        form.add(createEditField("Giá (VNĐ)", giaField));
        return form;
    }

    private JPanel createActions() {
        JPanel actions = new JPanel(new GridLayout(1, 2, 10, 0));
        actions.setOpaque(false);

        JButton cancel = createPillButton("Hủy", new Color(226, 232, 240), new Color(30, 41, 59), true);
        cancel.addActionListener(e -> closeEditor());

        JButton save = createPillButton("Lưu thay đổi", new Color(29, 78, 216), Color.WHITE, true);
        save.addActionListener(e -> saveChanges());

        actions.add(cancel);
        actions.add(save);
        return actions;
    }

    private JPanel createReadOnlyField(String labelText, JTextField field) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Lexend", Font.BOLD, 12));
        label.setForeground(new Color(30, 41, 59));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setFont(new Font("Lexend", Font.BOLD, 14));
        field.setCursor(Cursor.getDefaultCursor());
        JPanel wrapper = createInputWrapper(true);
        wrapper.add(field, BorderLayout.CENTER);

        JPanel container = new JPanel();
        container.setOpaque(false);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(label);
        container.add(Box.createVerticalStrut(6));
        container.add(wrapper);
        return container;
    }

    private JPanel createEditField(String labelText, JTextField field) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Lexend", Font.BOLD, 12));
        label.setForeground(new Color(30, 41, 59));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel wrapper = createInputWrapper();
        wrapper.add(field, BorderLayout.CENTER);

        JPanel container = new JPanel();
        container.setOpaque(false);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(label);
        container.add(Box.createVerticalStrut(6));
        container.add(wrapper);
        return container;
    }

    private JPanel createComboField(String labelText, JComponent component) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Lexend", Font.BOLD, 12));
        label.setForeground(new Color(30, 41, 59));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel wrapper = createInputWrapper();
        wrapper.add(component, BorderLayout.CENTER);

        JPanel container = new JPanel();
        container.setOpaque(false);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(label);
        container.add(Box.createVerticalStrut(6));
        container.add(wrapper);
        return container;
    }

    private static final Color READONLY_BG = new Color(241, 245, 249);

    private JPanel createInputWrapper() {
        return createInputWrapper(false);
    }

    private JPanel createInputWrapper(boolean readOnly) {
        Color fillColor = readOnly ? READONLY_BG : Color.WHITE;
        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(fillColor);
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
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setBorder(new EmptyBorder(0, 2, 0, 2));
                return label;
            }
        });

        comboBox.setUI(new BasicComboBoxUI() {
            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                // Do not paint any background/border here; wrapper handles the rounded box.
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

    private void saveChanges() {
        if (currentMaBg == null || currentMaBg.isBlank()) {
            JOptionPane.showMessageDialog(this, "Không xác định được bảng giá.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        AreaOption selectedArea = (AreaOption) areaComboBox.getSelectedItem();
        if (selectedArea == null) {
            JOptionPane.showMessageDialog(this, "Hãy chọn khu vực.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        KhungGioOption selectedKhungGio = (KhungGioOption) khungGioComboBox.getSelectedItem();
        int gioBatDau = selectedKhungGio == null ? 0 : selectedKhungGio.gioBatDau();
        int gioKetThuc = selectedKhungGio == null ? 1 : selectedKhungGio.gioKetThuc();

        BigDecimal gia;
        try {
            gia = new BigDecimal(giaField.getText().trim().replace(",", ""));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Giá không hợp lệ.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có muốn lưu thay đổi không?",
                "Xác nhận lưu",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            store.update(new CostItem(
                    currentMaBg,
                    selectedArea.maKv(),
                    selectedKhungGio == null ? null : selectedKhungGio.maKg(),
                    gioBatDau,
                    gioKetThuc,
                    gia,
                    false,
                    java.time.LocalDateTime.now()
            ));
            JOptionPane.showMessageDialog(this, "Đã cập nhật bảng giá.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            if (dialog != null) {
                dialog.setVisible(false);
            }
            onSaved.accept(currentMaBg);
        } catch (IllegalStateException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getCause() == null ? exception.getMessage() : exception.getCause().getMessage(),
                    "Lỗi cập nhật bảng giá",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void closeEditor() {
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
        textField.setFont(new Font("Lexend", Font.BOLD, 14));
        textField.setForeground(new Color(31, 41, 55));
        return textField;
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
}
