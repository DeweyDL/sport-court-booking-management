package com.sportcourt.modules.area.view;

import com.sportcourt.modules.area.controller.KhuVucController;
import com.sportcourt.modules.area.enitity.KhuVucCreateRequest;
import com.sportcourt.modules.area.enitity.LoaiTheThao;
import com.sportcourt.modules.area.enitity.SanCon;
import com.sportcourt.modules.area.enitity.SanConDraft;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// Màn thêm khu vực mới
public class ThemKhuVuc extends JPanel {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String ACTIVE_STATUS = "\u0110ANG HO\u1ea0T \u0110\u1ed8NG";
    private static final String MAINTENANCE_STATUS = "B\u1ea2O TR\u00cc";

    private final KhuVucController khuVucController;
    private final Runnable onBackToList;
    private final Consumer<String> onSaved;

    private final JTextField maKvField = new JTextField();
    private final JComboBox<LoaiTheThao> sportTypeComboBox = new JComboBox<>();
    private final JLabel courtCountValueLabel = new JLabel("0");
    private final JLabel footerLabel = new JLabel("Đang hiển thị 0 sân");
    private final JPanel tableBodyPanel = new JPanel();
    private final AreaImagePreviewPanel imagePreviewPanel = new AreaImagePreviewPanel();

    private Path selectedImagePath;
    private final List<SanConDraft> newSanCons = new ArrayList<>();

