package com.sportcourt.modules.managecustomer.view;

import com.formdev.flatlaf.FlatLightLaf;
import com.sportcourt.common.style.AppDialog;
import com.sportcourt.common.style.AppFonts;
import com.sportcourt.modules.managecustomer.controller.ManageCustomerController;
import com.sportcourt.modules.managecustomer.dto.CreateCustomerRequest;
import com.sportcourt.modules.managecustomer.dto.CustomerProfile;
import com.sportcourt.modules.managecustomer.dto.CustomerResult;
import com.sportcourt.modules.managecustomer.dto.CustomerSummary;
import com.sportcourt.modules.managecustomer.dto.UpdateCustomerRequest;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ManageCustomerPreviewScreen extends JPanel {
    private static final Color PAGE_BG = new Color(240, 248, 242);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BRAND_GREEN = new Color(58, 134, 45);
    private static final Color BRAND_GREEN_SOFT = new Color(220, 242, 214);
    private static final Color TEXT_DARK = new Color(30, 41, 59);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);

    private final ManageCustomerController controller = new ManageCustomerController();
    private final List<CustomerVm> customers = new ArrayList<>();

    private final JTextField txtSearch = new JTextField();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"MAKH", "HOTEN", "TIER", "STATUS", "ACTIONS"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    private CustomerVm selectedCustomer;
    private JLabel lblSummary;

    public ManageCustomerPreviewScreen() {
        AppFonts.register();
        setLayout(new BorderLayout());
        add(buildContent(), BorderLayout.CENTER);
        applyEvents();
        loadCustomers("");
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout(14, 14));
        root.setBackground(PAGE_BG);
        root.setBorder(new EmptyBorder(14, 14, 14, 14));
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildListPanel(), BorderLayout.CENTER);
        return root;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Quản Lý Khách Hàng");
        title.setForeground(TEXT_DARK);
        title.setFont(AppFonts.lexendBold(24f));
        header.add(title, BorderLayout.WEST);
        return header;
    }

    private JPanel buildListPanel() {
        JPanel panel = cardPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel top = new JPanel(new BorderLayout(8, 0));
        top.setOpaque(false);

        txtSearch.setPreferredSize(new Dimension(320, 36));
        txtSearch.setFont(AppFonts.lexendRegular(13f));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm theo tên hoặc số điện thoại...");
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(187, 227, 179)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        top.add(txtSearch, BorderLayout.CENTER);

        JButton btnCreate = brandButton("Thêm khách hàng");
        btnCreate.addActionListener(e -> openCreateDialog());
        top.add(btnCreate, BorderLayout.EAST);

        lblSummary = new JLabel("Đang tải dữ liệu khách hàng...");
        lblSummary.setForeground(TEXT_MUTED);
        lblSummary.setFont(AppFonts.lexendRegular(12f));
        panel.add(lblSummary, BorderLayout.SOUTH);
        panel.add(top, BorderLayout.NORTH);

        table.setRowHeight(34);
        table.setFont(AppFonts.lexendRegular(12f));
        table.getTableHeader().setFont(AppFonts.lexendBold(12f));
        table.getTableHeader().setBackground(Color.WHITE);
        table.getTableHeader().setForeground(new Color(107, 114, 128));
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(BRAND_GREEN_SOFT);
        table.setSelectionForeground(TEXT_DARK);
        table.setRowHeight(58);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setDefaultRenderer(Object.class, new ZebraCellRenderer());
        table.getColumnModel().getColumn(0).setPreferredWidth(110);
        table.getColumnModel().getColumn(1).setPreferredWidth(220);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(130);
        table.getColumnModel().getColumn(4).setPreferredWidth(260);
        table.getColumnModel().getColumn(2).setCellRenderer(new TierBadgeRenderer());
        table.getColumnModel().getColumn(3).setCellRenderer(new StatusBadgeRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new ActionsRenderer());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel cardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(227, 232, 240), 1),
                BorderFactory.createEmptyBorder()
        ));
        return panel;
    }

    private JButton brandButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(AppFonts.lexendBold(12f));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBackground(BRAND_GREEN);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(45, 110, 35), 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        btn.setBorderPainted(true);
        btn.setContentAreaFilled(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(118, 36));
        return btn;
    }

    private void applyEvents() {
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshCustomers();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshCustomers();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshCustomers();
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateSelectedCustomerFromTable();
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
                    updateSelectedCustomerFromTable();
                    openProfileForSelectedCustomer();
                }
            }
        });
    }

    private void loadCustomers(String keyword) {
        String selectedCode = selectedCustomer == null ? null : selectedCustomer.getMaKhachHang();
        CustomerResult<List<CustomerSummary>> result = controller.searchByName(keyword);
        if (!result.success()) {
            AppDialog.showError(this, buildFriendlyError("Không thể tải danh sách khách hàng lúc này.", result.message()));
            return;
        }

        customers.clear();
        for (CustomerSummary summary : result.data()) {
            customers.add(CustomerVm.fromSummary(summary));
        }

        refillTable();
        lblSummary.setText("Hiển thị " + customers.size() + " khách hàng trong danh sách.");
        restoreSelection(selectedCode);
    }

    private void refillTable() {
        tableModel.setRowCount(0);
        for (CustomerVm customer : customers) {
            tableModel.addRow(new Object[]{
                    compactCustomerCode(customer.getMaKhachHang()),
                    customer.getHoTen(),
                    customer.getTrangThai(),
                    customer.getTrangThai(),
                    "ACTIONS"
            });
        }
    }

    private void refreshCustomers() {
        loadCustomers(txtSearch.getText().trim());
    }

    private void restoreSelection(String maKhachHang) {
        selectedCustomer = null;
        if (maKhachHang == null) {
            table.clearSelection();
            return;
        }

        for (int i = 0; i < customers.size(); i++) {
            if (customers.get(i).getMaKhachHang().equals(maKhachHang)) {
                table.setRowSelectionInterval(i, i);
                selectedCustomer = customers.get(i);
                return;
            }
        }

        table.clearSelection();
    }

    private void updateSelectedCustomerFromTable() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= customers.size()) {
            selectedCustomer = null;
            return;
        }
        selectedCustomer = customers.get(row);
    }

    private void openCreateDialog() {
        CreateCustomerRequest request = CustomerCreateDialog.show(this);
        if (request == null) {
            return;
        }

        CustomerResult<CustomerProfile> result = controller.create(request);
        if (!result.success()) {
            AppDialog.showError(this, buildFriendlyError("Tạo khách hàng chưa thành công.", result.message()));
            return;
        }

        AppDialog.showInfo(this, "Đã thêm khách hàng.");
        refreshCustomers();
    }

    private void openProfileForSelectedCustomer() {
        if (selectedCustomer == null) {
            return;
        }

        CustomerResult<CustomerProfile> profileResult = controller.getProfile(selectedCustomer.getMaKhachHang());
        if (!profileResult.success() || profileResult.data() == null) {
            AppDialog.showError(this, buildFriendlyError("Không lấy được hồ sơ khách hàng.", profileResult.message()));
            return;
        }

        CustomerProfileDialog.Action action = CustomerProfileDialog.show(this, profileResult.data());
        if (action == null) {
            return;
        }

        if (action == CustomerProfileDialog.Action.RESTORE) {
            restoreSelectedCustomer();
            return;
        }

        if (action == CustomerProfileDialog.Action.DELETE) {
            deleteSelectedCustomer();
            return;
        }

        UpdateCustomerRequest request = CustomerEditDialog.show(this, profileResult.data());
        if (request == null) {
            return;
        }

        CustomerResult<CustomerProfile> updateResult = controller.update(selectedCustomer.getMaKhachHang(), request);
        if (!updateResult.success()) {
            AppDialog.showError(this, buildFriendlyError("Cập nhật khách hàng chưa thành công.", updateResult.message()));
            return;
        }

        AppDialog.showInfo(this, "Đã cập nhật thông tin khách hàng.");
        refreshCustomers();
        restoreSelection(selectedCustomer.getMaKhachHang());
    }

    private void deleteSelectedCustomer() {
        if (selectedCustomer == null) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa khách hàng này?",
                "Xác nhận xóa",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.OK_OPTION) {
            return;
        }

        CustomerResult<Void> result = controller.softDelete(selectedCustomer.getMaKhachHang());
        if (!result.success()) {
            AppDialog.showError(this, buildFriendlyError("Không thể xóa khách hàng vào lúc này.", result.message()));
            return;
        }

        AppDialog.showInfo(this, "Đã chuyển trạng thái khách hàng sang INACTIVE.");
        refreshCustomers();
    }

    private void restoreSelectedCustomer() {
        if (selectedCustomer == null) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn khôi phục khách hàng này?",
                "Xác nhận khôi phục",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm != JOptionPane.OK_OPTION) {
            return;
        }

        CustomerResult<Void> result = controller.restore(selectedCustomer.getMaKhachHang());
        if (!result.success()) {
            AppDialog.showError(this, buildFriendlyError("Không thể khôi phục khách hàng lúc này.", result.message()));
            return;
        }

        AppDialog.showInfo(this, "Đã khôi phục khách hàng.");
        refreshCustomers();
    }

    private String buildFriendlyError(String userMessage, String detail) {
        if (detail == null || detail.isBlank()) {
            return userMessage;
        }
        String normalized = detail.trim();
        if (normalized.endsWith(".") || normalized.endsWith("!") || normalized.endsWith("?")) {
            return normalized;
        }
        return normalized + ".";
    }

    private String formatCurrency(BigDecimal value) {
        if (value == null) {
            return "0 VNĐ";
        }
        return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(value) + " VNĐ";
    }

    private String compactCustomerCode(String maKhachHang) {
        if (maKhachHang == null || maKhachHang.isBlank()) {
            return "#C-0000";
        }
        String raw = maKhachHang.replace("KH_", "");
        String shortCode = raw.length() > 4 ? raw.substring(raw.length() - 4) : raw;
        return "#C-" + shortCode.toUpperCase();
    }

    private class ZebraCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
        ) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setBorder(new EmptyBorder(8, 14, 8, 14));
            label.setFont(AppFonts.lexendBold(column == 0 ? 18f : 15f));
            if (isSelected) {
                label.setBackground(BRAND_GREEN_SOFT);
                label.setForeground(TEXT_DARK);
            } else {
                label.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 251));
                label.setForeground(TEXT_DARK);
            }
            return label;
        }
    }

    private class TierBadgeRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
        ) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
            panel.setOpaque(true);
            panel.setBackground(isSelected ? BRAND_GREEN_SOFT : (row % 2 == 0 ? Color.WHITE : new Color(248, 250, 251)));

            String status = customers.get(row).getTrangThai();
            String tierText = "INACTIVE".equalsIgnoreCase(status) ? "SILVER" : "PLATINUM";
            JLabel chip = new JLabel(tierText);
            chip.setFont(AppFonts.lexendBold(11f));
            chip.setOpaque(true);
            chip.setBackground(new Color(229, 231, 235));
            chip.setForeground(new Color(55, 65, 81));
            chip.setBorder(new EmptyBorder(4, 10, 4, 10));
            panel.add(chip);
            return panel;
        }
    }

    private class StatusBadgeRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
        ) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
            panel.setOpaque(true);
            panel.setBackground(isSelected ? BRAND_GREEN_SOFT : (row % 2 == 0 ? Color.WHITE : new Color(248, 250, 251)));

            String status = String.valueOf(value);
            boolean active = "ACTIVE".equalsIgnoreCase(status);
            JLabel statusLabel = new JLabel((active ? "\u25CF  ACTIVE" : "\u25CF  INACTIVE"));
            statusLabel.setFont(AppFonts.lexendBold(12f));
            statusLabel.setForeground(active ? new Color(34, 197, 94) : new Color(156, 163, 175));
            panel.add(statusLabel);
            return panel;
        }
    }

    private class ActionsRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
        ) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
            panel.setOpaque(true);
            panel.setBackground(isSelected ? BRAND_GREEN_SOFT : (row % 2 == 0 ? Color.WHITE : new Color(248, 250, 251)));
            panel.add(actionPreviewButton("VIEW DETAILS", new Color(75, 85, 99), new Color(209, 213, 219), new Color(249, 250, 251)));
            panel.add(actionPreviewButton("DELETE\nINFORMATION", new Color(185, 28, 28), new Color(254, 205, 211), new Color(255, 251, 252)));
            return panel;
        }
    }

    private JButton actionPreviewButton(String text, Color fg, Color border, Color bg) {
        JButton btn = new JButton("<html><center>" + text.replace("\n", "<br>") + "</center></html>");
        btn.setEnabled(false);
        btn.setFont(AppFonts.lexendBold(11f));
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return btn;
    }

    public static JFrame createPreviewFrame() {
        JFrame frame = new JFrame("RENSTA - Quản lý khách hàng");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setContentPane(new ManageCustomerPreviewScreen());
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setMinimumSize(new Dimension(1280, 720));
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
