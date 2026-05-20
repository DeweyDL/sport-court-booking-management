CREATE OR REPLACE PACKAGE PKG_COURT_CTX AS
    G_INTERNAL_RECALC BOOLEAN := FALSE;
END PKG_COURT_CTX;
/

-- ============================================================
-- FN_LAY_GIA_THUE_SAN (MỚI)
-- Trích từ TRG_BIUD_CTHD_THUE_SAN_VALIDATE để tái sử dụng.
-- Validate: sân con và bảng giá cùng khu vực, còn hiệu lực.
-- Return: giá snapshot từ BANG_GIA.GIA
-- Raise error nếu:
--   - Sân con hoặc bảng giá không tồn tại / đã xóa mềm
--   - Sân con và bảng giá không cùng khu vực
-- ============================================================
CREATE OR REPLACE FUNCTION FN_LAY_GIA_THUE_SAN(
    P_MASAN IN SAN_CON.MASAN%TYPE,
    P_MABG  IN BANG_GIA.MABG%TYPE
) RETURN NUMBER
AS
    V_MAKV_SAN SAN_CON.MAKV%TYPE;
    V_MAKV_BG  BANG_GIA.MAKV%TYPE;
    V_GIA      BANG_GIA.GIA%TYPE;
BEGIN
    -- Lấy khu vực của sân con và khu vực + giá của bảng giá
    SELECT SC.MAKV, BG.MAKV, BG.GIA
    INTO V_MAKV_SAN, V_MAKV_BG, V_GIA
    FROM SAN_CON SC
             CROSS JOIN BANG_GIA BG
    WHERE SC.MASAN = P_MASAN
      AND BG.MABG = P_MABG
      AND SC.IS_DELETED = 0
      AND BG.IS_DELETED = 0;

    -- Kiểm tra sân con và bảng giá phải thuộc cùng khu vực
    IF V_MAKV_SAN <> V_MAKV_BG THEN
        RAISE_APPLICATION_ERROR(-20068, 'Bang gia khong thuoc cung khu vuc voi san con.');
    END IF;

    RETURN V_GIA;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20068, 'San con hoac bang gia khong ton tai, da xoa mem, hoac khong hop le.');
END;
/


-- ============================================================
-- PRC_THEM_BANG_GIA_THEO_KHUNG_GIO
-- Tao nhieu dong BANG_GIA 1 gio theo khoang gio bat dau.
-- Vi du P_GIO_BAT_DAU = 1, P_GIO_BAT_DAU_CUOI = 5:
-- insert 1-2, 2-3, 3-4, 4-5, 5-6 voi cung P_GIA.
-- MABG duoc sinh theo dang BG-<so tiep theo>.
-- ============================================================
CREATE OR REPLACE PROCEDURE PRC_THEM_BANG_GIA_THEO_KHUNG_GIO(
    P_MAKV             IN BANG_GIA.MAKV%TYPE,
    P_GIO_BAT_DAU      IN BANG_GIA.GIOBATDAU%TYPE,
    P_GIO_BAT_DAU_CUOI IN BANG_GIA.GIOBATDAU%TYPE,
    P_GIA              IN BANG_GIA.GIA%TYPE
)
AS
    V_COUNT       NUMBER := 0;
    V_NEXT_ID     NUMBER := 0;
    V_CURRENT_GIO NUMBER := 0;
BEGIN
    IF P_MAKV IS NULL THEN
        RAISE_APPLICATION_ERROR(-20130, 'MAKV khong duoc null.');
    END IF;

    IF P_GIA IS NULL OR P_GIA <= 0 THEN
        RAISE_APPLICATION_ERROR(-20131, 'Gia bang gia phai lon hon 0.');
    END IF;

    IF P_GIO_BAT_DAU IS NULL OR P_GIO_BAT_DAU_CUOI IS NULL THEN
        RAISE_APPLICATION_ERROR(-20132, 'Gio bat dau va gio bat dau cuoi khong duoc null.');
    END IF;

    IF P_GIO_BAT_DAU < 0 OR P_GIO_BAT_DAU > 23
        OR P_GIO_BAT_DAU_CUOI < 0 OR P_GIO_BAT_DAU_CUOI > 23 THEN
        RAISE_APPLICATION_ERROR(-20133, 'Gio phai nam trong khoang 0 den 23.');
    END IF;

    IF P_GIO_BAT_DAU_CUOI < P_GIO_BAT_DAU THEN
        RAISE_APPLICATION_ERROR(-20134, 'Gio bat dau cuoi phai lon hon hoac bang gio bat dau.');
    END IF;

    SELECT COUNT(1)
    INTO V_COUNT
    FROM KHU_VUC
    WHERE MAKV = P_MAKV
      AND IS_DELETED = 0;

    IF V_COUNT = 0 THEN
        RAISE_APPLICATION_ERROR(-20135, 'Khu vuc khong ton tai hoac da bi xoa.');
    END IF;

    LOCK TABLE BANG_GIA IN EXCLUSIVE MODE;

    SELECT COUNT(1)
    INTO V_COUNT
    FROM BANG_GIA
    WHERE MAKV = P_MAKV
      AND IS_DELETED = 0
      AND GIOBATDAU BETWEEN P_GIO_BAT_DAU AND P_GIO_BAT_DAU_CUOI;

    IF V_COUNT > 0 THEN
        RAISE_APPLICATION_ERROR(-20136, 'Da ton tai bang gia trong mot hoac nhieu khung gio da chon.');
    END IF;

    SELECT NVL(MAX(TO_NUMBER(REGEXP_SUBSTR(MABG, '[0-9]+$'))), 0) + 1
    INTO V_NEXT_ID
    FROM BANG_GIA
    WHERE REGEXP_LIKE(MABG, '^BG-[0-9]+$');

    V_CURRENT_GIO := P_GIO_BAT_DAU;
    WHILE V_CURRENT_GIO <= P_GIO_BAT_DAU_CUOI LOOP
        INSERT INTO BANG_GIA(MABG, MAKV, GIOBATDAU, GIOKETTHUC, GIA, CREATED_AT, IS_DELETED)
        VALUES ('BG-' || V_NEXT_ID, P_MAKV, V_CURRENT_GIO, V_CURRENT_GIO + 1, P_GIA, SYSDATE, 0);

        V_NEXT_ID := V_NEXT_ID + 1;
        V_CURRENT_GIO := V_CURRENT_GIO + 1;
    END LOOP;
END;
/


-- ============================================================
-- FN_LAY_GIA_DICH_VU (MỚI)
-- Trích từ TRG_BIUD_CTHD_DV_PRICE để tái sử dụng.
-- Validate: sản phẩm hoặc dụng cụ tồn tại, chưa xóa mềm.
-- Return: giá snapshot từ SAN_PHAM.GIA hoặc DUNG_CU_THE_THAO.GIA
-- Quy tắc RB53: chỉ đúng một trong hai MASP hoặc MADC có giá trị.
-- ============================================================
CREATE OR REPLACE FUNCTION FN_LAY_GIA_DICH_VU(
    P_MASP IN SAN_PHAM.MASP%TYPE,
    P_MADC IN DUNG_CU_THE_THAO.MADC%TYPE
) RETURN NUMBER
AS
    V_GIA NUMBER(12, 2);
BEGIN
    IF P_MASP IS NOT NULL AND P_MADC IS NULL THEN
        SELECT GIA
        INTO V_GIA
        FROM SAN_PHAM
        WHERE MASP = P_MASP
          AND IS_DELETED = 0;

    ELSIF P_MADC IS NOT NULL AND P_MASP IS NULL THEN
        SELECT GIA
        INTO V_GIA
        FROM DUNG_CU_THE_THAO
        WHERE MADC = P_MADC
          AND IS_DELETED = 0;

    ELSE
        -- Cả hai đều NULL hoặc cả hai đều có giá trị -> vi phạm RB53
        RAISE_APPLICATION_ERROR(-20053,
                                'Moi dong dich vu chi duoc chon dung mot trong hai: MASP hoac MADC.');
    END IF;

    RETURN V_GIA;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20052, 'San pham/dung cu khong ton tai hoac da bi xoa.');
END;
/


-- ============================================================
-- PRC_CAP_NHAT_SO_TIEN_HOA_DON
-- Tinh lai hoa don bang gia snapshot:
--   TONGGIATRI = SUM(DON_GIA_THUE) + SUM(SL * DON_GIA)
--   GIAM_HANG = SUM(DON_GIA_THUE) * CHIET_KHAU_HANG / 100
--   GIAM_HOA_DON = (TONGGIATRI - GIAM_HANG) * GIAMGIA / 100
--   TONGTIEN = TONGGIATRI - GIAM_HANG - GIAM_HOA_DON - TIEN_COC
-- Quy uoc nghiep vu theo yeu cau:
--   TIEN_COC = 0  => khach choi ngay
--   TIEN_COC > 0  => hoa don dat truoc va phai bang 70% tien thue san
-- ============================================================
CREATE OR REPLACE PROCEDURE PRC_CAP_NHAT_SO_TIEN_HOA_DON(
    P_MAHD IN HOA_DON.MAHD%TYPE
)
AS
    V_MAKH          HOA_DON.MAKH%TYPE;
    V_GIAMGIA       HOA_DON.GIAMGIA%TYPE  := 0;
    V_TIEN_COC      HOA_DON.TIEN_COC%TYPE := 0;
    V_MA_HANG       KHACH_HANG.MA_HANG%TYPE;
    V_CHIET_KHAU    NUMBER(12, 2)         := 0;
    V_TIEN_THUE_SAN NUMBER(12, 2)         := 0;
    V_TIEN_DICH_VU  NUMBER(12, 2)         := 0;
    V_TONG_GIA_TRI  NUMBER(12, 2)         := 0;
    V_GIAM_HANG     NUMBER(12, 2)         := 0;
    V_GIAM_HOA_DON  NUMBER(12, 2)         := 0;
    V_TONG_TIEN     NUMBER(12, 2)         := 0;
