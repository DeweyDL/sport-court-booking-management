package com.sportcourt.area.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

public class QuanLyKhuVuc extends JPanel {

    public QuanLyKhuVuc() {
        setLayout(new BorderLayout(0, 20));
        setBackground(Color.decode("#F5F7FA")); // Màu nền nhạt của cả trang
        setBorder(new EmptyBorder(30, 40, 30, 40));

        // 1. PHẦN HEADER (Tiêu đề + Thanh tìm kiếm)
        add(createHeaderSection(), BorderLayout.NORTH);

        // 2. PHẦN MAIN CONTENT (Bảng danh sách màu trắng)
        add(createMainContentSection(), BorderLayout.CENTER);
    }

    // --- TẠO PHẦN HEADER ---
    private JPanel createHeaderSection() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        // Tiêu đề
        JLabel titleLabel = new JLabel("Quản Lý Chi Nhánh (Sân)");
        titleLabel.setFont(new Font("Lexend", Font.BOLD, 28));
        titleLabel.setForeground(Color.decode("#1A1A1A"));

        // Phụ đề
        JLabel subtitleLabel = new JLabel("Quản lý tài sản sân bãi, theo dõi tình trạng trống và lên lịch bảo trì.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.decode("#6B7280"));
        subtitleLabel.setBorder(new EmptyBorder(5, 0, 20, 0));

        // Thanh tìm kiếm
        JPanel searchContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        searchContainer.setOpaque(false);

        JTextField searchField = new JTextField(30);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setPreferredSize(new Dimension(350, 40));
        searchField.putClientProperty("JTextField.placeholderText", "Tìm kiếm sân, mã ID, hoặc khu vực...");
        searchField.putClientProperty("JTextField.padding", new Insets(5, 10, 5, 10));
        // Nếu dùng FlatLaf mới sẽ có bo tròn, nếu không có thể để mặc định
        searchField.putClientProperty("JComponent.roundRect", true);

        searchContainer.add(searchField);

        headerPanel.add(titleLabel);
        headerPanel.add(subtitleLabel);
        headerPanel.add(searchContainer);

        return headerPanel;
    }

