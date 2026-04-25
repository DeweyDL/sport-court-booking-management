package com.sportcourt.modules.area.view;

import com.sportcourt.modules.area.controller.KhuVucController;
import com.sportcourt.modules.area.enitity.KhuVuc;
import com.sportcourt.modules.area.enitity.SanCon;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

// Màn chi tiết khu vực chỉ dùng để xem. Các thao tác sửa sẽ được chuyển sang SuaKhuVuc.
public class ChiTietKhuVuc extends JPanel {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final KhuVucController khuVucController;
    private final Runnable onBack;
    private final Consumer<String> onEdit;

    private final JLabel titleLabel = new JLabel("CHI TIẾT KHU VỰC");
    private final JLabel sportTypeValueLabel = createValueLabel("--");
    private final JLabel courtCountValueLabel = createValueLabel("--");
    private final JLabel infoHintLabel = new JLabel("Đây là những thông tin sẽ hiển thị với khách hàng.");
    private final JLabel footerLabel = new JLabel("Đang hiển thị 0 sân");
    private final JPanel tableBodyPanel = new JPanel();
    private final AreaImagePreviewPanel imagePreviewPanel = new AreaImagePreviewPanel();

    private String currentMaKv;
    private int totalCourtCount;

    public ChiTietKhuVuc(KhuVucController khuVucController, Runnable onBack, Consumer<String> onEdit) {
        this.khuVucController = khuVucController;
        this.onBack = onBack;
        this.onEdit = onEdit;

        setLayout(new BorderLayout(0, 18));
        setBackground(new Color(247, 247, 251));
        setBorder(new EmptyBorder(10, 0, 0, 0));

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);
    }

    public void showDetail(String maKv) {
        currentMaKv = maKv;
        KhuVuc khuVuc = khuVucController.getKhuVucDetail(maKv);
        List<SanCon> sanCons = khuVucController.getSanConList(maKv);
        bindKhuVuc(khuVuc, sanCons);
        loadAreaImage(maKv);
    }

    private JPanel createHeader() {
        JPanel headerWrapper = new JPanel();
        headerWrapper.setLayout(new BoxLayout(headerWrapper, BoxLayout.Y_AXIS));
        headerWrapper.setOpaque(false);
        headerWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel backToAreaList = new JLabel("< Quay lại trang Quản lý khu vực");
        backToAreaList.setFont(new Font("Lexend", Font.PLAIN, 15));
        backToAreaList.setForeground(new Color(58, 134, 45));
        backToAreaList.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backToAreaList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onBack.run();
            }
        });

        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        backRow.setOpaque(false);
        backRow.add(backToAreaList);

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.setBorder(new EmptyBorder(14, 0, 0, 0));

        titleLabel.setFont(new Font("Lexend", Font.BOLD, 30));
        titleLabel.setForeground(new Color(30, 31, 36));
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

    // Card trái của màn chi tiết: Giảm viền và khoảng cách để kéo nút Sửa lên
    private JPanel createBasicInfoCard() {
        JPanel basicInfoCard = new JPanel();
        basicInfoCard.setLayout(new BoxLayout(basicInfoCard, BoxLayout.Y_AXIS));
        basicInfoCard.setBackground(new Color(241, 242, 246));
        // Đã giảm padding trên/dưới xuống 16 để tiết kiệm không gian
        basicInfoCard.setBorder(new EmptyBorder(16, 32, 16, 32));
        basicInfoCard.setPreferredSize(new Dimension(440, 0));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setAlignmentX(Component.LEFT_ALIGNMENT); // Ép lề trái
        contentPanel.setMaximumSize(new Dimension(360, Integer.MAX_VALUE));

        JLabel cardTitle = new JLabel("Thông tin cơ bản");
        cardTitle.setFont(new Font("Lexend", Font.BOLD, 18));
        cardTitle.setForeground(new Color(35, 37, 43));
        cardTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoHintLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoHintLabel.setForeground(new Color(103, 112, 133));
        infoHintLabel.setBorder(new EmptyBorder(6, 0, 14, 0)); // Thu hẹp khoảng cách
        infoHintLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel imageWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        imageWrapper.setOpaque(false);
        imageWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        imageWrapper.add(imagePreviewPanel);

        JPanel sportTypeField = createFieldBlock("LOẠI THỂ THAO", sportTypeValueLabel);
        JPanel courtCountField = createFieldBlock("SỐ LƯỢNG SÂN CON", courtCountValueLabel);

        JButton editButton = createPillButton("SỬA THÔNG TIN", new Color(57, 255, 20), new Color(26, 42, 8), true);
        editButton.setPreferredSize(new Dimension(280, 48)); // Thu gọn chiều cao nút
        editButton.setMaximumSize(new Dimension(280, 48));
        editButton.setBorder(new EmptyBorder(14, 42, 14, 42));
        editButton.addActionListener(event -> {
            if (currentMaKv != null) {
                onEdit.accept(currentMaKv);
            }
        });

        // Đặt nút vào FlowLayout.CENTER để nút ở giữa, nhưng wrapper vẫn bám lề trái
        JPanel editButtonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        editButtonRow.setOpaque(false);
        editButtonRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        editButtonRow.setMaximumSize(new Dimension(360, 48));
        editButtonRow.add(editButton);

        // Giảm bớt các số trong VerticalStrut
        contentPanel.add(cardTitle);
        contentPanel.add(infoHintLabel);
        contentPanel.add(imageWrapper);
        contentPanel.add(Box.createVerticalStrut(16));
        contentPanel.add(sportTypeField);
        contentPanel.add(Box.createVerticalStrut(12));
        contentPanel.add(courtCountField);

        basicInfoCard.add(Box.createVerticalGlue());
        basicInfoCard.add(contentPanel);
        basicInfoCard.add(Box.createVerticalStrut(16));
        basicInfoCard.add(editButtonRow);
        basicInfoCard.add(Box.createVerticalGlue());
        return basicInfoCard;
    }

    private JPanel createFieldBlock(String labelText, JLabel valueLabel) {
        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.Y_AXIS));
        fieldPanel.setOpaque(false);
        fieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Thu gọn chiều cao ô hiển thị từ 84 xuống 74
        fieldPanel.setMaximumSize(new Dimension(360, 74));
        fieldPanel.setPreferredSize(new Dimension(360, 74));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(92, 102, 77));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel valueWrapper = new JPanel(new BorderLayout());
        valueWrapper.setBackground(Color.WHITE);
        valueWrapper.setBorder(new CompoundBorder(
                new MatteBorder(1, 1, 1, 1, new Color(229, 231, 235)),
                new EmptyBorder(10, 16, 10, 16) // Thu gọn padding bên trong
        ));
        valueWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        valueWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        valueWrapper.add(valueLabel, BorderLayout.CENTER);

        fieldPanel.add(label);
        fieldPanel.add(Box.createVerticalStrut(6));
        fieldPanel.add(valueWrapper);
        return fieldPanel;
    }

    // Bảng sân con
    private JPanel createSanConSection() {
        JPanel sectionPanel = new JPanel(new BorderLayout(0, 14));
        sectionPanel.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("Danh sách sân con");
        title.setFont(new Font("Lexend", Font.BOLD, 18));
        title.setForeground(new Color(35, 37, 43));

        JLabel subtitle = new JLabel("Cập nhật danh sách sân để hiển thị với khách hàng.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(107, 114, 128));
        subtitle.setBorder(new EmptyBorder(6, 0, 0, 0));

        titlePanel.add(title);
        titlePanel.add(subtitle);

        JPanel tableCard = createRoundedTableCard();
        tableCard.add(createSanConHeader(), BorderLayout.NORTH);

        tableBodyPanel.setLayout(new BoxLayout(tableBodyPanel, BoxLayout.Y_AXIS));
        tableBodyPanel.setBackground(Color.WHITE);
        tableCard.add(tableBodyPanel, BorderLayout.CENTER);
        tableCard.add(createFooter(), BorderLayout.SOUTH);

        sectionPanel.add(titlePanel, BorderLayout.NORTH);
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

        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        paginationPanel.setOpaque(false);
        paginationPanel.add(createSecondaryButton("Previous"));
        paginationPanel.add(createSecondaryButton("Next"));
        footerPanel.add(paginationPanel, BorderLayout.EAST);
        return footerPanel;
    }

    private JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(94, 103, 82));
        return label;
    }

    private void renderSanConTable(List<SanCon> sanCons) {
        tableBodyPanel.removeAll();

        if (sanCons.isEmpty()) {
            tableBodyPanel.add(createEmptyRow());
        } else {
            for (SanCon sanCon : sanCons) {
                tableBodyPanel.add(createSanConRow(sanCon));
            }
        }

        footerLabel.setText("Đang hiển thị " + sanCons.size() + " / " + totalCourtCount + " sân");
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
        actionCell.add(detailButton);

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

        JLabel messageLabel = new JLabel("Khu vực này chưa có sân con nào.");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageLabel.setForeground(new Color(107, 114, 128));
        rowPanel.add(messageLabel, BorderLayout.CENTER);
        return rowPanel;
    }

    private void bindKhuVuc(KhuVuc khuVuc, List<SanCon> sanCons) {
        sportTypeValueLabel.setText(valueOrPlaceholder(khuVuc.tenTheThao(), khuVuc.maTt()));
        totalCourtCount = khuVuc.soLuongSan();
        courtCountValueLabel.setText(String.valueOf(khuVuc.soLuongSan()));
        infoHintLabel.setText("Đây là những thông tin sẽ hiển thị với khách hàng.");
        renderSanConTable(sanCons);
    }

    private void loadAreaImage(String maKv) {
        khuVucController.getAreaImagePath(maKv)
                .ifPresentOrElse(
                        imagePreviewPanel::setImage,
                        imagePreviewPanel::clearImage
                );
    }

    private void showSanConDetail(SanCon sanCon) {
        String message = """
                Mã sân: %s
                Mã khu vực: %s
                Trạng thái: %s
                Ngày tạo: %s
                """.formatted(
                sanCon.maSan(),
                sanCon.maKv(),
                sanCon.trangThai(),
                formatDate(sanCon.createdAt())
        );

        JOptionPane.showMessageDialog(
                this,
                message,
                "Chi tiết sân con",
                JOptionPane.INFORMATION_MESSAGE
        );
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

    private JButton createSecondaryButton(String text) {
        JButton button = createPillButton(text, new Color(233, 234, 238), new Color(107, 114, 128), true);
        button.setEnabled(false);
        return button;
    }

    private JLabel createValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(new Color(31, 41, 55));
        return label;
    }

    private String formatDate(LocalDateTime dateTime) {
        return dateTime == null ? "--" : dateTime.format(DATE_FORMATTER);
    }

    private String valueOrPlaceholder(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        return fallback == null || fallback.isBlank() ? "--" : fallback;
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