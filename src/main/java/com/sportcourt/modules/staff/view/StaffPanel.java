package com.sportcourt.modules.staff.view;

import com.sportcourt.common.style.CrudViewStyle;
import com.sportcourt.modules.staff.dto.StaffResponse;
import com.sportcourt.modules.staff.dto.StaffSearchCriteria;
import com.sportcourt.modules.staff.service.StaffService;
import com.sportcourt.modules.staff.service.StaffServiceImpl;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class StaffPanel extends JPanel implements Scrollable {

    private static final Color ALTERNATE_ROW_BG = CrudViewStyle.ALTERNATE_ROW_BACKGROUND;

    private final StaffService staffService       = new StaffServiceImpl();
    private final JPanel       tablePanel         = new JPanel();
    private final JLabel       infoLabel          = new JLabel("Đang tải dữ liệu...");
    private final JTextField   searchField        = new JTextField(30);
    private final JPanel       searchWrapper      = new JPanel(new BorderLayout());
    private final JComboBox<String> cbSort = new JComboBox<>(new String[]{
            "Họ tên",
            "Mã NV",
            "Ngày vào làm",
            "Chức vụ",
            "Trạng thái"
    });
    private final JButton btnSortDir = new JButton("\u25B2");
    private final Timer        searchDebounceTimer;
    private boolean sortAscending = true;

    public StaffPanel() {
        setLayout(new BorderLayout());
        CrudViewStyle.applyPageDefaults(this);

        searchDebounceTimer = new Timer(300, e -> loadData());
        searchDebounceTimer.setRepeats(false);

        add(createPage(), BorderLayout.CENTER);
        CrudViewStyle.installResponsiveTypography(this);
        loadData();
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

        JLabel title = new JLabel("QUẢN LÝ NHÂN VIÊN");
        title.setFont(new Font("Lexend", Font.BOLD, 30));
        title.setForeground(new Color(30, 31, 36));
        title.setBorder(new EmptyBorder(0, 20, 0, 0));

        JLabel subtitle = new JLabel("Quản lý danh sách nhân sự, tìm kiếm và phân quyền.");
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

        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setBackground(Color.WHITE);
        topSection.add(createToolbar());
        topSection.add(createTableHeader());
        container.add(topSection, BorderLayout.NORTH);

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
        infoLabel.setForeground(new Color(107, 114, 128));
        footer.add(infoLabel, BorderLayout.WEST);
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

        JLabel tableTitle = new JLabel("DANH SÁCH NHÂN VIÊN");
        tableTitle.setFont(new Font("Lexend", Font.BOLD, 22));

        JButton addBtn = createPillButton("+ Thêm nhân viên", new Color(228, 250, 226), new Color(16, 110, 0), true);
        addBtn.setFont(new Font("Lexend", Font.BOLD, 16));
        addBtn.setBorder(new EmptyBorder(6, 22, 6, 22));
        CrudViewStyle.applyToolbarButtonHeight(addBtn);
        addBtn.addActionListener(e -> new AddStaffDialog((JFrame) SwingUtilities.getWindowAncestor(this), this, generateNextManv()).setVisible(true));
        JButton refreshBtn = CrudViewStyle.createRefreshButton(e -> loadData());

        leftToolbar.add(tableTitle);
        leftToolbar.add(addBtn);
        leftToolbar.add(refreshBtn);
        toolbar.add(leftToolbar, BorderLayout.WEST);

        JPanel rightToolbar = CrudViewStyle.createToolbarActionsPanel();
        rightToolbar.add(createSortWrapper());
        rightToolbar.add(Box.createHorizontalStrut(10));
        rightToolbar.add(createSearchFieldWithIcon());
        toolbar.add(rightToolbar, BorderLayout.EAST);

        return toolbar;
    }

    private JPanel createSearchFieldWithIcon() {
        searchField.putClientProperty("JTextField.placeholderText", "Tìm theo Mã NV, Tên, CCCD...");
        bindSearchListener();
        return CrudViewStyle.createSearchFieldWithIcon(searchWrapper, searchField, loadSearchIcon());
    }

    private JPanel createSortWrapper() {
        cbSort.addActionListener(event -> loadData());
        btnSortDir.addActionListener(event -> {
            sortAscending = !sortAscending;
            CrudViewStyle.updateSortDirectionButton(btnSortDir, sortAscending);
            loadData();
        });
        CrudViewStyle.updateSortDirectionButton(btnSortDir, sortAscending);
        return CrudViewStyle.createSortWrapper(cbSort, btnSortDir);
    }

    // --------- TABLE HEADER ---------

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
        gbc.insets = new Insets(0, 0, 0, 8);

        gbc.weightx = 0.10; header.add(createFlexibleCell(createHeaderLabel("MÃ NV"),         SwingConstants.CENTER, new Color(248, 249, 250), 0, 0), gbc);
        gbc.weightx = 0.20; header.add(createFlexibleCell(createHeaderLabel("HỌ TÊN"),        SwingConstants.LEFT,   new Color(248, 249, 250), 8, 0), gbc);
        gbc.weightx = 0.15; header.add(createFlexibleCell(createHeaderLabel("CĂN CƯỚC CD"),   SwingConstants.CENTER, new Color(248, 249, 250), 0, 0), gbc);
        gbc.weightx = 0.15; header.add(createFlexibleCell(createHeaderLabel("NGÀY VÀO LÀM"), SwingConstants.CENTER, new Color(248, 249, 250), 0, 0), gbc);
        gbc.weightx = 0.10; header.add(createFlexibleCell(createHeaderLabel("CHỨC VỤ"),       SwingConstants.CENTER, new Color(248, 249, 250), 0, 0), gbc);
        gbc.weightx = 0.10; header.add(createFlexibleCell(createHeaderLabel("TRẠNG THÁI"),    SwingConstants.CENTER, new Color(248, 249, 250), 0, 0), gbc);
        gbc.weightx = 0.20; gbc.insets = new Insets(0, 0, 0, 0); header.add(createFlexibleCell(createHeaderLabel("THAO TÁC"),      SwingConstants.CENTER, new Color(248, 249, 250), 0, 0), gbc);
        return header;
    }

    private JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(new Color(107, 114, 128));
        return label;
    }

    // --------- DATA ROW ---------

    private JPanel createDataRow(StaffResponse staff, int rowIndex) {
        Color rowBg = rowIndex % 2 == 0 ? Color.WHITE : ALTERNATE_ROW_BG;

        JPanel row = new JPanel(new GridBagLayout());
        row.setBackground(rowBg);
        row.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(243, 244, 246)),
                new EmptyBorder(0, 24, 0, 24)
        ));
        row.setPreferredSize(new Dimension(0, 72));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 8);

        // Cột 1: Mã NV
        JLabel idLabel = new JLabel(staff.getManv());
        idLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        idLabel.setForeground(new Color(22, 163, 74));
        gbc.weightx = 0.10; row.add(createFlexibleCell(idLabel, SwingConstants.CENTER, rowBg, 0, 0), gbc);

        // Cột 2: Họ tên
        gbc.weightx = 0.20; row.add(createFlexibleCell(createCellLabel(staff.getHoten(), new Color(17, 24, 39)), SwingConstants.LEFT, rowBg, 8, 0), gbc);

        // Cột 3: CCCD
        gbc.weightx = 0.15; row.add(createFlexibleCell(createCellLabel(staff.getCccd(), new Color(75, 85, 99)), SwingConstants.CENTER, rowBg, 0, 0), gbc);

        // Cột 4: Ngày vào làm
        gbc.weightx = 0.15; row.add(createFlexibleCell(createCellLabel(staff.getNgayVaoLamFormatted(), new Color(75, 85, 99)), SwingConstants.CENTER, rowBg, 0, 0), gbc);

        // Cột 5: Chức vụ
        gbc.weightx = 0.10; row.add(createFlexibleCell(createRoleBadge(staff.getIsQl()), SwingConstants.CENTER, rowBg, 0, 0), gbc);

        // Cột 6: Trạng thái
        gbc.weightx = 0.10; row.add(createFlexibleCell(createStatusBadge(staff), SwingConstants.CENTER, rowBg, 0, 0), gbc);

        // Cột 7: Thao tác
        JPanel actionGroup = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        actionGroup.setOpaque(false);

        if (staff.isDeleted()) {
            JButton restoreBtn = createMiniActionButton("Khôi phục", new Color(228, 250, 226), new Color(16, 110, 0));
            restoreBtn.addActionListener(e -> confirmRestore(staff));
            actionGroup.add(restoreBtn);
        } else {
            JButton deleteBtn = createMiniActionButton("Xóa", new Color(254, 226, 226), new Color(185, 28, 28));
            deleteBtn.addActionListener(e -> confirmDelete(staff));
            actionGroup.add(deleteBtn);

            JButton editBtn = createMiniActionButton("Chỉnh sửa", new Color(239, 246, 255), new Color(29, 78, 216));
            editBtn.addActionListener(e -> new EditStaffDialog((JFrame) SwingUtilities.getWindowAncestor(this), this, staff).setVisible(true));
            actionGroup.add(editBtn);
        }

        JPanel actionCell = new JPanel(new GridBagLayout());
        actionCell.setBackground(rowBg);
        actionCell.setOpaque(true);
        actionCell.add(actionGroup);

        gbc.weightx = 0.20; gbc.insets = new Insets(0, 0, 0, 0); row.add(createFlexibleCell(actionCell, SwingConstants.CENTER, rowBg, 0, 0), gbc);

        row.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { row.setBackground(new Color(249, 250, 251)); }
            @Override public void mouseExited(java.awt.event.MouseEvent e)  { row.setBackground(rowBg); }
        });

        return row;
    }

    // --------- BADGES ---------

    private JPanel createRoleBadge(int isQl) {
        boolean isManager = isQl == 1;
        Color badgeBg = isManager ? new Color(219, 234, 254) : new Color(243, 244, 246);
        Color badgeFg = isManager ? new Color(29, 78, 216)   : new Color(75, 85, 99);
        String text   = isManager ? "Quản lý" : "Nhân viên";
        return makeBadge(text, badgeBg, badgeFg);
    }

    private JPanel createStatusBadge(StaffResponse staff) {
        if (staff.isDeleted()) {
            return CrudViewStyle.createStatusPill("DELETED", CrudViewStyle.DANGER_BG, CrudViewStyle.DANGER_TEXT);
        }
        boolean active = "ACTIVE".equalsIgnoreCase(staff.getTrangThai());
        Color badgeBg  = active ? CrudViewStyle.SUCCESS_BG  : CrudViewStyle.DANGER_BG;
        Color badgeFg  = active ? CrudViewStyle.SUCCESS_TEXT : CrudViewStyle.DANGER_TEXT;
        String text    = active ? "Hoạt động" : (staff.getTrangThai() == null ? "--" : staff.getTrangThai());
        return CrudViewStyle.createStatusPill(text, badgeBg, badgeFg);
    }

    private JPanel makeBadge(String text, Color bg, Color fg) {
        JLabel label = new JLabel(text) {
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
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(fg);
        label.setOpaque(false);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBorder(new EmptyBorder(4, 12, 4, 12));

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.add(label);
        return wrapper;
    }

    private JPanel createMessageRow(String message) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setBorder(new EmptyBorder(24, 26, 24, 26));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));

        JLabel msg = new JLabel(message);
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        msg.setForeground(new Color(107, 114, 128));
        row.add(msg, BorderLayout.CENTER);
        return row;
    }

    // --------- DATA LOAD ---------

    public void loadData() {
        infoLabel.setText("Đang tải dữ liệu...");
        tablePanel.removeAll();

        StaffSearchCriteria criteria = new StaffSearchCriteria();
        criteria.setKeyword(searchField.getText().trim());

        try {
            List<StaffResponse> staffList = new ArrayList<>(staffService.searchStaff(criteria));
            sortStaff(staffList);

            if (staffList.isEmpty()) {
                tablePanel.add(createMessageRow("Không tìm thấy nhân viên phù hợp."));
                infoLabel.setText("Hiển thị 0 nhân viên");
            } else {
                int idx = 0;
                for (StaffResponse staff : staffList) {
                    tablePanel.add(createDataRow(staff, idx++));
                }
                infoLabel.setText("Hiển thị " + staffList.size() + " nhân viên");
            }
        } catch (Exception e) {
            tablePanel.add(createMessageRow("Lỗi tải dữ liệu: " + e.getMessage()));
            e.printStackTrace();
        }

        tablePanel.revalidate();
        tablePanel.repaint();
    }

    private String generateNextManv() {
        try {
            return staffService.generateNextManv();
        } catch (Exception ignored) {
            return "NV-1";
        }
    }

    private void sortStaff(List<StaffResponse> staffList) {
        String sortType = (String) cbSort.getSelectedItem();
        Comparator<StaffResponse> comparator;
        if ("Mã NV".equals(sortType)) {
            comparator = Comparator.comparing(staff -> sortKey(staff.getManv()));
        } else if ("Ngày vào làm".equals(sortType)) {
            comparator = Comparator.comparing(StaffResponse::getNgayVaoLam, Comparator.nullsLast(Comparator.naturalOrder()));
        } else if ("Chức vụ".equals(sortType)) {
            comparator = Comparator.comparingInt(StaffResponse::getIsQl)
                    .thenComparing(staff -> sortKey(staff.getHoten()));
        } else if ("Trạng thái".equals(sortType)) {
            comparator = Comparator.comparing(staff -> sortKey(staff.getTrangThai()));
        } else {
            comparator = Comparator.comparing(staff -> sortKey(staff.getHoten()));
        }
        comparator = comparator.thenComparing(staff -> sortKey(staff.getManv()));
        if (!sortAscending) {
            comparator = comparator.reversed();
        }
        staffList.sort(comparator);
    }

    private String sortKey(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    // --------- SEARCH ---------

    private void bindSearchListener() {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { searchDebounceTimer.restart(); }
            @Override public void removeUpdate(DocumentEvent e)  { searchDebounceTimer.restart(); }
            @Override public void changedUpdate(DocumentEvent e) { searchDebounceTimer.restart(); }
        });
    }

    // --------- DELETE / RESTORE ---------

    private void confirmDelete(StaffResponse staff) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa nhân viên \"" + staff.getHoten() + "\" (Mã: " + staff.getManv() + ")?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                staffService.deleteStaff(staff.getManv());
                JOptionPane.showMessageDialog(this, "Đã xóa nhân viên thành công.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi xóa nhân viên: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void confirmRestore(StaffResponse staff) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn khôi phục nhân viên \"" + staff.getHoten() + "\" (Mã: " + staff.getManv() + ")?",
                "Xác nhận khôi phục",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                staffService.restoreStaff(staff.getManv());
                JOptionPane.showMessageDialog(this, "Đã khôi phục nhân viên thành công.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi khôi phục nhân viên: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
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

        panel.setPreferredSize(new Dimension(0, 72));
        panel.setMinimumSize(new Dimension(0, 72));
        return panel;
    }

    private JLabel createCellLabel(String text, Color fg) {
        JLabel label = new JLabel(text == null || text.isBlank() ? "--" : text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        label.setForeground(fg);
        return label;
    }

    private Icon loadSearchIcon() {
        URL iconUrl = getClass().getResource("/icon/search.png");
        if (iconUrl == null) return UIManager.getIcon("FileView.fileIcon");
        Image image = new ImageIcon(iconUrl).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH);
        return new ImageIcon(image);
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
        JButton btn = createPillButton(text, bg, fg, true);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setBorder(new EmptyBorder(4, 12, 4, 12));
        return btn;
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
