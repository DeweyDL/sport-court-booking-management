

-- RB68, RB65, RB64
-- Validate court rental detail, copy snapshot price from BANG_GIA.GIA,
-- and guard arrival transition to DANG SU DUNG.

CREATE OR REPLACE TRIGGER TRG_BIUD_CTHD_THUE_SAN_VALIDATE
    BEFORE INSERT OR UPDATE OF MASAN, MABG, DON_GIA_THUE, TRANGTHAI, IS_DELETED
    ON CHI_TIET_HOA_DON_THUE_SAN
    FOR EACH ROW
DECLARE
    V_MAKV_SAN      SAN_CON.MAKV%TYPE;
    V_TRANGTHAI_SAN SAN_CON.TRANGTHAI%TYPE;
    V_MAKV_BG       BANG_GIA.MAKV%TYPE;
    V_GIA           BANG_GIA.GIA%TYPE;
BEGIN
    IF :NEW.IS_DELETED = 0 THEN
        SELECT SC.MAKV, SC.TRANGTHAI, BG.MAKV, BG.GIA
        INTO V_MAKV_SAN, V_TRANGTHAI_SAN, V_MAKV_BG, V_GIA
        FROM SAN_CON SC
                 CROSS JOIN BANG_GIA BG
        WHERE SC.MASAN = :NEW.MASAN
          AND BG.MABG = :NEW.MABG
          AND SC.IS_DELETED = 0
          AND BG.IS_DELETED = 0;

        IF V_MAKV_SAN <> V_MAKV_BG THEN
            RAISE_APPLICATION_ERROR(-20068, 'Bang gia khong thuoc cung khu vuc voi san con.');
        END IF;

        IF :NEW.TRANGTHAI <> 'ĐÃ HUỶ' AND V_TRANGTHAI_SAN <> 'ĐANG HOẠT ĐỘNG' THEN
            RAISE_APPLICATION_ERROR(-20065, 'Chi duoc thue san con dang hoat dong.');
        END IF;
    END IF;

    IF INSERTING
        OR NVL(:OLD.MASAN, '#') <> NVL(:NEW.MASAN, '#')
        OR NVL(:OLD.MABG, '#') <> NVL(:NEW.MABG, '#')
        OR :NEW.DON_GIA_THUE IS NULL THEN
        IF :NEW.IS_DELETED = 0 THEN
            :NEW.DON_GIA_THUE := V_GIA;
        END IF;
    ELSIF NVL(:OLD.DON_GIA_THUE, -1) <> NVL(:NEW.DON_GIA_THUE, -1) THEN
        RAISE_APPLICATION_ERROR(-20068, 'Khong duoc sua DON_GIA_THUE thu cong. Hay doi MABG/MASAN neu can lay gia moi.');
    END IF;

    IF INSERTING AND :NEW.TRANGTHAI = 'ĐANG SỬ DỤNG' THEN
        RAISE_APPLICATION_ERROR(-20064, 'Khong duoc tao moi chi tiet thue san o trang thai DANG SU DUNG. Hay tao DA XAC NHAN roi xac nhan khach den san.');
    END IF;

    IF UPDATING
        AND :NEW.IS_DELETED = 0
        AND :NEW.TRANGTHAI = 'ĐANG SỬ DỤNG'
        AND NVL(:OLD.TRANGTHAI, '#') <> 'ĐANG SỬ DỤNG' THEN
        IF :OLD.TRANGTHAI <> 'ĐÃ XÁC NHẬN' THEN
            RAISE_APPLICATION_ERROR(-20064, 'Chi duoc xac nhan khach den san khi chi tiet dang o trang thai DA XAC NHAN.');
        END IF;
    END IF;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20068, 'San con hoac bang gia khong ton tai, da xoa mem, hoac khong hop le.');
END;
/

-- ============================================================
-- RB52, RB59
-- Recalculate invoice after rental detail changes that affect money.
-- ============================================================
CREATE OR REPLACE TRIGGER TRG_FIUD_CTHD_THUE_SAN_RECALC
    FOR INSERT OR UPDATE OR DELETE
    ON CHI_TIET_HOA_DON_THUE_SAN
    COMPOUND TRIGGER

    TYPE T_MAHD_LIST IS TABLE OF HOA_DON.MAHD%TYPE INDEX BY PLS_INTEGER;
    G_MAHD_LIST T_MAHD_LIST;
    G_COUNT PLS_INTEGER := 0;

    PROCEDURE ADD_MAHD(P_MAHD IN HOA_DON.MAHD%TYPE) IS
    BEGIN
        IF P_MAHD IS NOT NULL THEN
            G_COUNT := G_COUNT + 1;
            G_MAHD_LIST(G_COUNT) := P_MAHD;
        END IF;
    END;

