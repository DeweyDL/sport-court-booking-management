package com.sportcourt.modules.customer.view;

import com.formdev.flatlaf.FlatLightLaf;
import com.sportcourt.common.style.AppDialog;
import com.sportcourt.common.style.AppFonts;
import com.sportcourt.modules.customer.controller.ManageCustomerController;
import com.sportcourt.modules.customer.dto.CreateCustomerRequest;
import com.sportcourt.modules.customer.dto.CustomerProfile;
import com.sportcourt.modules.customer.dto.CustomerResult;
import com.sportcourt.modules.customer.dto.CustomerSummary;
import com.sportcourt.modules.customer.dto.UpdateCustomerRequest;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ManageCustomerScreen extends JPanel implements Scrollable {
    private static final Color ALTERNATE_ROW_BG = new Color(248, 250, 252);
    private static final int HEADER_HEIGHT = 45;
    private static final int ROW_HEIGHT = 64;
    private static final int COLUMN_GAP = 16;
    private static final int COL_ID = 115;
    private static final int COL_NAME = 220;
    private static final int COL_PHONE = 150;
    private static final int COL_ADDRESS = 260;
    private static final int COL_TIER = 150;
    private static final int COL_STATUS = 145;
    private static final int COL_REVENUE = 150;
    private static final int COL_ACTIONS = 255;
    private static final int TABLE_WIDTH = COL_ID + COL_NAME + COL_PHONE + COL_ADDRESS + COL_TIER
            + COL_STATUS + COL_REVENUE + COL_ACTIONS + COLUMN_GAP * 7 + 48;

    private final ManageCustomerController controller = new ManageCustomerController();
    private final List<CustomerVm> customers = new ArrayList<>();

    private final JPanel tablePanel = new JPanel();
    private final JLabel footerLabel = new JLabel("Đang tải dữ liệu...");
    private final JTextField searchField = new JTextField(30);
    private final JPanel searchWrapper = new JPanel(new BorderLayout());
    private final JComboBox<String> cbSort = new JComboBox<>(new String[]{
            "Họ tên",
            "Doanh thu",
            "Trạng thái"
    });
    private final JButton btnSortDir = new JButton("\u25B2");

    private CustomerVm selectedCustomer;
    private boolean sortAscending = true;

    public ManageCustomerScreen() {
        AppFonts.register();
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(100, 70, 50, 70));

        add(createPage(), BorderLayout.CENTER);
        loadCustomers("");
    }

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

        JLabel title = new JLabel("QUẢN LÝ KHÁCH HÀNG");
        title.setFont(new Font("Lexend", Font.BOLD, 30));
        title.setForeground(new Color(30, 31, 36));
        title.setBorder(new EmptyBorder(0, 20, 0, 0));

        JLabel subtitle = new JLabel("Quản lý thông tin khách hàng và trạng thái tài khoản.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(103, 112, 133));
        subtitle.setBorder(new EmptyBorder(5, 20, 20, 0));

        header.add(title);
        header.add(subtitle);
        return header;
    }

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

        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setBackground(Color.WHITE);
        topSection.add(createToolbar());
        container.add(topSection, BorderLayout.NORTH);

        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
        tablePanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(tablePanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setColumnHeaderView(createTableHeader());
        container.add(scrollPane, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(20, 20, 0, 20));
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        footerLabel.setForeground(new Color(107, 114, 128));
        footer.add(footerLabel, BorderLayout.WEST);
        container.add(footer, BorderLayout.SOUTH);

        return container;
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(new EmptyBorder(10, 20, 20, 20));

        JPanel leftToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftToolbar.setBackground(Color.WHITE);

        JLabel tableTitle = new JLabel("DANH SÁCH KHÁCH HÀNG");
        tableTitle.setFont(new Font("Lexend", Font.BOLD, 22));

        JButton addBtn = createPillButton("+ Thêm khách hàng", new Color(228, 250, 226), new Color(16, 110, 0), true);
        addBtn.setFont(new Font("Lexend", Font.BOLD, 17));
        addBtn.setBorder(new EmptyBorder(4, 12, 6, 12));
        addBtn.addActionListener(event -> openCreateDialog());
        JPanel addBtnWrapper = new JPanel(new BorderLayout());
        addBtnWrapper.setOpaque(false);
        addBtnWrapper.setBorder(new EmptyBorder(0, 0, 0, 0));
        addBtnWrapper.add(addBtn, BorderLayout.CENTER);

        leftToolbar.add(tableTitle);
        leftToolbar.add(addBtnWrapper);
        toolbar.add(leftToolbar, BorderLayout.WEST);

        JPanel rightToolbar = new JPanel();
        rightToolbar.setLayout(new BoxLayout(rightToolbar, BoxLayout.X_AXIS));
        rightToolbar.setBackground(Color.WHITE);
        rightToolbar.setBorder(new EmptyBorder(0, 6, 0, 0));
        rightToolbar.add(createSortWrapper());
        rightToolbar.add(Box.createHorizontalStrut(10));
        rightToolbar.add(createSearchFieldWithIcon());
        toolbar.add(rightToolbar, BorderLayout.EAST);

        return toolbar;
    }

    private JPanel createSearchFieldWithIcon() {
        searchWrapper.removeAll();
        searchWrapper.setOpaque(false);
        searchWrapper.setPreferredSize(new Dimension(270, 41));
        searchWrapper.setMaximumSize(new Dimension(270, 41));

        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setPreferredSize(new Dimension(270, 41));
        searchField.putClientProperty("JTextField.placeholderText", "Tìm theo tên hoặc số điện thoại...");
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
        innerPanel.setPreferredSize(new Dimension(270, 41));
        innerPanel.setMaximumSize(new Dimension(270, 41));
        innerPanel.setBorder(new EmptyBorder(0, 12, 0, 12));
        innerPanel.add(iconLabel, BorderLayout.WEST);
        innerPanel.add(searchField, BorderLayout.CENTER);

        searchWrapper.add(innerPanel, BorderLayout.CENTER);
        return searchWrapper;
    }

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

        gbc.weightx = 0.07; header.add(createFlexibleCell(createHeaderLabel("MÃ KH"), SwingConstants.CENTER, new Color(248, 249, 250), 0, 8), gbc);
        gbc.weightx = 0.20; header.add(createFlexibleCell(createHeaderLabel("HỌ TÊN"), SwingConstants.CENTER, new Color(248, 249, 250), 0, 8), gbc);
        gbc.weightx = 0.11; header.add(createFlexibleCell(createHeaderLabel("SĐT"), SwingConstants.CENTER, new Color(248, 249, 250), 0, 8), gbc);
        gbc.weightx = 0.10; header.add(createFlexibleCell(createHeaderLabel("ĐỊA CHỈ"), SwingConstants.CENTER, new Color(248, 249, 250), 0, 8), gbc);
        gbc.weightx = 0.18; header.add(createFlexibleCell(createHeaderLabel("HẠNG"), SwingConstants.CENTER, new Color(248, 249, 250), 0, 8), gbc);
        gbc.weightx = 0.08; header.add(createFlexibleCell(createHeaderLabel("TRẠNG THÁI"), SwingConstants.CENTER, new Color(248, 249, 250), 0, 8), gbc);
        gbc.weightx = 0.08; header.add(createFlexibleCell(createHeaderLabel("DOANH THU"), SwingConstants.CENTER, new Color(248, 249, 250), 0, 8), gbc);
        gbc.weightx = 0.18; gbc.insets = new Insets(0, 0, 0, 0); header.add(createFlexibleCell(createHeaderLabel("THAO TÁC"), SwingConstants.CENTER, new Color(248, 249, 250), 0, 8), gbc);
        return header;
    }

    private JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 17));
        label.setForeground(new Color(107, 114, 128));
        return label;
    }

    private JPanel createDataRow(CustomerVm customer, int rowIndex) {
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

        JLabel idLabel = new JLabel(valueOrDash(customer.getMaKhachHang()));
        idLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        idLabel.setForeground(new Color(22, 163, 74));
        
        gbc.weightx = 0.07; row.add(createFlexibleCell(idLabel, SwingConstants.LEFT, rowBg, 0, 8), gbc);
        gbc.weightx = 0.20; row.add(createFlexibleCell(createCellLabel(customer.getHoTen(), new Color(17, 24, 39)), SwingConstants.LEFT, rowBg, 0, 8), gbc);
        gbc.weightx = 0.11; row.add(createFlexibleCell(createCellLabel(customer.getSdt(), new Color(75, 85, 99)), SwingConstants.CENTER, rowBg, 0, 8), gbc);
        gbc.weightx = 0.10; row.add(createFlexibleCell(createWrappedAddressLabel(customer.getDiaChi(), rowBg), SwingConstants.LEFT, rowBg, 0, 8), gbc);
        gbc.weightx = 0.18; row.add(createFlexibleCell(createTierCell(customer.getMaHang()), SwingConstants.CENTER, rowBg, 0, 8), gbc);
        gbc.weightx = 0.08; row.add(createFlexibleCell(createStatusPill(customer.getTrangThai()), SwingConstants.CENTER, rowBg, 0, 8), gbc);
        gbc.weightx = 0.08; row.add(createFlexibleCell(createCellLabel(formatCurrency(customer.getDoanhThu()), new Color(17, 24, 39)), SwingConstants.CENTER, rowBg, 0, 8), gbc);

        JPanel actionGroup = new JPanel();
        actionGroup.setLayout(new BoxLayout(actionGroup, BoxLayout.X_AXIS));
        actionGroup.setOpaque(false);

        boolean inactive = "INACTIVE".equalsIgnoreCase(customer.getTrangThai());
        JButton statusBtn = inactive
                ? createMiniActionButton("Khôi phục", new Color(228, 250, 226), new Color(16, 110, 0))
                : createMiniActionButton("Xóa", new Color(254, 226, 226), new Color(185, 28, 28));
        Dimension statusBtnSize = new Dimension(inactive ? 88 : 80, 30);
        statusBtn.setPreferredSize(statusBtnSize);
        statusBtn.setMinimumSize(statusBtnSize);
        statusBtn.setMaximumSize(statusBtnSize);
        statusBtn.addActionListener(event -> {
            selectedCustomer = customer;
            if (inactive) {
                restoreSelectedCustomer();
            } else {
                deleteSelectedCustomer();
            }
        });
        actionGroup.add(statusBtn);
        actionGroup.add(Box.createHorizontalStrut(10));

        JButton editBtn = createMiniActionButton("Chỉnh sửa", new Color(239, 246, 255), new Color(29, 78, 216));
        Dimension editBtnSize = new Dimension(89, 30);
        editBtn.setPreferredSize(editBtnSize);
        editBtn.setMinimumSize(editBtnSize);
        editBtn.setMaximumSize(editBtnSize);
        editBtn.addActionListener(event -> {
            selectedCustomer = customer;
            openEditForSelectedCustomer();
        });
        actionGroup.add(editBtn);

        JPanel actionCell = new JPanel(new GridBagLayout());
        actionCell.setBackground(rowBg);
        actionCell.setOpaque(true);
        actionCell.add(actionGroup);
        
        gbc.weightx = 0.18; gbc.insets = new Insets(0, 0, 0, 0); row.add(createFlexibleCell(actionCell, SwingConstants.CENTER, rowBg, 0, 0), gbc);

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

    private JPanel createTierCell(String tierName) {
        String displayText = (tierName == null || tierName.isBlank()) ? "CHƯA CÓ" : tierName;
        boolean hasTier = tierName != null && !tierName.isBlank();
        Color background = hasTier ? new Color(239, 246, 255) : new Color(243, 244, 246);
        Color foreground = hasTier ? new Color(29, 78, 216) : new Color(75, 85, 99);

        JPanel wrapper = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(background);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
            }
        };
        wrapper.setOpaque(false);
        Dimension fixedTierSize = new Dimension(130, 26);
        wrapper.setPreferredSize(fixedTierSize);
        wrapper.setMinimumSize(fixedTierSize);
        wrapper.setMaximumSize(fixedTierSize);

        JLabel textLabel = new JLabel(displayText);
        textLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        textLabel.setForeground(foreground);
        wrapper.add(textLabel);

        // Wrap in a centering panel
        JPanel container = new JPanel(new GridBagLayout());
        container.setOpaque(false);
        container.add(wrapper);
        return container;
    }

    private JPanel createStatusPill(String trangThai) {
        boolean isActive = "ACTIVE".equalsIgnoreCase(trangThai);
        Color background = isActive ? new Color(228, 250, 226) : new Color(254, 226, 226);
        Color foreground = isActive ? new Color(16, 110, 0) : new Color(185, 28, 28);

        JPanel wrapper = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(background);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
            }
        };
        wrapper.setOpaque(false);
        Dimension size = new Dimension(92, 24);
        wrapper.setPreferredSize(size);
        wrapper.setMinimumSize(size);
        wrapper.setMaximumSize(size);

        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);

        JPanel dot = createStatusDot(foreground);
        JLabel textLabel = new JLabel(isActive ? "Active" : "Inactive");
        textLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        textLabel.setForeground(foreground);

        GridBagConstraints dotConstraints = new GridBagConstraints();
        dotConstraints.gridx = 0;
        dotConstraints.gridy = 0;
        dotConstraints.insets = new Insets(2, 0, 0, 7);
        content.add(dot, dotConstraints);

        GridBagConstraints textConstraints = new GridBagConstraints();
        textConstraints.gridx = 1;
        textConstraints.gridy = 0;
        content.add(textLabel, textConstraints);

        wrapper.add(content);

        // Wrap in a centering panel
        JPanel container = new JPanel(new GridBagLayout());
        container.setOpaque(false);
        container.add(wrapper);
        return container;
    }

    private JPanel createStatusDot(Color color) {
        JPanel dot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        Dimension size = new Dimension(5, 5);
        dot.setOpaque(false);
        dot.setPreferredSize(size);
        dot.setMinimumSize(size);
        dot.setMaximumSize(size);
        return dot;
    }

    private JPanel createEmptyRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setBorder(new EmptyBorder(24, 26, 24, 26));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));

        JLabel msg = new JLabel("Không tìm thấy khách hàng phù hợp.");
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        msg.setForeground(new Color(107, 114, 128));
        row.add(msg, BorderLayout.CENTER);
        return row;
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

    private JLabel createCellLabel(String text, Color fg) {
        JLabel label = new JLabel(valueOrDash(text));
        label.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        label.setForeground(fg);
        return label;
    }

    private JLabel createWrappedAddressLabel(String text, Color bg) {
        boolean hasAddress = text != null && !text.isBlank();
        String value = hasAddress ? text.trim() : "--";

        JLabel label = new JLabel(value);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(new Color(43, 47, 55));
        label.setHorizontalAlignment(SwingConstants.LEFT);
        if (hasAddress) {
            label.setToolTipText(text.trim());
        }
        return label;
    }

    private void loadCustomers(String keyword) {
        String selectedCode = selectedCustomer == null ? null : selectedCustomer.getMaKhachHang();
        CustomerResult<List<CustomerSummary>> result = controller.searchByName(keyword);
        if (!result.success()) {
            AppDialog.showError(this, normalizeError("Không thể tải danh sách khách hàng.", result.message()));
            return;
        }

        customers.clear();
        for (CustomerSummary summary : result.data()) {
            customers.add(CustomerVm.fromSummary(summary));
        }

        sortCustomers();
        renderTable();
        restoreSelection(selectedCode);
    }

    private void renderTable() {
        tablePanel.removeAll();

        if (customers.isEmpty()) {
            tablePanel.add(createEmptyRow());
        } else {
            int index = 0;
            for (CustomerVm customer : customers) {
                tablePanel.add(createDataRow(customer, index++));
            }
        }

        footerLabel.setText("Hiển thị " + customers.size() + " khách hàng");
        tablePanel.revalidate();
        tablePanel.repaint();
    }

    private void bindSearchListener() {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshCustomers();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshCustomers();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshCustomers();
            }
        });
    }

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
            String selectedCode = selectedCustomer == null ? null : selectedCustomer.getMaKhachHang();
            sortCustomers();
            renderTable();
            restoreSelection(selectedCode);
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
            updateSortDirectionButton();
            String selectedCode = selectedCustomer == null ? null : selectedCustomer.getMaKhachHang();
            sortCustomers();
            renderTable();
            restoreSelection(selectedCode);
        });
        updateSortDirectionButton();

        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 28, 28);
                g2.dispose();
            }

            @Override
            public void paint(Graphics g) {
                super.paint(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(229, 231, 235));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 28, 28);
                g2.dispose();
            }
        };
        wrapper.setOpaque(false);
        wrapper.setPreferredSize(new Dimension(214, 41));
        wrapper.setMaximumSize(new Dimension(214, 41));
        wrapper.add(cbSort, BorderLayout.CENTER);
        wrapper.add(btnSortDir, BorderLayout.EAST);
        return wrapper;
    }

    private void sortCustomers() {
        String sortType = (String) cbSort.getSelectedItem();
        Comparator<CustomerVm> comparator;
        if ("Doanh thu".equals(sortType)) {
            comparator = Comparator.comparing(
                    (CustomerVm customer) -> customer.getDoanhThu() == null ? BigDecimal.ZERO : customer.getDoanhThu()
            ).thenComparing(customer -> normalizedSortKey(customer.getHoTen()));
        } else if ("Trạng thái".equals(sortType)) {
            comparator = Comparator.comparingInt((CustomerVm customer) -> statusOrder(customer.getTrangThai()))
                    .thenComparing(customer -> normalizedSortKey(customer.getHoTen()));
        } else {
            comparator = (left, right) -> compareNameRightToLeft(left.getHoTen(), right.getHoTen());
            comparator = comparator.thenComparing(customer -> normalizedSortKey(customer.getMaKhachHang()));
        }
        if (!sortAscending) {
            comparator = comparator.reversed();
        }
        customers.sort(comparator);
    }

    private void updateSortDirectionButton() {
        btnSortDir.setText(sortAscending ? "\u25B2" : "\u25BC");
        btnSortDir.setToolTipText(sortAscending
                ? "Đang sắp xếp tăng dần"
                : "Đang sắp xếp giảm dần");
    }

    private int statusOrder(String status) {
        if ("ACTIVE".equalsIgnoreCase(status)) {
            return 0;
        }
        if ("INACTIVE".equalsIgnoreCase(status)) {
            return 1;
        }
        return 2;
    }

    private String normalizedSortKey(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('\u0111', 'd')
                .replace('\u0110', 'D');
        return normalized.toLowerCase(Locale.ROOT);
    }

    private int compareNameRightToLeft(String leftName, String rightName) {
        String[] leftParts = normalizedSortKey(leftName).split("\\s+");
        String[] rightParts = normalizedSortKey(rightName).split("\\s+");

        int leftIndex = leftParts.length - 1;
        int rightIndex = rightParts.length - 1;
        while (leftIndex >= 0 && rightIndex >= 0) {
            int compared = leftParts[leftIndex].compareTo(rightParts[rightIndex]);
            if (compared != 0) {
                return compared;
            }
            leftIndex--;
            rightIndex--;
        }
        return Integer.compare(leftParts.length, rightParts.length);
    }

    private void refreshCustomers() {
        loadCustomers(searchField.getText().trim());
    }

    private void restoreSelection(String maKhachHang) {
        selectedCustomer = null;
        if (maKhachHang == null) {
            return;
        }
        for (CustomerVm customer : customers) {
            if (customer.getMaKhachHang().equals(maKhachHang)) {
                selectedCustomer = customer;
                return;
            }
        }
    }

    private void openCreateDialog() {
        CreateCustomerRequest request = CustomerCreateDialog.show(this);
        if (request == null) {
            return;
        }

        CustomerResult<CustomerProfile> result = controller.create(request);
        if (!result.success()) {
            AppDialog.showError(this, normalizeError("Tạo khách hàng chưa thành công.", result.message()));
            return;
        }

        AppDialog.showInfo(this, "Đã thêm khách hàng.");
        refreshCustomers();
    }

    private void openEditForSelectedCustomer() {
        if (selectedCustomer == null) {
            return;
        }

        String selectedCode = selectedCustomer.getMaKhachHang();
        CustomerResult<CustomerProfile> profileResult = controller.getProfile(selectedCode);
        if (!profileResult.success() || profileResult.data() == null) {
            AppDialog.showError(this, normalizeError("Không lấy được hồ sơ khách hàng.", profileResult.message()));
            return;
        }

        UpdateCustomerRequest request = CustomerEditDialog.show(this, profileResult.data());
        if (request == null) {
            return;
        }

        CustomerResult<CustomerProfile> updateResult = controller.update(selectedCode, request);
        if (!updateResult.success()) {
            AppDialog.showError(this, normalizeError("Cập nhật khách hàng chưa thành công.", updateResult.message()));
            return;
        }

        AppDialog.showInfo(this, "Đã cập nhật thông tin khách hàng.");
        refreshCustomers();
        restoreSelection(selectedCode);
    }

    private void deleteSelectedCustomer() {
        if (selectedCustomer == null) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa khách hàng này?",
                "Xác nhận xóa",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.OK_OPTION) {
            return;
        }

        CustomerResult<Void> result = controller.softDelete(selectedCustomer.getMaKhachHang());
        if (!result.success()) {
            AppDialog.showError(this, normalizeError("Không thể xóa khách hàng lúc này.", result.message()));
            return;
        }

        AppDialog.showInfo(this, normalizeError("Xóa thành công.", result.message()));
        refreshCustomers();
    }

    private void restoreSelectedCustomer() {
        if (selectedCustomer == null) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn khôi phục khách hàng này?",
                "Xác nhận khôi phục",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm != JOptionPane.OK_OPTION) {
            return;
        }

        CustomerResult<Void> result = controller.restore(selectedCustomer.getMaKhachHang());
        if (!result.success()) {
            AppDialog.showError(this, normalizeError("Không thể khôi phục khách hàng lúc này.", result.message()));
            return;
        }

        AppDialog.showInfo(this, normalizeError("Đã khôi phục tài khoản khách hàng.", result.message()));
        refreshCustomers();
    }

    private String normalizeError(String fallback, String detail) {
        if (detail == null || detail.isBlank()) {
            return fallback;
        }
        String normalized = detail.trim();
        if (normalized.endsWith(".") || normalized.endsWith("!") || normalized.endsWith("?")) {
            return normalized;
        }
        return normalized + ".";
    }

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

    private String valueOrDash(String text) {
        return text == null || text.isBlank() ? "--" : text;
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
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
        btn.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, 13));
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

    public static JFrame createPreviewFrame() {
        JFrame frame = new JFrame("RENSTA - Quản lý khách hàng");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setContentPane(new ManageCustomerScreen());
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setMinimumSize(new Dimension(1280, 720));
        frame.setLocationRelativeTo(null);
        return frame;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> createPreviewFrame().setVisible(true));
    }
}