BEGIN
    SELECT MAKH, NVL(GIAMGIA, 0), NVL(TIEN_COC, 0)
    INTO V_MAKH, V_GIAMGIA, V_TIEN_COC
    FROM HOA_DON
    WHERE MAHD = P_MAHD
      AND IS_DELETED = 0
        FOR UPDATE;

    IF V_GIAMGIA < 0 OR V_GIAMGIA > 100 THEN
        RAISE_APPLICATION_ERROR(-20057, 'GIAMGIA phai nam trong khoang 0 den 100.');
    END IF;

    IF V_TIEN_COC < 0 THEN
        RAISE_APPLICATION_ERROR(-20058, 'TIEN_COC khong duoc am.');
    END IF;

    SELECT NVL(SUM(CT.DON_GIA_THUE), 0)
    INTO V_TIEN_THUE_SAN
    FROM CHI_TIET_HOA_DON_THUE_SAN CT
    WHERE CT.MAHD = P_MAHD
      AND CT.IS_DELETED = 0
      AND CT.TRANGTHAI <> 'ĐÃ HUỶ';

    SELECT NVL(SUM(CT.SL * CT.DON_GIA), 0)
    INTO V_TIEN_DICH_VU
    FROM CHI_TIET_HOA_DON_DICH_VU_DA_DUNG CT
    WHERE CT.MAHD = P_MAHD
      AND CT.IS_DELETED = 0;


    IF V_TIEN_COC > 0 AND V_TIEN_THUE_SAN > 0 THEN
        IF ABS(V_TIEN_COC - ROUND(V_TIEN_THUE_SAN * 0.7, 2)) > 0.01 THEN
            RAISE_APPLICATION_ERROR(-20058, 'TIEN_COC phai bang 70% tong tien thue san cua hoa don dat truoc.');
        END IF;
    END IF;

    BEGIN
        SELECT KH.MA_HANG
        INTO V_MA_HANG
        FROM KHACH_HANG KH
        WHERE KH.MAKH = V_MAKH
          AND KH.IS_DELETED = 0;

        IF V_MA_HANG IS NOT NULL THEN
            BEGIN
                SELECT NVL(HK.CHIET_KHAU, 0)
                INTO V_CHIET_KHAU
                FROM HANG_KHACH_HANG HK
                WHERE HK.MA_HANG = V_MA_HANG
                  AND HK.IS_DELETED = 0;
            EXCEPTION
                WHEN NO_DATA_FOUND THEN
                    V_CHIET_KHAU := 0;
            END;
        END IF;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            V_CHIET_KHAU := 0;
    END;

    V_TONG_GIA_TRI := ROUND(V_TIEN_THUE_SAN + V_TIEN_DICH_VU, 2);
    V_GIAM_HANG := ROUND(V_TIEN_THUE_SAN * V_CHIET_KHAU / 100, 2);
    V_GIAM_HOA_DON := ROUND((V_TONG_GIA_TRI - V_GIAM_HANG) * V_GIAMGIA / 100, 2);
    V_TONG_TIEN := ROUND(V_TONG_GIA_TRI - V_GIAM_HANG - V_GIAM_HOA_DON - V_TIEN_COC, 2);

    -- [FIX] Cho phép TONGTIEN <= 0 khi tất cả chi tiết đã huỷ
    -- (hoá đơn sẽ bị huỷ ngay sau đó, TONGTIEN không còn ý nghĩa).
    -- Chỉ raise error khi hoá đơn CÒN chi tiết hợp lệ mà TONGTIEN âm.
    IF V_TONG_TIEN < 0 AND V_TIEN_THUE_SAN > 0 THEN
        RAISE_APPLICATION_ERROR(-20059, 'TONGTIEN khong duoc am.');
    END IF;

    PKG_COURT_CTX.G_INTERNAL_RECALC := TRUE;

    UPDATE HOA_DON
    SET TONGGIATRI = V_TONG_GIA_TRI,
        TONGTIEN   = V_TONG_TIEN
    WHERE MAHD = P_MAHD;

    PKG_COURT_CTX.G_INTERNAL_RECALC := FALSE;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        PKG_COURT_CTX.G_INTERNAL_RECALC := FALSE;
        NULL;
    WHEN OTHERS THEN
        PKG_COURT_CTX.G_INTERNAL_RECALC := FALSE;
        RAISE;
END;
/


-- ============================================================
-- PRC_DIEU_CHINH_TON_SAN_PHAM
-- P_DELTA > 0: cong ton; P_DELTA < 0: tru ton.
-- Lock row ton kho de tranh race condition khi xuat vuot ton.
-- ============================================================
CREATE OR REPLACE PROCEDURE PRC_DIEU_CHINH_TON_SAN_PHAM(
    P_MASP  IN SAN_PHAM.MASP%TYPE,
    P_DELTA IN NUMBER
)
AS
    V_SL_TON SAN_PHAM.SL_TON%TYPE;
BEGIN
    IF P_MASP IS NULL OR NVL(P_DELTA, 0) = 0 THEN
        RETURN;
    END IF;

    SELECT SL_TON
    INTO V_SL_TON
    FROM SAN_PHAM
    WHERE MASP = P_MASP
      AND IS_DELETED = 0
        FOR UPDATE;

    IF V_SL_TON + P_DELTA < 0 THEN
        RAISE_APPLICATION_ERROR(-20090, 'So luong ton san pham khong du.');
    END IF;

    UPDATE SAN_PHAM
    SET SL_TON = SL_TON + P_DELTA
    WHERE MASP = P_MASP
      AND IS_DELETED = 0;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20092, 'San pham khong ton tai hoac da bi xoa.');
END;
/


-- ============================================================
-- PRC_DIEU_CHINH_TON_DUNG_CU
-- P_DELTA > 0: tra/cong ton; P_DELTA < 0: thue/tru ton.
-- Lock row ton kho de tranh race condition khi thue vuot ton.
-- ============================================================
CREATE OR REPLACE PROCEDURE PRC_DIEU_CHINH_TON_DUNG_CU(
    P_MADC  IN DUNG_CU_THE_THAO.MADC%TYPE,
    P_DELTA IN NUMBER
)
AS
    V_SL_TON DUNG_CU_THE_THAO.SL_TON%TYPE;
BEGIN
    IF P_MADC IS NULL OR NVL(P_DELTA, 0) = 0 THEN
        RETURN;
    END IF;

    SELECT SL_TON
    INTO V_SL_TON
    FROM DUNG_CU_THE_THAO
    WHERE MADC = P_MADC
      AND IS_DELETED = 0
        FOR UPDATE;

    IF V_SL_TON + P_DELTA < 0 THEN
        RAISE_APPLICATION_ERROR(-20091, 'So luong ton dung cu khong du.');
    END IF;

    UPDATE DUNG_CU_THE_THAO
    SET SL_TON = SL_TON + P_DELTA
    WHERE MADC = P_MADC
      AND IS_DELETED = 0;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20093, 'Dung cu khong ton tai hoac da bi xoa.');
END;
/


-- ============================================================
-- PRC_CAP_NHAT_THANH_TOAN_CHI_TIET_HD (RB63)
-- Khi HOA_DON chuyen sang DA THANH TOAN,
-- chuyen toan bo chi tiet DANG SU DUNG -> DA HOAN THANH.
-- ============================================================
CREATE OR REPLACE PROCEDURE PRC_CAP_NHAT_THANH_TOAN_CHI_TIET_HD(P_MAHD IN HOA_DON.MAHD%TYPE)
AS
BEGIN
    UPDATE CHI_TIET_HOA_DON_THUE_SAN
    SET TRANGTHAI = 'ĐÃ HOÀN THÀNH'
    WHERE MAHD = P_MAHD
      AND TRANGTHAI = 'ĐANG SỬ DỤNG'
      AND IS_DELETED = 0;

    UPDATE CHI_TIET_HOA_DON_DICH_VU_DA_DUNG
    SET TRANGTHAI = 'ĐÃ HOÀN THÀNH'
    WHERE MAHD = P_MAHD
      AND TRANGTHAI = 'ĐANG SỬ DỤNG'
      AND IS_DELETED = 0;
END;
/


-- ============================================================
-- FN_KIEM_TRA_SAN_TRONG (RB66)
-- Return 1 nếu sân trống, 0 nếu đã có lịch.
-- ============================================================
CREATE OR REPLACE FUNCTION FN_KIEM_TRA_SAN_TRONG(
    P_MASAN    IN SAN_CON.MASAN%TYPE,
    P_NGAYTHUE IN DATE,
    P_MABG     IN BANG_GIA.MABG%TYPE
) RETURN NUMBER
AS
    V_COUNT NUMBER(1);