BEFORE EACH ROW IS
BEGIN
    IF INSERTING THEN
        ADD_MAHD(:NEW.MAHD);
    ELSIF DELETING THEN
        ADD_MAHD(:OLD.MAHD);
    ELSIF UPDATING THEN
        IF NVL(:OLD.MAHD, '#') <> NVL(:NEW.MAHD, '#')
            OR NVL(:OLD.MASAN, '#') <> NVL(:NEW.MASAN, '#')
            OR NVL(:OLD.MABG, '#') <> NVL(:NEW.MABG, '#')
            OR NVL(TRUNC(:OLD.NGAYTHUE), DATE '1900-01-01') <> NVL(TRUNC(:NEW.NGAYTHUE), DATE '1900-01-01')
            OR NVL(:OLD.DON_GIA_THUE, -1) <> NVL(:NEW.DON_GIA_THUE, -1)
            OR NVL(:OLD.IS_DELETED, -1) <> NVL(:NEW.IS_DELETED, -1)
            OR (:OLD.TRANGTHAI = 'ĐÃ HUỶ' AND :NEW.TRANGTHAI <> 'ĐÃ HUỶ')
            OR (:OLD.TRANGTHAI <> 'ĐÃ HUỶ' AND :NEW.TRANGTHAI = 'ĐÃ HUỶ') THEN
            ADD_MAHD(:NEW.MAHD);
            IF NVL(:OLD.MAHD, '#') <> NVL(:NEW.MAHD, '#') THEN
                ADD_MAHD(:OLD.MAHD);
            END IF;
        END IF;
    END IF;
END BEFORE EACH ROW;

    AFTER STATEMENT IS
    BEGIN
        FOR I IN 1 .. G_COUNT LOOP
                PRC_CAP_NHAT_SO_TIEN_HOA_DON(G_MAHD_LIST(I));
            END LOOP;
    END AFTER STATEMENT;

    END;
/


-- RB52, RB53, RB61
-- Validate service item and copy snapshot price from SAN_PHAM/DUNG_CU_THE_THAO.
-- Service details have no cancelled business state; employees only create used/active services.

CREATE OR REPLACE TRIGGER TRG_BIUD_CTHD_DV_PRICE
    BEFORE INSERT OR UPDATE OF MASP, MADC, DON_GIA, IS_DELETED
    ON CHI_TIET_HOA_DON_DICH_VU_DA_DUNG
    FOR EACH ROW
DECLARE
    V_GIA CHI_TIET_HOA_DON_DICH_VU_DA_DUNG.DON_GIA%TYPE;
BEGIN
    IF :NEW.IS_DELETED = 0 THEN
        IF :NEW.MASP IS NOT NULL THEN
            SELECT GIA
            INTO V_GIA
            FROM SAN_PHAM
            WHERE MASP = :NEW.MASP
              AND IS_DELETED = 0;
        ELSIF :NEW.MADC IS NOT NULL THEN
            SELECT GIA
            INTO V_GIA
            FROM DUNG_CU_THE_THAO
            WHERE MADC = :NEW.MADC
              AND IS_DELETED = 0;
        END IF;
    END IF;

    IF INSERTING
        OR NVL(:OLD.MASP, '#') <> NVL(:NEW.MASP, '#')
        OR NVL(:OLD.MADC, '#') <> NVL(:NEW.MADC, '#')
        OR :NEW.DON_GIA IS NULL THEN
        IF :NEW.IS_DELETED = 0 THEN
            :NEW.DON_GIA := V_GIA;
        END IF;
    ELSIF NVL(:OLD.DON_GIA, -1) <> NVL(:NEW.DON_GIA, -1) THEN
        RAISE_APPLICATION_ERROR(-20052, 'Khong duoc sua DON_GIA dich vu thu cong. Hay doi MASP/MADC neu can lay gia moi.');
    END IF;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20052, 'San pham/dung cu khong ton tai hoac da bi xoa.');
END;
/


-- RB52, RB59
-- Recalculate invoice after service detail changes that affect money.

