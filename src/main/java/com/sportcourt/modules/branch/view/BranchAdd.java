package com.sportcourt.modules.branch.view;

import com.sportcourt.modules.branch.controller.BranchController;
import com.sportcourt.modules.branch.dto.BranchCreateRequest;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Consumer;

public class BranchAdd extends JPanel {
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
    

    private JDialog dialog;
    private String generatedMaCn;

    public BranchAdd(BranchController branchController, Consumer<String> onSaved) {
        this.branchController = branchController;
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
        generatedMaCn = branchController.generateNextMaCn();
        maCnField.setText(generatedMaCn);
        tenChiNhanhField.setText("");
        diaChiField.setText("");
        hotlineField.setFont(new Font("Lexend", Font.BOLD, 16));
    }

    private JDialog ensureDialog(Component parent) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        if (dialog == null || dialog.getOwner() != owner) {
            if (dialog != null) {
                dialog.dispose();
            }
            dialog = new JDialog(owner, "Thêm chi nhánh", Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
            dialog.setContentPane(this);
            dialog.setResizable(false);
            dialog.pack();
            applyResponsiveWindowSize(dialog, 560, dialog.getHeight());
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

        JLabel titleLabel = new JLabel("Thêm chi nhánh");
        titleLabel.setFont(new Font("Lexend", Font.BOLD, 22));
        titleLabel.setForeground(new Color(30, 31, 36));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Khởi tạo các thông tin cơ bản cho chi nhánh mới.");
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
        form.setMaximumSize(new Dimension(520, Integer.MAX_VALUE));

        // Các hàm này sẽ được định nghĩa bên dưới
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
        // Sử dụng GridBagLayout để kiểm soát vị trí chính xác
        JPanel actions = new JPanel(new GridBagLayout());
        actions.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();

        // Thiết lập khoảng cách giữa các thành phần (padding)
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH; // Cho phép component giãn ra
        gbc.weightx = 1.0; // Ưu tiên chiếm không gian chiều ngang

        // --- Hàng 1: Nút "Xem danh sách các sân con" ---
        JButton areaButton = createPillButton("Xem danh sách các khu vực", new Color(220, 252, 231), new Color(21, 128, 61), true);
        areaButton.addActionListener(event -> showAreaList());

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; // Chiếm 2 cột (toàn bộ hàng)
        actions.add(areaButton, gbc);

        // --- Hàng 2: Hai nút nằm cạnh nhau ---
        gbc.gridwidth = 1; // Trả về mặc định chiếm 1 cột
        gbc.gridy = 1; // Xuống hàng thứ 2

        // Nút "Hủy"
        JButton cancelButton = createPillButton("Hủy thay đổi", new Color(229, 231, 235), new Color(31, 41, 55), true);
        cancelButton.addActionListener(event -> cancelCreate());
        gbc.gridx = 0;
        actions.add(cancelButton, gbc);

        // Nút "Tạo khu vực"
        // Sử dụng màu xanh đậm hơn như trong ảnh
        JButton saveButton = createPillButton("Lưu thay đổi", new Color(16, 110, 0), Color.WHITE, true);
        saveButton.addActionListener(event -> saveNewBranch());
        gbc.gridx = 1;
        actions.add(saveButton, gbc);

        return actions;
    }

    private JPanel createReadOnlyField(String labelText, JTextField field) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(new Color(107, 114, 128));
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
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(new Color(107, 114, 128));
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

    private JPanel createFieldWrapper(JTextField field, boolean editable) {
        field.setEditable(editable);
        field.setBorder(null);
        field.setOpaque(false);
        field.setFocusable(editable);
        field.setFont(new Font("Segoe UI", editable ? Font.PLAIN : Font.BOLD, 15));
        field.setForeground(new Color(31, 41, 55));
        field.setPreferredSize(new Dimension(480, 40));

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
        wrapper.setPreferredSize(new Dimension(520, 40));
        wrapper.setMinimumSize(new Dimension(520, 40));
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        wrapper.add(field, BorderLayout.CENTER);
        wrapper.setBorder(new EmptyBorder(0, 12, 0, 12));
        return wrapper;
    }

    private void saveNewBranch() {
        if (generatedMaCn == null || generatedMaCn.isBlank()) {
            JOptionPane.showMessageDialog(this, "Không thể sinh mã chi nhánh mới.", "Thông báo", JOptionPane.WARNING_MESSAGE);
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
                "Bạn có muốn tạo chi nhánh này không?",
                "Xác nhận tạo",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        BranchCreateRequest request = new BranchCreateRequest(generatedMaCn, ten, diaChi, hotline.isEmpty() ? null : hotline);

        try {
            branchController.createBranch(request);
            JOptionPane.showMessageDialog(this, "Đã thêm chi nhánh mới.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            if (dialog != null) {
                dialog.setVisible(false);
            }
            onSaved.accept(generatedMaCn);
        } catch (IllegalStateException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getCause() == null ? exception.getMessage() : exception.getCause().getMessage(),
                    "Lỗi thêm chi nhánh",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void cancelCreate() {
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
        textField.setFont(new Font("Segoe UI", Font.BOLD, 15));
        textField.setForeground(new Color(31, 41, 55));
        return textField;
    }

    private JTextField createEditableField() {
        JTextField textField = new JTextField();
        textField.setBorder(null);
        textField.setOpaque(false);
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
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
