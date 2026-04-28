package com.sportcourt.modules.area.view;

import com.sportcourt.modules.area.controller.AreaController;
import com.sportcourt.modules.area.enitity.AreaCreateRequest;
import com.sportcourt.modules.area.enitity.SportType;
import com.sportcourt.modules.area.enitity.Court;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// Popup thêm khu vực mới, dùng chung layout với AreaChange
public class AreaAdd extends JPanel {
    private final AreaController areaController;
    private final Consumer<String> onSaved;

    private final JTextField maKvField = createDisplayField();
    private final JTextField maCnField = createDisplayField();
    private final JComboBox<SportType> sportTypeComboBox = new JComboBox<>();
    private final JTextField courtCountField = createDisplayField();

    private JDialog dialog;
    private String generatedMaKv;
    // Khởi tạo list sân con trống, phục vụ cho nút xem danh sách sau này
    private final List<Court> currentCourts = new ArrayList<>();

    public AreaAdd(AreaController areaController, Consumer<String> onSaved) {
        this.areaController = areaController;
        this.onSaved = onSaved;

        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(18, 18, 18, 18));
        add(createContent(), BorderLayout.CENTER);
    }

    public void showCreator(Component parent) {
        prepareCreateForm();
        JDialog popup = ensureDialog(parent);
        popup.setLocationRelativeTo(parent);
        popup.setVisible(true);
    }

    private void prepareCreateForm() {
        generatedMaKv = areaController.generateNextMaKv();
        maKvField.setText(generatedMaKv);
        maCnField.setText(areaController.getDefaultChiNhanhId());

        currentCourts.clear();
        courtCountField.setText("0");

        bindLoaiTheThao();
    }

    private JDialog ensureDialog(Component parent) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        if (dialog == null || dialog.getOwner() != owner) {
            if (dialog != null) {
                dialog.dispose();
            }
            dialog = new JDialog(owner, "Thêm khu vực", Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
            dialog.setContentPane(this);
            dialog.setResizable(false);
            dialog.pack();
        }
        return dialog;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout(0, 18)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 28, 28);
                g2.setColor(new Color(229, 231, 235));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 28, 28);
                g2.dispose();
            }

            @Override
            protected void paintChildren(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setClip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 28, 28));
                super.paintChildren(g2);
                g2.dispose();
            }
        };
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 22, 20, 22));
        content.add(createHeader(), BorderLayout.NORTH);
        content.add(createForm(), BorderLayout.CENTER);
        content.add(createActions(), BorderLayout.SOUTH);
        return content;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Thêm khu vực");
        titleLabel.setFont(new Font("Lexend", Font.BOLD, 22));
        titleLabel.setForeground(new Color(30, 31, 36));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Khởi tạo các thông tin cơ bản cho khu vực mới.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(107, 114, 128));
        subtitleLabel.setBorder(new EmptyBorder(6, 0, 0, 0));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(titleLabel);
        header.add(subtitleLabel);
        return header;
    }

    private JPanel createForm() {
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.setMaximumSize(new Dimension(420, Integer.MAX_VALUE));

        form.add(createReadOnlyField("Mã khu vực", maKvField));
        form.add(Box.createVerticalStrut(17));
        form.add(createReadOnlyField("Mã chi nhánh", maCnField));
        form.add(Box.createVerticalStrut(17));
        form.add(createEditableField("Loại thể thao", sportTypeComboBox));
        form.add(Box.createVerticalStrut(17));
        form.add(createReadOnlyField("Số lượng sân con", courtCountField));
        form.add(Box.createVerticalStrut(18));
        form.add(createSubCourtButton()); // Giữ nguyên nút theo yêu cầu
        return form;
    }

    private JButton createSubCourtButton() {
        JButton button = createPillButton(
                "Xem danh sách các sân con",
                new Color(228, 250, 226),
                new Color(16, 110, 0),
                true
        );
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMinimumSize(new Dimension(420, 44));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        button.setPreferredSize(new Dimension(420, 44));
        button.addActionListener(event -> showSubCourtList());
        return button;
    }

    private JPanel createActions() {
        JPanel actions = new JPanel(new GridLayout(1, 2, 12, 0));
        actions.setOpaque(false);
        actions.setBorder(new EmptyBorder(2, 0, 0, 0));

        JButton cancelButton = createPillButton("Hủy", new Color(229, 231, 235), new Color(31, 41, 55), true);
        cancelButton.addActionListener(event -> cancelCreate());

        JButton saveButton = createPillButton("Tạo khu vực", new Color(16, 110, 0), new Color(228, 250, 226), true);
        saveButton.addActionListener(event -> saveNewKhuVuc());

        actions.add(cancelButton);
        actions.add(saveButton);
        return actions;
    }

    private JPanel createEditableField(String labelText, JComponent editor) {
        JPanel fieldPanel = createFieldPanel();

        JLabel label = createFieldLabel(labelText);
        editor.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        JPanel editorWrapper = createRoundedInputWrapper();
        editorWrapper.setBorder(new EmptyBorder(6, 12, 6, 12));
        editorWrapper.add(editor, BorderLayout.CENTER);

        fieldPanel.add(label);
        fieldPanel.add(Box.createVerticalStrut(6));
        fieldPanel.add(editorWrapper);
        return fieldPanel;
    }

    private JPanel createReadOnlyField(String labelText, JTextField valueField) {
        JPanel fieldPanel = createFieldPanel();

        JPanel wrapper = createRoundedInputWrapper();
        wrapper.setBorder(new EmptyBorder(10, 14, 10, 14));
        wrapper.add(valueField, BorderLayout.CENTER);

        fieldPanel.add(createFieldLabel(labelText));
        fieldPanel.add(Box.createVerticalStrut(6));
        fieldPanel.add(wrapper);
        return fieldPanel;
    }

    private JPanel createFieldPanel() {
        JPanel fieldPanel = new JPanel();
        fieldPanel.setOpaque(false);
        fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.Y_AXIS));
        fieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        fieldPanel.setPreferredSize(new Dimension(420, 68));
        fieldPanel.setMinimumSize(new Dimension(420, 68));
        fieldPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
        return fieldPanel;
    }

    private JLabel createFieldLabel(String labelText) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(new Color(75, 85, 99));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JPanel createRoundedInputWrapper() {
        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(249, 250, 251));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(new Color(203, 213, 225));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
            }
        };
        wrapper.setOpaque(false);
        wrapper.setPreferredSize(new Dimension(420, 40));
        wrapper.setMinimumSize(new Dimension(420, 40));
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        return wrapper;
    }

    private void bindLoaiTheThao() {
        DefaultComboBoxModel<SportType> model = new DefaultComboBoxModel<>();
        for (SportType sportType : areaController.getLoaiTheThaoList()) {
            model.addElement(sportType);
        }
        sportTypeComboBox.setModel(model);
        sportTypeComboBox.setBorder(null);
        sportTypeComboBox.setBackground(new Color(249, 250, 251));

        // Đã bỏ phần Custom UI cho ComboBox ở đây theo yêu cầu
        if (model.getSize() > 0) {
            sportTypeComboBox.setSelectedIndex(0);
        }
    }

    private void saveNewKhuVuc() {
        if (generatedMaKv == null || generatedMaKv.isBlank()) {
            JOptionPane.showMessageDialog(this, "Không thể sinh mã khu vực mới.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SportType selectedSportType = (SportType) sportTypeComboBox.getSelectedItem();
        if (selectedSportType == null) {
            JOptionPane.showMessageDialog(this, "Hãy chọn loại thể thao.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có muốn tạo khu vực này không?",
                "Xác nhận tạo",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        AreaCreateRequest request = new AreaCreateRequest(
                generatedMaKv,
                areaController.getDefaultChiNhanhId(),
                selectedSportType.maTt(),
                0, // Khởi tạo với 0 sân con
                List.of()
        );

        try {
            areaController.createKhuVuc(request);
            JOptionPane.showMessageDialog(this, "Đã thêm khu vực mới.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            if (dialog != null) {
                dialog.setVisible(false);
            }
            onSaved.accept(generatedMaKv);
        } catch (IllegalStateException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getCause() == null ? exception.getMessage() : exception.getCause().getMessage(),
                    "Lỗi thêm khu vực",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void cancelCreate() {
        if (dialog != null) {
            dialog.setVisible(false);
        }
    }

    private void showSubCourtList() {
        // List sân con lúc này luôn rỗng, show thông báo chuẩn như AreaChange
        if (currentCourts.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Khu vực này chưa có sân con nào.", "Danh sách sân con", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
    }

    private JTextField createDisplayField() {
        JTextField textField = new JTextField();
        textField.setEditable(false);
        textField.setBorder(null);
        textField.setOpaque(false);
        textField.setFocusable(false);
        textField.setFont(new Font("Segoe UI", Font.BOLD, 15));
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
        button.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, 14));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 18, 10, 18));
        return button;
    }
}