BEGIN
    SELECT COUNT(1)
    INTO V_COUNT
    FROM CHI_TIET_HOA_DON_THUE_SAN
    WHERE MASAN = P_MASAN
      AND TRUNC(NGAYTHUE) = TRUNC(P_NGAYTHUE)
      AND MABG = P_MABG
      AND IS_DELETED = 0
      AND TRANGTHAI <> 'ĐÃ HUỶ';

    RETURN CASE WHEN V_COUNT = 0 THEN 1 ELSE 0 END;
END;
/


-- ============================================================
-- FN_TINH_DOANH_THU_NGAY_CHI_NHANH
-- Tinh tong doanh thu cua mot chi nhanh trong mot ngay.
-- ============================================================
CREATE OR REPLACE FUNCTION FN_TINH_DOANH_THU_NGAY_CHI_NHANH(
    P_MACN IN CHI_NHANH.MACN%TYPE,
    P_NGAY IN DATE
) RETURN NUMBER
AS
    V_TOTAL DOANH_THU.TONGDOANHTHU%TYPE;
BEGIN
    SELECT NVL(SUM(HD.TONGTIEN), 0)
    INTO V_TOTAL
    FROM HOA_DON HD
             JOIN NHAN_VIEN NV ON NV.MANV = HD.MANV
    WHERE NV.MACN = P_MACN
      AND TRUNC(HD.CREATED_AT) = TRUNC(P_NGAY)
      AND HD.TRANGTHAI = 'ĐÃ THANH TOÁN'
      AND HD.IS_DELETED = 0;
    RETURN V_TOTAL;
END;
/


-- ============================================================
-- FN_TIM_HANG_KHACH_HANG (RB70)
-- Tim hang khach hang phu hop nhat dua tren doanh thu.
-- Lay hang co MUC_TIEN cao nhat ma doanh thu >= MUC_TIEN.
-- ============================================================
CREATE OR REPLACE FUNCTION FN_TIM_HANG_KHACH_HANG(
    P_DOANH_THU IN NUMBER
) RETURN HANG_KHACH_HANG.MA_HANG%TYPE
AS
    V_HANG_KHACH_HANG HANG_KHACH_HANG.MA_HANG%TYPE;
BEGIN
    SELECT MA_HANG
    INTO V_HANG_KHACH_HANG
    FROM HANG_KHACH_HANG
    WHERE MUC_TIEN <= P_DOANH_THU
      AND IS_DELETED = 0
    ORDER BY MUC_TIEN DESC
        FETCH FIRST 1 ROW ONLY;

    RETURN V_HANG_KHACH_HANG;
END;
/


-- ============================================================
-- PRC_TAO_HOA_DON
-- Tao moi hoa don voi trang thai CHUA THANH TOAN.
-- ============================================================
CREATE OR REPLACE PROCEDURE PRC_TAO_HOA_DON(
    P_MAHD     IN HOA_DON.MAHD%TYPE,
    P_MAKH     IN KHACH_HANG.MAKH%TYPE,
    P_MANV     IN NHAN_VIEN.MANV%TYPE,
    P_GIAMGIA  IN HOA_DON.GIAMGIA%TYPE DEFAULT 0,
    P_TIEN_COC IN HOA_DON.TIEN_COC%TYPE
)
AS
    V_COUNT NUMBER := 0;
BEGIN
    IF P_MAHD IS NULL THEN
        RAISE_APPLICATION_ERROR(-20110, 'MAHD khong duoc null.');
    END IF;

    IF NVL(P_GIAMGIA, 0) < 0 OR NVL(P_GIAMGIA, 0) > 100 THEN
        RAISE_APPLICATION_ERROR(-20111, 'GIAMGIA phai nam trong khoang 0 den 100.');
    END IF;

    IF NVL(P_TIEN_COC, 0) < 0 THEN
        RAISE_APPLICATION_ERROR(-20112, 'TIEN_COC khong duoc am.');
    END IF;

    SELECT COUNT(1)
    INTO V_COUNT
    FROM KHACH_HANG
    WHERE MAKH = P_MAKH
      AND IS_DELETED = 0;

    IF V_COUNT = 0 THEN
        RAISE_APPLICATION_ERROR(-20113, 'Khach hang khong ton tai hoac da bi xoa.');
    END IF;

    SELECT COUNT(1)
    INTO V_COUNT
    FROM NHAN_VIEN
    WHERE MANV = P_MANV
      AND IS_DELETED = 0;

    IF V_COUNT = 0 THEN
        RAISE_APPLICATION_ERROR(-20114, 'Nhan vien khong ton tai hoac da bi xoa.');
    END IF;

    INSERT INTO HOA_DON(MAHD, MAKH, MANV, TIEN_COC, GIAMGIA,
                        TONGGIATRI, TRANGTHAI, TONGTIEN, CREATED_AT, IS_DELETED)
    VALUES (P_MAHD, P_MAKH, P_MANV, NVL(P_TIEN_COC, 0), NVL(P_GIAMGIA, 0),
            0, 'CHƯA THANH TOÁN', 0, SYSDATE, 0);
END;
/


-- ============================================================
-- PRC_THEM_CHI_TIET_THUE_SAN
-- Them chi tiet thue san vao hoa don.
-- Goi FN_KIEM_TRA_SAN_TRONG de kiem tra trung lich.
-- Trigger se tu dong gan DON_GIA_THUE snapshot.
-- ============================================================
CREATE OR REPLACE PROCEDURE PRC_THEM_CHI_TIET_THUE_SAN(
    P_MACT_THUE_SAN IN CHI_TIET_HOA_DON_THUE_SAN.MACT_THUE_SAN%TYPE,
    P_MAHD          IN HOA_DON.MAHD%TYPE,
    P_MASAN         IN SAN_CON.MASAN%TYPE,
    P_MABG          IN BANG_GIA.MABG%TYPE,
    P_NGAYTHUE      IN DATE,
    P_TRANGTHAI     IN CHI_TIET_HOA_DON_THUE_SAN.TRANGTHAI%TYPE DEFAULT 'ĐÃ XÁC NHẬN'
)
AS
    V_COUNT NUMBER := 0;
BEGIN
    IF P_MACT_THUE_SAN IS NULL THEN
        RAISE_APPLICATION_ERROR(-20120, 'MACT_THUE_SAN khong duoc null.');
    END IF;

    SELECT COUNT(1)
    INTO V_COUNT
    FROM HOA_DON
    WHERE MAHD = P_MAHD
      AND TRANGTHAI = 'CHƯA THANH TOÁN'
      AND IS_DELETED = 0;

    IF V_COUNT = 0 THEN
        RAISE_APPLICATION_ERROR(-20121, 'Hoa don khong ton tai hoac khong o trang thai CHUA THANH TOAN.');
    END IF;

    SELECT COUNT(1)
    INTO V_COUNT
    FROM SAN_CON SC
             JOIN BANG_GIA BG ON BG.MAKV = SC.MAKV
    WHERE SC.MASAN = P_MASAN
      AND BG.MABG = P_MABG
      AND SC.TRANGTHAI = 'ĐANG HOẠT ĐỘNG'
      AND SC.IS_DELETED = 0
      AND BG.IS_DELETED = 0;

    IF V_COUNT = 0 THEN
        RAISE_APPLICATION_ERROR(-20122, 'San con/bang gia khong hop le, da xoa, bao tri hoac khong cung khu vuc.');
    END IF;

    IF FN_KIEM_TRA_SAN_TRONG(P_MASAN, P_NGAYTHUE, P_MABG) = 0 THEN
        RAISE_APPLICATION_ERROR(-20123, 'San da co lich o ngay va khung gio nay.');
    END IF;

    INSERT INTO CHI_TIET_HOA_DON_THUE_SAN(MACT_THUE_SAN, MAHD, MASAN, MABG,
                                          NGAYTHUE, TRANGTHAI, CREATED_AT, IS_DELETED)
    VALUES (P_MACT_THUE_SAN, P_MAHD, P_MASAN, P_MABG,
            P_NGAYTHUE, NVL(P_TRANGTHAI, 'ĐÃ XÁC NHẬN'), SYSDATE, 0);
END;
/


-- ============================================================
-- PRC_THEM_CHI_TIET_DICH_VU
-- Them san pham ban kem hoac dung cu cho thue vao hoa don.
-- ============================================================
CREATE OR REPLACE PROCEDURE PRC_THEM_CHI_TIET_DICH_VU(
    P_MACT_DICH_VU IN CHI_TIET_HOA_DON_DICH_VU_DA_DUNG.MACT_DICH_VU%TYPE,
    P_MAHD         IN HOA_DON.MAHD%TYPE,
    P_MASP         IN SAN_PHAM.MASP%TYPE DEFAULT NULL,
    P_MADC         IN DUNG_CU_THE_THAO.MADC%TYPE DEFAULT NULL,
    P_SL           IN NUMBER,
    P_TRANGTHAI    IN CHI_TIET_HOA_DON_DICH_VU_DA_DUNG.TRANGTHAI%TYPE DEFAULT 'ĐANG SỬ DỤNG'
)
AS
    V_COUNT NUMBER;
