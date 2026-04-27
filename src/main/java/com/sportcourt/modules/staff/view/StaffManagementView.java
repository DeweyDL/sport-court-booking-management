package com.sportcourt.modules.staff.view;

import com.sportcourt.modules.staff.dto.StaffCreateRequest;
import com.sportcourt.modules.staff.dto.StaffDetailResponse;
import com.sportcourt.modules.staff.dto.StaffResponse;
import com.sportcourt.modules.staff.dto.StaffSearchCriteria;
import com.sportcourt.modules.staff.dto.StaffUpdateRequest;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class StaffManagementView extends JPanel {
    private static final Color PAGE_BG = new Color(246, 247, 251);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color HEADER_BG = new Color(243, 244, 248);
    private static final Color TEXT_DARK = new Color(17, 24, 39);
    private static final Color TEXT_MUTED = new Color(104, 114, 130);
    private static final Color BORDER = new Color(229, 231, 235);

    private static final Color GREEN_BG = new Color(220, 252, 231);
    private static final Color GREEN_TEXT = new Color(22, 163, 74);

    private static final Color RED_BG = new Color(254, 226, 226);
    private static final Color RED_TEXT = new Color(220, 38, 38);

    private static final Color BLUE_BG = new Color(239, 246, 255);
    private static final Color BLUE_TEXT = new Color(37, 99, 235);

    private static final Color GRAY_BG = new Color(243, 244, 246);
    private static final Color SELECTED_BG = new Color(239, 246, 255);

    private static final int ACTION_COLUMN = 6;

    private JTextField txtKeyword;
    private JButton btnAdd;
    private JTable tblStaff;
    private StaffListTableModel tableModel;
    private JLabel lblFooter;

    private ActionListener searchListener;
    private ActionListener addListener;
    private ActionListener updateListener;
    private ActionListener deleteListener;
    private ActionListener detailListener;
    private ActionListener refreshListener;

    public StaffManagementView() {
        initComponents();
        initLayout();
        initEvents();
    }

    private void initComponents() {
        txtKeyword = new JTextField();
        txtKeyword.setPreferredSize(new Dimension(460, 42));
        txtKeyword.setFont(fontPlain(14));
        txtKeyword.setForeground(TEXT_DARK);
        txtKeyword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(0, 18, 0, 18)
        ));

        btnAdd = new PillButton("+ Thêm nhân viên", GREEN_BG, new Color(21, 128, 61));
        btnAdd.setFont(fontBold(15));

        tableModel = new StaffListTableModel();

        tblStaff = new JTable(tableModel);
        tblStaff.setRowHeight(74);
        tblStaff.setShowGrid(false);
        tblStaff.setIntercellSpacing(new Dimension(0, 0));
        tblStaff.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblStaff.setFillsViewportHeight(true);
        tblStaff.setBackground(CARD_BG);
        tblStaff.setFont(fontPlain(14));
        tblStaff.setForeground(TEXT_MUTED);

        JTableHeader header = tblStaff.getTableHeader();
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 56));
        header.setBackground(HEADER_BG);
        header.setForeground(new Color(91, 98, 113));
        header.setFont(fontBold(13));
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

        tblStaff.setDefaultRenderer(Object.class, new StaffCellRenderer());

        tblStaff.getColumnModel().getColumn(0).setPreferredWidth(120);
        tblStaff.getColumnModel().getColumn(1).setPreferredWidth(220);
        tblStaff.getColumnModel().getColumn(2).setPreferredWidth(140);
        tblStaff.getColumnModel().getColumn(3).setPreferredWidth(240);
        tblStaff.getColumnModel().getColumn(4).setPreferredWidth(150);
        tblStaff.getColumnModel().getColumn(5).setPreferredWidth(150);
        tblStaff.getColumnModel().getColumn(6).setPreferredWidth(230);

        tblStaff.getColumnModel().getColumn(4).setCellRenderer(new RoleBadgeRenderer());
        tblStaff.getColumnModel().getColumn(6).setCellRenderer(new ActionCellRenderer());

        lblFooter = new JLabel("Hiển thị 0 nhân viên");
        lblFooter.setFont(fontPlain(14));
        lblFooter.setForeground(TEXT_MUTED);
    }

    private void initLayout() {
        setLayout(new BorderLayout());
        setBackground(PAGE_BG);
        setBorder(BorderFactory.createEmptyBorder(48, 56, 40, 56));

        JPanel pageHeader = new JPanel(new BorderLayout());
        pageHeader.setOpaque(false);

        JLabel title = new JLabel("QUẢN LÝ NHÂN VIÊN");
        title.setFont(fontBold(30));
        title.setForeground(TEXT_DARK);

        JLabel subtitle = new JLabel("Hiển thị danh sách nhân viên và hỗ trợ tìm kiếm.");
        subtitle.setFont(fontPlain(15));
        subtitle.setForeground(TEXT_MUTED);
        subtitle.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JPanel titleBox = new JPanel(new BorderLayout());
        titleBox.setOpaque(false);
        titleBox.add(title, BorderLayout.NORTH);
        titleBox.add(subtitle, BorderLayout.CENTER);

        pageHeader.add(titleBox, BorderLayout.WEST);
        add(pageHeader, BorderLayout.NORTH);

        RoundedPanel card = new RoundedPanel(22, CARD_BG);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        JPanel cardTop = new JPanel(new BorderLayout());
        cardTop.setOpaque(false);
        cardTop.setBorder(BorderFactory.createEmptyBorder(22, 28, 22, 28));

        JPanel leftTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
        leftTop.setOpaque(false);

        JLabel listTitle = new JLabel("DANH SÁCH NHÂN VIÊN");
        listTitle.setFont(fontBold(22));
        listTitle.setForeground(TEXT_DARK);

        leftTop.add(listTitle);
        leftTop.add(btnAdd);

        JPanel searchWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        searchWrap.setOpaque(false);
        searchWrap.add(txtKeyword);

        cardTop.add(leftTop, BorderLayout.WEST);
        cardTop.add(searchWrap, BorderLayout.EAST);

        JScrollPane scrollPane = new JScrollPane(tblStaff);
        scrollPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));
        scrollPane.getViewport().setBackground(CARD_BG);

        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(18, 28, 18, 28));
        footerPanel.add(lblFooter, BorderLayout.WEST);

        card.add(cardTop, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);
        card.add(footerPanel, BorderLayout.SOUTH);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(48, 0, 0, 0));
        center.add(card, BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);

        tableModel.setFooterLabel(lblFooter);
    }

    private void initEvents() {
        btnAdd.addActionListener(e -> {
            if (addListener != null) {
                addListener.actionPerformed(e);
            }
        });

        txtKeyword.addActionListener(e -> {
            if (searchListener != null) {
                searchListener.actionPerformed(e);
            }
        });

        tblStaff.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                Point point = e.getPoint();
                int row = tblStaff.rowAtPoint(point);
                int column = tblStaff.columnAtPoint(point);

                if (row < 0) {
                    return;
                }

                tblStaff.setRowSelectionInterval(row, row);

                if (column == ACTION_COLUMN) {
                    handleActionColumnClick(e, row);
                    return;
                }

                if (e.getClickCount() == 2 && detailListener != null) {
                    detailListener.actionPerformed(
                            new ActionEvent(tblStaff, ActionEvent.ACTION_PERFORMED, "detail")
                    );
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                tblStaff.setCursor(Cursor.getDefaultCursor());
            }
        });

        tblStaff.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int row = tblStaff.rowAtPoint(e.getPoint());
                int column = tblStaff.columnAtPoint(e.getPoint());

                if (row >= 0 && column == ACTION_COLUMN) {
                    tblStaff.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    tblStaff.setCursor(Cursor.getDefaultCursor());
                }
            }
        });
    }

    private void handleActionColumnClick(java.awt.event.MouseEvent e, int row) {
        int modelRow = tblStaff.convertRowIndexToModel(row);

        if (modelRow < 0 || modelRow >= tableModel.getRowCount()) {
            return;
        }

        int cellX = e.getX() - tblStaff.getCellRect(row, ACTION_COLUMN, true).x;

        if (cellX < 82) {
            if (deleteListener != null) {
                deleteListener.actionPerformed(
                        new ActionEvent(tblStaff, ActionEvent.ACTION_PERFORMED, "delete")
                );
            }
        } else {
            if (updateListener != null) {
                updateListener.actionPerformed(
                        new ActionEvent(tblStaff, ActionEvent.ACTION_PERFORMED, "update")
                );
            }
        }
    }

    public StaffSearchCriteria getSearchCriteria() {
        StaffSearchCriteria criteria = new StaffSearchCriteria();
        criteria.setKeyword(txtKeyword.getText().trim());
        return criteria;
    }

    public void showStaffTable(List<StaffResponse> staffList) {
        tableModel.setData(staffList);
    }

    public String getSelectedStaffId() {
        int row = tblStaff.getSelectedRow();

        if (row < 0) {
            return null;
        }

        int modelRow = tblStaff.convertRowIndexToModel(row);
        StaffResponse staff = tableModel.getStaffAt(modelRow);

        if (staff == null) {
            return null;
        }

        return staff.getMaNv();
    }

    public StaffCreateRequest showCreateDialog() {
        StaffFormDialog dialog = StaffFormDialog.createMode();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        return dialog.getCreateRequest();
    }

    public StaffUpdateRequest showUpdateDialog(StaffDetailResponse detail) {
        StaffFormDialog dialog = StaffFormDialog.updateMode(detail);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        return dialog.getUpdateRequest();
    }

    public void showDetailDialog(StaffDetailResponse detail) {
        JDialog dialog = new StaffDetailDialog(detail);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    public boolean confirm(String message) {
        int result = JOptionPane.showConfirmDialog(
                this,
                message,
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );

        return result == JOptionPane.YES_OPTION;
    }

    public void setSearchAction(ActionListener listener) {
        this.searchListener = listener;
    }

    public void setAddAction(ActionListener listener) {
        this.addListener = listener;
    }

    public void setUpdateAction(ActionListener listener) {
        this.updateListener = listener;
    }

    public void setDeleteAction(ActionListener listener) {
        this.deleteListener = listener;
    }

    public void setViewDetailAction(ActionListener listener) {
        this.detailListener = listener;
    }

    public void setRefreshAction(ActionListener listener) {
        this.refreshListener = listener;
    }

    private Font fontPlain(int size) {
        return new Font("Segoe UI", Font.PLAIN, size);
    }

    private Font fontBold(int size) {
        return new Font("Segoe UI", Font.BOLD, size);
    }

    private static class StaffListTableModel extends AbstractTableModel {
        private final String[] columns = {
                "MÃ NV",
                "HỌ TÊN",
                "SĐT",
                "EMAIL",
                "CHỨC VỤ",
                "NGÀY VÀO LÀM",
                "THAO TÁC"
        };

        private List<StaffResponse> data = new ArrayList<>();
        private JLabel footerLabel;

        public void setFooterLabel(JLabel footerLabel) {
            this.footerLabel = footerLabel;
            updateFooter();
        }

        public void setData(List<StaffResponse> data) {
            this.data = data == null ? new ArrayList<>() : data;
            fireTableDataChanged();
            updateFooter();
        }

        public StaffResponse getStaffAt(int row) {
            if (row < 0 || row >= data.size()) {
                return null;
            }

            return data.get(row);
        }

        private void updateFooter() {
            if (footerLabel != null) {
                footerLabel.setText("Hiển thị " + data.size() + " nhân viên");
            }
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            StaffResponse staff = data.get(rowIndex);

            switch (columnIndex) {
                case 0:
                    return safe(staff.getMaNv());
                case 1:
                    return safe(staff.getHoTen());
                case 2:
                    return safe(staff.getSdt());
                case 3:
                    return safe(staff.getEmail());
                case 4:
                    return staff.isQuanLy() ? "QUẢN LÝ" : "THU NGÂN";
                case 5:
                    if (staff.getNgayVaoLam() == null) {
                        return "--";
                    }
                    return staff.getNgayVaoLam().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                case 6:
                    return "";
                default:
                    return "";
            }
        }

        private String safe(String value) {
            return value == null || value.trim().isEmpty() ? "--" : value;
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

            label.setFont(fontPlain(14));
            label.setForeground(TEXT_MUTED);
            label.setBorder(BorderFactory.createEmptyBorder(0, 22, 0, 10));
            label.setVerticalAlignment(SwingConstants.CENTER);

            if (column == 0) {
                label.setForeground(new Color(0, 128, 0));
                label.setFont(fontBold(14));
            }

            label.setBackground(isSelected ? SELECTED_BG : CARD_BG);
            return label;
        }
    }

    private class RoleBadgeRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBackground(isSelected ? SELECTED_BG : CARD_BG);
            panel.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

            String text = value == null ? "" : value.toString();

            Color bg = text.equals("QUẢN LÝ") ? GREEN_BG : GRAY_BG;
            Color fg = text.equals("QUẢN LÝ") ? GREEN_TEXT : TEXT_DARK;

            PillLabel badge = new PillLabel("• " + text, bg, fg);
            badge.setFont(fontBold(13));

            panel.add(badge);
            return panel;
        }
    }

    private class ActionCellRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 20));
            panel.setBackground(isSelected ? SELECTED_BG : CARD_BG);
            panel.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));

            PillLabel delete = new PillLabel("Xóa", RED_BG, RED_TEXT);
            delete.setFont(fontBold(13));

            PillLabel update = new PillLabel("Chỉnh sửa", BLUE_BG, BLUE_TEXT);
            update.setFont(fontBold(13));

            panel.add(delete);
            panel.add(update);

            return panel;
        }
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color background;

        RoundedPanel(int radius, Color background) {
            this.radius = radius;
            this.background = background;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D graphics = (Graphics2D) g.create();

            graphics.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            graphics.setColor(background);
            graphics.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            graphics.setColor(new Color(238, 240, 244));
            graphics.setStroke(new BasicStroke(1f));
            graphics.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

            graphics.dispose();

            super.paintComponent(g);
        }
    }

    private static class PillButton extends JButton {
        private final Color bg;
        private final Color fg;

        PillButton(String text, Color bg, Color fg) {
            super(text);
            this.bg = bg;
            this.fg = fg;

            setForeground(fg);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D graphics = (Graphics2D) g.create();

            graphics.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            graphics.setColor(bg);
            graphics.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());

            graphics.dispose();

            super.paintComponent(g);
        }
    }

    private static class PillLabel extends JLabel {
        private final Color bg;
        private final Color fg;

        PillLabel(String text, Color bg, Color fg) {
            super(text);
            this.bg = bg;
            this.fg = fg;

            setForeground(fg);
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D graphics = (Graphics2D) g.create();

            graphics.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            graphics.setColor(bg);
            graphics.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());

            graphics.dispose();

            super.paintComponent(g);
        }
    }
}