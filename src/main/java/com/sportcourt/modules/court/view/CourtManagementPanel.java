package com.sportcourt.modules.court.view;

import com.sportcourt.common.style.AppFonts;
import com.sportcourt.modules.court.controller.CourtManagementController;
import com.sportcourt.modules.court.dto.CourtSearchCriteria;
import com.sportcourt.modules.court.dto.CourtTableRow;
import com.sportcourt.modules.court.entity.Court;

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
import java.util.List;

public class CourtManagementPanel extends JPanel {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int TABLE_COLUMN_GAP = 0;
    private static final int[] TABLE_COLUMN_WIDTHS = {140, 150, 180, 200, 170, 200};

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
    private static final Color SOFT_GREEN_BG = new Color(216, 255, 208);
    private static final Color SOFT_GREEN_TEXT = new Color(44, 154, 16);
    private static final Color SOFT_RED_BG = new Color(254, 226, 226);
    private static final Color SOFT_RED_TEXT = new Color(185, 28, 28);
    private static final Color CREATE_BG = new Color(220, 252, 231);
    private static final Color CREATE_TEXT = new Color(22, 101, 52);
    private static final Color EDIT_BG = new Color(239, 246, 255);
    private static final Color EDIT_TEXT = new Color(29, 78, 216);
    private static final Color INPUT_BORDER = new Color(229, 231, 235);

    private final CourtManagementController courtController = new CourtManagementController();

    private final JPanel tableBodyPanel = new JPanel();
    private final JLabel footerLabel = new JLabel("Đang hiển thị 0 / 0 sân con");
    private final JTextField txtSearch = new JTextField();
    private final Timer searchDebounceTimer;

    private static final String LIST_CARD = "LIST";
    private static final String DETAIL_CARD = "DETAIL";

    private final CardLayout contentCardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(contentCardLayout);
    private final CourtDetailPanel courtDetailPanel =
            new CourtDetailPanel(courtController, this::showListView, this::showEditView);

    private String currentBranchId = "CN_TEST_01";

    public CourtManagementPanel() {
        AppFonts.register();

        setLayout(new BorderLayout(0, 18));
        setBackground(PAGE_BACKGROUND);
        setBorder(new EmptyBorder(40, 70, 40, 70));

        searchDebounceTimer = new Timer(300, event -> loadCourtData(txtSearch.getText()));
        searchDebounceTimer.setRepeats(false);

        contentPanel.setOpaque(false);
        contentPanel.add(createListPage(), LIST_CARD);
        contentPanel.add(courtDetailPanel, DETAIL_CARD);

        add(contentPanel, BorderLayout.CENTER);

        bindSearchListener();
        loadCourtData(null);
    }

    public void setCurrentBranchId(String currentBranchId) {
        this.currentBranchId = currentBranchId;
        loadCourtData(txtSearch.getText());
    }

