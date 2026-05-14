package com.sportcourt.modules.equipment.view;

import com.sportcourt.common.style.CrudViewStyle;
import com.sportcourt.modules.equipment.view.EquipmentMockData.EquipmentItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class EquipmentManagement extends JPanel implements Scrollable {
    private static final int HEADER_HEIGHT = 52;
    private static final int ROW_HEIGHT = 72;
    private static final int COLUMN_GAP = 12;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Color ALTERNATE_ROW_BG = new Color(248, 250, 252);

    private final JPanel tablePanel = new JPanel();
    private final JLabel footerLabel = new JLabel("Đang tải dữ liệu...");
    private final JTextField searchField = new JTextField(30);
    private final JPanel searchWrapper = new JPanel(new BorderLayout());
    private final JComboBox<String> cbSort = new JComboBox<>(new String[]{"Tên dụng cụ", "Giá", "SL tồn"});
    private final JButton btnSortDir = new JButton("\u25B2");

    private List<EquipmentItem> equipmentList = new ArrayList<>();
    private boolean sortAscending = true;

    private final com.sportcourt.modules.equipment.controller.EquipmentController controller = new com.sportcourt.modules.equipment.controller.EquipmentController();

    public void refresh() {
        loadData(searchField.getText());
    }

    public EquipmentManagement() {
        setLayout(new BorderLayout());
        CrudViewStyle.applyPageDefaults(this);

        add(createPage(), BorderLayout.CENTER);
        CrudViewStyle.installResponsiveTypography(this);
        loadData(null);
    }

    // --------- PAGE LAYOUT ---------

    private JPanel createPage() {
        JPanel page = new JPanel(new BorderLayout(0, 12));
        page.setOpaque(false);
        page.add(createHeaderSection(), BorderLayout.NORTH);
        page.add(createMainSection(), BorderLayout.CENTER);
        return page;
    }

    private JPanel createHeaderSection() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);

        JLabel title = new JLabel("QUẢN LÝ DỤNG CỤ");
        title.setFont(new Font("Lexend", Font.BOLD, 30));
        title.setForeground(new Color(30, 31, 36));
        title.setBorder(new EmptyBorder(0, 20, 0, 0));

        JLabel subtitle = new JLabel("Quản lý thông tin các loại dụng cụ thể thao.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(103, 112, 133));
        subtitle.setBorder(new EmptyBorder(5, 20, 20, 0));

        header.add(title);
        header.add(subtitle);
        return header;
    }

    // --------- MAIN CONTENT (rounded card) ---------

    private JPanel createMainSection() {
        JPanel container = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50);
                g2.dispose();
            }

            @Override
            protected void paintChildren(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                super.paintChildren(g2);
                g2.dispose();
            }
        };
        container.setOpaque(false);
        container.setBackground(Color.WHITE);
        container.setBorder(new EmptyBorder(12, 0, 16, 0));

        // Top section: toolbar
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setBackground(Color.WHITE);
        topSection.add(createToolbar());
        container.add(topSection, BorderLayout.NORTH);

        // Scrollable rows
        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
        tablePanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(tablePanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        CrudViewStyle.configureScrollPane(scrollPane);
        scrollPane.setColumnHeaderView(createTableHeader());
        container.add(scrollPane, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(20, 20, 0, 20));
        footerLabel.setForeground(new Color(107, 114, 128));
        footer.add(footerLabel, BorderLayout.WEST);
        container.add(footer, BorderLayout.SOUTH);

        return container;
    }

    // --------- TOOLBAR ---------

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(new EmptyBorder(8, 20, 14, 20));

        JPanel leftToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftToolbar.setBackground(Color.WHITE);

        JLabel tableTitle = new JLabel("DANH SÁCH DỤNG CỤ");
        tableTitle.setFont(new Font("Lexend", Font.BOLD, 22));

        JButton addBtn = createPillButton("+ Thêm dụng cụ", new Color(228, 250, 226), new Color(16, 110, 0), true);
        addBtn.setFont(new Font("Lexend", Font.BOLD, 16));
        addBtn.setBorder(new EmptyBorder(6, 22, 6, 22));
        CrudViewStyle.applyToolbarButtonHeight(addBtn);
        addBtn.addActionListener(event -> openCreateDialog());
        JButton refreshBtn = CrudViewStyle.createRefreshButton(event -> refresh());

        leftToolbar.add(tableTitle);
        leftToolbar.add(addBtn);
        leftToolbar.add(refreshBtn);
        toolbar.add(leftToolbar, BorderLayout.WEST);

        JPanel rightToolbar = new JPanel();
        rightToolbar.setLayout(new BoxLayout(rightToolbar, BoxLayout.X_AXIS));
        rightToolbar.setBackground(Color.WHITE);
        rightToolbar.add(createSortWrapper());
        rightToolbar.add(Box.createHorizontalStrut(10));
        rightToolbar.add(createSearchFieldWithIcon());
        toolbar.add(rightToolbar, BorderLayout.EAST);

        return toolbar;
    }

    private JPanel createSearchFieldWithIcon() {
        searchField.putClientProperty("JTextField.placeholderText", "Tìm theo mã hoặc tên dụng cụ...");
        bindSearchListener();
        return CrudViewStyle.createSearchFieldWithIcon(searchWrapper, searchField, loadSearchIcon());
    }


    // --------- TABLE ---------

    private JPanel createTableHeader() {
        JPanel header = new JPanel(new GridBagLayout());
        header.setBackground(new Color(248, 249, 250));
        header.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 1, 0, new Color(229, 231, 235)),
                new EmptyBorder(0, 24, 0, 24)
        ));
        header.setPreferredSize(new Dimension(1000, HEADER_HEIGHT));
        header.setMinimumSize(new Dimension(800, HEADER_HEIGHT));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, COLUMN_GAP);

        gbc.weightx = 0.10; header.add(createFlexibleCell(createHeaderLabel("Mã DC"), SwingConstants.CENTER, new Color(248, 249, 250), 0, 8), gbc);
        gbc.weightx = 0.25; header.add(createFlexibleCell(createHeaderLabel("TÊN DỤNG CỤ"), SwingConstants.LEFT, new Color(248, 249, 250), 8, 8), gbc);
        gbc.weightx = 0.13; header.add(createFlexibleCell(createHeaderLabel("ĐƠN VỊ TÍNH"), SwingConstants.CENTER, new Color(248, 249, 250), 0, 8), gbc);
        gbc.weightx = 0.17; header.add(createFlexibleCell(createHeaderLabel("GIÁ"), SwingConstants.RIGHT, new Color(248, 249, 250), 0, 8), gbc);
        gbc.weightx = 0.12; header.add(createFlexibleCell(createHeaderLabel("SỐ LƯỢNG"), SwingConstants.CENTER, new Color(248, 249, 250), 0, 4), gbc);
        gbc.weightx = 0.23; gbc.insets = new Insets(0, 0, 0, 0); header.add(createFlexibleCell(createHeaderLabel("THAO TÁC"), SwingConstants.CENTER, new Color(248, 249, 250), 0, 0), gbc);

        return header;
    }

    private JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(new Color(107, 114, 128));
        return label;
    }

    private JPanel createDataRow(EquipmentItem item, int rowIndex) {
        Color rowBg = rowIndex % 2 == 0 ? Color.WHITE : ALTERNATE_ROW_BG;

        JPanel row = new JPanel(new GridBagLayout());
        row.setBackground(rowBg);
        row.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(243, 244, 246)),
                new EmptyBorder(0, 24, 0, 24)
        ));
        row.setPreferredSize(new Dimension(1000, ROW_HEIGHT));
        row.setMinimumSize(new Dimension(800, ROW_HEIGHT));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, ROW_HEIGHT));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, COLUMN_GAP);

        // Mã DC - highlight xanh lá
        JLabel idLabel = new JLabel(item.maDc());
        idLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        idLabel.setForeground(new Color(22, 163, 74));
        gbc.weightx = 0.10; row.add(createFlexibleCell(idLabel, SwingConstants.CENTER, rowBg, 0, 8), gbc);

        // Tên dụng cụ
        gbc.weightx = 0.25; row.add(createFlexibleCell(createCellLabel(item.tenDc(), new Color(17, 24, 39)), SwingConstants.LEFT, rowBg, 8, 8), gbc);

        // Đơn vị tính
        gbc.weightx = 0.13; row.add(createFlexibleCell(createCellLabel(item.dvt(), new Color(75, 85, 99)), SwingConstants.CENTER, rowBg, 0, 8), gbc);

        // Giá
        gbc.weightx = 0.17; row.add(createFlexibleCell(createCellLabel(formatCurrency(item.gia()), new Color(17, 24, 39)), SwingConstants.RIGHT, rowBg, 0, 8), gbc);

        // Số lượng
        gbc.weightx = 0.12; row.add(createFlexibleCell(createCellLabel(String.valueOf(item.slTon()), new Color(17, 24, 39)), SwingConstants.CENTER, rowBg, 0, 4), gbc);

        // Thao tác
        JPanel actionGroup = new JPanel();
        actionGroup.setLayout(new BoxLayout(actionGroup, BoxLayout.X_AXIS));
        actionGroup.setOpaque(false);

        JButton deleteBtn = createMiniActionButton("Xóa", new Color(254, 226, 226), new Color(185, 28, 28));
        Dimension deleteBtnSize = new Dimension(64, 28);
        deleteBtn.setPreferredSize(deleteBtnSize);
        deleteBtn.setMinimumSize(deleteBtnSize);
        deleteBtn.setMaximumSize(deleteBtnSize);
        deleteBtn.addActionListener(event -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa dụng cụ này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    controller.deleteEquipment(item.maDc());
                    JOptionPane.showMessageDialog(this, "Xóa thành công!");
                    refresh();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        actionGroup.add(deleteBtn);
        actionGroup.add(Box.createHorizontalStrut(8));

        JButton editBtn = createMiniActionButton("Chỉnh sửa", new Color(239, 246, 255), new Color(29, 78, 216));
        Dimension editBtnSize = new Dimension(86, 28);
        editBtn.setPreferredSize(editBtnSize);
        editBtn.setMinimumSize(editBtnSize);
        editBtn.setMaximumSize(editBtnSize);
        editBtn.addActionListener(event -> openEditDialog(item));
        actionGroup.add(editBtn);

        JPanel actionCell = new JPanel(new GridBagLayout());
        actionCell.setBackground(rowBg);
        actionCell.setOpaque(true);
        actionCell.add(actionGroup);

        gbc.weightx = 0.23; gbc.insets = new Insets(0, 0, 0, 0); row.add(createFlexibleCell(actionCell, SwingConstants.CENTER, rowBg, 0, 0), gbc);

        // Hover effect
        row.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                row.setBackground(new Color(249, 250, 251));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                row.setBackground(rowBg);
            }
        });

        return row;
    }

    private JPanel createEmptyRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setBorder(new EmptyBorder(24, 26, 24, 26));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));

        JLabel msg = new JLabel("Không tìm thấy dụng cụ phù hợp.");
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        msg.setForeground(new Color(107, 114, 128));
        row.add(msg, BorderLayout.CENTER);
        return row;
    }

    // --------- HELPERS ---------

    private JPanel createFlexibleCell(Component component, int alignment, Color bg, int leftPad, int rightPad) {
        if (component instanceof JLabel label) {
            label.setHorizontalAlignment(alignment);
        }
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(bg);
        panel.setOpaque(true);
        panel.setBorder(new EmptyBorder(0, leftPad, 0, rightPad));
        panel.add(component, BorderLayout.CENTER);

        panel.setPreferredSize(new Dimension(0, ROW_HEIGHT));
        panel.setMinimumSize(new Dimension(0, ROW_HEIGHT));
        return panel;
    }

    private JLabel createCellLabel(String text, Color fg) {
        JLabel label = new JLabel(text == null || text.isBlank() ? "--" : text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        label.setForeground(fg);
        return label;
    }

    // --------- DATA OPERATIONS ---------

    private void loadData(String keyword) {
        List<EquipmentItem> all = new ArrayList<>();
        try {
            List<com.sportcourt.modules.equipment.entity.Equipment> dbEquipments = controller.searchEquipments(keyword);
            for (com.sportcourt.modules.equipment.entity.Equipment eq : dbEquipments) {
                all.add(new EquipmentItem(
                        eq.getMaDc(),
                        eq.getTenDc(),
                        eq.getDvt() != null && !eq.getDvt().isEmpty() ? eq.getDvt() : "--",
                        eq.getGia(),
                        eq.getSlTon(),
                        eq.getCreatedAt() != null ? eq.getCreatedAt() : java.time.LocalDateTime.now()
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            footerLabel.setText("Lỗi kết nối CSDL: " + e.getMessage());
            return;
        }

        equipmentList = all;
        sortList();
        renderTable();
    }


    private void renderTable() {
        tablePanel.removeAll();

        if (equipmentList.isEmpty()) {
            tablePanel.add(createEmptyRow());
        } else {
            int index = 0;
            for (EquipmentItem item : equipmentList) {
                tablePanel.add(createDataRow(item, index++));
            }
        }

        footerLabel.setText("Hiển thị " + equipmentList.size() + " dụng cụ");
        tablePanel.revalidate();
        tablePanel.repaint();
    }

    // --------- SEARCH ---------

    private void bindSearchListener() {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                loadData(searchField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                loadData(searchField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                loadData(searchField.getText());
            }
        });
    }

    // --------- SORT ---------

    private JPanel createSortWrapper() {
        cbSort.addActionListener(event -> {
            sortList();
            renderTable();
        });
        btnSortDir.addActionListener(event -> {
            sortAscending = !sortAscending;
            CrudViewStyle.updateSortDirectionButton(btnSortDir, sortAscending);
            sortList();
            renderTable();
        });
        CrudViewStyle.updateSortDirectionButton(btnSortDir, sortAscending);
        return CrudViewStyle.createSortWrapper(cbSort, btnSortDir);
    }

    private void sortList() {
        String sortType = (String) cbSort.getSelectedItem();
        Comparator<EquipmentItem> comparator;

        if ("Giá".equals(sortType)) {
            comparator = Comparator.comparing(EquipmentItem::gia);
        } else if ("SL tồn".equals(sortType)) {
            comparator = Comparator.comparingInt(EquipmentItem::slTon);
        } else {
            comparator = Comparator.comparing(item -> item.tenDc().toLowerCase());
        }

        if (!sortAscending) {
            comparator = comparator.reversed();
        }
        equipmentList.sort(comparator);
    }

    // --------- DIALOGS ---------

    private void openCreateDialog() {
        EquipmentCreateDialog.show(this);
    }

    private void openEditDialog(EquipmentItem item) {
        EquipmentEditDialog.show(this, item);
    }

    // --------- UTILS ---------

    private Icon loadSearchIcon() {
        URL iconUrl = getClass().getResource("/icon/search.png");
        if (iconUrl == null) {
            return UIManager.getIcon("FileView.fileIcon");
        }
        Image image = new ImageIcon(iconUrl).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH);
        return new ImageIcon(image);
    }

    private String formatCurrency(BigDecimal value) {
        if (value == null) {
            return "0 VNĐ";
        }
        return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(value) + " VNĐ";
    }

    private JButton createPillButton(String text, Color bg, Color fg, boolean bold) {
        JButton btn = new JButton(text) {
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
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, 14));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(5, 12, 5, 12));
        return btn;
    }

    private JButton createMiniActionButton(String text, Color bg, Color fg) {
        JButton button = createPillButton(text, bg, fg, true);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBorder(new EmptyBorder(4, 12, 4, 12));
        return button;
    }
    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 16;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 64;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return true;
    }
}
