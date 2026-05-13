package com.sportcourt.modules.branch.view;

import com.sportcourt.common.style.CrudViewStyle;
import com.sportcourt.modules.branch.controller.BranchController;
import com.sportcourt.modules.branch.entity.Branch;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.Scrollable;

public class BranchManagement extends JPanel implements Scrollable {
    private static final int HEADER_HEIGHT = 52;
    private static final int ROW_HEIGHT = 72;
    private static final int COLUMN_GAP = 12;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Color ALTERNATE_ROW_BACKGROUND = CrudViewStyle.ALTERNATE_ROW_BACKGROUND;

    private final BranchController branchController = new BranchController();
    private final JPanel tablePanel = new JPanel();
    private final JLabel infoLabel = new JLabel("Đang tải dữ liệu...");
    private final JTextField searchField = new JTextField(30);
    private final JPanel searchWrapper = new JPanel(new BorderLayout());
    private final JComboBox<String> cbSort = new JComboBox<>(new String[]{
            "Mã chi nhánh",
            "Tên chi nhánh",
            "Hotline",
            "Ngày tạo"
    });
    private final JButton btnSortDir = new JButton("\u25B2");
    private final Timer searchDebounceTimer;
    private boolean sortAscending = true;

    private final BranchChange suaChiNhanh = new BranchChange(branchController, id -> loadChiNhanhData(searchField.getText()));
    private final BranchAdd themChiNhanh = new BranchAdd(branchController, id -> loadChiNhanhData(searchField.getText()));