CREATE OR REPLACE TRIGGER TRG_FIUD_CTHD_DICH_VU_RECALC
    FOR INSERT OR UPDATE OR DELETE
    ON CHI_TIET_HOA_DON_DICH_VU_DA_DUNG
    COMPOUND TRIGGER

    TYPE T_MAHD_LIST IS TABLE OF HOA_DON.MAHD%TYPE INDEX BY PLS_INTEGER;
    G_MAHD_LIST T_MAHD_LIST;
    G_COUNT PLS_INTEGER := 0;

    PROCEDURE ADD_MAHD(P_MAHD IN HOA_DON.MAHD%TYPE) IS
    BEGIN
        IF P_MAHD IS NOT NULL THEN
            G_COUNT := G_COUNT + 1;
            G_MAHD_LIST(G_COUNT) := P_MAHD;
        END IF;
    END;

BEFORE EACH ROW IS
BEGIN
    IF INSERTING THEN
        ADD_MAHD(:NEW.MAHD);
    ELSIF DELETING THEN
        ADD_MAHD(:OLD.MAHD);
    ELSIF UPDATING THEN
        IF NVL(:OLD.MAHD, '#') <> NVL(:NEW.MAHD, '#')
            OR NVL(:OLD.MASP, '#') <> NVL(:NEW.MASP, '#')
            OR NVL(:OLD.MADC, '#') <> NVL(:NEW.MADC, '#')
            OR NVL(:OLD.SL, -1) <> NVL(:NEW.SL, -1)
            OR NVL(:OLD.DON_GIA, -1) <> NVL(:NEW.DON_GIA, -1)
            OR NVL(:OLD.IS_DELETED, -1) <> NVL(:NEW.IS_DELETED, -1) THEN
            ADD_MAHD(:NEW.MAHD);
            IF NVL(:OLD.MAHD, '#') <> NVL(:NEW.MAHD, '#') THEN
                ADD_MAHD(:OLD.MAHD);
            END IF;
        END IF;
    END IF;
END BEFORE EACH ROW;

    AFTER STATEMENT IS
    BEGIN
        FOR I IN 1 .. G_COUNT LOOP
                PRC_CAP_NHAT_SO_TIEN_HOA_DON(G_MAHD_LIST(I));
            END LOOP;
    END AFTER STATEMENT;

    END;
/


-- RB58, RB56 basic checks on HOA_DON.
-- Actual 70% deposit check is inside PRC_CAP_NHAT_SO_TIEN_HOA_DON,
-- so it runs after details are available.

CREATE OR REPLACE TRIGGER TRG_BIU_HOA_DON_VALIDATE
    BEFORE INSERT OR UPDATE OF GIAMGIA, TIEN_COC, TONGGIATRI, TONGTIEN, TRANGTHAI, IS_DELETED
    ON HOA_DON
    FOR EACH ROW
BEGIN
    :NEW.GIAMGIA    := NVL(:NEW.GIAMGIA, 0);
    :NEW.TIEN_COC   := NVL(:NEW.TIEN_COC, 0);
    :NEW.TONGGIATRI := NVL(:NEW.TONGGIATRI, 0);
    :NEW.TONGTIEN   := NVL(:NEW.TONGTIEN, 0);

    IF :NEW.GIAMGIA < 0 OR :NEW.GIAMGIA > 100 THEN
        RAISE_APPLICATION_ERROR(-20057, 'GIAMGIA phai nam trong khoang 0 den 100.');
    END IF;

    IF :NEW.TIEN_COC < 0 THEN
        RAISE_APPLICATION_ERROR(-20058, 'TIEN_COC khong duoc am.');
    END IF;

    IF :NEW.TONGGIATRI < 0 OR :NEW.TONGTIEN < 0 THEN
        RAISE_APPLICATION_ERROR(-20059, 'Gia tri hoa don khong duoc am.');
    END IF;
END;
/

-- Recalculate when MAKH/GIAMGIA/TIEN_COC changes.
CREATE OR REPLACE TRIGGER TRG_FU_HOA_DON_RECALC_AMOUNT
    FOR UPDATE OF MAKH, GIAMGIA, TIEN_COC, IS_DELETED
    ON HOA_DON
    COMPOUND TRIGGER

    TYPE T_MAHD_LIST IS TABLE OF HOA_DON.MAHD%TYPE INDEX BY PLS_INTEGER;
    G_MAHD_LIST T_MAHD_LIST;
    G_COUNT PLS_INTEGER := 0;

    PROCEDURE ADD_MAHD(P_MAHD IN HOA_DON.MAHD%TYPE) IS
    BEGIN
        IF P_MAHD IS NOT NULL THEN
            G_COUNT := G_COUNT + 1;
            G_MAHD_LIST(G_COUNT) := P_MAHD;
        END IF;
    END;

