package com.sportcourt.modules.revenue.view;

import com.sportcourt.modules.branch.entity.Branch;
import com.sportcourt.modules.revenue.controller.RevenueController;
import com.sportcourt.modules.revenue.dto.RevenueCreateRequest;
import com.sportcourt.modules.revenue.dto.RevenueSearchCriteria;
import com.sportcourt.modules.revenue.dto.RevenueSummary;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;

final class RevenueCreateDialog {

    private static final Color DIALOG_BG   = new Color(248, 249, 252);
    private static final Color CARD_BG     = Color.WHITE;
    private static final Color BRAND_GREEN = new Color(16, 110, 0);
    private static final Color GREEN_MAIN  = new Color(34, 197, 94);
    private static final Color TEXT_DARK   = new Color(30, 41, 59);
    private static final Color TEXT_MUTED  = new Color(100, 116, 139);
    private static final Color BORDER_CLR  = new Color(203, 213, 225);
    private static final Color READONLY_BG = new Color(241, 245, 249);
    private static final Color RESULT_BG   = new Color(240, 253, 244);
    private static final Color BLUE_TEXT   = new Color(29, 78, 216);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String[] LOAI_LABELS = {"Theo ngày", "Theo tuần", "Theo tháng", "Theo năm"};
    private static final String[] LOAI_CODES  = {"NGAY",      "TUAN",      "THANG",      "NAM"};

    private RevenueCreateDialog() {}

    static RevenueCreateRequest show(Component parent, String nextId, List<Branch> branches, Branch selectedBranch) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, "Lập báo cáo doanh thu",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(DIALOG_BG);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        dialog.setContentPane(root);

        // ── Header ───────────────────────────────────────────────────────────
        JLabel title = new JLabel("Lập báo cáo doanh thu");
        title.setFont(new Font("Lexend", Font.BOLD, 22));
        title.setForeground(TEXT_DARK);

        JLabel subtitle = new JLabel("Chọn kỳ báo cáo — hệ thống tự tính toán từ hóa đơn.");
        subtitle.setFont(new Font("Lexend", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_MUTED);
        subtitle.setBorder(new EmptyBorder(4, 0, 0, 0));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(title);
        header.add(subtitle);
        root.add(header, BorderLayout.NORTH);

        // ── Form ─────────────────────────────────────────────────────────────
        JTextField txtMaDt = createReadOnlyField(nextId);

        List<Branch> realBranches = branches.stream().filter(b -> b != null).toList();

        // Chi nhánh: hiển thị read-only, lấy từ header
        String branchDisplay = selectedBranch != null ? selectedBranch.tenChiNhanh() : "Tất cả chi nhánh";
        JTextField txtBranchLocked = createReadOnlyField(branchDisplay);

        JComboBox<String> cbLoai = new JComboBox<>(LOAI_LABELS);
        cbLoai.setFont(new Font("Lexend", Font.PLAIN, 14));
        cbLoai.setBackground(CARD_BG);

        JTextField txtNgay  = createReadOnlyField(LocalDate.now().format(DATE_FMT));
        JTextField txtThang = createReadOnlyField(String.format("%02d/%d", LocalDate.now().getMonthValue(), LocalDate.now().getYear()));
        JTextField txtNam   = createReadOnlyField(String.valueOf(LocalDate.now().getYear()));

        JPanel pnlNgay  = createField("Ngày (dd/MM/yyyy)", txtNgay);
        JPanel pnlThang = createField("Tháng/Năm (MM/yyyy)", txtThang);
        JPanel pnlNam   = createField("Năm (yyyy)", txtNam);

        JTextField txtNoiDung = new JTextField();

        // Kết quả tính toán (read-only)
        JLabel lblDtThueSan = new JLabel("--");
        JLabel lblDtDichVu  = new JLabel("--");
        JLabel lblTongDt    = new JLabel("--");
        lblDtThueSan.setFont(new Font("Lexend", Font.BOLD, 16));
        lblDtThueSan.setForeground(BLUE_TEXT);
        lblDtDichVu.setFont(new Font("Lexend", Font.BOLD, 16));
        lblDtDichVu.setForeground(new Color(194, 65, 12));
        lblTongDt.setFont(new Font("Lexend", Font.BOLD, 18));
        lblTongDt.setForeground(BRAND_GREEN);

        JPanel resultPanel = buildResultPanel(lblDtThueSan, lblDtDichVu, lblTongDt);
        resultPanel.setVisible(false);

        final BigDecimal[] calcValues = new BigDecimal[3]; // [thueSan, dichVu, tong]

        // ── Assemble form ───────────────────────────────────────────────────
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(CARD_BG);
        form.setBorder(new EmptyBorder(18, 18, 18, 18));
        form.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(createField("Mã báo cáo", txtMaDt));
        form.add(Box.createVerticalStrut(12));
        form.add(createField("Chi nhánh", txtBranchLocked));
        form.add(Box.createVerticalStrut(12));
        form.add(createComboField("Loại kỳ", cbLoai));
        form.add(Box.createVerticalStrut(12));
        form.add(pnlNgay);
        form.add(pnlThang);
        form.add(pnlNam);
        form.add(Box.createVerticalStrut(12));

        JButton calcBtn = createPillButton("Tính toán doanh thu", GREEN_MAIN, Color.WHITE);
        calcBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        calcBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        form.add(calcBtn);
        form.add(Box.createVerticalStrut(12));
        form.add(resultPanel);
        form.add(Box.createVerticalStrut(12));
        form.add(createField("Nội dung / Ghi chú", txtNoiDung));

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(null);
        formScroll.getViewport().setBackground(CARD_BG);
        formScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);
        root.add(formScroll, BorderLayout.CENTER);

