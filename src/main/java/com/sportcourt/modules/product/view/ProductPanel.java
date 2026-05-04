package com.sportcourt.modules.product.view;

import com.sportcourt.modules.product.dto.ProductCreateRequest;
import com.sportcourt.modules.product.dto.ProductResponse;
import com.sportcourt.modules.product.dto.ProductSearchCriteria;
import com.sportcourt.modules.product.dto.ProductUpdateRequest;

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

public class ProductPanel extends JPanel {
    private static final Color PAGE_BACKGROUND = new Color(247, 248, 252);
    private static final Color TEXT            = new Color(15, 23, 42);
    private static final Color MUTED           = new Color(100, 116, 139);
    private static final Color BORDER          = new Color(226, 232, 240);

    private static final Color GREEN       = new Color(22, 163, 74);
    private static final Color GREEN_LIGHT = new Color(220, 252, 231);
    private static final Color RED         = new Color(220, 38, 38);
    private static final Color RED_LIGHT   = new Color(254, 226, 226);
    private static final Color BLUE        = new Color(37, 99, 235);
    private static final Color BLUE_LIGHT  = new Color(219, 234, 254);

    private final ProductTableModel tableModel = new ProductTableModel();
    private final JTable            table      = new JTable(tableModel);
    private final JTextField        searchField = new JTextField();
    private final JButton           addButton   = createAddButton();
    private final JLabel            footerLabel = new JLabel("Đang hiển thị 0 sản phẩm");
    private final Timer             searchTimer;

    private ActionListener searchAction;
    private ActionListener addAction;
    private ActionListener updateAction;
    private ActionListener deleteAction;
    private ActionListener restoreAction;
    private ActionListener refreshAction;

    private String selectedProductId;

    public ProductPanel() {
        setLayout(new BorderLayout(0, 24));
        setBackground(PAGE_BACKGROUND);
        setBorder(new EmptyBorder(22, 24, 22, 24));

        searchTimer = new Timer(350, e -> fireSearchAction());
        searchTimer.setRepeats(false);

        add(createPageHeader(), BorderLayout.NORTH);
        add(createTableCard(), BorderLayout.CENTER);

        bindEvents();
    }

    public void setSearchAction(ActionListener l)  { this.searchAction  = l; }
    public void setAddAction(ActionListener l)      { this.addAction     = l; }
    public void setUpdateAction(ActionListener l)   { this.updateAction  = l; }
    public void setDeleteAction(ActionListener l)   { this.deleteAction  = l; }
    public void setRestoreAction(ActionListener l)  { this.restoreAction = l; }
    public void setRefreshAction(ActionListener l)  { this.refreshAction = l; }

    public ProductSearchCriteria getSearchCriteria() {
        ProductSearchCriteria criteria = new ProductSearchCriteria();
        criteria.setKeyword(searchField.getText());
        criteria.setIncludeDeleted(true);
        return criteria;
    }

    public String getSelectedProductId() {
        if (selectedProductId != null && !selectedProductId.trim().isEmpty()) {
            return selectedProductId;
        }
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return null;
        ProductResponse row = getProductByViewRow(viewRow);
        return row == null ? null : row.getMaSp();
    }