BEGIN
    IF P_MACT_DICH_VU IS NULL THEN
        RAISE_APPLICATION_ERROR(-20130, 'MACT_DICH_VU khong duoc null.');
    END IF;

    IF NVL(P_SL, 0) <= 0 THEN
        RAISE_APPLICATION_ERROR(-20131, 'So luong dich vu phai lon hon 0.');
    END IF;

    IF (P_MASP IS NULL AND P_MADC IS NULL) OR (P_MASP IS NOT NULL AND P_MADC IS NOT NULL) THEN
        RAISE_APPLICATION_ERROR(-20132, 'Moi dong dich vu chi duoc chon dung mot trong hai: MASP hoac MADC.');
    END IF;

    IF NVL(P_TRANGTHAI, 'ĐANG SỬ DỤNG') NOT IN ('ĐANG SỬ DỤNG', 'ĐÃ HOÀN THÀNH') THEN
        RAISE_APPLICATION_ERROR(-20133, 'Trang thai dich vu khong hop le.');
    END IF;

    SELECT COUNT(1)
    INTO V_COUNT
    FROM HOA_DON
    WHERE MAHD = P_MAHD
      AND TRANGTHAI = 'CHƯA THANH TOÁN'
      AND IS_DELETED = 0;

    IF V_COUNT = 0 THEN
        RAISE_APPLICATION_ERROR(-20134, 'Hoa don khong ton tai hoac khong o trang thai CHUA THANH TOAN.');
    END IF;

    IF P_MASP IS NOT NULL THEN
        SELECT COUNT(1)
        INTO V_COUNT
        FROM SAN_PHAM
        WHERE MASP = P_MASP
          AND IS_DELETED = 0;

        IF V_COUNT = 0 THEN
            RAISE_APPLICATION_ERROR(-20135, 'San pham khong ton tai hoac da bi xoa.');
        END IF;
    ELSE
        SELECT COUNT(1)
        INTO V_COUNT
        FROM DUNG_CU_THE_THAO
        WHERE MADC = P_MADC
          AND IS_DELETED = 0;

        IF V_COUNT = 0 THEN
            RAISE_APPLICATION_ERROR(-20136, 'Dung cu khong ton tai hoac da bi xoa.');
        END IF;
    END IF;

    INSERT INTO CHI_TIET_HOA_DON_DICH_VU_DA_DUNG(MACT_DICH_VU, MAHD, MASP, MADC,
                                                 SL, TRANGTHAI, CREATED_AT, IS_DELETED)
    VALUES (P_MACT_DICH_VU, P_MAHD, P_MASP, P_MADC,
            P_SL, NVL(P_TRANGTHAI, 'ĐANG SỬ DỤNG'), SYSDATE, 0);
END;
/


-- ============================================================
-- PRC_XAC_NHAN_KHACH_DEN_SAN (RB64)
-- Chuyen chi tiet thue san tu DA XAC NHAN -> DANG SU DUNG.
-- Trigger TRG_BIUD_CTHD_THUE_SAN_VALIDATE se kiem tra:
--   - Chi tiet phai o trang thai DA XAC NHAN
--   - San con phai DANG HOAT DONG
-- ============================================================
CREATE OR REPLACE PROCEDURE PRC_XAC_NHAN_KHACH_DEN_SAN(
    P_MACT_THUE_SAN IN CHI_TIET_HOA_DON_THUE_SAN.MACT_THUE_SAN%TYPE
)
AS
BEGIN
    UPDATE CHI_TIET_HOA_DON_THUE_SAN
    SET TRANGTHAI = 'ĐANG SỬ DỤNG'
    WHERE MACT_THUE_SAN = P_MACT_THUE_SAN
      AND TRANGTHAI = 'ĐÃ XÁC NHẬN'
      AND IS_DELETED = 0;
    IF SQL%ROWCOUNT = 0 THEN
        RAISE_APPLICATION_ERROR(-20140,
                                'Khong the xac nhan nhan san. Chi tiet khong ton tai hoac khong o trang thai DA XAC NHAN.');
    END IF;
END;
/


-- ============================================================
-- PRC_THANH_TOAN_HOA_DON (RB62)
-- Kiem tra dieu kien thanh toan, tinh lai tien,
-- chuyen trang thai hoa don -> DA THANH TOAN.
-- Trigger TRG_AU_HOA_DON_COMPLETE_DETAILS se tu dong
-- chuyen cac chi tiet DANG SU DUNG -> DA HOAN THANH.
-- ============================================================
CREATE OR REPLACE PROCEDURE PRC_THANH_TOAN_HOA_DON(
    P_MAHD IN HOA_DON.MAHD%TYPE
)
AS
    V_TRANGTHAI HOA_DON.TRANGTHAI%TYPE;
    V_COUNT     NUMBER;
BEGIN
    SELECT TRANGTHAI
    INTO V_TRANGTHAI
    FROM HOA_DON
    WHERE MAHD = P_MAHD
      AND IS_DELETED = 0
        FOR UPDATE;

    IF V_TRANGTHAI <> 'CHƯA THANH TOÁN' THEN
        RAISE_APPLICATION_ERROR(-20150, 'Chi duoc thanh toan hoa don dang o trang thai CHUA THANH TOAN.');
    END IF;

    SELECT COUNT(1)
    INTO V_COUNT
    FROM CHI_TIET_HOA_DON_THUE_SAN
    WHERE MAHD = P_MAHD
      AND IS_DELETED = 0
      AND TRANGTHAI IN ('ĐANG SỬ DỤNG', 'ĐÃ HOÀN THÀNH');

    IF V_COUNT = 0 THEN
        RAISE_APPLICATION_ERROR(-20151, 'Hoa don chua co chi tiet thue san dang su dung/da hoan thanh de thanh toan.');
    END IF;

    SELECT COUNT(1)
    INTO V_COUNT
    FROM CHI_TIET_HOA_DON_THUE_SAN
    WHERE MAHD = P_MAHD
      AND IS_DELETED = 0
      AND TRANGTHAI IN ('ĐÃ ĐẶT CHỜ CỌC', 'ĐÃ CỌC CHỜ XÁC NHẬN', 'ĐÃ CỌC', 'ĐÃ XÁC NHẬN');

    IF V_COUNT > 0 THEN
        RAISE_APPLICATION_ERROR(-20152, 'Khong the thanh toan khi con chi tiet thue san chua bat dau su dung.');
    END IF;

    PRC_CAP_NHAT_SO_TIEN_HOA_DON(P_MAHD);

    UPDATE HOA_DON
    SET TRANGTHAI = 'ĐÃ THANH TOÁN'
    WHERE MAHD = P_MAHD
      AND IS_DELETED = 0;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20153, 'Hoa don khong ton tai hoac da bi xoa.');
END;
/


-- ============================================================
-- PRC_HUY_CHI_TIET_THUE_SAN (CẬP NHẬT)
-- Huỷ chi tiết thuê sân + áp dụng quy tắc hoàn cọc:
--   - Huỷ TRƯỚC >= 2 ngày so với NGAYTHUE → hoàn 100% cọc
--   - Huỷ TRONG vòng < 2 ngày → KHÔNG hoàn cọc
-- Nếu huỷ hết chi tiết → huỷ hoá đơn.
-- Nếu còn chi tiết → cập nhật lại TIEN_COC = 70% phần còn lại.
--
-- Luồng xử lý:
--   1. Validate trạng thái hoá đơn + chi tiết
--   2. Tính số ngày còn lại (quy tắc hoàn cọc)
--   3. Đếm chi tiết còn lại (trừ cái đang huỷ)
--   4. Cập nhật TIEN_COC trước (dùng G_INTERNAL_RECALC tránh recalc đệ quy)
--   5. Huỷ chi tiết (compound trigger tự recalc TONGGIATRI/TONGTIEN)
--   6. Huỷ hoá đơn nếu hết chi tiết
-- ============================================================
CREATE OR REPLACE PROCEDURE PRC_HUY_CHI_TIET_THUE_SAN(
    P_MACT_THUE_SAN IN CHI_TIET_HOA_DON_THUE_SAN.MACT_THUE_SAN%TYPE
)
AS
    V_MAHD          HOA_DON.MAHD%TYPE;
    V_HD_STATUS     HOA_DON.TRANGTHAI%TYPE;
    V_CT_STATUS     CHI_TIET_HOA_DON_THUE_SAN.TRANGTHAI%TYPE;
    V_NGAYTHUE      CHI_TIET_HOA_DON_THUE_SAN.NGAYTHUE%TYPE;
    V_TIEN_COC_CU   HOA_DON.TIEN_COC%TYPE;
    V_NGAY_CON_LAI  NUMBER;
    V_REMAINING     NUMBER;
    V_TONG_SAN_MOI  NUMBER(12, 2) := 0;
    V_TIEN_COC_MOI  NUMBER(12, 2) := 0;
