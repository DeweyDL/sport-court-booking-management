package com.sportcourt.modules.revenue.dao;

import com.sportcourt.common.db.ConnectionUtils;
import com.sportcourt.modules.revenue.dto.BranchRevenueRow;
import com.sportcourt.modules.revenue.dto.CourtRevenueRow;
import com.sportcourt.modules.revenue.dto.RevenueChartData;
import com.sportcourt.modules.revenue.dto.RevenueCreateRequest;
import com.sportcourt.modules.revenue.dto.ServiceRevenueRow;
import com.sportcourt.modules.revenue.dto.RevenueRow;
import com.sportcourt.modules.revenue.dto.RevenueSearchCriteria;
import com.sportcourt.modules.revenue.dto.RevenueSummary;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JdbcRevenueDAO implements RevenueDAO {

    @Override
    public List<RevenueRow> findRows(RevenueSearchCriteria criteria) throws SQLException {
        String sql = """
                SELECT dt.MADT,
                       dt.MACN,
                       cn.TEN_CHI_NHANH,
                       dt.LOAI,
                       dt.NOIDUNG,
                       dt.NGAY,
                       dt.NGAY_BAT_DAU,
                       dt.NGAY_KET_THUC,
                       dt.DT_THUE_SAN,
                       dt.DT_DICH_VU,
                       dt.TONGDOANHTHU
                FROM DOANH_THU dt
                LEFT JOIN CHI_NHANH cn ON cn.MACN = dt.MACN
                WHERE dt.IS_DELETED = 0
                  AND (? IS NULL OR dt.NGAY >= ?)
                  AND (? IS NULL OR dt.NGAY <= ?)
                  AND (? = 0 AND (? IS NULL OR dt.MACN = ?) OR ? = 1 AND dt.MACN IS NULL)
                  AND (
                        ? IS NULL
                        OR UPPER(dt.MADT)    LIKE ?
                        OR UPPER(dt.NOIDUNG) LIKE ?
                        OR UPPER(cn.TEN_CHI_NHANH) LIKE ?
                  )
                  AND (? IS NULL OR dt.LOAI = ?)
                ORDER BY dt.NGAY DESC, dt.MADT ASC
                """;

        LocalDate from       = criteria.getFromDate();
        LocalDate to         = criteria.getToDate();
        String    branch     = normalizeBranch(criteria.getMaCn());
        String    keyword    = normalizeKeyword(criteria.getKeyword());
        String    loai       = criteria.getLoai();
        int       nullBranch = criteria.isFilterNullBranch() ? 1 : 0;

        List<RevenueRow> rows = new ArrayList<>();
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, from == null ? null : Date.valueOf(from));
            ps.setDate(2, from == null ? null : Date.valueOf(from));
            ps.setDate(3, to   == null ? null : Date.valueOf(to));
            ps.setDate(4, to   == null ? null : Date.valueOf(to));
            ps.setInt(5, nullBranch);
            ps.setString(6, branch);
            ps.setString(7, branch);
            ps.setInt(8, nullBranch);
            ps.setString(9, keyword);
            ps.setString(10, toLike(keyword));
            ps.setString(11, toLike(keyword));
            ps.setString(12, toLike(keyword));
            ps.setString(13, loai);
            ps.setString(14, loai);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(mapRow(rs));
                }
            }
        }
        return rows;
    }

    @Override
    public RevenueSummary getSummary(RevenueSearchCriteria criteria) throws SQLException {
        // Tổng doanh thu  = SUM(HOA_DON.TONGTIEN) trạng thái ĐÃ THANH TOÁN
        // Doanh thu thuê sân = SUM từ CHI_TIET_HOA_DON_THUE_SAN (DON_GIA_THUE)
        // Doanh thu dịch vụ  = SUM từ CHI_TIET_HOA_DON_DICH_VU_DA_DUNG (DON_GIA * SL)
        // Chi nhánh xác định qua: HOA_DON.MANV → NHAN_VIEN.MACN
        String sql = """
                SELECT NVL(SUM(HD.TONGTIEN),      0) AS TONG_DT,
                       NVL(SUM(TS.DT_THUE_SAN),   0) AS DT_THUE_SAN,
                       NVL(SUM(DV.DT_DICH_VU),    0) AS DT_DICH_VU
                FROM HOA_DON HD
                JOIN NHAN_VIEN NV ON HD.MANV = NV.MANV
                LEFT JOIN (
                    SELECT MAHD, SUM(DON_GIA_THUE) AS DT_THUE_SAN
                    FROM CHI_TIET_HOA_DON_THUE_SAN
                    WHERE IS_DELETED = 0
                    GROUP BY MAHD
                ) TS ON TS.MAHD = HD.MAHD
                LEFT JOIN (
                    SELECT MAHD, SUM(DON_GIA * SL) AS DT_DICH_VU
                    FROM CHI_TIET_HOA_DON_DICH_VU_DA_DUNG
                    WHERE IS_DELETED = 0
                    GROUP BY MAHD
                ) DV ON DV.MAHD = HD.MAHD
                WHERE HD.IS_DELETED = 0
                  AND HD.TRANGTHAI = 'ĐÃ THANH TOÁN'
                  AND (? IS NULL OR TRUNC(HD.CREATED_AT) >= ?)
                  AND (? IS NULL OR TRUNC(HD.CREATED_AT) <= ?)
                  AND (? IS NULL OR NV.MACN = ?)
                """;

        LocalDate from   = criteria.getFromDate();
        LocalDate to     = criteria.getToDate();
        String    branch = normalizeBranch(criteria.getMaCn());

        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, from == null ? null : Date.valueOf(from));
            ps.setDate(2, from == null ? null : Date.valueOf(from));
            ps.setDate(3, to   == null ? null : Date.valueOf(to));
            ps.setDate(4, to   == null ? null : Date.valueOf(to));
            ps.setString(5, branch);
            ps.setString(6, branch);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    RevenueSummary summary = new RevenueSummary();
                    summary.setTongDoanhThu(rs.getBigDecimal("TONG_DT"));
                    summary.setDoanhThuThueSan(rs.getBigDecimal("DT_THUE_SAN"));
                    summary.setDoanhThuDichVu(rs.getBigDecimal("DT_DICH_VU"));
                    return summary;
                }
            }
        }
        RevenueSummary empty = new RevenueSummary();
        empty.setTongDoanhThu(BigDecimal.ZERO);
        empty.setDoanhThuThueSan(BigDecimal.ZERO);
        empty.setDoanhThuDichVu(BigDecimal.ZERO);
        return empty;
    }

    @Override
    public RevenueChartData getChartData(RevenueSearchCriteria criteria) throws SQLException {
        LocalDate toDate   = criteria.getToDate()   != null ? criteria.getToDate()   : LocalDate.now();
        LocalDate fromDate = criteria.getFromDate()  != null ? criteria.getFromDate() : toDate.minusDays(6);

        long      days     = java.time.temporal.ChronoUnit.DAYS.between(fromDate, toDate) + 1;
        LocalDate prevTo   = fromDate.minusDays(1);
        LocalDate prevFrom = prevTo.minusDays(days - 1);
        String    branch   = normalizeBranch(criteria.getMaCn());

        // Tính doanh thu theo từng ngày từ HOA_DON → NHAN_VIEN (để lọc chi nhánh)
        String sql = """
                SELECT TRUNC(HD.CREATED_AT) AS NGAY_TRUNC,
                       NVL(SUM(HD.TONGTIEN), 0) AS TONG
                FROM HOA_DON HD
                JOIN NHAN_VIEN NV ON HD.MANV = NV.MANV
                WHERE HD.IS_DELETED = 0
                  AND HD.TRANGTHAI = 'ĐÃ THANH TOÁN'
                  AND TRUNC(HD.CREATED_AT) >= ?
                  AND TRUNC(HD.CREATED_AT) <= ?
                  AND (? IS NULL OR NV.MACN = ?)
                GROUP BY TRUNC(HD.CREATED_AT)
                ORDER BY TRUNC(HD.CREATED_AT) ASC
                """;

        Map<LocalDate, Double> currentMap  = queryByDay(sql, fromDate, toDate,   branch);
        Map<LocalDate, Double> previousMap = queryByDay(sql, prevFrom, prevTo,   branch);

        int      size     = (int) days;
        String[] labels   = new String[size];
        double[] current  = new double[size];
        double[] previous = new double[size];
        String[] DOW      = {"CN", "T2", "T3", "T4", "T5", "T6", "T7"};

        for (int i = 0; i < size; i++) {
            LocalDate day     = fromDate.plusDays(i);
            LocalDate prevDay = prevFrom.plusDays(i);
            labels[i]   = DOW[day.getDayOfWeek() == DayOfWeek.SUNDAY ? 0 : day.getDayOfWeek().getValue()];
            current[i]  = currentMap.getOrDefault(day, 0.0)      / 1_000_000.0;
            previous[i] = previousMap.getOrDefault(prevDay, 0.0)  / 1_000_000.0;
        }
        return new RevenueChartData(labels, current, previous);
    }

    @Override
    public List<BranchRevenueRow> getBranchRevenue(RevenueSearchCriteria criteria) throws SQLException {
        // Chi nhánh xác định qua HOA_DON.MANV → NHAN_VIEN.MACN → CHI_NHANH
        // Tách riêng doanh thu thuê sân và dịch vụ đa dụng qua LEFT JOIN detail tables
        String sql = """
                SELECT NV.MACN,
                       CN.TEN_CHI_NHANH,
                       NVL(SUM(HD.TONGTIEN),      0) AS TONG,
                       NVL(SUM(TS.DT_THUE_SAN),   0) AS DT_THUE_SAN,
                       NVL(SUM(DV.DT_DICH_VU),    0) AS DT_DICH_VU
                FROM HOA_DON HD
                JOIN NHAN_VIEN NV ON HD.MANV = NV.MANV
                JOIN CHI_NHANH CN ON NV.MACN  = CN.MACN
                LEFT JOIN (
                    SELECT MAHD, SUM(DON_GIA_THUE) AS DT_THUE_SAN
                    FROM CHI_TIET_HOA_DON_THUE_SAN
                    WHERE IS_DELETED = 0
                    GROUP BY MAHD
                ) TS ON TS.MAHD = HD.MAHD
                LEFT JOIN (
                    SELECT MAHD, SUM(DON_GIA * SL) AS DT_DICH_VU
                    FROM CHI_TIET_HOA_DON_DICH_VU_DA_DUNG
                    WHERE IS_DELETED = 0
                    GROUP BY MAHD
                ) DV ON DV.MAHD = HD.MAHD
                WHERE HD.IS_DELETED = 0
                  AND HD.TRANGTHAI = 'ĐÃ THANH TOÁN'
                  AND (? IS NULL OR TRUNC(HD.CREATED_AT) >= ?)
                  AND (? IS NULL OR TRUNC(HD.CREATED_AT) <= ?)
                GROUP BY NV.MACN, CN.TEN_CHI_NHANH
                ORDER BY TO_NUMBER(SUBSTR(NV.MACN, INSTR(NV.MACN, '-') + 1)) ASC
                """;

        LocalDate from = criteria.getFromDate();
        LocalDate to   = criteria.getToDate();

        List<BranchRevenueRow> list = new ArrayList<>();
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, from == null ? null : Date.valueOf(from));
            ps.setDate(2, from == null ? null : Date.valueOf(from));
            ps.setDate(3, to   == null ? null : Date.valueOf(to));
            ps.setDate(4, to   == null ? null : Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BranchRevenueRow row = new BranchRevenueRow();
                    row.setMaCn(rs.getString("MACN"));
                    row.setTenChiNhanh(rs.getString("TEN_CHI_NHANH"));
                    row.setTongDoanhThu(rs.getBigDecimal("TONG"));
                    row.setDoanhThuThueSan(rs.getBigDecimal("DT_THUE_SAN"));
                    row.setDoanhThuDichVu(rs.getBigDecimal("DT_DICH_VU"));
                    list.add(row);
                }
            }
        }
        return list;
    }

    @Override
    public List<CourtRevenueRow> getCourtRevenue(RevenueSearchCriteria criteria) throws SQLException {
        String sql = """
                SELECT CTS.MASAN,
                       SC.MAKV,
                       CN.TEN_CHI_NHANH,
                       NVL(SUM(CTS.DON_GIA_THUE), 0) AS DT_THUE_SAN
                FROM CHI_TIET_HOA_DON_THUE_SAN CTS
                JOIN HOA_DON HD  ON HD.MAHD  = CTS.MAHD
                JOIN SAN_CON SC  ON SC.MASAN = CTS.MASAN
                JOIN KHU_VUC KV  ON KV.MAKV  = SC.MAKV  AND KV.IS_DELETED = 0
                JOIN CHI_NHANH CN ON CN.MACN = KV.MACN  AND CN.IS_DELETED = 0
                WHERE CTS.IS_DELETED = 0
                  AND HD.IS_DELETED  = 0
                  AND HD.TRANGTHAI   = 'ĐÃ THANH TOÁN'
                  AND (? IS NULL OR TRUNC(HD.CREATED_AT) >= ?)
                  AND (? IS NULL OR TRUNC(HD.CREATED_AT) <= ?)
                  AND (? IS NULL OR KV.MACN = ?)
                GROUP BY CTS.MASAN, SC.MAKV, KV.MACN, CN.TEN_CHI_NHANH
                ORDER BY TO_NUMBER(SUBSTR(CTS.MASAN, INSTR(CTS.MASAN, '-') + 1)) ASC
                """;

        LocalDate from   = criteria.getFromDate();
        LocalDate to     = criteria.getToDate();
        String    branch = normalizeBranch(criteria.getMaCn());

        List<CourtRevenueRow> list = new ArrayList<>();
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, from == null ? null : Date.valueOf(from));
            ps.setDate(2, from == null ? null : Date.valueOf(from));
            ps.setDate(3, to   == null ? null : Date.valueOf(to));
            ps.setDate(4, to   == null ? null : Date.valueOf(to));
            ps.setString(5, branch);
            ps.setString(6, branch);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CourtRevenueRow row = new CourtRevenueRow();
                    row.setMaSan(rs.getString("MASAN"));
                    row.setMaKv(rs.getString("MAKV"));
                    row.setTenChiNhanh(rs.getString("TEN_CHI_NHANH"));
                    row.setDoanhThuThueSan(rs.getBigDecimal("DT_THUE_SAN"));
                    list.add(row);
                }
            }
        }
        return list;
    }

    @Override
    public List<ServiceRevenueRow> getServiceRevenue(RevenueSearchCriteria criteria) throws SQLException {
        String sql = """
                SELECT
                    CASE WHEN CTDV.MASP IS NOT NULL THEN CTDV.MASP  ELSE CTDV.MADC  END AS MA_ITEM,
                    CASE WHEN CTDV.MASP IS NOT NULL THEN SP.TENSP    ELSE DC.TENDC   END AS TEN_ITEM,
                    CASE WHEN CTDV.MASP IS NOT NULL THEN 'Sản phẩm'  ELSE 'Dụng cụ' END AS LOAI,
                    NVL(SUM(CTDV.DON_GIA * CTDV.SL), 0)             AS DT_DICH_VU
                FROM CHI_TIET_HOA_DON_DICH_VU_DA_DUNG CTDV
                JOIN HOA_DON      HD ON HD.MAHD  = CTDV.MAHD
                JOIN NHAN_VIEN    NV ON NV.MANV  = HD.MANV
                LEFT JOIN SAN_PHAM           SP ON SP.MASP = CTDV.MASP
                LEFT JOIN DUNG_CU_THE_THAO   DC ON DC.MADC = CTDV.MADC
                WHERE CTDV.IS_DELETED = 0
                  AND HD.IS_DELETED   = 0
                  AND HD.TRANGTHAI    = 'ĐÃ THANH TOÁN'
                  AND (? IS NULL OR TRUNC(HD.CREATED_AT) >= ?)
                  AND (? IS NULL OR TRUNC(HD.CREATED_AT) <= ?)
                  AND (? IS NULL OR NV.MACN = ?)
                GROUP BY
                    CASE WHEN CTDV.MASP IS NOT NULL THEN CTDV.MASP  ELSE CTDV.MADC  END,
                    CASE WHEN CTDV.MASP IS NOT NULL THEN SP.TENSP    ELSE DC.TENDC   END,
                    CASE WHEN CTDV.MASP IS NOT NULL THEN 'Sản phẩm'  ELSE 'Dụng cụ' END
                ORDER BY DT_DICH_VU DESC
                """;

        LocalDate from   = criteria.getFromDate();
        LocalDate to     = criteria.getToDate();
        String    branch = normalizeBranch(criteria.getMaCn());

        List<ServiceRevenueRow> list = new ArrayList<>();
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, from == null ? null : Date.valueOf(from));
            ps.setDate(2, from == null ? null : Date.valueOf(from));
            ps.setDate(3, to   == null ? null : Date.valueOf(to));
            ps.setDate(4, to   == null ? null : Date.valueOf(to));
            ps.setString(5, branch);
            ps.setString(6, branch);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ServiceRevenueRow row = new ServiceRevenueRow();
                    row.setMaItem(rs.getString("MA_ITEM"));
                    row.setTenItem(rs.getString("TEN_ITEM"));
                    row.setLoai(rs.getString("LOAI"));
                    row.setDoanhThu(rs.getBigDecimal("DT_DICH_VU"));
                    list.add(row);
                }
            }
        }
        return list;
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Map<LocalDate, Double> queryByDay(String sql,
                                              LocalDate from, LocalDate to,
                                              String branch) throws SQLException {
        Map<LocalDate, Double> map = new LinkedHashMap<>();
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            ps.setString(3, branch);
            ps.setString(4, branch);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDate day = rs.getDate("NGAY_TRUNC").toLocalDate();
                    map.put(day, rs.getDouble("TONG"));
                }
            }
        }
        return map;
    }

    private RevenueRow mapRow(ResultSet rs) throws SQLException {
        RevenueRow row = new RevenueRow();
        row.setMaDt(rs.getString("MADT"));
        row.setMaCn(rs.getString("MACN"));
        row.setTenChiNhanh(rs.getString("TEN_CHI_NHANH"));
        row.setLoai(rs.getString("LOAI"));
        row.setNoiDung(rs.getString("NOIDUNG"));
        Date ngay = rs.getDate("NGAY");
        row.setNgay(ngay == null ? null : ngay.toLocalDate());
        Date batDau = rs.getDate("NGAY_BAT_DAU");
        row.setNgayBatDau(batDau == null ? null : batDau.toLocalDate());
        Date ketThuc = rs.getDate("NGAY_KET_THUC");
        row.setNgayKetThuc(ketThuc == null ? null : ketThuc.toLocalDate());
        row.setDtThueSan(rs.getBigDecimal("DT_THUE_SAN"));
        row.setDtDichVu(rs.getBigDecimal("DT_DICH_VU"));
        row.setTongDoanhThu(rs.getBigDecimal("TONGDOANHTHU"));
        return row;
    }

    @Override
    public void create(RevenueCreateRequest req) throws SQLException {
        String sql = """
                INSERT INTO DOANH_THU (MADT, MACN, LOAI, NOIDUNG, NGAY, NGAY_BAT_DAU, NGAY_KET_THUC,
                                       DT_THUE_SAN, DT_DICH_VU, TONGDOANHTHU, IS_DELETED)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)
                """;
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, req.getMaDt());
            ps.setString(2, req.getMaCn());
            ps.setString(3, req.getLoai());
            ps.setString(4, req.getNoiDung());
            ps.setDate(5, Date.valueOf(req.getNgay()));
            ps.setDate(6, Date.valueOf(req.getNgayBatDau()));
            ps.setDate(7, Date.valueOf(req.getNgayKetThuc()));
            ps.setBigDecimal(8, req.getDtThueSan());
            ps.setBigDecimal(9, req.getDtDichVu());
            ps.setBigDecimal(10, req.getTongDoanhThu());
            ps.executeUpdate();
        }
    }

    @Override
    public String generateNextId() throws SQLException {
        String sql = """
                SELECT NVL(MAX(TO_NUMBER(SUBSTR(MADT, 4))), 0) + 1 AS NEXT_NUM
                FROM DOANH_THU
                WHERE REGEXP_LIKE(MADT, '^DT-[0-9]+$')
                """;
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return "DT-" + rs.getInt("NEXT_NUM");
            }
        }
        return "DT-1";
    }

    private String normalizeBranch(String maCn) {
        if (maCn == null || maCn.isBlank()) return null;
        String trimmed = maCn.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) return null;
        String trimmed = keyword.trim().toUpperCase();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String toLike(String keyword) {
        return keyword == null ? null : "%" + keyword + "%";
    }
}