    public ProductCreateRequest showCreateDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        AddProductDialog dialog = new AddProductDialog(owner);
        return dialog.showDialog();
    }

    public ProductUpdateRequest showUpdateDialog(ProductResponse product) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        EditProductDialog dialog = new EditProductDialog(owner, product);
        return dialog.showDialog();
    }

    public void showProductTable(List<ProductResponse> products) {
        boolean searchFocused  = searchField.isFocusOwner();
        int     caretPosition  = searchField.getCaretPosition();

        tableModel.setRows(products);
        selectedProductId = null;
        footerLabel.setText("Đang hiển thị " + tableModel.getRowCount() + " sản phẩm");

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
        footerLabel.setText(loading
                ? "Đang tải dữ liệu..."
                : "Đang hiển thị " + tableModel.getRowCount() + " sản phẩm");
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message == null ? "Có lỗi xảy ra." : message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    public boolean confirm(String message) {
        int result = JOptionPane.showConfirmDialog(this, message, "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }

    // ── Layout ──────────────────────────────────────────────────────────────

    private JPanel createPageHeader() {
        JPanel header = new JPanel();
        header.setLayout(new javax.swing.BoxLayout(header, javax.swing.BoxLayout.Y_AXIS));
        header.setOpaque(false);

        JLabel title = new JLabel("QUẢN LÝ SẢN PHẨM");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Hiển thị danh sách sản phẩm và hỗ trợ tìm kiếm.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
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
        card.add(createToolbar(),     BorderLayout.NORTH);
        card.add(createTableScroll(), BorderLayout.CENTER);
        card.add(createFooter(),      BorderLayout.SOUTH);
        return card;
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(16, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(18, 26, 18, 26));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 22, 0));
        left.setOpaque(false);

        JLabel listTitle = new JLabel("DANH SÁCH SẢN PHẨM");
        listTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        listTitle.setForeground(TEXT);

        left.add(listTitle);
        left.add(addButton);

        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        searchField.setPreferredSize(new Dimension(380, 40));
        searchField.putClientProperty("JTextField.placeholderText", "Tìm kiếm");
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(0, 16, 0, 16)
        ));

        toolbar.add(left,        BorderLayout.WEST);
        toolbar.add(searchField, BorderLayout.EAST);
        return toolbar;
    }

    private JScrollPane createTableScroll() {
        setupTable();
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, BORDER));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(54);
        scrollPane.getVerticalScrollBar().setBlockIncrement(300);
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

    // ── Table setup ──────────────────────────────────────────────────────────

    private void setupTable() {
        table.setRowHeight(68);
        table.setShowGrid(true);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(true);
        table.setGridColor(BORDER);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(239, 246, 255));
        table.setSelectionForeground(TEXT);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        table.setForeground(MUTED);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 52));
        header.setBackground(Color.WHITE);
        header.setForeground(new Color(71, 85, 105));
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Cột: MÃ SP | TÊN SẢN PHẨM | ĐƠN VỊ TÍNH | ĐƠN GIÁ | SỐ LƯỢNG TỒN | THAO TÁC
        table.getColumnModel().getColumn(ProductTableModel.COL_ID).setPreferredWidth(100);
        table.getColumnModel().getColumn(ProductTableModel.COL_NAME).setPreferredWidth(280);
        table.getColumnModel().getColumn(ProductTableModel.COL_DVT).setPreferredWidth(150);
        table.getColumnModel().getColumn(ProductTableModel.COL_PRICE).setPreferredWidth(150);
        table.getColumnModel().getColumn(ProductTableModel.COL_STOCK).setPreferredWidth(140);
        table.getColumnModel().getColumn(ProductTableModel.COL_ACTION).setPreferredWidth(220);

        table.setDefaultRenderer(Object.class, new ProductCellRenderer());
        table.getColumnModel().getColumn(ProductTableModel.COL_ACTION).setCellRenderer(new ActionCellRenderer());
    }

    private JButton createAddButton() {
        RoundedButton button = new RoundedButton("+ Thêm sản phẩm", GREEN_LIGHT, new Color(187, 247, 208), 28);
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

    // ── Events ───────────────────────────────────────────────────────────────

    private void bindEvents() {
        addButton.addActionListener(e -> {
            selectedProductId = null;
            if (addAction != null) {
                addAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "add"));
            }
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { restartSearchTimer(); }
            @Override public void removeUpdate(DocumentEvent e)  { restartSearchTimer(); }
            @Override public void changedUpdate(DocumentEvent e) { restartSearchTimer(); }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int viewRow    = table.rowAtPoint(e.getPoint());
                int viewColumn = table.columnAtPoint(e.getPoint());
                if (viewRow < 0 || viewColumn < 0) return;

                ProductResponse product = getProductByViewRow(viewRow);
                if (product == null) return;

                selectedProductId = product.getMaSp();

                int modelColumn = table.convertColumnIndexToModel(viewColumn);
                if (modelColumn != ProductTableModel.COL_ACTION) return;

                Rectangle cellRect  = table.getCellRect(viewRow, viewColumn, false);
                int       relativeX = e.getX() - cellRect.x;

                if (relativeX < cellRect.width / 2) {
                    if (product.isDeleted()) fireRestoreAction();
                    else                     fireDeleteAction();
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
                table.setCursor(modelColumn == ProductTableModel.COL_ACTION
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getDefaultCursor());
            }
        });
    }

    private ProductResponse getProductByViewRow(int viewRow) {
        int modelRow = table.convertRowIndexToModel(viewRow);
        return tableModel.getRow(modelRow);
    }

    private void restartSearchTimer()   { searchTimer.restart(); }
    private void fireSearchAction()     { if (searchAction  != null) searchAction .actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "search"));  }
    private void fireUpdateAction()     { if (updateAction  != null) updateAction .actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "update"));  }
    private void fireDeleteAction()     { if (deleteAction  != null) deleteAction .actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "delete"));  }
    private void fireRestoreAction()    { if (restoreAction != null) restoreAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "restore")); }
    @SuppressWarnings("unused")
    private void fireRefreshAction()    { if (refreshAction != null) refreshAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "refresh")); }

    // ── Renderers ────────────────────────────────────────────────────────────

    private class ProductCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            ProductResponse product = tableModel.getRow(table.convertRowIndexToModel(row));
            boolean deleted = product != null && product.isDeleted();

            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 1, BORDER),
                    new EmptyBorder(0, 14, 0, 14)
            ));
            label.setFont(new Font("Segoe UI", column == ProductTableModel.COL_ID ? Font.BOLD : Font.PLAIN, 15));
            label.setForeground(column == ProductTableModel.COL_ID ? new Color(0, 150, 40) : MUTED);
            label.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);

            if (deleted) {
                label.setForeground(new Color(148, 163, 184));
            }
            return label;
        }
    }

    private class ActionCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            ProductResponse product = tableModel.getRow(table.convertRowIndexToModel(row));
            boolean deleted = product != null && product.isDeleted();

            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 11));
            panel.setOpaque(true);
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, BORDER));

            if (deleted) {
                panel.add(createPill("Khôi phục", GREEN, GREEN_LIGHT, 88, 30, 22));
            } else {
                panel.add(createPill("Xóa",       RED,  RED_LIGHT,   62, 30, 22));
            }
            panel.add(createPill("Chỉnh sửa", BLUE, BLUE_LIGHT, 96, 30, 22));
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

    // ── Custom components ────────────────────────────────────────────────────

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
        private final int   radius;
        private RoundedButton(String text, Color backgroundColor, Color borderColor, int radius) {
            super(text);
            this.backgroundColor = backgroundColor;
            this.borderColor     = borderColor;
            this.radius          = radius;
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
        private final int   radius;
        private final Color backgroundColor;
        private final Color borderColor;
        private RoundedPanel(int radius, Color backgroundColor, Color borderColor) {
            this.radius          = radius;
            this.backgroundColor = backgroundColor;
            this.borderColor     = borderColor;
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
            if (borderColor == null) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(borderColor);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.dispose();
        }
    }
}