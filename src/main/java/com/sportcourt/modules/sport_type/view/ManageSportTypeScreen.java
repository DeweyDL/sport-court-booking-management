package com.sportcourt.modules.sport_type.view;

import com.formdev.flatlaf.FlatLightLaf;
import com.sportcourt.common.style.AppDialog;
import com.sportcourt.common.style.AppFonts;
import com.sportcourt.modules.sport_type.controller.SportTypeController;
import com.sportcourt.modules.sport_type.dto.SportTypeForm;
import com.sportcourt.modules.sport_type.dto.SportTypeTableRow;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URL;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ManageSportTypeScreen extends JPanel {

    private static final Color ALTERNATE_ROW_BG = new Color(248, 250, 252);
    private static final int HEADER_HEIGHT = 42;
    private static final int ROW_HEIGHT = 52;
    private static final int COL_ID = 100;
    private static final int COL_NAME = 160;
    private static final int COL_ACTIONS = 160;

    private final SportTypeController controller = new SportTypeController();
    private final List<SportTypeTableRow> allRows = new ArrayList<>();
    private final List<SportTypeTableRow> displayedRows = new ArrayList<>();

    private final JPanel tablePanel = new JPanel();
    private final JLabel footerLabel = new JLabel("Đang tải dữ liệu...");
    private final JTextField searchField = new JTextField(30);
    private final JPanel searchWrapper = new JPanel(new BorderLayout());
    private final JComboBox<String> cbSort = new JComboBox<>(new String[]{"Tên", "Mã loại"});
    private final JButton btnSortDir = new JButton("▲");

    private SportTypeTableRow selectedRow;
    private boolean sortAscending = true;

    public ManageSportTypeScreen() {
        AppFonts.register();
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(60, 60, 40, 60));

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int w = getWidth();
                int h = getHeight();
                int hPad = Math.max(20, Math.min(70, w / 18));
                int vPad = Math.max(20, Math.min(60, h / 18));
                setBorder(new EmptyBorder(vPad, hPad, vPad / 2, hPad));
            }
        });

        add(createPage(), BorderLayout.CENTER);
        loadData();
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

        JLabel title = new JLabel("QUẢN LÝ LOẠI THỂ THAO");
        title.setFont(new Font("Lexend", Font.BOLD, 30));
        title.setForeground(new Color(30, 31, 36));
        title.setBorder(new EmptyBorder(0, 20, 0, 0));

        JLabel subtitle = new JLabel("Quản lý danh mục loại thể thao trong hệ thống.");
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
        topSection.add(createTableHeader());
        container.add(topSection, BorderLayout.NORTH);

        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
        tablePanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(tablePanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
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

        JLabel tableTitle = new JLabel("DANH SÁCH LOẠI THỂ THAO");
        tableTitle.setFont(new Font("Lexend", Font.BOLD, 22));

        JButton addBtn = createPillButton("+ Thêm loại thể thao", new Color(228, 250, 226), new Color(16, 110, 0), true);
        addBtn.setFont(new Font("Lexend", Font.BOLD, 14));
        addBtn.setBorder(new EmptyBorder(4, 10, 4, 10));
        addBtn.addActionListener(e -> openCreateDialog());
        JPanel addBtnWrapper = new JPanel(new BorderLayout());
        addBtnWrapper.setOpaque(false);
        addBtnWrapper.setBorder(new EmptyBorder(10, 0, 0, 0));
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
        searchWrapper.setPreferredSize(new Dimension(260, 41));
        searchWrapper.setMaximumSize(new Dimension(260, 41));

        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setPreferredSize(new Dimension(260, 41));
        searchField.putClientProperty("JTextField.placeholderText", "Tìm theo tên hoặc mã...");
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
        innerPanel.setPreferredSize(new Dimension(260, 41));
        innerPanel.setMaximumSize(new Dimension(260, 41));
        innerPanel.setBorder(new EmptyBorder(0, 12, 0, 12));
        innerPanel.add(iconLabel, BorderLayout.WEST);
        innerPanel.add(searchField, BorderLayout.CENTER);

        searchWrapper.add(innerPanel, BorderLayout.CENTER);
        return searchWrapper;
    }

    private JPanel createTableHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
        header.setBackground(new Color(248, 249, 250));
        header.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 1, 0, new Color(229, 231, 235)),
                new EmptyBorder(0, 24, 0, 24)
        ));
        header.setPreferredSize(new Dimension(0, HEADER_HEIGHT));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, HEADER_HEIGHT));

        Color hBg = new Color(248, 249, 250);
        header.add(createFixedCell(createHeaderLabel("MÃ LOẠI"), COL_ID, HEADER_HEIGHT, SwingConstants.CENTER, hBg, 0, 8));
        header.add(Box.createHorizontalStrut(10));
        header.add(createFixedCell(createHeaderLabel("TÊN LOẠI THỂ THAO"), COL_NAME, HEADER_HEIGHT, SwingConstants.LEFT, hBg, 0, 8));
        header.add(Box.createHorizontalStrut(10));
        header.add(createFlexCell(createHeaderLabel("MÔ TẢ"), HEADER_HEIGHT, hBg, SwingConstants.LEFT));
        header.add(Box.createHorizontalStrut(10));
        header.add(createFixedCell(createHeaderLabel("THAO TÁC"), COL_ACTIONS, HEADER_HEIGHT, SwingConstants.CENTER, hBg, 0, 8));
        return header;
    }

    private JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(new Color(107, 114, 128));
        return label;
    }

    private JPanel createDataRow(SportTypeTableRow row, int rowIndex) {
        Color rowBg = rowIndex % 2 == 0 ? Color.WHITE : ALTERNATE_ROW_BG;

        JPanel rowPanel = new JPanel();
        rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
        rowPanel.setBackground(rowBg);
        rowPanel.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(243, 244, 246)),
                new EmptyBorder(0, 24, 0, 24)
        ));
        rowPanel.setPreferredSize(new Dimension(0, ROW_HEIGHT));
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, ROW_HEIGHT));

        JLabel idLabel = new JLabel(valueOrDash(row.sportId()));
        idLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        idLabel.setForeground(new Color(22, 163, 74));
        rowPanel.add(createFixedCell(idLabel, COL_ID, ROW_HEIGHT, SwingConstants.LEFT, rowBg, 0, 8));
        rowPanel.add(Box.createHorizontalStrut(10));

        rowPanel.add(createFixedCell(createCellLabel(row.name(), new Color(17, 24, 39)), COL_NAME, ROW_HEIGHT, SwingConstants.LEFT, rowBg, 0, 8));
        rowPanel.add(Box.createHorizontalStrut(10));

        rowPanel.add(createFlexCell(createDescriptionLabel(row.description()), ROW_HEIGHT, rowBg, SwingConstants.LEFT));
        rowPanel.add(Box.createHorizontalStrut(10));

        JPanel actionGroup = new JPanel();
        actionGroup.setLayout(new BoxLayout(actionGroup, BoxLayout.X_AXIS));
        actionGroup.setOpaque(false);

        JButton editBtn = createMiniActionButton("Chỉnh sửa", new Color(239, 246, 255), new Color(29, 78, 216));
        Dimension editSize = new Dimension(76, 25);
        editBtn.setPreferredSize(editSize);
        editBtn.setMinimumSize(editSize);
        editBtn.setMaximumSize(editSize);
        editBtn.addActionListener(e -> {
            selectedRow = row;
            openEditDialog();
        });

        JButton deleteBtn = createMiniActionButton("Xóa", new Color(254, 226, 226), new Color(185, 28, 28));
        Dimension deleteSize = new Dimension(52, 25);
        deleteBtn.setPreferredSize(deleteSize);
        deleteBtn.setMinimumSize(deleteSize);
        deleteBtn.setMaximumSize(deleteSize);
        deleteBtn.addActionListener(e -> {
            selectedRow = row;
            deleteSelected();
        });

        actionGroup.add(editBtn);
        actionGroup.add(Box.createHorizontalStrut(8));
        actionGroup.add(deleteBtn);

        JPanel actionCell = new JPanel(new BorderLayout());
        actionCell.setBackground(rowBg);
        actionCell.setOpaque(true);
        actionCell.setBorder(new EmptyBorder(0, 4, 0, 4));
        actionCell.add(actionGroup, BorderLayout.CENTER);
        rowPanel.add(createFixedCell(actionCell, COL_ACTIONS, ROW_HEIGHT, SwingConstants.LEFT, rowBg, 0, 0));

        rowPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                rowPanel.setBackground(new Color(249, 250, 251));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                rowPanel.setBackground(rowBg);
            }
        });

        return rowPanel;
    }

    private JLabel createDescriptionLabel(String text) {
        if (text == null || text.isBlank()) {
            JLabel label = new JLabel("--");
            label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            label.setForeground(new Color(156, 163, 175));
            return label;
        }
        String escaped = escapeHtml(text.trim());
        JLabel label = new JLabel("<html><div style='text-align:left;'>" + escaped + "</div></html>");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(new Color(75, 85, 99));
        return label;
    }

    private JPanel createEmptyRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setBorder(new EmptyBorder(24, 26, 24, 26));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));

        JLabel msg = new JLabel("Không tìm thấy loại thể thao phù hợp.");
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        msg.setForeground(new Color(107, 114, 128));
        row.add(msg, BorderLayout.CENTER);
        return row;
    }

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

    private JPanel createFlexCell(JLabel label, int height, Color bg, int alignment) {
        label.setHorizontalAlignment(alignment);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(bg);
        panel.setOpaque(true);
        panel.setBorder(new EmptyBorder(0, 8, 0, 8));
        panel.add(label, BorderLayout.CENTER);
        panel.setMinimumSize(new Dimension(80, height));
        panel.setPreferredSize(new Dimension(200, height));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        return panel;
    }

    private JLabel createCellLabel(String text, Color fg) {
        JLabel label = new JLabel(valueOrDash(text));
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(fg);
        return label;
    }

    private void loadData() {
        String selectedId = selectedRow == null ? null : selectedRow.sportId();
        allRows.clear();
        allRows.addAll(controller.loadAll());
        applyFilterAndSort();
        renderTable();
        restoreSelection(selectedId);
    }

    private void applyFilterAndSort() {
        String keyword = normalizedSortKey(searchField.getText().trim());
        displayedRows.clear();
        for (SportTypeTableRow row : allRows) {
            if (keyword.isEmpty()
                    || normalizedSortKey(row.sportId()).contains(keyword)
                    || normalizedSortKey(row.name()).contains(keyword)
                    || normalizedSortKey(row.description()).contains(keyword)) {
                displayedRows.add(row);
            }
        }
        sortRows();
    }

    private void sortRows() {
        Comparator<SportTypeTableRow> comparator;
        if ("Mã loại".equals(cbSort.getSelectedItem())) {
            comparator = Comparator.comparing(r -> normalizedSortKey(r.sportId()));
        } else {
            comparator = Comparator.comparing(r -> normalizedSortKey(r.name()));
        }
        if (!sortAscending) {
            comparator = comparator.reversed();
        }
        displayedRows.sort(comparator);
    }

    private void renderTable() {
        tablePanel.removeAll();

        if (displayedRows.isEmpty()) {
            tablePanel.add(createEmptyRow());
        } else {
            int index = 0;
            for (SportTypeTableRow row : displayedRows) {
                tablePanel.add(createDataRow(row, index++));
            }
        }

        footerLabel.setText("Hiển thị " + displayedRows.size() + " loại thể thao");
        tablePanel.revalidate();
        tablePanel.repaint();
    }

    private void bindSearchListener() {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refresh();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refresh();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refresh();
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
        cbSort.addActionListener(e -> {
            String selectedId = selectedRow == null ? null : selectedRow.sportId();
            applyFilterAndSort();
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
        btnSortDir.addActionListener(e -> {
            sortAscending = !sortAscending;
            updateSortDirectionButton();
            String selectedId = selectedRow == null ? null : selectedRow.sportId();
            applyFilterAndSort();
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
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 28, 28);
                g2.dispose();
            }
        };
        wrapper.setOpaque(false);
        wrapper.setPreferredSize(new Dimension(190, 41));
        wrapper.setMaximumSize(new Dimension(190, 41));
        wrapper.add(cbSort, BorderLayout.CENTER);
        wrapper.add(btnSortDir, BorderLayout.EAST);
        return wrapper;
    }

    private void updateSortDirectionButton() {
        btnSortDir.setText(sortAscending ? "▲" : "▼");
        btnSortDir.setToolTipText(sortAscending ? "Đang sắp xếp tăng dần" : "Đang sắp xếp giảm dần");
    }

    private void refresh() {
        String selectedId = selectedRow == null ? null : selectedRow.sportId();
        applyFilterAndSort();
        renderTable();
        restoreSelection(selectedId);
    }

    private void restoreSelection(String sportId) {
        selectedRow = null;
        if (sportId == null) {
            return;
        }
        for (SportTypeTableRow row : displayedRows) {
            if (row.sportId().equals(sportId)) {
                selectedRow = row;
                return;
            }
        }
    }

    private void openCreateDialog() {
        SportTypeForm form = SportTypeCreateDialog.show(this);
        if (form == null) {
            return;
        }

        String error = controller.create(form);
        if (error != null) {
            AppDialog.showError(this, normalizeError("Không thể thêm loại thể thao.", error));
            return;
        }

        AppDialog.showInfo(this, "Đã thêm loại thể thao thành công.");
        loadData();
    }

    private void openEditDialog() {
        if (selectedRow == null) {
            return;
        }

        SportTypeForm form = SportTypeEditDialog.show(this, selectedRow);
        if (form == null) {
            return;
        }

        String error = controller.update(form);
        if (error != null) {
            AppDialog.showError(this, normalizeError("Không thể cập nhật loại thể thao.", error));
            return;
        }

        AppDialog.showInfo(this, "Đã cập nhật loại thể thao thành công.");
        loadData();
    }

    private void deleteSelected() {
        if (selectedRow == null) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa loại thể thao \"" + selectedRow.name() + "\"?",
                "Xác nhận xóa",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.OK_OPTION) {
            return;
        }

        SportTypeForm form = new SportTypeForm(selectedRow.sportId(), selectedRow.name(), selectedRow.description());
        String error = controller.delete(form);
        if (error != null) {
            AppDialog.showError(this, normalizeError("Không thể xóa loại thể thao.", error));
            return;
        }

        AppDialog.showInfo(this, "Đã xóa loại thể thao.");
        selectedRow = null;
        loadData();
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

    private String valueOrDash(String text) {
        return text == null || text.isBlank() ? "--" : text;
    }

    private String escapeHtml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private String normalizedSortKey(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'D');
        return normalized.toLowerCase(Locale.ROOT);
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
        button.setFont(new Font("Segoe UI", Font.BOLD, 10));
        button.setBorder(new EmptyBorder(5, 8, 5, 8));
        return button;
    }

    public static JFrame createPreviewFrame() {
        JFrame frame = new JFrame("RENSTA - Quản lý loại thể thao");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setContentPane(new ManageSportTypeScreen());
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setMinimumSize(new Dimension(900, 600));
        frame.setLocationRelativeTo(null);
        return frame;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> createPreviewFrame().setVisible(true));
    }
}