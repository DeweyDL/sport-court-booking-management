package com.sportcourt.modules.court.view;

import com.sportcourt.common.style.AppDialog;
import com.sportcourt.common.style.AppFonts;
import com.sportcourt.modules.auth.service.SessionManager;
import com.sportcourt.modules.court.controller.CourtManagementController;
import com.sportcourt.modules.court.dto.CourtSearchCriteria;
import com.sportcourt.modules.court.dto.CourtTableRow;
import com.sportcourt.modules.court.entity.Court;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.net.URL;
import java.sql.SQLException;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class CourtManagementPanel extends JPanel {
    private static final Color ALTERNATE_ROW_BG = new Color(248, 250, 252);
    private static final int HEADER_HEIGHT = 45;
    private static final int ROW_HEIGHT = 64;
    private static final int COLUMN_GAP = 16;
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final CourtManagementController controller = new CourtManagementController();
    private final String branchId;
    private final List<CourtTableRow> courts = new ArrayList<>();

    private final JPanel tablePanel = new JPanel();
    private final JLabel footerLabel = new JLabel("Đang tải dữ liệu...");
    private final JTextField searchField = new JTextField(30);
    private final JPanel searchWrapper = new JPanel(new BorderLayout());
    private final JComboBox<String> cbSort = new JComboBox<>(new String[]{
            "Mã sân",
            "Ngày tạo",
            "Trạng thái"
    });
    private final JButton btnSortDir = new JButton("\u25B2");

    private CourtTableRow selectedCourt;
    private boolean sortAscending = true;

    public CourtManagementPanel() {
        AppFonts.register();
        this.branchId = SessionManager.requireSession().getBranchId();
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(100, 70, 50, 70));

        add(createPage(), BorderLayout.CENTER);
        loadCourts("");
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

        JLabel title = new JLabel("QUẢN LÝ SÂN CON");
        title.setFont(new Font("Lexend", Font.BOLD, 30));
        title.setForeground(new Color(30, 31, 36));
        title.setBorder(new EmptyBorder(0, 20, 0, 0));

        JLabel subtitle = new JLabel("Quản lý thông tin sân con và trạng thái hoạt động.");
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

        JLabel tableTitle = new JLabel("DANH SÁCH SÂN CON");
        tableTitle.setFont(new Font("Lexend", Font.BOLD, 22));

        JButton addBtn = createPillButton("+ Thêm sân con", new Color(228, 250, 226), new Color(16, 110, 0), true);
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
        searchField.putClientProperty("JTextField.placeholderText", "Tìm theo mã sân hoặc khu vực...");
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

        gbc.weightx = 0.14; header.add(createFlexibleCell(createHeaderLabel("MÃ SÂN"), SwingConstants.CENTER, new Color(248, 249, 250), 0, 8), gbc);
        gbc.weightx = 0.14; header.add(createFlexibleCell(createHeaderLabel("MÃ KHU VỰC"), SwingConstants.CENTER, new Color(248, 249, 250), 0, 8), gbc);
        gbc.weightx = 0.20; header.add(createFlexibleCell(createHeaderLabel("LOẠI THỂ THAO"), SwingConstants.CENTER, new Color(248, 249, 250), 0, 8), gbc);
        gbc.weightx = 0.14; header.add(createFlexibleCell(createHeaderLabel("TRẠNG THÁI"), SwingConstants.CENTER, new Color(248, 249, 250), 0, 8), gbc);
        gbc.weightx = 0.16; header.add(createFlexibleCell(createHeaderLabel("NGÀY TẠO"), SwingConstants.CENTER, new Color(248, 249, 250), 0, 8), gbc);
        gbc.weightx = 0.22; gbc.insets = new Insets(0, 0, 0, 0); header.add(createFlexibleCell(createHeaderLabel("THAO TÁC"), SwingConstants.CENTER, new Color(248, 249, 250), 0, 8), gbc);
        return header;
    }

    private JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 17));
        label.setForeground(new Color(107, 114, 128));
        return label;
    }

    private JPanel createDataRow(CourtTableRow court, int rowIndex) {
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

        JLabel idLabel = new JLabel(valueOrDash(court.getCourtId()));
        idLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        idLabel.setForeground(new Color(22, 163, 74));

        gbc.weightx = 0.14; row.add(createFlexibleCell(idLabel, SwingConstants.LEFT, rowBg, 0, 8), gbc);
        gbc.weightx = 0.14; row.add(createFlexibleCell(createCellLabel(court.getAreaId(), new Color(17, 24, 39)), SwingConstants.CENTER, rowBg, 0, 8), gbc);
        gbc.weightx = 0.20; row.add(createFlexibleCell(createCellLabel(court.getSportTypeName(), new Color(75, 85, 99)), SwingConstants.CENTER, rowBg, 0, 8), gbc);
        gbc.weightx = 0.14; row.add(createFlexibleCell(createStatusPill(court.getStatus()), SwingConstants.CENTER, rowBg, 0, 8), gbc);
        gbc.weightx = 0.16; row.add(createFlexibleCell(createCellLabel(formatDate(court.getCreatedAt()), new Color(75, 85, 99)), SwingConstants.CENTER, rowBg, 0, 8), gbc);

        JPanel actionGroup = new JPanel();
        actionGroup.setLayout(new BoxLayout(actionGroup, BoxLayout.X_AXIS));
        actionGroup.setOpaque(false);

        JButton deleteBtn = createMiniActionButton("Xóa", new Color(254, 226, 226), new Color(185, 28, 28));
        Dimension deleteBtnSize = new Dimension(80, 30);
        deleteBtn.setPreferredSize(deleteBtnSize);
        deleteBtn.setMinimumSize(deleteBtnSize);
        deleteBtn.setMaximumSize(deleteBtnSize);
        deleteBtn.addActionListener(event -> {
            selectedCourt = court;
            deleteSelectedCourt();
        });
        actionGroup.add(deleteBtn);
        actionGroup.add(Box.createHorizontalStrut(10));

        JButton editBtn = createMiniActionButton("Chỉnh sửa", new Color(239, 246, 255), new Color(29, 78, 216));
        Dimension editBtnSize = new Dimension(89, 30);
        editBtn.setPreferredSize(editBtnSize);
        editBtn.setMinimumSize(editBtnSize);
        editBtn.setMaximumSize(editBtnSize);
        editBtn.addActionListener(event -> {
            selectedCourt = court;
            openEditForSelectedCourt();
        });
        actionGroup.add(editBtn);

        JPanel actionCell = new JPanel(new GridBagLayout());
        actionCell.setBackground(rowBg);
        actionCell.setOpaque(true);
        actionCell.add(actionGroup);

        gbc.weightx = 0.22; gbc.insets = new Insets(0, 0, 0, 0); row.add(createFlexibleCell(actionCell, SwingConstants.CENTER, rowBg, 0, 0), gbc);

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

    private JPanel createStatusPill(String trangThai) {
        boolean isActive = "ĐANG HOẠT ĐỘNG".equalsIgnoreCase(trangThai);
        Color background = isActive ? new Color(228, 250, 226) : new Color(254, 226, 226);
        Color foreground = isActive ? new Color(16, 110, 0) : new Color(185, 28, 28);
        String displayText = isActive ? "Hoạt động" : "Bảo trì";

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
        Dimension size = new Dimension(100, 24);
        wrapper.setPreferredSize(size);
        wrapper.setMinimumSize(size);
        wrapper.setMaximumSize(size);

        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);

        JPanel dot = createStatusDot(foreground);
        JLabel textLabel = new JLabel(displayText);
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

        JLabel msg = new JLabel("Không tìm thấy sân con phù hợp.");
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        msg.setForeground(new Color(107, 114, 128));
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
        label.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        label.setForeground(fg);
        return label;
    }

    private void loadCourts(String keyword) {
        String selectedId = selectedCourt == null ? null : selectedCourt.getCourtId();
        try {
            CourtSearchCriteria criteria = new CourtSearchCriteria();
            criteria.setBranchId(branchId);
            criteria.setKeyword(keyword);

            List<CourtTableRow> result = controller.search(criteria);
            courts.clear();
            courts.addAll(result);

            sortCourts();
            renderTable();
            restoreSelection(selectedId);
        } catch (SQLException e) {
            AppDialog.showError(this, normalizeError("Không thể tải danh sách sân con.", e.getMessage()));
        }
    }

    private void renderTable() {
        tablePanel.removeAll();

        if (courts.isEmpty()) {
            tablePanel.add(createEmptyRow());
        } else {
            int index = 0;
            for (CourtTableRow court : courts) {
                tablePanel.add(createDataRow(court, index++));
            }
        }

        footerLabel.setText("Hiển thị " + courts.size() + " sân con");
        tablePanel.revalidate();
        tablePanel.repaint();
    }

    private void bindSearchListener() {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshCourts();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshCourts();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshCourts();
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
            String selectedId = selectedCourt == null ? null : selectedCourt.getCourtId();
            sortCourts();
            renderTable();
            restoreSelection(selectedId);
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
            String selectedId = selectedCourt == null ? null : selectedCourt.getCourtId();
            sortCourts();
            renderTable();
            restoreSelection(selectedId);
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
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 28, 28);
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

    private void sortCourts() {
        String sortType = (String) cbSort.getSelectedItem();
        Comparator<CourtTableRow> comparator;
        if ("Ngày tạo".equals(sortType)) {
            comparator = Comparator.comparing(
                    (CourtTableRow c) -> c.getCreatedAt() == null ? LocalDateTime.MIN : c.getCreatedAt()
            ).thenComparing(c -> normalizedSortKey(c.getCourtId()));
        } else if ("Trạng thái".equals(sortType)) {
            comparator = Comparator.comparingInt((CourtTableRow c) -> statusOrder(c.getStatus()))
                    .thenComparing(c -> normalizedSortKey(c.getCourtId()));
        } else {
            comparator = Comparator.comparing(c -> normalizedSortKey(c.getCourtId()));
        }
        if (!sortAscending) {
            comparator = comparator.reversed();
        }
        courts.sort(comparator);
    }

    private void updateSortDirectionButton() {
        btnSortDir.setText(sortAscending ? "\u25B2" : "\u25BC");
        btnSortDir.setToolTipText(sortAscending
                ? "Đang sắp xếp tăng dần"
                : "Đang sắp xếp giảm dần");
    }

    private int statusOrder(String status) {
        if ("ĐANG HOẠT ĐỘNG".equalsIgnoreCase(status)) {
            return 0;
        }
        if ("BẢO TRÌ".equalsIgnoreCase(status)) {
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

    private void refreshCourts() {
        loadCourts(searchField.getText().trim());
    }

    private void restoreSelection(String courtId) {
        selectedCourt = null;
        if (courtId == null) {
            return;
        }
        for (CourtTableRow court : courts) {
            if (court.getCourtId().equals(courtId)) {
                selectedCourt = court;
                return;
            }
        }
    }

    private void openCreateDialog() {
        try {
            List<String> areaIds = controller.getAreaIdsByBranch(branchId);
            Court court = CourtCreatePanel.show(this, areaIds);
            if (court == null) {
                return;
            }

            controller.create(court, branchId);
            AppDialog.showInfo(this, "Đã thêm sân con thành công.");
            refreshCourts();
        } catch (SQLException e) {
            AppDialog.showError(this, normalizeError("Thêm sân con chưa thành công.", e.getMessage()));
        }
    }

    private void openEditForSelectedCourt() {
        if (selectedCourt == null) {
            return;
        }

        try {
            List<String> areaIds = controller.getAreaIdsByBranch(branchId);
            Court court = CourtEditPanel.show(this, selectedCourt, areaIds);
            if (court == null) {
                return;
            }

            controller.update(court, branchId);
            AppDialog.showInfo(this, "Đã cập nhật thông tin sân con.");
            refreshCourts();
        } catch (SQLException e) {
            AppDialog.showError(this, normalizeError("Cập nhật sân con chưa thành công.", e.getMessage()));
        }
    }

    private void deleteSelectedCourt() {
        if (selectedCourt == null) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa sân con này?",
                "Xác nhận xóa",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            controller.delete(selectedCourt.getCourtId(), branchId);
            AppDialog.showInfo(this, "Xóa sân con thành công.");
            refreshCourts();
        } catch (SQLException e) {
            AppDialog.showError(this, normalizeError("Không thể xóa sân con lúc này.", e.getMessage()));
        }
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

    private String formatDate(LocalDateTime dateTime) {
        return dateTime == null ? "--" : dateTime.format(DATE_FORMATTER);
    }

    private String valueOrDash(String text) {
        return text == null || text.isBlank() ? "--" : text;
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
}