BEGIN
    -- ====== BƯỚC 1: Lấy thông tin + khoá dòng ======
    SELECT CT.MAHD, CT.TRANGTHAI, CT.NGAYTHUE,
           HD.TRANGTHAI, HD.TIEN_COC
    INTO V_MAHD, V_CT_STATUS, V_NGAYTHUE,
         V_HD_STATUS, V_TIEN_COC_CU
    FROM CHI_TIET_HOA_DON_THUE_SAN CT
             JOIN HOA_DON HD ON HD.MAHD = CT.MAHD
    WHERE CT.MACT_THUE_SAN = P_MACT_THUE_SAN
      AND CT.IS_DELETED = 0
      AND HD.IS_DELETED = 0
        FOR UPDATE;

    -- ====== BƯỚC 2: Validate ======
    IF V_HD_STATUS <> 'CHƯA THANH TOÁN' THEN
        RAISE_APPLICATION_ERROR(-20160, 'Chi duoc huy chi tiet cua hoa don CHUA THANH TOAN.');
    END IF;

    IF V_CT_STATUS IN ('ĐÃ HUỶ', 'ĐÃ HOÀN THÀNH') THEN
        RAISE_APPLICATION_ERROR(-20161, 'Chi tiet thue san da huy hoac da hoan thanh, khong the huy.');
    END IF;

    -- ====== BƯỚC 3: Tính số ngày còn lại (quy tắc hoàn cọc) ======
    V_NGAY_CON_LAI := TRUNC(V_NGAYTHUE) - TRUNC(SYSDATE);

    -- ====== BƯỚC 4: Đếm chi tiết còn lại SAU KHI huỷ cái này ======
    SELECT COUNT(1)
    INTO V_REMAINING
    FROM CHI_TIET_HOA_DON_THUE_SAN
    WHERE MAHD = V_MAHD
      AND IS_DELETED = 0
      AND TRANGTHAI <> 'ĐÃ HUỶ'
      AND MACT_THUE_SAN <> P_MACT_THUE_SAN;

    -- ====== BƯỚC 5: Cập nhật TIEN_COC TRƯỚC khi huỷ chi tiết ======
    -- (Tránh PRC_CAP_NHAT_SO_TIEN_HOA_DON bị lỗi khi compound trigger fire)
    IF V_TIEN_COC_CU > 0 THEN
        PKG_COURT_CTX.G_INTERNAL_RECALC := TRUE;

        IF V_REMAINING = 0 THEN
            -- Đây là chi tiết cuối cùng
            IF V_NGAY_CON_LAI < 2 THEN
                -- Huỷ trong vòng 2 ngày → KHÔNG hoàn cọc → giữ TIEN_COC
                NULL;
            ELSE
                -- Huỷ trước >= 2 ngày → hoàn 100% cọc → TIEN_COC = 0
                UPDATE HOA_DON
                SET TIEN_COC = 0
                WHERE MAHD = V_MAHD AND IS_DELETED = 0;
            END IF;
        ELSE
            -- Còn chi tiết khác → TIEN_COC = 70% tổng tiền thuê sân còn lại
            SELECT NVL(SUM(DON_GIA_THUE), 0)
            INTO V_TONG_SAN_MOI
            FROM CHI_TIET_HOA_DON_THUE_SAN
            WHERE MAHD = V_MAHD
              AND IS_DELETED = 0
              AND TRANGTHAI <> 'ĐÃ HUỶ'
              AND MACT_THUE_SAN <> P_MACT_THUE_SAN;

            V_TIEN_COC_MOI := ROUND(V_TONG_SAN_MOI * 0.7, 2);

            UPDATE HOA_DON
            SET TIEN_COC = V_TIEN_COC_MOI
            WHERE MAHD = V_MAHD AND IS_DELETED = 0;
        END IF;

        PKG_COURT_CTX.G_INTERNAL_RECALC := FALSE;
    END IF;

    -- ====== BƯỚC 6: Huỷ chi tiết ======
    -- Compound trigger TRG_FIUD_CTHD_THUE_SAN_RECALC sẽ fire
    -- và gọi PRC_CAP_NHAT_SO_TIEN_HOA_DON để tính lại TONGGIATRI/TONGTIEN
    UPDATE CHI_TIET_HOA_DON_THUE_SAN
    SET TRANGTHAI = 'ĐÃ HUỶ'
    WHERE MACT_THUE_SAN = P_MACT_THUE_SAN;

    -- ====== BƯỚC 7: Huỷ hoá đơn nếu hết chi tiết ======
    IF V_REMAINING = 0 THEN
        UPDATE HOA_DON
        SET TRANGTHAI = 'ĐÃ HUỶ'
        WHERE MAHD = V_MAHD
          AND TRANGTHAI = 'CHƯA THANH TOÁN';

        IF V_TIEN_COC_CU > 0 THEN
            IF V_NGAY_CON_LAI < 2 THEN
                DBMS_OUTPUT.PUT_LINE(
                    'HOA DON ' || V_MAHD || ' DA HUY. ' ||
                    'COC ' || TO_CHAR(V_TIEN_COC_CU, 'FM999G999G990') ||
                    ' KHONG DUOC HOAN (huy trong vong 2 ngay truoc ngay thue).'
                );
            ELSE
                DBMS_OUTPUT.PUT_LINE(
                    'HOA DON ' || V_MAHD || ' DA HUY. ' ||
                    'HOAN 100% COC: ' || TO_CHAR(V_TIEN_COC_CU, 'FM999G999G990') || '.'
                );
            END IF;
        ELSE
            DBMS_OUTPUT.PUT_LINE('HOA DON ' || V_MAHD || ' DA HUY (khong co coc).');
        END IF;
    ELSE
        IF V_TIEN_COC_CU > 0 THEN
            DBMS_OUTPUT.PUT_LINE(
                'DA HUY CHI TIET ' || P_MACT_THUE_SAN || '. ' ||
                'COC CU: ' || TO_CHAR(V_TIEN_COC_CU, 'FM999G999G990') ||
                ' -> COC MOI: ' || TO_CHAR(V_TIEN_COC_MOI, 'FM999G999G990') ||
                ' (70% tien thue san con lai).'
            );
        ELSE
            DBMS_OUTPUT.PUT_LINE('DA HUY CHI TIET ' || P_MACT_THUE_SAN || '.');
        END IF;
    END IF;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        PKG_COURT_CTX.G_INTERNAL_RECALC := FALSE;
        RAISE_APPLICATION_ERROR(-20162, 'Chi tiet thue san khong ton tai hoac da bi xoa.');
    WHEN OTHERS THEN
        PKG_COURT_CTX.G_INTERNAL_RECALC := FALSE;
        RAISE;
END;
/


-- ============================================================
-- PRC_CAP_NHAT_DOANH_THU_NGAY_CHI_NHANH
-- Tinh lai va cap nhat/tao moi doanh thu ngay cua chi nhanh.
-- ============================================================
CREATE OR REPLACE PROCEDURE PRC_CAP_NHAT_DOANH_THU_NGAY_CHI_NHANH(
    P_MACN IN CHI_NHANH.MACN%TYPE,
    P_NGAY IN DATE
)
AS
    V_TOTAL NUMBER(12, 2);
    V_COUNT NUMBER;
    V_MADT  DOANH_THU.MADT%TYPE;
BEGIN
    SELECT COUNT(1)
    INTO V_COUNT
    FROM CHI_NHANH
    WHERE MACN = P_MACN
      AND IS_DELETED = 0;

    IF V_COUNT = 0 THEN
        RAISE_APPLICATION_ERROR(-20170, 'Chi nhanh khong ton tai hoac da bi xoa.');
    END IF;

    V_TOTAL := FN_TINH_DOANH_THU_NGAY_CHI_NHANH(P_MACN, P_NGAY);

    UPDATE DOANH_THU
    SET TONGDOANHTHU = V_TOTAL,
        NOIDUNG      = 'Doanh thu ngay cua chi nhanh ' || P_MACN
    WHERE MACN = P_MACN
      AND TRUNC(NGAY) = TRUNC(P_NGAY)
      AND IS_DELETED = 0;

    IF SQL%ROWCOUNT = 0 THEN
        V_MADT := 'DT-' || REPLACE(P_MACN, '-', '') || '-' || TO_CHAR(P_NGAY, 'YYYYMMDD');

        INSERT INTO DOANH_THU(MADT, MACN, NOIDUNG, NGAY, TONGDOANHTHU, CREATED_AT, IS_DELETED)
        VALUES (V_MADT, P_MACN,
                'Doanh thu ngay ' || TO_CHAR(P_NGAY, 'YYYY-MM-DD') || ' cua chi nhanh ' || P_MACN,
                TRUNC(P_NGAY), V_TOTAL, SYSDATE, 0);
    END IF;
END;
/


-- ============================================================
-- PRC_DAT_SAN (nghiệp vụ phức tạp nhất)
-- Orchestrate: tạo/kiểm tra hóa đơn → thêm chi tiết thuê sân
-- → tính tiền cọc → tính lại tổng tiền.
-- Hỗ trợ cả đặt trước (cọc 70%) và chơi ngay (cọc = 0).
-- ============================================================
CREATE OR REPLACE PROCEDURE PRC_DAT_SAN(
    P_MAHD          IN HOA_DON.MAHD%TYPE,
    P_MACT_THUE_SAN IN CHI_TIET_HOA_DON_THUE_SAN.MACT_THUE_SAN%TYPE,
    P_MAKH          IN KHACH_HANG.MAKH%TYPE,
    P_MANV          IN NHAN_VIEN.MANV%TYPE,
    P_MASAN         IN SAN_CON.MASAN%TYPE,
    P_MABG          IN BANG_GIA.MABG%TYPE,
    P_NGAYTHUE      IN DATE,
    P_LA_DAT_TRUOC  IN NUMBER DEFAULT 1,
    P_GIAMGIA       IN HOA_DON.GIAMGIA%TYPE DEFAULT 0
)
AS
    V_COUNT_HD      NUMBER := 0;
    V_COUNT_CT      NUMBER := 0;
    V_MAKH_HD       HOA_DON.MAKH%TYPE;
    V_MANV_HD       HOA_DON.MANV%TYPE;
    V_TRANGTHAI_HD  HOA_DON.TRANGTHAI%TYPE;
    V_TIEN_COC_CU   HOA_DON.TIEN_COC%TYPE;
    V_TONG_TIEN_SAN NUMBER := 0;
    V_TIEN_COC_MOI  NUMBER := 0;
    V_INITIAL_STATUS CHI_TIET_HOA_DON_THUE_SAN.TRANGTHAI%TYPE;
