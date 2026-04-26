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
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ManageCustomerPreviewScreen extends JPanel {
    private static final int TABLE_COLUMN_GAP = 0;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int[] TABLE_COLUMN_WIDTHS = {120, 165, 140, 210, 110, 95, 110, 130, 180};
    private static final int ADDRESS_WRAP_WIDTH = 190;

    private final ManageCustomerController controller = new ManageCustomerController();
    private final List<CustomerVm> customers = new ArrayList<>();

    private final JLabel titleLabel = new JLabel("QUẢN LÝ KHÁCH HÀNG");
    private final JTextField txtSearch = new JTextField();
    private final JComboBox<String> cbSort = new JComboBox<>(new String[]{
            "Họ tên",
            "Doanh thu",
            "Trạng thái"
    });
    private final JButton btnSortDirection = new JButton("\u25B2");
    private final JLabel footerLabel = new JLabel("Đang hiển thị 0 / 0 khách hàng");
    private final JPanel tableBodyPanel = new JPanel();

    private CustomerVm selectedCustomer;
    private boolean sortAscending = true;

    public ManageCustomerPreviewScreen() {
        AppFonts.register();
        setLayout(new BorderLayout(0, 18));
        setBackground(new Color(247, 247, 251));
        setBorder(new EmptyBorder(90, 70, 40, 70));

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);

        applyEvents();
        loadCustomers("");
    }

    private JPanel createHeader() {
        JPanel headerWrapper = new JPanel();
        headerWrapper.setLayout(new BoxLayout(headerWrapper, BoxLayout.Y_AXIS));
        headerWrapper.setOpaque(false);
        headerWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerWrapper.setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.setBorder(new EmptyBorder(0, 20, 0, 0));

        titleLabel.setFont(new Font("Lexend", Font.BOLD, 30));
        titleLabel.setForeground(new Color(30, 31, 36));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleRow.add(titleLabel, BorderLayout.WEST);

        JPanel subtitleRow = new JPanel(new BorderLayout());
        subtitleRow.setOpaque(false);
        subtitleRow.setBorder(new EmptyBorder(5, 20, 20, 0));
        JLabel subtitle = new JLabel("Hiển thị dữ liệu khách hàng và hỗ trợ tìm kiếm nhanh.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(103, 112, 133));
        subtitleRow.add(subtitle, BorderLayout.WEST);

        headerWrapper.add(titleRow);
        headerWrapper.add(subtitleRow);
        return headerWrapper;
    }

    private JPanel createMainContent() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);
        mainPanel.add(createCustomerSection(), BorderLayout.CENTER);
        return mainPanel;
    }

    private JPanel createCustomerSection() {
        JPanel sectionPanel = new JPanel(new BorderLayout(0, 14));
        sectionPanel.setOpaque(false);
        sectionPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel titlePanel = new JPanel(new BorderLayout(10, 0));
        titlePanel.setOpaque(true);
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(new EmptyBorder(12, 20, 18, 20));

        JPanel titleTextPanel = new JPanel();
        titleTextPanel.setLayout(new BoxLayout(titleTextPanel, BoxLayout.Y_AXIS));
        titleTextPanel.setOpaque(false);
        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightControls.setOpaque(false);

        JLabel title = new JLabel("Danh sách khách hàng");
        title.setFont(new Font("Lexend", Font.BOLD, 18));
        title.setForeground(new Color(35, 37, 43));

        txtSearch.setPreferredSize(new Dimension(310, 38));
        txtSearch.setFont(AppFonts.lexendRegular(13f));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm theo tên hoặc số điện thoại...");
        txtSearch.setBorder(new EmptyBorder(0, 8, 0, 14));
        txtSearch.setOpaque(false);
        txtSearch.putClientProperty("JComponent.roundRect", true);
        txtSearch.putClientProperty("JTextField.arc", 999);
        JLabel searchIconLabel = new JLabel(scaleIcon("/icon/search.png", 16, 16));
        searchIconLabel.setBorder(new EmptyBorder(0, 12, 0, 0));

        JButton createButton = createPillButton("+ Thêm khách hàng", new Color(220, 252, 231), new Color(22, 101, 52), true);
        createButton.setPreferredSize(new Dimension(160, 36));
        createButton.setBorder(new EmptyBorder(7, 14, 7, 14));
        createButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        createButton.addActionListener(event -> openCreateDialog());

        JPanel titleWithActionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleWithActionRow.setOpaque(false);
        titleWithActionRow.add(title);
        titleWithActionRow.add(createButton);

        JPanel searchWrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.setColor(new Color(229, 231, 235));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                g2.dispose();
            }
        };
        searchWrapper.setOpaque(false);
        searchWrapper.setPreferredSize(new Dimension(310, 38));
        searchWrapper.add(searchIconLabel, BorderLayout.WEST);
        searchWrapper.add(txtSearch, BorderLayout.CENTER);

        cbSort.setFont(AppFonts.lexendRegular(13f));
        cbSort.setFocusable(false);
        cbSort.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        cbSort.setOpaque(false);
        cbSort.setBackground(Color.WHITE);
        cbSort.putClientProperty("JComponent.roundRect", true);
        cbSort.putClientProperty("JComponent.arc", 999);
        cbSort.putClientProperty("JComboBox.buttonStyle", "button");
        cbSort.putClientProperty("JComboBox.padding", new Insets(0, 4, 0, 4));
        cbSort.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                Object displayValue = index < 0 ? "Sắp xếp: " + value : value;
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, displayValue, index, isSelected, cellHasFocus
                );
                label.setBorder(new EmptyBorder(6, 10, 6, 10));
                return label;
            }
        });
        btnSortDirection.setFont(new Font("Segoe UI Symbol", Font.BOLD, 11));
        btnSortDirection.setForeground(new Color(75, 85, 99));
        btnSortDirection.setBorder(new EmptyBorder(0, 0, 0, 12));
        btnSortDirection.setContentAreaFilled(false);
        btnSortDirection.setBorderPainted(false);
        btnSortDirection.setFocusPainted(false);
        btnSortDirection.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSortDirection.setOpaque(false);
        updateSortDirectionButton();
        JPanel sortWrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.setColor(new Color(229, 231, 235));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                g2.dispose();
            }
        };
        sortWrapper.setOpaque(false);
        sortWrapper.setPreferredSize(new Dimension(230, 38));
        sortWrapper.add(cbSort, BorderLayout.CENTER);
        sortWrapper.add(btnSortDirection, BorderLayout.EAST);

        titleTextPanel.add(titleWithActionRow);
        rightControls.add(searchWrapper);
        rightControls.add(sortWrapper);

        titlePanel.add(titleTextPanel, BorderLayout.WEST);
        titlePanel.add(rightControls, BorderLayout.EAST);

        JPanel tableCard = createRoundedTableCard();
        tableCard.add(createAlignedCustomerHeader(), BorderLayout.NORTH);

        tableBodyPanel.setLayout(new BoxLayout(tableBodyPanel, BoxLayout.Y_AXIS));
        tableBodyPanel.setBackground(Color.WHITE);
        JScrollPane tableScrollPane = new JScrollPane(tableBodyPanel);
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableScrollPane.getViewport().setBackground(Color.WHITE);
        tableScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        tableScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        tableCard.add(tableScrollPane, BorderLayout.CENTER);
        tableCard.add(createFooter(), BorderLayout.SOUTH);

        JPanel contentFrame = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 28, 28);
                g2.setColor(new Color(236, 236, 239));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 28, 28);
                g2.dispose();
            }

            @Override
            protected void paintChildren(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Shape shape = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 28, 28);
                g2.setClip(shape);
                super.paintChildren(g2);
                g2.dispose();
            }
        };
        contentFrame.setOpaque(false);
        contentFrame.setBackground(Color.WHITE);
        contentFrame.add(titlePanel, BorderLayout.NORTH);
        contentFrame.add(tableCard, BorderLayout.CENTER);

        sectionPanel.add(contentFrame, BorderLayout.CENTER);
        return sectionPanel;
    }

    private JPanel createRoundedTableCard() {
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setOpaque(true);
        tableCard.setBackground(Color.WHITE);
        return tableCard;
    }

    private JPanel createCustomerHeader() {
        JPanel headerPanel = new JPanel(new GridLayout(1, 7, TABLE_COLUMN_GAP, 0));
        headerPanel.setBackground(new Color(241, 242, 246));
        headerPanel.setBorder(new EmptyBorder(16, 26, 16, 26));
        headerPanel.add(createHeaderLabel("MÃ KH", SwingConstants.CENTER));
        headerPanel.add(createHeaderLabel("HỌ TÊN", SwingConstants.CENTER));
        headerPanel.add(createHeaderLabel("SỐ ĐIỆN THOẠI", SwingConstants.CENTER));
        headerPanel.add(createHeaderLabel("DOANH THU", SwingConstants.CENTER));
        headerPanel.add(createHeaderLabel("TRẠNG THÁI", SwingConstants.CENTER));
        headerPanel.add(createHeaderLabel("HẠNG", SwingConstants.CENTER));
        headerPanel.add(createHeaderLabel("THAO TÁC", SwingConstants.CENTER));
        return headerPanel;
    }

    private JPanel createAlignedCustomerHeader() {
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setBackground(new Color(241, 242, 246));
        headerPanel.setBorder(new EmptyBorder(14, 24, 14, 24));
        addColumnCell(headerPanel, createHeaderLabel("MÃ KH", SwingConstants.LEFT), 0, SwingConstants.LEFT);
        addColumnCell(headerPanel, createHeaderLabel("HỌ TÊN", SwingConstants.CENTER), 1, SwingConstants.CENTER);
        addColumnCell(headerPanel, createHeaderLabel("SỐ ĐIỆN THOẠI", SwingConstants.CENTER), 2, SwingConstants.CENTER);
        addColumnCell(headerPanel, createHeaderLabel("ĐỊA CHỈ", SwingConstants.CENTER), 3, SwingConstants.CENTER);
        addColumnCell(headerPanel, createHeaderLabel("NGÀY SINH", SwingConstants.CENTER), 4, SwingConstants.CENTER);
        addColumnCell(headerPanel, createHeaderLabel("HẠNG", SwingConstants.CENTER), 5, SwingConstants.CENTER);
        addColumnCell(headerPanel, createHeaderLabel("TRẠNG THÁI", SwingConstants.CENTER), 6, SwingConstants.CENTER);
        addColumnCell(headerPanel, createHeaderLabel("DOANH THU", SwingConstants.CENTER), 7, SwingConstants.CENTER);
        addColumnCell(headerPanel, createHeaderLabel("THAO TÁC", SwingConstants.CENTER), 8, SwingConstants.CENTER);
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

    private JLabel createHeaderLabel(String text, int alignment) {
        JLabel label = new JLabel(text, alignment);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(94, 103, 82));
        return label;
    }

    private void applyEvents() {
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
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
        cbSort.addActionListener(event -> {
            String selectedCode = selectedCustomer == null ? null : selectedCustomer.getMaKhachHang();
            sortCustomers();
            renderCustomerTable();
            restoreSelection(selectedCode);
        });
        btnSortDirection.addActionListener(event -> {
            sortAscending = !sortAscending;
            updateSortDirectionButton();
            String selectedCode = selectedCustomer == null ? null : selectedCustomer.getMaKhachHang();
            sortCustomers();
            renderCustomerTable();
            restoreSelection(selectedCode);
        });
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
        renderCustomerTable();
        restoreSelection(selectedCode);
    }

    private void sortCustomers() {
        String sortType = (String) cbSort.getSelectedItem();
        Comparator<CustomerVm> comparator;
        if (sortType != null && sortType.contains("Doanh thu")) {
            comparator = Comparator.comparing(
                    (CustomerVm customer    ) -> customer.getDoanhThu() == null ? BigDecimal.ZERO : customer.getDoanhThu()
            ).thenComparing((CustomerVm customer) -> normalizedSortKey(customer.getHoTen()));
        } else if (sortType != null && sortType.contains("Trạng thái")) {
            comparator = Comparator.comparingInt((CustomerVm customer) -> statusOrder(customer.getTrangThai()))
                    .thenComparing(customer -> normalizedSortKey(customer.getHoTen()));
        } else {
            comparator = (left, right) -> compareNameRightToLeft(left.getHoTen(), right.getHoTen());
            comparator = comparator
                    .thenComparing((CustomerVm customer) -> normalizedSortKey(customer.getMaKhachHang()));
        }
        if (!sortAscending) {
            comparator = comparator.reversed();
        }
        customers.sort(comparator);
    }

    private void updateSortDirectionButton() {
        btnSortDirection.setText(sortAscending ? "\u25B2" : "\u25BC");
        btnSortDirection.setToolTipText(sortAscending
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

    private void renderCustomerTable() {
        tableBodyPanel.removeAll();
        if (customers.isEmpty()) {
            tableBodyPanel.add(createEmptyRow());
        } else {
            for (CustomerVm customer : customers) {
                tableBodyPanel.add(createAlignedCustomerRow(customer));
            }
        }
        footerLabel.setText("Đang hiển thị " + customers.size() + " / " + customers.size() + " khách hàng");
        tableBodyPanel.revalidate();
        tableBodyPanel.repaint();
    }

    private JPanel createCustomerRow(CustomerVm customer) {
        JPanel rowPanel = new JPanel(new GridLayout(1, 7, TABLE_COLUMN_GAP, 0));
        rowPanel.setBackground(Color.WHITE);
        rowPanel.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(236, 236, 239)),
                new EmptyBorder(18, 26, 18, 26)
        ));
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));

        JLabel maKhLabel = createBodyLabel(customer.getMaKhachHang(), true);
        maKhLabel.setForeground(new Color(29, 78, 216));
        rowPanel.add(createTableCellWrapper(maKhLabel, SwingConstants.LEFT));
        rowPanel.add(createTableCellWrapper(createBodyLabel(customer.getHoTen(), false), SwingConstants.CENTER));
        rowPanel.add(createTableCellWrapper(createBodyLabel(customer.getSdt(), false), SwingConstants.CENTER));
        rowPanel.add(createTableCellWrapper(createBodyLabel(formatCurrency(customer.getDoanhThu()), false), SwingConstants.CENTER));
        rowPanel.add(createTableCellWrapper(createStatusPill(customer.getTrangThai()), SwingConstants.CENTER));
        rowPanel.add(createTableCellWrapper(createTierPill(customer.getMaHang()), SwingConstants.CENTER));

        JPanel actionGroup = new JPanel();
        actionGroup.setLayout(new BoxLayout(actionGroup, BoxLayout.X_AXIS));
        actionGroup.setOpaque(false);

        boolean inactive = "INACTIVE".equalsIgnoreCase(customer.getTrangThai());
        JButton statusBtn = inactive
                ? createMiniActionButton("Khôi phục", new Color(220, 252, 231), new Color(22, 101, 52))
                : createMiniActionButton("Xóa", new Color(254, 226, 226), new Color(185, 28, 28));
        Dimension actionButtonSize = new Dimension(88, 30);
        statusBtn.setPreferredSize(actionButtonSize);
        statusBtn.setMinimumSize(actionButtonSize);
        statusBtn.setMaximumSize(actionButtonSize);
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

        JButton detailBtn = createMiniActionButton("Chỉnh sửa", new Color(239, 246, 255), new Color(29, 78, 216));
        Dimension detailButtonSize = new Dimension(96, 30);
        detailBtn.setPreferredSize(detailButtonSize);
        detailBtn.setMinimumSize(detailButtonSize);
        detailBtn.setMaximumSize(detailButtonSize);
        detailBtn.addActionListener(event -> {
            selectedCustomer = customer;
            openEditForSelectedCustomer();
        });
        actionGroup.add(detailBtn);
        JPanel actionCell = createTableCellWrapper(actionGroup, SwingConstants.CENTER);
        actionCell.setBorder(new EmptyBorder(0, -5, 0, 0));
        rowPanel.add(actionCell);
        return rowPanel;
    }

    private JPanel createAlignedCustomerRow(CustomerVm customer) {
        JPanel rowPanel = new JPanel(new GridBagLayout());
        rowPanel.setBackground(Color.WHITE);
        rowPanel.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(236, 236, 239)),
                new EmptyBorder(14, 24, 14, 24)
        ));
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        rowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel maKhLabel = createBodyLabel(customer.getMaKhachHang(), true);
        maKhLabel.setForeground(new Color(29, 78, 216));
        addColumnCell(rowPanel, createTableCellWrapper(maKhLabel, SwingConstants.LEFT), 0, SwingConstants.LEFT);
        addColumnCell(rowPanel, createTableCellWrapper(createCenteredBodyLabel(customer.getHoTen(), false), SwingConstants.CENTER), 1, SwingConstants.CENTER);
        addColumnCell(rowPanel, createTableCellWrapper(createCenteredBodyLabel(customer.getSdt(), false), SwingConstants.CENTER), 2, SwingConstants.CENTER);
        addColumnCell(rowPanel, createTableCellWrapper(createWrappedAddressLabel(customer.getDiaChi()), SwingConstants.CENTER), 3, SwingConstants.CENTER);
        addColumnCell(rowPanel, createTableCellWrapper(createCenteredBodyLabel(formatDate(customer.getNgaySinh()), false), SwingConstants.CENTER), 4, SwingConstants.CENTER);
        addColumnCell(rowPanel, createTableCellWrapper(createTierPill(customer.getMaHang()), SwingConstants.CENTER), 5, SwingConstants.CENTER);
        addColumnCell(rowPanel, createTableCellWrapper(createStatusPill(customer.getTrangThai()), SwingConstants.CENTER), 6, SwingConstants.CENTER);
        addColumnCell(rowPanel, createTableCellWrapper(createCenteredBodyLabel(formatCurrency(customer.getDoanhThu()), false), SwingConstants.CENTER), 7, SwingConstants.CENTER);

        JPanel actionGroup = new JPanel();
        actionGroup.setLayout(new BoxLayout(actionGroup, BoxLayout.X_AXIS));
        actionGroup.setOpaque(false);

        boolean inactive = "INACTIVE".equalsIgnoreCase(customer.getTrangThai());
        JButton statusBtn = inactive
                ? createMiniActionButton("Khôi phục", new Color(220, 252, 231), new Color(22, 101, 52))
                : createMiniActionButton("Xóa", new Color(254, 226, 226), new Color(185, 28, 28));
        Dimension actionButtonSize = new Dimension(80, 28);
        statusBtn.setPreferredSize(actionButtonSize);
        statusBtn.setMinimumSize(actionButtonSize);
        statusBtn.setMaximumSize(actionButtonSize);
        statusBtn.addActionListener(event -> {
            selectedCustomer = customer;
            if (inactive) {
                restoreSelectedCustomer();
            } else {
                deleteSelectedCustomer();
            }
        });
        actionGroup.add(statusBtn);
        actionGroup.add(Box.createHorizontalStrut(6));

        JButton detailBtn = createMiniActionButton("Chỉnh sửa", new Color(239, 246, 255), new Color(29, 78, 216));
        Dimension detailButtonSize = new Dimension(86, 28);
        detailBtn.setPreferredSize(detailButtonSize);
        detailBtn.setMinimumSize(detailButtonSize);
        detailBtn.setMaximumSize(detailButtonSize);
        detailBtn.addActionListener(event -> {
            selectedCustomer = customer;
            openEditForSelectedCustomer();
        });
        actionGroup.add(detailBtn);
        addColumnCell(rowPanel, createTableCellWrapper(actionGroup, SwingConstants.CENTER), 8, SwingConstants.CENTER);
        return rowPanel;
    }

    private void addColumnCell(JPanel panel, Component component, int columnIndex, int alignment) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = columnIndex;
        g.gridy = 0;
        g.weightx = 0;
        g.fill = GridBagConstraints.NONE;
        g.insets = new Insets(0, 4, 0, 4);
        g.anchor = alignment == SwingConstants.LEFT ? GridBagConstraints.WEST : GridBagConstraints.CENTER;
        JPanel bounded = new JPanel(new BorderLayout());
        bounded.setOpaque(false);
        bounded.setPreferredSize(new Dimension(TABLE_COLUMN_WIDTHS[columnIndex], component.getPreferredSize().height));
        bounded.setMinimumSize(new Dimension(TABLE_COLUMN_WIDTHS[columnIndex], component.getMinimumSize().height));
        bounded.setMaximumSize(new Dimension(TABLE_COLUMN_WIDTHS[columnIndex], Integer.MAX_VALUE));
        bounded.add(component, BorderLayout.CENTER);
        panel.add(bounded, g);
    }

    private JPanel createTableCellWrapper(Component component, int alignment) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setMinimumSize(new Dimension(0, 0));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 0;
        g.weightx = 1.0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = alignment == SwingConstants.LEFT ? GridBagConstraints.WEST : GridBagConstraints.CENTER;
        panel.add(component, g);
        return panel;
    }

    private JPanel createTierPill(String tierName) {
        String displayText = (tierName == null || tierName.isBlank()) ? "CHƯA CÓ" : tierName;
        boolean hasTier = tierName != null && !tierName.isBlank();
        Color background = hasTier ? new Color(219, 234, 254) : new Color(229, 231, 235);
        Color foreground = hasTier ? new Color(30, 64, 175) : new Color(75, 85, 99);
        Dimension fixedTierSize = new Dimension(90, 30);

        JPanel pill = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
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
        wrapper.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 6));
        wrapper.setOpaque(false);
        wrapper.setPreferredSize(fixedTierSize);
        wrapper.setMinimumSize(fixedTierSize);
        wrapper.setMaximumSize(fixedTierSize);

        JLabel textLabel = new JLabel(displayText);
        textLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        textLabel.setForeground(foreground);

        wrapper.add(textLabel);
        pill.add(wrapper);
        return pill;
    }

    private JPanel createStatusPill(String trangThai) {
        boolean isActive = "ACTIVE".equalsIgnoreCase(trangThai);
        Color background = isActive ? new Color(216, 255, 208) : new Color(254, 226, 226);
        Color foreground = isActive ? new Color(44, 154, 16) : new Color(185, 28, 28);

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

        JLabel textLabel = new JLabel(isActive ? "Active" : "Inactive");
        textLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        textLabel.setForeground(foreground);

        wrapper.add(dotLabel);
        wrapper.add(textLabel);
        pill.add(wrapper);
        return pill;
    }

    private JPanel createEmptyRow() {
        JPanel rowPanel = new JPanel(new BorderLayout());
        rowPanel.setBackground(Color.WHITE);
        rowPanel.setBorder(new EmptyBorder(24, 26, 24, 26));
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));

        JLabel messageLabel = new JLabel("Chưa có khách hàng nào phù hợp.");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageLabel.setForeground(new Color(107, 114, 128));
        rowPanel.add(messageLabel, BorderLayout.CENTER);
        return rowPanel;
    }

    private JLabel createBodyLabel(String text, boolean bold) {
        JLabel label = new JLabel(text == null || text.isBlank() ? "--" : text);
        label.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, 13));
        label.setForeground(new Color(43, 47, 55));
        return label;
    }

    private JLabel createCenteredBodyLabel(String text, boolean bold) {
        JLabel label = createBodyLabel(text, bold);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    private JLabel createWrappedAddressLabel(String text) {
        String value = text == null || text.isBlank() ? "--" : escapeHtml(text.trim());
        JLabel label = new JLabel("<html><div style='width:" + ADDRESS_WRAP_WIDTH + "px;'>" + value + "</div></html>");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(new Color(43, 47, 55));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    private void refreshCustomers() {
        loadCustomers(txtSearch.getText().trim());
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

        CustomerResult<CustomerProfile> profileResult = controller.getProfile(selectedCustomer.getMaKhachHang());
        if (!profileResult.success() || profileResult.data() == null) {
            AppDialog.showError(this, normalizeError("Không lấy được hồ sơ khách hàng.", profileResult.message()));
            return;
        }

        UpdateCustomerRequest request = CustomerEditDialog.show(this, profileResult.data());
        if (request == null) {
            return;
        }

        CustomerResult<CustomerProfile> updateResult = controller.update(selectedCustomer.getMaKhachHang(), request);
        if (!updateResult.success()) {
            AppDialog.showError(this, normalizeError("Cập nhật khách hàng chưa thành công.", updateResult.message()));
            return;
        }

        AppDialog.showInfo(this, "Đã cập nhật thông tin khách hàng.");
        refreshCustomers();
        restoreSelection(selectedCustomer.getMaKhachHang());
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

    private String formatCurrency(BigDecimal value) {
        if (value == null) {
            return "0 VNĐ";
        }
        return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(value) + " VNĐ";
    }

    private String formatDate(LocalDate date) {
        return date == null ? "" : DATE_FORMAT.format(date);
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private Icon scaleIcon(String path, int width, int height) {
        URL resource = getClass().getResource(path);
        if (resource == null) {
            return new ImageIcon();
        }
        Image image = new ImageIcon(resource).getImage()
                .getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(image);
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
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setBorder(new EmptyBorder(6, 10, 6, 10));
        return button;
    }

    public static JFrame createPreviewFrame() {
        JFrame frame = new JFrame("RENSTA - Quản lý khách hàng");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setContentPane(new ManageCustomerPreviewScreen());
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
