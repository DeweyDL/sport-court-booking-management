package com.sportcourt.modules.equipment.view;

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

public class EquipmentManagement extends JPanel {
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

    public EquipmentManagement() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(100, 70, 50, 70));

        add(createPage(), BorderLayout.CENTER);
        loadData(null);
    }

    // --------- PAGE LAYOUT ---------

    private JPanel createPage() {
        JPanel page = new JPanel(new BorderLayout(0, 20));
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
        container.setBorder(new EmptyBorder(20, 0, 20, 0));

        // Top section: toolbar + fixed header
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setBackground(Color.WHITE);
        topSection.add(createToolbar());
        topSection.add(createTableHeader());
        container.add(topSection, BorderLayout.NORTH);

        // Scrollable rows only
        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
        tablePanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(tablePanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
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
        toolbar.setBorder(new EmptyBorder(10, 20, 20, 20));

        JPanel leftToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftToolbar.setBackground(Color.WHITE);

        JLabel tableTitle = new JLabel("DANH SÁCH DỤNG CỤ");
        tableTitle.setFont(new Font("Lexend", Font.BOLD, 22));

        JButton addBtn = createPillButton("+ Thêm dụng cụ", new Color(228, 250, 226), new Color(16, 110, 0), true);
        addBtn.setFont(new Font("Lexend", Font.BOLD, 17));
        addBtn.addActionListener(event -> openCreateDialog());

        leftToolbar.add(tableTitle);
        leftToolbar.add(addBtn);
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
        searchWrapper.removeAll();
        searchWrapper.setOpaque(false);
        searchWrapper.setPreferredSize(new Dimension(300, 45));
        searchWrapper.setMaximumSize(new Dimension(300, 45));

        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setPreferredSize(new Dimension(300, 45));
        searchField.putClientProperty("JTextField.placeholderText", "Tìm theo mã hoặc tên dụng cụ...");
        searchField.putClientProperty("JTextField.padding", new Insets(5, 8, 5, 10));
        searchField.putClientProperty("JComponent.roundRect", true);
        searchField.setBorder(null);
        searchField.setOpaque(false);
        bindSearchListener();

        JLabel iconLabel = new JLabel(loadSearchIcon());
        iconLabel.setBorder(new EmptyBorder(0, 0, 0, 8));

        JPanel innerPanel = new JPanel(new BorderLayout()) {
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
        };
        innerPanel.setOpaque(false);
        innerPanel.setPreferredSize(new Dimension(300, 45));
        innerPanel.setMaximumSize(new Dimension(300, 45));
        innerPanel.setBorder(new EmptyBorder(0, 12, 0, 12));
        innerPanel.add(iconLabel, BorderLayout.WEST);
        innerPanel.add(searchField, BorderLayout.CENTER);

        searchWrapper.add(innerPanel, BorderLayout.CENTER);
        return searchWrapper;
    }


    // --------- TABLE ---------

    private JPanel createTableHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
        header.setBackground(new Color(248, 249, 250));
        header.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 1, 0, new Color(229, 231, 235)),
                new EmptyBorder(0, 24, 0, 24)
        ));
        header.setPreferredSize(new Dimension(0, 45));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        header.add(createFixedCell(createHeaderLabel("Mã DC"), 120, 45, SwingConstants.LEFT, new Color(248, 249, 250), 0, 8));
        header.add(Box.createHorizontalStrut(10));
        header.add(createFixedCell(createHeaderLabel("TÊN DỤNG CỤ"), 240, 45, SwingConstants.LEFT, new Color(248, 249, 250), 0, 8));
        header.add(Box.createHorizontalStrut(10));
        header.add(createFixedCell(createHeaderLabel("ĐƠN VỊ TÍNH"), 130, 45, SwingConstants.CENTER, new Color(248, 249, 250), 0, 8));
        header.add(Box.createHorizontalStrut(10));
        header.add(createFixedCell(createHeaderLabel("GIÁ"), 170, 45, SwingConstants.CENTER, new Color(248, 249, 250), 0, 8));
        header.add(Box.createHorizontalStrut(10));
        header.add(createFixedCell(createHeaderLabel("SỐ LƯỢNG"), 110, 45, SwingConstants.CENTER, new Color(248, 249, 250), 0, 4));
        header.add(Box.createHorizontalStrut(10));
        header.add(createFixedCell(createHeaderLabel("THAO TÁC"), 210, 45, SwingConstants.CENTER, new Color(248, 249, 250), 0, 0));
        return header;
    }
    private JPanel createHeaderCell(String text, int alignment) {
        JLabel label = createHeaderLabel(text);
        label.setHorizontalAlignment(alignment);
        return createAlignedCell(label, 0, new Color(248, 249, 250));
    }

    private JPanel createHeaderCell(String text, int alignment, int leftPad, int rightPad) {
        JLabel label = createHeaderLabel(text);
        label.setHorizontalAlignment(alignment);
        return createAlignedCell(label, leftPad, rightPad, new Color(248, 249, 250));
    }

    private JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 17));
        label.setForeground(new Color(107, 114, 128));
        return label;
    }

    private JPanel createDataRow(EquipmentItem item, int rowIndex) {
        Color rowBg = rowIndex % 2 == 0 ? Color.WHITE : ALTERNATE_ROW_BG;

        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setBackground(rowBg);
        row.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(243, 244, 246)),
                new EmptyBorder(0, 24, 0, 24)
        ));
        row.setPreferredSize(new Dimension(0, 64));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        // Ma DC - highlight xanh duong
        JLabel idLabel = new JLabel(item.maDc());
        idLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        idLabel.setForeground(new Color(22, 163, 74));
        row.add(createFixedCell(idLabel, 120, 64, SwingConstants.LEFT, rowBg, 0, 8));
        row.add(Box.createHorizontalStrut(10));

        // Ten dung cu
        row.add(createFixedCell(createCellLabel(item.tenDc(), new Color(17, 24, 39)), 240, 64, SwingConstants.LEFT, rowBg, 0, 8));
        row.add(Box.createHorizontalStrut(10));

        // Don vi tinh - can giuan
        JLabel dvtLabel = createCellLabel(item.dvt(), new Color(75, 85, 99));
        dvtLabel.setHorizontalAlignment(SwingConstants.CENTER);
        row.add(createFixedCell(dvtLabel, 130, 64, SwingConstants.CENTER, rowBg, 0, 8));
        row.add(Box.createHorizontalStrut(10));

        // Don vi tinh - can giuan
        JLabel giaLabel = createCellLabel(formatCurrency(item.gia()), new Color(17, 24, 39));
        giaLabel.setHorizontalAlignment(SwingConstants.CENTER);
        row.add(createFixedCell(giaLabel, 170, 64, SwingConstants.CENTER, rowBg, 0, 8));
        row.add(Box.createHorizontalStrut(10));

        // Don vi tinh - can giuan, một màu duy nhất
        JLabel stockLabel = createCellLabel(String.valueOf(item.slTon()), new Color(17, 24, 39));
        stockLabel.setHorizontalAlignment(SwingConstants.CENTER);
        row.add(createFixedCell(stockLabel, 110, 64, SwingConstants.CENTER, rowBg, 0, 4));
        row.add(Box.createHorizontalStrut(10));

        // Thao tac - giong customer
        JPanel actionGroup = new JPanel();
        actionGroup.setLayout(new BoxLayout(actionGroup, BoxLayout.X_AXIS));
        actionGroup.setOpaque(false);

        JButton deleteBtn = createMiniActionButton("Xóa", new Color(254, 226, 226), new Color(185, 28, 28));
        Dimension deleteBtnSize = new Dimension(80, 30);
        deleteBtn.setPreferredSize(deleteBtnSize);
        deleteBtn.setMinimumSize(deleteBtnSize);
        deleteBtn.setMaximumSize(deleteBtnSize);
        deleteBtn.addActionListener(event ->
                JOptionPane.showMessageDialog(this, "Chức năng xóa sẽ hoạt động khi có BE.", "Thông báo", JOptionPane.INFORMATION_MESSAGE)
        );
        actionGroup.add(deleteBtn);
        actionGroup.add(Box.createHorizontalStrut(10));

        JButton editBtn = createMiniActionButton("Chỉnh sửa", new Color(239, 246, 255), new Color(29, 78, 216));
        Dimension editBtnSize = new Dimension(89, 30);
        editBtn.setPreferredSize(editBtnSize);
        editBtn.setMinimumSize(editBtnSize);
        editBtn.setMaximumSize(editBtnSize);
        editBtn.addActionListener(event -> openEditDialog(item));
        actionGroup.add(editBtn);

        JPanel actionCell = new JPanel(new BorderLayout());
        actionCell.setBackground(rowBg);
        actionCell.setOpaque(true);
        actionCell.setBorder(new EmptyBorder(0, 20, 0, 4));
        actionCell.add(actionGroup, BorderLayout.CENTER);
        row.add(createFixedCell(actionCell, 205, 64, SwingConstants.LEFT, rowBg, 0, 0));

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

    private JPanel createAlignedCell(Component component, int leftPad, Color bg) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(bg);
        panel.setOpaque(true);
        panel.setBorder(new EmptyBorder(0, leftPad, 0, 0));
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }


    private JPanel createFixedCell(Component component, int width, int height, int alignment, Color bg, int leftPad, int rightPad) {
        if (component instanceof JLabel label) {
            label.setHorizontalAlignment(alignment);
        }
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(bg);
        panel.setOpaque(true);
        panel.setBorder(new EmptyBorder(0, leftPad, 0, rightPad));
        panel.add(component, BorderLayout.CENTER);

        Dimension size = new Dimension(width, height);
        panel.setPreferredSize(size);
        panel.setMinimumSize(size);
        panel.setMaximumSize(size);
        return panel;
    }
    private JPanel createAlignedCell(Component component, int leftPad, int rightPad, Color bg) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(bg);
        panel.setOpaque(true);
        panel.setBorder(new EmptyBorder(0, leftPad, 0, rightPad));
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    private JLabel createCellLabel(String text, Color fg) {
        JLabel label = new JLabel(text == null || text.isBlank() ? "--" : text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        label.setForeground(fg);
        return label;
    }

    // --------- DATA OPERATIONS ---------

    private void loadData(String keyword) {
        List<EquipmentItem> all = EquipmentMockData.createSampleData();

        if (keyword != null && !keyword.isBlank()) {
            String lower = keyword.toLowerCase().trim();
            all = all.stream()
                    .filter(item -> item.maDc().toLowerCase().contains(lower)
                            || item.tenDc().toLowerCase().contains(lower))
                    .toList();
        }

        equipmentList = new ArrayList<>(all);
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
        cbSort.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbSort.setFocusable(false);
        cbSort.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        cbSort.setOpaque(false);
        cbSort.setBackground(Color.WHITE);
        cbSort.putClientProperty("JComponent.roundRect", true);
        cbSort.putClientProperty("JComponent.arc", 999);
        cbSort.putClientProperty("JComboBox.buttonStyle", "button");
        cbSort.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                Object display = index < 0 ? "Sắp xếp: " + value : value;
                JLabel label = (JLabel) super.getListCellRendererComponent(list, display, index, isSelected, cellHasFocus);
                label.setBorder(new EmptyBorder(6, 10, 6, 10));
                return label;
            }
        });
        cbSort.addActionListener(event -> {
            sortList();
            renderTable();
        });

        btnSortDir.setFont(new Font("Segoe UI Symbol", Font.BOLD, 11));
        btnSortDir.setForeground(new Color(75, 85, 99));
        btnSortDir.setBorder(new EmptyBorder(0, 0, 0, 12));
        btnSortDir.setContentAreaFilled(false);
        btnSortDir.setBorderPainted(false);
        btnSortDir.setFocusPainted(false);
        btnSortDir.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSortDir.addActionListener(event -> {
            sortAscending = !sortAscending;
            btnSortDir.setText(sortAscending ? "\u25B2" : "\u25BC");
            sortList();
            renderTable();
        });

        JPanel wrapper = new JPanel(new BorderLayout()) {
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
        };
        wrapper.setOpaque(false);
        wrapper.setPreferredSize(new Dimension(230, 45));
        wrapper.setMaximumSize(new Dimension(230, 45));
        wrapper.add(cbSort, BorderLayout.CENTER);
        wrapper.add(btnSortDir, BorderLayout.EAST);
        return wrapper;
    }

    private void sortList() {
        String sortType = (String) cbSort.getSelectedItem();
        Comparator<EquipmentItem> comparator;

        if ("Giá".equals(sortType)) {
            comparator = Comparator.comparing(EquipmentItem::gia);
        } else if ("SỐ LƯỢNG".equals(sortType)) {
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
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(5, 12, 5, 12));
        return btn;
    }

    private JButton createMiniActionButton(String text, Color bg, Color fg) {
        JButton button = createPillButton(text, bg, fg, true);
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setBorder(new EmptyBorder(6, 10, 6, 10));
        return button;
    }
}