    public BranchManagement() {
        setLayout(new BorderLayout());
        CrudViewStyle.applyPageDefaults(this);

        searchDebounceTimer = new Timer(300, event -> loadChiNhanhData(searchField.getText()));
        searchDebounceTimer.setRepeats(false);

        add(createListPage(), BorderLayout.CENTER);
        CrudViewStyle.installResponsiveTypography(this);
        loadChiNhanhData(null);
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

        JLabel titleLabel = new JLabel("QUẢN LÝ CHI NHÁNH");
        titleLabel.setFont(new Font("Lexend", Font.BOLD, 30));
        titleLabel.setForeground(new Color(26, 26, 26));
        titleLabel.setBorder(new EmptyBorder(0, 20, 0, 0));

        JLabel subtitleLabel = new JLabel("Hiển thị dữ liệu các chi nhánh và hỗ trợ tìm kiếm.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(107, 114, 128));
        subtitleLabel.setBorder(new EmptyBorder(5, 20, 20, 0));

        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setPreferredSize(new Dimension(CrudViewStyle.TOOLBAR_SEARCH_WIDTH, CrudViewStyle.TOOLBAR_CONTROL_HEIGHT));
        searchField.putClientProperty("JTextField.placeholderText", "Tìm theo MACN hoặc tên chi nhánh...");
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

        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setBackground(Color.WHITE);

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(new EmptyBorder(8, 20, 14, 20));

        JPanel leftToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftToolbar.setBackground(Color.WHITE);

        JLabel tableTitle = new JLabel("DANH SÁCH CHI NHÁNH");
        tableTitle.setFont(new Font("Lexend", Font.BOLD, 22));

        JButton addButton = createPillButton("+ Thêm chi nhánh", new Color(228, 250, 226), new Color(16, 110, 0), true);
        addButton.setFont(new Font("Lexend", Font.BOLD, 16));
        addButton.setBorder(new EmptyBorder(6, 22, 6, 22));
        CrudViewStyle.applyToolbarButtonHeight(addButton);
        addButton.addActionListener(event -> showCreateView());
        JButton refreshButton = CrudViewStyle.createRefreshButton(event -> loadChiNhanhData(searchField.getText()));

        leftToolbar.add(tableTitle);
        leftToolbar.add(addButton);
        leftToolbar.add(refreshButton);
        toolbar.add(leftToolbar, BorderLayout.WEST);

        JPanel rightToolbar = CrudViewStyle.createToolbarActionsPanel();
        rightToolbar.add(createSortWrapper());
        rightToolbar.add(Box.createHorizontalStrut(10));
        rightToolbar.add(createSearchFieldWithIcon());
        toolbar.add(rightToolbar, BorderLayout.EAST);

        topSection.add(toolbar);
        container.add(topSection, BorderLayout.NORTH);

        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
        tablePanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(tablePanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setColumnHeaderView(createTableHeader());
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
        searchField.putClientProperty("JTextField.placeholderText", "Tìm theo tên hoặc mã chi nhánh...");
        bindSearchListener();
        return CrudViewStyle.createSearchFieldWithIcon(searchWrapper, searchField, loadSearchIcon());
    }

    private JPanel createSortWrapper() {
        cbSort.addActionListener(event -> loadChiNhanhData(searchField.getText()));
        btnSortDir.addActionListener(event -> {
            sortAscending = !sortAscending;
            CrudViewStyle.updateSortDirectionButton(btnSortDir, sortAscending);
            loadChiNhanhData(searchField.getText());
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
            @Override
            public void insertUpdate(DocumentEvent e) {
                restartSearchTimer();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                restartSearchTimer();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                restartSearchTimer();
            }
        });
    }

    private void restartSearchTimer() {
        searchDebounceTimer.restart();
    }

    private void loadChiNhanhData(String keyword) {
        infoLabel.setText("Đang tải dữ liệu...");
        renderLoadingState();

        SwingWorker<List<Branch>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Branch> doInBackground() {
                return branchController.getBranchList(keyword);
            }

            @Override
            protected void done() {
                try {
                    List<Branch> branches = get();
                    renderTableData(branches);
                } catch (Exception exception) {
                    renderErrorState(exception);
                }
            }
        };
        worker.execute();
    }

    private void renderLoadingState() {
        tablePanel.removeAll();
        tablePanel.add(createMessageRow("Đang tải dữ liệu..."));
        tablePanel.revalidate();
        tablePanel.repaint();
    }

    private void renderTableData(List<Branch> branches) {
        List<Branch> sortedBranches = branches == null ? new ArrayList<>() : new ArrayList<>(branches);
        sortBranches(sortedBranches);
        tablePanel.removeAll();

        if (sortedBranches.isEmpty()) {
            tablePanel.add(createMessageRow("Không tìm thấy chi nhánh phù hợp."));
            infoLabel.setText("Hiển thị 0 chi nhánh");
        } else {
            int rowIndex = 0;
            for (Branch branch : sortedBranches) {
                tablePanel.add(createDataRow(branch, rowIndex++));
            }
            infoLabel.setText("Hiển thị " + branches.size() + " chi nhánh");
        }

        tablePanel.revalidate();
        tablePanel.repaint();
    }

    private void sortBranches(List<Branch> branches) {
        String sortType = (String) cbSort.getSelectedItem();
        Comparator<Branch> comparator;
        if ("Tên chi nhánh".equals(sortType)) {
            comparator = Comparator.comparing(branch -> sortKey(branch.tenChiNhanh()));
        } else if ("Hotline".equals(sortType)) {
            comparator = Comparator.comparing(branch -> sortKey(branch.hotline()));
        } else if ("Ngày tạo".equals(sortType)) {
            comparator = Comparator.comparing(Branch::createdAt, Comparator.nullsLast(Comparator.naturalOrder()));
        } else {
            comparator = Comparator.comparing(branch -> sortKey(branch.maCn()));
        }
        comparator = comparator.thenComparing(branch -> sortKey(branch.maCn()));
        if (!sortAscending) {
            comparator = comparator.reversed();
        }
        branches.sort(comparator);
    }

    private String sortKey(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private void renderErrorState(Exception exception) {
        tablePanel.removeAll();
        tablePanel.add(createMessageRow("Không thể tải dữ liệu từ database."));
        infoLabel.setText("Lỗi tải dữ liệu");
        tablePanel.revalidate();
        tablePanel.repaint();

        JOptionPane.showMessageDialog(
                this,
                exception.getCause() == null ? exception.getMessage() : exception.getCause().getMessage(),
                "Lỗi dữ liệu chi nhánh",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private JPanel createTableHeader() {
        JPanel header = new JPanel(new GridBagLayout());
        header.setBackground(new Color(248, 249, 250));
        header.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 1, 0, new Color(229, 231, 235)),
                new EmptyBorder(0, 24, 0, 24)
        ));
        header.setPreferredSize(new Dimension(1100, HEADER_HEIGHT));
        header.setMinimumSize(new Dimension(900, HEADER_HEIGHT));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, COLUMN_GAP);

        gbc.weightx = 0.13; header.add(createFlexibleCell(createHeaderLabel("MÃ CHI NHÁNH"), SwingConstants.CENTER, new Color(248, 249, 250), 0, 8), gbc);
        gbc.weightx = 0.19; header.add(createFlexibleCell(createHeaderLabel("TÊN CHI NHÁNH"), SwingConstants.LEFT, new Color(248, 249, 250), 8, 8), gbc);
        gbc.weightx = 0.22; header.add(createFlexibleCell(createHeaderLabel("ĐỊA CHỈ"), SwingConstants.LEFT, new Color(248, 249, 250), 8, 8), gbc);
        gbc.weightx = 0.13; header.add(createFlexibleCell(createHeaderLabel("HOTLINE"), SwingConstants.CENTER, new Color(248, 249, 250), 0, 8), gbc);
        gbc.weightx = 0.15; header.add(createFlexibleCell(createHeaderLabel("NGÀY TẠO"), SwingConstants.CENTER, new Color(248, 249, 250), 0, 8), gbc);
        gbc.weightx = 0.18; gbc.insets = new Insets(0, 0, 0, 0); header.add(createFlexibleCell(createHeaderLabel("THAO TÁC"), SwingConstants.CENTER, new Color(248, 249, 250), 0, 0), gbc);

        return header;
    }