BEFORE EACH ROW IS
BEGIN
    IF NOT PKG_COURT_CTX.G_INTERNAL_RECALC THEN
        ADD_MAHD(:NEW.MAHD);
    END IF;
END BEFORE EACH ROW;

    AFTER STATEMENT IS
    BEGIN
        IF NOT PKG_COURT_CTX.G_INTERNAL_RECALC THEN
            FOR I IN 1 .. G_COUNT LOOP
                    PRC_CAP_NHAT_SO_TIEN_HOA_DON(G_MAHD_LIST(I));
                END LOOP;
        END IF;
    END AFTER STATEMENT;

    END;
/


-- RB62, RB63
-- Validate payment transition and complete related details.

CREATE OR REPLACE TRIGGER TRG_BU_HOA_DON_THANH_TOAN_CHECK
    BEFORE UPDATE OF TRANGTHAI
    ON HOA_DON
    FOR EACH ROW
DECLARE
    V_COUNT        PLS_INTEGER := 0;
    V_INVALID_SAN  PLS_INTEGER := 0;
BEGIN
    IF :OLD.TRANGTHAI IN ('ĐÃ THANH TOÁN', 'ĐÃ HUỶ')
        AND :NEW.TRANGTHAI <> :OLD.TRANGTHAI THEN
        RAISE_APPLICATION_ERROR(-20061, 'Khong duoc thay doi trang thai cua hoa don da thanh toan hoac da huy.');
    END IF;

    IF :NEW.TRANGTHAI = 'ĐÃ THANH TOÁN' AND :OLD.TRANGTHAI <> 'ĐÃ THANH TOÁN' THEN
        IF :OLD.TRANGTHAI <> 'CHƯA THANH TOÁN' THEN
            RAISE_APPLICATION_ERROR(-20061, 'Chi duoc xac nhan thanh toan hoa don dang o trang thai CHUA THANH TOAN.');
        END IF;

        SELECT COUNT(1)
        INTO V_COUNT
        FROM CHI_TIET_HOA_DON_THUE_SAN CT
        WHERE CT.MAHD = :OLD.MAHD
          AND CT.IS_DELETED = 0
          AND CT.TRANGTHAI IN ('ĐÃ XÁC NHẬN', 'ĐANG SỬ DỤNG', 'ĐÃ HOÀN THÀNH');

        IF V_COUNT = 0 THEN
            RAISE_APPLICATION_ERROR(-20062, 'Hoa don khong co chi tiet thue san hop le de thanh toan.');
        END IF;

        SELECT COUNT(1)
        INTO V_INVALID_SAN
        FROM CHI_TIET_HOA_DON_THUE_SAN CT
                 JOIN SAN_CON SC ON SC.MASAN = CT.MASAN
        WHERE CT.MAHD = :OLD.MAHD
          AND CT.IS_DELETED = 0
          AND CT.TRANGTHAI <> 'ĐÃ HUỶ'
          AND (SC.IS_DELETED <> 0 OR SC.TRANGTHAI <> 'ĐANG HOẠT ĐỘNG');

        IF V_INVALID_SAN > 0 THEN
            RAISE_APPLICATION_ERROR(-20063, 'Khong the thanh toan vi co san con da xoa hoac dang bao tri.');
        END IF;
    END IF;
END;
/

CREATE OR REPLACE TRIGGER TRG_AU_HOA_DON_COMPLETE_DETAILS
    AFTER UPDATE OF TRANGTHAI
    ON HOA_DON
    FOR EACH ROW
BEGIN
    IF :OLD.TRANGTHAI <> 'ĐÃ THANH TOÁN' AND :NEW.TRANGTHAI = 'ĐÃ THANH TOÁN' THEN
        UPDATE CHI_TIET_HOA_DON_THUE_SAN
        SET TRANGTHAI = 'ĐÃ HOÀN THÀNH'
        WHERE MAHD = :NEW.MAHD
          AND TRANGTHAI IN ('ĐÃ XÁC NHẬN', 'ĐANG SỬ DỤNG')
          AND IS_DELETED = 0;

        UPDATE CHI_TIET_HOA_DON_DICH_VU_DA_DUNG
        SET TRANGTHAI = 'ĐÃ HOÀN THÀNH'
        WHERE MAHD = :NEW.MAHD
          AND TRANGTHAI = 'ĐANG SỬ DỤNG'
          AND IS_DELETED = 0;
    END IF;
END;
/


-- RB61: import details update stock.