    public void refreshData() {
        loadCourtData(txtSearch.getText());
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

        JLabel titleLabel = new JLabel("QUẢN LÝ SÂN CON");
        titleLabel.setFont(AppFonts.lexendBold(30f));
        titleLabel.setForeground(TITLE_TEXT);
        titleRow.add(titleLabel, BorderLayout.WEST);

        JPanel subtitleRow = new JPanel(new BorderLayout());
        subtitleRow.setOpaque(false);
        subtitleRow.setBorder(new EmptyBorder(5, 20, 20, 0));
        JLabel subtitle = new JLabel("Hiển thị dữ liệu sân con trực thuộc chi nhánh và hỗ trợ tìm kiếm nhanh.");
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

        JLabel title = new JLabel("Danh sách sân con");
        title.setFont(AppFonts.lexendBold(18f));
        title.setForeground(new Color(35, 37, 43));

        JButton createButton = createPillButton("+ Thêm sân con", CREATE_BG, CREATE_TEXT, true);
        createButton.setPreferredSize(new Dimension(160, 36));
        createButton.setBorder(new EmptyBorder(7, 14, 7, 14));
        createButton.setFont(AppFonts.lexendBold(12f));
        createButton.addActionListener(event -> showCreateView());

        JPanel titleWithActionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleWithActionRow.setOpaque(false);
        titleWithActionRow.add(title);
        titleWithActionRow.add(createButton);

        JPanel searchWrapper = createSearchFieldWithIcon();

        titlePanel.add(titleWithActionRow, BorderLayout.WEST);
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
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm theo mã sân, mã khu vực, loại thể thao...");
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

    private Icon loadSearchIcon() {
        URL iconUrl = getClass().getResource("/icon/search.png");
        if (iconUrl == null) {
            return UIManager.getIcon("FileView.fileIcon");
        }
        Image image = new ImageIcon(iconUrl).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
        return new ImageIcon(image);
    }

    private JPanel createTableHeader() {
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setBackground(HEADER_BACKGROUND);
        headerPanel.setBorder(new EmptyBorder(14, 24, 14, 24));
        addColumnCell(headerPanel, createHeaderLabel("MÃ SÂN", SwingConstants.LEFT), 0, SwingConstants.LEFT);
        addColumnCell(headerPanel, createHeaderLabel("MÃ KHU VỰC", SwingConstants.CENTER), 1, SwingConstants.CENTER);
        addColumnCell(headerPanel, createHeaderLabel("LOẠI THỂ THAO", SwingConstants.CENTER), 2, SwingConstants.CENTER);
        addColumnCell(headerPanel, createHeaderLabel("TRẠNG THÁI", SwingConstants.CENTER), 3, SwingConstants.CENTER);
        addColumnCell(headerPanel, createHeaderLabel("NGÀY TẠO", SwingConstants.CENTER), 4, SwingConstants.CENTER);
        addColumnCell(headerPanel, createHeaderLabel("THAO TÁC", SwingConstants.CENTER), 5, SwingConstants.CENTER);
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

    private void loadCourtData(String keyword) {
        footerLabel.setText("Đang tải dữ liệu...");

        CourtSearchCriteria criteria = new CourtSearchCriteria();
        criteria.setBranchId(currentBranchId);
        criteria.setKeyword(keyword == null ? "" : keyword.trim());
        criteria.setAreaId("");
        criteria.setStatus("");
        criteria.setSortBy("courtId");
        criteria.setSortDirection("ASC");

        SwingWorker<List<CourtTableRow>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<CourtTableRow> doInBackground() throws Exception {
                return courtController.search(criteria);
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

    private void renderTableData(List<CourtTableRow> courts) {
        tableBodyPanel.removeAll();

        if (courts.isEmpty()) {
            tableBodyPanel.add(createEmptyRow());
        } else {
            for (CourtTableRow court : courts) {
                tableBodyPanel.add(createDataRow(court));
            }
        }

        footerLabel.setText("Đang hiển thị " + courts.size() + " / " + courts.size() + " sân con");

        tableBodyPanel.revalidate();
        tableBodyPanel.repaint();
    }

    private void renderErrorState(Exception exception) {
        tableBodyPanel.removeAll();
        tableBodyPanel.add(createEmptyRow("Không thể tải dữ liệu từ database."));
        footerLabel.setText("Lỗi tải dữ liệu");

        tableBodyPanel.revalidate();
        tableBodyPanel.repaint();

        JOptionPane.showMessageDialog(
                this,
                exception.getCause() == null ? exception.getMessage() : exception.getCause().getMessage(),
                "Lỗi dữ liệu sân con",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private JPanel createDataRow(CourtTableRow court) {
        JPanel rowPanel = new JPanel(new GridBagLayout());
        rowPanel.setBackground(Color.WHITE);
        rowPanel.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, ROW_BORDER),
                new EmptyBorder(14, 24, 14, 24)
        ));
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel courtIdLabel = createBodyLabel(court.getCourtId(), true);
        courtIdLabel.setForeground(BLUE_TEXT);
        addColumnCell(rowPanel, createTableCellWrapper(courtIdLabel, SwingConstants.LEFT), 0, SwingConstants.LEFT);
        addColumnCell(rowPanel, createTableCellWrapper(createCenteredBodyLabel(court.getAreaId(), false), SwingConstants.CENTER), 1, SwingConstants.CENTER);
        addColumnCell(rowPanel, createTableCellWrapper(createCenteredBodyLabel(court.getSportTypeName(), false), SwingConstants.CENTER), 2, SwingConstants.CENTER);
        addColumnCell(rowPanel, createTableCellWrapper(createStatusPill(court.getStatus()), SwingConstants.CENTER), 3, SwingConstants.CENTER);
        addColumnCell(rowPanel, createTableCellWrapper(createCenteredBodyLabel(formatDate(court.getCreatedAt()), false), SwingConstants.CENTER), 4, SwingConstants.CENTER);

        JPanel actionGroup = new JPanel();
        actionGroup.setLayout(new BoxLayout(actionGroup, BoxLayout.X_AXIS));
        actionGroup.setOpaque(false);

        JButton btnDelete = createMiniActionButton("Xóa", SOFT_RED_BG, SOFT_RED_TEXT);
        Dimension actionButtonSize = new Dimension(80, 28);
        btnDelete.setPreferredSize(actionButtonSize);
        btnDelete.setMinimumSize(actionButtonSize);
        btnDelete.setMaximumSize(actionButtonSize);
        btnDelete.addActionListener(event -> confirmDelete(court));
        actionGroup.add(btnDelete);
        actionGroup.add(Box.createHorizontalStrut(6));

        JButton btnEdit = createMiniActionButton("Chỉnh sửa", EDIT_BG, EDIT_TEXT);
        Dimension editSize = new Dimension(86, 28);
        btnEdit.setPreferredSize(editSize);
        btnEdit.setMinimumSize(editSize);
        btnEdit.setMaximumSize(editSize);
        btnEdit.addActionListener(event -> showDetailView(court));
        actionGroup.add(btnEdit);

        addColumnCell(rowPanel, createTableCellWrapper(actionGroup, SwingConstants.CENTER), 5, SwingConstants.CENTER);
        return rowPanel;
    }

    private JPanel createEmptyRow() {
        return createEmptyRow("Không tìm thấy sân con phù hợp.");
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

    private JPanel createStatusPill(String status) {
        boolean active = "ĐANG HOẠT ĐỘNG".equalsIgnoreCase(status);
        boolean maintenance = "BẢO TRÌ".equalsIgnoreCase(status);

        Color background;
        Color foreground;
        String displayText;

        if (active) {
            background = SOFT_GREEN_BG;
            foreground = SOFT_GREEN_TEXT;
            displayText = "ĐANG HOẠT ĐỘNG";
        } else if (maintenance) {
            background = SOFT_RED_BG;
            foreground = SOFT_RED_TEXT;
            displayText = "BẢO TRÌ";
        } else {
            background = new Color(229, 231, 235);
            foreground = new Color(75, 85, 99);
            displayText = status == null || status.isBlank() ? "--" : status;
        }

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

        JLabel textLabel = new JLabel(displayText);
        textLabel.setFont(AppFonts.lexendBold(13f));
        textLabel.setForeground(foreground);

        wrapper.add(dotLabel);
        wrapper.add(textLabel);
        pill.add(wrapper);
        return pill;
    }

    private String formatDate(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATE_FORMATTER);
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

    private void confirmDelete(CourtTableRow court) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn chắc chắn muốn xóa sân con " + court.getCourtId() + " không?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            courtController.delete(court.getCourtId(), currentBranchId);
            loadCourtData(txtSearch.getText());

            JOptionPane.showMessageDialog(
                    this,
                    "Đã xóa sân con " + court.getCourtId() + ".",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getCause() == null ? exception.getMessage() : exception.getCause().getMessage(),
                    "Lỗi xóa sân con",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void showDetailView(CourtTableRow court) {
        courtDetailPanel.showDetail(court.getCourtId(), currentBranchId);
        contentCardLayout.show(contentPanel, DETAIL_CARD);
    }

    private void showCreateView() {
        try {
            List<String> areaIds = courtController.getAreaIdsByBranch(currentBranchId);
            if (areaIds.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Chi nhánh hiện tại chưa có khu vực khả dụng để thêm sân con.",
                        "Thêm sân con",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            Court court = CourtCreatePanel.show(this, areaIds);
            if (court == null) {
                return;
            }

            courtController.create(court, currentBranchId);
            loadCourtData(txtSearch.getText());
            JOptionPane.showMessageDialog(
                    this,
                    "Đã thêm sân con " + court.getCourtId() + ".",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getCause() == null ? exception.getMessage() : exception.getCause().getMessage(),
                    "Lỗi thêm sân con",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void showListView() {
        contentCardLayout.show(contentPanel, LIST_CARD);
        loadCourtData(txtSearch.getText());
    }

    private void showEditView(CourtTableRow court) {
        try {
            List<String> areaIds = courtController.getAreaIdsByBranch(currentBranchId);
            Court updatePayload = CourtEditPanel.show(this, court, areaIds);
            if (updatePayload == null) {
                return;
            }

            courtController.update(updatePayload, currentBranchId);
            courtDetailPanel.showDetail(court.getCourtId(), currentBranchId);
            loadCourtData(txtSearch.getText());
            JOptionPane.showMessageDialog(
                    this,
                    "Đã cập nhật sân con " + court.getCourtId() + ".",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getCause() == null ? exception.getMessage() : exception.getCause().getMessage(),
                    "Lỗi cập nhật sân con",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
