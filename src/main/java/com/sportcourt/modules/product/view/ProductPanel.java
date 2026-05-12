package com.sportcourt.modules.product.view;

import com.sportcourt.common.style.AppFonts;
import com.sportcourt.common.style.CrudViewStyle;
import com.sportcourt.modules.product.controller.ProductController;
import com.sportcourt.modules.product.dto.ProductCreateRequest;
import com.sportcourt.modules.product.dto.ProductResponse;
import com.sportcourt.modules.product.dto.ProductSearchCriteria;
import com.sportcourt.modules.product.dto.ProductUpdateRequest;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ProductPanel extends JPanel implements Scrollable {
    private static final Color PAGE_BG = new Color(245, 247, 250);
    private static final Color ALTERNATE_ROW_BG = new Color(248, 250, 252);
    private static final Color BORDER = new Color(229, 231, 235);
    private static final Color TEXT = new Color(17, 24, 39);
    private static final Color MUTED = new Color(107, 114, 128);
    private static final int HEADER_HEIGHT = 52;
    private static final int ROW_HEIGHT = 72;
    private static final int COLUMN_GAP = 16;

    private final List<ProductVm> products = new ArrayList<>();
    private final JPanel tablePanel = new JPanel();
    private final JLabel footerLabel = new JLabel("Đang tải dữ liệu...");
    private final JTextField searchField = new JTextField(30);
    private final JPanel searchWrapper = new JPanel(new BorderLayout());
    private final JComboBox<String> cbSort = new JComboBox<>(new String[]{
            "Tên sản phẩm",
            "Đơn giá",
            "Tồn kho"
    });
    private final JButton btnSortDir = new JButton("\u25B2");
    private final JButton addButton;

    private ActionListener searchAction;
    private ActionListener addAction;
    private ActionListener updateAction;
    private ActionListener deleteAction;
    private ActionListener restoreAction;
    private ActionListener refreshAction;

    private ProductVm selectedProduct;
    private String selectedProductId;
    private boolean sortAscending = true;
    private boolean loading;

    public ProductPanel() {
        AppFonts.register();
        setLayout(new BorderLayout());
        CrudViewStyle.applyPageDefaults(this);

        addButton = createPillButton("+ Thêm sản phẩm", new Color(228, 250, 226), new Color(16, 110, 0), true);
        addButton.setFont(new Font("Lexend", Font.BOLD, 16));
        addButton.setBorder(new EmptyBorder(6, 22, 6, 22));
        CrudViewStyle.applyToolbarButtonHeight(addButton);

        add(createPage(), BorderLayout.CENTER);
        CrudViewStyle.installResponsiveTypography(this);
        bindEvents();
        new ProductController(this);
    }

    public void setSearchAction(ActionListener l) {
        this.searchAction = l;
    }

    public void setAddAction(ActionListener l) {
        this.addAction = l;
    }

    public void setUpdateAction(ActionListener l) {
        this.updateAction = l;
    }

    public void setDeleteAction(ActionListener l) {
        this.deleteAction = l;
    }

    public void setRestoreAction(ActionListener l) {
        this.restoreAction = l;
    }

    public void setRefreshAction(ActionListener l) {
        this.refreshAction = l;
    }

    public ProductSearchCriteria getSearchCriteria() {
        ProductSearchCriteria criteria = new ProductSearchCriteria();
        criteria.setKeyword(searchField.getText() == null ? "" : searchField.getText().trim());
        criteria.setIncludeDeleted(true);
        return criteria;
    }

    public String getSelectedProductId() {
        if (selectedProductId != null && !selectedProductId.trim().isEmpty()) {
            return selectedProductId;
        }
        return selectedProduct == null ? null : selectedProduct.getMaSp();
    }

    public ProductCreateRequest showCreateDialog() {
        return ProductCreateDialog.show(this, generateNextProductId());
    }

    public ProductUpdateRequest showUpdateDialog(ProductResponse product) {
        return ProductEditDialog.show(this, product);
    }

    public void showProductTable(List<ProductResponse> productResponses) {
        boolean searchFocused = searchField.isFocusOwner();
        int caretPosition = searchField.getCaretPosition();
        String selectedCode = selectedProductId;

        products.clear();
        if (productResponses != null) {
            for (ProductResponse response : productResponses) {
                products.add(ProductVm.fromResponse(response));
            }
        }

        sortProducts();
        renderTable();
        restoreSelection(selectedCode);

        if (searchFocused) {
            SwingUtilities.invokeLater(() -> {
                searchField.requestFocusInWindow();
                int length = searchField.getText() == null ? 0 : searchField.getText().length();
                searchField.setCaretPosition(Math.min(caretPosition, length));
            });
        }
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
        searchField.setEnabled(!loading);
        addButton.setEnabled(!loading);
        cbSort.setEnabled(!loading);
        btnSortDir.setEnabled(!loading);
        footerLabel.setText(loading ? "Đang tải dữ liệu..." : "Hiển thị " + products.size() + " sản phẩm");
        tablePanel.setEnabled(!loading);
        tablePanel.repaint();
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message == null ? "Có lỗi xảy ra." : message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    public boolean confirm(String message) {
        int result = JOptionPane.showConfirmDialog(this, message, "Xác nhận", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.OK_OPTION;
    }

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

        JLabel title = new JLabel("QUẢN LÝ SẢN PHẨM");
        title.setFont(new Font("Lexend", Font.BOLD, 30));
        title.setForeground(new Color(30, 31, 36));
        title.setBorder(new EmptyBorder(0, 20, 0, 0));

        JLabel subtitle = new JLabel("Quản lý thông tin sản phẩm, đơn giá và tồn kho.");
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
        container.setBorder(new EmptyBorder(12, 0, 16, 0));

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
        footerLabel.setForeground(MUTED);
        footer.add(footerLabel, BorderLayout.WEST);
        container.add(footer, BorderLayout.SOUTH);

        return container;
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(new EmptyBorder(8, 20, 14, 20));

        JPanel leftToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftToolbar.setBackground(Color.WHITE);

        JLabel tableTitle = new JLabel("DANH SÁCH SẢN PHẨM");
        tableTitle.setFont(new Font("Lexend", Font.BOLD, 22));

        JPanel addBtnWrapper = new JPanel(new BorderLayout());
        addBtnWrapper.setOpaque(false);
        addBtnWrapper.add(addButton, BorderLayout.CENTER);

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
        searchWrapper.setPreferredSize(new Dimension(CrudViewStyle.TOOLBAR_SEARCH_WIDTH, CrudViewStyle.TOOLBAR_CONTROL_HEIGHT));
        searchWrapper.setMaximumSize(new Dimension(CrudViewStyle.TOOLBAR_SEARCH_WIDTH, CrudViewStyle.TOOLBAR_CONTROL_HEIGHT));

        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setPreferredSize(new Dimension(CrudViewStyle.TOOLBAR_SEARCH_WIDTH, CrudViewStyle.TOOLBAR_CONTROL_HEIGHT));
        searchField.putClientProperty("JTextField.placeholderText", "Tìm theo mã hoặc tên sản phẩm...");
        searchField.putClientProperty("JTextField.padding", new Insets(5, 8, 5, 10));
        searchField.putClientProperty("JComponent.roundRect", true);
        searchField.setBorder(null);
        searchField.setOpaque(false);

        JLabel iconLabel = new JLabel(loadSearchIcon());
        iconLabel.setBorder(new EmptyBorder(0, 0, 0, 8));

        JPanel innerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 28, 28);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 28, 28);
                g2.dispose();
            }
        };
        innerPanel.setOpaque(false);
        innerPanel.setPreferredSize(new Dimension(CrudViewStyle.TOOLBAR_SEARCH_WIDTH, CrudViewStyle.TOOLBAR_CONTROL_HEIGHT));
        innerPanel.setMaximumSize(new Dimension(CrudViewStyle.TOOLBAR_SEARCH_WIDTH, CrudViewStyle.TOOLBAR_CONTROL_HEIGHT));
        innerPanel.setBorder(new EmptyBorder(0, 12, 0, 12));
        innerPanel.add(iconLabel, BorderLayout.WEST);
        innerPanel.add(searchField, BorderLayout.CENTER);

        searchWrapper.add(innerPanel, BorderLayout.CENTER);
        return searchWrapper;
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

        btnSortDir.setFont(new Font("Segoe UI Symbol", Font.BOLD, 11));
        btnSortDir.setForeground(new Color(75, 85, 99));
        btnSortDir.setBorder(new EmptyBorder(0, 0, 0, 12));
        btnSortDir.setContentAreaFilled(false);
        btnSortDir.setBorderPainted(false);
        btnSortDir.setFocusPainted(false);
        btnSortDir.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
                g2.setColor(BORDER);
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

    private JPanel createTableHeader() {
        JPanel header = new JPanel(new GridBagLayout());
        header.setBackground(new Color(248, 249, 250));
        header.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 1, 0, BORDER),
                new EmptyBorder(0, 24, 0, 24)
        ));
        header.setPreferredSize(new Dimension(1000, HEADER_HEIGHT));
        header.setMinimumSize(new Dimension(800, HEADER_HEIGHT));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, COLUMN_GAP);

        gbc.weightx = 0.12;
        header.add(createFlexibleCell(createHeaderLabel("MÃ SP"), SwingConstants.CENTER, new Color(248, 249, 250), 0, 8), gbc);
        gbc.weightx = 0.22;
        header.add(createFlexibleCell(createHeaderLabel("TÊN SẢN PHẨM"), SwingConstants.LEFT, new Color(248, 249, 250), 8, 8), gbc);
        gbc.weightx = 0.14;
        header.add(createFlexibleCell(createHeaderLabel("ĐƠN VỊ TÍNH"), SwingConstants.CENTER, new Color(248, 249, 250), 0, 8), gbc);
        gbc.weightx = 0.16;
        header.add(createFlexibleCell(createHeaderLabel("ĐƠN GIÁ"), SwingConstants.RIGHT, new Color(248, 249, 250), 0, 8), gbc);
        gbc.weightx = 0.14;
        header.add(createFlexibleCell(createHeaderLabel("TỒN KHO"), SwingConstants.CENTER, new Color(248, 249, 250), 0, 8), gbc);
        gbc.weightx = 0.22;
        gbc.insets = new Insets(0, 0, 0, 0);
        header.add(createFlexibleCell(createHeaderLabel("THAO TÁC"), SwingConstants.CENTER, new Color(248, 249, 250), 0, 8), gbc);
        return header;
    }

    private JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(MUTED);
        return label;
    }

    private JPanel createDataRow(ProductVm product, int rowIndex) {
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

        JLabel idLabel = createCellLabel(product.getMaSp(), new Color(22, 163, 74));
        idLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        gbc.weightx = 0.12;
        row.add(createFlexibleCell(idLabel, SwingConstants.CENTER, rowBg, 0, 8), gbc);
        gbc.weightx = 0.22;
        row.add(createFlexibleCell(createCellLabel(product.getTenSp(), TEXT), SwingConstants.LEFT, rowBg, 8, 8), gbc);
        gbc.weightx = 0.14;
        row.add(createFlexibleCell(createUnitPill(product.getDvt()), SwingConstants.CENTER, rowBg, 0, 8), gbc);
        gbc.weightx = 0.16;
        row.add(createFlexibleCell(createCellLabel(formatCurrency(product.getGia()), TEXT), SwingConstants.RIGHT, rowBg, 0, 8), gbc);
        gbc.weightx = 0.14;
        row.add(createFlexibleCell(createStockPill(product.getSlTon()), SwingConstants.CENTER, rowBg, 0, 8), gbc);
        gbc.weightx = 0.22;
        gbc.insets = new Insets(0, 0, 0, 0);
        row.add(createFlexibleCell(createActionCell(product, rowBg), SwingConstants.CENTER, rowBg, 0, 0), gbc);

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

    private JPanel createActionCell(ProductVm product, Color rowBg) {
        JPanel actionGroup = new JPanel();
        actionGroup.setLayout(new BoxLayout(actionGroup, BoxLayout.X_AXIS));
        actionGroup.setOpaque(false);

        boolean deleted = product.isDeleted();
        JButton statusBtn = deleted
                ? createMiniActionButton("Khôi phục", new Color(228, 250, 226), new Color(16, 110, 0))
                : createMiniActionButton("Xóa", new Color(254, 226, 226), new Color(185, 28, 28));
        statusBtn.setEnabled(!loading);
        statusBtn.addActionListener(event -> {
            selectProduct(product);
            if (deleted) {
                fireRestoreAction();
            } else {
                fireDeleteAction();
            }
        });
        actionGroup.add(statusBtn);
        actionGroup.add(Box.createHorizontalStrut(10));

        JButton editBtn = createMiniActionButton("Chỉnh sửa", new Color(239, 246, 255), new Color(29, 78, 216));
        editBtn.setEnabled(!loading);
        editBtn.addActionListener(event -> {
            selectProduct(product);
            fireUpdateAction();
        });
        actionGroup.add(editBtn);

        JPanel actionCell = new JPanel(new GridBagLayout());
        actionCell.setBackground(rowBg);
        actionCell.setOpaque(true);
        actionCell.add(actionGroup);
        return actionCell;
    }

    private JPanel createUnitPill(String unit) {
        String text = valueOrDash(unit).toUpperCase(Locale.ROOT);
        return createSimplePill(text, new Color(239, 246, 255), new Color(29, 78, 216), new Dimension(98, 26));
    }

    private JPanel createStockPill(Integer stock) {
        int value = stock == null ? 0 : stock;
        Color background;
        Color foreground;
        if (value <= 0) {
            background = new Color(254, 226, 226);
            foreground = new Color(185, 28, 28);
        } else if (value <= 10) {
            background = new Color(254, 249, 195);
            foreground = new Color(133, 77, 14);
        } else {
            background = new Color(228, 250, 226);
            foreground = new Color(16, 110, 0);
        }
        return createSimplePill(String.valueOf(value), background, foreground, new Dimension(74, 26));
    }

    private JPanel createSimplePill(String text, Color background, Color foreground, Dimension size) {
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
        wrapper.setPreferredSize(size);
        wrapper.setMinimumSize(size);
        wrapper.setMaximumSize(size);

        JLabel textLabel = new JLabel(text);
        textLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        textLabel.setForeground(foreground);
        wrapper.add(textLabel);

        JPanel container = new JPanel(new GridBagLayout());
        container.setOpaque(false);
        container.add(wrapper);
        return container;
    }

    private JPanel createEmptyRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setBorder(new EmptyBorder(24, 26, 24, 26));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));

        JLabel msg = new JLabel("Không tìm thấy sản phẩm phù hợp.");
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        msg.setForeground(MUTED);
        row.add(msg, BorderLayout.CENTER);
        return row;
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

    private JLabel createCellLabel(String text, Color fg) {
        JLabel label = new JLabel(valueOrDash(text));
        label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        label.setForeground(fg);
        return label;
    }

    private void bindEvents() {
        addButton.addActionListener(event -> {
            selectedProduct = null;
            selectedProductId = null;
            if (addAction != null) {
                addAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "add"));
            }
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                fireSearchAction();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                fireSearchAction();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                fireSearchAction();
            }
        });

        cbSort.addActionListener(event -> {
            String selectedCode = getSelectedProductId();
            sortProducts();
            renderTable();
            restoreSelection(selectedCode);
        });

        btnSortDir.addActionListener(event -> {
            sortAscending = !sortAscending;
            updateSortDirectionButton();
            String selectedCode = getSelectedProductId();
            sortProducts();
            renderTable();
            restoreSelection(selectedCode);
        });
    }

    private void renderTable() {
        tablePanel.removeAll();

        if (products.isEmpty()) {
            tablePanel.add(createEmptyRow());
        } else {
            int index = 0;
            for (ProductVm product : products) {
                tablePanel.add(createDataRow(product, index++));
            }
        }

        footerLabel.setText("Hiển thị " + products.size() + " sản phẩm");
        tablePanel.revalidate();
        tablePanel.repaint();
    }

    private void sortProducts() {
        String sortType = (String) cbSort.getSelectedItem();
        Comparator<ProductVm> comparator;
        if ("Đơn giá".equals(sortType)) {
            comparator = Comparator.comparing(
                    (ProductVm product) -> product.getGia() == null ? BigDecimal.ZERO : product.getGia()
            ).thenComparing(product -> normalizedSortKey(product.getTenSp()));
        } else if ("Tồn kho".equals(sortType)) {
            comparator = Comparator.comparingInt((ProductVm product) -> product.getSlTon() == null ? 0 : product.getSlTon())
                    .thenComparing(product -> normalizedSortKey(product.getTenSp()));
        } else {
            comparator = Comparator.comparing((ProductVm product) -> normalizedSortKey(product.getTenSp()))
                    .thenComparing(product -> normalizedSortKey(product.getMaSp()));
        }
        if (!sortAscending) {
            comparator = comparator.reversed();
        }
        products.sort(comparator);
    }

    private void updateSortDirectionButton() {
        btnSortDir.setText(sortAscending ? "\u25B2" : "\u25BC");
        btnSortDir.setToolTipText(sortAscending ? "Đang sắp xếp tăng dần" : "Đang sắp xếp giảm dần");
    }

    private void restoreSelection(String maSp) {
        selectedProduct = null;
        selectedProductId = null;
        if (maSp == null) {
            return;
        }
        for (ProductVm product : products) {
            if (maSp.equals(product.getMaSp())) {
                selectProduct(product);
                return;
            }
        }
    }

    private void selectProduct(ProductVm product) {
        selectedProduct = product;
        selectedProductId = product == null ? null : product.getMaSp();
    }

    private void fireSearchAction() {
        if (searchAction != null) {
            searchAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "search"));
        }
    }

    private void fireUpdateAction() {
        if (updateAction != null) {
            updateAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "update"));
        }
    }

    private void fireDeleteAction() {
        if (deleteAction != null) {
            deleteAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "delete"));
        }
    }

    private void fireRestoreAction() {
        if (restoreAction != null) {
            restoreAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "restore"));
        }
    }

    @SuppressWarnings("unused")
    private void fireRefreshAction() {
        if (refreshAction != null) {
            refreshAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "refresh"));
        }
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

    private String valueOrDash(String text) {
        return text == null || text.isBlank() ? "--" : text;
    }

    private String generateNextProductId() {
        int maxNumber = 0;
        for (ProductVm product : products) {
            String code = product.getMaSp();
            if (code == null) {
                continue;
            }
            String normalized = code.trim().toUpperCase(Locale.ROOT);
            if (!normalized.startsWith("SP-")) {
                continue;
            }
            String numberPart = normalized.substring(3);
            try {
                maxNumber = Math.max(maxNumber, Integer.parseInt(numberPart));
            } catch (NumberFormatException ignored) {
            }
        }
        return "SP-" + (maxNumber + 1);
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
