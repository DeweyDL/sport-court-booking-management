package com.sportcourt.modules.court.view;

import com.sportcourt.common.style.AppFonts;
import com.sportcourt.modules.court.dto.CourtSearchCriteria;
import com.sportcourt.modules.court.dto.CourtTableRow;
import com.sportcourt.modules.court.service.CourtService;
import com.sportcourt.modules.court.service.CourtServiceImpl;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CourtManagementPanel extends JPanel {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int PAGE_HORIZONTAL_PADDING = 40;
    private static final int CARD_HORIZONTAL_PADDING = 24;

    private static final Color PAGE_BACKGROUND = new Color(245, 247, 250);
    private static final Color CARD_BACKGROUND = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(229, 231, 235);
    private static final Color HEADER_BACKGROUND = new Color(248, 249, 250);
    private static final Color MUTED_TEXT = new Color(107, 114, 128);
    private static final Color DARK_TEXT = new Color(26, 26, 26);
    private static final Color GREEN_TEXT = new Color(16, 110, 0);
    private static final Color GREEN_BG = new Color(228, 250, 226);
    private static final Color RED_TEXT = new Color(185, 28, 28);
    private static final Color RED_BG = new Color(254, 226, 226);
    private static final Color BLUE_TEXT = new Color(37, 99, 235);
    private static final Color ALTERNATE_ROW_BACKGROUND = new Color(251, 254, 247);
    private static final Color ACTIVE_TEXT = new Color(228, 250, 226);
    private static final Color ACTIVE_BG = new Color(16, 110, 0);

    private final CourtService courtService = new CourtServiceImpl();

    private final JPanel tablePanel = new JPanel();
    private final JLabel infoLabel = new JLabel("Đang tải dữ liệu...");
    private final JTextField searchField = new JTextField(30);
    private final JPanel searchWrapper = new JPanel(new BorderLayout());
    private final Timer searchDebounceTimer;

    private static final String LIST_CARD = "LIST";
    private static final String DETAIL_CARD = "DETAIL";

    private final CardLayout contentCardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(contentCardLayout);
    private final CourtDetailPanel courtDetailPanel =
            new CourtDetailPanel(courtService, this::showListView, this::showEditView);
    private String currentBranchId = "CN_TEST_01";

    public CourtManagementPanel() {
        AppFonts.register();

        setLayout(new BorderLayout());
        setBackground(PAGE_BACKGROUND);
        setBorder(new EmptyBorder(40, PAGE_HORIZONTAL_PADDING, 32, PAGE_HORIZONTAL_PADDING));

        searchDebounceTimer = new Timer(300, event -> loadCourtData(searchField.getText()));
        searchDebounceTimer.setRepeats(false);

        contentPanel.setOpaque(false);
        contentPanel.add(createListPage(), LIST_CARD);
        contentPanel.add(courtDetailPanel, DETAIL_CARD);

        add(contentPanel, BorderLayout.CENTER);

        loadCourtData(null);
    }

    public void setCurrentBranchId(String currentBranchId) {
        this.currentBranchId = currentBranchId;
        loadCourtData(searchField.getText());
    }

    public void refreshData() {
        loadCourtData(searchField.getText());
    }

    private JPanel createListPage() {
        JPanel page = new JPanel(new BorderLayout(0, 20));
        page.setOpaque(false);
        page.add(createHeaderSection(), BorderLayout.NORTH);
        page.add(createMainContentSection(), BorderLayout.CENTER);
        return page;
    }

    private JPanel createHeaderSection() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, CARD_HORIZONTAL_PADDING, 0, CARD_HORIZONTAL_PADDING));

        JLabel titleLabel = new JLabel("QUẢN LÝ SÂN CON");
        titleLabel.setFont(new Font("Lexend", Font.BOLD, 30));
        titleLabel.setForeground(DARK_TEXT);
        titleLabel.setBorder(new EmptyBorder(0, 0, 0, 0));

        JLabel subtitleLabel = new JLabel("Hiển thị dữ liệu sân con trực thuộc chi nhánh và hỗ trợ tìm kiếm.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(MUTED_TEXT);
        subtitleLabel.setBorder(new EmptyBorder(5, 0, 20, 0));

        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setPreferredSize(new Dimension(300, 45));
        searchField.putClientProperty("JTextField.placeholderText", "Tìm theo mã sân, mã khu vực, loại thể thao...");
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
            protected void paintComponent(Graphics graphics) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50);
                g2.dispose();
            }

            @Override
            protected void paintChildren(Graphics graphics) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Shape shape = new java.awt.geom.RoundRectangle2D.Float(
                        0,
                        0,
                        getWidth(),
                        getHeight(),
                        20,
                        20
                );

                g2.setClip(shape);
                super.paintChildren(g2);
                g2.dispose();
            }
        };

        container.setOpaque(false);
        container.setBackground(CARD_BACKGROUND);
        container.setBorder(new EmptyBorder(20, CARD_HORIZONTAL_PADDING, 20, CARD_HORIZONTAL_PADDING));

        JPanel toolbar = createToolbar();
        container.add(toolbar, BorderLayout.NORTH);

        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
        tablePanel.setBackground(CARD_BACKGROUND);
        container.add(tablePanel, BorderLayout.CENTER);

        JPanel footer = createFooter();
        container.add(footer, BorderLayout.SOUTH);

        return container;
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(CARD_BACKGROUND);
        toolbar.setBorder(new EmptyBorder(10, 0, 20, 0));

        JPanel leftToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftToolbar.setBackground(CARD_BACKGROUND);

        JLabel tableTitle = new JLabel("DANH SÁCH SÂN CON");
        tableTitle.setFont(new Font("Lexend", Font.BOLD, 22));
        tableTitle.setForeground(DARK_TEXT);

        JButton addButton = createPillButton(
                "+ Thêm sân con",
                new Color(228, 250, 226),
                new Color(16, 110, 0),
                true
        );

        addButton.setFont(new Font("Lexend", Font.BOLD, 17));
        addButton.addActionListener(event -> showCreateView());

        leftToolbar.add(tableTitle);
        leftToolbar.add(addButton);
        toolbar.add(leftToolbar, BorderLayout.WEST);

        JPanel rightToolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightToolbar.setBackground(CARD_BACKGROUND);
        rightToolbar.add(createSearchFieldWithIcon());

        toolbar.add(rightToolbar, BorderLayout.EAST);

        return toolbar;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(CARD_BACKGROUND);
        footer.setBorder(new EmptyBorder(20, 0, 0, 0));

        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoLabel.setForeground(MUTED_TEXT);

        footer.add(infoLabel, BorderLayout.WEST);

        return footer;
    }

    private JPanel createSearchFieldWithIcon() {
        searchWrapper.removeAll();
        searchWrapper.setOpaque(false);
        searchWrapper.setPreferredSize(new Dimension(420, 45));

        JLabel iconLabel = new JLabel();
        iconLabel.setIcon(loadSearchIcon());
        iconLabel.setBorder(new EmptyBorder(0, 0, 0, 8));

        JPanel innerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 28, 28);

                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 28, 28);

                g2.dispose();
            }
        };

        innerPanel.setOpaque(false);
        innerPanel.setPreferredSize(new Dimension(420, 45));
        innerPanel.setBorder(new EmptyBorder(0, 12, 0, 12));

        innerPanel.add(iconLabel, BorderLayout.WEST);
        innerPanel.add(searchField, BorderLayout.CENTER);

        searchWrapper.add(innerPanel, BorderLayout.CENTER);

        return searchWrapper;
    }

    private Icon loadSearchIcon() {
        URL iconUrl = getClass().getResource("/icon/search.png");

        if (iconUrl == null) {
            return UIManager.getIcon("FileView.fileIcon");
        }

        Image image = new ImageIcon(iconUrl)
                .getImage()
                .getScaledInstance(18, 18, Image.SCALE_SMOOTH);

        return new ImageIcon(image);
    }


    private void bindSearchListener() {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                restartSearchTimer();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                restartSearchTimer();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                restartSearchTimer();
            }
        });
    }

    private void restartSearchTimer() {
        searchDebounceTimer.restart();
    }

    private void loadCourtData(String keyword) {
        infoLabel.setText("Đang tải dữ liệu...");

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
                return courtService.search(criteria);
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
        tablePanel.removeAll();
        tablePanel.add(createTableHeader());

        if (courts.isEmpty()) {
            tablePanel.add(createMessageRow("Không tìm thấy sân con phù hợp."));
        } else {
            int rowIndex = 0;

            for (CourtTableRow court : courts) {
                tablePanel.add(createDataRow(court, rowIndex++));
            }
        }

        infoLabel.setText("Hiển thị " + courts.size() + " sân con");

        tablePanel.revalidate();
        tablePanel.repaint();
    }

    private void renderErrorState(Exception exception) {
        tablePanel.removeAll();
        tablePanel.add(createTableHeader());
        tablePanel.add(createMessageRow("Không thể tải dữ liệu từ database."));

        infoLabel.setText("Lỗi tải dữ liệu");

        tablePanel.revalidate();
        tablePanel.repaint();

        JOptionPane.showMessageDialog(
                this,
                exception.getCause() == null ? exception.getMessage() : exception.getCause().getMessage(),
                "Lỗi dữ liệu sân con",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private JPanel createTableHeader() {
        JPanel header = new JPanel(new GridLayout(1, 6, 10, 0));
        header.setBackground(HEADER_BACKGROUND);
        header.setBorder(new MatteBorder(1, 0, 1, 0, BORDER_COLOR));
        header.setPreferredSize(new Dimension(0, 45));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        header.add(createHeaderCell("MÃ SÂN"));
        header.add(createHeaderCell("MÃ KHU VỰC"));
        header.add(createHeaderCell("LOẠI THỂ THAO"));
        header.add(createHeaderCell("TRẠNG THÁI"));
        header.add(createHeaderCell("NGÀY TẠO"));
        header.add(createHeaderCell("THAO TÁC"));

        return header;
    }

    private JPanel createHeaderCell(String text) {
        return createAlignedCellPanel(createHeaderLabel(text), 20, HEADER_BACKGROUND);
    }

    private JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 15));
        label.setForeground(MUTED_TEXT);
        return label;
    }

    private JPanel createDataRow(CourtTableRow court, int rowIndex) {
        Color rowBackground = rowIndex % 2 == 0 ? Color.WHITE : ALTERNATE_ROW_BACKGROUND;

        JPanel row = new JPanel(new GridLayout(1, 6, 10, 0));
        row.setBackground(rowBackground);
        row.setBorder(new MatteBorder(0, 0, 1, 0, new Color(243, 244, 246)));
        row.setPreferredSize(new Dimension(0, 68));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));

        JLabel courtIdLabel = new JLabel(court.getCourtId());
        courtIdLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        courtIdLabel.setForeground(GREEN_TEXT);

        row.add(createAlignedCellPanel(courtIdLabel, 25, rowBackground));

        row.add(createAlignedCellPanel(
                createCellLabel(court.getAreaId(), BLUE_TEXT),
                20,
                rowBackground
        ));

        row.add(createAlignedCellPanel(
                createCellLabel(court.getSportTypeName(), new Color(75, 85, 99)),
                20,
                rowBackground
        ));

        row.add(createAlignedCellPanel(
                createStatusBadge(court.getStatus(), rowBackground),
                20,
                rowBackground
        ));

        row.add(createAlignedCellPanel(
                createCellLabel(formatDate(court.getCreatedAt()), new Color(75, 85, 99)),
                15,
                rowBackground
        ));

        JPanel actionContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 12));
        actionContainer.setOpaque(false);

        JButton deleteButton = createPillButton(
                "Xóa",
                RED_BG,
                RED_TEXT,
                true
        );

        deleteButton.addActionListener(event -> confirmDelete(court));

        JButton detailButton = createPillButton(
                "Chỉnh sửa",
                new Color(243, 244, 246),
                new Color(31, 41, 55),
                false
        );

        detailButton.addActionListener(event -> showDetailView(court));

        actionContainer.add(deleteButton);
        actionContainer.add(detailButton);

        row.add(createAlignedCellPanel(actionContainer, 5, rowBackground));

        row.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent event) {
                row.setBackground(new Color(249, 250, 251));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent event) {
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
        label.setForeground(MUTED_TEXT);
        label.setBorder(new EmptyBorder(0, 20, 0, 0));

        row.add(label, BorderLayout.CENTER);

        return row;
    }

    private JLabel createCellLabel(String text, Color foreground) {
        JLabel label = new JLabel(text == null || text.isBlank() ? "--" : text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        label.setForeground(foreground);
        return label;
    }

    private JPanel createAlignedCellPanel(Component component, int leftPadding, Color background) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(background);
        panel.setOpaque(true);
        panel.setBorder(new EmptyBorder(0, leftPadding, 0, 0));
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    private Component createStatusBadge(String status, Color rowBackground) {
        String text;
        Color background;
        Color foreground;

        if ("ĐANG HOẠT ĐỘNG".equals(status)) {
            text = "• ĐANG HOẠT ĐỘNG";
            background = ACTIVE_TEXT;
            foreground = ACTIVE_BG;
        } else if ("BẢO TRÌ".equals(status)) {
            text = "• BẢO TRÌ";
            background = RED_BG;
            foreground = RED_TEXT;
        } else {
            text = status == null || status.isBlank() ? "--" : status;
            background = new Color(243, 244, 246);
            foreground = new Color(75, 85, 99);
        }

        JButton badge = createPillButton(text, background, foreground, true);
        badge.setFocusable(false);
        badge.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        badge.setRolloverEnabled(false);

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 14));
        wrapper.setBackground(rowBackground);
        wrapper.setOpaque(true);
        wrapper.add(badge);

        return wrapper;
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
        button.setFont(new Font("Segoe UI", isBold ? Font.BOLD : Font.PLAIN, 14));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(5, 12, 5, 12));

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
            courtService.delete(court.getCourtId(), currentBranchId);
            loadCourtData(searchField.getText());

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
        JOptionPane.showMessageDialog(
                this,
                "Màn thêm sân con sẽ làm ở bước tiếp theo.",
                "Thêm sân con",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showListView() {
        contentCardLayout.show(contentPanel, LIST_CARD);
        loadCourtData(searchField.getText());
    }

    private void showEditView(CourtTableRow court) {
        JOptionPane.showMessageDialog(
                this,
                "Màn sửa sân con sẽ làm ở bước tiếp theo: " + court.getCourtId(),
                "Sửa sân con",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}
