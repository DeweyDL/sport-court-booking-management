package com.sportcourt.modules.bill.util;

import com.sportcourt.modules.bill.dto.BillDetail;
import com.sportcourt.modules.bill.dto.CourtRentalItem;
import com.sportcourt.modules.bill.dto.ServiceItem;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class BillPdfExporter {

    private static final Color CLR_HEADER_BG = new Color(33, 60, 33);
    private static final Color CLR_TABLE_HEAD = new Color(46, 125, 50);
    private static final Color CLR_ROW_ALT = new Color(245, 250, 245);
    private static final Color CLR_DIVIDER = new Color(210, 215, 210);
    private static final Color CLR_TOTAL_BG = new Color(237, 247, 237);
    private static final Color CLR_GREEN_TEXT = new Color(22, 130, 50);
    private static final Color CLR_MUTED = new Color(120, 120, 120);

    private static final float PAGE_W = PDRectangle.A4.getWidth();
    private static final float PAGE_H = PDRectangle.A4.getHeight();
    private static final float MARGIN = 40f;
    private static final float CONTENT_W = PAGE_W - 2 * MARGIN;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat MONEY_FMT = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    private PDDocument doc;
    private PDPage page;
    private PDPageContentStream cs;
    private PDType0Font fontRegular;
    private PDType0Font fontBold;
    private float curY;

    public static void export(BillDetail bill, String filePath) throws Exception {
        new BillPdfExporter().doExport(bill, filePath);
    }

    private static String money(BigDecimal v) {
        return v != null ? MONEY_FMT.format(v.longValue()) : "0";
    }

    private static String nvl(String s) {
        return s != null ? s : "";
    }

    public static void exportFromView(
            com.sportcourt.modules.bill.controller.ManageBillController controller,
            String maHD,
            Component parent
    ) {
        var result = controller.getDetail(maHD);
        if (!result.success()) {
            JOptionPane.showMessageDialog(parent,
                    "Lỗi lấy dữ liệu: " + result.message(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu hóa đơn PDF");
        chooser.setSelectedFile(new File("HoaDon_" + maHD + ".pdf"));
        chooser.setFileFilter(new FileNameExtensionFilter("PDF Files (*.pdf)", "pdf"));

        if (chooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            if (!path.endsWith(".pdf")) path += ".pdf";
            final String finalPath = path;
            try {
                BillPdfExporter.export(result.data(), finalPath);
                int open = JOptionPane.showConfirmDialog(parent,
                        "Xuất PDF thành công!\n" + finalPath + "\n\nMở file ngay?",
                        "Thành công", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                if (open == JOptionPane.YES_OPTION)
                    Desktop.getDesktop().open(new File(finalPath));
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(parent,
                        "Lỗi xuất PDF: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void doExport(BillDetail bill, String filePath) throws Exception {
        doc = new PDDocument();
        loadFonts();
        newPage();

        drawHeader(bill);
        drawInfoBoxes(bill);
        drawCourtTable(bill.danhSachThuesan());
        drawServiceTable(bill.danhSachDichVu());
        drawSummary(bill);

        cs.close();
        doc.save(new File(filePath));
        doc.close();
    }

    private void loadFonts() throws Exception {
        try (InputStream reg = getClass().getResourceAsStream("/font/Lexend-Regular.ttf");
             InputStream bold = getClass().getResourceAsStream("/font/Lexend-Bold.ttf")) {

            if (reg == null || bold == null) {
                throw new IllegalStateException(
                        "Không tìm thấy font Lexend!\n" +
                                "Đặt Lexend-Regular.ttf và Lexend-Bold.ttf vào src/main/resources/fonts/"
                );
            }
            fontRegular = PDType0Font.load(doc, reg);
            fontBold = PDType0Font.load(doc, bold);
        }
    }

    private void newPage() throws Exception {
        if (cs != null) cs.close();
        page = new PDPage(PDRectangle.A4);
        doc.addPage(page);
        cs = new PDPageContentStream(doc, page);
        curY = PAGE_H - MARGIN;
    }

    private void checkPageBreak(float neededH) throws Exception {
        if (curY - neededH < MARGIN + 50) newPage();
    }

    private void drawHeader(BillDetail bill) throws Exception {
        float h = 72f;

        fillRect(MARGIN, curY - h, CONTENT_W, h, CLR_HEADER_BG);

        // Tên & tagline bên trái
        drawText("RENTSTA", MARGIN + 14, curY - 24, fontBold, 16, Color.WHITE);
        drawText("Hóa đơn thuê sân thể thao", MARGIN + 14, curY - 42, fontRegular, 9, new Color(180, 220, 180));

        String maHD = "Mã HĐ: " + nvl(bill.maHD());
        String ngay = "Ngày:    " + (bill.createdAt() != null ? bill.createdAt().format(DT_FMT) : "");
        float rightEdge = MARGIN + CONTENT_W - 12;
        drawText(maHD, rightEdge - textW(maHD, fontBold, 10), curY - 24, fontBold, 10, Color.WHITE);
        drawText(ngay, rightEdge - textW(ngay, fontRegular, 9), curY - 42, fontRegular, 9, new Color(200, 230, 200));

        drawLine(MARGIN, curY - h, MARGIN + CONTENT_W, curY - h, CLR_GREEN_TEXT);
        curY -= h + 14;
    }

    private void drawInfoBoxes(BillDetail bill) throws Exception {
        checkPageBreak(80);
        float boxH = 72f;
        float half = (CONTENT_W - 8) / 2;

        float lx = MARGIN;
        fillRect(lx, curY - boxH, half, boxH, CLR_ROW_ALT);
        drawRectBorder(lx, curY - boxH, half, boxH, CLR_DIVIDER);
        drawText("KHÁCH HÀNG", lx + 10, curY - 14, fontBold, 8, CLR_TABLE_HEAD);
        drawText("Mã KH  : " + nvl(bill.maKH()), lx + 10, curY - 29, fontRegular, 9, Color.BLACK);
        drawText("Họ tên : " + nvl(bill.tenKhachHang()), lx + 10, curY - 44, fontRegular, 9, Color.BLACK);
        drawText("SĐT    : " + nvl(bill.sdtKhachHang()), lx + 10, curY - 59, fontRegular, 9, Color.BLACK);

        float rx = MARGIN + half + 8;
        fillRect(rx, curY - boxH, half, boxH, CLR_ROW_ALT);
        drawRectBorder(rx, curY - boxH, half, boxH, CLR_DIVIDER);
        drawText("NHÂN VIÊN XỬ LÝ", rx + 10, curY - 14, fontBold, 8, CLR_TABLE_HEAD);
        drawText("Mã NV      : " + nvl(bill.maNV()), rx + 10, curY - 29, fontRegular, 9, Color.BLACK);
        drawText("Họ tên     : " + nvl(bill.tenNhanVien()), rx + 10, curY - 44, fontRegular, 9, Color.BLACK);
        drawText("Chi nhánh  : " + nvl(bill.maCn()), rx + 10, curY - 59, fontRegular, 9, Color.BLACK);

        curY -= boxH + 18;
    }

    private void drawCourtTable(List<CourtRentalItem> items) throws Exception {
        drawSectionTitle("CHI TIẾT THUÊ SÂN");

        float[] cols = {0.22f, 0.22f, 0.30f, 0.26f};
        String[] heads = {"Mã sân", "Ngày thuê", "Giờ (bắt đầu - kết thúc)", "Đơn giá (VNĐ)"};
        drawTableHeader(cols, heads);

        if (items == null || items.isEmpty()) {
            drawEmptyRow();
        } else {
            int i = 0;
            for (CourtRentalItem item : items) {
                checkPageBreak(22);
                String ngay = item.ngayThue() != null ? item.ngayThue().toLocalDate().format(DATE_FMT) : "";
                String gio = item.gioBatDau() + "h00 - " + item.gioKetThuc() + "h00";
                String gia = item.donGiaThue() != null ? money(item.donGiaThue()) : "";
                drawTableRow(cols,
                        new String[]{nvl(item.maSan()), ngay, gio, gia},
                        i++ % 2 == 1);
            }
        }
        curY -= 12;
    }

    private void drawServiceTable(List<ServiceItem> items) throws Exception {
        drawSectionTitle("CHI TIẾT DỊCH VỤ / SẢN PHẨM");

        float[] cols = {0.22f, 0.22f, 0.30f, 0.26f};
        String[] heads = {"Tên sản phẩm / dụng cụ", "Số lượng", "Đơn giá (VNĐ)", "Thành tiền (VNĐ)"};
        drawTableHeader(cols, heads);

        if (items == null || items.isEmpty()) {
            drawEmptyRow();
        } else {
            int i = 0;
            for (ServiceItem item : items) {
                checkPageBreak(22);
                BigDecimal thanhTien = item.donGia() != null
                        ? item.donGia().multiply(BigDecimal.valueOf(item.soLuong()))
                        : BigDecimal.ZERO;
                drawTableRow(cols,
                        new String[]{
                                nvl(item.tenSanPham()),
                                String.valueOf(item.soLuong()),
                                item.donGia() != null ? money(item.donGia()) : "",
                                money(thanhTien)
                        },
                        i++ % 2 == 1);
            }
        }
        curY -= 12;
    }

    private void drawSummary(BillDetail bill) throws Exception {
        checkPageBreak(130);

        float sumW = 230f;
        float sumX = MARGIN + CONTENT_W - sumW;
        float lineH = 21f;
        float sumH = lineH * 5 + 18;

        fillRect(sumX, curY - sumH, sumW, sumH, CLR_TOTAL_BG);
        drawRectBorder(sumX, curY - sumH, sumW, sumH, CLR_DIVIDER);

        float lx = sumX + 10;
        float rx = sumX + sumW - 10;
        float y = curY - 16;

        sumRow("Tổng giá trị :", bill.tongGiaTri(), lx, rx, y);
        sumRow("Tiền cọc :", bill.tienCoc(), lx, rx, y - lineH);
        sumRow("Giảm giá (%) :", bill.giamGia(), lx, rx, y - lineH * 2);
        sumRow("Chiết khấu HH :", bill.chietKhauHang(), lx, rx, y - lineH * 3);


        drawText("TỔNG THANH TOÁN:", lx, y - lineH * 4, fontBold, 9, Color.BLACK);
        String ttStr = bill.tongTien() != null ? money(bill.tongTien()) + " VNĐ" : "---";
        drawText(ttStr, rx - textW(ttStr, fontBold, 10), y - lineH * 4, fontBold, 10, CLR_GREEN_TEXT);

        curY -= sumH + 16;
    }

    private void sumRow(String label, BigDecimal val, float lx, float rx, float y) throws Exception {
        drawText(label, lx, y, fontRegular, 8, Color.DARK_GRAY);
        String s = val != null ? money(val) + " VNĐ" : "---";
        drawText(s, rx - textW(s, fontRegular, 8), y, fontRegular, 8, Color.BLACK);
    }


    private void drawSectionTitle(String title) throws Exception {
        checkPageBreak(28);
        drawText(title, MARGIN, curY, fontBold, 10, CLR_TABLE_HEAD);
        drawLine(MARGIN, curY - 5, MARGIN + CONTENT_W, curY - 5, CLR_TABLE_HEAD);
        curY -= 18;
    }

    private void drawTableHeader(float[] cols, String[] labels) throws Exception {
        checkPageBreak(22);
        float rh = 20f;
        fillRect(MARGIN, curY - rh, CONTENT_W, rh, CLR_TABLE_HEAD);
        float x = MARGIN + 8;
        for (int i = 0; i < cols.length; i++) {
            drawText(labels[i], x, curY - 13, fontBold, 8, Color.WHITE);
            x += CONTENT_W * cols[i];
        }
        curY -= rh;
    }

    private void drawTableRow(float[] cols, String[] values, boolean alt) throws Exception {
        float rh = 20f;
        if (alt) fillRect(MARGIN, curY - rh, CONTENT_W, rh, CLR_ROW_ALT);
        drawLine(MARGIN, curY - rh, MARGIN + CONTENT_W, curY - rh, CLR_DIVIDER);
        float x = MARGIN + 8;  // giống header
        for (int i = 0; i < cols.length; i++) {
            float cw = CONTENT_W * cols[i];
            String v = truncate(nvl(values[i]), fontRegular, 8, cw - 10);
            drawText(v, x, curY - 13, fontRegular, 8, Color.BLACK);  // bỏ (i==0?0:4)
            x += cw;
        }
        curY -= rh;
    }

    private void drawEmptyRow() throws Exception {
        checkPageBreak(20);
        fillRect(MARGIN, curY - 20, CONTENT_W, 20, CLR_ROW_ALT);
        drawText("(Không có dữ liệu)", MARGIN + 8, curY - 13, fontRegular, 8, CLR_MUTED);
        curY -= 20;
    }

    private void drawText(String text, float x, float y, PDType0Font font, float size, Color color) throws Exception {
        cs.beginText();
        cs.setFont(font, size);
        cs.setNonStrokingColor(color);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
    }

    private void fillRect(float x, float y, float w, float h, Color c) throws Exception {
        cs.setNonStrokingColor(c);
        cs.addRect(x, y, w, h);
        cs.fill();
        cs.setNonStrokingColor(Color.BLACK);
    }

    private void drawRectBorder(float x, float y, float w, float h, Color c) throws Exception {
        cs.setStrokingColor(c);
        cs.addRect(x, y, w, h);
        cs.stroke();
        cs.setStrokingColor(Color.BLACK);
    }

    private void drawLine(float x1, float y1, float x2, float y2, Color c) throws Exception {
        cs.setStrokingColor(c);
        cs.moveTo(x1, y1);
        cs.lineTo(x2, y2);
        cs.stroke();
        cs.setStrokingColor(Color.BLACK);
    }

    private float textW(String text, PDType0Font font, float size) throws Exception {
        return font.getStringWidth(text) / 1000f * size;
    }

    private String truncate(String text, PDType0Font font, float size, float maxW) throws Exception {
        if (textW(text, font, size) <= maxW) return text;
        while (text.length() > 1 && textW(text + "...", font, size) > maxW)
            text = text.substring(0, text.length() - 1);
        return text + "...";
    }
}