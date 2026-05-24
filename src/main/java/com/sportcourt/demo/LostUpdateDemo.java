package com.sportcourt.demo;

import com.sportcourt.common.db.ConnectionUtils;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;

/**
 * Demo: Lost Update trong PRC_CAP_NHAT_DOANH_THU_NGAY_CHI_NHANH
 *
 * Procedure dùng pattern đọc-tính-ghi (recompute-and-overwrite):
 *   1. V_TOTAL = FN_TINH_DOANH_THU_NGAY_CHI_NHANH(macn, ngay)   ← đọc SUM
 *   2. UPDATE DOANH_THU SET TONGDOANHTHU = V_TOTAL                ← ghi đè
 *
 * Khi 2 session chạy đồng thời:
 *   - S1 đọc SUM trước khi S2 commit → thấy tập dữ liệu thiếu
 *   - S2 đọc SUM trước khi S1 commit → thấy tập dữ liệu thiếu
 *   - Phiên commit sau GHI ĐÈ kết quả của phiên commit trước → mất doanh thu
 *
 * So sánh: TRG_CAPNHAT_DOANHTHU_KH dùng DOANH_THU = DOANH_THU + delta → không bị lost update.
 *
 * Chạy: mvn exec:java -Dexec.mainClass="com.sportcourt.demo.LostUpdateDemo"
 */
public class LostUpdateDemo {

    // Dữ liệu demo — điều chỉnh nếu NV-1 không thuộc CN-1 trong DB của bạn
    private static final String MACN  = "CN-1";
    private static final String MANV  = "NV-1";   // phải thuộc CN-1
    private static final String MAKH_A = "KH-1";
    private static final String MAKH_B = "KH-2";
    private static final String MAHD_A = "HD-DEMO-A";
    private static final String MAHD_B = "HD-DEMO-B";
    private static final long   TONGTIEN_A = 500_000L;
    private static final long   TONGTIEN_B = 300_000L;

    private static final String SEP = "─".repeat(60);

    public static void main(String[] args) throws Exception {
        System.out.println("═".repeat(60));
        System.out.println("  DEMO LOST UPDATE — DOANH THU CHI NHANH");
        System.out.println("═".repeat(60));

        verifyEmployee();
        setup();

        System.out.println("\n" + SEP);
        System.out.println("  PHẦN 1: LOST UPDATE (chưa có locking)");
        System.out.println(SEP);
        runBuggyDemo();

        System.out.println("\n" + SEP);
        System.out.println("  PHẦN 2: FIX — SELECT FOR UPDATE trước khi tính");
        System.out.println(SEP);
        resetRevenue();
        runFixedDemo();

        cleanup();
        System.out.println("\nDemo hoàn thành.");
    }

    // ── Kiểm tra NV có trong CN-1 không ────────────────────────────