    // --- TẠO PHẦN NỘI DUNG CHÍNH (BẢNG TRẮNG) ---
    private JPanel createMainContentSection() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Color.WHITE);
        // Có thể dùng Custom Border để bo góc, ở đây dùng EmptyBorder cho đơn giản và tương thích tốt
        container.setBorder(new EmptyBorder(20, 0, 20, 0));

        // Toolbar (Active Inventory + Nút Add)
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(new EmptyBorder(0, 20, 20, 20));

        JPanel leftToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftToolbar.setBackground(Color.WHITE);

        JLabel tableTitle = new JLabel("Danh sách hoạt động");
        tableTitle.setFont(new Font("Lexend", Font.BOLD, 18));

        JButton btnAdd = createPillButton("+ Thêm sân mới", Color.decode("#20C968"), Color.WHITE, true);

        leftToolbar.add(tableTitle);
        leftToolbar.add(btnAdd);
        toolbar.add(leftToolbar, BorderLayout.WEST);

        container.add(toolbar, BorderLayout.NORTH);

        // Table Header & Rows
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
        tablePanel.setBackground(Color.WHITE);

        // Tạo Header của bảng
        tablePanel.add(createTableHeader());

        // Tạo các dòng dữ liệu (Giả lập giống ảnh)
        tablePanel.add(createDataRow("SN-001-A", "Khu vực Alpha", "SẴN SÀNG", "24 Thg 10, 2023"));
        tablePanel.add(createDataRow("SN-042-B", "Khu vực Bravo", "ĐÃ ĐẶT", "12 Thg 11, 2023"));
        tablePanel.add(createDataRow("SN-009-C", "Khu vực Charlie", "BẢO TRÌ", "05 Thg 12, 2023"));
        tablePanel.add(createDataRow("SN-112-A", "Khu vực Alpha", "SẴN SÀNG", "18 Thg 01, 2024"));

        container.add(tablePanel, BorderLayout.CENTER);

        // Footer (Pagination)
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(20, 20, 0, 20));

        JLabel infoLabel = new JLabel("Hiển thị 4 trên tổng số 28 sân");
        infoLabel.setForeground(Color.decode("#6B7280"));
        footer.add(infoLabel, BorderLayout.WEST);

        // Nút phân trang
        JPanel pagination = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        pagination.setBackground(Color.WHITE);
        pagination.add(createPillButton("<", Color.decode("#E5E7EB"), Color.BLACK, false));
        pagination.add(createPillButton("1", Color.decode("#20C968"), Color.WHITE, false));
        pagination.add(createPillButton("2", Color.decode("#E5E7EB"), Color.BLACK, false));
        pagination.add(createPillButton(">", Color.decode("#E5E7EB"), Color.BLACK, false));

        footer.add(pagination, BorderLayout.EAST);
        container.add(footer, BorderLayout.SOUTH);

        return container;
    }

    // --- HEADER CỦA BẢNG DỮ LIỆU ---
    private JPanel createTableHeader() {
        JPanel header = new JPanel(new GridLayout(1, 5, 10, 0));
        header.setBackground(Color.decode("#F8F9FA"));
        header.setBorder(new MatteBorder(1, 0, 1, 0, Color.decode("#E5E7EB")));
        header.setPreferredSize(new Dimension(0, 45));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        header.add(createHeaderLabel("MÃ SÂN"));
        header.add(createHeaderLabel("KHU VỰC"));
        header.add(createHeaderLabel("TRẠNG THÁI"));
        header.add(createHeaderLabel("NGÀY TẠO"));
        header.add(createHeaderLabel("THAO TÁC"));

        return header;
    }

    private JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(Color.decode("#6B7280"));
        label.setBorder(new EmptyBorder(0, 20, 0, 0));
        return label;
    }

    // --- TẠO MỘT DÒNG DỮ LIỆU CỦA BẢNG ---
    private JPanel createDataRow(String id, String area, String status, String date) {
        JPanel row = new JPanel(new GridLayout(1, 5, 10, 0));
        row.setBackground(Color.WHITE);
        row.setBorder(new MatteBorder(0, 0, 1, 0, Color.decode("#F3F4F6"))); // Dòng kẻ mờ ngăn cách
        row.setPreferredSize(new Dimension(0, 60));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        // 1. Cột Mã Sân (Màu xanh đậm)
        JLabel idLabel = new JLabel(id);
        idLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        idLabel.setForeground(Color.decode("#16A34A"));
        idLabel.setBorder(new EmptyBorder(0, 20, 0, 0));
        row.add(idLabel);

        // 2. Cột Khu vực
        JLabel areaLabel = new JLabel("•  " + area); // Dùng dấu chấm tròn giả làm icon
        areaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        if(area.contains("Alpha")) areaLabel.setForeground(Color.decode("#3B82F6")); // Xanh dương
        else if (area.contains("Bravo")) areaLabel.setForeground(Color.decode("#F59E0B")); // Cam
        else areaLabel.setForeground(Color.decode("#8B5CF6")); // Tím
        row.add(areaLabel);

        // 3. Cột Trạng Thái (Hình viên thuốc)
        JPanel statusContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 15));
        statusContainer.setBackground(Color.WHITE);
        Color bgCol = Color.WHITE, fgCol = Color.BLACK;

        if (status.equals("SẴN SÀNG")) { bgCol = Color.decode("#DCFCE7"); fgCol = Color.decode("#16A34A"); }
        else if (status.equals("ĐÃ ĐẶT")) { bgCol = Color.decode("#E5E7EB"); fgCol = Color.decode("#4B5563"); }
        else if (status.equals("BẢO TRÌ")) { bgCol = Color.decode("#FEE2E2"); fgCol = Color.decode("#EF4444"); }

        statusContainer.add(createStatusPill(status, bgCol, fgCol));
        row.add(statusContainer);

        // 4. Cột Ngày tạo
        JLabel dateLabel = new JLabel(date);
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateLabel.setForeground(Color.decode("#4B5563"));
        row.add(dateLabel);

        // 5. Cột Thao tác (Các nút bấm)
        JPanel actionContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 12));
        actionContainer.setBackground(Color.WHITE);

        JButton btnView = createPillButton("Chi tiết", Color.decode("#F3F4F6"), Color.decode("#1F2937"), false);
        JButton btnDelete = createPillButton("Xóa", Color.decode("#FEF2F2"), Color.decode("#EF4444"), false);

        actionContainer.add(btnView);
        actionContainer.add(btnDelete);
        row.add(actionContainer);

        // Hover Effect cho dòng
        row.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                row.setBackground(Color.decode("#F9FAFB"));
                statusContainer.setBackground(Color.decode("#F9FAFB"));
                actionContainer.setBackground(Color.decode("#F9FAFB"));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                row.setBackground(Color.WHITE);
                statusContainer.setBackground(Color.WHITE);
                actionContainer.setBackground(Color.WHITE);
            }
        });

        return row;
    }

    // --- HÀM TẠO NÚT BẤM BO TRÒN (Cho thao tác) ---
    private JButton createPillButton(String text, Color bg, Color fg, boolean isBold) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight()); // Bo tròn hoàn toàn
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", isBold ? Font.BOLD : Font.PLAIN, 12));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(5, 12, 5, 12));
        return btn;
    }

    // --- HÀM TẠO LABEL TRẠNG THÁI BO TRÒN (KHÔNG BẤM ĐƯỢC) ---
    private JPanel createStatusPill(String text, Color bg, Color fg) {
        JPanel pill = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            }
        };
        pill.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
        pill.setOpaque(false);

        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(fg);
        pill.add(label);

        return pill;
    }
}