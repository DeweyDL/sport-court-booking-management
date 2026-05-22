package com.sportcourt.modules.area.view;

import com.sportcourt.modules.area.controller.AreaController;
import com.sportcourt.modules.area.enitity.Area;
import com.sportcourt.modules.area.dto.AreaUpdateRequest;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

public class AreaChange extends JPanel {
    private final AreaController areaController;
    private final Consumer<String> onSaved;

    private final JTextField maKvField = createDisplayField();
    private final JComboBox<Area.ChiNhanhOption> chiNhanhComboBox = new JComboBox<>();
    private final JComboBox<Area.SportTypeOption> sportTypeComboBox = new JComboBox<>();
    private final JTextField courtCountField = createDisplayField();

    private String currentMaKv;
    private JDialog dialog;

    public AreaChange(AreaController areaController, Consumer<String> onSaved) {
        this.areaController = areaController;
        this.onSaved = onSaved;

        setOpaque(true);
        setBackground(new Color(248, 249, 252));
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));
        add(createContent(), BorderLayout.CENTER);
    }

    public void showEditor(Component parent, String maKv) {
        currentMaKv = maKv;
        bindData(maKv);

        JDialog popup = ensureDialog(parent);
        popup.setLocationRelativeTo(parent);
        popup.setVisible(true);
    }

    private JDialog ensureDialog(Component parent) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        if (dialog == null || dialog.getOwner() != owner) {
            if (dialog != null) {
                dialog.dispose();
            }
            dialog = new JDialog(owner, "Chỉnh sửa khu vực", Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
            dialog.setContentPane(this);
            dialog.setResizable(false);
            dialog.pack();
            dialog.setSize(Math.max(dialog.getWidth(), 560), dialog.getHeight());
            dialog.setMinimumSize(new Dimension(560, dialog.getHeight()));
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

        JLabel titleLabel = new JLabel("Cập nhật khu vực");
        titleLabel.setFont(new Font("Lexend", Font.BOLD, 24));
        titleLabel.setForeground(new Color(30, 41, 59));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Chỉnh sửa các thông tin cơ bản của khu vực.");
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

        form.add(createReadOnlyField("Mã khu vực", maKvField));
        form.add(Box.createVerticalStrut(17));
        form.add(createEditableField("Chi nhánh", chiNhanhComboBox));
        form.add(Box.createVerticalStrut(17));
        form.add(createEditableField("Loại thể thao", sportTypeComboBox));
        form.add(Box.createVerticalStrut(17));
        form.add(createReadOnlyField("Số lượng sân con", courtCountField));
        return form;
    }

    private JPanel createActions() {
        JPanel actions = new JPanel(new GridLayout(1, 2, 10, 0));
        actions.setOpaque(false);
        actions.setBorder(new EmptyBorder(2, 0, 0, 0));

        JButton cancelButton = createPillButton("Hủy", new Color(226, 232, 240), new Color(30, 41, 59), true);
        cancelButton.addActionListener(event -> cancelEdit());

        JButton saveButton = createPillButton("Lưu thay đổi", new Color(29, 78, 216), Color.WHITE, true);
        saveButton.addActionListener(event -> saveChanges());

        actions.add(cancelButton);
        actions.add(saveButton);
        return actions;
    }

    private JPanel createEditableField(String labelText, JComponent editor) {
        JPanel fieldPanel = createFieldPanel();

        JLabel label = createFieldLabel(labelText);
        editor.setFont(new Font("Lexend", Font.PLAIN, 14));

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

        valueField.setFont(new Font("Lexend", Font.BOLD, 14));
        valueField.setCursor(Cursor.getDefaultCursor());
        JPanel wrapper = createRoundedInputWrapper(true);
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
        label.setFont(new Font("Lexend", Font.BOLD, 12));
        label.setForeground(new Color(30, 41, 59));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private static final Color READONLY_BG = new Color(241, 245, 249);

    private JPanel createRoundedInputWrapper() {
        return createRoundedInputWrapper(false);
    }

    private JPanel createRoundedInputWrapper(boolean readOnly) {
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
        wrapper.setPreferredSize(new Dimension(420, 40));
        wrapper.setMinimumSize(new Dimension(420, 40));
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        return wrapper;
    }

    private void bindData(String maKv) {
        Area area = areaController.getKhuVucDetail(maKv);
        maKvField.setText(area.maKv());
        courtCountField.setText(String.valueOf(area.soLuongSan()));
        bindChiNhanh(area.maCn());
        bindLoaiTheThao(area.maTt());
    }

    private void bindChiNhanh(String selectedMaCn) {
        DefaultComboBoxModel<Area.ChiNhanhOption> model = new DefaultComboBoxModel<>();
        for (Area.ChiNhanhOption chiNhanh : areaController.getChiNhanhList()) {
            model.addElement(chiNhanh);
        }
        chiNhanhComboBox.setModel(model);
        chiNhanhComboBox.setBorder(null);
        chiNhanhComboBox.setBackground(new Color(249, 250, 251));
        for (int i = 0; i < model.getSize(); i++) {
            if (model.getElementAt(i).maCn().equals(selectedMaCn)) {
                chiNhanhComboBox.setSelectedIndex(i);
                break;
            }
        }
    }

    private void bindLoaiTheThao(String selectedMaTt) {
        DefaultComboBoxModel<Area.SportTypeOption> model = new DefaultComboBoxModel<>();
        for (Area.SportTypeOption sportType : areaController.getLoaiTheThaoList()) {
            model.addElement(sportType);
        }
        sportTypeComboBox.setModel(model);
        sportTypeComboBox.setBorder(null);
        sportTypeComboBox.setBackground(new Color(249, 250, 251));

        for (int index = 0; index < model.getSize(); index++) {
            Area.SportTypeOption sportType = model.getElementAt(index);
            if (sportType != null && sportType.maTt().equals(selectedMaTt)) {
                sportTypeComboBox.setSelectedIndex(index);
                break;
            }
        }

    }

    private void saveChanges() {
        if (currentMaKv == null || currentMaKv.isBlank()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy khu vực cần sửa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Area.ChiNhanhOption selectedChiNhanh = (Area.ChiNhanhOption) chiNhanhComboBox.getSelectedItem();
        if (selectedChiNhanh == null) {
            JOptionPane.showMessageDialog(this, "Hãy chọn chi nhánh.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Area.SportTypeOption selectedSportType = (Area.SportTypeOption) sportTypeComboBox.getSelectedItem();
        if (selectedSportType == null) {
            JOptionPane.showMessageDialog(this, "Hãy chọn loại thể thao.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có muốn lưu thay đổi khu vực này không?",
                "Xác nhận lưu",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        AreaUpdateRequest request = new AreaUpdateRequest(currentMaKv, selectedChiNhanh.maCn(), selectedSportType.maTt());

        try {
            areaController.saveKhuVucChanges(request);
            JOptionPane.showMessageDialog(this, "Đã cập nhật khu vực.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            if (dialog != null) {
                dialog.setVisible(false);
            }
            onSaved.accept(currentMaKv);
        } catch (IllegalStateException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getCause() == null ? exception.getMessage() : exception.getCause().getMessage(),
                    "Lỗi cập nhật khu vực",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void cancelEdit() {
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
