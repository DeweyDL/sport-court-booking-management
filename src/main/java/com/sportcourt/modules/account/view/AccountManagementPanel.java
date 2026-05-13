package com.sportcourt.modules.account.view;

import com.sportcourt.common.style.AppFonts;
import com.sportcourt.common.style.CrudViewStyle;
import com.sportcourt.modules.account.controller.AccountManagementController;
import com.sportcourt.modules.account.dto.AccountRow;
import com.sportcourt.modules.account.dto.AccountUpsertRequest;
import com.sportcourt.modules.account.dto.RoleGroupOption;
import com.sportcourt.modules.auth.dto.FunctionId;
import com.sportcourt.modules.auth.dto.PermissionAction;
import com.sportcourt.modules.auth.dto.UserSession;
import com.sportcourt.modules.auth.service.SessionManager;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AccountManagementPanel extends JPanel implements Scrollable {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int ROW_HEIGHT = 72;
    private static final Color ALTERNATE_ROW_BG = CrudViewStyle.ALTERNATE_ROW_BACKGROUND;
    private static final Color FOOTER_BACKGROUND = Color.WHITE;
    private static final Color BODY_TEXT = new Color(43, 47, 55);
    private static final Color EDIT_BG = CrudViewStyle.EDIT_BG;
    private static final Color EDIT_TEXT = CrudViewStyle.EDIT_TEXT;
    private static final Color CREATE_BG = CrudViewStyle.SUCCESS_BG;
    private static final Color CREATE_TEXT = CrudViewStyle.SUCCESS_TEXT;
    private static final Color SOFT_RED_BG = CrudViewStyle.DANGER_BG;
    private static final Color SOFT_RED_TEXT = CrudViewStyle.DANGER_TEXT;
    private static final Color INPUT_BORDER = CrudViewStyle.BORDER;

    private final AccountManagementController controller = new AccountManagementController();
    private final JPanel tableBodyPanel = new JPanel();
    private final JLabel footerLabel = new JLabel("Đang hiển thị 0 / 0 tài khoản");
    private final JTextField txtSearch = new JTextField();
    private final JPanel searchWrapper = new JPanel(new BorderLayout());
    private final JComboBox<String> cbSort = new JComboBox<>(new String[]{
            "Username",
            "Họ tên",
            "Trạng thái",
            "Quyền",
            "Ngày tạo"
    });
    private final JButton btnSortDir = new JButton("\u25B2");
    private final Timer searchDebounceTimer;
    private final UserSession session = SessionManager.requireSession();
    private List<RoleGroupOption> roleGroups = new ArrayList<>();
    private boolean sortAscending = true;

    public AccountManagementPanel() {
        AppFonts.register();

        setLayout(new BorderLayout());
        CrudViewStyle.applyPageDefaults(this);

        searchDebounceTimer = new Timer(300, event -> loadAccounts(txtSearch.getText()));
        searchDebounceTimer.setRepeats(false);

        add(createListPage(), BorderLayout.CENTER);
        CrudViewStyle.installResponsiveTypography(this);

        bindSearchListener();
        loadRoleGroupOptions();
        loadAccounts(null);
    }

    private JPanel createListPage() {
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

        JLabel title = new JLabel("QUẢN LÝ TÀI KHOẢN");
        title.setFont(new Font("Lexend", Font.BOLD, 30));
        title.setForeground(new Color(30, 31, 36));
        title.setBorder(new EmptyBorder(0, 20, 0, 0));

        JLabel subtitle = new JLabel("Quản lý thông tin tài khoản người dùng và phân quyền hệ thống.");
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
                g2.setClip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
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

        tableBodyPanel.setLayout(new BoxLayout(tableBodyPanel, BoxLayout.Y_AXIS));
        tableBodyPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(tableBodyPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setColumnHeaderView(createTableHeader());
        container.add(scrollPane, BorderLayout.CENTER);

        JPanel footer = createFooter();
        container.add(footer, BorderLayout.SOUTH);

        return container;
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(new EmptyBorder(8, 20, 14, 20));

        JPanel leftToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftToolbar.setBackground(Color.WHITE);

        JLabel tableTitle = new JLabel("DANH SÁCH TÀI KHOẢN");
        tableTitle.setFont(new Font("Lexend", Font.BOLD, 22));

        JButton addBtn = createPillButton("+ Thêm tài khoản", new Color(228, 250, 226), new Color(16, 110, 0), true);
        addBtn.setFont(new Font("Lexend", Font.BOLD, 16));
        addBtn.setBorder(new EmptyBorder(6, 22, 6, 22));
        CrudViewStyle.applyToolbarButtonHeight(addBtn);
        addBtn.addActionListener(event -> showCreateView());

        leftToolbar.add(tableTitle);
        if (canAdd()) {
            leftToolbar.add(addBtn);
        }
        toolbar.add(leftToolbar, BorderLayout.WEST);

        JPanel rightToolbar = CrudViewStyle.createToolbarActionsPanel();
        rightToolbar.add(createSortWrapper());
        rightToolbar.add(Box.createHorizontalStrut(10));
        rightToolbar.add(createSearchFieldWithIcon());
        toolbar.add(rightToolbar, BorderLayout.EAST);

        return toolbar;
    }

    private JPanel createSearchFieldWithIcon() {
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm theo account, username, họ tên...");
        return CrudViewStyle.createSearchFieldWithIcon(searchWrapper, txtSearch, loadSearchIcon());
    }

    private JPanel createSortWrapper() {
        cbSort.addActionListener(event -> loadAccounts(txtSearch.getText()));
        btnSortDir.addActionListener(event -> {
            sortAscending = !sortAscending;
            CrudViewStyle.updateSortDirectionButton(btnSortDir, sortAscending);
            loadAccounts(txtSearch.getText());
        });
        CrudViewStyle.updateSortDirectionButton(btnSortDir, sortAscending);
        return CrudViewStyle.createSortWrapper(cbSort, btnSortDir);
    }

    private JPanel createTableHeader() {
        JPanel header = new JPanel(new GridBagLayout());
        header.setBackground(CrudViewStyle.TABLE_HEADER_BACKGROUND);
        header.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 1, 0, CrudViewStyle.BORDER),
                new EmptyBorder(0, 24, 0, 24)
        ));
        header.setPreferredSize(new Dimension(1000, 52));
        header.setMinimumSize(new Dimension(800, 52));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 8);

        gbc.weightx = 0.08; header.add(createFlexibleCell(createHeaderLabel("MÃ TK", SwingConstants.CENTER), SwingConstants.CENTER, new Color(248, 249, 250), 0, 8), gbc);
        gbc.weightx = 0.12; header.add(createFlexibleCell(createHeaderLabel("USERNAME", SwingConstants.LEFT), SwingConstants.LEFT, new Color(248, 249, 250), 8, 8), gbc);
        gbc.weightx = 0.18; header.add(createFlexibleCell(createHeaderLabel("HỌ TÊN", SwingConstants.LEFT), SwingConstants.LEFT, new Color(248, 249, 250), 8, 8), gbc);
        gbc.weightx = 0.12; header.add(createFlexibleCell(createHeaderLabel("SĐT", SwingConstants.CENTER), SwingConstants.CENTER, new Color(248, 249, 250), 0, 8), gbc);
        gbc.weightx = 0.08; header.add(createFlexibleCell(createHeaderLabel("STATUS", SwingConstants.CENTER), SwingConstants.CENTER, new Color(248, 249, 250), 0, 8), gbc);
        gbc.weightx = 0.12; header.add(createFlexibleCell(createHeaderLabel("QUYỀN", SwingConstants.CENTER), SwingConstants.CENTER, new Color(248, 249, 250), 0, 8), gbc);
        gbc.weightx = 0.15; header.add(createFlexibleCell(createHeaderLabel("NGÀY TẠO", SwingConstants.CENTER), SwingConstants.CENTER, new Color(248, 249, 250), 0, 8), gbc);
        gbc.weightx = 0.15; gbc.insets = new Insets(0, 0, 0, 0); header.add(createFlexibleCell(createHeaderLabel("THAO TÁC", SwingConstants.CENTER), SwingConstants.CENTER, new Color(248, 249, 250), 0, 0), gbc);

        return header;
    }

    private JPanel createFooter() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(FOOTER_BACKGROUND);
        footerPanel.setBorder(new EmptyBorder(20, 20, 0, 20));
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        footerLabel.setForeground(new Color(107, 114, 128));
        footerPanel.add(footerLabel, BorderLayout.WEST);
        return footerPanel;
    }

    private JLabel createHeaderLabel(String text, int alignment) {
        JLabel label = new JLabel(text, alignment);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(CrudViewStyle.MUTED);
        return label;
    }

    private Icon loadSearchIcon() {
        URL iconUrl = getClass().getResource("/icon/search.png");
        if (iconUrl == null) {
            return UIManager.getIcon("FileView.fileIcon");
        }
        Image image = new ImageIcon(iconUrl).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
        return new ImageIcon(image);
    }

    private void bindSearchListener() {
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                searchDebounceTimer.restart();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                searchDebounceTimer.restart();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                searchDebounceTimer.restart();
            }
        });
    }

    private void loadRoleGroupOptions() {
        try {
            roleGroups = controller.getRoleGroups();
        } catch (Exception exception) {
            roleGroups = new ArrayList<>();
            JOptionPane.showMessageDialog(
                    this,
                    exception.getCause() == null ? exception.getMessage() : exception.getCause().getMessage(),
                    "Lỗi tải role group",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void loadAccounts(String keyword) {
        footerLabel.setText("Đang tải dữ liệu...");
        SwingWorker<List<AccountRow>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<AccountRow> doInBackground() throws Exception {
                return controller.searchAccounts(keyword == null ? "" : keyword.trim());
            }

            @Override
            protected void done() {
                try {
                    renderTableData(get());
                } catch (Exception exception) {
                    renderErrorState(exception);
                }
            }
        };
        worker.execute();
    }

    private void renderTableData(List<AccountRow> rows) {
        List<AccountRow> sortedRows = new ArrayList<>(rows);
        sortAccounts(sortedRows);
        tableBodyPanel.removeAll();
        if (sortedRows.isEmpty()) {
            tableBodyPanel.add(createEmptyRow("Không tìm thấy account phù hợp."));
        } else {
            int index = 0;
            for (AccountRow row : sortedRows) {
                tableBodyPanel.add(createDataRow(row, index++));
            }
        }
        footerLabel.setText("Đang hiển thị " + rows.size() + " / " + rows.size() + " tài khoản");
        tableBodyPanel.revalidate();
        tableBodyPanel.repaint();
    }

    private void sortAccounts(List<AccountRow> rows) {
        String sortType = (String) cbSort.getSelectedItem();
        Comparator<AccountRow> comparator;
        if ("Họ tên".equals(sortType)) {
            comparator = Comparator.comparing(row -> sortKey(row.getDisplayName()));
        } else if ("Trạng thái".equals(sortType)) {
            comparator = Comparator.comparing(row -> sortKey(displayStatus(row)));
        } else if ("Quyền".equals(sortType)) {
            comparator = Comparator.comparing(row -> sortKey(displayRole(row)));
        } else if ("Ngày tạo".equals(sortType)) {
            comparator = Comparator.comparing(AccountRow::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
        } else {
            comparator = Comparator.comparing(row -> sortKey(row.getUsername()));
        }
        comparator = comparator.thenComparing(row -> sortKey(row.getAccountId()));
        if (!sortAscending) {
            comparator = comparator.reversed();
        }
        rows.sort(comparator);
    }

    private String sortKey(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private void renderErrorState(Exception exception) {
        tableBodyPanel.removeAll();
        tableBodyPanel.add(createEmptyRow("Không thể tải dữ liệu account."));
        footerLabel.setText("Lỗi tải dữ liệu");
        tableBodyPanel.revalidate();
        tableBodyPanel.repaint();
        JOptionPane.showMessageDialog(
                this,
                exception.getCause() == null ? exception.getMessage() : exception.getCause().getMessage(),
                "Lỗi quản lý tài khoản",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private JPanel createDataRow(AccountRow row, int rowIndex) {
        Color rowBg = rowIndex % 2 == 0 ? Color.WHITE : ALTERNATE_ROW_BG;

        JPanel rowPanel = new JPanel(new GridBagLayout());
        rowPanel.setBackground(rowBg);
        rowPanel.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, CrudViewStyle.ROW_BORDER),
                new EmptyBorder(0, 24, 0, 24)
        ));
        rowPanel.setPreferredSize(new Dimension(0, ROW_HEIGHT));
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, ROW_HEIGHT));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 8);

        JLabel accountIdLabel = createBodyLabel(row.getAccountId(), true);

        gbc.weightx = 0.08; rowPanel.add(createFlexibleCell(accountIdLabel, SwingConstants.CENTER, rowBg, 0, 8), gbc);
        gbc.weightx = 0.12; rowPanel.add(createFlexibleCell(createBodyLabel(row.getUsername(), false), SwingConstants.LEFT, rowBg, 8, 8), gbc);
        gbc.weightx = 0.18; rowPanel.add(createFlexibleCell(createBodyLabel(row.getDisplayName(), false), SwingConstants.LEFT, rowBg, 8, 8), gbc);
        gbc.weightx = 0.12; rowPanel.add(createFlexibleCell(createBodyLabel(row.getPhone(), false), SwingConstants.CENTER, rowBg, 0, 8), gbc);
        gbc.weightx = 0.08; rowPanel.add(createFlexibleCell(createStatusPill(row), SwingConstants.CENTER, rowBg, 0, 8), gbc);
        gbc.weightx = 0.12; rowPanel.add(createFlexibleCell(createBodyLabel(displayRole(row), true), SwingConstants.CENTER, rowBg, 0, 8), gbc);
        gbc.weightx = 0.15; rowPanel.add(createFlexibleCell(createBodyLabel(formatDate(row.getCreatedAt()), false), SwingConstants.CENTER, rowBg, 0, 8), gbc);

        JPanel actionGroup = createActionButtons(row);
        gbc.weightx = 0.15; gbc.insets = new Insets(0, 0, 0, 0); 
        rowPanel.add(createFlexibleCell(actionGroup, SwingConstants.CENTER, rowBg, 0, 0), gbc);

        rowPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { rowPanel.setBackground(new Color(249, 250, 251)); }
            @Override public void mouseExited(java.awt.event.MouseEvent e) { rowPanel.setBackground(rowBg); }
        });

        return rowPanel;
    }

    private JPanel createActionButtons(AccountRow row) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        panel.setOpaque(false);

        boolean isDeleted = row.isDeleted();
        if (isDeleted) {
            JButton restoreButton = createMiniActionButton("Khôi phục", CREATE_BG, CREATE_TEXT);
            restoreButton.addActionListener(event -> handleRestore(row));
            restoreButton.setEnabled(canRestore());
            panel.add(restoreButton);
        } else {
            JButton deleteButton = createMiniActionButton("Xóa", SOFT_RED_BG, SOFT_RED_TEXT);
            deleteButton.addActionListener(event -> handleDelete(row));
            deleteButton.setEnabled(canDelete());
            panel.add(deleteButton);

            JButton editButton = createMiniActionButton("Chỉnh sửa", EDIT_BG, EDIT_TEXT);
            editButton.addActionListener(event -> showEditView(row));
            editButton.setEnabled(canEdit());
            panel.add(editButton);
        }
        return panel;
    }

    private void showCreateView() {
        String generatedAccountId;
        try {
            generatedAccountId = controller.generateNextAccountId();
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getCause() == null ? exception.getMessage() : exception.getCause().getMessage(),
                    "Lỗi sinh mã account",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        AccountUpsertRequest request = AccountCreatePanel.show(this, roleGroups, generatedAccountId);
        if (request == null) {
            return;
        }
        try {
            controller.createAccount(request);
            loadAccounts(txtSearch.getText());
            JOptionPane.showMessageDialog(this, "Đã tạo account thành công.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getCause() == null ? exception.getMessage() : exception.getCause().getMessage(),
                    "Lỗi tạo account",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void showEditView(AccountRow row) {
        AccountEditPanel.EditResult result = AccountEditPanel.show(this, row, roleGroups);
        if (result == null || result.action() == AccountEditPanel.EditAction.CANCEL) {
            return;
        }
        try {
            controller.updateAccount(result.request());
            loadAccounts(txtSearch.getText());
            JOptionPane.showMessageDialog(this, "Đã cập nhật account thành công.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getCause() == null ? exception.getMessage() : exception.getCause().getMessage(),
                    "Lỗi cập nhật account",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void handleDelete(AccountRow row) {
        int option = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa account " + row.getAccountId() + " ?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION
        );
        if (option != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            controller.deleteAccount(row.getAccountId());
            loadAccounts(txtSearch.getText());
            JOptionPane.showMessageDialog(this, "Đã xóa account thành công.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getCause() == null ? exception.getMessage() : exception.getCause().getMessage(),
                    "Lỗi xóa account",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void handleRestore(AccountRow row) {
        int option = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn khôi phục account " + row.getAccountId() + " ?",
                "Xác nhận khôi phục",
                JOptionPane.YES_NO_OPTION
        );
        if (option != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            controller.restoreAccount(row.getAccountId());
            loadAccounts(txtSearch.getText());
            JOptionPane.showMessageDialog(this, "Đã khôi phục account thành công.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getCause() == null ? exception.getMessage() : exception.getCause().getMessage(),
                    "Lỗi khôi phục account",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private JPanel createEmptyRow(String message) {
        JPanel rowPanel = new JPanel(new BorderLayout());
        rowPanel.setBackground(Color.WHITE);
        rowPanel.setBorder(new EmptyBorder(24, 26, 24, 26));
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));

        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageLabel.setForeground(new Color(107, 114, 128));
        rowPanel.add(messageLabel, BorderLayout.CENTER);
        return rowPanel;
    }

    private JLabel createBodyLabel(String text, boolean bold) {
        JLabel label = new JLabel(text == null || text.isBlank() ? "--" : text);
        label.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, 16));
        label.setForeground(BODY_TEXT);
        return label;
    }

    private JPanel createFlexibleCell(Component component, int alignment, Color bg, int leftPad, int rightPad) {
        if (component instanceof JLabel label) {
            label.setHorizontalAlignment(alignment);
        }
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(bg);
        panel.setOpaque(true);
        panel.setBorder(new EmptyBorder(0, leftPad, 0, rightPad));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.NONE; // Don't stretch, just center
        gbc.anchor = alignment == SwingConstants.LEFT ? GridBagConstraints.WEST : 
                     alignment == SwingConstants.RIGHT ? GridBagConstraints.EAST : 
                     GridBagConstraints.CENTER;
        
        panel.add(component, gbc);

        panel.setPreferredSize(new Dimension(0, ROW_HEIGHT));
        panel.setMinimumSize(new Dimension(0, ROW_HEIGHT));
        return panel;
    }

    private String formatDate(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATE_FORMATTER);
    }

    private String displayStatus(AccountRow row) {
        return row.isDeleted() ? "DELETED" : row.getStatus();
    }

    private JPanel createStatusPill(AccountRow row) {
        String status = displayStatus(row);
        boolean active = "ACTIVE".equalsIgnoreCase(status);
        boolean inactive = "INACTIVE".equalsIgnoreCase(status) || "DELETED".equalsIgnoreCase(status);
        Color background = active ? CREATE_BG : inactive ? SOFT_RED_BG : EDIT_BG;
        Color foreground = active ? CREATE_TEXT : inactive ? SOFT_RED_TEXT : EDIT_TEXT;
        return CrudViewStyle.createStatusPill(status, background, foreground);
    }

    private String displayRole(AccountRow row) {
        return row.getGroupName() == null || row.getGroupName().isBlank() ? row.getGroupId() : row.getGroupName();
    }

    private String findRoleName(String groupId) {
        if (groupId == null) {
            return "--";
        }
        for (RoleGroupOption option : roleGroups) {
            if (groupId.equals(option.getGroupId())) {
                return option.getGroupName();
            }
        }
        return groupId;
    }

    private JButton createPillButton(String text, Color background, Color foreground, boolean isBold) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(background);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                super.paintComponent(graphics);
                g2.dispose();
            }
        };
        button.setForeground(foreground);
        button.setFont(new Font("Segoe UI", isBold ? Font.BOLD : Font.PLAIN, 13));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(5, 12, 5, 12));
        return button;
    }

    private JButton createMiniActionButton(String text, Color background, Color foreground) {
        JButton button = createPillButton(text, background, foreground, true);
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

    private boolean canAdd() {
        return session.hasPermission(FunctionId.ACCOUNT_MANAGEMENT, PermissionAction.ADD);
    }

    private boolean canEdit() {
        return session.hasPermission(FunctionId.ACCOUNT_MANAGEMENT, PermissionAction.EDIT);
    }

    private boolean canDelete() {
        return session.hasPermission(FunctionId.ACCOUNT_MANAGEMENT, PermissionAction.DELETE);
    }

    private boolean canRestore() {
        return session.hasPermission(FunctionId.ACCOUNT_MANAGEMENT, PermissionAction.EDIT);
    }
}
