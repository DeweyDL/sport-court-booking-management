package com.sportcourt.modules.staff.view;

import com.sportcourt.modules.staff.dto.StaffCreateRequest;
import com.sportcourt.modules.staff.dto.StaffDetailResponse;
import com.sportcourt.modules.staff.dto.StaffResponse;
import com.sportcourt.modules.staff.dto.StaffSearchCriteria;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class StaffPanel extends JPanel {
    private static final Color PAGE_BACKGROUND = new Color(247, 248, 252);
    private static final Color TEXT = new Color(15, 23, 42);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Color BORDER = new Color(226, 232, 240);

    private static final Color GREEN = new Color(22, 163, 74);
    private static final Color GREEN_LIGHT = new Color(220, 252, 231);

    private static final Color RED = new Color(220, 38, 38);
    private static final Color RED_LIGHT = new Color(254, 226, 226);

    private static final Color BLUE = new Color(37, 99, 235);
    private static final Color BLUE_LIGHT = new Color(219, 234, 254);

    private static final Color YELLOW = new Color(180, 83, 9);
    private static final Color YELLOW_LIGHT = new Color(254, 243, 199);

    private final StaffTableModel tableModel = new StaffTableModel();
    private final JTable table = new JTable(tableModel);
    private final JTextField searchField = new JTextField();
    private final JButton addButton = createAddButton();
    private final JLabel footerLabel = new JLabel("Đang hiển thị 0 nhân viên");
    private final Timer searchTimer;

    private ActionListener searchAction;
    private ActionListener addAction;
    private ActionListener updateAction;
    private ActionListener deleteAction;
    private ActionListener restoreAction;
    private ActionListener refreshAction;

    private String selectedStaffId;
    private String currentBranchId = "CN01";
    private String managerTypeId = "LNV01";
    private String staffTypeId = "LNV02";

    public StaffPanel() {
        setLayout(new BorderLayout(0, 24));
        setBackground(PAGE_BACKGROUND);
        setBorder(new EmptyBorder(22, 24, 22, 24));

        searchTimer = new Timer(350, e -> fireSearchAction());
        searchTimer.setRepeats(false);

        add(createPageHeader(), BorderLayout.NORTH);
        add(createTableCard(), BorderLayout.CENTER);

        bindEvents();
    }

    public void setCurrentBranchId(String currentBranchId) {
        if (currentBranchId != null && !currentBranchId.trim().isEmpty()) {
            this.currentBranchId = currentBranchId.trim();
        }
    }

    public void setStaffTypeIds(String managerTypeId, String staffTypeId) {
        if (managerTypeId != null && !managerTypeId.trim().isEmpty()) {
            this.managerTypeId = managerTypeId.trim();
        }

        if (staffTypeId != null && !staffTypeId.trim().isEmpty()) {
            this.staffTypeId = staffTypeId.trim();
        }
    }

    public void setSearchAction(ActionListener searchAction) {
        this.searchAction = searchAction;
    }

    public void setAddAction(ActionListener addAction) {
        this.addAction = addAction;
    }

    public void setUpdateAction(ActionListener updateAction) {
        this.updateAction = updateAction;
    }

    public void setDeleteAction(ActionListener deleteAction) {
        this.deleteAction = deleteAction;
    }

    public void setRestoreAction(ActionListener restoreAction) {
        this.restoreAction = restoreAction;
    }

    public void setRefreshAction(ActionListener refreshAction) {
        this.refreshAction = refreshAction;
    }

    public StaffSearchCriteria getSearchCriteria() {
        StaffSearchCriteria criteria = new StaffSearchCriteria();
        criteria.setKeyword(searchField.getText());
        criteria.setMaCn(currentBranchId);
        return criteria;
    }

    public String getSelectedStaffId() {
        if (selectedStaffId != null && !selectedStaffId.trim().isEmpty()) {
            return selectedStaffId;
        }

        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }

        StaffResponse row = getStaffByViewRow(viewRow);
        return row == null ? null : row.getMaNv();
    }

    public StaffCreateRequest showCreateDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        AddStaffDialog dialog = new AddStaffDialog(owner, currentBranchId, managerTypeId, staffTypeId);
        return dialog.showDialog();
    }

    public com.sportcourt.modules.staff.dto.StaffUpdateRequest showUpdateDialog(StaffDetailResponse detail) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        EditStaffDialog dialog = new EditStaffDialog(owner, detail, currentBranchId, managerTypeId, staffTypeId);
        return dialog.showDialog();
    }

    public void showStaffTable(List<StaffResponse> staff) {
        boolean searchFocused = searchField.isFocusOwner();
        int caretPosition = searchField.getCaretPosition();

        tableModel.setRows(staff);
        selectedStaffId = null;
        footerLabel.setText("Đang hiển thị " + tableModel.getRowCount() + " nhân viên");

        if (searchFocused) {
            SwingUtilities.invokeLater(() -> {
                searchField.requestFocusInWindow();
                int length = searchField.getText() == null ? 0 : searchField.getText().length();
                searchField.setCaretPosition(Math.min(caretPosition, length));
            });
        }
    }

    public void setLoading(boolean loading) {
        table.setEnabled(!loading);
        searchField.setEnabled(!loading);
        addButton.setEnabled(!loading);

        if (loading) {
            footerLabel.setText("Đang tải dữ liệu...");
        } else {
            footerLabel.setText("Đang hiển thị " + tableModel.getRowCount() + " nhân viên");
        }
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Thông báo",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message == null ? "Có lỗi xảy ra." : message,
                "Lỗi",
                JOptionPane.ERROR_MESSAGE
        );
    }

    public boolean confirm(String message) {
        int result = JOptionPane.showConfirmDialog(
                this,
                message,
                "Xác nhận",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        return result == JOptionPane.YES_OPTION;
    }

    private JPanel createPageHeader() {
        JPanel header = new JPanel();
        header.setLayout(new javax.swing.BoxLayout(header, javax.swing.BoxLayout.Y_AXIS));
        header.setOpaque(false);

        JLabel title = new JLabel("QUẢN LÝ NHÂN VIÊN");
        title.setFont(new Font("Segoe UI", Font.BOLD, 29));
        title.setForeground(TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Hiển thị danh sách nhân viên và hỗ trợ tìm kiếm.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitle.setForeground(MUTED);
        subtitle.setBorder(new EmptyBorder(8, 0, 0, 0));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(title);
        header.add(subtitle);

        return header;
    }

    private JPanel createTableCard() {
        JPanel card = new RoundedPanel(18, Color.WHITE, BORDER);
        card.setLayout(new BorderLayout());
        card.add(createToolbar(), BorderLayout.NORTH);
        card.add(createTableScroll(), BorderLayout.CENTER);
        card.add(createFooter(), BorderLayout.SOUTH);
        return card;
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(16, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(18, 26, 18, 26));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 22, 0));
        left.setOpaque(false);

        JLabel listTitle = new JLabel("DANH SÁCH NHÂN VIÊN");
        listTitle.setFont(new Font("Segoe UI", Font.BOLD, 23));
        listTitle.setForeground(TEXT);

        left.add(listTitle);
        left.add(addButton);

        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        searchField.setPreferredSize(new Dimension(340, 40));
        searchField.putClientProperty("JTextField.placeholderText", "Tìm kiếm");
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(0, 16, 0, 16)
        ));

        toolbar.add(left, BorderLayout.WEST);
        toolbar.add(searchField, BorderLayout.EAST);

        return toolbar;
    }

    private JScrollPane createTableScroll() {
        setupTable();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, BORDER));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(48);
        scrollPane.getVerticalScrollBar().setBlockIncrement(260);

        return scrollPane;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(14, 34, 14, 34));

        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        footerLabel.setForeground(MUTED);

        footer.add(footerLabel, BorderLayout.WEST);

        return footer;
    }

    private void setupTable() {
        table.setRowHeight(68);
        table.setShowGrid(true);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(true);
        table.setGridColor(BORDER);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(240, 253, 244));
        table.setSelectionForeground(TEXT);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setForeground(MUTED);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 52));
        header.setBackground(Color.WHITE);
        header.setForeground(new Color(71, 85, 105));
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));

        table.getColumnModel().getColumn(StaffTableModel.COL_ID).setPreferredWidth(95);
        table.getColumnModel().getColumn(StaffTableModel.COL_NAME).setPreferredWidth(185);
        table.getColumnModel().getColumn(StaffTableModel.COL_PHONE).setPreferredWidth(125);
        table.getColumnModel().getColumn(StaffTableModel.COL_EMAIL).setPreferredWidth(220);
        table.getColumnModel().getColumn(StaffTableModel.COL_ROLE).setPreferredWidth(125);
        table.getColumnModel().getColumn(StaffTableModel.COL_START_DATE).setPreferredWidth(145);
        table.getColumnModel().getColumn(StaffTableModel.COL_STATUS).setPreferredWidth(150);
        table.getColumnModel().getColumn(StaffTableModel.COL_ACTION).setPreferredWidth(210);

        table.setDefaultRenderer(Object.class, new StaffCellRenderer());
        table.getColumnModel().getColumn(StaffTableModel.COL_ROLE).setCellRenderer(new RoleCellRenderer());
        table.getColumnModel().getColumn(StaffTableModel.COL_STATUS).setCellRenderer(new StatusCellRenderer());
        table.getColumnModel().getColumn(StaffTableModel.COL_ACTION).setCellRenderer(new ActionCellRenderer());
    }

    private JButton createAddButton() {
        RoundedButton button = new RoundedButton("+ Thêm nhân viên", GREEN_LIGHT, new Color(187, 247, 208), 28);
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setForeground(new Color(21, 128, 61));
        button.setBorder(new EmptyBorder(10, 22, 10, 22));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void bindEvents() {
        addButton.addActionListener(e -> {
            selectedStaffId = null;
            if (addAction != null) {
                addAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "add"));
            }
        });

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

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int viewRow = table.rowAtPoint(e.getPoint());
                int viewColumn = table.columnAtPoint(e.getPoint());

                if (viewRow < 0 || viewColumn < 0) {
                    return;
                }

                StaffResponse staff = getStaffByViewRow(viewRow);
                if (staff == null) {
                    return;
                }

                selectedStaffId = staff.getMaNv();

                int modelColumn = table.convertColumnIndexToModel(viewColumn);
                if (modelColumn != StaffTableModel.COL_ACTION) {
                    return;
                }

                Rectangle cellRect = table.getCellRect(viewRow, viewColumn, false);
                int relativeX = e.getX() - cellRect.x;

                if (relativeX < cellRect.width / 2) {
                    if (staff.isDeleted()) {
                        fireRestoreAction();
                    } else {
                        fireDeleteAction();
                    }
                } else {
                    fireUpdateAction();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                table.setCursor(Cursor.getDefaultCursor());
            }
        });

        table.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int viewColumn = table.columnAtPoint(e.getPoint());

                if (viewColumn < 0) {
                    table.setCursor(Cursor.getDefaultCursor());
                    return;
                }

                int modelColumn = table.convertColumnIndexToModel(viewColumn);

                if (modelColumn == StaffTableModel.COL_ACTION) {
                    table.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    table.setCursor(Cursor.getDefaultCursor());
                }
            }
        });
    }

    private StaffResponse getStaffByViewRow(int viewRow) {
        int modelRow = table.convertRowIndexToModel(viewRow);
        return tableModel.getRow(modelRow);
    }

    private void restartSearchTimer() {
        searchTimer.restart();
    }

    private void fireSearchAction() {
        if (searchAction != null) {
            searchAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "search"));
        }
    }

    private void fireUpdateAction() {
        if (updateAction != null) {
            updateAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "update"));
        }
    }

    private void fireDeleteAction() {
        if (deleteAction != null) {
            deleteAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "delete"));
        }
    }

    private void fireRestoreAction() {
        if (restoreAction != null) {
            restoreAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "restore"));
        }
    }

    @SuppressWarnings("unused")
    private void fireRefreshAction() {
        if (refreshAction != null) {
            refreshAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "refresh"));
        }
    }

    private class StaffCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table,
                    value,
                    isSelected,
                    hasFocus,
                    row,
                    column
            );

            StaffResponse staff = tableModel.getRow(table.convertRowIndexToModel(row));
            boolean deleted = staff != null && staff.isDeleted();

            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 1, BORDER),
                    new EmptyBorder(0, 10, 0, 10)
            ));
            label.setFont(new Font("Segoe UI", column == StaffTableModel.COL_ID ? Font.BOLD : Font.PLAIN, 15));
            label.setForeground(column == StaffTableModel.COL_ID ? new Color(0, 150, 40) : MUTED);
            label.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);

            if (deleted) {
                label.setForeground(new Color(148, 163, 184));
            }

            if (column == StaffTableModel.COL_ID) {
                label.setText(shorten(String.valueOf(value), 12));
            }

            return label;
        }

        private String shorten(String value, int maxLength) {
            if (value == null || value.length() <= maxLength) {
                return value;
            }

            return value.substring(0, Math.max(0, maxLength - 3)) + "...";
        }
    }

    private class RoleCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            String rawText = String.valueOf(value);
            boolean isManager = "QUẢN LÝ".equalsIgnoreCase(rawText);
            String text = isManager ? "QUẢN LÝ" : "THU NGÂN";

            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 12));
            wrapper.setOpaque(true);
            wrapper.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            wrapper.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, BORDER));

            wrapper.add(createPill(
                    text,
                    isManager ? GREEN : TEXT,
                    isManager ? GREEN_LIGHT : new Color(241, 245, 249),
                    112,
                    30,
                    22
            ));

            return wrapper;
        }
    }

    private class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            String status = String.valueOf(value);
            boolean deleted = "ĐÃ XOÁ".equalsIgnoreCase(status);

            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 12));
            wrapper.setOpaque(true);
            wrapper.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            wrapper.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, BORDER));

            wrapper.add(createPill(
                    "• " + status,
                    deleted ? RED : GREEN,
                    deleted ? RED_LIGHT : GREEN_LIGHT,
                    126,
                    30,
                    22
            ));

            return wrapper;
        }
    }

    private class ActionCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            StaffResponse staff = tableModel.getRow(table.convertRowIndexToModel(row));
            boolean deleted = staff != null && staff.isDeleted();

            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 11));
            panel.setOpaque(true);
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, BORDER));

            if (deleted) {
                panel.add(createActionPill("Khôi phục", GREEN, GREEN_LIGHT, 88, 30, 22));
            } else {
                panel.add(createActionPill("Xóa", RED, RED_LIGHT, 62, 30, 22));
            }

            panel.add(createActionPill("Chỉnh sửa", BLUE, BLUE_LIGHT, 96, 30, 22));

            return panel;
        }
    }

    private JLabel createPill(String text, Color foreground, Color background, int width, int height, int radius) {
        RoundedLabel label = new RoundedLabel(text, radius);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(foreground);
        label.setBackground(background);
        label.setPreferredSize(new Dimension(width, height));
        label.setBorder(new EmptyBorder(5, 12, 5, 12));
        return label;
    }

    private JLabel createActionPill(String text, Color foreground, Color background, int width, int height, int radius) {
        RoundedLabel label = new RoundedLabel(text, radius);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(foreground);
        label.setBackground(background);
        label.setPreferredSize(new Dimension(width, height));
        label.setBorder(new EmptyBorder(5, 12, 5, 12));
        return label;
    }

    private static class RoundedLabel extends JLabel {
        private final int radius;

        private RoundedLabel(String text, int radius) {
            super(text);
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            g2.dispose();

            super.paintComponent(g);
        }
    }

    private static class RoundedButton extends JButton {
        private final Color backgroundColor;
        private final Color borderColor;
        private final int radius;

        private RoundedButton(String text, Color backgroundColor, Color borderColor, int radius) {
            super(text);
            this.backgroundColor = backgroundColor;
            this.borderColor = borderColor;
            this.radius = radius;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (!isEnabled()) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
            }

            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            }

            g2.dispose();

            super.paintComponent(g);
        }
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color backgroundColor;
        private final Color borderColor;

        private RoundedPanel(int radius, Color backgroundColor, Color borderColor) {
            this.radius = radius;
            this.backgroundColor = backgroundColor;
            this.borderColor = borderColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

            g2.dispose();

            super.paintComponent(g);
        }

        @Override
        protected void paintBorder(Graphics g) {
            if (borderColor == null) {
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(borderColor);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

            g2.dispose();
        }
    }
}
