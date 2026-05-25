package com.sportcourt.modules.bill.view;

import com.formdev.flatlaf.FlatLightLaf;
import com.sportcourt.common.style.AppDialog;
import com.sportcourt.common.style.AppFonts;
import com.sportcourt.common.style.CrudViewStyle;
import com.sportcourt.modules.auth.dto.UserSession;
import com.sportcourt.modules.auth.service.SessionManager;
import com.sportcourt.modules.bill.controller.ManageBillController;
import com.sportcourt.modules.branch.controller.BranchController;
import com.sportcourt.modules.branch.entity.Branch;
import com.sportcourt.modules.bill.dto.BillResult;
import com.sportcourt.modules.bill.dto.BillSummary;
import com.sportcourt.modules.bill.dto.ServiceItem;
import com.sportcourt.modules.bill.util.BillPdfExporter;
import com.sportcourt.modules.customer.controller.ManageCustomerController;
import com.sportcourt.modules.payment.dto.PaymentQrInfo;
import com.sportcourt.modules.payment.service.PaymentService;
import com.sportcourt.modules.payment.service.PaymentServiceImpl;
import com.sportcourt.modules.payment.util.QrCodeRenderer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ManageBillScreen extends JPanel implements Scrollable {
    private static final Color ALTERNATE_ROW_BG = new Color(248, 250, 252);
    private static final int HEADER_HEIGHT = 52;
    private static final int ROW_HEIGHT = 68;
    private static final int COLUMN_GAP = 14;

    private static final double W_BRANCH = 0.12;
    private static final double W_BILL_ID = 0.10;
    private static final double W_CUSTOMER = 0.15;
    private static final double W_STAFF = 0.12;
    private static final double W_DEPOSIT = 0.09;
    private static final double W_TOTAL_VALUE = 0.11;
    private static final double W_DATE = 0.09;
    private static final double W_STATUS = 0.11;
    private static final double W_ACTIONS = 0.13;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String STATUS_UNPAID = "CHƯA THANH TOÁN";
    private static final String STATUS_PAID = "ĐÃ THANH TOÁN";
    private static final String STATUS_CANCELLED = "ĐÃ HUỶ";

    private final ManageBillController controller = new ManageBillController();
    private final List<BillVm> bills = new ArrayList<>();
    private final boolean isOwner;

    private final CardLayout cardLayout  = new CardLayout();
    private final JPanel     cardPanel   = new JPanel(cardLayout);
    private final JPanel     editWrapper = new JPanel(new BorderLayout());

    private final JPanel tablePanel = new JPanel();
    private final JLabel footerLabel = new JLabel("Đang tải dữ liệu...");
    private final JTextField searchField = new JTextField(30);
    private final JPanel searchWrapper = new JPanel(new BorderLayout());
    private final JComboBox<String> cbSort = new JComboBox<>(new String[]{
            "Ngày tạo", "Tổng tiền", "Trạng thái"
    });
    private final JButton btnSortDir = new JButton("▲");

    private BillVm selectedBill;
    private boolean sortAscending = false;

    public ManageBillScreen() {
        AppFonts.register();
        isOwner = SessionManager.requireSession().isOwner();
        setLayout(new BorderLayout());
        setBackground(CrudViewStyle.PAGE_BACKGROUND);

        // Trang danh sách: padding chuẩn như các module khác.
        JPanel listPage = new JPanel(new BorderLayout());
        CrudViewStyle.applyPageDefaults(listPage);
        listPage.add(createPage(), BorderLayout.CENTER);

        // Màn edit: bung rộng tối đa, chỉ chừa lề nhỏ để thấy bo góc.
        editWrapper.setOpaque(true);
        editWrapper.setBackground(CrudViewStyle.PAGE_BACKGROUND);
        editWrapper.setBorder(new EmptyBorder(10, 10, 10, 10));

        cardPanel.setOpaque(true);
        cardPanel.setBackground(CrudViewStyle.PAGE_BACKGROUND);
        cardPanel.add(listPage, "list");
        cardPanel.add(editWrapper, "edit");
        add(cardPanel, BorderLayout.CENTER);
        CrudViewStyle.installResponsiveTypography(this);
        loadBills("");
    }


    private JPanel createPage() {
        JPanel page = new JPanel(new BorderLayout(0, 12));
        page.setOpaque(false);
        page.add(createHeaderSection(), BorderLayout.NORTH);
        page.add(createMainSection(), BorderLayout.CENTER);
        return page;
    }

    private JPanel createHeaderSection() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);

        JLabel title = new JLabel("QUẢN LÝ HÓA ĐƠN");
        title.setFont(new Font("Lexend", Font.BOLD, 30));
        title.setForeground(new Color(30, 31, 36));
        title.setBorder(new EmptyBorder(0, 20, 0, 0));

        JLabel subtitle = new JLabel("Tra cứu và quản lý trạng thái hóa đơn đặt sân.");
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
        container.setBorder(new EmptyBorder(12, 0, 16, 0));

        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setBackground(Color.WHITE);
        topSection.add(createToolbar());
        container.add(topSection, BorderLayout.NORTH);

        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
        tablePanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(tablePanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        CrudViewStyle.configureScrollPane(scrollPane);
        scrollPane.setColumnHeaderView(createTableHeader());
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
        toolbar.setBorder(new EmptyBorder(8, 20, 14, 20));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        left.setBackground(Color.WHITE);

        JLabel tableTitle = new JLabel("DANH SÁCH HÓA ĐƠN");
        tableTitle.setFont(new Font("Lexend", Font.BOLD, 22));

        JButton addBtn = miniButton("+ Thêm hóa đơn", new Color(228, 250, 226), new Color(16, 110, 0));
        addBtn.setFont(new Font("Lexend", Font.BOLD, 16));
        addBtn.setBorder(new EmptyBorder(6, 22, 6, 22));
        CrudViewStyle.applyToolbarButtonHeight(addBtn);
        addBtn.addActionListener(e -> {
            BranchSelectDialog.SelectResult sel = BranchSelectDialog.show(
                    this, new ManageCustomerController(), new BranchController());
            if (sel == null) return;
            UserSession session = SessionManager.requireSession();
            BillResult<String> res = controller.createEmptyBill(sel.customer().maKhachHang(), session.getEmployeeId());
            if (!res.success() || res.data() == null) {
                AppDialog.showError(this, res.message() != null ? res.message() : "Không thể tạo hóa đơn.");
                return;
            }
            showCreate(res.data(), sel.branch());
        });

        JButton refreshBtn = CrudViewStyle.createRefreshButton(e -> refreshBills());

        left.add(tableTitle);
        left.add(addBtn);
        left.add(refreshBtn);
        toolbar.add(left, BorderLayout.WEST);

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.X_AXIS));
        right.setBackground(Color.WHITE);
        right.setBorder(new EmptyBorder(0, 6, 0, 0));
        right.add(createSortWrapper());
        right.add(Box.createHorizontalStrut(10));
        right.add(createSearchFieldWithIcon());
        toolbar.add(right, BorderLayout.EAST);

        return toolbar;
    }

    private JPanel createSearchFieldWithIcon() {
        searchField.putClientProperty("JTextField.placeholderText", "Tìm theo mã HD hoặc khách hàng...");
        bindSearchListener();
        return CrudViewStyle.createSearchFieldWithIcon(searchWrapper, searchField, loadSearchIcon());
    }

    private JPanel createTableHeader() {
        JPanel header = new JPanel(new GridBagLayout());
        header.setBackground(new Color(248, 249, 250));
        header.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 1, 0, new Color(229, 231, 235)),
                new EmptyBorder(0, 24, 0, 24)
        ));
        header.setPreferredSize(new Dimension(1000, HEADER_HEIGHT));
        header.setMinimumSize(new Dimension(800, HEADER_HEIGHT));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, COLUMN_GAP);

        Color hBg = new Color(248, 249, 250);
        if (isOwner) {
            gbc.weightx = W_BRANCH;  header.add(cell(headerLabel("CHI NHÁNH"),    SwingConstants.LEFT,   hBg, 8, 8), gbc);
        }
        gbc.weightx = W_BILL_ID;     header.add(cell(headerLabel("MÃ HÓA ĐƠN"),    SwingConstants.LEFT,   hBg, 8, 8), gbc);
        gbc.weightx = W_CUSTOMER;    header.add(cell(headerLabel("KHÁCH HÀNG"),    SwingConstants.LEFT,   hBg, 8, 8), gbc);
        gbc.weightx = W_STAFF;       header.add(cell(headerLabel("NHÂN VIÊN"),     SwingConstants.LEFT,   hBg, 8, 8), gbc);
        gbc.weightx = W_DEPOSIT;     header.add(cell(headerLabel("TIỀN CỌC"),      SwingConstants.RIGHT,  hBg, 0, 8), gbc);
        gbc.weightx = W_TOTAL_VALUE; header.add(cell(headerLabel("TỔNG TIỀN"), SwingConstants.RIGHT,  hBg, 0, 8), gbc);
        gbc.weightx = W_DATE;        header.add(cell(headerLabel("NGÀY TẠO"),      SwingConstants.CENTER, hBg, 0, 8), gbc);
        gbc.weightx = W_STATUS;      header.add(cell(headerLabel("TRẠNG THÁI"),    SwingConstants.CENTER, hBg, 0, 8), gbc);
        gbc.weightx = W_ACTIONS;     gbc.insets = new Insets(0, 0, 0, 0);
        header.add(cell(headerLabel("THAO TÁC"), SwingConstants.CENTER, hBg, 0, 0), gbc);

        return header;
    }

    private JPanel createDataRow(BillVm bill, int rowIndex) {
        Color rowBg = rowIndex % 2 == 0 ? Color.WHITE : ALTERNATE_ROW_BG;

        JPanel row = new JPanel(new GridBagLayout());
        row.setBackground(rowBg);
        row.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(243, 244, 246)),
                new EmptyBorder(0, 24, 0, 24)
        ));
        row.setPreferredSize(new Dimension(1000, ROW_HEIGHT));
        row.setMinimumSize(new Dimension(800, ROW_HEIGHT));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, ROW_HEIGHT));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, COLUMN_GAP);

        String dateText = bill.getCreatedAt() != null ? bill.getCreatedAt().format(DATE_FMT) : "--";

        if (isOwner) {
            gbc.weightx = W_BRANCH;  row.add(cell(cellLabel(bill.getTenChiNhanh(), new Color(17, 24, 39)), SwingConstants.LEFT, rowBg, 8, 8), gbc);
        }
        gbc.weightx = W_BILL_ID;     row.add(cell(cellLabel(bill.getMaHD(), new Color(17, 24, 39)), SwingConstants.LEFT, rowBg, 8, 8), gbc);
        gbc.weightx = W_CUSTOMER;    row.add(cell(cellLabel(bill.getTenKhachHang(), new Color(17, 24, 39)), SwingConstants.LEFT, rowBg, 8, 8), gbc);
        gbc.weightx = W_STAFF;       row.add(cell(cellLabel(bill.getTenNhanVien(), new Color(75, 85, 99)), SwingConstants.LEFT, rowBg, 8, 8), gbc);
        gbc.weightx = W_DEPOSIT;     row.add(cell(cellLabel(formatCurrency(bill.getTienCoc()), new Color(17, 24, 39)), SwingConstants.RIGHT, rowBg, 0, 8), gbc);
        gbc.weightx = W_TOTAL_VALUE; row.add(cell(cellLabel(formatCurrency(bill.getTongGiaTri()), new Color(17, 24, 39)), SwingConstants.RIGHT, rowBg, 0, 8), gbc);
        gbc.weightx = W_DATE;        row.add(cell(cellLabel(dateText, new Color(107, 114, 128)), SwingConstants.CENTER, rowBg, 0, 8), gbc);
        gbc.weightx = W_STATUS;      row.add(cell(buildStatusPill(bill.getTrangThai()), SwingConstants.CENTER, rowBg, 0, 8), gbc);

        JPanel actionGroup = buildActionGroup(bill, rowBg);
        gbc.weightx = W_ACTIONS;
        gbc.insets = new Insets(0, 0, 0, 0);
        row.add(cell(actionGroup, SwingConstants.CENTER, rowBg, 0, 0), gbc);

        row.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                row.setBackground(new Color(249, 250, 251));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                row.setBackground(rowBg);
            }
        });
        return row;
    }

    private JPanel buildActionGroup(BillVm bill, Color rowBg) {
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.X_AXIS));
        group.setOpaque(false);

        String status = statusKey(bill.getTrangThai());

        if ("PAID".equals(status)) {
            // ĐÃ THANH TOÁN: In hóa đơn (green) | Xem chi tiết (xanh - chỉ xem)
            JButton printBtn = miniButton("In hóa đơn", new Color(228, 250, 226), new Color(16, 110, 0));
            printBtn.addActionListener(e -> printBill(bill));
            group.add(printBtn);
            group.add(Box.createHorizontalStrut(6));
            JButton detailBtn = miniButton("Xem chi tiết", CrudViewStyle.EDIT_BG, CrudViewStyle.EDIT_TEXT);
            detailBtn.addActionListener(e -> openDetailReadOnly(bill));
            group.add(detailBtn);

        } else if ("UNPAID".equals(status)) {
            // CHƯA THANH TOÁN: Xóa (red) | Chỉnh sửa (xanh)
            JButton deleteBtn = miniButton("Xóa", new Color(254, 226, 226), new Color(185, 28, 28));
            deleteBtn.addActionListener(e -> {
                selectedBill = bill;
                deleteSelectedBill();
            });
            group.add(deleteBtn);
            group.add(Box.createHorizontalStrut(6));
            JButton editBtn = miniButton("Chỉnh sửa", CrudViewStyle.EDIT_BG, CrudViewStyle.EDIT_TEXT);
            editBtn.addActionListener(e -> openEdit(bill));
            group.add(editBtn);

        } else {
            // ĐÃ HUỶ hoặc unknown: Xóa (red) | Xem chi tiết (xanh - chỉ xem)
            JButton deleteBtn = miniButton("Xóa", new Color(254, 226, 226), new Color(185, 28, 28));
            deleteBtn.addActionListener(e -> {
                selectedBill = bill;
                deleteSelectedBill();
            });
            group.add(deleteBtn);
            group.add(Box.createHorizontalStrut(6));
            JButton detailBtn = miniButton("Xem chi tiết", CrudViewStyle.EDIT_BG, CrudViewStyle.EDIT_TEXT);
            detailBtn.addActionListener(e -> openDetailReadOnly(bill));
            group.add(detailBtn);
        }

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(rowBg);
        wrapper.add(group);
        return wrapper;
    }

    private JLabel buildStatusPill(String trangThai) {
        String key = statusKey(trangThai);
        Color fg;
        String label;
        if ("PAID".equals(key)) {
            fg = CrudViewStyle.SUCCESS_TEXT;
            label = "Đã thanh toán";
        } else if ("CANCELLED".equals(key)) {
            fg = CrudViewStyle.DANGER_TEXT;
            label = "Đã huỷ";
        } else {
            fg = new Color(146, 100, 0);
            label = "Chưa thanh toán";
        }
        JLabel lbl = new JLabel(label);
        lbl.setForeground(fg);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return lbl;
    }

    /**
     * Nhận diện trạng thái từ DB bằng ký tự ASCII ổn định, không phụ thuộc encoding tiếng Việt.
     * - "CHƯA THANH TOÁN" → chữ đầu tiên là 'C' (ASCII, luôn ổn định)
     * - "ĐÃ THANH TOÁN"   → chứa "THANH" ở giữa (không bắt đầu bằng C)
     * - "ĐÃ HUỶ"          → chứa "HU" hoặc "HY" (ngắn, không có "THANH")
     */
    private String statusKey(String trangThai) {
        if (trangThai == null) return "UNPAID";
        String v = trangThai.trim().toUpperCase(java.util.Locale.ROOT);
        if (v.isEmpty()) return "UNPAID";
        // "CHƯA THANH TOÁN" là trạng thái DUY NHẤT bắt đầu bằng chữ C (ASCII)
        if (v.charAt(0) == 'C') return "UNPAID";
        // "ĐÃ THANH TOÁN" chứa chuỗi ASCII "THANH" (7 ký tự trước chứa space)
        if (v.contains("THANH")) return "PAID";
        // "ĐÃ HUỶ" chứa "HU" (bất kể ỶY encode ra sao)
        if (v.contains("HU") || v.contains("HY")) return "CANCELLED";
        return "UNKNOWN";
    }

    private JPanel createEmptyRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setBorder(new EmptyBorder(24, 26, 24, 26));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));
        JLabel msg = new JLabel("Không tìm thấy hóa đơn phù hợp.");
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        msg.setForeground(new Color(107, 114, 128));
        row.add(msg, BorderLayout.CENTER);
        return row;
    }

    private JPanel createSortWrapper() {
        cbSort.addActionListener(e -> {
            sortBills();
            renderTable();
        });
        btnSortDir.addActionListener(e -> {
            sortAscending = !sortAscending;
            CrudViewStyle.updateSortDirectionButton(btnSortDir, sortAscending);
            sortBills();
            renderTable();
        });
        CrudViewStyle.updateSortDirectionButton(btnSortDir, sortAscending);
        return CrudViewStyle.createSortWrapper(cbSort, btnSortDir);
    }

    private void loadBills(String keyword) {
        BillResult<List<BillSummary>> result = controller.searchBills(keyword);
        if (!result.success()) {
            AppDialog.showError(this, normalizeError("Không thể tải danh sách hóa đơn.", result.message()));
            return;
        }
        bills.clear();
        for (BillSummary s : result.data()) {
            bills.add(BillVm.fromSummary(s));
        }
        sortBills();
        renderTable();
    }

    private void renderTable() {
        tablePanel.removeAll();
        if (bills.isEmpty()) {
            tablePanel.add(createEmptyRow());
        } else {
            int index = 0;
            for (BillVm bill : bills) {
                tablePanel.add(createDataRow(bill, index++));
            }
        }
        footerLabel.setText("Hiển thị " + bills.size() + " hóa đơn");
        tablePanel.revalidate();
        tablePanel.repaint();
    }

    private void bindSearchListener() {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { refreshBills(); }
            @Override public void removeUpdate(DocumentEvent e) { refreshBills(); }
            @Override public void changedUpdate(DocumentEvent e) { refreshBills(); }
        });
    }

    private void sortBills() {
        String sortType = (String) cbSort.getSelectedItem();
        Comparator<BillVm> cmp;
        if ("Tổng tiền".equals(sortType)) {
            cmp = Comparator.comparing(b -> b.getTongTien() == null ? BigDecimal.ZERO : b.getTongTien());
        } else if ("Trạng thái".equals(sortType)) {
            cmp = Comparator.comparingInt(b -> statusOrder(b.getTrangThai()));
        } else {
            cmp = Comparator.comparing(b -> b.getCreatedAt() == null
                    ? java.time.LocalDateTime.MIN : b.getCreatedAt());
        }
        if (!sortAscending) {
            cmp = cmp.reversed();
        }
        bills.sort(cmp);
    }

    private int statusOrder(String status) {
        return switch (statusKey(status)) {
            case "UNPAID" -> 0;
            case "PAID" -> 1;
            case "CANCELLED" -> 2;
            default -> 3;
        };
    }

    private void refreshBills() {
        loadBills(searchField.getText().trim());
    }

    private void openEdit(BillVm bill) {
        showEdit(bill.getMaHD(), null);
    }

    private void openDetailReadOnly(BillVm bill) {
        showDetail(bill.getMaHD(), null);
    }

    private void deleteSelectedBill() {
        if (selectedBill == null) return;
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa hóa đơn " + selectedBill.getMaHD() + "?",
                "Xác nhận xóa",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.OK_OPTION) return;

        BillResult<Void> result = controller.softDelete(selectedBill.getMaHD());
        if (!result.success()) {
            AppDialog.showError(this, normalizeError("Không thể xóa hóa đơn.", result.message()));
            return;
        }
        AppDialog.showInfo(this, "Đã xóa hóa đơn.");
        refreshBills();
    }

    private void printBill(BillVm bill) {
        if (bill == null || bill.getMaHD() == null || bill.getMaHD().isBlank()) {
            AppDialog.showError(this, "Thiếu mã hóa đơn để xuất PDF.");
            return;
        }

        BillPdfExporter.exportFromView(controller, bill.getMaHD(), this);
    }

    private void showList() {
        cardLayout.show(cardPanel, "list");
        scrollToTop();
        refreshBills();
    }

    private void showEdit(String maHD, Branch branch) {
        showEdit(maHD, branch, false);
    }

    private void showEdit(String maHD, Branch branch, boolean creatingBill) {
        if (creatingBill) {
            showCreate(maHD, branch);
            return;
        }
        editWrapper.removeAll();
        editWrapper.add(new BillEditScreen(maHD, branch, this::showList,
                () -> showAddEquipment(maHD, branch, false),
                () -> showAddProduct(maHD, branch, false),
                advanceBooking -> showAddCourt(maHD, branch, advanceBooking, false)), BorderLayout.CENTER);
        editWrapper.revalidate();
        editWrapper.repaint();
        cardLayout.show(cardPanel, "edit");
        scrollToTop();
    }

    private void showCreate(String maHD, Branch branch) {
        editWrapper.removeAll();
        editWrapper.add(new BillCreateScreen(maHD, branch, this::showList,
                () -> showAddEquipment(maHD, branch, true),
                () -> showAddProduct(maHD, branch, true),
                advanceBooking -> showAddCourt(maHD, branch, advanceBooking, true),
                true), BorderLayout.CENTER);
        editWrapper.revalidate();
        editWrapper.repaint();
        cardLayout.show(cardPanel, "edit");
        scrollToTop();
    }

    private void showAddCourt(String maHD, Branch branch) {
        showAddCourt(maHD, branch, false);
    }

    private void showAddCourt(String maHD, Branch branch, boolean advanceBooking) {
        showAddCourt(maHD, branch, advanceBooking, false);
    }

    private void showAddCourt(String maHD, Branch branch, boolean advanceBooking, boolean creatingBill) {
        editWrapper.removeAll();
        editWrapper.add(new AddCourtScreen(maHD, () -> showEdit(maHD, branch, creatingBill), advanceBooking), BorderLayout.CENTER);
        editWrapper.revalidate();
        editWrapper.repaint();
        cardLayout.show(cardPanel, "edit");
        scrollToTop();
    }

    private void showDetail(String maHD, Branch branch) {
        editWrapper.removeAll();
        editWrapper.add(new BillDetailScreen(maHD, branch, this::showList), BorderLayout.CENTER);
        editWrapper.revalidate();
        editWrapper.repaint();
        cardLayout.show(cardPanel, "edit");
        scrollToTop();
    }

    private void persistAndReturn(String maHD, Branch branch, List<ServiceItem> sel) {
        persistAndReturn(maHD, branch, sel, false);
    }

    private void persistAndReturn(String maHD, Branch branch, List<ServiceItem> sel, boolean creatingBill) {
        if (sel != null && !sel.isEmpty()) {
            BillResult<Void> r = controller.addServiceItems(maHD, sel);
            if (!r.success()) {
                AppDialog.showError(this, r.message() != null ? r.message() : "Không thể thêm vào hóa đơn.");
            }
        }
        showEdit(maHD, branch, creatingBill);
    }

    private void showAddEquipment(String maHD, Branch branch) {
        showAddEquipment(maHD, branch, false);
    }

    private void showAddEquipment(String maHD, Branch branch, boolean creatingBill) {
        editWrapper.removeAll();
        editWrapper.add(new AddEquipmentScreen(
                () -> showEdit(maHD, branch, creatingBill),                          // Quay lại (không lưu)
                sel -> persistAndReturn(maHD, branch, sel, creatingBill)             // Hoàn tất → lưu DB
        ), BorderLayout.CENTER);
        editWrapper.revalidate();
        editWrapper.repaint();
        cardLayout.show(cardPanel, "edit");
        scrollToTop();
    }

    private void showAddProduct(String maHD, Branch branch) {
        showAddProduct(maHD, branch, false);
    }

    private void showAddProduct(String maHD, Branch branch, boolean creatingBill) {
        editWrapper.removeAll();
        editWrapper.add(new AddProductScreen(
                () -> showEdit(maHD, branch, creatingBill),                          // Quay lại (không lưu)
                sel -> persistAndReturn(maHD, branch, sel, creatingBill)             // Hoàn tất → lưu DB
        ), BorderLayout.CENTER);
        editWrapper.revalidate();
        editWrapper.repaint();
        cardLayout.show(cardPanel, "edit");
        scrollToTop();
    }

    private void scrollToTop() {
        SwingUtilities.invokeLater(() -> scrollRectToVisible(new Rectangle(0, 0, 1, 1)));
    }

    // ─── helpers ────────────────────────────────────────────────────────────

    private JLabel headerLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(107, 114, 128));
        return label;
    }

    private JLabel cellLabel(String text, Color fg) {
        JLabel label = new JLabel(valueOrDash(text));
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(fg);
        return label;
    }

    private JPanel cell(Component component, int alignment, Color bg, int leftPad, int rightPad) {
        if (component instanceof JLabel label) {
            label.setHorizontalAlignment(alignment);
        }
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(bg);
        panel.setOpaque(true);
        panel.setBorder(new EmptyBorder(0, leftPad, 0, rightPad));
        panel.add(component, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(0, ROW_HEIGHT));
        panel.setMinimumSize(new Dimension(0, ROW_HEIGHT));
        return panel;
    }

    private JButton miniButton(String text, Color bg, Color fg) {
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
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(4, 10, 4, 10));
        return btn;
    }

    private Icon loadSearchIcon() {
        URL iconUrl = getClass().getResource("/icon/search.png");
        if (iconUrl == null) return UIManager.getIcon("FileView.fileIcon");
        Image image = new ImageIcon(iconUrl).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH);
        return new ImageIcon(image);
    }

    private String formatCurrency(BigDecimal value) {
        if (value == null) return "0 VNĐ";
        return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(value) + " VNĐ";
    }

    private String valueOrDash(String text) {
        return text == null || text.isBlank() ? "--" : text;
    }

    private String normalizeError(String fallback, String detail) {
        if (detail == null || detail.isBlank()) return fallback;
        String normalized = detail.trim();
        return (normalized.endsWith(".") || normalized.endsWith("!") || normalized.endsWith("?"))
                ? normalized : normalized + ".";
    }

    // ─── Scrollable ──────────────────────────────────────────────────────────

    @Override
    public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) { return 16; }
    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) { return 64; }
    @Override
    public boolean getScrollableTracksViewportWidth() { return true; }
    @Override
    public boolean getScrollableTracksViewportHeight() { return true; }

    // ─── preview ─────────────────────────────────────────────────────────────

    public static JFrame createPreviewFrame() {
        JFrame frame = new JFrame("RENSTA - Quản lý hóa đơn");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setContentPane(new ManageBillScreen());
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setMinimumSize(new Dimension(1280, 720));
        frame.setLocationRelativeTo(null);
        return frame;
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(new FlatLightLaf()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> createPreviewFrame().setVisible(true));
    }
}