    public ThemKhuVuc(KhuVucController khuVucController, Runnable onBackToList, Consumer<String> onSaved) {
        this.khuVucController = khuVucController;
        this.onBackToList = onBackToList;
        this.onSaved = onSaved;

        setLayout(new BorderLayout(0, 18));
        setBackground(new Color(247, 247, 251));
        setBorder(new EmptyBorder(10, 0, 0, 0));

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);
    }

    public void prepareCreateForm() {
        maKvField.setText("");
        selectedImagePath = null;
        newSanCons.clear();
        imagePreviewPanel.clearImage();
        bindLoaiTheThao();
        updateCourtCountLabel();
        renderSanConTable();
    }

    private JPanel createHeader() {
        JPanel headerWrapper = new JPanel();
        headerWrapper.setLayout(new BoxLayout(headerWrapper, BoxLayout.Y_AXIS));
        headerWrapper.setOpaque(false);

        JLabel backLabel = new JLabel("< Quay lại trang Quản lý khu vực");
        backLabel.setFont(new Font("Lexend", Font.PLAIN, 15));
        backLabel.setForeground(new Color(58, 134, 45));
        backLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onBackToList.run();
            }
        });

        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        backRow.setOpaque(false);
        backRow.add(backLabel);

        JLabel titleLabel = new JLabel("THÊM KHU VỰC");
        titleLabel.setFont(new Font("Lexend", Font.BOLD, 30));
        titleLabel.setForeground(new Color(30, 31, 36));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.setBorder(new EmptyBorder(14, 0, 0, 0));
        titleRow.add(titleLabel, BorderLayout.WEST);

        headerWrapper.add(backRow);
        headerWrapper.add(titleRow);
        return headerWrapper;
    }

    private JPanel createMainContent() {
        JPanel mainPanel = new JPanel(new BorderLayout(28, 0));
        mainPanel.setOpaque(false);
        mainPanel.add(createBasicInfoCard(), BorderLayout.WEST);
        mainPanel.add(createSanConSection(), BorderLayout.CENTER);
        return mainPanel;
    }

    private JPanel createBasicInfoCard() {
        JPanel basicInfoCard = new JPanel();
        basicInfoCard.setLayout(new BoxLayout(basicInfoCard, BoxLayout.Y_AXIS));
        basicInfoCard.setBackground(new Color(241, 242, 246));
        // Đã giảm viền trên và dưới để nhường chỗ cho nút Lưu
        basicInfoCard.setBorder(new EmptyBorder(16, 32, 16, 32));
        basicInfoCard.setPreferredSize(new Dimension(440, 0));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.setMaximumSize(new Dimension(360, Integer.MAX_VALUE));

        JLabel cardTitle = new JLabel("Thông tin cơ bản");
        cardTitle.setFont(new Font("Lexend", Font.BOLD, 18));
        cardTitle.setForeground(new Color(35, 37, 43));
        cardTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel infoHintLabel = new JLabel("Nhập thông tin khu vực mới và danh sách sân con.");
        infoHintLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoHintLabel.setForeground(new Color(103, 112, 133));
        infoHintLabel.setBorder(new EmptyBorder(6, 0, 14, 0));
        infoHintLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel imageWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        imageWrapper.setOpaque(false);
        imageWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        imageWrapper.setMaximumSize(new Dimension(360, 240));
        imageWrapper.add(imagePreviewPanel);

        JButton uploadButton = createPillButton("TẢI ẢNH TỪ MÁY", new Color(233, 244, 255), new Color(20, 81, 163), true);
        uploadButton.setPreferredSize(new Dimension(300, 44));
        uploadButton.setMaximumSize(new Dimension(300, 44));
        uploadButton.setMinimumSize(new Dimension(300, 44));
        uploadButton.setBorder(new EmptyBorder(10, 28, 10, 28));
        uploadButton.addActionListener(event -> chooseAreaImage());

        JPanel uploadButtonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        uploadButtonRow.setOpaque(false);
        uploadButtonRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        uploadButtonRow.setMaximumSize(new Dimension(360, 44));
        uploadButtonRow.add(uploadButton);

        JPanel maKvFieldPanel = createTextFieldBlock("MÃ KHU VỰC", maKvField);
        JPanel sportTypeField = createEditableField("LOẠI THỂ THAO", sportTypeComboBox);
        JPanel courtCountField = createReadOnlyField("SỐ LƯỢNG SÂN CON", courtCountValueLabel);

        JButton saveButton = createPillButton("LƯU KHU VỰC", new Color(57, 255, 20), new Color(26, 42, 8), true);
        saveButton.setPreferredSize(new Dimension(300, 48));
        saveButton.setMaximumSize(new Dimension(300, 48));
        saveButton.setMinimumSize(new Dimension(300, 48));
        saveButton.setBorder(new EmptyBorder(14, 40, 14, 40));
        saveButton.addActionListener(event -> saveNewKhuVuc());

        JPanel saveButtonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        saveButtonRow.setOpaque(false);
        saveButtonRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveButtonRow.setMaximumSize(new Dimension(360, 48));
        saveButtonRow.add(saveButton);

        // Đã giảm khoảng cách (Vertical Strut) giữa các phần tử
        contentPanel.add(cardTitle);
        contentPanel.add(infoHintLabel);
        contentPanel.add(imageWrapper);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(uploadButtonRow);
        contentPanel.add(Box.createVerticalStrut(16));
        contentPanel.add(maKvFieldPanel);
        contentPanel.add(Box.createVerticalStrut(12));
        contentPanel.add(sportTypeField);
        contentPanel.add(Box.createVerticalStrut(12));
        contentPanel.add(courtCountField);

        basicInfoCard.add(Box.createVerticalGlue());
        basicInfoCard.add(contentPanel);
        basicInfoCard.add(Box.createVerticalStrut(16));
        basicInfoCard.add(saveButtonRow);
        basicInfoCard.add(Box.createVerticalGlue());
        return basicInfoCard;
    }

    private JPanel createTextFieldBlock(String labelText, JTextField textField) {
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textField.setBorder(null);
        textField.setOpaque(false);
        return createEditableField(labelText, textField);
    }

    private JPanel createEditableField(String labelText, JComponent editor) {
        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.Y_AXIS));
        fieldPanel.setOpaque(false);
        fieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Đã giảm chiều cao ô nhập liệu từ 84 xuống 74
        fieldPanel.setMaximumSize(new Dimension(360, 74));
        fieldPanel.setPreferredSize(new Dimension(360, 74));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(92, 102, 77));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel editorWrapper = new JPanel(new BorderLayout());
        editorWrapper.setBackground(Color.WHITE);
        editorWrapper.setBorder(new CompoundBorder(
                new MatteBorder(1, 1, 1, 1, new Color(229, 231, 235)),
                new EmptyBorder(6, 12, 6, 12)
        ));
        editorWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        editorWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        editorWrapper.add(editor, BorderLayout.CENTER);

        fieldPanel.add(label);
        fieldPanel.add(Box.createVerticalStrut(6));
        fieldPanel.add(editorWrapper);
        return fieldPanel;
    }

    private JPanel createReadOnlyField(String labelText, JLabel valueLabel) {
        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.Y_AXIS));
        fieldPanel.setOpaque(false);
        fieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Đã giảm chiều cao ô nhập liệu từ 84 xuống 74
        fieldPanel.setMaximumSize(new Dimension(360, 74));
        fieldPanel.setPreferredSize(new Dimension(360, 74));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(92, 102, 77));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        valueLabel.setForeground(new Color(31, 41, 55));
        valueLabel.setHorizontalAlignment(SwingConstants.LEFT);

        JPanel valueWrapper = new JPanel(new BorderLayout());
        valueWrapper.setBackground(Color.WHITE);
        valueWrapper.setBorder(new CompoundBorder(
                new MatteBorder(1, 1, 1, 1, new Color(229, 231, 235)),
                new EmptyBorder(10, 16, 10, 16)
        ));
        valueWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        valueWrapper.add(valueLabel, BorderLayout.CENTER);

        fieldPanel.add(label);
        fieldPanel.add(Box.createVerticalStrut(6));
        fieldPanel.add(valueWrapper);
        return fieldPanel;
    }

    private JPanel createSanConSection() {
        JPanel sectionPanel = new JPanel(new BorderLayout(0, 14));
        sectionPanel.setOpaque(false);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("Danh sách sân con");
        title.setFont(new Font("Lexend", Font.BOLD, 18));
        title.setForeground(new Color(35, 37, 43));

        JLabel subtitle = new JLabel("Thêm sân con mới, số lượng sân sẽ tự động cập nhật.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(107, 114, 128));
        subtitle.setBorder(new EmptyBorder(6, 0, 0, 0));

        titlePanel.add(title);
        titlePanel.add(subtitle);

        JButton addButton = createPillButton("+ Thêm sân con", new Color(32, 180, 7), Color.WHITE, true);
        addButton.addActionListener(event -> openAddSanConDialog());

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(addButton, BorderLayout.EAST);

        JPanel tableCard = createRoundedTableCard();
        tableCard.add(createSanConHeader(), BorderLayout.NORTH);

        tableBodyPanel.setLayout(new BoxLayout(tableBodyPanel, BoxLayout.Y_AXIS));
        tableBodyPanel.setBackground(Color.WHITE);
        tableCard.add(tableBodyPanel, BorderLayout.CENTER);
        tableCard.add(createFooter(), BorderLayout.SOUTH);

        sectionPanel.add(headerPanel, BorderLayout.NORTH);
        sectionPanel.add(tableCard, BorderLayout.CENTER);
        return sectionPanel;
    }

    private JPanel createRoundedTableCard() {
        JPanel tableCard = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 42, 42);
                g2.setColor(new Color(236, 236, 239));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 42, 42);
                g2.dispose();
            }

            @Override
            protected void paintChildren(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Shape shape = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 42, 42);
                g2.setClip(shape);
                super.paintChildren(g2);
                g2.dispose();
            }
        };
        tableCard.setOpaque(false);
        tableCard.setBackground(Color.WHITE);
        return tableCard;
    }

    private JPanel createSanConHeader() {
        JPanel headerPanel = new JPanel(new GridLayout(1, 4, 0, 0));
        headerPanel.setBackground(new Color(241, 242, 246));
        headerPanel.setBorder(new EmptyBorder(16, 26, 16, 26));
        headerPanel.add(createHeaderLabel("MÃ SÂN"));
        headerPanel.add(createHeaderLabel("TRẠNG THÁI"));
        headerPanel.add(createHeaderLabel("NGÀY TẠO"));
        headerPanel.add(createHeaderLabel("THAO TÁC"));
        return headerPanel;
    }

    private JPanel createFooter() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(new Color(246, 246, 248));
        footerPanel.setBorder(new EmptyBorder(18, 22, 18, 22));

        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        footerLabel.setForeground(new Color(107, 114, 128));
        footerPanel.add(footerLabel, BorderLayout.WEST);
        return footerPanel;
    }

    private JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(94, 103, 82));
        return label;
    }

    private void bindLoaiTheThao() {
        DefaultComboBoxModel<LoaiTheThao> model = new DefaultComboBoxModel<>();
        for (LoaiTheThao loaiTheThao : khuVucController.getLoaiTheThaoList()) {
            model.addElement(loaiTheThao);
        }
        sportTypeComboBox.setModel(model);
        sportTypeComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sportTypeComboBox.setBorder(null);
        sportTypeComboBox.setOpaque(false);
    }

    private void renderSanConTable() {
        tableBodyPanel.removeAll();

        if (newSanCons.isEmpty()) {
            tableBodyPanel.add(createEmptyRow());
        } else {
            for (SanConDraft sanConDraft : newSanCons) {
                tableBodyPanel.add(createSanConRow(new SanCon(sanConDraft.maSan(), maKvField.getText().trim(), sanConDraft.trangThai(), null)));
            }
        }

        footerLabel.setText("Đang hiển thị " + newSanCons.size() + " sân");
        tableBodyPanel.revalidate();
        tableBodyPanel.repaint();
    }

    private JPanel createSanConRow(SanCon sanCon) {
        JPanel rowPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        rowPanel.setBackground(Color.WHITE);
        rowPanel.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(236, 236, 239)),
                new EmptyBorder(18, 26, 18, 26)
        ));
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));

        JLabel maSanLabel = new JLabel(sanCon.maSan());
        maSanLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        maSanLabel.setForeground(new Color(43, 47, 55));

        JPanel statusCell = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statusCell.setOpaque(false);
        statusCell.add(createStatusPill(sanCon.trangThai()));

        JLabel createdAtLabel = new JLabel(formatDate(sanCon.createdAt()));
        createdAtLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        createdAtLabel.setForeground(new Color(107, 114, 128));

        JPanel actionCell = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actionCell.setOpaque(false);

        JButton detailButton = createMiniActionButton("Xem chi tiết", new Color(239, 246, 255), new Color(29, 78, 216));
        detailButton.addActionListener(event -> showSanConDetail(sanCon));

        JButton deleteButton = createMiniActionButton("Xóa", new Color(254, 242, 242), new Color(220, 38, 38));
        deleteButton.addActionListener(event -> removeSanCon(sanCon.maSan()));

        actionCell.add(detailButton);
        actionCell.add(deleteButton);

        rowPanel.add(maSanLabel);
        rowPanel.add(statusCell);
        rowPanel.add(createdAtLabel);
        rowPanel.add(actionCell);
        return rowPanel;
    }

    private JPanel createEmptyRow() {
        JPanel rowPanel = new JPanel(new BorderLayout());
        rowPanel.setBackground(Color.WHITE);
        rowPanel.setBorder(new EmptyBorder(24, 26, 24, 26));
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));

        JLabel messageLabel = new JLabel("Khu vực mới chưa có sân con nào.");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageLabel.setForeground(new Color(107, 114, 128));
        rowPanel.add(messageLabel, BorderLayout.CENTER);
        return rowPanel;
    }

    private void openAddSanConDialog() {
        JTextField maSanField = new JTextField();
        JComboBox<String> statusComboBox = new JComboBox<>(new String[]{ACTIVE_STATUS, MAINTENANCE_STATUS});

        JPanel formPanel = new JPanel(new GridLayout(0, 1, 0, 8));
        formPanel.add(new JLabel("Mã sân"));
        formPanel.add(maSanField);
        formPanel.add(new JLabel("Trạng thái"));
        formPanel.add(statusComboBox);

        int result = JOptionPane.showConfirmDialog(
                this,
                formPanel,
                "Thêm sân con",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String maSan = maSanField.getText() == null ? "" : maSanField.getText().trim().toUpperCase();
        if (maSan.isBlank()) {
            JOptionPane.showMessageDialog(this, "Mã sân không được để trống.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (isDuplicateMaSan(maSan)) {
            JOptionPane.showMessageDialog(this, "Mã sân đã tồn tại trong danh sách đang tạo.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String trangThai = toDatabaseStatus((String) statusComboBox.getSelectedItem());
        newSanCons.add(new SanConDraft(maSan, trangThai));
        updateCourtCountLabel();
        renderSanConTable();
    }

    private boolean isDuplicateMaSan(String maSan) {
        for (SanConDraft sanConDraft : newSanCons) {
            if (sanConDraft.maSan().equalsIgnoreCase(maSan)) {
                return true;
            }
        }
        return false;
    }

    private void removeSanCon(String maSan) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn chắc chắn muốn xóa sân con " + maSan + " không?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        newSanCons.removeIf(item -> item.maSan().equalsIgnoreCase(maSan));
        updateCourtCountLabel();
        renderSanConTable();
    }

    private void updateCourtCountLabel() {
        courtCountValueLabel.setText(String.valueOf(newSanCons.size()));
    }

    private void chooseAreaImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn ảnh khu vực");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Image files", "png", "jpg", "jpeg", "webp"));

        int result = fileChooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        selectedImagePath = fileChooser.getSelectedFile().toPath();
        imagePreviewPanel.setImage(selectedImagePath);
    }

    private void saveNewKhuVuc() {
        String maKv = maKvField.getText() == null ? "" : maKvField.getText().trim().toUpperCase();
        if (maKv.isBlank()) {
            JOptionPane.showMessageDialog(this, "Hãy nhập mã khu vực.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LoaiTheThao selectedLoaiTheThao = (LoaiTheThao) sportTypeComboBox.getSelectedItem();
        if (selectedLoaiTheThao == null) {
            JOptionPane.showMessageDialog(this, "Hãy chọn loại thể thao.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        KhuVucCreateRequest request = new KhuVucCreateRequest(
                maKv,
                null,
                selectedLoaiTheThao.maTt(),
                newSanCons.size(),
                List.copyOf(newSanCons)
        );

        try {
            khuVucController.createKhuVuc(request);
            if (selectedImagePath != null) {
                khuVucController.saveAreaImage(maKv, selectedImagePath);
            }
            JOptionPane.showMessageDialog(this, "Đã thêm khu vực mới.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            onSaved.accept(maKv);
        } catch (IllegalStateException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getCause() == null ? exception.getMessage() : exception.getCause().getMessage(),
                    "Lỗi thêm khu vực",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void showSanConDetail(SanCon sanCon) {
        String message = """
                Mã sân: %s
                Mã khu vực: %s
                Trạng thái: %s
                Ngày tạo: %s
                """.formatted(
                sanCon.maSan(),
                sanCon.maKv() == null || sanCon.maKv().isBlank() ? maKvField.getText().trim() : sanCon.maKv(),
                sanCon.trangThai(),
                formatDate(sanCon.createdAt())
        );
        JOptionPane.showMessageDialog(this, message, "Chi tiết sân con", JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel createStatusPill(String trangThai) {
        boolean isActive = normalizeStatus(trangThai).contains("HOAT");
        Color background = isActive ? new Color(216, 255, 208) : new Color(229, 231, 235);
        Color foreground = isActive ? new Color(44, 154, 16) : new Color(95, 107, 122);

        JPanel pill = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        pill.setOpaque(false);

        JPanel wrapper = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(background);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
            }
        };
        wrapper.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 5));
        wrapper.setOpaque(false);

        JLabel dotLabel = new JLabel("\u2022");
        dotLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        dotLabel.setForeground(foreground);

        JLabel textLabel = new JLabel(isActive ? "Available" : "Maintenance");
        textLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        textLabel.setForeground(foreground);

        wrapper.add(dotLabel);
        wrapper.add(textLabel);
        pill.add(wrapper);
        return pill;
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
        button.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, 13));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 22, 10, 22));
        return button;
    }

    private JButton createMiniActionButton(String text, Color bg, Color fg) {
        JButton button = createPillButton(text, bg, fg, true);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBorder(new EmptyBorder(7, 14, 7, 14));
        return button;
    }

    private String formatDate(LocalDateTime dateTime) {
        return dateTime == null ? "--" : dateTime.format(DATE_FORMATTER);
    }

    private String toDatabaseStatus(String value) {
        if (value == null) {
            return ACTIVE_STATUS;
        }
        return normalizeStatus(value).contains("BAO") ? MAINTENANCE_STATUS : ACTIVE_STATUS;
    }

    private String normalizeStatus(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('\u0110', 'D')
                .replace('\u0111', 'd');
        return normalized.toUpperCase();
    }
}