BEGIN
    IF P_MAHD IS NULL THEN
        RAISE_APPLICATION_ERROR(-20200, 'MAHD khong duoc null.');
    END IF;

    IF P_LA_DAT_TRUOC NOT IN (0, 1) THEN
        RAISE_APPLICATION_ERROR(
                -20201,
                'P_LA_DAT_TRUOC chi duoc nhan 0 hoac 1. 1 = dat truoc, 0 = choi ngay.'
            );
    END IF;

    SAVEPOINT SP_DAT_SAN;

    SELECT COUNT(1)
    INTO V_COUNT_HD
    FROM HOA_DON
    WHERE MAHD = P_MAHD
      AND IS_DELETED = 0;

    IF V_COUNT_HD = 0 THEN
        -- Chua co hoa don thi tao moi
        PRC_TAO_HOA_DON(
                P_MAHD     => P_MAHD,
                P_MAKH     => P_MAKH,
                P_MANV     => P_MANV,
                P_GIAMGIA  => P_GIAMGIA,
                P_TIEN_COC => 0
            );
    ELSE
        -- Da co hoa don thi khoa dong hoa don de kiem tra va tranh update dong thoi
        SELECT MAKH, MANV, TRANGTHAI, TIEN_COC
        INTO V_MAKH_HD, V_MANV_HD, V_TRANGTHAI_HD, V_TIEN_COC_CU
        FROM HOA_DON
        WHERE MAHD = P_MAHD
          AND IS_DELETED = 0
            FOR UPDATE;

        IF V_TRANGTHAI_HD <> 'CHƯA THANH TOÁN' THEN
            RAISE_APPLICATION_ERROR(
                    -20202,
                    'Chi duoc them chi tiet thue san vao hoa don CHUA THANH TOAN.'
                );
        END IF;

        IF V_MAKH_HD <> P_MAKH THEN
            RAISE_APPLICATION_ERROR(
                    -20203,
                    'Hoa don da ton tai nhung khong thuoc khach hang nay.'
                );
        END IF;

        IF V_MANV_HD <> P_MANV THEN
            RAISE_APPLICATION_ERROR(
                    -20204,
                    'Hoa don da ton tai nhung khong thuoc nhan vien nay.'
                );
        END IF;

        SELECT COUNT(1)
        INTO V_COUNT_CT
        FROM CHI_TIET_HOA_DON_THUE_SAN
        WHERE MAHD = P_MAHD
          AND IS_DELETED = 0
          AND TRANGTHAI <> 'ĐÃ HUỶ';

        -- Neu hoa don da co chi tiet, khong nen doi loai don giua dat truoc va choi ngay
        IF V_COUNT_CT > 0 THEN
            IF V_TIEN_COC_CU > 0 AND P_LA_DAT_TRUOC = 0 THEN
                RAISE_APPLICATION_ERROR(
                        -20205,
                        'Hoa don nay dang la hoa don dat truoc, khong the them san theo kieu choi ngay.'
                    );
            END IF;

            IF V_TIEN_COC_CU = 0 AND P_LA_DAT_TRUOC = 1 THEN
                RAISE_APPLICATION_ERROR(
                        -20206,
                        'Hoa don nay dang la hoa don choi ngay, khong the them san theo kieu dat truoc.'
                    );
            END IF;
        END IF;
    END IF;

    PKG_COURT_CTX.G_INTERNAL_RECALC := TRUE;

    IF P_LA_DAT_TRUOC = 1 THEN
        V_INITIAL_STATUS := 'ĐÃ ĐẶT CHỜ CỌC';
    ELSE
        V_INITIAL_STATUS := 'ĐANG SỬ DỤNG';
    END IF;

    PRC_THEM_CHI_TIET_THUE_SAN(
            P_MACT_THUE_SAN => P_MACT_THUE_SAN,
            P_MAHD          => P_MAHD,
            P_MASAN         => P_MASAN,
            P_MABG          => P_MABG,
            P_NGAYTHUE      => P_NGAYTHUE,
            P_TRANGTHAI     => V_INITIAL_STATUS
        );

    SELECT NVL(SUM(DON_GIA_THUE), 0)
    INTO V_TONG_TIEN_SAN
    FROM CHI_TIET_HOA_DON_THUE_SAN
    WHERE MAHD = P_MAHD
      AND IS_DELETED = 0
      AND TRANGTHAI <> 'ĐÃ HUỶ';

    IF P_LA_DAT_TRUOC = 1 THEN
        V_TIEN_COC_MOI := ROUND(V_TONG_TIEN_SAN * 0.7, 2);
    ELSE
        V_TIEN_COC_MOI := 0;
    END IF;

    UPDATE HOA_DON
    SET TIEN_COC = V_TIEN_COC_MOI
    WHERE MAHD = P_MAHD
      AND IS_DELETED = 0;

    PKG_COURT_CTX.G_INTERNAL_RECALC := FALSE;

    PRC_CAP_NHAT_SO_TIEN_HOA_DON(P_MAHD);

EXCEPTION
    WHEN OTHERS THEN
        PKG_COURT_CTX.G_INTERNAL_RECALC := FALSE;
        ROLLBACK TO SP_DAT_SAN;
        RAISE;
END;
/


-- ============================================================
-- PRC_IN_BANG_GIA_SAN_CON
-- In thong tin bang gia cua san con ra DBMS_OUTPUT.
-- ============================================================
CREATE OR REPLACE PROCEDURE PRC_IN_BANG_GIA_SAN_CON(
    P_MASAN IN SAN_CON.MASAN%TYPE,
    P_MABG  IN BANG_GIA.MABG%TYPE DEFAULT NULL
)
AS
    V_DA_IN_HEADER BOOLEAN := FALSE;
    V_SO_DONG      NUMBER  := 0;
BEGIN
    DBMS_OUTPUT.PUT_LINE(RPAD('=', 80, '='));
    DBMS_OUTPUT.PUT_LINE('THONG TIN BANG GIA CUA SAN CON');
    DBMS_OUTPUT.PUT_LINE('MASAN CAN TRA CUU: ' || P_MASAN);

    IF P_MABG IS NOT NULL THEN
        DBMS_OUTPUT.PUT_LINE('MABG CAN TRA CUU : ' || P_MABG);
    END IF;

    DBMS_OUTPUT.PUT_LINE(RPAD('-', 80, '-'));

    FOR R IN (
        SELECT cn.MACN,
               cn.TEN_CHI_NHANH,
               cn.DIACHI,
               kv.MAKV,
               kv.SO_LUONG_SAN,
               ltt.MATT,
               ltt.TEN      AS TEN_THE_THAO,
               sc.MASAN,
               sc.TRANGTHAI AS TRANGTHAI_SAN,
               bg.MABG,
               bg.GIOBATDAU,
               bg.GIOKETTHUC,
               bg.GIA,
               bg.CREATED_AT
        FROM SAN_CON sc
                 JOIN KHU_VUC kv
                      ON kv.MAKV = sc.MAKV
                 JOIN CHI_NHANH cn
                      ON cn.MACN = kv.MACN
                 JOIN LOAI_THE_THAO ltt
                      ON ltt.MATT = kv.MATT
                 JOIN BANG_GIA bg
                      ON bg.MAKV = kv.MAKV
        WHERE sc.MASAN = P_MASAN
          AND sc.IS_DELETED = 0
          AND kv.IS_DELETED = 0
          AND cn.IS_DELETED = 0
          AND ltt.IS_DELETED = 0
          AND bg.IS_DELETED = 0
          AND (P_MABG IS NULL OR bg.MABG = P_MABG)
        ORDER BY bg.GIOBATDAU, bg.GIOKETTHUC
        )
        LOOP
            V_SO_DONG := V_SO_DONG + 1;

            IF NOT V_DA_IN_HEADER THEN
                DBMS_OUTPUT.PUT_LINE('CHI NHANH   : ' || R.MACN || ' - ' || R.TEN_CHI_NHANH);
                DBMS_OUTPUT.PUT_LINE('DIA CHI     : ' || R.DIACHI);
                DBMS_OUTPUT.PUT_LINE('KHU VUC     : ' || R.MAKV);
                DBMS_OUTPUT.PUT_LINE('LOAI THE THAO: ' || R.MATT || ' - ' || R.TEN_THE_THAO);
                DBMS_OUTPUT.PUT_LINE('SAN CON     : ' || R.MASAN);
                DBMS_OUTPUT.PUT_LINE('TRANG THAI  : ' || R.TRANGTHAI_SAN);
                DBMS_OUTPUT.PUT_LINE('SO LUONG SAN TRONG KHU VUC: ' || R.SO_LUONG_SAN);
                DBMS_OUTPUT.PUT_LINE(RPAD('-', 80, '-'));
                DBMS_OUTPUT.PUT_LINE('DANH SACH BANG GIA CUA KHU VUC');
                V_DA_IN_HEADER := TRUE;
            END IF;

            DBMS_OUTPUT.PUT_LINE(
                    'MABG=' || R.MABG ||
                    ' | KHUNG GIO=' || LPAD(R.GIOBATDAU, 2, '0') || ':00-' ||
                    LPAD(R.GIOKETTHUC, 2, '0') || ':00' ||
                    ' | GIA=' || TO_CHAR(R.GIA, 'FM999G999G999G990D00')
                );
        END LOOP;

    IF V_SO_DONG = 0 THEN
        RAISE_APPLICATION_ERROR(
                -20101,
                'Khong tim thay bang gia hop le cho san con ' || P_MASAN
            );
    END IF;

    DBMS_OUTPUT.PUT_LINE(RPAD('=', 80, '='));