    private JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(new Color(107, 114, 128));
        return label;
    }

    private JPanel createDataRow(Branch branch, int rowIndex) {
        Color rowBackground = rowIndex % 2 == 0 ? Color.WHITE : ALTERNATE_ROW_BACKGROUND;
        JPanel row = new JPanel(new GridBagLayout());
        row.setBackground(rowBackground);
        row.setBorder(new MatteBorder(0, 0, 1, 0, new Color(243, 244, 246)));
        row.setPreferredSize(new Dimension(1100, ROW_HEIGHT));
        row.setMinimumSize(new Dimension(900, ROW_HEIGHT));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, ROW_HEIGHT));
        row.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(243, 244, 246)),
                new EmptyBorder(0, 24, 0, 24)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, COLUMN_GAP);

        JLabel maCnLabel = new JLabel(branch.maCn());
        maCnLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        maCnLabel.setForeground(new Color(22, 163, 74));
        gbc.weightx = 0.13; row.add(createFlexibleCell(maCnLabel, SwingConstants.CENTER, rowBackground, 0, 8), gbc);

        gbc.weightx = 0.19; row.add(createFlexibleCell(createCellLabel(branch.tenChiNhanh(), new Color(37, 99, 235)), SwingConstants.LEFT, rowBackground, 8, 8), gbc);
        gbc.weightx = 0.22; row.add(createFlexibleCell(createCellLabel(branch.diaChi(), new Color(75, 85, 99)), SwingConstants.LEFT, rowBackground, 8, 8), gbc);
        gbc.weightx = 0.13; row.add(createFlexibleCell(createCellLabel(branch.hotline(), new Color(17, 24, 39)), SwingConstants.CENTER, rowBackground, 0, 8), gbc);
        gbc.weightx = 0.15; row.add(createFlexibleCell(createCellLabel(formatDate(branch.createdAt()), new Color(75, 85, 99)), SwingConstants.CENTER, rowBackground, 0, 8), gbc);

        JPanel actionContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        actionContainer.setOpaque(false);

        JButton deleteButton = createMiniActionButton("Xóa", new Color(254, 226, 226), new Color(185, 28, 28));
        deleteButton.addActionListener(event -> confirmDelete(branch));

        JButton editButton = createMiniActionButton("Chỉnh sửa", new Color(239, 246, 255), new Color(29, 78, 216));
        editButton.addActionListener(event -> showEditView(branch.maCn()));

        actionContainer.add(deleteButton);
        actionContainer.add(editButton);

        JPanel actionCell = new JPanel(new GridBagLayout());
        actionCell.setBackground(rowBackground);
        actionCell.setOpaque(true);
        actionCell.add(actionContainer);

        gbc.weightx = 0.18; gbc.insets = new Insets(0, 0, 0, 0); row.add(createFlexibleCell(actionCell, SwingConstants.CENTER, rowBackground, 0, 0), gbc);

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

        panel.setPreferredSize(new Dimension(0, ROW_HEIGHT));
        panel.setMinimumSize(new Dimension(0, ROW_HEIGHT));
        return panel;
    }

    private void confirmDelete(Branch branch) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn chắc chắn muốn xóa chi nhánh này không?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            branchController.deleteBranch(branch.maCn());
            loadChiNhanhData(searchField.getText());
            JOptionPane.showMessageDialog(this, "Đã xóa chi nhánh " + branch.maCn() + ".", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalStateException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getCause() == null ? exception.getMessage() : exception.getCause().getMessage(),
                    "Lỗi xóa chi nhánh",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void showEditView(String maCn) {
        suaChiNhanh.showEditor(this, maCn);
    }

    private void showCreateView() {
        themChiNhanh.showCreator(this);
    }

    private String formatDate(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATE_FORMATTER);
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
        return 100;
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
