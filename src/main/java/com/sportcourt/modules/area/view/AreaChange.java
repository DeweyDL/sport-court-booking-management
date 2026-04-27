package com.sportcourt.modules.area.view;

import com.sportcourt.modules.area.controller.KhuVucController;
import com.sportcourt.modules.area.enitity.KhuVuc;
import com.sportcourt.modules.area.enitity.KhuVucUpdateRequest;
import com.sportcourt.modules.area.enitity.LoaiTheThao;
import com.sportcourt.modules.area.enitity.SanCon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// Popup sửa khu vực nhỏ gọn, chỉ cho phép cập nhật loại thể thao.
public class AreaChange extends JPanel {
    private final KhuVucController khuVucController;
    private final Consumer<String> onSaved;

    private final JTextField maKvField = createDisplayField();
    private final JTextField maCnField = createDisplayField();
    private final JComboBox<LoaiTheThao> sportTypeComboBox = new JComboBox<>();
    private final JTextField courtCountField = createDisplayField();

    private String currentMaKv;
    private JDialog dialog;
    private final List<SanCon> currentSanCons = new ArrayList<>();

    public AreaChange(KhuVucController khuVucController, Consumer<String> onSaved) {
        this.khuVucController = khuVucController;
        this.onSaved = onSaved;

        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(18, 18, 18, 18));
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

        JLabel titleLabel = new JLabel("Cập nhật khu vực");
        titleLabel.setFont(new Font("Lexend", Font.BOLD, 22));
        titleLabel.setForeground(new Color(30, 31, 36));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Chỉnh sửa các thông tin cơ bản của khu vực.");
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
        //form.setPreferredSize(new Dimension(420, Integer.MAX_VALUE));
        form.setMaximumSize(new Dimension(420, Integer.MAX_VALUE));

        form.add(createReadOnlyField("Mã khu vực", maKvField));
        form.add(Box.createVerticalStrut(17));
        form.add(createReadOnlyField("Mã chi nhánh", maCnField));
        form.add(Box.createVerticalStrut(17));
        form.add(createEditableField("Loại thể thao", sportTypeComboBox));
        form.add(Box.createVerticalStrut(17));
        form.add(createReadOnlyField("Số lượng sân con", courtCountField));
        form.add(Box.createVerticalStrut(18));
        form.add(createSubCourtButton());
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
        cancelButton.addActionListener(event -> cancelEdit());

        JButton saveButton = createPillButton("Lưu thay đổi", new Color(16, 110, 0), new Color(228, 250, 226), true);
        saveButton.addActionListener(event -> saveChanges());

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

    private void bindData(String maKv) {
        KhuVuc khuVuc = khuVucController.getKhuVucDetail(maKv);
        currentSanCons.clear();
        currentSanCons.addAll(khuVucController.getSanConList(maKv));
        maKvField.setText(khuVuc.maKv());
        maCnField.setText(khuVuc.maCn());
        courtCountField.setText(String.valueOf(khuVuc.soLuongSan()));
        bindLoaiTheThao(khuVuc.maTt());
    }

    private void bindLoaiTheThao(String selectedMaTt) {
        DefaultComboBoxModel<LoaiTheThao> model = new DefaultComboBoxModel<>();
        for (LoaiTheThao loaiTheThao : khuVucController.getLoaiTheThaoList()) {
            model.addElement(loaiTheThao);
        }
        sportTypeComboBox.setModel(model);
        sportTypeComboBox.setBorder(null);
        //sportTypeComboBox.setOpaque(true);
        sportTypeComboBox.setBackground(new Color(249, 250, 251));

        for (int index = 0; index < model.getSize(); index++) {
            LoaiTheThao loaiTheThao = model.getElementAt(index);
            if (loaiTheThao != null && loaiTheThao.maTt().equals(selectedMaTt)) {
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

        LoaiTheThao selectedLoaiTheThao = (LoaiTheThao) sportTypeComboBox.getSelectedItem();
        if (selectedLoaiTheThao == null) {
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

        KhuVucUpdateRequest request = new KhuVucUpdateRequest(currentMaKv, selectedLoaiTheThao.maTt());

        try {
            khuVucController.saveKhuVucChanges(request);
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

    private void showSubCourtList() {
        if (currentSanCons.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Khu vực này chưa có sân con nào.", "Danh sách sân con", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder builder = new StringBuilder();
        for (SanCon sanCon : currentSanCons) {
            builder.append("- ")
                    .append(sanCon.maSan())
                    .append(" | ")
                    .append(sanCon.trangThai())
                    .append('\n');
        }

        JOptionPane.showMessageDialog(this, builder.toString(), "Danh sách sân con", JOptionPane.INFORMATION_MESSAGE);
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