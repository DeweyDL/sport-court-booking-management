package com.sportcourt.modules.account.view;

import com.sportcourt.common.style.AppFonts;
import com.sportcourt.modules.account.controller.AccountManagementController;
import com.sportcourt.modules.account.dto.AccountRow;
import com.sportcourt.modules.account.dto.AccountUpsertRequest;
import com.sportcourt.modules.account.dto.RoleGroupOption;
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
import java.util.List;

public class AccountManagementPanel extends JPanel {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int[] TABLE_COLUMN_WIDTHS = {110, 130, 170, 120, 110, 140, 170, 170};

    private static final Color PAGE_BACKGROUND = new Color(247, 247, 251);
    private static final Color CARD_BORDER = new Color(236, 236, 239);
    private static final Color HEADER_BACKGROUND = new Color(241, 242, 246);
    private static final Color FOOTER_BACKGROUND = new Color(246, 246, 248);
    private static final Color ROW_BORDER = new Color(236, 236, 239);
    private static final Color TITLE_TEXT = new Color(30, 31, 36);
    private static final Color SUBTITLE_TEXT = new Color(103, 112, 133);
    private static final Color HEADER_LABEL_TEXT = new Color(94, 103, 82);
    private static final Color BODY_TEXT = new Color(43, 47, 55);
    private static final Color BLUE_TEXT = new Color(29, 78, 216);
    private static final Color CREATE_BG = new Color(220, 252, 231);
    private static final Color CREATE_TEXT = new Color(22, 101, 52);
    private static final Color EDIT_BG = new Color(239, 246, 255);
    private static final Color EDIT_TEXT = new Color(29, 78, 216);
    private static final Color SOFT_RED_BG = new Color(254, 226, 226);
    private static final Color SOFT_RED_TEXT = new Color(185, 28, 28);
    private static final Color INPUT_BORDER = new Color(229, 231, 235);

    private static final String LIST_CARD = "LIST";
    private static final String DETAIL_CARD = "DETAIL";

    private final AccountManagementController controller = new AccountManagementController();
    private final JPanel tableBodyPanel = new JPanel();
    private final JLabel footerLabel = new JLabel("Đang hiển thị 0 / 0 tài khoản");
    private final JTextField txtSearch = new JTextField();
    private final Timer searchDebounceTimer;
    private final UserSession session = SessionManager.requireSession();
    private final CardLayout contentCardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(contentCardLayout);
    private final AccountDetailPanel detailPanel = new AccountDetailPanel(this::showListView, this::showEditView, this::handleDelete, this::handleRestore);

    private List<RoleGroupOption> roleGroups = new ArrayList<>();

    public AccountManagementPanel() {
        AppFonts.register();

        setLayout(new BorderLayout(0, 18));
        setBackground(PAGE_BACKGROUND);
        setBorder(new EmptyBorder(40, 70, 40, 70));

        searchDebounceTimer = new Timer(300, event -> loadAccounts(txtSearch.getText()));
        searchDebounceTimer.setRepeats(false);

        contentPanel.setOpaque(false);
        contentPanel.add(createListPage(), LIST_CARD);
        contentPanel.add(detailPanel, DETAIL_CARD);
        add(contentPanel, BorderLayout.CENTER);

        bindSearchListener();
        loadRoleGroupOptions();
        loadAccounts(null);
    }

    private JPanel createListPage() {
        JPanel page = new JPanel(new BorderLayout(0, 18));
        page.setOpaque(false);
        page.add(createHeader(), BorderLayout.NORTH);
        page.add(createMainContent(), BorderLayout.CENTER);
        return page;
    }

