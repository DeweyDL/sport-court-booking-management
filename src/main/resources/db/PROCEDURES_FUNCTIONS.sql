CREATE OR REPLACE PACKAGE PKG_COURT_CTX AS
    G_INTERNAL_RECALC BOOLEAN := FALSE;
END PKG_COURT_CTX;
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

    IF V_TIEN_COC > 0 THEN
        IF V_TIEN_THUE_SAN <= 0 THEN
            RAISE_APPLICATION_ERROR(-20058, 'Hoa don dat truoc phai co chi tiet thue san hop le.');
        END IF;

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
    V_GIAM_HANG    := ROUND(V_TIEN_THUE_SAN * V_CHIET_KHAU / 100, 2);
    V_GIAM_HOA_DON := ROUND((V_TONG_GIA_TRI - V_GIAM_HANG) * V_GIAMGIA / 100, 2);
    V_TONG_TIEN    := ROUND(V_TONG_GIA_TRI - V_GIAM_HANG - V_GIAM_HOA_DON - V_TIEN_COC, 2);

    IF V_TONG_TIEN < 0 THEN
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


-- PRC_DIEU_CHINH_TON_SAN_PHAM
-- P_DELTA > 0: cong ton; P_DELTA < 0: tru ton.
-- Lock row ton kho de tranh race condition khi xuat vuot ton.

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


-- PRC_DIEU_CHINH_TON_DUNG_CU
-- P_DELTA > 0: tra/cong ton; P_DELTA < 0: thue/tru ton.
-- Lock row ton kho de tranh race condition khi thue vuot ton.

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