    private static void verifyEmployee() throws SQLException {
        String sql = "SELECT MACN FROM NHAN_VIEN WHERE MANV = ? AND IS_DELETED = 0";
        try (Connection c = ConnectionUtils.getMyConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, MANV);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new RuntimeException(MANV + " không tồn tại trong DB.");
                String macn = rs.getString(1);
                if (!MACN.equals(macn)) {
                    throw new RuntimeException(MANV + " thuộc " + macn + ", không phải " + MACN
                            + ". Hãy sửa hằng số MANV trong LostUpdateDemo.java.");
                }
            }
        }
        System.out.println("[Verify] " + MANV + " thuộc " + MACN + " — OK");
    }

    // ── Setup: tạo 2 hóa đơn demo ─────────────────────────────────

    private static void setup() throws SQLException {
        try (Connection c = ConnectionUtils.getMyConnection()) {
            c.setAutoCommit(false);

            // Xóa mềm dữ liệu demo cũ
            exec(c, "UPDATE HOA_DON SET IS_DELETED=1, TRANGTHAI='ĐÃ HUỶ' WHERE MAHD IN (?,?) AND IS_DELETED=0",
                    MAHD_A, MAHD_B);
            exec(c, "UPDATE DOANH_THU SET IS_DELETED=1 WHERE MADT=? AND IS_DELETED=0",
                    revenueMadt());

            // Tạo 2 hóa đơn CHƯA THANH TOÁN (TONGTIEN=0, sẽ update trong mỗi luồng)
            exec(c, "INSERT INTO HOA_DON(MAHD,MAKH,MANV,TIEN_COC,GIAMGIA,TONGGIATRI,TRANGTHAI,TONGTIEN,CREATED_AT,IS_DELETED)"
                            + " VALUES(?,?,?,0,0,0,'CHƯA THANH TOÁN',0,SYSDATE,0)",
                    MAHD_A, MAKH_A, MANV);
            exec(c, "INSERT INTO HOA_DON(MAHD,MAKH,MANV,TIEN_COC,GIAMGIA,TONGGIATRI,TRANGTHAI,TONGTIEN,CREATED_AT,IS_DELETED)"
                            + " VALUES(?,?,?,0,0,0,'CHƯA THANH TOÁN',0,SYSDATE,0)",
                    MAHD_B, MAKH_B, MANV);

            // Tạo trước dòng DOANH_THU hôm nay = 0
            // (pre-create để tránh PK conflict khi 2 luồng INSERT đồng thời)
            exec(c, "INSERT INTO DOANH_THU(MADT,MACN,LOAI,NOIDUNG,NGAY,NGAY_BAT_DAU,NGAY_KET_THUC,"
                            + "DT_THUE_SAN,DT_DICH_VU,TONGDOANHTHU,CREATED_AT,IS_DELETED)"
                            + " VALUES(?,'CN-1','NGAY','Demo lost update',TRUNC(SYSDATE),TRUNC(SYSDATE),TRUNC(SYSDATE),0,0,0,SYSDATE,0)",
                    revenueMadt());

            c.commit();
            System.out.println("[Setup] Tạo " + MAHD_A + " (500k), " + MAHD_B + " (300k), DOANH_THU = 0.");
        }
    }

    // ── Phần 1: Buggy — đọc-tính-ghi không có locking ─────────────

    private static void runBuggyDemo() throws Exception {
        // Latch để đồng bộ: Thread 1 pause sau khi đọc, Thread 2 đọc+ghi+commit trước
        CountDownLatch t1Read  = new CountDownLatch(1);  // T1 báo đã đọc xong
        CountDownLatch t2Done  = new CountDownLatch(1);  // T2 báo đã commit xong

        Thread t1 = new Thread(() -> {
            try (Connection conn = ConnectionUtils.getMyConnection()) {
                conn.setAutoCommit(false);

                // Bước 1: "Thanh toán" hóa đơn A trong transaction
                exec(conn,
                        "UPDATE HOA_DON SET TRANGTHAI='ĐÃ THANH TOÁN',TONGTIEN=?,TONGGIATRI=? WHERE MAHD=? AND IS_DELETED=0",
                        TONGTIEN_A, TONGTIEN_A, MAHD_A);

                // Bước 2: Đọc tổng doanh thu (inline FN_TINH)
                // — Oracle MVCC: S1 thấy HD-DEMO-A (cùng txn), KHÔNG thấy HD-DEMO-B (S2 chưa commit)
                long total = readRevenueSumInTxn(conn);
                System.out.printf("[S1] Đọc TONGDOANHTHU = %,d đ (chưa thấy HD-DEMO-B)%n", total);

                t1Read.countDown();  // báo T2 bắt đầu

                // Bước 3: Pause — để T2 chạy và commit trong thời gian này
                t2Done.await();

                // Bước 4: Ghi (STALE VALUE — không biết T2 đã commit 300k)
                writeRevenue(conn, total);
                conn.commit();
                System.out.printf("[S1] Ghi và COMMIT: TONGDOANHTHU = %,d đ%n", total);

            } catch (Exception e) {
                System.out.println("[S1] Lỗi: " + e.getMessage());
            }
        }, "Session-1");

        Thread t2 = new Thread(() -> {
            try {
                t1Read.await();  // chờ T1 đọc xong (nhưng chưa ghi)
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            try (Connection conn = ConnectionUtils.getMyConnection()) {
                conn.setAutoCommit(false);

                // Bước 1: "Thanh toán" hóa đơn B
                exec(conn,
                        "UPDATE HOA_DON SET TRANGTHAI='ĐÃ THANH TOÁN',TONGTIEN=?,TONGGIATRI=? WHERE MAHD=? AND IS_DELETED=0",
                        TONGTIEN_B, TONGTIEN_B, MAHD_B);

                // Bước 2: Đọc tổng — S2 thấy HD-DEMO-B, KHÔNG thấy HD-DEMO-A (T1 chưa commit)
                long total = readRevenueSumInTxn(conn);
                System.out.printf("[S2] Đọc TONGDOANHTHU = %,d đ (chưa thấy HD-DEMO-A)%n", total);

                // Bước 3: Ghi và commit
                writeRevenue(conn, total);
                conn.commit();
                System.out.printf("[S2] Ghi và COMMIT: TONGDOANHTHU = %,d đ%n", total);

                t2Done.countDown();  // cho phép T1 tiếp tục

            } catch (Exception e) {
                System.out.println("[S2] Lỗi: " + e.getMessage());
                t2Done.countDown();
            }
        }, "Session-2");

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        long actual   = getCurrentRevenue();
        long expected = TONGTIEN_A + TONGTIEN_B;
        System.out.println();
        System.out.printf("  Kỳ vọng  : %,d đ (A=%,d + B=%,d)%n", expected, TONGTIEN_A, TONGTIEN_B);
        System.out.printf("  Thực tế  : %,d đ%n", actual);
        System.out.printf("  Bị mất   : %,d đ  ← LOST UPDATE!%n", expected - actual);
        System.out.println();
        System.out.println("  Giải thích:");
        System.out.println("  S1 đọc SUM trước khi S2 commit → V_TOTAL_1 chỉ có A.");
        System.out.println("  S2 đọc SUM trước khi S1 commit → V_TOTAL_2 chỉ có B.");
        System.out.println("  S1 commit sau cùng → GHI ĐÈ S2's value bằng V_TOTAL_1 (thiếu B).");
    }

    // ── Reset doanh thu về 0 giữa 2 demo ──────────────────────────

    private static void resetRevenue() throws SQLException {
        try (Connection c = ConnectionUtils.getMyConnection()) {
            c.setAutoCommit(false);
            exec(c, "UPDATE HOA_DON SET TRANGTHAI='CHƯA THANH TOÁN',TONGTIEN=0,TONGGIATRI=0"
                    + " WHERE MAHD IN (?,?) AND IS_DELETED=0", MAHD_A, MAHD_B);
            exec(c, "UPDATE DOANH_THU SET TONGDOANHTHU=0 WHERE MADT=? AND IS_DELETED=0", revenueMadt());
            c.commit();
            System.out.println("[Reset] Đặt lại TONGDOANHTHU = 0, hoàn nguyên 2 hóa đơn về CHƯA THANH TOÁN.");
        }
    }

    // ── Phần 2: Fixed — SELECT FOR UPDATE trước khi đọc SUM ────────

    private static void runFixedDemo() throws Exception {
        CountDownLatch t1Locked = new CountDownLatch(1);
        CountDownLatch t2Done   = new CountDownLatch(1);

        Thread t1 = new Thread(() -> {
            try (Connection conn = ConnectionUtils.getMyConnection()) {
                conn.setAutoCommit(false);

                exec(conn,
                        "UPDATE HOA_DON SET TRANGTHAI='ĐÃ THANH TOÁN',TONGTIEN=?,TONGGIATRI=? WHERE MAHD=? AND IS_DELETED=0",
                        TONGTIEN_A, TONGTIEN_A, MAHD_A);

                // ★ FIX: Khóa dòng DOANH_THU TRƯỚC KHI đọc SUM
                // → S2 sẽ block tại đây cho đến khi S1 commit
                lockRevenueRow(conn);
                System.out.println("[S1-FIX] Đã khóa dòng DOANH_THU (S2 sẽ phải chờ).");

                t1Locked.countDown();  // báo T2 thử lock (sẽ bị block)

                // Đọc SUM SAU KHI đã giữ khóa → dữ liệu nhất quán trong S1's scope
                long total = readRevenueSumInTxn(conn);
                System.out.printf("[S1-FIX] Đọc TONGDOANHTHU = %,d đ%n", total);

                writeRevenue(conn, total);
                conn.commit();
                System.out.printf("[S1-FIX] Ghi và COMMIT: TONGDOANHTHU = %,d đ%n", total);

            } catch (Exception e) {
                System.out.println("[S1-FIX] Lỗi: " + e.getMessage());
            }
        }, "Session-1-Fix");

        Thread t2 = new Thread(() -> {
            try {
                t1Locked.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            try (Connection conn = ConnectionUtils.getMyConnection()) {
                conn.setAutoCommit(false);

                exec(conn,
                        "UPDATE HOA_DON SET TRANGTHAI='ĐÃ THANH TOÁN',TONGTIEN=?,TONGGIATRI=? WHERE MAHD=? AND IS_DELETED=0",
                        TONGTIEN_B, TONGTIEN_B, MAHD_B);

                // ★ FIX: S2 cũng khóa DOANH_THU trước khi đọc
                // → S2 BLOCK ở đây vì S1 đang giữ lock, chờ S1 commit
                System.out.println("[S2-FIX] Đang chờ khóa DOANH_THU (bị S1 chặn)...");
                lockRevenueRow(conn);  // unblock sau khi S1 commit
                System.out.println("[S2-FIX] Được giải phóng. Đọc lại SUM sau khi S1 commit.");

                // Đọc SUM SAU KHI S1 đã commit → thấy đủ cả A lẫn B
                long total = readRevenueSumInTxn(conn);
                System.out.printf("[S2-FIX] Đọc TONGDOANHTHU = %,d đ (thấy cả A + B)%n", total);

                writeRevenue(conn, total);
                conn.commit();
                System.out.printf("[S2-FIX] Ghi và COMMIT: TONGDOANHTHU = %,d đ%n", total);

                t2Done.countDown();

            } catch (Exception e) {
                System.out.println("[S2-FIX] Lỗi: " + e.getMessage());
                t2Done.countDown();
            }
        }, "Session-2-Fix");

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        long actual   = getCurrentRevenue();
        long expected = TONGTIEN_A + TONGTIEN_B;
        System.out.println();
        System.out.printf("  Kỳ vọng  : %,d đ%n", expected);
        System.out.printf("  Thực tế  : %,d đ%n", actual);
        System.out.printf("  Chênh lệch: %,d đ%n", expected - actual);
        if (actual == expected) {
            System.out.println("  → KẾT QUẢ ĐÚNG! SELECT FOR UPDATE đã ngăn lost update.");
        } else {
            System.out.println("  → VẪN SAI — kiểm tra lại logic.");
        }
    }

    // ── Cleanup ────────────────────────────────────────────────────

    private static void cleanup() throws SQLException {
        try (Connection c = ConnectionUtils.getMyConnection()) {
            c.setAutoCommit(false);
            exec(c, "UPDATE HOA_DON SET IS_DELETED=1, TRANGTHAI='ĐÃ HUỶ' WHERE MAHD IN (?,?) AND IS_DELETED=0",
                    MAHD_A, MAHD_B);
            exec(c, "UPDATE DOANH_THU SET IS_DELETED=1 WHERE MADT=? AND IS_DELETED=0", revenueMadt());
            c.commit();
            System.out.println("\n[Cleanup] Đã soft-delete dữ liệu demo.");
        }
    }

    // ── Helpers ────────────────────────────────────────────────────

    /** Đọc SUM TONGTIEN của tất cả hóa đơn ĐÃ THANH TOÁN hôm nay của CN-1.
     *  Đây chính là logic của FN_TINH_DOANH_THU_NGAY_CHI_NHANH.
     *  Chạy trong transaction của conn → Oracle MVCC đảm bảo chỉ thấy
     *  dữ liệu đã commit của session khác + dữ liệu uncommitted trong session này. */
    private static long readRevenueSumInTxn(Connection conn) throws SQLException {
        String sql = "SELECT NVL(SUM(HD.TONGTIEN), 0) " +
                     "FROM HOA_DON HD JOIN NHAN_VIEN NV ON NV.MANV = HD.MANV " +
                     "WHERE NV.MACN = ? " +
                     "  AND TRUNC(HD.CREATED_AT) = TRUNC(SYSDATE) " +
                     "  AND HD.TRANGTHAI = 'ĐÃ THANH TOÁN' " +
                     "  AND HD.IS_DELETED = 0";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, MACN);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        }
    }

    /** Ghi tổng doanh thu lên DOANH_THU (recompute-and-overwrite — mô phỏng procedure). */
    private static void writeRevenue(Connection conn, long total) throws SQLException {
        exec(conn,
                "UPDATE DOANH_THU SET TONGDOANHTHU = ? WHERE MADT = ? AND IS_DELETED = 0",
                total, revenueMadt());
    }

    /** Khóa dòng DOANH_THU bằng SELECT FOR UPDATE — phiên khác sẽ block cho đến khi commit. */
    private static void lockRevenueRow(Connection conn) throws SQLException {
        String sql = "SELECT TONGDOANHTHU FROM DOANH_THU WHERE MADT = ? AND IS_DELETED = 0 FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, revenueMadt());
            ps.executeQuery();
        }
    }

    /** Đọc TONGDOANHTHU hiện tại (autocommit, ngoài mọi transaction). */
    private static long getCurrentRevenue() throws SQLException {
        String sql = "SELECT TONGDOANHTHU FROM DOANH_THU WHERE MADT = ? AND IS_DELETED = 0";
        try (Connection c = ConnectionUtils.getMyConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, revenueMadt());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : -1L;
            }
        }
    }

    private static String revenueMadt() {
        return "DT-CN1-" + LocalDate.now().toString().replace("-", "");
    }

    /** Tiện ích execute UPDATE với tham số Object varargs. */
    private static void exec(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                if (params[i] instanceof Long)       ps.setLong(i + 1, (Long) params[i]);
                else if (params[i] instanceof Integer) ps.setInt(i + 1, (Integer) params[i]);
                else                                   ps.setString(i + 1, params[i].toString());
            }
            ps.executeUpdate();
        }
    }
}