END;
/


-- ============================================================
-- PRC_IN_DOI_CHIEU_GIA_THUE_SAN
-- In doi chieu gia snapshot vs gia hien tai.
-- ============================================================
CREATE OR REPLACE PROCEDURE PRC_IN_DOI_CHIEU_GIA_THUE_SAN(
    P_MACT_THUE_SAN IN CHI_TIET_HOA_DON_THUE_SAN.MACT_THUE_SAN%TYPE
)
AS
BEGIN
    DBMS_OUTPUT.PUT_LINE(RPAD('=', 80, '='));
    DBMS_OUTPUT.PUT_LINE('DOI CHIEU GIA CHI TIET THUE SAN VOI BANG GIA HIEN TAI');
    DBMS_OUTPUT.PUT_LINE('MACT_THUE_SAN: ' || P_MACT_THUE_SAN);
    DBMS_OUTPUT.PUT_LINE(RPAD('-', 80, '-'));

    FOR R IN (
        SELECT ct.MACT_THUE_SAN,
               ct.MAHD,
               ct.MASAN,
               ct.MABG,
               ct.NGAYTHUE,
               ct.DON_GIA_THUE,
               ct.TRANGTHAI,
               bg.GIA AS GIA_BANG_GIA_HIEN_TAI,
               kv.MAKV,
               cn.MACN,
               cn.TEN_CHI_NHANH
        FROM CHI_TIET_HOA_DON_THUE_SAN ct
                 JOIN BANG_GIA bg
                      ON bg.MABG = ct.MABG
                 JOIN SAN_CON sc
                      ON sc.MASAN = ct.MASAN
                 JOIN KHU_VUC kv
                      ON kv.MAKV = sc.MAKV
                 JOIN CHI_NHANH cn
                      ON cn.MACN = kv.MACN
        WHERE ct.MACT_THUE_SAN = P_MACT_THUE_SAN
        )
        LOOP
            DBMS_OUTPUT.PUT_LINE('CHI NHANH             : ' || R.MACN || ' - ' || R.TEN_CHI_NHANH);
            DBMS_OUTPUT.PUT_LINE('KHU VUC               : ' || R.MAKV);
            DBMS_OUTPUT.PUT_LINE('MA HOA DON            : ' || R.MAHD);
            DBMS_OUTPUT.PUT_LINE('SAN CON               : ' || R.MASAN);
            DBMS_OUTPUT.PUT_LINE('BANG GIA              : ' || R.MABG);
            DBMS_OUTPUT.PUT_LINE('NGAY THUE             : ' || TO_CHAR(R.NGAYTHUE, 'YYYY-MM-DD HH24:MI'));
            DBMS_OUTPUT.PUT_LINE('TRANG THAI            : ' || R.TRANGTHAI);
            DBMS_OUTPUT.PUT_LINE('DON_GIA_THUE SNAPSHOT : ' || TO_CHAR(R.DON_GIA_THUE, 'FM999G999G999G990D00'));
            DBMS_OUTPUT.PUT_LINE('GIA BANG_GIA HIEN TAI : ' ||
                                 TO_CHAR(R.GIA_BANG_GIA_HIEN_TAI, 'FM999G999G999G990D00'));
        END LOOP;

    DBMS_OUTPUT.PUT_LINE(RPAD('=', 80, '='));
END;
/


-- ============================================================
-- PRC_THEM_KHACH_HANG
-- Them khach hang moi: tao USERS, KHACH_HANG, ACCOUNT, ACCOUNT_ROLE_GROUP.
-- ============================================================
CREATE OR REPLACE PROCEDURE PRC_THEM_KHACH_HANG(
    P_USER_ID               IN USERS.USER_ID%TYPE,
    P_MAKH                  IN KHACH_HANG.MAKH%TYPE,
    P_ACCOUNT_ID            IN ACCOUNT.ACCOUNT_ID%TYPE,
    P_ACCOUNT_ROLE_GROUP_ID IN ACCOUNT_ROLE_GROUP.ACCOUNT_ROLE_GROUP_ID%TYPE,
    P_HOTEN                 IN USERS.HOTEN%TYPE,
    P_SDT                   IN USERS.SDT%TYPE,
    P_PASSWORD_HASH         IN ACCOUNT.PASSWORD_HASH%TYPE
)
AS
    V_GROUP_ID CONSTANT ROLE_GROUP.GROUP_ID%TYPE := 'RG-4';
BEGIN
    IF P_HOTEN IS NULL THEN
        RAISE_APPLICATION_ERROR(-20300, 'Vui long nhap ho ten.');
    END IF;

    IF P_SDT IS NULL THEN
        RAISE_APPLICATION_ERROR(-20301, 'Vui long nhap so dien thoai.');
    END IF;

    INSERT INTO USERS (USER_ID, HOTEN, SDT, EMAIL, NGAYSINH, DIACHI, CREATED_AT, IS_DELETED)
    VALUES (P_USER_ID, P_HOTEN, P_SDT, NULL, NULL, NULL, SYSDATE, 0);

    INSERT INTO KHACH_HANG (MAKH, USER_ID, MA_HANG, TRANGTHAI, DOANH_THU, CREATED_AT, IS_DELETED)
    VALUES (P_MAKH, P_USER_ID, NULL, 'ACTIVE', 0, SYSDATE, 0);

    INSERT INTO ACCOUNT (ACCOUNT_ID, USER_ID, USERNAME, PASSWORD_HASH, STATUS, CREATED_AT, IS_DELETED)
    VALUES (P_ACCOUNT_ID, P_USER_ID, P_SDT, P_PASSWORD_HASH, 'ACTIVE', SYSDATE, 0);

    INSERT INTO ACCOUNT_ROLE_GROUP (ACCOUNT_ROLE_GROUP_ID, ACCOUNT_ID, GROUP_ID, CREATED_AT, IS_DELETED)
    VALUES (P_ACCOUNT_ROLE_GROUP_ID, P_ACCOUNT_ID, V_GROUP_ID, SYSDATE, 0);
END;
/


-- ============================================================
-- PRC_XOA_KHACH_HANG
-- Xoa mem khach hang: KHACH_HANG, USERS, ACCOUNT, ACCOUNT_ROLE_GROUP.
-- ============================================================
CREATE OR REPLACE PROCEDURE PRC_XOA_KHACH_HANG(
    P_MAKH IN KHACH_HANG.MAKH%TYPE
)
AS
    V_USER_ID    KHACH_HANG.USER_ID%TYPE;
    V_ACCOUNT_ID ACCOUNT.ACCOUNT_ID%TYPE;
    V_COUNT      NUMBER := 0;
BEGIN
    SELECT COUNT(1)
    INTO V_COUNT
    FROM KHACH_HANG
    WHERE MAKH = P_MAKH
      AND IS_DELETED = 0;

    IF V_COUNT = 0 THEN
        RAISE_APPLICATION_ERROR(-20321, 'Khach hang khong ton tai hoac da bi xoa.');
    END IF;

    SELECT kh.USER_ID, a.ACCOUNT_ID
    INTO V_USER_ID, V_ACCOUNT_ID
    FROM KHACH_HANG kh
             JOIN ACCOUNT a
                  ON a.USER_ID = kh.USER_ID
                      AND a.IS_DELETED = 0
             JOIN USERS u
                  ON u.USER_ID = kh.USER_ID
                      AND u.IS_DELETED = 0
    WHERE kh.MAKH = P_MAKH
      AND kh.IS_DELETED = 0;

    UPDATE KHACH_HANG
    SET TRANGTHAI  = 'INACTIVE',
        IS_DELETED = 1
    WHERE MAKH = P_MAKH
      AND IS_DELETED = 0;

    UPDATE USERS
    SET IS_DELETED = 1
    WHERE USER_ID = V_USER_ID
      AND IS_DELETED = 0;

    UPDATE ACCOUNT
    SET STATUS     = 'INACTIVE',
        IS_DELETED = 1
    WHERE ACCOUNT_ID = V_ACCOUNT_ID
      AND IS_DELETED = 0;

    UPDATE ACCOUNT_ROLE_GROUP
    SET IS_DELETED = 1
    WHERE ACCOUNT_ID = V_ACCOUNT_ID
      AND IS_DELETED = 0;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20321, 'Khach hang khong ton tai hoac da bi xoa.');
END;
/

