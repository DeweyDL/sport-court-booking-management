package com.sportcourt.modules.revenue.view;

import com.sportcourt.common.style.AppFonts;
import com.sportcourt.common.style.CrudViewStyle;
import com.sportcourt.modules.branch.controller.BranchController;
import com.sportcourt.modules.branch.entity.Branch;
import com.sportcourt.modules.revenue.controller.RevenueController;
import com.sportcourt.modules.revenue.dto.BranchRevenueRow;
import com.sportcourt.modules.revenue.dto.CourtRevenueRow;
import com.sportcourt.modules.revenue.dto.RevenueCreateRequest;
import com.sportcourt.modules.revenue.dto.ServiceRevenueRow;
import com.sportcourt.modules.revenue.dto.RevenueChartData;
import com.sportcourt.modules.revenue.dto.RevenueRow;
import com.sportcourt.modules.revenue.dto.RevenueSearchCriteria;
import com.sportcourt.modules.revenue.dto.RevenueSummary;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RevenuePanel extends JPanel implements Scrollable {

    // ── palette ─────────────────────────────────────────────────────────────
    private static final Color PAGE_BG        = new Color(245, 247, 250);
    private static final Color WHITE          = Color.WHITE;
    private static final Color BORDER         = new Color(229, 231, 235);
    private static final Color MUTED          = new Color(107, 114, 128);
    private static final Color TEXT_DARK      = new Color(30,  31,  36);
    private static final Color GREEN_MAIN     = new Color(34, 197, 94);
    private static final Color GREEN_BG       = new Color(228, 250, 226);
    private static final Color GREEN_TEXT     = new Color(16, 110, 0);
    private static final Color BLUE_TEXT      = new Color(29,  78, 216);
    private static final Color RED_TEXT       = new Color(185, 28,  28);
    private static final Color HEADER_BG      = new Color(248, 249, 250);
    private static final Color ROW_BORDER_CLR = new Color(243, 244, 246);
    private static final Color ALT_ROW        = new Color(248, 250, 252);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ── controller ───────────────────────────────────────────────────────────
    private final RevenueController controller       = new RevenueController();
    private final BranchController  branchController = new BranchController();

    /** Parallel list to cbBranch items: index 0 = null (tất cả), index i = Branch */
    private final List<Branch> branchItems = new ArrayList<>();

    // ── filter state ─────────────────────────────────────────────────────────
    private final JComboBox<String> cbDateRange = new JComboBox<>(new String[]{
            "30 ngày qua", "7 ngày qua", "Hôm nay", "Tháng này", "Năm nay"
    });
    private final JComboBox<String> cbBranch = new JComboBox<>(new String[]{
            "Tất cả chi nhánh"
    });

    // ── summary labels ───────────────────────────────────────────────────────
    private final JLabel lblTotalRevenue = new JLabel("--");
    private final JLabel lblTotalOrders  = new JLabel("--");
    private final JLabel lblProfit       = new JLabel("--");

    // ── chart ────────────────────────────────────────────────────────────────
    private ChartPanel chartPanel;

    // ── branch performance lists (populated dynamically) ─────────────────────
    private final JPanel branchCourtListPanel = new JPanel();
    private final JPanel branchSvcListPanel   = new JPanel();
    private final JPanel branchPerfListPanel  = new JPanel();  // hiệu suất chi nhánh (chỉ khi "Tất cả")
    private JPanel branchChartsContainer;   // BoxLayout wrapper cho right-side charts
    private JPanel branchPerfCard;          // card hiệu suất chi nhánh (thêm/xóa động)

    // ── revenue table body ───────────────────────────────────────────────────
    private final JPanel tableBodyPanel = new JPanel();

    public RevenuePanel() {
        AppFonts.register();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(PAGE_BG);
        setOpaque(true);
        setDoubleBuffered(true);
        setBorder(new EmptyBorder(0, 0, 24, 0));

        add(buildHeaderSection());
        add(Box.createVerticalStrut(16));
        add(buildStatsRow());
        add(Box.createVerticalStrut(16));
        add(buildChartsRow());
        add(Box.createVerticalStrut(16));
        add(buildInvoiceSection());

        CrudViewStyle.installResponsiveTypography(this);
        loadData();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Header + filter bar
    // ═════════════════════════════════════════════════════════════════════════

    private JPanel buildHeaderSection() {
        JPanel wrap = new JPanel(new BorderLayout(0, 10));
        wrap.setOpaque(true);
        wrap.setBackground(PAGE_BG);

        // Title block
        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel title = new JLabel("BÁO CÁO DOANH THU");
        title.setFont(new Font("Lexend", Font.BOLD, 28));
        title.setForeground(TEXT_DARK);

        JLabel sub = new JLabel("Hiệu suất tài chính thời gian thực và giám sát chi nhánh.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sub.setForeground(MUTED);

        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(sub);

        // Filter bar (right-aligned)
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterBar.setOpaque(false);

        styleComboBox(cbDateRange);
        styleComboBox(cbBranch);

        JButton applyBtn = createPillButton("Áp dụng", GREEN_MAIN, WHITE, true);
        applyBtn.addActionListener(e -> refreshData());

        filterBar.add(cbDateRange);
        filterBar.add(cbBranch);
        filterBar.add(applyBtn);

        wrap.add(titleBlock, BorderLayout.WEST);
        wrap.add(filterBar, BorderLayout.EAST);
        return wrap;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Stats cards
    // ═════════════════════════════════════════════════════════════════════════

    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 16, 0));
        row.setOpaque(true);
        row.setBackground(PAGE_BG);

        row.add(buildStatCard("TỔNG DOANH THU",             lblTotalRevenue, GREEN_BG,                GREEN_TEXT,              "💰"));
        row.add(buildStatCard("DOANH THU DỊCH VỤ ĐA DỤNG", lblTotalOrders,  new Color(255, 237, 213), new Color(194, 65,  12), "🛎"));
        row.add(buildStatCard("DOANH THU THUÊ SÂN",         lblProfit,       new Color(239, 246, 255), BLUE_TEXT,               "🏟"));
        return row;
    }

    private JPanel buildStatCard(String labelText, JLabel valueLabel, Color iconBg, Color valueFg, String emoji) {
        JPanel card = roundedCard(WHITE, 16);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Top row: label + icon badge
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(MUTED);

        JLabel iconBadge = new JLabel(emoji, SwingConstants.CENTER);
        iconBadge.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        iconBadge.setOpaque(true);
        iconBadge.setBackground(iconBg);
        iconBadge.setPreferredSize(new Dimension(40, 40));
        iconBadge.setBorder(new EmptyBorder(4, 4, 4, 4));

        topRow.add(lbl,       BorderLayout.WEST);
        topRow.add(iconBadge, BorderLayout.EAST);

        valueLabel.setFont(new Font("Lexend", Font.BOLD, 24));
        valueLabel.setForeground(valueFg);

        card.add(topRow,      BorderLayout.NORTH);
        card.add(valueLabel,  BorderLayout.CENTER);
        return card;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Charts row
    // ═════════════════════════════════════════════════════════════════════════

    private JPanel buildChartsRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 16, 0));
        row.setOpaque(true);
        row.setBackground(PAGE_BG);

        row.add(buildBarChartCard());
        row.add(buildBranchChartsPanel());
        return row;
    }

    // ── Bar chart ─────────────────────────────────────────────────────────────

    private JPanel buildBarChartCard() {
        JPanel card = roundedCard(WHITE, 16);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Header
        JPanel cardHeader = new JPanel(new BorderLayout());
        cardHeader.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel chartLbl = new JLabel("Biểu đồ");
        chartLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        chartLbl.setForeground(MUTED);

        JLabel chartTitle = new JLabel("TỔNG DOANH THU");
        chartTitle.setFont(new Font("Lexend", Font.BOLD, 16));
        chartTitle.setForeground(TEXT_DARK);

        titleBlock.add(chartLbl);
        titleBlock.add(chartTitle);

        // Legend
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        legend.setOpaque(false);
        legend.add(buildLegendDot(GREEN_MAIN, "KỲ NÀY"));
        legend.add(buildLegendDot(new Color(200, 200, 200), "KỲ TRƯỚC"));

        cardHeader.add(titleBlock, BorderLayout.WEST);
        cardHeader.add(legend,    BorderLayout.EAST);

        chartPanel = new ChartPanel(buildBarChart()) {
            @Override public void paintComponent(Graphics g) {
                // Luôn fill nền trắng trước — ngăn JFreeChart vẽ thiếu background
                g.setColor(WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        chartPanel.setOpaque(true);
        chartPanel.setBackground(WHITE);
        chartPanel.setPreferredSize(new Dimension(0, 220));
        chartPanel.setMouseWheelEnabled(false);
        chartPanel.setPopupMenu(null);
        chartPanel.setDoubleBuffered(true);
        chartPanel.setMinimumDrawWidth(0);
        chartPanel.setMinimumDrawHeight(0);
        chartPanel.setMaximumDrawWidth(2000);
        chartPanel.setMaximumDrawHeight(1000);

        card.add(cardHeader,  BorderLayout.NORTH);
        card.add(chartPanel,  BorderLayout.CENTER);
        return card;
    }

    private JFreeChart buildBarChart() {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        String[] days     = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        double[] current  = {4.2, 3.1, 6.8, 8.5, 9.2, 11.4, 7.3};
        double[] previous = {3.5, 4.0, 5.2, 6.1, 7.8,  9.0, 6.0};
        for (int i = 0; i < days.length; i++) {
            ds.addValue(current[i],  "KỲ NÀY",   days[i]);
            ds.addValue(previous[i], "KỲ TRƯỚC", days[i]);
        }
        return applyChartStyle(ChartFactory.createBarChart(
                null, null, null, ds, PlotOrientation.VERTICAL, false, false, false));
    }

    private JFreeChart applyChartStyle(JFreeChart chart) {
        chart.setBackgroundPaint(WHITE);
        chart.setBorderVisible(false);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(WHITE);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(new Color(229, 231, 235));
        plot.setRangeGridlineStroke(new BasicStroke(1f));
        plot.setDomainGridlinesVisible(false);

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        domainAxis.setTickLabelPaint(MUTED);
        domainAxis.setAxisLineVisible(false);
        domainAxis.setCategoryMargin(0.3);
        domainAxis.setLowerMargin(0.02);
        domainAxis.setUpperMargin(0.02);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        rangeAxis.setTickLabelPaint(MUTED);
        rangeAxis.setAxisLineVisible(false);
        rangeAxis.setTickMarksVisible(false);

        BarRenderer renderer = new BarRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setMaximumBarWidth(0.08);
        renderer.setItemMargin(0.05);
        renderer.setSeriesPaint(0, GREEN_MAIN);
        renderer.setSeriesPaint(1, new Color(200, 200, 200));
        renderer.setDrawBarOutline(false);
        plot.setRenderer(renderer);

        return chart;
    }

    private JPanel buildLegendDot(Color color, String label) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);

        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        dot.setOpaque(false);
        dot.setPreferredSize(new Dimension(8, 8));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(MUTED);

        p.add(dot);
        p.add(lbl);
        return p;
    }

    // ── Branch performance lists ──────────────────────────────────────────────

    private JPanel buildBranchChartsPanel() {
        branchPerfCard = buildBranchListCard("HIỆU SUẤT CHI NHÁNH", branchPerfListPanel, new Color(34, 197, 94));

        branchChartsContainer = new JPanel();
        branchChartsContainer.setLayout(new BoxLayout(branchChartsContainer, BoxLayout.Y_AXIS));
        branchChartsContainer.setOpaque(false);
        // 2 card mặc định (sân + dịch vụ), branchPerfCard được thêm động khi cần
        branchChartsContainer.add(buildBranchListCard("BẢNG XẾP HẠNG DOANH THU SÂN",    branchCourtListPanel, new Color(29, 78, 216)));
        branchChartsContainer.add(Box.createVerticalStrut(16));
        branchChartsContainer.add(buildBranchListCard("BẢNG XẾP HẠNG DOANH THU DỊCH VỤ", branchSvcListPanel,   new Color(251, 146, 60)));
        return branchChartsContainer;
    }

    private JPanel buildBranchListCard(String title, JPanel listPanel, Color barColor) {
        JPanel card = roundedCard(WHITE, 16);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Lexend", Font.BOLD, 14));
        lbl.setForeground(TEXT_DARK);

        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);
        listPanel.putClientProperty("barColor", barColor);

        JLabel loading = new JLabel("Đang tải...");
        loading.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        loading.setForeground(MUTED);
        listPanel.add(loading);

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(WHITE);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        // Cố định chiều cao viewport — nội dung dài hơn sẽ tự xuất hiện scrollbar
        scroll.setPreferredSize(new Dimension(0, 160));
        scroll.setMinimumSize(new Dimension(0, 160));

        card.add(lbl,    BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private void updateBranchCharts(List<CourtRevenueRow> courts, List<ServiceRevenueRow> services) {
        populateCourtList(branchCourtListPanel, courts);
        populateServiceList(branchSvcListPanel, services);
    }

    /** Panel thuê sân: hiển thị doanh thu theo từng sân con (SC-1, SC-2...) */
    private void populateCourtList(JPanel listPanel, List<CourtRevenueRow> rows) {
        listPanel.removeAll();
        Color barColor = (Color) listPanel.getClientProperty("barColor");
        if (barColor == null) barColor = new Color(29, 78, 216);
        if (rows.isEmpty()) {
            JLabel empty = new JLabel("Không có dữ liệu.");
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            empty.setForeground(MUTED);
            listPanel.add(empty);
        } else {
            BigDecimal max = rows.stream()
                    .map(CourtRevenueRow::getDoanhThuThueSan)
                    .filter(v -> v != null)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ONE);
            if (max.compareTo(BigDecimal.ZERO) == 0) max = BigDecimal.ONE;

            for (int i = 0; i < rows.size(); i++) {
                CourtRevenueRow r = rows.get(i);
                BigDecimal value  = r.getDoanhThuThueSan();
                String name       = r.getMaSan();   // SC-1, SC-2...
                String amount     = formatCurrency(value);
                double ratio      = value == null ? 0 : value.doubleValue() / max.doubleValue();
                listPanel.add(buildBranchRow(name, amount, ratio, barColor));
                if (i < rows.size() - 1) listPanel.add(Box.createVerticalStrut(10));
            }
        }
        listPanel.setPreferredSize(new Dimension(0, rows.size() * 44 + Math.max(0, rows.size() - 1) * 10));
        listPanel.revalidate();
        listPanel.repaint();
    }

    /** Panel dịch vụ: hiển thị doanh thu theo từng sản phẩm / dụng cụ */
    private void populateServiceList(JPanel listPanel, List<ServiceRevenueRow> rows) {
        listPanel.removeAll();
        Color barColor = (Color) listPanel.getClientProperty("barColor");
        if (barColor == null) barColor = new Color(251, 146, 60);
        if (rows.isEmpty()) {
            JLabel empty = new JLabel("Không có dữ liệu.");
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            empty.setForeground(MUTED);
            listPanel.add(empty);
        } else {
            BigDecimal max = rows.stream()
                    .map(ServiceRevenueRow::getDoanhThu)
                    .filter(v -> v != null)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ONE);
            if (max.compareTo(BigDecimal.ZERO) == 0) max = BigDecimal.ONE;

            for (int i = 0; i < rows.size(); i++) {
                ServiceRevenueRow r = rows.get(i);
                BigDecimal value    = r.getDoanhThu();
                // Hiển thị "[Loại] Tên" để phân biệt sản phẩm và dụng cụ
                String displayName  = (r.getLoai() != null ? "[" + r.getLoai() + "] " : "")
                        + (r.getTenItem() != null ? r.getTenItem() : r.getMaItem());
                String amount       = formatCurrency(value);
                double ratio        = value == null ? 0 : value.doubleValue() / max.doubleValue();
                listPanel.add(buildBranchRow(displayName, amount, ratio, barColor));
                if (i < rows.size() - 1) listPanel.add(Box.createVerticalStrut(10));
            }
        }
        listPanel.setPreferredSize(new Dimension(0, rows.size() * 44 + Math.max(0, rows.size() - 1) * 10));
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel buildBranchRow(String name, String amount, double ratio, Color barColor) {
        JPanel row = new JPanel(new BorderLayout(8, 4));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 44));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JPanel labels = new JPanel(new BorderLayout());
        labels.setOpaque(false);

        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        nameLbl.setForeground(TEXT_DARK);

        JLabel amtLbl = new JLabel(amount);
        amtLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        amtLbl.setForeground(TEXT_DARK);
        amtLbl.setHorizontalAlignment(SwingConstants.RIGHT);

        labels.add(nameLbl, BorderLayout.WEST);
        labels.add(amtLbl,  BorderLayout.EAST);

        final Color finalBarColor = barColor;
        JPanel bar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(229, 231, 235));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                int filled = (int) (getWidth() * ratio);
                g2.setColor(finalBarColor);
                g2.fillRoundRect(0, 0, filled, getHeight(), getHeight(), getHeight());
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(0, 6));

        row.add(labels, BorderLayout.NORTH);
        row.add(bar,    BorderLayout.SOUTH);
        return row;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Invoice table
    // ═════════════════════════════════════════════════════════════════════════

    private JPanel buildInvoiceSection() {
        JPanel card = roundedCard(WHITE, 16);
        card.setLayout(new BorderLayout());

        card.add(buildRevenueToolbar(), BorderLayout.NORTH);

        tableBodyPanel.setLayout(new BoxLayout(tableBodyPanel, BoxLayout.Y_AXIS));
        tableBodyPanel.setBackground(WHITE);

        JScrollPane scroll = new JScrollPane(tableBodyPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(WHITE);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setColumnHeaderView(buildRevenueTableHeader());
        scroll.setPreferredSize(new Dimension(0, 380));
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildRevenueToolbar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(WHITE);
        bar.setBorder(new EmptyBorder(14, 20, 10, 20));

        JLabel title = new JLabel("Danh sách doanh thu");
        title.setFont(new Font("Lexend", Font.BOLD, 18));
        title.setForeground(TEXT_DARK);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        JButton addBtn = createPillButton("+ Thêm báo cáo", GREEN_MAIN, WHITE, true);
        addBtn.addActionListener(e -> openCreateDialog());

        right.add(addBtn);

        bar.add(title, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private void openCreateDialog() {
        try {
            String nextId = controller.generateNextId();
                RevenueCreateRequest req = RevenueCreateDialog.show(this, nextId, branchItems);
            if (req != null) {
                controller.create(req);
                loadData();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JButton iconButton(String icon) {
        JButton btn = new JButton(icon);
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        btn.setForeground(MUTED);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(4, 8, 4, 8));
        return btn;
    }

    private JPanel buildRevenueTableHeader() {
        JPanel header = new JPanel(new GridBagLayout());
        header.setBackground(HEADER_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 1, 0, BORDER),
                new EmptyBorder(0, 20, 0, 20)
        ));
        header.setPreferredSize(new Dimension(900, 48));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.weighty = 1.0;
        g.insets = new Insets(0, 0, 0, 8);

        String[] cols    = {"MÃ BÁO CÁO", "CHI NHÁNH", "NỘI DUNG", "NGÀY", "TỔNG DOANH THU"};
        double[] weights = {0.15,           0.20,         0.35,        0.15,   0.15};
        int[] aligns     = {SwingConstants.CENTER, SwingConstants.LEFT, SwingConstants.LEFT,
                            SwingConstants.CENTER, SwingConstants.RIGHT};

        for (int i = 0; i < cols.length; i++) {
            g.weightx = weights[i];
            if (i == cols.length - 1) g.insets = new Insets(0, 0, 0, 0);
            JLabel lbl = new JLabel(cols[i]);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lbl.setForeground(MUTED);
            header.add(createCell(lbl, aligns[i], HEADER_BG, 48), g);
        }
        return header;
    }

    private void renderTable(List<RevenueRow> rows) {
        tableBodyPanel.removeAll();
        if (rows.isEmpty()) {
            JPanel empty = new JPanel(new BorderLayout());
            empty.setBackground(WHITE);
            empty.setBorder(new EmptyBorder(24, 20, 24, 20));
            JLabel msg = new JLabel("Không có dữ liệu doanh thu.");
            msg.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            msg.setForeground(MUTED);
            empty.add(msg, BorderLayout.WEST);
            tableBodyPanel.add(empty);
        } else {
            for (int i = 0; i < rows.size(); i++) {
                tableBodyPanel.add(buildRevenueRow(rows.get(i), i));
            }
        }
        tableBodyPanel.revalidate();
        tableBodyPanel.repaint();
    }

    private JPanel buildRevenueRow(RevenueRow data, int index) {
        Color rowBg = index % 2 == 0 ? WHITE : ALT_ROW;
        JPanel row = new JPanel(new GridBagLayout());
        row.setBackground(rowBg);
        row.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, ROW_BORDER_CLR),
                new EmptyBorder(0, 20, 0, 20)
        ));
        row.setPreferredSize(new Dimension(900, 62));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.weighty = 1.0;
        g.insets = new Insets(0, 0, 0, 8);

        double[] weights = {0.15, 0.20, 0.35, 0.15, 0.15};

        String maDt        = nullSafe(data.getMaDt());
        String tenCn       = data.getTenChiNhanh() != null ? data.getTenChiNhanh() : nullSafe(data.getMaCn());
        String noiDung     = nullSafe(data.getNoiDung());
        String ngay        = data.getNgay() != null ? data.getNgay().format(DATE_FMT) : "--";
        String tongDt      = formatCurrency(data.getTongDoanhThu());

        String[] values = {maDt, tenCn, noiDung, ngay, tongDt};
        int[] aligns    = {SwingConstants.CENTER, SwingConstants.LEFT, SwingConstants.LEFT,
                           SwingConstants.CENTER, SwingConstants.RIGHT};

        for (int i = 0; i < values.length; i++) {
            g.weightx = weights[i];
            if (i == values.length - 1) g.insets = new Insets(0, 0, 0, 0);

            JLabel lbl = new JLabel(values[i]);
            if (i == 0) {
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lbl.setForeground(GREEN_TEXT);
            } else if (i == 4) {
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lbl.setForeground(BLUE_TEXT);
            } else {
                lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                lbl.setForeground(new Color(17, 24, 39));
            }
            row.add(createCell(lbl, aligns[i], rowBg, 62), g);
        }

        row.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { row.setBackground(new Color(249, 250, 251)); }
            @Override public void mouseExited(java.awt.event.MouseEvent e)  { row.setBackground(rowBg); }
        });
        return row;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Data loading
    // ═════════════════════════════════════════════════════════════════════════

    private RevenueSearchCriteria buildCriteria() {
        RevenueSearchCriteria c = new RevenueSearchCriteria();

        // Date range
        LocalDate today = LocalDate.now();
        switch ((String) cbDateRange.getSelectedItem()) {
            case "7 ngày qua"  -> { c.setFromDate(today.minusDays(6));  c.setToDate(today); }
            case "Hôm nay"     -> { c.setFromDate(today);               c.setToDate(today); }
            case "Tháng này"   -> { c.setFromDate(today.withDayOfMonth(1)); c.setToDate(today); }
            case "Năm nay"     -> { c.setFromDate(today.withDayOfYear(1));  c.setToDate(today); }
            default            -> { c.setFromDate(today.minusDays(29)); c.setToDate(today); } // 30 ngày qua
        }

        // Branch (index 0 = tất cả, index i → branchItems.get(i).maCn())
        int idx = cbBranch.getSelectedIndex();
        if (idx > 0 && idx < branchItems.size() && branchItems.get(idx) != null) {
            c.setMaCn(branchItems.get(idx).maCn());
        }
        return c;
    }

    private void loadData() {
        RevenueSearchCriteria criteria = buildCriteria();
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            RevenueSummary          summary;
            RevenueChartData        chart;
            List<BranchRevenueRow>  branches;
            List<CourtRevenueRow>   courts;
            List<ServiceRevenueRow> services;
            List<RevenueRow>        rows;
            List<Branch>            allBranches;
            String                  error;

            @Override
            protected Void doInBackground() {
                try {
                    allBranches = branchController.getBranchList("");
                    summary     = controller.getSummary(criteria);
                    chart       = controller.getChartData(criteria);
                    branches    = controller.getBranchRevenue(criteria);
                    courts      = controller.getCourtRevenue(criteria);
                    services    = controller.getServiceRevenue(criteria);
                    rows        = controller.search(criteria);
                } catch (Exception ex) {
                    error = ex.getMessage();
                }
                return null;
            }

            @Override
            protected void done() {
                if (error != null) {
                    JOptionPane.showMessageDialog(RevenuePanel.this, error, "Lỗi tải dữ liệu", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // Populate branch dropdown (only on first load)
                if (allBranches != null && branchItems.isEmpty()) {
                    branchItems.add(null); // index 0 = "Tất cả chi nhánh"
                    for (Branch b : allBranches) {
                        branchItems.add(b);
                        cbBranch.addItem(b.tenChiNhanh());
                    }
                }
                // Stats
                lblTotalRevenue.setText(formatCurrency(summary.getTongDoanhThu()));
                lblTotalOrders.setText(formatCurrency(summary.getDoanhThuDichVu()));
                lblProfit.setText(formatCurrency(summary.getDoanhThuThueSan()));
                // Chart
                if (chartPanel != null) {
                    chartPanel.setChart(buildBarChartFrom(chart));
                    chartPanel.repaint();
                }
                // Branch performance
                updateBranchCharts(courts, services);
                // Table
                renderTable(rows);
                // Force repaint để tránh artifact khi SwingWorker cập nhật component
                revalidate();
                repaint();
            }
        };
        worker.execute();
    }

    private void refreshData() {
        loadData();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Chart from real data
    // ═════════════════════════════════════════════════════════════════════════

    private JFreeChart buildBarChartFrom(RevenueChartData data) {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        String[] labels   = data.getLabels();
        double[] current  = data.getCurrentValues();
        double[] previous = data.getPreviousValues();
        for (int i = 0; i < labels.length; i++) {
            ds.addValue(current[i],  "KỲ NÀY",   labels[i]);
            ds.addValue(previous[i], "KỲ TRƯỚC", labels[i]);
        }
        return applyChartStyle(ChartFactory.createBarChart(
                null, null, null, ds, PlotOrientation.VERTICAL, false, false, false));
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Helpers
    // ═════════════════════════════════════════════════════════════════════════

    private static JPanel roundedCard(Color bg, int arc) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Xóa toàn bộ vùng bằng nền parent trước khi vẽ bo góc
                g2.setColor(getParent() != null ? getParent().getBackground() : PAGE_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                g2.dispose();
            }
            @Override protected void paintChildren(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setClip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc));
                super.paintChildren(g2);
                g2.dispose();
            }
        };
        card.setOpaque(true);
        card.setDoubleBuffered(true);
        return card;
    }

    private static String formatCurrency(BigDecimal value) {
        if (value == null) return "0đ";
        return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(value) + "đ";
    }

    private static String nullSafe(String value) {
        return value == null || value.isBlank() ? "--" : value;
    }

    private static void styleComboBox(JComboBox<String> cb) {
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cb.setBackground(WHITE);
        cb.setFocusable(false);
        cb.putClientProperty("JComponent.roundRect", true);
        cb.putClientProperty("JComponent.arc", 999);
    }

    private static JButton createPillButton(String text, Color bg, Color fg, boolean bold) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? bg.darker() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, 13));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        return btn;
    }

    /**
     * Wraps a label in a GridBagLayout panel so it aligns flush to the
     * correct edge (anchor) without stretching the label itself.
     * This mirrors the createFlexibleCell pattern used in AccountManagementPanel.
     */
    private static JPanel createCell(JLabel lbl, int alignment, Color bg, int height) {
        lbl.setHorizontalAlignment(alignment);
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(bg);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx  = 0;
        gbc.gridy  = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill   = GridBagConstraints.NONE;
        gbc.anchor = alignment == SwingConstants.LEFT  ? GridBagConstraints.WEST  :
                     alignment == SwingConstants.RIGHT ? GridBagConstraints.EAST  :
                                                         GridBagConstraints.CENTER;
        panel.add(lbl, gbc);
        panel.setPreferredSize(new Dimension(0, height));
        panel.setMinimumSize(new Dimension(0, height));
        return panel;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Scrollable
    // ═════════════════════════════════════════════════════════════════════════

    @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
    @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 64; }
    @Override public boolean getScrollableTracksViewportWidth()  { return true; }
    @Override public boolean getScrollableTracksViewportHeight() { return false; }
}
