package com.sportcourt.modules.bill.view;

import com.sportcourt.common.style.AppFonts;
import com.sportcourt.common.style.CrudViewStyle;
import com.sportcourt.modules.bill.dto.BillDetail;
import com.sportcourt.modules.bill.dto.CourtRentalItem;
import com.sportcourt.modules.bill.dto.ServiceItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class BillDetailDialog extends JDialog {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Color SECTION_BG = new Color(248, 250, 252);

    private BillDetailDialog(Component parent, BillDetail detail) {
        super(SwingUtilities.getWindowAncestor(parent), "Chi tiết hóa đơn – " + detail.maHD(), ModalityType.APPLICATION_MODAL);
        AppFonts.register();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(24, 28, 24, 28));

        content.add(buildHeaderSection(detail));
        content.add(Box.createVerticalStrut(16));
        content.add(buildInfoRow("Mã hóa đơn", detail.maHD()));
        content.add(buildInfoRow("Khách hàng", detail.tenKhachHang() + " (" + detail.maKH() + ")"));
        content.add(buildInfoRow("Số điện thoại KH", detail.sdtKhachHang()));
        content.add(buildInfoRow("Nhân viên", detail.tenNhanVien() + " (" + detail.maNV() + ")"));
        content.add(buildInfoRow("Ngày tạo", detail.createdAt() != null ? detail.createdAt().format(DATE_FMT) : "--"));
        content.add(buildInfoRow("Tiền cọc", formatCurrency(detail.tienCoc())));
        content.add(buildInfoRow("Giảm giá", detail.giamGia() + "%"));
        content.add(buildInfoRow("Tổng giá trị", formatCurrency(detail.tongGiaTri())));
        content.add(buildInfoRow("Tổng tiền", formatCurrency(detail.tongTien())));

        if (!detail.danhSachThuesan().isEmpty()) {
            content.add(Box.createVerticalStrut(16));
            content.add(buildSectionTitle("Danh sách thuê sân"));
            content.add(buildCourtTable(detail.danhSachThuesan()));
        }

        if (!detail.danhSachDichVu().isEmpty()) {
            content.add(Box.createVerticalStrut(16));
            content.add(buildSectionTitle("Danh sách dịch vụ / sản phẩm"));
            content.add(buildServiceTable(detail.danhSachDichVu()));
        }

        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(Color.WHITE);
        body.add(buildHeroBanner(), BorderLayout.NORTH);
        body.add(content, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(body);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        CrudViewStyle.configureScrollPane(scrollPane);
        add(scrollPane, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        footer.setBackground(Color.WHITE);
        footer.setBorder(new MatteBorder(1, 0, 0, 0, new Color(229, 231, 235)));
        JButton closeBtn = createButton("Đóng", new Color(243, 244, 246), new Color(75, 85, 99));
        closeBtn.addActionListener(e -> dispose());
        footer.add(closeBtn);
        add(footer, BorderLayout.SOUTH);

        setSize(640, 820);
        setLocationRelativeTo(parent);
    }

    public static void show(Component parent, BillDetail detail) {
        new BillDetailDialog(parent, detail).setVisible(true);
    }

    private JPanel buildHeroBanner() {
        JPanel banner = new JPanel() {
            private Image img;
            {
                java.net.URL url = getClass().getResource("/image/Stadium Hero.png");
                if (url != null) img = new ImageIcon(url).getImage();
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (img != null) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2.drawImage(img, 0, 0, getWidth(), getHeight(), this);
                    g2.setColor(new Color(0, 0, 0, 80));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.dispose();
                } else {
                    g.setColor(new Color(30, 41, 59));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        banner.setPreferredSize(new Dimension(0, 160));
        banner.setLayout(null);
        return banner;
    }

    private JPanel buildHeaderSection(BillDetail detail) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("HÓA ĐƠN");
        title.setFont(new Font("Lexend", Font.BOLD, 22));
        title.setForeground(new Color(30, 31, 36));

        JPanel statusWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        statusWrapper.setBackground(Color.WHITE);
        statusWrapper.add(buildStatusPill(detail.trangThai()));

        panel.add(title, BorderLayout.WEST);
        panel.add(statusWrapper, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildInfoRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setBorder(new EmptyBorder(5, 0, 5, 0));

        JLabel labelComp = new JLabel(label + ":");
        labelComp.setFont(new Font("Segoe UI", Font.BOLD, 13));
        labelComp.setForeground(new Color(107, 114, 128));
        labelComp.setPreferredSize(new Dimension(160, 0));

        JLabel valueComp = new JLabel(value == null || value.isBlank() ? "--" : value);
        valueComp.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        valueComp.setForeground(new Color(17, 24, 39));

        row.add(labelComp, BorderLayout.WEST);
        row.add(valueComp, BorderLayout.CENTER);
        return row;
    }

    private JLabel buildSectionTitle(String text) {
        JLabel label = new JLabel(text.toUpperCase());
        label.setFont(new Font("Lexend", Font.BOLD, 13));
        label.setForeground(new Color(107, 114, 128));
        label.setBorder(new EmptyBorder(0, 0, 6, 0));
        return label;
    }

    private JPanel buildCourtTable(java.util.List<CourtRentalItem> items) {
        String[] cols = {"MÃ SÂN", "NGÀY THUÊ", "ĐƠN GIÁ", "TRẠNG THÁI"};
        Object[][] data = items.stream().map(item -> new Object[]{
                item.maSan(),
                String.format("%02d:00-%02d:00", item.gioBatDau(), item.gioKetThuc())
                        + (item.ngayThue() != null ? " | " + item.ngayThue().format(DATE_FMT) : ""),
                formatCurrency(item.donGiaThue()),
                item.trangThai()
        }).toArray(Object[][]::new);
        return buildSimpleTable(cols, data);
    }

    private JPanel buildServiceTable(java.util.List<ServiceItem> items) {
        String[] cols = {"TÊN SẢN PHẨM/DỤNG CỤ", "SỐ LƯỢNG", "ĐƠN GIÁ", "TRẠNG THÁI"};
        Object[][] data = items.stream().map(item -> new Object[]{
                item.tenSanPham() != null ? item.tenSanPham() : "--",
                item.soLuong(),
                formatCurrency(item.donGia()),
                item.trangThai()
        }).toArray(Object[][]::new);
        return buildSimpleTable(cols, data);
    }

    private JPanel buildSimpleTable(String[] columns, Object[][] data) {
        JTable table = new JTable(data, columns) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(SECTION_BG);
        table.setGridColor(new Color(229, 231, 235));
        table.setShowGrid(true);
        table.setBackground(Color.WHITE);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235)));
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        int h = Math.min(data.length * 28 + table.getTableHeader().getPreferredSize().height + 4, 200);
        sp.setPreferredSize(new Dimension(0, h));
        wrapper.add(sp, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildStatusPill(String trangThai) {
        Color bg;
        Color fg;
        if ("ĐÃ THANH TOÁN".equals(trangThai)) {
            bg = CrudViewStyle.SUCCESS_BG;
            fg = CrudViewStyle.SUCCESS_TEXT;
        } else if ("ĐÃ HUỶ".equals(trangThai)) {
            bg = CrudViewStyle.DANGER_BG;
            fg = CrudViewStyle.DANGER_TEXT;
        } else {
            bg = new Color(255, 243, 205);
            fg = new Color(146, 100, 0);
        }
        return CrudViewStyle.createStatusPill(trangThai, bg, fg);
    }

    private JButton createButton(String text, Color bg, Color fg) {
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
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(6, 20, 6, 20));
        return btn;
    }

    private String formatCurrency(BigDecimal value) {
        if (value == null) return "0 VNĐ";
        return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(value) + " VNĐ";
    }
}