- ============================================================
-- PRC_THEM_NHAN_VIEN
-- Tạo mới nhân viên: USERS → NHAN_VIEN → ACCOUNT → ACCOUNT_ROLE_GROUP.
-- Tự động gán ROLE_GROUP dựa trên IS_QL:
--   IS_QL = 1 → 'RG-2' (Quản lý chi nhánh)
--   IS_QL = 0 → 'RG-3' (Nhân viên thu ngân)
-- ============================================================
CREATE OR REPLACE PROCEDURE PRC_THEM_NHAN_VIEN(
    P_USER_ID               IN USERS.USER_ID%TYPE,
    P_MANV                  IN NHAN_VIEN.MANV%TYPE,
    P_ACCOUNT_ID            IN ACCOUNT.ACCOUNT_ID%TYPE,
    P_ACCOUNT_ROLE_GROUP_ID IN ACCOUNT_ROLE_GROUP.ACCOUNT_ROLE_GROUP_ID%TYPE,
    P_HOTEN                 IN USERS.HOTEN%TYPE,
    P_SDT                   IN USERS.SDT%TYPE,
    P_EMAIL                 IN USERS.EMAIL%TYPE DEFAULT NULL,
    P_PASSWORD_HASH         IN ACCOUNT.PASSWORD_HASH%TYPE,
    P_MACN                  IN CHI_NHANH.MACN%TYPE,
    P_MALNV                 IN LOAI_NHAN_VIEN.MALNV%TYPE,
    P_CCCD                  IN NHAN_VIEN.CCCD%TYPE DEFAULT NULL,
    P_IS_QL                 IN NHAN_VIEN.IS_QL%TYPE DEFAULT 0
)
AS
    V_GROUP_ID ROLE_GROUP.GROUP_ID%TYPE;
    V_COUNT    NUMBER;
BEGIN
    -- Validate đầu vào bắt buộc
    IF P_HOTEN IS NULL THEN
        RAISE_APPLICATION_ERROR(-20400, 'Vui long nhap ho ten.');
END IF;

    IF P_SDT IS NULL THEN
        RAISE_APPLICATION_ERROR(-20401, 'Vui long nhap so dien thoai.');
END IF;

    IF P_MACN IS NULL THEN
        RAISE_APPLICATION_ERROR(-20402, 'Vui long nhap ma chi nhanh.');
END IF;

    IF P_MALNV IS NULL THEN
        RAISE_APPLICATION_ERROR(-20403, 'Vui long nhap ma loai nhan vien.');
END IF;

    IF P_IS_QL NOT IN (0, 1) THEN
        RAISE_APPLICATION_ERROR(-20404, 'IS_QL chi duoc nhan 0 hoac 1.');
END IF;

    -- Kiểm tra SDT chưa tồn tại
SELECT COUNT(1) INTO V_COUNT
FROM USERS WHERE SDT = P_SDT AND IS_DELETED = 0;

IF V_COUNT > 0 THEN
        RAISE_APPLICATION_ERROR(-20405, 'So dien thoai da ton tai trong he thong.');
END IF;

    -- Kiểm tra CCCD chưa tồn tại (nếu có)
    IF P_CCCD IS NOT NULL THEN
SELECT COUNT(1) INTO V_COUNT
FROM NHAN_VIEN WHERE CCCD = P_CCCD AND IS_DELETED = 0;

IF V_COUNT > 0 THEN
            RAISE_APPLICATION_ERROR(-20406, 'CCCD da ton tai trong he thong.');
END IF;
END IF;

    -- Kiểm tra chi nhánh tồn tại
SELECT COUNT(1) INTO V_COUNT
FROM CHI_NHANH WHERE MACN = P_MACN AND IS_DELETED = 0;

IF V_COUNT = 0 THEN
        RAISE_APPLICATION_ERROR(-20407, 'Chi nhanh khong ton tai hoac da bi xoa.');
END IF;

    -- Kiểm tra loại nhân viên tồn tại
SELECT COUNT(1) INTO V_COUNT
FROM LOAI_NHAN_VIEN WHERE MALNV = P_MALNV AND IS_DELETED = 0;

IF V_COUNT = 0 THEN
        RAISE_APPLICATION_ERROR(-20408, 'Loai nhan vien khong ton tai hoac da bi xoa.');
END IF;

    -- Xác định ROLE_GROUP dựa trên IS_QL
    IF P_IS_QL = 1 THEN
        V_GROUP_ID := 'RG-2'; -- Quản lý chi nhánh
ELSE
        V_GROUP_ID := 'RG-3'; -- Nhân viên thu ngân
END IF;

    -- Kiểm tra ROLE_GROUP tồn tại
SELECT COUNT(1) INTO V_COUNT
FROM ROLE_GROUP WHERE GROUP_ID = V_GROUP_ID AND IS_DELETED = 0;

IF V_COUNT = 0 THEN
        RAISE_APPLICATION_ERROR(-20409, 'Role group ' || V_GROUP_ID || ' khong ton tai.');
END IF;

    -- 1. Tạo USERS
INSERT INTO USERS (USER_ID, HOTEN, SDT, EMAIL, NGAYSINH, DIACHI, CREATED_AT, IS_DELETED)
VALUES (P_USER_ID, P_HOTEN, P_SDT, P_EMAIL, NULL, NULL, SYSDATE, 0);

-- 2. Tạo NHAN_VIEN
INSERT INTO NHAN_VIEN (MANV, USER_ID, MALNV, MACN, NVL, CCCD, IS_QL, TRANG_THAI, CREATED_AT, IS_DELETED)
VALUES (P_MANV, P_USER_ID, P_MALNV, P_MACN, SYSDATE, P_CCCD, P_IS_QL, 'HOẠT ĐỘNG', SYSDATE, 0);

-- 3. Tạo ACCOUNT (USERNAME = SDT)
INSERT INTO ACCOUNT (ACCOUNT_ID, USER_ID, USERNAME, PASSWORD_HASH, STATUS, CREATED_AT, IS_DELETED)
VALUES (P_ACCOUNT_ID, P_USER_ID, P_SDT, P_PASSWORD_HASH, 'ACTIVE', SYSDATE, 0);

-- 4. Gán ROLE_GROUP
INSERT INTO ACCOUNT_ROLE_GROUP (ACCOUNT_ROLE_GROUP_ID, ACCOUNT_ID, GROUP_ID, CREATED_AT, IS_DELETED)
VALUES (P_ACCOUNT_ROLE_GROUP_ID, P_ACCOUNT_ID, V_GROUP_ID, SYSDATE, 0);
END;
/


-- ============================================================
-- PRC_XOA_NHAN_VIEN
-- Xóa mềm nhân viên: NHAN_VIEN → ACCOUNT → ACCOUNT_ROLE_GROUP → USERS.
-- Kiểm tra không còn hóa đơn CHƯA THANH TOÁN do nhân viên phụ trách.
-- Chủ sân / QLCN cần thanh toán hoặc chuyển hóa đơn trước khi xóa.
-- ============================================================
CREATE OR REPLACE PROCEDURE PRC_XOA_NHAN_VIEN(
    P_MANV IN NHAN_VIEN.MANV%TYPE
)
AS
    V_USER_ID       NHAN_VIEN.USER_ID%TYPE;
    V_ACCOUNT_ID    ACCOUNT.ACCOUNT_ID%TYPE;
    V_COUNT_PENDING NUMBER := 0;
BEGIN
    -- Kiểm tra nhân viên tồn tại
SELECT USER_ID
INTO V_USER_ID
FROM NHAN_VIEN
WHERE MANV = P_MANV
  AND IS_DELETED = 0;

-- Kiểm tra không còn hóa đơn chưa thanh toán
SELECT COUNT(1)
INTO V_COUNT_PENDING
FROM HOA_DON
WHERE MANV = P_MANV
  AND TRANGTHAI = 'CHƯA THANH TOÁN'
  AND IS_DELETED = 0;

IF V_COUNT_PENDING > 0 THEN
        RAISE_APPLICATION_ERROR(-20422,
            'Khong the xoa nhan vien vi con ' || V_COUNT_PENDING ||
            ' hoa don CHUA THANH TOAN. Vui long thanh toan hoac chuyen nhan vien phu trach truoc.');
END IF;

    -- Lấy ACCOUNT_ID
SELECT ACCOUNT_ID
INTO V_ACCOUNT_ID
FROM ACCOUNT
WHERE USER_ID = V_USER_ID
  AND IS_DELETED = 0;

-- Xóa mềm theo thứ tự
UPDATE ACCOUNT_ROLE_GROUP
SET IS_DELETED = 1
WHERE ACCOUNT_ID = V_ACCOUNT_ID
  AND IS_DELETED = 0;

UPDATE ACCOUNT
SET STATUS     = 'INACTIVE',
    IS_DELETED = 1
WHERE ACCOUNT_ID = V_ACCOUNT_ID
  AND IS_DELETED = 0;

UPDATE NHAN_VIEN
SET TRANG_THAI = 'ĐÃ XOÁ',
    IS_DELETED = 1
WHERE MANV = P_MANV
  AND IS_DELETED = 0;

UPDATE USERS
SET IS_DELETED = 1
WHERE USER_ID = V_USER_ID
  AND IS_DELETED = 0;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20421, 'Nhan vien hoac tai khoan khong ton tai hoac da bi xoa.');
END;
/
