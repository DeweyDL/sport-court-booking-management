package com.sportcourt.modules.cost.view;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class CostManagement extends JPanel implements Scrollable {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Color ALTERNATE_ROW_BACKGROUND = new Color(251, 254, 247);

    private final com.sportcourt.modules.cost.controller.CostController controller = new com.sportcourt.modules.cost.controller.CostController();
    private final JPanel tablePanel = new JPanel();
    private final JLabel infoLabel = new JLabel("Đang tải dữ liệu...");
    private final JTextField searchField = new JTextField(30);
    private final JPanel searchWrapper = new JPanel(new BorderLayout());
    private final Timer searchDebounceTimer;

    private final CostChange suaBangGia = new CostChange(controller, id -> loadBangGiaData(searchField.getText()));
    private final CostAdd themBangGia = new CostAdd(controller, id -> loadBangGiaData(searchField.getText()));

    public CostManagement() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(100, 70, 50, 70));

        searchDebounceTimer = new Timer(300, event -> loadBangGiaData(searchField.getText()));
        searchDebounceTimer.setRepeats(false);

        add(createListPage(), BorderLayout.CENTER);
        loadBangGiaData(null);
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

        JLabel titleLabel = new JLabel("QUẢN LÝ BẢNG GIÁ");
        titleLabel.setFont(new Font("Lexend", Font.BOLD, 30));
        titleLabel.setForeground(new Color(26, 26, 26));
        titleLabel.setBorder(new EmptyBorder(0, 20, 0, 0));

        JLabel subtitleLabel = new JLabel("Hiển thị dữ liệu bảng giá theo khung giờ và hỗ trợ tìm kiếm.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(107, 114, 128));
        subtitleLabel.setBorder(new EmptyBorder(5, 20, 20, 0));

        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setPreferredSize(new Dimension(300, 45));
        searchField.putClientProperty("JTextField.placeholderText", "Tìm theo MABG hoặc MAKV...");
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
                Shape shape = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20);
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

        JLabel tableTitle = new JLabel("DANH SÁCH BẢNG GIÁ");
        tableTitle.setFont(new Font("Lexend", Font.BOLD, 22));

        JButton addButton = createPillButton("+ Thêm bảng giá", new Color(228, 250, 226), new Color(16, 110, 0), true);
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
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        container.add(scrollPane, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(20, 30, 20, 30));
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
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

    private void loadBangGiaData(String keyword) {
        infoLabel.setText("Đang tải dữ liệu...");
        tablePanel.removeAll();
        tablePanel.add(createTableHeader());


        try {
            List<com.sportcourt.modules.cost.entity.Cost> costs = controller.searchCosts(keyword);
            if (costs.isEmpty()) {
                tablePanel.add(createMessageRow("Không có dữ liệu phù hợp."));
                infoLabel.setText("Tổng cộng: 0 dòng");
            } else {
                int idx = 0;
                for (com.sportcourt.modules.cost.entity.Cost cost : costs) {
                    tablePanel.add(createDataRow(cost, idx++));
                }
                infoLabel.setText("Tổng cộng: " + costs.size() + " dòng");
            }
        } catch (Exception e) {
            e.printStackTrace();
            tablePanel.add(createMessageRow("Lỗi tải dữ liệu: " + e.getMessage()));
            infoLabel.setText("Lỗi kết nối");
        }
        tablePanel.revalidate();
        tablePanel.repaint();
    }

    private JPanel createTableHeader() {
        JPanel header = new JPanel(new GridBagLayout());
        header.setBackground(new Color(248, 249, 250));
        header.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 1, 0, new Color(229, 231, 235)),
                new EmptyBorder(0, 24, 0, 24)
        ));
        header.setPreferredSize(new Dimension(0, 50));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        gbc.weightx = 0.12; header.add(createHeaderCell("MÃ KHU VỰC", SwingConstants.LEFT), gbc);
        gbc.weightx = 0.14; header.add(createHeaderCell("MÃ BẢNG GIÁ", SwingConstants.LEFT), gbc);
        gbc.weightx = 0.14; header.add(createHeaderCell("GIỜ BẮT ĐẦU", SwingConstants.CENTER), gbc);
        gbc.weightx = 0.14; header.add(createHeaderCell("GIỜ KẾT THÚC", SwingConstants.CENTER), gbc);
        gbc.weightx = 0.16; header.add(createHeaderCell("GIÁ", SwingConstants.CENTER), gbc);
        gbc.weightx = 0.30; header.add(createHeaderCell("THAO TÁC", SwingConstants.CENTER), gbc);

        return header;
    }

    private JPanel createHeaderCell(String text, int alignment) {
        return createFlexibleCell(createHeaderLabel(text), alignment, new Color(248, 249, 250), 0, 0);
    }

    private JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 17));
        label.setForeground(new Color(107, 114, 128));
        return label;
    }

    private JPanel createDataRow(com.sportcourt.modules.cost.entity.Cost cost, int rowIndex) {
        Color rowBg = rowIndex % 2 == 0 ? Color.WHITE : ALTERNATE_ROW_BACKGROUND;
        JPanel row = new JPanel(new GridBagLayout());
        row.setBackground(rowBg);
        row.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(243, 244, 246)),
                new EmptyBorder(0, 24, 0, 24)
        ));
        row.setPreferredSize(new Dimension(0, 64));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        gbc.weightx = 0.12; row.add(createFlexibleCell(createCellLabel(cost.getMaKv(), new Color(37, 99, 235)), SwingConstants.LEFT, rowBg, 0, 0), gbc);

        JLabel maBgLabel = new JLabel(cost.getMaBg());
        maBgLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        maBgLabel.setForeground(new Color(22, 163, 74));
        gbc.weightx = 0.14; row.add(createFlexibleCell(maBgLabel, SwingConstants.LEFT, rowBg, 5, 0), gbc);

        gbc.weightx = 0.14; row.add(createFlexibleCell(createCellLabel(formatHour(cost.getGioBatDau()), new Color(17, 24, 39)), SwingConstants.CENTER, rowBg, 0, 0), gbc);
        gbc.weightx = 0.14; row.add(createFlexibleCell(createCellLabel(formatHour(cost.getGioKetThuc()), new Color(17, 24, 39)), SwingConstants.CENTER, rowBg, 0, 0), gbc);
        gbc.weightx = 0.16; row.add(createFlexibleCell(createCellLabel(formatMoney(cost.getGia()), new Color(37, 99, 235)), SwingConstants.CENTER, rowBg, 0, 0), gbc);

        JPanel actionContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        actionContainer.setOpaque(false);

        JButton deleteBtn = createMiniActionButton("Xóa", new Color(254, 226, 226), new Color(185, 28, 28));
        deleteBtn.addActionListener(event -> confirmDelete(cost));

        JButton editBtn = createMiniActionButton("Chỉnh sửa", new Color(239, 246, 255), new Color(29, 78, 216));
        editBtn.addActionListener(event -> showEditView(cost.getMaBg()));

        actionContainer.add(deleteBtn);
        actionContainer.add(editBtn);

        JPanel actionCell = new JPanel(new GridBagLayout());
        actionCell.setBackground(rowBg);
        actionCell.setOpaque(true);
        actionCell.add(actionContainer);

        gbc.weightx = 0.30; row.add(createFlexibleCell(actionCell, SwingConstants.CENTER, rowBg, 0, 0), gbc);

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

    private void confirmDelete(com.sportcourt.modules.cost.entity.Cost cost) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn chắc chắn muốn xóa bảng giá này không?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            controller.deleteCost(cost.getMaBg());
            loadBangGiaData(searchField.getText());
            JOptionPane.showMessageDialog(this, "Đã xóa bảng giá " + cost.getMaBg() + ".", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getCause() == null ? exception.getMessage() : exception.getCause().getMessage(),
                    "Lỗi xóa bảng giá",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void showEditView(String maBg) {
        suaBangGia.showEditor(this, maBg);
    }

    private void showCreateView() {
        themBangGia.showCreator(this);
    }

    private String formatDate(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATE_FORMATTER);
    }

    private String formatHour(int hour) {
        return "%02d:00".formatted(hour);
    }

    private String formatMoney(java.math.BigDecimal value) {
        if (value == null) {
            return "--";
        }
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        nf.setMaximumFractionDigits(0);
        return nf.format(value) + " VND";
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
