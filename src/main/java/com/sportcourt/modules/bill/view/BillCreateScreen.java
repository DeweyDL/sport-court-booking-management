package com.sportcourt.modules.bill.view;

import com.sportcourt.common.style.AppDialog;
import com.sportcourt.modules.bill.controller.ManageBillController;
import com.sportcourt.modules.bill.dto.BillDetail;
import com.sportcourt.modules.bill.dto.BillResult;
import com.sportcourt.modules.bill.dto.CourtRentalItem;
import com.sportcourt.modules.bill.dto.ServiceItem;
import com.sportcourt.modules.branch.entity.Branch;
import com.sportcourt.modules.payment.dto.PaymentQrInfo;
import com.sportcourt.modules.payment.service.PaymentService;
import com.sportcourt.modules.payment.service.PaymentServiceImpl;
import com.sportcourt.modules.payment.util.QrCodeRenderer;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class BillCreateScreen extends JPanel {

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
    private static final int NAME_COL_W = 200;  // cột tên — rộng cố định để mọi dòng thẳng hàng
    private static final int COURT_NAME_COL_W = 64;
    private static final int COURT_TIME_COL_W = 184;
    private static final int MID_COL_W = 240;   // bề rộng cột giữa (giờ-sân / số lượng) — dùng chung
    private static final int PRICE_COL_W = 112; // cột giá — rộng cố định
    private static final int ACTION_COL_W = 34;  // cột nút xóa cuối dòng — dùng chung để thẳng hàng

    private static final DateTimeFormatter DATE_FMT  = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat      MONEY_FMT = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    private final String maHD;
    private final Branch branch;
    private final Runnable onBack;
    private final Runnable onAddEquipment;
    private final Runnable onAddProduct;
    private final Runnable onAddCourt;
    private final Consumer<Boolean> onAddCourtWithMode;
    private final java.util.List<ServiceItem> extraServices;
    private final boolean readOnly;
    private final boolean allowBookingModeControls;
    private final ManageBillController controller = new ManageBillController();
    private JScrollPane scrollPane;   // giữ tham chiếu để khôi phục vị trí cuộn sau reload
    private JPanel contentPanel;      // vùng cột (trái/phải) — chỉ refresh phần này khi đổi số lượng
    private boolean advanceBookingMode;
    private boolean completed = false;
    private boolean navigatingToSubScreen = false;

    public BillCreateScreen(String maHD, Branch branch, Runnable onBack) {
        this(maHD, branch, onBack, null, null, null, null, false);
    }

    public BillCreateScreen(String maHD, Branch branch, Runnable onBack, Runnable onAddEquipment) {
        this(maHD, branch, onBack, onAddEquipment, null, null, null, false);
    }

    public BillCreateScreen(String maHD, Branch branch, Runnable onBack,
                          Runnable onAddEquipment, Runnable onAddProduct, Runnable onAddCourt) {
        this(maHD, branch, onBack, onAddEquipment, onAddProduct, onAddCourt, null, false);
    }

    public BillCreateScreen(String maHD, Branch branch, Runnable onBack,
                          Runnable onAddEquipment, Runnable onAddProduct, Consumer<Boolean> onAddCourtWithMode) {
        this(maHD, branch, onBack, onAddEquipment, onAddProduct, null, null, false, onAddCourtWithMode);
    }

    protected BillCreateScreen(String maHD, Branch branch, Runnable onBack,
                             Runnable onAddEquipment, Runnable onAddProduct, Runnable onAddCourt,
                             java.util.List<ServiceItem> extraServices, boolean readOnly) {
        this(maHD, branch, onBack, onAddEquipment, onAddProduct, onAddCourt, extraServices, readOnly, null);
    }

    private BillCreateScreen(String maHD, Branch branch, Runnable onBack,
                           Runnable onAddEquipment, Runnable onAddProduct, Runnable onAddCourt,
                           java.util.List<ServiceItem> extraServices, boolean readOnly,
                           Consumer<Boolean> onAddCourtWithMode) {
        this(maHD, branch, onBack, onAddEquipment, onAddProduct, onAddCourt,
                extraServices, readOnly, onAddCourtWithMode, false);
    }

    public BillCreateScreen(String maHD, Branch branch, Runnable onBack,
                          Runnable onAddEquipment, Runnable onAddProduct,
                          Consumer<Boolean> onAddCourtWithMode, boolean allowBookingModeControls) {
        this(maHD, branch, onBack, onAddEquipment, onAddProduct, null,
                null, false, onAddCourtWithMode, allowBookingModeControls);
    }

    private BillCreateScreen(String maHD, Branch branch, Runnable onBack,
                           Runnable onAddEquipment, Runnable onAddProduct, Runnable onAddCourt,
                           java.util.List<ServiceItem> extraServices, boolean readOnly,
                           Consumer<Boolean> onAddCourtWithMode, boolean allowBookingModeControls) {
        this.maHD   = maHD;
        this.branch = branch;
        this.onBack = onBack;
        this.onAddEquipment = onAddEquipment;
        this.onAddProduct = onAddProduct;
        this.onAddCourt = onAddCourt;
        this.onAddCourtWithMode = onAddCourtWithMode;
        this.extraServices = extraServices == null
                ? java.util.List.of() : java.util.List.copyOf(extraServices);
        this.readOnly = readOnly;
        this.allowBookingModeControls = allowBookingModeControls;
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
        setOpaque(true);
        setBackground(PAGE_BG);
        BillResult<BillDetail> res = controller.getDetail(maHD);
        if (!res.success() || res.data() == null) {
            JLabel err = new JLabel(res.message() == null ? "Không tải được hóa đơn." : res.message(), SwingConstants.CENTER);
            err.setForeground(Color.RED);
            add(err, BorderLayout.CENTER);
            return;
        }
        BillDetail detail = res.data();
        if (detail.tienCoc() != null && detail.tienCoc().compareTo(BigDecimal.ZERO) > 0) {
            advanceBookingMode = true;
        }
        JPanel card = mainContainer();
        card.add(buildBody(detail), BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);
    }

    private JPanel mainContainer() {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(true);
        container.setBackground(PAGE_BG);
        container.setBorder(null);
        return container;
    }

    // ── Top bar (logo + back) ────────────────────────────────────────────────

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(true);
        bar.setBackground(PAGE_BG);
        bar.setBorder(new EmptyBorder(24, 24, 12, 24));

        JButton backBtn = new JButton("← Quay lại");
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        backBtn.setForeground(MUTED);
        backBtn.setBorderPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> {
            if (!completed) {
                controller.softDelete(maHD);
            }
            if (onBack != null) onBack.run();
        });

        bar.add(backBtn, BorderLayout.EAST);
        return bar;
    }

    // ── Banner ───────────────────────────────────────────────────────────────

    private static final int BANNER_H = 210;
    private static final int BANNER_RADIUS = 40;

    private JPanel buildBanner() {
        final BufferedImage heroImg = loadHeroImage();

        JPanel banner = new JPanel(new GridLayout(2, 1));
        banner.setOpaque(false);
        banner.setBackground(PAGE_BG);
        banner.setPreferredSize(new Dimension(0, BANNER_H));

        JLabel imageLabel = new JLabel();
        imageLabel.setOpaque(false);
        imageLabel.setBackground(new Color(46, 125, 50));
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);

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

        JPanel darkBand = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BANNER_DARK);
                g2.fill(bottomRoundedClip(getWidth(), getHeight(), BANNER_RADIUS));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        darkBand.setOpaque(false);
        darkBand.setBackground(BANNER_DARK);
        darkBand.add(textBand, BorderLayout.CENTER);

        banner.add(imageLabel);
        banner.add(darkBand);
        imageLabel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                updateHeroIcon(imageLabel, heroImg, imageLabel.getWidth(), imageLabel.getHeight());
            }
        });
        SwingUtilities.invokeLater(() -> updateHeroIcon(imageLabel, heroImg, imageLabel.getWidth(), imageLabel.getHeight()));

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(true);
        wrap.setBackground(PAGE_BG);
        wrap.setBorder(new EmptyBorder(12, 24, 8, 24));
        wrap.setPreferredSize(new Dimension(0, BANNER_H + 20));
        wrap.setMinimumSize(new Dimension(0, BANNER_H + 20));
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, BANNER_H + 20));
        wrap.add(banner, BorderLayout.CENTER);
        return wrap;
    }

    private BufferedImage loadHeroImage() {
        java.net.URL heroUrl = getClass().getResource("/image/Stadium Hero.png");
        if (heroUrl == null) {
            return null;
        }
        try {
            return ImageIO.read(heroUrl);
        } catch (IOException ex) {
            return null;
        }
    }

    private void updateHeroIcon(JLabel imageLabel, BufferedImage heroImg, int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }
        String iconSize = width + "x" + height;
        if (iconSize.equals(imageLabel.getClientProperty("heroIconSize"))) {
            return;
        }
        imageLabel.putClientProperty("heroIconSize", iconSize);
        imageLabel.setOpaque(false);
        imageLabel.setIcon(createHeroIcon(heroImg, width, height));
    }

    private ImageIcon createHeroIcon(BufferedImage source, int width, int height) {
        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scaled.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setComposite(AlphaComposite.Clear);
        g2.fillRect(0, 0, width, height);
        g2.setComposite(AlphaComposite.SrcOver);
        g2.setClip(topRoundedClip(width, height, BANNER_RADIUS));
        g2.setColor(new Color(46, 125, 50));
        g2.fillRect(0, 0, width, height);

        if (source != null) {
            double scale = Math.max(width / (double) source.getWidth(), height / (double) source.getHeight());
            int drawW = (int) Math.ceil(source.getWidth() * scale);
            int drawH = (int) Math.ceil(source.getHeight() * scale);
            int x = (width - drawW) / 2;
            int y = (height - drawH) / 2;
            g2.drawImage(source, x, y, drawW, drawH, null);
        }
        g2.dispose();
        return new ImageIcon(scaled);
    }

    private Shape topRoundedClip(int width, int height, int radius) {
        int r = Math.min(radius, Math.min(width, height));
        Path2D.Float path = new Path2D.Float();
        path.moveTo(r, 0);
        path.lineTo(width - r, 0);
        path.quadTo(width, 0, width, r);
        path.lineTo(width, height);
        path.lineTo(0, height);
        path.lineTo(0, r);
        path.quadTo(0, 0, r, 0);
        path.closePath();
        return path;
    }

    private Shape bottomRoundedClip(int width, int height, int radius) {
        int r = Math.min(radius, Math.min(width, height));
        Path2D.Float path = new Path2D.Float();
        path.moveTo(0, 0);
        path.lineTo(width, 0);
        path.lineTo(width, height - r);
        path.quadTo(width, height, width - r, height);
        path.lineTo(r, height);
        path.quadTo(0, height, 0, height - r);
        path.closePath();
        return path;
    }

    // ── Body ─────────────────────────────────────────────────────────────────

    private JComponent buildBody(BillDetail detail) {
        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(true);
        content.setBackground(PAGE_BG);
        content.setBorder(new EmptyBorder(8, 24, 24, 24));
        content.setAlignmentX(LEFT_ALIGNMENT);
        this.contentPanel = content;
        populateContent(content, detail);

        JPanel topBar = buildTopBar();
        topBar.setAlignmentX(LEFT_ALIGNMENT);
        topBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

        JPanel banner = buildBanner();
        banner.setAlignmentX(LEFT_ALIGNMENT);
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, BANNER_H + 24));

        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setOpaque(true);
        page.setBackground(PAGE_BG);
        page.add(topBar);
        page.add(banner);
        page.add(content);

        JScrollPane scroll = new JScrollPane(page);
        scroll.setOpaque(true);
        scroll.setBackground(PAGE_BG);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setOpaque(true);
        scroll.getViewport().setBackground(PAGE_BG);
        scroll.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        this.scrollPane = scroll;
        return scroll;
    }

    private void populateContent(JPanel content, BillDetail detail) {
        content.removeAll();
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
    }

    /** Chỉ dựng lại phần cột (giữ banner/topbar/scroll) — không giật như reload cả màn. */
    private void refreshContent() {
        if (contentPanel == null) { return; }
        int scrollPos = scrollPane != null ? scrollPane.getVerticalScrollBar().getValue() : 0;
        BillResult<BillDetail> res = controller.getDetail(maHD);
        if (!res.success() || res.data() == null) {
            AppDialog.showError(this, res.message() != null ? res.message() : "Không tải lại được hóa đơn.");
            return;
        }
        populateContent(contentPanel, res.data());
        contentPanel.revalidate();
        contentPanel.repaint();
        SwingUtilities.invokeLater(() -> {
            if (scrollPane != null) scrollPane.getVerticalScrollBar().setValue(scrollPos);
        });
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
        Runnable addAction = onAddCourtWithMode != null ? () -> onAddCourtWithMode.accept(advanceBookingMode)
                : onAddCourt != null ? onAddCourt
                : () -> AppDialog.showInfo(this, "Chức năng đang phát triển.");
        card.add(courtSectionHeader(addAction));
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
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(9, 18, 9, 18));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel name = new JLabel(item.maSan());
        name.setFont(new Font("Segoe UI", Font.BOLD, 13));
        name.setForeground(TITLE_DARK);
        fixedSize(name, COURT_NAME_COL_W, 22);

        String time = String.format("%02d:00 - %02d:00", item.gioBatDau(), item.gioKetThuc())
                + " | " + (item.ngayThue() != null ? item.ngayThue().format(DATE_FMT) : "--");
        JLabel timeLbl = new JLabel(time);
        timeLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        timeLbl.setForeground(ACCENT_GREEN);
        timeLbl.setHorizontalAlignment(SwingConstants.LEFT);
        fixedSize(timeLbl, COURT_TIME_COL_W, 22);

        JLabel price = new JLabel(money(item.donGiaThue()) + "\u0111/gi\u1EDD");
        price.setFont(new Font("Segoe UI", Font.BOLD, 13));
        price.setForeground(ACCENT_GREEN);
        price.setHorizontalAlignment(SwingConstants.LEFT);
        fixedSize(price, PRICE_COL_W, 22);

        row.add(name, courtRowGbc(0));
        row.add(timeLbl, courtRowGbc(1));
        row.add(price, courtRowGbc(2));
        row.add(Box.createHorizontalGlue(), courtRowGbc(3));
        row.add(readOnly ? actionSpacer() : trashButton(() -> deleteCourt(item)), courtRowGbc(4));
        return row;
    }

    private void deleteCourt(CourtRentalItem item) {
        if (item.maCTHDTS() == null) {
            AppDialog.showError(this, "Khung giờ này chưa lưu, không thể xóa.");
            return;
        }
        BillResult<Void> r = controller.deleteCourtRental(item.maCTHDTS());
        if (!r.success()) {
            AppDialog.showError(this, r.message() != null ? r.message() : "Không thể xóa khung giờ.");
            return;
        }
        refreshContent();
    }

    private Component actionSpacer() {
        return Box.createRigidArea(new Dimension(ACTION_COL_W, 1));
    }

    private JButton trashButton(Runnable action) {
        JButton b = new JButton("🗑") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(254, 226, 226));
                g2.fillOval(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(ACTION_COL_W, ACTION_COL_W));
        b.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        b.setForeground(new Color(185, 28, 28));
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setToolTipText("Xóa khung giờ");
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> action.run());
        return b;
    }

    private JPanel serviceRow(ServiceItem item, String priceSuffix) {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(9, 18, 9, 18));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel name = new JLabel(item.tenSanPham() != null ? item.tenSanPham() : "--");
        name.setFont(new Font("Segoe UI", Font.BOLD, 13));
        name.setForeground(TITLE_DARK);
        name.setPreferredSize(new Dimension(NAME_COL_W, 22));

        JLabel price = new JLabel(money(item.donGia()) + priceSuffix);
        price.setFont(new Font("Segoe UI", Font.BOLD, 13));
        price.setForeground(ACCENT_GREEN);
        price.setHorizontalAlignment(SwingConstants.LEFT);
        price.setPreferredSize(new Dimension(PRICE_COL_W, 22));

        JPanel qty = qtyControl(item);
        qty.setPreferredSize(new Dimension(MID_COL_W, 30));

        // Mọi cột rộng CỐ ĐỊNH + cột đệm cuối hút phần dư
        // → mọi dòng (sân/dụng cụ/sản phẩm) thẳng hàng nhau tuyệt đối.
        row.add(name,         rowGbc(0));
        row.add(qty,          rowGbc(1));
        row.add(price, rowGbc(2));
        row.add(Box.createHorizontalGlue(), rowGbc(3));
        row.add(actionSpacer(), rowGbc(4));
        return row;
    }

    /** Ràng buộc GridBag dùng chung cho courtRow & serviceRow để các cột trùng khít. */
    private GridBagConstraints rowGbc(int gridx) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0;
        g.gridx = gridx;
        g.weightx = 0;
        g.fill = GridBagConstraints.NONE;
        switch (gridx) {
            case 0 -> { g.anchor = GridBagConstraints.WEST; }                       // tên
            case 1 -> { g.anchor = GridBagConstraints.WEST; }                       // giờ/số lượng
            case 2 -> { g.anchor = GridBagConstraints.WEST; g.insets = new Insets(0, 12, 0, 0); } // giá
            case 3 -> { g.weightx = 1.0; g.fill = GridBagConstraints.HORIZONTAL; } // đệm hút phần dư
            default -> { g.anchor = GridBagConstraints.CENTER; g.insets = new Insets(0, 10, 0, 0); } // nút
        }
        return g;
    }

    private GridBagConstraints courtRowGbc(int gridx) {
        GridBagConstraints g = rowGbc(gridx);
        if (gridx == 1) {
            g.insets = new Insets(0, 8, 0, 0);
        } else if (gridx == 2) {
            g.insets = new Insets(0, 12, 0, 0);
        }
        return g;
    }

    private void fixedSize(JComponent component, int width, int height) {
        Dimension size = new Dimension(width, height);
        component.setPreferredSize(size);
        component.setMinimumSize(size);
        component.setMaximumSize(size);
    }

    private JPanel qtyControl(ServiceItem item) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        JLabel lbl = new JLabel("Số lượng: " + item.soLuong());
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(MUTED);
        if (readOnly) {
            p.add(lbl);
            return p;
        }
        p.add(circleBtn("−", () -> changeQty(item, item.soLuong() - 1)));
        p.add(lbl);
        p.add(circleBtn("+", () -> changeQty(item, item.soLuong() + 1)));
        return p;
    }

    private void changeQty(ServiceItem item, int newQty) {
        if (item.maCTHDDV() == null) {
            AppDialog.showError(this, "Mục này chưa lưu, không thể chỉnh số lượng.");
            return;
        }
        BillResult<Void> r = controller.updateServiceItemQty(item.maCTHDDV(), newQty);
        if (!r.success()) {
            AppDialog.showError(this, r.message() != null ? r.message() : "Không thể cập nhật số lượng.");
            return;
        }
        refreshContent();
    }

    private JButton circleBtn(String sign, Runnable action) {
        JButton btn = new JButton(sign) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor("+".equals(sign) ? ACTION_GREEN : new Color(220, 53, 69));
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
        btn.addActionListener(e -> action.run());
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
            card.add(addressRow("Địa chỉ", branch.diaChi()));
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
        BigDecimal subtotal = detail.tongGiaTri() != null ? detail.tongGiaTri() : BigDecimal.ZERO;
        BigDecimal deposit = detail.tienCoc() == null ? BigDecimal.ZERO : detail.tienCoc();
        BigDecimal remaining = detail.tongTien() != null ? detail.tongTien() : BigDecimal.ZERO;

        card.add(payRow("Tiền thuê sân",     courtTotal));
        card.add(payRow("Tiền thuê dụng cụ", equipTotal));
        card.add(payRow("Tiền sản phẩm",     prodTotal));
        card.add(payRowBold("Tổng cộng",     subtotal));

        // Giảm giá % nhập tay
        BigDecimal chietKhau = detail.chietKhauHang() != null ? detail.chietKhauHang() : BigDecimal.ZERO;
        BigDecimal tierDiscount = courtTotal.multiply(chietKhau).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
        BigDecimal pct = detail.giamGia() == null ? BigDecimal.ZERO : detail.giamGia();
        BigDecimal pctDiscount = subtotal.subtract(tierDiscount).multiply(pct).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
        card.add(discountInputRow(detail.giamGia()));
        if (pctDiscount.compareTo(BigDecimal.ZERO) > 0 || pct.compareTo(BigDecimal.ZERO) > 0) {
            card.add(payRowDiscount("Tiền giảm (" + pct.toPlainString() + "%)", pctDiscount));
        }
        // Giảm giá hạng khách hàng
        card.add(payRowDiscount("Giảm giá hạng KH (" + chietKhau.stripTrailingZeros().toPlainString() + "%)", tierDiscount));

        card.add(payRow("Tiền cọc",          deposit));
        card.add(grandTotalRow(remaining));
        if (!readOnly) {
            card.add(payButton(detail));
        }
        card.add(Box.createVerticalStrut(8));
        return card;
    }

    private JPanel discountInputRow(BigDecimal currentPct) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(6, 18, 6, 18));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel lbl = new JLabel("% Giảm giá (thêm)");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(MUTED);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        right.setOpaque(false);
        JTextField txtPct = new JTextField(currentPct != null ? currentPct.stripTrailingZeros().toPlainString() : "0", 3);
        txtPct.setHorizontalAlignment(SwingConstants.RIGHT);
        JButton btnUpdate = new JButton("Cập nhật");
        btnUpdate.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnUpdate.setMargin(new Insets(2, 4, 2, 4));
        btnUpdate.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnUpdate.addActionListener(e -> {
            try {
                int pct = Integer.parseInt(txtPct.getText().trim());
                if (pct < 0 || pct > 100) throw new NumberFormatException();
                BillResult<Void> r = controller.updateDiscount(maHD, pct);
                if (r.success()) {
                    refreshContent();
                } else {
                    AppDialog.showError(this, r.message());
                }
            } catch (Exception ex) {
                AppDialog.showError(this, "Phần trăm giảm giá không hợp lệ (0-100).");
            }
        });
        if (readOnly) {
            txtPct.setEditable(false);
            btnUpdate.setVisible(false);
        }
        right.add(txtPct);
        right.add(btnUpdate);

        row.add(lbl, BorderLayout.WEST);
        row.add(right, BorderLayout.EAST);
        return row;
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

        header.add(left, BorderLayout.WEST);
        if (!readOnly) {
            JButton btn = pillBtn(btnLabel);
            btn.addActionListener(e -> {
                navigatingToSubScreen = true;
                action.run();
            });
            header.add(btn, BorderLayout.EAST);
        }
        return header;
    }

    private JPanel courtSectionHeader(Runnable addAction) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(14, 18, 10, 18));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(bookingIcon());
        JLabel lbl = new JLabel("ĐẶT SÂN");
        lbl.setFont(new Font("Lexend", Font.BOLD, 15));
        lbl.setForeground(TITLE_DARK);
        left.add(lbl);

        if (!readOnly && allowBookingModeControls) {
            left.add(modeBtn("Chơi ngay", false));
            left.add(modeBtn("Đặt trước", true));
        }

        header.add(left, BorderLayout.WEST);
        if (!readOnly) {
            JButton btn = pillBtn("Thêm sân");
            btn.addActionListener(e -> {
                navigatingToSubScreen = true;
                addAction.run();
            });
            header.add(btn, BorderLayout.EAST);
        }
        return header;
    }

    private JButton modeBtn(String text, boolean advanceMode) {
        boolean selected = advanceBookingMode == advanceMode;
        JButton btn = new JButton(text);
        btn.setFont(new Font("Lexend", Font.BOLD, 11));
        btn.setForeground(selected ? Color.WHITE : ACCENT_GREEN);
        btn.setBackground(selected ? ACCENT_GREEN : Color.WHITE);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_GREEN, 1, true),
                new EmptyBorder(5, 12, 5, 12)
        ));
        btn.addActionListener(e -> {
            advanceBookingMode = advanceMode;
            refreshContent();
        });
        return btn;
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

        JLabel val = new JLabel(money(value) + "\u0111");
        val.setFont(new Font("Lexend", Font.BOLD, 16));
        val.setForeground(TITLE_DARK);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        return row;
    }

    private JPanel addressRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(5, 18, 5, 18));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(MUTED);
        lbl.setVerticalAlignment(SwingConstants.TOP);

        JLabel val = new JLabel();
        val.setFont(new Font("Segoe UI", Font.BOLD, 13));
        val.setForeground(TITLE_DARK);
        val.setHorizontalAlignment(SwingConstants.RIGHT);
        val.setVerticalAlignment(SwingConstants.TOP);

        String displayValue = (value == null || value.isBlank()) ? "--" : value;
        val.setText("<html><div style='text-align: right; width: 170px;'>" + displayValue + "</div></html>");

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

        JLabel val = new JLabel(money(amount) + "\u0111");
        val.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        val.setForeground(TITLE_DARK);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        return row;
    }

    private JPanel payRowDiscount(String label, BigDecimal amount) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(6, 18, 6, 18));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(MUTED);

        String text = amount.compareTo(BigDecimal.ZERO) > 0 ? "-" + money(amount) + "\u0111" : "0\u0111";
        JLabel val = new JLabel(text);
        val.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        val.setForeground(new Color(220, 53, 69));

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        return row;
    }

    private JPanel payRowBold(String label, BigDecimal amount) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(6, 18, 6, 18),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(1, 0, 0, 0, DIVIDER),
                        new EmptyBorder(10, 0, 0, 0))));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(TITLE_DARK);

        JLabel val = new JLabel(money(amount) + "\u0111");
        val.setFont(new Font("Segoe UI", Font.BOLD, 13));
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
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel lbl = new JLabel("Còn lại");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(TITLE_DARK);

        JLabel val = new JLabel(money(total) + "\u0111");
        val.setFont(new Font("Segoe UI", Font.BOLD, 17));
        val.setForeground(ACCENT_GREEN);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        return row;
    }

    private JComponent payButton(BillDetail detail) {
        String status = detail.trangThai() == null ? "" : detail.trangThai().trim();
        boolean unpaid = "CHƯA THANH TOÁN".equalsIgnoreCase(status);
        String buttonText = unpaid ? "HOÀN TẤT" : "ĐÃ HOÀN TẤT";

        JButton btn = new JButton(buttonText) {
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
            completed = true;
            if (onBack != null) onBack.run();
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

    private boolean showPaymentDialog(BillDetail bill) {
        BigDecimal amount = bill.tongTien() == null ? BigDecimal.ZERO : bill.tongTien();
        int method = choosePaymentMethod("Thanh toán hóa đơn " + bill.maHD(), amount);
        if (method == 0) {
            return showCashPaymentDialog("Thanh toán hóa đơn " + bill.maHD(), amount, bill);
        }
        if (method == 1) {
            return showTransferPaymentDialog(bill);
        }
        return false;
    }

    private boolean showDepositPaymentDialog(BigDecimal deposit) {
        int method = choosePaymentMethod("Thanh toán cọc đặt trước", deposit);
        if (method == 0) {
            return showCashPaymentDialog("Thanh toán cọc đặt trước", deposit, null);
        }
        if (method == 1) {
            return showDepositTransferPaymentDialog(deposit);
        }
        return false;
    }

    private int choosePaymentMethod(String title, BigDecimal amount) {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 8));
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        JLabel amountLabel = new JLabel("Số tiền cần thu: " + money(amount) + "đ");
        amountLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        amountLabel.setForeground(ACCENT_GREEN);

        panel.add(titleLabel);
        panel.add(amountLabel);

        Object[] options = {"Tiền mặt", "Chuyển khoản", "Hủy"};
        return JOptionPane.showOptionDialog(
                this,
                panel,
                "Chọn phương thức thanh toán",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );
    }

    private boolean showCashPaymentDialog(String title, BigDecimal amount, BillDetail bill) {
        BigDecimal due = amount == null ? BigDecimal.ZERO : amount;
        boolean[] completedPayment = {false};

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(18, 18));
        root.setBackground(PAGE_BG);
        root.setBorder(new EmptyBorder(22, 22, 22, 22));

        JPanel details = new JPanel(new GridLayout(0, 1, 0, 6));
        details.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        details.add(titleLabel);

        if (bill != null) {
            JLabel invoice = new JLabel("Mã hóa đơn: " + bill.maHD());
            invoice.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            details.add(invoice);

            JLabel subtotal = new JLabel("Tổng tiền: " + money(bill.tongGiaTri()) + "đ");
            subtotal.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            details.add(subtotal);

            JLabel deposit = new JLabel("Tiền cọc: " + money(bill.tienCoc()) + "đ");
            deposit.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            details.add(deposit);
        }

        JLabel dueLabel = new JLabel("Số tiền cần thu: " + money(due) + "đ");
        dueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        dueLabel.setForeground(ACCENT_GREEN);
        details.add(dueLabel);
        root.add(details, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 0;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(0, 0, 10, 12);
        JLabel receivedLabel = new JLabel("Tiền khách đưa");
        receivedLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        form.add(receivedLabel, g);

        JTextField receivedField = new JTextField(money(due), 16);
        receivedField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        g.gridx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        form.add(receivedField, g);

        JLabel changeLabel = new JLabel("Tiền thừa: 0đ");
        changeLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        changeLabel.setForeground(TITLE_DARK);
        g.gridx = 0;
        g.gridy = 1;
        g.gridwidth = 2;
        g.fill = GridBagConstraints.NONE;
        g.weightx = 0;
        form.add(changeLabel, g);
        root.add(form, BorderLayout.CENTER);

        JButton doneBtn = new JButton("Hoàn tất");
        doneBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        doneBtn.setForeground(Color.WHITE);
        doneBtn.setBackground(ACTION_GREEN);
        doneBtn.setOpaque(true);
        doneBtn.setFocusPainted(false);
        doneBtn.setBorder(new EmptyBorder(10, 24, 10, 24));
        doneBtn.addActionListener(e -> {
            BigDecimal received = parseMoneyInput(receivedField.getText());
            if (received.compareTo(due) < 0) {
                AppDialog.showError(dialog, "Tiền khách đưa chưa đủ để hoàn tất thanh toán.");
                return;
            }
            completedPayment[0] = true;
            dialog.dispose();
        });

        Runnable updateCashState = () -> updateCashPaymentState(receivedField, changeLabel, doneBtn, due);
        receivedField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { updateCashState.run(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { updateCashState.run(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { updateCashState.run(); }
        });
        updateCashState.run();

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton cancelBtn = new JButton("Hủy");
        cancelBtn.addActionListener(e -> dialog.dispose());
        actions.add(cancelBtn);
        actions.add(doneBtn);
        root.add(actions, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(520, 360));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        return completedPayment[0];
    }

    private void updateCashPaymentState(JTextField receivedField, JLabel changeLabel, JButton doneBtn, BigDecimal due) {
        BigDecimal received = parseMoneyInput(receivedField.getText());
        BigDecimal change = received.subtract(due);
        boolean enough = change.compareTo(BigDecimal.ZERO) >= 0;
        doneBtn.setEnabled(enough);
        changeLabel.setForeground(enough ? TITLE_DARK : new Color(220, 53, 69));
        changeLabel.setText(enough
                ? "Tiền thừa: " + money(change.max(BigDecimal.ZERO)) + "đ"
                : "Còn thiếu: " + money(change.abs()) + "đ");
    }

    private BigDecimal parseMoneyInput(String text) {
        String digits = text == null ? "" : text.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(digits);
    }

    private boolean showTransferPaymentDialog(BillDetail bill) {
        PaymentService paymentService = new PaymentServiceImpl();
        PaymentQrInfo qr;
        BigDecimal amount = bill.tongTien() == null ? BigDecimal.ZERO : bill.tongTien();
        try {
            qr = paymentService.createPaymentLink(amount.intValue(), "TT " + bill.maHD());
        } catch (RuntimeException e) {
            AppDialog.showError(this, "Không tạo được mã thanh toán: " + e.getMessage());
            return false;
        }

        boolean[] paid = {false};
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Thanh toán hóa đơn", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setContentPane(buildPaymentContent(dialog, paymentService, qr, bill));
        dialog.pack();
        dialog.setMinimumSize(new Dimension(620, 480));
        dialog.setLocationRelativeTo(this);

        SwingWorker<Void, String> poller = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                while (!isCancelled()) {
                    String st = paymentService.checkStatus(qr.orderCode());
                    publish(st);
                    if ("ĐÃ THANH TOÁN".equals(st) || "ĐÃ HUỶ".equals(st) || "HẾT HẠN".equals(st)) {
                        break;
                    }
                    Thread.sleep(4000);
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                String st = chunks.get(chunks.size() - 1);
                if ("ĐÃ THANH TOÁN".equals(st)) {
                    paid[0] = true;
                    dialog.dispose();
                } else if ("HẾT HẠN".equals(st)) {
                    AppDialog.showInfo(dialog, "Mã thanh toán đã hết hạn.");
                    dialog.dispose();
                }
            }
        };
        poller.execute();

        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                poller.cancel(true);
            }
        });

        dialog.setVisible(true);
        return paid[0];
    }

    private JComponent buildPaymentContent(JDialog dialog, PaymentService paymentService, PaymentQrInfo qr, BillDetail bill) {
        JPanel root = new JPanel(new BorderLayout(18, 18));
        root.setBackground(new Color(248, 250, 252));
        root.setBorder(new EmptyBorder(22, 22, 22, 22));

        JPanel header = new JPanel(new GridLayout(0, 1, 0, 6));
        header.setOpaque(false);
        JLabel title = new JLabel("Thanh toán hóa đơn " + bill.maHD());
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JPanel detailsPanel = new JPanel(new GridLayout(0, 1, 0, 4));
        detailsPanel.setOpaque(false);
        detailsPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        BigDecimal subtotal = bill.tongGiaTri() == null ? BigDecimal.ZERO : bill.tongGiaTri();
        JLabel lblTong = new JLabel("Tổng tiền: " + money(subtotal) + "đ");
        lblTong.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        JLabel lblCoc = new JLabel("Tiền cọc: " + money(bill.tienCoc()) + "đ");
        lblCoc.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        detailsPanel.add(lblTong);
        detailsPanel.add(lblCoc);

        BigDecimal chietKhau = bill.chietKhauHang() != null ? bill.chietKhauHang() : BigDecimal.ZERO;
        BigDecimal courtTotal = bill.danhSachThuesan().stream()
                .map(c -> c.donGiaThue() == null ? BigDecimal.ZERO : c.donGiaThue())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tierDiscount = courtTotal.multiply(chietKhau).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
        BigDecimal pct = bill.giamGia() == null ? BigDecimal.ZERO : bill.giamGia();
        BigDecimal pctDiscount = subtotal.subtract(tierDiscount).multiply(pct).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);

        if (pctDiscount.compareTo(BigDecimal.ZERO) > 0 || pct.compareTo(BigDecimal.ZERO) > 0) {
            String pctText = pctDiscount.compareTo(BigDecimal.ZERO) > 0 ? "-" + money(pctDiscount) : money(pctDiscount);
            JLabel lblPctGiam = new JLabel("Giảm giá (" + pct.toPlainString() + "%): " + pctText + "đ");
            lblPctGiam.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            lblPctGiam.setForeground(new Color(220, 53, 69));
            detailsPanel.add(lblPctGiam);
        }

        String tierText = tierDiscount.compareTo(BigDecimal.ZERO) > 0 ? "-" + money(tierDiscount) : "0";
        JLabel lblTierGiam = new JLabel("Giảm giá hạng KH (" + chietKhau.stripTrailingZeros().toPlainString() + "%): " + tierText + "đ");
        lblTierGiam.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblTierGiam.setForeground(new Color(220, 53, 69));
        detailsPanel.add(lblTierGiam);

        JLabel lblConLai = new JLabel("Còn lại cần thanh toán: " + money(bill.tongTien()) + "đ");
        lblConLai.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblConLai.setForeground(new Color(16, 110, 0));
        detailsPanel.add(lblConLai);

        header.add(title);
        header.add(detailsPanel);
        root.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(18, 0));
        body.setOpaque(false);
        JLabel qrLabel = new JLabel(QrCodeRenderer.toIcon(qr.qrCodeData(), 210));
        qrLabel.setHorizontalAlignment(SwingConstants.CENTER);
        body.add(qrLabel, BorderLayout.WEST);

        JTextArea info = new JTextArea(
                "Ngân hàng: " + bankName(qr.bin()) + "\n"
                        + "Số tài khoản: " + qr.accountNumber() + "\n"
                        + "Chủ tài khoản: " + qr.accountName() + "\n"
                        + "Nội dung: " + qr.description()
        );
        info.setEditable(false);
        info.setOpaque(false);
        info.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        info.setLineWrap(true);
        info.setWrapStyleWord(true);
        body.add(info, BorderLayout.CENTER);
        root.add(body, BorderLayout.CENTER);

        return root;
    }

    private boolean showDepositTransferPaymentDialog(BigDecimal deposit) {
        PaymentService paymentService = new PaymentServiceImpl();
        PaymentQrInfo qr;
        try {
            qr = paymentService.createPaymentLink(deposit.intValue(), "Coc " + maHD);
        } catch (RuntimeException e) {
            AppDialog.showError(this, "Không tạo được mã thanh toán cọc: " + e.getMessage());
            return false;
        }

        boolean[] paid = {false};
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Thanh toán cọc", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setContentPane(buildDepositPaymentContent(dialog, paymentService, qr, deposit, paid));
        dialog.pack();
        dialog.setMinimumSize(new Dimension(620, 480));
        dialog.setLocationRelativeTo(this);

        // Polling SwingWorker to automatically check status
        SwingWorker<Void, String> poller = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                while (!isCancelled()) {
                    String st = paymentService.checkStatus(qr.orderCode());
                    publish(st);
                    if ("ĐÃ THANH TOÁN".equals(st) || "ĐÃ HUỶ".equals(st) || "HẾT HẠN".equals(st)) {
                        break;
                    }
                    Thread.sleep(4000);
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                String st = chunks.get(chunks.size() - 1);
                if ("ĐÃ THANH TOÁN".equals(st)) {
                    paid[0] = true;
                    dialog.dispose();
                } else if ("HẾT HẠN".equals(st)) {
                    AppDialog.showInfo(dialog, "Mã thanh toán đã hết hạn.");
                    dialog.dispose();
                }
            }
        };
        poller.execute();

        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                poller.cancel(true);
            }
        });

        dialog.setVisible(true);
        return paid[0];
    }

    private JComponent buildDepositPaymentContent(JDialog dialog,
                                                  PaymentService paymentService,
                                                  PaymentQrInfo qr,
                                                  BigDecimal deposit,
                                                  boolean[] paid) {
        JPanel root = new JPanel(new BorderLayout(18, 18));
        root.setBackground(PAGE_BG);
        root.setBorder(new EmptyBorder(22, 22, 22, 22));

        JPanel header = new JPanel(new GridLayout(0, 1, 0, 6));
        header.setOpaque(false);
        JLabel title = new JLabel("Thanh toán cọc đặt trước");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        JLabel amount = new JLabel("Tiền cọc: " + money(deposit) + "đ");
        amount.setFont(new Font("Segoe UI", Font.BOLD, 18));
        amount.setForeground(ACCENT_GREEN);
        header.add(title);
        header.add(amount);
        root.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(18, 0));
        body.setOpaque(false);
        JLabel qrLabel = new JLabel(QrCodeRenderer.toIcon(qr.qrCodeData(), 210));
        qrLabel.setHorizontalAlignment(SwingConstants.CENTER);
        body.add(qrLabel, BorderLayout.WEST);

        JTextArea info = new JTextArea(
                "Ngân hàng: " + bankName(qr.bin()) + "\n"
                        + "Số tài khoản: " + qr.accountNumber() + "\n"
                        + "Chủ tài khoản: " + qr.accountName() + "\n"
                        + "Nội dung: " + qr.description()
        );
        info.setEditable(false);
        info.setOpaque(false);
        info.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        info.setLineWrap(true);
        info.setWrapStyleWord(true);
        body.add(info, BorderLayout.CENTER);
        root.add(body, BorderLayout.CENTER);

        return root;
    }

    private String bankName(String bin) {
        if (bin == null) {
            return "";
        }
        return switch (bin) {
            case "970436" -> "Vietcombank";
            case "970415" -> "VietinBank";
            case "970418" -> "BIDV";
            case "970405" -> "Agribank";
            case "970422" -> "MB Bank";
            case "970407" -> "Techcombank";
            case "970416" -> "ACB";
            case "970432" -> "VPBank";
            case "970423" -> "TPBank";
            case "970403" -> "Sacombank";
            default -> bin;
        };
    }

    @Override
    public void removeNotify() {
        if (!completed && !navigatingToSubScreen) {
            controller.softDelete(maHD);
        }
        super.removeNotify();
    }

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
