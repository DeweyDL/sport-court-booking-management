package com.sportcourt.modules.branch.view;

import com.sportcourt.modules.branch.controller.BranchController;
import com.sportcourt.modules.branch.dto.BranchUpdateRequest;
import com.sportcourt.modules.branch.entity.Branch;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

public class BranchChange extends JPanel {
    private static void applyResponsiveWindowSize(JDialog dialog, int baseWidth, int baseHeight) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double widthRatio = screenSize.getWidth() / 1920.0;
        double heightRatio = screenSize.getHeight() / 1080.0;
        double ratio = Math.min(widthRatio, heightRatio);
        if (ratio < 0.8) ratio = 0.8;

        int width = (int) (baseWidth * ratio);
        int height = (int) (baseHeight * ratio);
        dialog.setSize(width, height);
    }
    private final BranchController branchController;
    private final Consumer<String> onSaved;

    private final JTextField maCnField = createDisplayField();
    private final JTextField tenChiNhanhField = createEditableField();
    private final JTextField diaChiField = createEditableField();
    private final JTextField hotlineField = createEditableField();

    private String currentMaCn;
    private JDialog dialog;

    public BranchChange(BranchController branchController, Consumer<String> onSaved) {
        this.branchController = branchController;
        this.onSaved = onSaved;

        setOpaque(true);
        setBackground(new Color(248, 249, 252));
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));
        add(createContent(), BorderLayout.CENTER);
    }

    public void showEditor(Component parent, String maCn) {
        currentMaCn = maCn;
        bindData(maCn);

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
            dialog = new JDialog(owner, "Chỉnh sửa chi nhánh", Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
            dialog.setContentPane(this);
            dialog.setResizable(false);
            dialog.pack();
            applyResponsiveWindowSize(dialog, 560, 580);
        }
        return dialog;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setOpaque(false);
        content.add(createHeader(), BorderLayout.NORTH);

        JScrollPane formScroll = new JScrollPane(createForm());
        formScroll.setBorder(null);
        com.sportcourt.common.style.CrudViewStyle.configureScrollPane(formScroll);
        formScroll.getViewport().setBackground(new Color(248, 249, 252));
        content.add(formScroll, BorderLayout.CENTER);

        content.add(createActions(), BorderLayout.SOUTH);
        return content;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Cập nhật chi nhánh");
        titleLabel.setFont(new Font("Lexend", Font.BOLD, 24));
        titleLabel.setForeground(new Color(30, 41, 59));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Chỉnh sửa các thông tin cơ bản của chi nhánh.");
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
        form.setMaximumSize(new Dimension(520, Integer.MAX_VALUE));

        form.add(createReadOnlyField("Mã chi nhánh", maCnField));
        form.add(Box.createVerticalStrut(17));
        form.add(createEditableField("Tên chi nhánh", tenChiNhanhField));
        form.add(Box.createVerticalStrut(17));
        form.add(createEditableField("Địa chỉ", diaChiField));
        form.add(Box.createVerticalStrut(17));
        form.add(createEditableField("Hotline", hotlineField));
        return form;
    }

    private JPanel createActions() {
        JPanel actions = new JPanel(new GridLayout(1, 2, 10, 0));
        actions.setOpaque(false);

        JButton cancelButton = createPillButton("Hủy", new Color(226, 232, 240), new Color(30, 41, 59), true);
        cancelButton.addActionListener(event -> cancelEdit());

        JButton saveButton = createPillButton("Lưu thay đổi", new Color(29, 78, 216), Color.WHITE, true);
        saveButton.addActionListener(event -> saveChanges());

        actions.add(cancelButton);
        actions.add(saveButton);
        return actions;
    }

    private JPanel createReadOnlyField(String labelText, JTextField field) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Lexend", Font.BOLD, 12));
        label.setForeground(new Color(30, 41, 59));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel wrapper = createFieldWrapper(field, false);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel container = new JPanel();
        container.setOpaque(false);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(label);
        container.add(Box.createVerticalStrut(6));
        container.add(wrapper);
        return container;
    }

    private JPanel createEditableField(String labelText, JTextField field) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Lexend", Font.BOLD, 12));
        label.setForeground(new Color(30, 41, 59));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel wrapper = createFieldWrapper(field, true);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

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

    private JPanel createFieldWrapper(JTextField field, boolean editable) {
        field.setEditable(editable);
        field.setBorder(null);
        field.setOpaque(false);
        field.setFocusable(editable);
        if (!editable) field.setCursor(Cursor.getDefaultCursor());
        field.setFont(new Font("Segoe UI", editable ? Font.PLAIN : Font.BOLD, 15));
        field.setForeground(new Color(31, 41, 55));
        field.setPreferredSize(new Dimension(480, 40));

        Color fillColor = editable ? Color.WHITE : READONLY_BG;
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
        wrapper.setPreferredSize(new Dimension(520, 40));
        wrapper.setMinimumSize(new Dimension(520, 40));
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        wrapper.add(field, BorderLayout.CENTER);
        wrapper.setBorder(new EmptyBorder(0, 12, 0, 12));
        return wrapper;
    }

    private void bindData(String maCn) {
        Branch branch = branchController.getBranchDetail(maCn);
        maCnField.setText(branch.maCn());
        tenChiNhanhField.setText(branch.tenChiNhanh());
        diaChiField.setText(branch.diaChi());
        hotlineField.setText(branch.hotline() == null ? "" : branch.hotline());
    }

    private void saveChanges() {
        if (currentMaCn == null || currentMaCn.isBlank()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy chi nhánh cần sửa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String ten = tenChiNhanhField.getText() == null ? "" : tenChiNhanhField.getText().trim();
        String diaChi = diaChiField.getText() == null ? "" : diaChiField.getText().trim();
        String hotline = hotlineField.getText() == null ? "" : hotlineField.getText().trim();

        if (ten.isEmpty() || diaChi.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên chi nhánh và địa chỉ là bắt buộc.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có muốn lưu thay đổi chi nhánh này không?",
                "Xác nhận lưu",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        BranchUpdateRequest request = new BranchUpdateRequest(currentMaCn, ten, diaChi, hotline.isEmpty() ? null : hotline);

        try {
            branchController.saveBranchChanges(request);
            JOptionPane.showMessageDialog(this, "Đã cập nhật chi nhánh.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            if (dialog != null) {
                dialog.setVisible(false);
            }
            onSaved.accept(currentMaCn);
        } catch (IllegalStateException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getCause() == null ? exception.getMessage() : exception.getCause().getMessage(),
                    "Lỗi cập nhật chi nhánh",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void cancelEdit() {
        if (dialog != null) {
            dialog.setVisible(false);
        }
    }

    private void showAreaList() {
        JOptionPane.showMessageDialog(this, "Chưa có chức năng này.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
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

    private JTextField createEditableField() {
        JTextField textField = new JTextField();
        textField.setBorder(null);
        textField.setOpaque(false);
        textField.setFont(new Font("Lexend", Font.PLAIN, 14));
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
