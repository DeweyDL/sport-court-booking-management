package com.sportcourt.modules.staff.view;

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
import java.util.List;

public class StaffPanel extends JPanel {

    private static final Color ALTERNATE_ROW_BG = new Color(248, 250, 252);

    private final StaffService staffService       = new StaffServiceImpl();
    private final JPanel       tablePanel         = new JPanel();
    private final JLabel       infoLabel          = new JLabel("Đang tải dữ liệu...");
    private final JTextField   searchField        = new JTextField(30);
    private final JPanel       searchWrapper      = new JPanel(new BorderLayout());
    private final Timer        searchDebounceTimer;

    public StaffPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(100, 70, 50, 70));

        searchDebounceTimer = new Timer(300, e -> loadData());
        searchDebounceTimer.setRepeats(false);

        add(createPage(), BorderLayout.CENTER);
        loadData();
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
        container.setBorder(new EmptyBorder(20, 0, 20, 0));

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
        toolbar.setBorder(new EmptyBorder(10, 20, 20, 20));

        JPanel leftToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftToolbar.setBackground(Color.WHITE);

        JLabel tableTitle = new JLabel("DANH SÁCH NHÂN VIÊN");
        tableTitle.setFont(new Font("Lexend", Font.BOLD, 22));

        JButton addBtn = createPillButton("+ Thêm nhân viên", new Color(228, 250, 226), new Color(16, 110, 0), true);
        addBtn.setFont(new Font("Lexend", Font.BOLD, 17));
        addBtn.addActionListener(e -> new AddStaffDialog((JFrame) SwingUtilities.getWindowAncestor(this), this).setVisible(true));

        leftToolbar.add(tableTitle);
        leftToolbar.add(addBtn);
        toolbar.add(leftToolbar, BorderLayout.WEST);

        JPanel rightToolbar = new JPanel();
        rightToolbar.setLayout(new BoxLayout(rightToolbar, BoxLayout.X_AXIS));
        rightToolbar.setBackground(Color.WHITE);
        rightToolbar.add(createSearchFieldWithIcon());
        toolbar.add(rightToolbar, BorderLayout.EAST);

        return toolbar;
    }

    private JPanel createSearchFieldWithIcon() {
        searchWrapper.removeAll();
        searchWrapper.setOpaque(false);
        searchWrapper.setPreferredSize(new Dimension(320, 45));
        searchWrapper.setMaximumSize(new Dimension(320, 45));

        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.putClientProperty("JTextField.placeholderText", "Tìm theo Mã NV, Tên, CCCD...");
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
        innerPanel.setPreferredSize(new Dimension(320, 45));
        innerPanel.setMaximumSize(new Dimension(320, 45));
        innerPanel.setBorder(new EmptyBorder(0, 12, 0, 12));
        innerPanel.add(iconLabel, BorderLayout.WEST);
        innerPanel.add(searchField, BorderLayout.CENTER);

        searchWrapper.add(innerPanel, BorderLayout.CENTER);
        return searchWrapper;
    }

    // --------- TABLE HEADER ---------

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

        header.add(createFixedCell(createHeaderLabel("MÃ NV"),         110, 45, SwingConstants.LEFT,   new Color(248, 249, 250), 0, 8));
        header.add(Box.createHorizontalStrut(8));
        header.add(createFixedCell(createHeaderLabel("HỌ TÊN"),        180, 45, SwingConstants.LEFT,   new Color(248, 249, 250), 0, 8));
        header.add(Box.createHorizontalStrut(8));
        header.add(createFixedCell(createHeaderLabel("CĂN CƯỚC CD"),   150, 45, SwingConstants.CENTER, new Color(248, 249, 250), 0, 8));
        header.add(Box.createHorizontalStrut(8));
        header.add(createFixedCell(createHeaderLabel("NGÀY VÀO LÀM"), 135, 45, SwingConstants.CENTER, new Color(248, 249, 250), 0, 8));
        header.add(Box.createHorizontalStrut(8));
        header.add(createFixedCell(createHeaderLabel("CHỨC VỤ"),       110, 45, SwingConstants.CENTER, new Color(248, 249, 250), 0, 8));
        header.add(Box.createHorizontalStrut(8));
        header.add(createFixedCell(createHeaderLabel("TRẠNG THÁI"),    120, 45, SwingConstants.CENTER, new Color(248, 249, 250), 0, 8));
        header.add(Box.createHorizontalStrut(8));
        header.add(createFixedCell(createHeaderLabel("THAO TÁC"),      200, 45, SwingConstants.CENTER, new Color(248, 249, 250), 0, 0));
        return header;
    }

    private JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 17));
        label.setForeground(new Color(107, 114, 128));
        return label;
    }

    // --------- DATA ROW ---------

    private JPanel createDataRow(StaffResponse staff, int rowIndex) {
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

        // Cột 1: Mã NV
        JLabel idLabel = new JLabel(staff.getManv());
        idLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        idLabel.setForeground(new Color(22, 163, 74));
        row.add(createFixedCell(idLabel, 110, 64, SwingConstants.LEFT, rowBg, 0, 8));
        row.add(Box.createHorizontalStrut(8));

        // Cột 2: Họ tên
        row.add(createFixedCell(createCellLabel(staff.getHoten(), new Color(17, 24, 39)), 180, 64, SwingConstants.LEFT, rowBg, 0, 8));
        row.add(Box.createHorizontalStrut(8));

        // Cột 3: CCCD
        JLabel cccdLabel = createCellLabel(staff.getCccd(), new Color(75, 85, 99));
        cccdLabel.setHorizontalAlignment(SwingConstants.CENTER);
        row.add(createFixedCell(cccdLabel, 150, 64, SwingConstants.CENTER, rowBg, 0, 8));
        row.add(Box.createHorizontalStrut(8));

        // Cột 4: Ngày vào làm
        JLabel dateLabel = createCellLabel(staff.getNgayVaoLamFormatted(), new Color(75, 85, 99));
        dateLabel.setHorizontalAlignment(SwingConstants.CENTER);
        row.add(createFixedCell(dateLabel, 135, 64, SwingConstants.CENTER, rowBg, 0, 8));
        row.add(Box.createHorizontalStrut(8));

        // Cột 5: Chức vụ - badge
        JPanel chucVuBadge = createRoleBadge(staff.getIsQl());
        JPanel chucVuCell  = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        chucVuCell.setBackground(rowBg);
        chucVuCell.add(chucVuBadge);
        row.add(createFixedCell(chucVuCell, 110, 64, SwingConstants.CENTER, rowBg, 0, 8));
        row.add(Box.createHorizontalStrut(8));

        // Cột 6: Trạng thái - badge
        JPanel statusBadge = createStatusBadge(staff.getTrangThai());
        JPanel statusCell  = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        statusCell.setBackground(rowBg);
        statusCell.add(statusBadge);
        row.add(createFixedCell(statusCell, 120, 64, SwingConstants.CENTER, rowBg, 0, 8));
        row.add(Box.createHorizontalStrut(8));

        // Cột 7: Thao tác
        JPanel actionGroup = new JPanel();
        actionGroup.setLayout(new BoxLayout(actionGroup, BoxLayout.X_AXIS));
        actionGroup.setOpaque(false);

        JButton deleteBtn = createMiniActionButton("Xóa", new Color(254, 226, 226), new Color(185, 28, 28));
        Dimension deleteBtnSize = new Dimension(80, 30);
        deleteBtn.setPreferredSize(deleteBtnSize);
        deleteBtn.setMinimumSize(deleteBtnSize);
        deleteBtn.setMaximumSize(deleteBtnSize);
        deleteBtn.addActionListener(e -> confirmDelete(staff));
        actionGroup.add(deleteBtn);
        actionGroup.add(Box.createHorizontalStrut(10));

        JButton editBtn = createMiniActionButton("Chỉnh sửa", new Color(239, 246, 255), new Color(29, 78, 216));
        Dimension editBtnSize = new Dimension(89, 30);
        editBtn.setPreferredSize(editBtnSize);
        editBtn.setMinimumSize(editBtnSize);
        editBtn.setMaximumSize(editBtnSize);
        editBtn.addActionListener(e -> new EditStaffDialog((JFrame) SwingUtilities.getWindowAncestor(this), this, staff).setVisible(true));
        actionGroup.add(editBtn);

        JPanel actionCell = new JPanel(new BorderLayout());
        actionCell.setBackground(rowBg);
        actionCell.setOpaque(true);
        actionCell.setBorder(new EmptyBorder(0, 16, 0, 4));
        actionCell.add(actionGroup, BorderLayout.CENTER);
        row.add(createFixedCell(actionCell, 200, 64, SwingConstants.LEFT, rowBg, 0, 0));

        // Hover effect
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

    private JPanel createStatusBadge(String trangThai) {
        boolean active = "ACTIVE".equalsIgnoreCase(trangThai);
        Color badgeBg  = active ? new Color(220, 252, 231)  : new Color(254, 226, 226);
        Color badgeFg  = active ? new Color(22, 101, 52)    : new Color(185, 28, 28);
        String text    = active ? "Hoạt động" : (trangThai == null ? "--" : trangThai);
        return makeBadge(text, badgeBg, badgeFg);
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

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 17));
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
            List<StaffResponse> staffList = staffService.searchStaff(criteria);

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

    // --------- SEARCH ---------

    private void bindSearchListener() {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { searchDebounceTimer.restart(); }
            @Override public void removeUpdate(DocumentEvent e)  { searchDebounceTimer.restart(); }
            @Override public void changedUpdate(DocumentEvent e) { searchDebounceTimer.restart(); }
        });
    }

    // --------- DELETE ---------

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

    // --------- HELPERS ---------

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

    private JLabel createCellLabel(String text, Color fg) {
        JLabel label = new JLabel(text == null || text.isBlank() ? "--" : text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 15));
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
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(5, 12, 5, 12));
        return btn;
    }

    private JButton createMiniActionButton(String text, Color bg, Color fg) {
        JButton btn = createPillButton(text, bg, fg, true);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setBorder(new EmptyBorder(6, 10, 6, 10));
        return btn;
    }
}