    private JPanel createHeader() {
        JPanel headerWrapper = new JPanel();
        headerWrapper.setLayout(new BoxLayout(headerWrapper, BoxLayout.Y_AXIS));
        headerWrapper.setOpaque(false);

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.setBorder(new EmptyBorder(0, 20, 0, 0));

        JLabel titleLabel = new JLabel("QUẢN LÝ TÀI KHOẢN");
        titleLabel.setFont(AppFonts.lexendBold(30f));
        titleLabel.setForeground(TITLE_TEXT);
        titleRow.add(titleLabel, BorderLayout.WEST);

        JPanel subtitleRow = new JPanel(new BorderLayout());
        subtitleRow.setOpaque(false);
        subtitleRow.setBorder(new EmptyBorder(5, 20, 20, 0));
        JLabel subtitle = new JLabel("Danh sách account và phân quyền ROLE GROUP.");
        subtitle.setFont(AppFonts.lexendRegular(14f));
        subtitle.setForeground(SUBTITLE_TEXT);
        subtitleRow.add(subtitle, BorderLayout.WEST);

        headerWrapper.add(titleRow);
        headerWrapper.add(subtitleRow);
        return headerWrapper;
    }

    private JPanel createMainContent() {
        JPanel sectionPanel = new JPanel(new BorderLayout(0, 14));
        sectionPanel.setOpaque(false);

        JPanel titlePanel = new JPanel(new BorderLayout(10, 0));
        titlePanel.setOpaque(true);
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(new EmptyBorder(12, 20, 18, 20));

        JLabel title = new JLabel("Danh sách account");
        title.setFont(AppFonts.lexendBold(18f));
        title.setForeground(new Color(35, 37, 43));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleRow.setOpaque(false);
        titleRow.add(title);

        JButton addButton = createPillButton("+ Thêm tài khoản", CREATE_BG, CREATE_TEXT, true);
        addButton.setPreferredSize(new Dimension(165, 36));
        addButton.setBorder(new EmptyBorder(7, 14, 7, 14));
        addButton.setFont(AppFonts.lexendBold(12f));
        addButton.addActionListener(event -> showCreateView());
        addButton.setVisible(canAdd());
        titleRow.add(addButton);

        JPanel searchWrapper = createSearchFieldWithIcon();

        titlePanel.add(titleRow, BorderLayout.WEST);
        titlePanel.add(searchWrapper, BorderLayout.EAST);

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(Color.WHITE);
        tableCard.add(createTableHeader(), BorderLayout.NORTH);

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
                g2.setColor(CARD_BORDER);
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

    private JPanel createSearchFieldWithIcon() {
        txtSearch.setPreferredSize(new Dimension(310, 38));
        txtSearch.setFont(AppFonts.lexendRegular(13f));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm theo account, username, họ tên...");
        txtSearch.setBorder(new EmptyBorder(0, 8, 0, 14));
        txtSearch.setOpaque(false);
        txtSearch.putClientProperty("JComponent.roundRect", true);
        txtSearch.putClientProperty("JTextField.arc", 999);

        JLabel searchIconLabel = new JLabel(loadSearchIcon());
        searchIconLabel.setBorder(new EmptyBorder(0, 12, 0, 0));

        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.setColor(INPUT_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                g2.dispose();
            }
        };
        wrapper.setOpaque(false);
        wrapper.setPreferredSize(new Dimension(310, 38));
        wrapper.add(searchIconLabel, BorderLayout.WEST);
        wrapper.add(txtSearch, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createTableHeader() {
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setBackground(HEADER_BACKGROUND);
        headerPanel.setBorder(new EmptyBorder(14, 24, 14, 24));
        addColumnCell(headerPanel, createHeaderLabel("ACCOUNT ID", SwingConstants.LEFT), 0, SwingConstants.LEFT);
        addColumnCell(headerPanel, createHeaderLabel("USERNAME", SwingConstants.CENTER), 1, SwingConstants.CENTER);
        addColumnCell(headerPanel, createHeaderLabel("HỌ TÊN", SwingConstants.CENTER), 2, SwingConstants.CENTER);
        addColumnCell(headerPanel, createHeaderLabel("SĐT", SwingConstants.CENTER), 3, SwingConstants.CENTER);
        addColumnCell(headerPanel, createHeaderLabel("STATUS", SwingConstants.CENTER), 4, SwingConstants.CENTER);
        addColumnCell(headerPanel, createHeaderLabel("ROLE GROUP", SwingConstants.CENTER), 5, SwingConstants.CENTER);
        addColumnCell(headerPanel, createHeaderLabel("THAO TÁC", SwingConstants.CENTER), 6, SwingConstants.CENTER);
        addColumnCell(headerPanel, createHeaderLabel("NGÀY TẠO", SwingConstants.CENTER), 7, SwingConstants.CENTER);
        return headerPanel;
    }

    private JPanel createFooter() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(FOOTER_BACKGROUND);
        footerPanel.setBorder(new EmptyBorder(18, 22, 18, 22));
        footerLabel.setFont(AppFonts.lexendRegular(14f));
        footerLabel.setForeground(new Color(107, 114, 128));
        footerPanel.add(footerLabel, BorderLayout.WEST);
        return footerPanel;
    }

    private JLabel createHeaderLabel(String text, int alignment) {
        JLabel label = new JLabel(text, alignment);
        label.setFont(AppFonts.lexendBold(13f));
        label.setForeground(HEADER_LABEL_TEXT);
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
        tableBodyPanel.removeAll();
        if (rows.isEmpty()) {
            tableBodyPanel.add(createEmptyRow("Không tìm thấy account phù hợp."));
        } else {
            for (AccountRow row : rows) {
                tableBodyPanel.add(createDataRow(row));
            }
        }
        footerLabel.setText("Đang hiển thị " + rows.size() + " / " + rows.size() + " tài khoản");
        tableBodyPanel.revalidate();
        tableBodyPanel.repaint();
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

    private JPanel createDataRow(AccountRow row) {
        JPanel rowPanel = new JPanel(new GridBagLayout());
        rowPanel.setBackground(Color.WHITE);
        rowPanel.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, ROW_BORDER),
                new EmptyBorder(12, 24, 12, 24)
        ));
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel accountIdLabel = createBodyLabel(row.getAccountId(), true);
        accountIdLabel.setForeground(BLUE_TEXT);
        accountIdLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        accountIdLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showDetailView(row);
            }
        });

        addColumnCell(rowPanel, createTableCellWrapper(accountIdLabel, SwingConstants.LEFT), 0, SwingConstants.LEFT);
        addColumnCell(rowPanel, createTableCellWrapper(createCenteredBodyLabel(row.getUsername(), false), SwingConstants.CENTER), 1, SwingConstants.CENTER);
        addColumnCell(rowPanel, createTableCellWrapper(createCenteredBodyLabel(row.getDisplayName(), false), SwingConstants.CENTER), 2, SwingConstants.CENTER);
        addColumnCell(rowPanel, createTableCellWrapper(createCenteredBodyLabel(row.getPhone(), false), SwingConstants.CENTER), 3, SwingConstants.CENTER);
        addColumnCell(rowPanel, createTableCellWrapper(createCenteredBodyLabel(displayStatus(row), false), SwingConstants.CENTER), 4, SwingConstants.CENTER);
        addColumnCell(rowPanel, createTableCellWrapper(createCenteredBodyLabel(displayRole(row), true), SwingConstants.CENTER), 5, SwingConstants.CENTER);
        addColumnCell(rowPanel, createTableCellWrapper(createActionButtons(row), SwingConstants.CENTER), 6, SwingConstants.CENTER);
        addColumnCell(rowPanel, createTableCellWrapper(createCenteredBodyLabel(formatDate(row.getCreatedAt()), false), SwingConstants.CENTER), 7, SwingConstants.CENTER);
        return rowPanel;
    }

    private JPanel createActionButtons(AccountRow row) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setOpaque(false);

        boolean isDeleted = row.isDeleted();
        JButton toggleButton = isDeleted
                ? createMiniActionButton("Khôi phục", CREATE_BG, CREATE_TEXT)
                : createMiniActionButton("Xóa", SOFT_RED_BG, SOFT_RED_TEXT);
        Dimension toggleSize = new Dimension(isDeleted ? 90 : 76, 28);
        toggleButton.setPreferredSize(toggleSize);
        toggleButton.setMinimumSize(toggleSize);
        toggleButton.setMaximumSize(toggleSize);
        toggleButton.addActionListener(event -> {
            if (row.isDeleted()) {
                handleRestore(row);
            } else {
                handleDelete(row);
            }
        });
        toggleButton.setEnabled(isDeleted ? canRestore() : canDelete());

        panel.add(toggleButton);
        panel.add(Box.createHorizontalStrut(6));

        JButton editButton = createMiniActionButton("Chỉnh sửa", EDIT_BG, EDIT_TEXT);
        Dimension editSize = new Dimension(86, 28);
        editButton.setPreferredSize(editSize);
        editButton.setMinimumSize(editSize);
        editButton.setMaximumSize(editSize);
        editButton.addActionListener(event -> showEditView(row));
        editButton.setEnabled(canEdit());

        panel.add(editButton);
        return panel;
    }

    private void showCreateView() {
        AccountUpsertRequest request = AccountCreatePanel.show(this, roleGroups);
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
            detailPanel.bindAccount(result.request().getAccountId(), result.request().getDisplayName(), result.request().getPhone(),
                    result.request().getEmail(), result.request().getUsername(), result.request().getStatus(), row.getCreatedAt(),
                    findRoleName(result.request().getRoleGroupId()), false);
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

    private void showDetailView(AccountRow row) {
        detailPanel.bindAccount(
                row.getAccountId(),
                row.getDisplayName(),
                row.getPhone(),
                row.getEmail(),
                row.getUsername(),
                row.getStatus(),
                row.getCreatedAt(),
                displayRole(row),
                row.isDeleted()
        );
        detailPanel.setCurrentRow(row);
        contentCardLayout.show(contentPanel, DETAIL_CARD);
    }

    private void showListView() {
        contentCardLayout.show(contentPanel, LIST_CARD);
        loadAccounts(txtSearch.getText());
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
        messageLabel.setFont(AppFonts.lexendRegular(14f));
        messageLabel.setForeground(new Color(107, 114, 128));
        rowPanel.add(messageLabel, BorderLayout.CENTER);
        return rowPanel;
    }

    private JLabel createBodyLabel(String text, boolean bold) {
        JLabel label = new JLabel(text == null || text.isBlank() ? "--" : text);
        label.setFont(bold ? AppFonts.lexendBold(13f) : AppFonts.lexendRegular(13f));
        label.setForeground(BODY_TEXT);
        return label;
    }

    private JLabel createCenteredBodyLabel(String text, boolean bold) {
        JLabel label = createBodyLabel(text, bold);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
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

    private String formatDate(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATE_FORMATTER);
    }

    private String displayStatus(AccountRow row) {
        return row.isDeleted() ? "DELETED" : row.getStatus();
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
        button.setFont(isBold ? AppFonts.lexendBold(13f) : AppFonts.lexendRegular(13f));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 22, 10, 22));
        return button;
    }

    private JButton createMiniActionButton(String text, Color background, Color foreground) {
        JButton button = createPillButton(text, background, foreground, true);
        button.setFont(AppFonts.lexendBold(11f));
        button.setBorder(new EmptyBorder(6, 10, 6, 10));
        return button;
    }

    private boolean canAdd() {
        return session.hasPermission("ACCOUNT_MANAGEMENT", PermissionAction.ADD);
    }

    private boolean canEdit() {
        return session.hasPermission("ACCOUNT_MANAGEMENT", PermissionAction.EDIT);
    }

    private boolean canDelete() {
        return session.hasPermission("ACCOUNT_MANAGEMENT", PermissionAction.DELETE);
    }

    private boolean canRestore() {
        return session.hasPermission("ACCOUNT_MANAGEMENT", PermissionAction.EDIT);
    }
}