        // ── Date panel switching ──────────────────────────────────────────────
        Runnable updateDatePanels = () -> {
            int idx = cbLoai.getSelectedIndex();
            pnlNgay.setVisible(idx == 0 || idx == 1);
            pnlThang.setVisible(idx == 2);
            pnlNam.setVisible(idx == 3);
            resultPanel.setVisible(false);
            calcValues[0] = null; calcValues[1] = null; calcValues[2] = null;
            form.revalidate();
        };
        cbLoai.addActionListener(e -> updateDatePanels.run());
        updateDatePanels.run();

        // ── Calculate action ──────────────────────────────────────────────────
        RevenueController calcController = new RevenueController();

        calcBtn.addActionListener(e -> {
            int loaiIdx = cbLoai.getSelectedIndex();
            LocalDate from, to;
            try {
                switch (loaiIdx) {
                    case 0 -> { // NGAY
                        LocalDate d = LocalDate.parse(txtNgay.getText().trim(), DATE_FMT);
                        from = d; to = d;
                    }
                    case 1 -> { // TUAN
                        LocalDate d = LocalDate.parse(txtNgay.getText().trim(), DATE_FMT);
                        from = d.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                        to   = d.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                    }
                    case 2 -> { // THANG
                        String[] parts = txtThang.getText().trim().split("/");
                        YearMonth ym = YearMonth.of(Integer.parseInt(parts[1]), Integer.parseInt(parts[0]));
                        from = ym.atDay(1);
                        to   = ym.atEndOfMonth();
                    }
                    case 3 -> { // NAM
                        int year = Integer.parseInt(txtNam.getText().trim());
                        from = LocalDate.of(year, 1, 1);
                        to   = LocalDate.of(year, 12, 31);
                    }
                    default -> { return; }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Ngày/tháng/năm không hợp lệ.", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String maCn = selectedBranch != null ? selectedBranch.maCn() : null;
            RevenueSearchCriteria criteria = new RevenueSearchCriteria();
            criteria.setFromDate(from);
            criteria.setToDate(to);
            criteria.setMaCn(maCn);

            try {
                RevenueSummary summary = calcController.getSummary(criteria);
                calcValues[0] = summary.getDoanhThuThueSan();
                calcValues[1] = summary.getDoanhThuDichVu();
                calcValues[2] = summary.getTongDoanhThu();
                lblDtThueSan.setText(formatCurrency(calcValues[0]));
                lblDtDichVu.setText(formatCurrency(calcValues[1]));
                lblTongDt.setText(formatCurrency(calcValues[2]));
                resultPanel.setVisible(true);
                form.revalidate();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Lỗi truy vấn: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        // ── Actions ──────────────────────────────────────────────────────────
        JPanel actions = new JPanel(new GridLayout(1, 2, 12, 0));
        actions.setOpaque(false);
        actions.setBorder(new EmptyBorder(8, 0, 0, 0));

        JButton cancelBtn = createPillButton("Hủy", new Color(226, 232, 240), new Color(30, 41, 59));
        JButton saveBtn   = createPillButton("Lưu báo cáo", BRAND_GREEN, Color.WHITE);
        actions.add(cancelBtn);
        actions.add(saveBtn);
        root.add(actions, BorderLayout.SOUTH);

        final RevenueCreateRequest[] result = new RevenueCreateRequest[1];

        cancelBtn.addActionListener(e -> dialog.dispose());
        saveBtn.addActionListener(e -> {
            if (calcValues[2] == null) {
                JOptionPane.showMessageDialog(dialog,
                        "Vui lòng bấm \"Tính toán doanh thu\" trước khi lưu.",
                        "Chưa tính toán", JOptionPane.WARNING_MESSAGE);
                return;
            }


            int loaiIdx = cbLoai.getSelectedIndex();
            String loaiCode = LOAI_CODES[loaiIdx];
            LocalDate from, to;
            try {
                switch (loaiIdx) {
                    case 0 -> {
                        LocalDate d = LocalDate.parse(txtNgay.getText().trim(), DATE_FMT);
                        from = d; to = d;
                    }
                    case 1 -> {
                        LocalDate d = LocalDate.parse(txtNgay.getText().trim(), DATE_FMT);
                        from = d.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                        to   = d.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                    }
                    case 2 -> {
                        String[] parts = txtThang.getText().trim().split("/");
                        YearMonth ym = YearMonth.of(Integer.parseInt(parts[1]), Integer.parseInt(parts[0]));
                        from = ym.atDay(1);
                        to   = ym.atEndOfMonth();
                    }
                    case 3 -> {
                        int year = Integer.parseInt(txtNam.getText().trim());
                        from = LocalDate.of(year, 1, 1);
                        to   = LocalDate.of(year, 12, 31);
                    }
                    default -> { return; }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Ngày/tháng/năm không hợp lệ.", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }   

            String maCn    = selectedBranch != null ? selectedBranch.maCn() : null;
            String noiDung = txtNoiDung.getText().trim();

            result[0] = new RevenueCreateRequest(
                    txtMaDt.getText().trim(), maCn, loaiCode, noiDung,
                    LocalDate.now(), from, to,
                    calcValues[0], calcValues[1], calcValues[2]);
            dialog.dispose();
        });

        dialog.pack();
        applySize(dialog, 0.38, 0.75, 520, 620);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return result[0];
    }

    // ── Result panel ─────────────────────────────────────────────────────────

    private static JPanel buildResultPanel(JLabel lblThueSan, JLabel lblDichVu, JLabel lblTong) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(RESULT_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(new Color(187, 247, 208), 12),
                new EmptyBorder(14, 16, 14, 16)));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel header = new JLabel("KẾT QUẢ TÍNH TOÁN");
        header.setFont(new Font("Lexend", Font.BOLD, 12));
        header.setForeground(new Color(22, 101, 52));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(header);
        panel.add(Box.createVerticalStrut(10));

        panel.add(buildResultRow("Doanh thu thuê sân:", lblThueSan));
        panel.add(Box.createVerticalStrut(6));
        panel.add(buildResultRow("Doanh thu dịch vụ:", lblDichVu));
        panel.add(Box.createVerticalStrut(8));

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(new Color(187, 247, 208));
        panel.add(sep);
        panel.add(Box.createVerticalStrut(8));

        panel.add(buildResultRow("TỔNG DOANH THU:", lblTong));
        return panel;
    }

    private static JPanel buildResultRow(String label, JLabel valueLabel) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Lexend", Font.PLAIN, 13));
        lbl.setForeground(TEXT_DARK);

        row.add(lbl, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.EAST);
        return row;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static String formatCurrency(BigDecimal value) {
        if (value == null) return "0đ";
        return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(value) + "đ";
    }

    private static void applySize(JDialog d, double wr, double hr, int minW, int minH) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int w = Math.max(minW, (int) (screen.width  * wr));
        int h = Math.max(minH, (int) (screen.height * hr));
        d.setSize(Math.min(w, screen.width), Math.min(h, screen.height));
        d.setMinimumSize(new Dimension(minW, minH));
    }

    private static JTextField createReadOnlyField(String value) {
        JTextField field = new JTextField(value == null ? "" : value);
        field.setEditable(false);
        field.setFocusable(false);
        field.setOpaque(false);
        field.setFont(new Font("Lexend", Font.BOLD, 14));
        field.setForeground(new Color(31, 41, 55));
        field.setBackground(READONLY_BG);
        return field;
    }

    private static JPanel createField(String labelText, JTextField field) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Lexend", Font.BOLD, 12));
        label.setForeground(new Color(75, 85, 99));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        boolean editable = field.isEditable();
        field.setFont(new Font("Lexend", editable ? Font.PLAIN : Font.BOLD, 14));
        field.setForeground(new Color(31, 41, 55));
        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(BORDER_CLR, 25),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        field.setBackground(editable ? Color.WHITE : READONLY_BG);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        panel.add(label);
        panel.add(Box.createVerticalStrut(6));
        panel.add(field);
        return panel;
    }

    private static JPanel createComboField(String labelText, JComboBox<String> combo) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Lexend", Font.BOLD, 12));
        label.setForeground(new Color(75, 85, 99));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        combo.setAlignmentX(Component.LEFT_ALIGNMENT);
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        combo.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(BORDER_CLR, 25),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)));

        panel.add(label);
        panel.add(Box.createVerticalStrut(6));
        panel.add(combo);
        return panel;
    }

    private static JButton createPillButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btn.setForeground(fg);
        btn.setFont(new Font("Lexend", Font.BOLD, 13));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 18, 10, 18));
        return btn;
    }

    private static final class RoundedLineBorder extends AbstractBorder {
        private final Color color;
        private final int arc;
        RoundedLineBorder(Color color, int arc) { this.color = color; this.arc = arc; }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x + 1, y + 1, w - 3, h - 3, arc, arc);
            g2.dispose();
        }

        @Override public Insets getBorderInsets(Component c) { return new Insets(1, 1, 1, 1); }
    }
}
