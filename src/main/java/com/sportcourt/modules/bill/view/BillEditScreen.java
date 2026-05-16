package com.sportcourt.modules.bill.view;

import com.sportcourt.common.style.AppDialog;
import com.sportcourt.modules.bill.controller.ManageBillController;
import com.sportcourt.modules.bill.dto.BillDetail;
import com.sportcourt.modules.bill.dto.BillResult;
import com.sportcourt.modules.bill.dto.CourtRentalItem;
import com.sportcourt.modules.bill.dto.ServiceItem;
import com.sportcourt.modules.branch.entity.Branch;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class BillEditScreen extends JPanel {

    private static final Color PAGE_BG      = Color.WHITE;
    private static final Color CARD_BG      = new Color(247, 248, 250);
    private static final Color PILL_GREEN   = new Color(34, 139, 58);
    private static final Color ACTION_GREEN = new Color(46, 204, 88);
    private static final Color ACCENT_GREEN = new Color(22, 130, 50);
    private static final Color BANNER_DARK  = new Color(33, 60, 33);
    private static final Color BORDER_BLUE  = new Color(29, 78, 216);
    private static final Color TITLE_DARK   = new Color(17, 24, 39);
    private static final Color MUTED        = new Color(107, 114, 128);
    private static final Color DIVIDER      = new Color(229, 231, 235);

    private static final int CARD_RADIUS = 18;

    private static final DateTimeFormatter DATE_FMT  = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat      MONEY_FMT = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    private final String maHD;
    private final Branch branch;
    private final Runnable onBack;
    private final Runnable onAddEquipment;
    private final Runnable onAddProduct;
    private final java.util.List<ServiceItem> extraServices;
    private final ManageBillController controller = new ManageBillController();

    public BillEditScreen(String maHD, Branch branch, Runnable onBack) {
        this(maHD, branch, onBack, null, null, null);
    }

    public BillEditScreen(String maHD, Branch branch, Runnable onBack, Runnable onAddEquipment) {
        this(maHD, branch, onBack, onAddEquipment, null, null);
    }

    public BillEditScreen(String maHD, Branch branch, Runnable onBack,
                          Runnable onAddEquipment, Runnable onAddProduct) {
        this(maHD, branch, onBack, onAddEquipment, onAddProduct, null);
    }

    public BillEditScreen(String maHD, Branch branch, Runnable onBack,
                          Runnable onAddEquipment, Runnable onAddProduct,
                          java.util.List<ServiceItem> extraServices) {
        this.maHD   = maHD;
        this.branch = branch;
        this.onBack = onBack;
        this.onAddEquipment = onAddEquipment;
        this.onAddProduct = onAddProduct;
        this.extraServices = extraServices == null
                ? java.util.List.of() : java.util.List.copyOf(extraServices);
        setLayout(new BorderLayout());
        setBackground(PAGE_BG);
        buildScreen();
    }

    /** Danh sách dịch vụ của hóa đơn + các mục vừa chọn thêm (chưa lưu DB). */
    private java.util.List<ServiceItem> allServices(BillDetail detail) {
        if (extraServices.isEmpty()) return detail.danhSachDichVu();
        java.util.List<ServiceItem> all = new java.util.ArrayList<>(detail.danhSachDichVu());
        all.addAll(extraServices);
        return all;
    }

    // ── Init ─────────────────────────────────────────────────────────────────

    private void buildScreen() {
        setOpaque(false);
        BillResult<BillDetail> res = controller.getDetail(maHD);
        if (!res.success() || res.data() == null) {
            JLabel err = new JLabel(res.message() == null ? "Không tải được hóa đơn." : res.message(), SwingConstants.CENTER);
            err.setForeground(Color.RED);
            add(err, BorderLayout.CENTER);
            return;
        }
        BillDetail detail = res.data();
        JPanel card = mainContainer();
        card.add(buildBody(detail), BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);
    }

    private JPanel mainContainer() {
        JPanel container = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50);
                g2.dispose();
            }
            @Override
            protected void paintChildren(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setClip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                super.paintChildren(g2);
                g2.dispose();
            }
        };
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(12, 0, 16, 0));
        return container;
    }

    // ── Top bar (logo + back) ────────────────────────────────────────────────

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(16, 24, 12, 24));

        JButton backBtn = new JButton("← Quay lại");
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        backBtn.setForeground(MUTED);
        backBtn.setBorderPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> { if (onBack != null) onBack.run(); });

        bar.add(backBtn, BorderLayout.EAST);
        return bar;
    }

    // ── Banner ───────────────────────────────────────────────────────────────

    private static final int BANNER_H = 210;

    private JPanel buildBanner() {
        java.net.URL heroUrl = getClass().getResource("/image/Stadium Hero.png");
        final Image heroImg  = heroUrl != null ? new ImageIcon(heroUrl).getImage() : null;

        JPanel banner = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                int w = getWidth(), h = getHeight();
                g2.setClip(new RoundRectangle2D.Float(0, 0, w, h, 26, 26));
                // Ảnh sân phủ toàn bộ banner
                if (heroImg != null) {
                    g2.drawImage(heroImg, 0, 0, w, h, this);
                } else {
                    g2.setColor(new Color(46, 125, 50));
                    g2.fillRect(0, 0, w, h);
                }
                // Phông màu tối phủ nửa dưới của hình để làm nổi bật chữ
                g2.setColor(BANNER_DARK);
                g2.fillRect(0, h / 2, w, h - h / 2);
                // Viền xanh biển
                g2.setClip(null);
                g2.setColor(BORDER_BLUE);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, w - 3, h - 3, 26, 26);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        banner.setOpaque(false);
        banner.setPreferredSize(new Dimension(0, BANNER_H));

        JLabel title = new JLabel("THÔNG TIN HÓA ĐƠN", SwingConstants.CENTER);
        title.setFont(new Font("Lexend", Font.BOLD, 26));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Mã đơn: " + maHD, SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(220, 240, 220));

        JPanel textBand = new JPanel(new GridBagLayout());
        textBand.setOpaque(false);
        textBand.setPreferredSize(new Dimension(0, BANNER_H / 2));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1;
        textBand.add(title, gbc);
        gbc.gridy = 1;
        gbc.insets = new Insets(2, 0, 0, 0);
        textBand.add(sub, gbc);

        banner.add(Box.createVerticalStrut(BANNER_H / 2), BorderLayout.NORTH);
        banner.add(textBand, BorderLayout.CENTER);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(PAGE_BG);
        wrap.setBorder(new EmptyBorder(12, 24, 8, 24));
        wrap.add(banner, BorderLayout.CENTER);
        return wrap;
    }

    // ── Body ─────────────────────────────────────────────────────────────────

    private JComponent buildBody(BillDetail detail) {
        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(8, 24, 24, 24));

        JPanel left  = buildLeftColumn(detail);
        JPanel right = buildRightColumn(detail);
        right.setPreferredSize(new Dimension(360, right.getPreferredSize().height));

        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0;
        g.fill = GridBagConstraints.BOTH;
        g.anchor = GridBagConstraints.NORTHWEST;
        g.gridx = 0; g.weightx = 1.0; g.weighty = 1.0;
        content.add(left, g);
        g.gridx = 1; g.weightx = 0; g.insets = new Insets(0, 20, 0, 0);
        content.add(right, g);

        JPanel topBar = buildTopBar();
        topBar.setAlignmentX(LEFT_ALIGNMENT);
        topBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

        JPanel banner = buildBanner();
        banner.setAlignmentX(LEFT_ALIGNMENT);
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, BANNER_H + 24));
        content.setAlignmentX(LEFT_ALIGNMENT);

        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBackground(PAGE_BG);
        page.add(topBar);
        page.add(banner);
        page.add(content);

        JScrollPane scroll = new JScrollPane(page);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(PAGE_BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    // ── Left column ──────────────────────────────────────────────────────────

    private JPanel buildLeftColumn(BillDetail detail) {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setOpaque(false);

        List<CourtRentalItem> courts = detail.danhSachThuesan();
        List<ServiceItem> services = allServices(detail);
        List<ServiceItem> equips = services.stream().filter(s -> s.maDC() != null).toList();
        List<ServiceItem> prods  = services.stream().filter(s -> s.maSP() != null).toList();

        col.add(buildCourtSection(courts));
        col.add(Box.createVerticalStrut(18));
        col.add(buildEquipSection(equips));
        col.add(Box.createVerticalStrut(18));
        col.add(buildProductSection(prods));
        col.add(Box.createVerticalGlue());
        return col;
    }

    private JPanel buildCourtSection(List<CourtRentalItem> courts) {
        JPanel card = card();
        card.add(sectionHeader("ĐẶT SÂN", "Thêm sân"));
        if (courts.isEmpty()) {
            card.add(emptyRow("Chưa có sân nào được đặt."));
        } else {
            for (CourtRentalItem item : courts) card.add(courtRow(item));
        }
        BigDecimal total = courts.stream()
                .map(c -> c.donGiaThue() == null ? BigDecimal.ZERO : c.donGiaThue())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        card.add(sectionFooter("Tổng tiền thuê sân", total));
        return card;
    }

    private JPanel buildEquipSection(List<ServiceItem> items) {
        JPanel card = card();
        Runnable addAction = onAddEquipment != null ? onAddEquipment
                : () -> AppDialog.showInfo(this, "Chức năng đang phát triển.");
        card.add(sectionHeader("DỤNG CỤ", "Thêm dụng cụ", addAction));
        if (items.isEmpty()) {
            card.add(emptyRow("Chưa có dụng cụ nào."));
        } else {
            for (ServiceItem item : items) card.add(serviceRow(item, "đ/giờ"));
        }
        BigDecimal total = items.stream()
                .map(s -> s.donGia() == null ? BigDecimal.ZERO : s.donGia().multiply(BigDecimal.valueOf(s.soLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        card.add(sectionFooter("Tổng tiền thuê dụng cụ", total));
        return card;
    }

    private JPanel buildProductSection(List<ServiceItem> items) {
        JPanel card = card();
        Runnable addAction = onAddProduct != null ? onAddProduct
                : () -> AppDialog.showInfo(this, "Chức năng đang phát triển.");
        card.add(sectionHeader("SẢN PHẨM", "Thêm sản phẩm", addAction));
        if (items.isEmpty()) {
            card.add(emptyRow("Chưa có sản phẩm nào."));
        } else {
            for (ServiceItem item : items) card.add(serviceRow(item, "đ/1"));
        }
        BigDecimal total = items.stream()
                .map(s -> s.donGia() == null ? BigDecimal.ZERO : s.donGia().multiply(BigDecimal.valueOf(s.soLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        card.add(sectionFooter("Tổng tiền sản phẩm", total));
        return card;
    }

    private JPanel courtRow(CourtRentalItem item) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(11, 18, 11, 18));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JLabel name = new JLabel(item.maSan());
        name.setFont(new Font("Segoe UI", Font.BOLD, 13));
        name.setForeground(TITLE_DARK);
        name.setPreferredSize(new Dimension(70, 0));

        String time = String.format("%02d:00 - %02d:00", item.gioBatDau(), item.gioKetThuc())
                + "  |  " + (item.ngayThue() != null ? item.ngayThue().format(DATE_FMT) : "--");
        JLabel timeLbl = new JLabel(time);
        timeLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        timeLbl.setForeground(ACCENT_GREEN);
        timeLbl.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel price = new JLabel(money(item.donGiaThue()) + "đ/giờ");
        price.setFont(new Font("Segoe UI", Font.BOLD, 13));
        price.setForeground(ACCENT_GREEN);

        row.add(name,    BorderLayout.WEST);
        row.add(timeLbl, BorderLayout.CENTER);
        row.add(price,   BorderLayout.EAST);
        return row;
    }

    private JPanel serviceRow(ServiceItem item, String priceSuffix) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(11, 18, 11, 18));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JLabel name = new JLabel(item.tenSanPham() != null ? item.tenSanPham() : "--");
        name.setFont(new Font("Segoe UI", Font.BOLD, 13));
        name.setForeground(TITLE_DARK);

        JLabel price = new JLabel(money(item.donGia()) + priceSuffix);
        price.setFont(new Font("Segoe UI", Font.BOLD, 13));
        price.setForeground(ACCENT_GREEN);

        row.add(name,                       BorderLayout.WEST);
        row.add(qtyControl(item.soLuong()), BorderLayout.CENTER);
        row.add(price,                      BorderLayout.EAST);
        return row;
    }

    private JPanel qtyControl(int qty) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        p.setOpaque(false);
        JLabel lbl = new JLabel("Số lượng: " + qty);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(MUTED);
        p.add(circleBtn("−"));
        p.add(lbl);
        p.add(circleBtn("+"));
        return p;
    }

    private JButton circleBtn(String sign) {
        JButton btn = new JButton(sign) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ACTION_GREEN);
                g2.fillOval(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(20, 20));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> AppDialog.showInfo(this, "Chức năng đang phát triển."));
        return btn;
    }

    // ── Right column ─────────────────────────────────────────────────────────

    private JPanel buildRightColumn(BillDetail detail) {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setOpaque(false);
        col.add(buildInfoCard(detail));
        col.add(Box.createVerticalStrut(18));
        col.add(buildPaymentCard(detail));
        col.add(Box.createVerticalGlue());
        return col;
    }

    private JPanel buildInfoCard(BillDetail detail) {
        JPanel card = card();
        card.add(rightHeader("THÔNG TIN CHI NHÁNH"));
        if (branch != null) {
            card.add(infoRow("Tên",     branch.tenChiNhanh()));
            card.add(infoRow("Địa chỉ", branch.diaChi()));
        } else {
            card.add(emptyRow("Không có thông tin chi nhánh."));
        }
        card.add(Box.createVerticalStrut(6));
        card.add(rightHeader("THÔNG TIN KHÁCH HÀNG"));
        card.add(infoRow("Tên",           detail.tenKhachHang()));
        card.add(infoRow("Số điện thoại", detail.sdtKhachHang()));
        card.add(Box.createVerticalStrut(8));
        return card;
    }

    private JPanel buildPaymentCard(BillDetail detail) {
        JPanel card = card();
        card.add(rightHeader("CHI TIẾT THANH TOÁN"));

        BigDecimal courtTotal = detail.danhSachThuesan().stream()
                .map(c -> c.donGiaThue() == null ? BigDecimal.ZERO : c.donGiaThue())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        List<ServiceItem> services = allServices(detail);
        BigDecimal equipTotal = services.stream()
                .filter(s -> s.maDC() != null)
                .map(s -> s.donGia() == null ? BigDecimal.ZERO : s.donGia().multiply(BigDecimal.valueOf(s.soLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal prodTotal = services.stream()
                .filter(s -> s.maSP() != null)
                .map(s -> s.donGia() == null ? BigDecimal.ZERO : s.donGia().multiply(BigDecimal.valueOf(s.soLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal subtotal = courtTotal.add(equipTotal).add(prodTotal);
        BigDecimal pct      = detail.giamGia() == null ? BigDecimal.ZERO : detail.giamGia();
        BigDecimal discount = subtotal.multiply(pct)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
        BigDecimal grand    = subtotal.subtract(discount);

        card.add(payRow("Tiền thuê sân",     courtTotal));
        card.add(payRow("Tiền thuê dụng cụ", equipTotal));
        card.add(payRow("Tiền sản phẩm",     prodTotal));
        card.add(payRow("Giảm giá",          discount));
        card.add(grandTotalRow(grand));
        card.add(payButton(detail));
        card.add(Box.createVerticalStrut(8));
        return card;
    }

    // ── Builders ─────────────────────────────────────────────────────────────

    private JPanel card() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), CARD_RADIUS, CARD_RADIUS);
                g2.dispose();
            }
        };
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(6, 0, 6, 0));
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return p;
    }

    private JPanel sectionHeader(String title, String btnLabel) {
        return sectionHeader(title, btnLabel,
                () -> AppDialog.showInfo(this, "Chức năng đang phát triển."));
    }

    private JPanel sectionHeader(String title, String btnLabel, Runnable action) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(14, 18, 10, 18));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(bookingIcon());
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Lexend", Font.BOLD, 15));
        lbl.setForeground(TITLE_DARK);
        left.add(lbl);

        JButton btn = pillBtn(btnLabel);
        btn.addActionListener(e -> action.run());

        header.add(left, BorderLayout.WEST);
        header.add(btn,  BorderLayout.EAST);
        return header;
    }

    private JComponent bookingIcon() {
        JComponent c = new JComponent() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int s = 20;
                g2.setColor(ACCENT_GREEN);
                g2.fillRoundRect(0, 1, s, s, 6, 6);
                g2.setColor(Color.WHITE);
                g2.fillRect(3, 6, s - 6, 2);
                g2.fillRect(5, 2, 2, 5);
                g2.fillRect(s - 7, 2, 2, 5);
                for (int yy = 10; yy < s - 1; yy += 4)
                    for (int xx = 4; xx < s - 3; xx += 4)
                        g2.fillRect(xx, yy, 2, 2);
                g2.dispose();
            }
        };
        c.setPreferredSize(new Dimension(20, 22));
        return c;
    }

    private JPanel rightHeader(String title) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(14, 18, 8, 18));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Lexend", Font.BOLD, 14));
        lbl.setForeground(TITLE_DARK);
        header.add(lbl, BorderLayout.WEST);
        return header;
    }

    private JPanel sectionFooter(String label, BigDecimal value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(0, 18, 0, 18),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(1, 0, 0, 0, DIVIDER),
                        new EmptyBorder(11, 0, 13, 0))));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(MUTED);

        JLabel val = new JLabel(money(value) + "đ");
        val.setFont(new Font("Lexend", Font.BOLD, 16));
        val.setForeground(TITLE_DARK);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        return row;
    }

    private JPanel infoRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(5, 18, 5, 18));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(MUTED);

        JLabel val = new JLabel(value == null || value.isBlank() ? "--" : value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 13));
        val.setForeground(TITLE_DARK);
        val.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        return row;
    }

    private JPanel payRow(String label, BigDecimal amount) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(6, 18, 6, 18));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(MUTED);

        JLabel val = new JLabel(money(amount) + "đ");
        val.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        val.setForeground(TITLE_DARK);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        return row;
    }

    private JPanel grandTotalRow(BigDecimal total) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(8, 18, 4, 18),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(1, 0, 0, 0, DIVIDER),
                        new EmptyBorder(12, 0, 12, 0))));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        JLabel lbl = new JLabel("TỔNG CỘNG");
        lbl.setFont(new Font("Lexend", Font.BOLD, 15));
        lbl.setForeground(TITLE_DARK);

        JLabel val = new JLabel(money(total) + "đ");
        val.setFont(new Font("Lexend", Font.BOLD, 17));
        val.setForeground(ACCENT_GREEN);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        return row;
    }

    private JComponent payButton(BillDetail detail) {
        String status = detail.trangThai() == null ? "" : detail.trangThai().trim();
        boolean unpaid = "CHƯA THANH TOÁN".equalsIgnoreCase(status);

        JButton btn = new JButton(unpaid ? "HOÀN TẤT" : "ĐÃ HOÀN TẤT") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? ACTION_GREEN : new Color(180, 188, 184));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Lexend", Font.BOLD, 15));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setEnabled(unpaid);
        btn.setCursor(new Cursor(unpaid ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        btn.addActionListener(e -> {
            BillResult<Void> r = controller.markAsPaid(maHD);
            if (r.success()) {
                AppDialog.showInfo(this, "Hoàn tất hóa đơn thành công!");
                if (onBack != null) onBack.run();
            } else {
                AppDialog.showError(this, r.message() == null ? "Hoàn tất hóa đơn thất bại." : r.message());
            }
        });

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(8, 18, 6, 18));
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        btn.setPreferredSize(new Dimension(0, 46));
        wrap.add(btn, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel emptyRow(String msg) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 6));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        JLabel lbl = new JLabel(msg);
        lbl.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lbl.setForeground(MUTED);
        row.add(lbl);
        return row;
    }

    // ── Utilities ────────────────────────────────────────────────────────────

    private static String money(BigDecimal value) {
        return MONEY_FMT.format(value == null ? BigDecimal.ZERO : value);
    }

    private JButton pillBtn(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(PILL_GREEN);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Lexend", Font.BOLD, 12));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(7, 16, 7, 16));
        return btn;
    }
}
