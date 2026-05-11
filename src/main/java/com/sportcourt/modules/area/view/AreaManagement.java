package com.sportcourt.modules.area.view;

import com.sportcourt.modules.area.controller.AreaController;
import com.sportcourt.modules.area.enitity.Area;

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
import javax.swing.Scrollable;

public class AreaManagement extends JPanel implements Scrollable {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Color ALTERNATE_ROW_BACKGROUND = new Color(251, 254, 247);

    private final AreaController areaController = new AreaController();
    private final JPanel tablePanel = new JPanel();
    private final JLabel infoLabel = new JLabel("Đang tải dữ liệu...");
    private final JTextField searchField = new JTextField(30);
    private final JPanel searchWrapper = new JPanel(new BorderLayout());
    private final Timer searchDebounceTimer;

    private final AreaChange suaKhuVuc = new AreaChange(areaController, id -> {
        loadKhuVucData(searchField.getText());
    });

    private final AreaAdd themKhuVuc = new AreaAdd(areaController, id -> {
        loadKhuVucData(searchField.getText());
    });

    public AreaManagement() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(100, 70, 50, 70));

        searchDebounceTimer = new Timer(300, event -> loadKhuVucData(searchField.getText()));
        searchDebounceTimer.setRepeats(false);

        add(createListPage(), BorderLayout.CENTER);

        loadKhuVucData(null);
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

        JLabel titleLabel = new JLabel("QUẢN LÝ KHU VỰC");
        titleLabel.setFont(new Font("Lexend", Font.BOLD, 30));
        titleLabel.setForeground(new Color(26, 26, 26));
        titleLabel.setBorder(new EmptyBorder(0, 20, 0, 0));

        JLabel subtitleLabel = new JLabel("Hiển thị dữ liệu các khu vực trực thuộc chi nhánh và hỗ trợ tìm kiếm.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(107, 114, 128));
        subtitleLabel.setBorder(new EmptyBorder(5, 20, 20, 0));

        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setPreferredSize(new Dimension(300, 45));
        searchField.putClientProperty("JTextField.placeholderText", "Tìm theo MAKV hoặc MACN...");
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
        container.setBorder(new EmptyBorder(20, 0, 20, 0));

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(new EmptyBorder(10, 20, 20, 20));

        JPanel leftToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftToolbar.setBackground(Color.WHITE);

        JLabel tableTitle = new JLabel("DANH SÁCH KHU VỰC");
        tableTitle.setFont(new Font("Lexend", Font.BOLD, 22));

        JButton addButton = createPillButton("+ Thêm khu vực", new Color(228, 250, 226), new Color(16, 110, 0), true);
        addButton.setFont(new Font("Lexend", Font.BOLD, 17));
        addButton.addActionListener(event -> showCreateView());

        leftToolbar.add(tableTitle);
        leftToolbar.add(addButton);
        toolbar.add(leftToolbar, BorderLayout.WEST);

        JPanel rightToolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightToolbar.setBackground(Color.WHITE);
        rightToolbar.add(createSearchFieldWithIcon());
        toolbar.add(rightToolbar, BorderLayout.EAST);

        container.add(toolbar, BorderLayout.NORTH);

        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
        tablePanel.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(tablePanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setColumnHeaderView(createTableHeader());

        container.add(scrollPane, BorderLayout.CENTER); // Add scroll pane instead of tablePanel directly

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(20, 20, 0, 20));
        infoLabel.setForeground(new Color(107, 114, 128));
        footer.add(infoLabel, BorderLayout.WEST);
        container.add(footer, BorderLayout.SOUTH);
        return container;
    }

    private JPanel createSearchFieldWithIcon() {
        searchWrapper.removeAll();
        searchWrapper.setOpaque(false);
        searchWrapper.setPreferredSize(new Dimension(360, 45));

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
        innerPanel.setPreferredSize(new Dimension(360, 45));
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

    private void loadKhuVucData(String keyword) {
        infoLabel.setText("Đang tải dữ liệu...");

        SwingWorker<List<Area>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Area> doInBackground() {
                return areaController.getKhuVucList(keyword);
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

    private void renderTableData(List<Area> areas) {
        tablePanel.removeAll();

        if (areas.isEmpty()) {
            tablePanel.add(createMessageRow("Không tìm thấy khu vực phù hợp."));
        } else {
            int rowIndex = 0;
            for (Area area : areas) {
                tablePanel.add(createDataRow(area, rowIndex++));
            }
        }

        infoLabel.setText("Hiển thị " + areas.size() + " khu vực");
        tablePanel.revalidate();
        tablePanel.repaint();
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
                "Lỗi dữ liệu khu vực",
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
        header.setPreferredSize(new Dimension(1000, 45));
        header.setMinimumSize(new Dimension(800, 45));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 12);

        gbc.weightx = 0.12; header.add(createFlexibleCell(createHeaderLabel("MÃ KHU VỰC"), SwingConstants.LEFT, new Color(248, 249, 250), 0, 8), gbc);
        gbc.weightx = 0.14; header.add(createFlexibleCell(createHeaderLabel("MÃ CHI NHÁNH"), SwingConstants.LEFT, new Color(248, 249, 250), 0, 8), gbc);
        gbc.weightx = 0.16; header.add(createFlexibleCell(createHeaderLabel("LOẠI THỂ THAO"), SwingConstants.LEFT, new Color(248, 249, 250), 0, 8), gbc);
        gbc.weightx = 0.14; header.add(createFlexibleCell(createHeaderLabel("SỐ LƯỢNG SÂN"), SwingConstants.CENTER, new Color(248, 249, 250), 0, 8), gbc);
        gbc.weightx = 0.14; header.add(createFlexibleCell(createHeaderLabel("NGÀY TẠO"), SwingConstants.CENTER, new Color(248, 249, 250), 0, 8), gbc);
        gbc.weightx = 0.12; header.add(createFlexibleCell(createHeaderLabel("TRẠNG THÁI"), SwingConstants.CENTER, new Color(248, 249, 250), 0, 8), gbc);
        gbc.weightx = 0.18; gbc.insets = new Insets(0, 0, 0, 0); header.add(createFlexibleCell(createHeaderLabel("THAO TÁC"), SwingConstants.CENTER, new Color(248, 249, 250), 0, 0), gbc);

        return header;
    }

    private JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 17));
        label.setForeground(new Color(107, 114, 128));
        return label;
    }

    private JPanel createDataRow(Area area, int rowIndex) {
        Color rowBg = rowIndex % 2 == 0 ? Color.WHITE : ALTERNATE_ROW_BACKGROUND;
        JPanel row = new JPanel(new GridBagLayout());
        row.setBackground(rowBg);
        row.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(243, 244, 246)),
                new EmptyBorder(0, 24, 0, 24)
        ));
        row.setPreferredSize(new Dimension(1000, 64));
        row.setMinimumSize(new Dimension(800, 64));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 12);

        JLabel maKvLabel = new JLabel(area.maKv());
        maKvLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        maKvLabel.setForeground(new Color(22, 163, 74));
        gbc.weightx = 0.12; row.add(createFlexibleCell(maKvLabel, SwingConstants.LEFT, rowBg, 0, 8), gbc);

        gbc.weightx = 0.14; row.add(createFlexibleCell(createCellLabel(area.maCn(), new Color(37, 99, 235)), SwingConstants.LEFT, rowBg, 0, 8), gbc);
        gbc.weightx = 0.16; row.add(createFlexibleCell(createCellLabel(area.tenTheThao(), new Color(75, 85, 99)), SwingConstants.LEFT, rowBg, 0, 8), gbc);
        gbc.weightx = 0.14; row.add(createFlexibleCell(createCellLabel(String.valueOf(area.soLuongSan()), new Color(17, 24, 39)), SwingConstants.CENTER, rowBg, 0, 8), gbc);
        gbc.weightx = 0.14; row.add(createFlexibleCell(createCellLabel(formatDate(area.createdAt()), new Color(75, 85, 99)), SwingConstants.CENTER, rowBg, 0, 8), gbc);

        // Status cell
        Color statusColor = area.isDeleted() ? new Color(185, 28, 28) : new Color(16, 110, 0);
        gbc.weightx = 0.12; row.add(createFlexibleCell(createCellLabel(area.getStatus(), statusColor), SwingConstants.CENTER, rowBg, 0, 8), gbc);

        JPanel actionContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        actionContainer.setOpaque(false);

        JButton deleteButton = createMiniActionButton("Xóa", new Color(254, 226, 226), new Color(185, 28, 28));
        deleteButton.addActionListener(event -> confirmDelete(area));

        JButton editButton = createMiniActionButton("Chỉnh sửa", new Color(239, 246, 255), new Color(29, 78, 216));
        editButton.addActionListener(event -> showEditView(area.maKv()));

        actionContainer.add(deleteButton);
        actionContainer.add(editButton);

        JPanel actionCell = new JPanel(new GridBagLayout());
        actionCell.setBackground(rowBg);
        actionCell.setOpaque(true);
        actionCell.add(actionContainer);

        gbc.weightx = 0.18; gbc.insets = new Insets(0, 0, 0, 0); row.add(createFlexibleCell(actionCell, SwingConstants.CENTER, rowBg, 0, 0), gbc);

        row.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                row.setBackground(new Color(249, 250, 251));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                row.setBackground(rowBg);
            }
        });
        return row;
    }

    private JPanel createMessageRow(String message) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setBorder(new EmptyBorder(24, 26, 24, 26));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));

        JLabel label = new JLabel(message);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(new Color(107, 114, 128));
        row.add(label, BorderLayout.CENTER);
        return row;
    }

    private JLabel createCellLabel(String text, Color fg) {
        JLabel label = new JLabel(text == null || text.isBlank() ? "--" : text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 15));
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

        panel.setPreferredSize(new Dimension(0, 64));
        panel.setMinimumSize(new Dimension(0, 64));
        return panel;
    }

    private void confirmDelete(Area area) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn chắc chắn muốn xóa khu vực này không?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            areaController.deleteKhuVuc(area.maKv());
            loadKhuVucData(searchField.getText());
            JOptionPane.showMessageDialog(this, "Đã xóa khu vực " + area.maKv() + ".", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalStateException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getCause() == null ? exception.getMessage() : exception.getCause().getMessage(),
                    "Lỗi xóa khu vực",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void showEditView(String maKv) {
        suaKhuVuc.showEditor(this, maKv);
    }

    private void showCreateView() {
        themKhuVuc.showCreator(this);
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