CREATE OR REPLACE TRIGGER TRG_CTNH_CAP_NHAT_TON_SAN_PHAM
    AFTER INSERT OR UPDATE OR DELETE
    ON CHI_TIET_NHAP_HANG
    FOR EACH ROW
BEGIN
    IF INSERTING THEN
        IF :NEW.IS_DELETED = 0 THEN
            PRC_DIEU_CHINH_TON_SAN_PHAM(:NEW.MASP, :NEW.SLTHUCNHAP);
        END IF;
    ELSIF DELETING THEN
        IF :OLD.IS_DELETED = 0 THEN
            PRC_DIEU_CHINH_TON_SAN_PHAM(:OLD.MASP, -:OLD.SLTHUCNHAP);
        END IF;
    ELSIF UPDATING THEN
        IF :OLD.IS_DELETED = 0 THEN
            PRC_DIEU_CHINH_TON_SAN_PHAM(:OLD.MASP, -:OLD.SLTHUCNHAP);
        END IF;
        IF :NEW.IS_DELETED = 0 THEN
            PRC_DIEU_CHINH_TON_SAN_PHAM(:NEW.MASP, :NEW.SLTHUCNHAP);
        END IF;
    END IF;
END;
/

CREATE OR REPLACE TRIGGER TRG_CTNDC_CAP_NHAT_TON_DUNG_CU
    AFTER INSERT OR UPDATE OR DELETE
    ON CHI_TIET_NHAP_DUNG_CU
    FOR EACH ROW
BEGIN
    IF INSERTING THEN
        IF :NEW.IS_DELETED = 0 THEN
            PRC_DIEU_CHINH_TON_DUNG_CU(:NEW.MADC, :NEW.SLTHUCNHAP);
        END IF;
    ELSIF DELETING THEN
        IF :OLD.IS_DELETED = 0 THEN
            PRC_DIEU_CHINH_TON_DUNG_CU(:OLD.MADC, -:OLD.SLTHUCNHAP);
        END IF;
    ELSIF UPDATING THEN
        IF :OLD.IS_DELETED = 0 THEN
            PRC_DIEU_CHINH_TON_DUNG_CU(:OLD.MADC, -:OLD.SLTHUCNHAP);
        END IF;
        IF :NEW.IS_DELETED = 0 THEN
            PRC_DIEU_CHINH_TON_DUNG_CU(:NEW.MADC, :NEW.SLTHUCNHAP);
        END IF;
    END IF;
END;
/


-- RB61: service details update product/tool stock by state effect.
-- Product sale consumes stock in DANG SU DUNG and remains consumed in DA HOAN THANH.
-- Tool rental consumes stock only while DANG SU DUNG and returns on DA HOAN THANH.

CREATE OR REPLACE TRIGGER TRG_CTHD_DV_CAP_NHAT_TON
    BEFORE INSERT OR UPDATE OR DELETE
    ON CHI_TIET_HOA_DON_DICH_VU_DA_DUNG
    FOR EACH ROW
BEGIN
    IF DELETING OR UPDATING THEN
        IF :OLD.MASP IS NOT NULL
            AND :OLD.IS_DELETED = 0
            AND :OLD.TRANGTHAI IN ('ĐANG SỬ DỤNG', 'ĐÃ HOÀN THÀNH') THEN
            PRC_DIEU_CHINH_TON_SAN_PHAM(:OLD.MASP, :OLD.SL);
        END IF;

        IF :OLD.MADC IS NOT NULL
            AND :OLD.IS_DELETED = 0
            AND :OLD.TRANGTHAI = 'ĐANG SỬ DỤNG' THEN
            PRC_DIEU_CHINH_TON_DUNG_CU(:OLD.MADC, :OLD.SL);
        END IF;
    END IF;

    IF INSERTING OR UPDATING THEN
        IF :NEW.MASP IS NOT NULL
            AND :NEW.IS_DELETED = 0
            AND :NEW.TRANGTHAI IN ('ĐANG SỬ DỤNG', 'ĐÃ HOÀN THÀNH') THEN
            PRC_DIEU_CHINH_TON_SAN_PHAM(:NEW.MASP, -:NEW.SL);
        END IF;

        IF :NEW.MADC IS NOT NULL
            AND :NEW.IS_DELETED = 0
            AND :NEW.TRANGTHAI = 'ĐANG SỬ DỤNG' THEN
            PRC_DIEU_CHINH_TON_DUNG_CU(:NEW.MADC, -:NEW.SL);
        END IF;
    END IF;
END;
/
