package com.sportcourt.modules.customer_rank.view;

import com.sportcourt.common.style.CrudViewStyle;
import com.sportcourt.modules.customer_rank.controller.CustomerRankController;
import com.sportcourt.modules.customer_rank.entity.CustomerRank;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class CustomerRankManagement extends JPanel implements Scrollable {

    private static final Color ALTERNATE_ROW_BACKGROUND = CrudViewStyle.ALTERNATE_ROW_BACKGROUND;

    private final CustomerRankController controller = new CustomerRankController();

    private final JPanel tablePanel = new JPanel();
    private final JLabel infoLabel = new JLabel("Đang tải dữ liệu...");
    private final JTextField searchField = new JTextField(30);
    private final JPanel searchWrapper = new JPanel(new BorderLayout());
    private final JComboBox<String> cbSort = new JComboBox<>(new String[]{
            "Mã hạng",
            "Tên hạng",
            "Chiết khấu",
            "Mức tiền"
    });
    private final JButton btnSortDir = new JButton("\u25B2");
    private final Timer searchDebounceTimer;
    private boolean sortAscending = true;

    public CustomerRankManagement() {
        setLayout(new BorderLayout());
        CrudViewStyle.applyPageDefaults(this);
        searchDebounceTimer = new Timer(300, event -> loadData(searchField.getText()));
        searchDebounceTimer.setRepeats(false);

        add(createListPage(), BorderLayout.CENTER);
        CrudViewStyle.installResponsiveTypography(this);
        loadData(null);
    }

    private JPanel createListPage() {
        JPanel page = new JPanel(new BorderLayout(0, 12));
        page.setOpaque(false);
        page.add(createHeaderSection(), BorderLayout.NORTH);
        page.add(createMainContentSection(), BorderLayout.CENTER);
        return page;
    }

    private JPanel createHeaderSection() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("QUẢN LÝ HẠNG KHÁCH HÀNG");
        titleLabel.setFont(new Font("Lexend", Font.BOLD, 30));
        titleLabel.setForeground(new Color(26, 26, 26));
        titleLabel.setBorder(new EmptyBorder(0, 20, 0, 0));

        JLabel subtitleLabel = new JLabel("Quản lý các hạng khách hàng, chiết khấu và mức tiền tối thiểu.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(107, 114, 128));
        subtitleLabel.setBorder(new EmptyBorder(5, 20, 20, 0));

        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setPreferredSize(new Dimension(CrudViewStyle.TOOLBAR_SEARCH_WIDTH, CrudViewStyle.TOOLBAR_CONTROL_HEIGHT));
        searchField.putClientProperty("JTextField.placeholderText", "Tìm theo mã hoặc tên hạng...");
        searchField.putClientProperty("JTextField.padding", new Insets(5, 8, 5, 10));
        searchField.putClientProperty("JComponent.roundRect", true);
        searchField.setBorder(null);
        searchField.setOpaque(false);
        bindSearchListener();

        headerPanel.add(titleLabel);
        headerPanel.add(subtitleLabel);
        return headerPanel;
    }

    private JPanel createMainContentSection() {
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
                Shape shape = new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setClip(shape);
                super.paintChildren(g2);
                g2.dispose();
            }
        };

        container.setOpaque(false);
        container.setBackground(Color.WHITE);
        container.setBorder(new EmptyBorder(12, 0, 16, 0));

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(new EmptyBorder(8, 20, 14, 20));

        JPanel leftToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftToolbar.setBackground(Color.WHITE);

        JLabel tableTitle = new JLabel("DANH SÁCH HẠNG KHÁCH HÀNG");
        tableTitle.setFont(new Font("Lexend", Font.BOLD, 22));

        JButton addButton = createPillButton("+ Thêm hạng", new Color(228, 250, 226), new Color(16, 110, 0), true);
        addButton.setFont(new Font("Lexend", Font.BOLD, 16));
        addButton.setBorder(new EmptyBorder(6, 22, 6, 22));
        CrudViewStyle.applyToolbarButtonHeight(addButton);
        addButton.addActionListener(event ->
                CustomerRankCreateDialog.show(this, controller, this::loadData));

        leftToolbar.add(tableTitle);
        leftToolbar.add(addButton);
        toolbar.add(leftToolbar, BorderLayout.WEST);
        JPanel rightToolbar = CrudViewStyle.createToolbarActionsPanel();
        rightToolbar.add(createSortWrapper());
        rightToolbar.add(Box.createHorizontalStrut(10));
        rightToolbar.add(createSearchFieldWithIcon());
        toolbar.add(rightToolbar, BorderLayout.EAST);

        container.add(toolbar, BorderLayout.NORTH);

        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
        tablePanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(tablePanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        container.add(scrollPane, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(20, 20, 0, 20));
        infoLabel.setForeground(new Color(107, 114, 128));
        footer.add(infoLabel, BorderLayout.WEST);
        container.add(footer, BorderLayout.SOUTH);
        return container;
    }

    private JPanel createSearchFieldWithIcon() {
        searchField.putClientProperty("JTextField.placeholderText", "Tìm theo mã hoặc tên hạng...");
        return CrudViewStyle.createSearchFieldWithIcon(searchWrapper, searchField, loadSearchIcon());
    }

    private JPanel createSortWrapper() {
        cbSort.addActionListener(event -> loadData(searchField.getText()));
        btnSortDir.addActionListener(event -> {
            sortAscending = !sortAscending;
            CrudViewStyle.updateSortDirectionButton(btnSortDir, sortAscending);
            loadData(searchField.getText());
        });
        CrudViewStyle.updateSortDirectionButton(btnSortDir, sortAscending);
        return CrudViewStyle.createSortWrapper(cbSort, btnSortDir);
    }

    private Icon loadSearchIcon() {
        URL iconUrl = getClass().getResource("/icon/search.png");
        if (iconUrl == null) {
            return UIManager.getIcon("FileView.fileIcon");
        }
        Image image = new ImageIcon(iconUrl).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH);
        return new ImageIcon(image);
    }

    private void bindSearchListener() {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { searchDebounceTimer.restart(); }
            @Override public void removeUpdate(DocumentEvent e) { searchDebounceTimer.restart(); }
            @Override public void changedUpdate(DocumentEvent e) { searchDebounceTimer.restart(); }
        });
    }

    public void loadData(String keyword) {
        infoLabel.setText("Đang tải dữ liệu...");
        tablePanel.removeAll();
        tablePanel.add(createTableHeader());

        List<CustomerRank> all;
        try {
            all = new ArrayList<>(controller.searchRanks(keyword));
            sortRanks(all);
        } catch (SQLException ex) {
            tablePanel.add(createMessageRow("Lỗi tải dữ liệu: " + ex.getMessage()));
            infoLabel.setText("Lỗi kết nối cơ sở dữ liệu");
            tablePanel.revalidate();
            tablePanel.repaint();
            return;
        }

        if (all.isEmpty()) {
            tablePanel.add(createMessageRow("Không tìm thấy hạng khách hàng phù hợp."));
            infoLabel.setText("Hiển thị 0 hạng khách hàng");
        } else {
            int idx = 0;
            for (CustomerRank item : all) {
                tablePanel.add(createDataRow(item, idx++));
            }
            infoLabel.setText("Hiển thị " + all.size() + " hạng khách hàng");
        }
        tablePanel.revalidate();
        tablePanel.repaint();
    }

    private void sortRanks(List<CustomerRank> ranks) {
        String sortType = (String) cbSort.getSelectedItem();
        Comparator<CustomerRank> comparator;
        if ("Tên hạng".equals(sortType)) {
            comparator = Comparator.comparing(rank -> sortKey(rank.getTenHang()));
        } else if ("Chiết khấu".equals(sortType)) {
            comparator = Comparator.comparing(CustomerRank::getChietKhau, Comparator.nullsLast(Comparator.naturalOrder()));
        } else if ("Mức tiền".equals(sortType)) {
            comparator = Comparator.comparing(CustomerRank::getMucTien, Comparator.nullsLast(Comparator.naturalOrder()));
        } else {
            comparator = Comparator.comparing(rank -> sortKey(rank.getMaHang()));
        }
        comparator = comparator.thenComparing(rank -> sortKey(rank.getMaHang()));
        if (!sortAscending) {
            comparator = comparator.reversed();
        }
        ranks.sort(comparator);
    }

    private String sortKey(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private JPanel createTableHeader() {
        JPanel header = new JPanel(new GridBagLayout());
        header.setBackground(new Color(248, 249, 250));
        header.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 1, 0, new Color(229, 231, 235)),
                new EmptyBorder(0, 24, 0, 24)
        ));
        header.setPreferredSize(new Dimension(0, 52));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 16);

        gbc.weightx = 0.15; header.add(createHeaderCell("MÃ HẠNG"), gbc);
        gbc.weightx = 0.25; header.add(createFlexibleCell(createHeaderLabel("TÊN HẠNG"), SwingConstants.LEFT, new Color(248, 249, 250), 8, 0), gbc);
        gbc.weightx = 0.20; header.add(createHeaderCell("CHIẾT KHẤU"), gbc);
        gbc.weightx = 0.20; header.add(createFlexibleCell(createHeaderLabel("MỨC TIỀN"), SwingConstants.RIGHT, new Color(248, 249, 250), 0, 8), gbc);
        gbc.weightx = 0.20; gbc.insets = new Insets(0, 0, 0, 0); header.add(createHeaderCell("THAO TÁC"), gbc);
        return header;
    }

    private JPanel createHeaderCell(String text) {
        return createFlexibleCell(createHeaderLabel(text), SwingConstants.CENTER, new Color(248, 249, 250), 0, 0);
    }

    private JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(new Color(107, 114, 128));
        return label;
    }

    private JPanel createDataRow(CustomerRank item, int rowIndex) {
        Color rowBackground = rowIndex % 2 == 0 ? Color.WHITE : ALTERNATE_ROW_BACKGROUND;
        JPanel row = new JPanel(new GridBagLayout());
        row.setBackground(rowBackground);
        row.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(243, 244, 246)),
                new EmptyBorder(0, 24, 0, 24)
        ));
        row.setPreferredSize(new Dimension(0, 72));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 16);

        JLabel idLabel = new JLabel(item.getMaHang());
        idLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        idLabel.setForeground(new Color(22, 163, 74));
        gbc.weightx = 0.15; row.add(createFlexibleCell(idLabel, SwingConstants.CENTER, rowBackground, 0, 0), gbc);

        gbc.weightx = 0.25; row.add(createFlexibleCell(createCellLabel(item.getTenHang(), new Color(17, 24, 39)), SwingConstants.LEFT, rowBackground, 8, 0), gbc);

        JPanel discountBadge = createDiscountBadge(item.getChietKhau());
        gbc.weightx = 0.20; row.add(createFlexibleCell(discountBadge, SwingConstants.CENTER, rowBackground, 0, 0), gbc);

        gbc.weightx = 0.20; row.add(createFlexibleCell(createCellLabel(formatMoney(item.getMucTien()), new Color(37, 99, 235)), SwingConstants.RIGHT, rowBackground, 0, 8), gbc);

        JPanel actionContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        actionContainer.setOpaque(false);

        JButton deleteButton = createMiniActionButton("Xóa", new Color(254, 226, 226), new Color(185, 28, 28));
        deleteButton.addActionListener(event -> confirmDelete(item));

        JButton editButton = createMiniActionButton("Chỉnh sửa", new Color(239, 246, 255), new Color(29, 78, 216));
        editButton.addActionListener(event ->
                CustomerRankEditDialog.show(this, item, controller, this::loadData));

        actionContainer.add(deleteButton);
        actionContainer.add(editButton);

        JPanel actionCell = new JPanel(new GridBagLayout());
        actionCell.setBackground(rowBackground);
        actionCell.setOpaque(true);
        actionCell.add(actionContainer);

        gbc.weightx = 0.20; gbc.insets = new Insets(0, 0, 0, 0);
        row.add(createFlexibleCell(actionCell, SwingConstants.CENTER, rowBackground, 0, 0), gbc);

        row.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                row.setBackground(new Color(249, 250, 251));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                row.setBackground(rowBackground);
            }
        });
        return row;
    }

    private JPanel createMessageRow(String message) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setBorder(new MatteBorder(0, 0, 1, 0, new Color(243, 244, 246)));
        row.setPreferredSize(new Dimension(0, 60));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel label = new JLabel(message);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(new Color(107, 114, 128));
        label.setBorder(new EmptyBorder(0, 20, 0, 0));
        row.add(label, BorderLayout.CENTER);
        return row;
    }

    @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    @Override public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) { return 16; }
    @Override public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) { return 64; }
    @Override public boolean getScrollableTracksViewportWidth() { return true; }
    @Override public boolean getScrollableTracksViewportHeight() { return true; }

    private JLabel createCellLabel(String text, Color fg) {
        JLabel label = new JLabel(text == null || text.isBlank() ? "--" : text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        label.setForeground(fg);
        return label;
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
        panel.setPreferredSize(new Dimension(0, 72));
        panel.setMinimumSize(new Dimension(0, 72));
        return panel;
    }

    private JPanel createDiscountBadge(BigDecimal chietKhau) {
        double value = chietKhau == null ? 0 : chietKhau.doubleValue();
        Color badgeBg, badgeFg;
        if (value == 0) { badgeBg = new Color(243, 244, 246); badgeFg = new Color(107, 114, 128); }
        else if (value <= 5) { badgeBg = new Color(219, 234, 254); badgeFg = new Color(29, 78, 216); }
        else if (value <= 10) { badgeBg = new Color(220, 252, 231); badgeFg = new Color(22, 101, 52); }
        else if (value <= 15) { badgeBg = new Color(254, 243, 199); badgeFg = new Color(146, 64, 14); }
        else { badgeBg = new Color(254, 226, 226); badgeFg = new Color(185, 28, 28); }

        String text = value == 0 ? "Không" : (int) value + "%";
        JLabel label = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(badgeBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                super.paintComponent(g);
                g2.dispose();
            }
        };
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(badgeFg);
        label.setOpaque(false);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBorder(new EmptyBorder(4, 14, 4, 14));
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.add(label);
        return wrapper;
    }

    private void confirmDelete(CustomerRank item) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa hạng \"" + item.getTenHang() + "\"?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean deleted = controller.deleteRank(item.getMaHang());
                if (deleted) {
                    JOptionPane.showMessageDialog(this, "Xóa hạng thành công.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    loadData(searchField.getText());
                } else {
                    JOptionPane.showMessageDialog(this, "Không tìm thấy hạng để xóa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) return "0 VNĐ";
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        nf.setMaximumFractionDigits(0);
        return nf.format(value) + " VNĐ";
    }

    private JButton createPillButton(String text, Color bg, Color fg, boolean isBold) {
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
        btn.setFont(new Font("Segoe UI", isBold ? Font.BOLD : Font.PLAIN, 14));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(5, 12, 5, 12));
        return btn;
    }

    private JButton createMiniActionButton(String text, Color bg, Color fg) {
        JButton btn = createPillButton(text, bg, fg, true);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setBorder(new EmptyBorder(4, 12, 4, 12));
        return btn;
    }
}
