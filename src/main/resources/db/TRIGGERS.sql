-- ============================================================
-- RB68, RB65, RB64
-- Validate chi tiết thuê sân, gán giá snapshot từ BANG_GIA.GIA
-- thông qua FN_LAY_GIA_THUE_SAN, và kiểm tra chuyển trạng thái.
--
-- [REFACTORED] Trích logic lấy giá + validate khu vực ra
-- FN_LAY_GIA_THUE_SAN để trigger gọn hơn và function tái sử dụng.
-- ============================================================
CREATE OR REPLACE TRIGGER TRG_BIUD_CTHD_THUE_SAN_VALIDATE
    BEFORE INSERT OR UPDATE OF MASAN, MABG, DON_GIA_THUE, TRANGTHAI, IS_DELETED
    ON CHI_TIET_HOA_DON_THUE_SAN
    FOR EACH ROW
DECLARE
    V_TRANGTHAI_SAN SAN_CON.TRANGTHAI%TYPE;
    V_GIA           BANG_GIA.GIA%TYPE;
BEGIN
    IF :NEW.IS_DELETED = 0 THEN

        V_GIA := FN_LAY_GIA_THUE_SAN(:NEW.MASAN, :NEW.MABG);

        SELECT SC.TRANGTHAI
        INTO V_TRANGTHAI_SAN
        FROM SAN_CON SC
        WHERE SC.MASAN = :NEW.MASAN
          AND SC.IS_DELETED = 0;

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
        RAISE_APPLICATION_ERROR(-20068,
                                'Khong duoc sua DON_GIA_THUE thu cong. Hay doi MABG/MASAN neu can lay gia moi.');
    END IF;

    IF INSERTING AND :NEW.TRANGTHAI = 'ĐANG SỬ DỤNG' THEN
        RAISE_APPLICATION_ERROR(-20064,
                                'Khong duoc tao moi chi tiet thue san o trang thai DANG SU DUNG. Hay tao DA XAC NHAN roi xac nhan khach den san.');
    END IF;

    IF UPDATING
        AND :NEW.IS_DELETED = 0
        AND :NEW.TRANGTHAI = 'ĐANG SỬ DỤNG'
        AND NVL(:OLD.TRANGTHAI, '#') <> 'ĐANG SỬ DỤNG' THEN
        IF :OLD.TRANGTHAI <> 'ĐÃ XÁC NHẬN' THEN
            RAISE_APPLICATION_ERROR(-20064,
                                    'Chi duoc xac nhan khach den san khi chi tiet dang o trang thai DA XAC NHAN.');
        END IF;
    END IF;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20068, 'San con hoac bang gia khong ton tai, da xoa mem, hoac khong hop le.');
END;
/


-- ============================================================
-- RB52, RB59
-- Compound trigger: thu thập danh sách MAHD bị ảnh hưởng,
-- sau đó tính lại số tiền hóa đơn ở AFTER STATEMENT
-- để tránh mutating table error.
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
        IF NOT PKG_COURT_CTX.G_INTERNAL_RECALC THEN
            FOR I IN 1 .. G_COUNT
                LOOP
                    PRC_CAP_NHAT_SO_TIEN_HOA_DON(G_MAHD_LIST(I));
                END LOOP;
        END IF;
    END AFTER STATEMENT;
    END;
/


-- ============================================================
-- RB52, RB53, RB61
-- Validate dòng dịch vụ và gán giá snapshot.
--
-- [REFACTORED] Trích logic lấy giá ra FN_LAY_GIA_DICH_VU.
-- Dòng dịch vụ không có trạng thái ĐÃ HUỶ (nhân viên chỉ tạo
-- dịch vụ đang sử dụng hoặc đã hoàn thành).
-- ============================================================
CREATE OR REPLACE TRIGGER TRG_BIUD_CTHD_DV_PRICE
    BEFORE INSERT OR UPDATE OF MASP, MADC, DON_GIA, IS_DELETED
    ON CHI_TIET_HOA_DON_DICH_VU_DA_DUNG
    FOR EACH ROW
DECLARE
    V_GIA CHI_TIET_HOA_DON_DICH_VU_DA_DUNG.DON_GIA%TYPE;
BEGIN
    -- Gọi function mới: validate + trả về giá snapshot
    IF :NEW.IS_DELETED = 0 THEN
        V_GIA := FN_LAY_GIA_DICH_VU(:NEW.MASP, :NEW.MADC);
    END IF;

    -- Gán giá snapshot khi INSERT hoặc khi đổi MASP/MADC
    IF INSERTING
        OR NVL(:OLD.MASP, '#') <> NVL(:NEW.MASP, '#')
        OR NVL(:OLD.MADC, '#') <> NVL(:NEW.MADC, '#')
        OR :NEW.DON_GIA IS NULL THEN
        IF :NEW.IS_DELETED = 0 THEN
            :NEW.DON_GIA := V_GIA;
        END IF;
    ELSIF NVL(:OLD.DON_GIA, -1) <> NVL(:NEW.DON_GIA, -1) THEN
        RAISE_APPLICATION_ERROR(-20052,
                                'Khong duoc sua DON_GIA dich vu thu cong. Hay doi MASP/MADC neu can lay gia moi.');
    END IF;
END;
/


-- ============================================================
-- RB52, RB59
-- Compound trigger: tính lại hóa đơn khi chi tiết dịch vụ thay đổi.
-- ============================================================
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
        FOR I IN 1 .. G_COUNT
            LOOP
                PRC_CAP_NHAT_SO_TIEN_HOA_DON(G_MAHD_LIST(I));
            END LOOP;
    END AFTER STATEMENT;
    END;
/


-- ============================================================
-- RB58, RB56: Validate cơ bản trên HOA_DON.
-- Kiểm tra 70% deposit thực sự nằm trong PRC_CAP_NHAT_SO_TIEN_HOA_DON.
-- ============================================================
CREATE OR REPLACE TRIGGER TRG_BIU_HOA_DON_VALIDATE
    BEFORE INSERT OR UPDATE OF GIAMGIA, TIEN_COC, TONGGIATRI, TONGTIEN, TRANGTHAI, IS_DELETED
    ON HOA_DON
    FOR EACH ROW
BEGIN
    :NEW.GIAMGIA := NVL(:NEW.GIAMGIA, 0);
    :NEW.TIEN_COC := NVL(:NEW.TIEN_COC, 0);
    :NEW.TONGGIATRI := NVL(:NEW.TONGGIATRI, 0);
    :NEW.TONGTIEN := NVL(:NEW.TONGTIEN, 0);

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


-- ============================================================
-- Compound trigger: tính lại hóa đơn khi MAKH/GIAMGIA/TIEN_COC thay đổi.
-- Dùng PKG_COURT_CTX.G_INTERNAL_RECALC để tránh vòng lặp đệ quy
-- khi PRC_CAP_NHAT_SO_TIEN_HOA_DON cập nhật ngược lại HOA_DON.
-- ============================================================
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
            FOR I IN 1 .. G_COUNT
                LOOP
                    PRC_CAP_NHAT_SO_TIEN_HOA_DON(G_MAHD_LIST(I));
                END LOOP;
        END IF;
    END AFTER STATEMENT;
    END;
/


-- ============================================================
-- RB62, RB63
-- Validate chuyển trạng thái thanh toán hóa đơn.
-- Kiểm tra: chỉ CHUA THANH TOAN → DA THANH TOAN,
-- phải có chi tiết DANG SU DUNG/DA HOAN THANH,
-- không còn chi tiết chờ sử dụng, sân con phải hoạt động.
-- ============================================================
CREATE OR REPLACE TRIGGER TRG_BU_HOA_DON_THANH_TOAN_CHECK
    BEFORE UPDATE OF TRANGTHAI
    ON HOA_DON
    FOR EACH ROW
DECLARE
    V_COUNT       PLS_INTEGER := 0;
    V_INVALID_SAN PLS_INTEGER := 0;
BEGIN
    -- Không cho thay đổi từ trạng thái đã kết thúc
    IF :OLD.TRANGTHAI IN ('ĐÃ THANH TOÁN', 'ĐÃ HUỶ')
        AND :NEW.TRANGTHAI <> :OLD.TRANGTHAI THEN
        RAISE_APPLICATION_ERROR(-20061, 'Khong duoc thay doi trang thai cua hoa don da thanh toan hoac da huy.');
    END IF;

    -- Validate chuyển sang ĐÃ THANH TOÁN
    IF :NEW.TRANGTHAI = 'ĐÃ THANH TOÁN' AND :OLD.TRANGTHAI <> 'ĐÃ THANH TOÁN' THEN
        IF :OLD.TRANGTHAI <> 'CHƯA THANH TOÁN' THEN
            RAISE_APPLICATION_ERROR(-20061, 'Chi duoc xac nhan thanh toan hoa don dang o trang thai CHUA THANH TOAN.');
        END IF;

        -- Phải có ít nhất 1 chi tiết thuê sân đang/đã sử dụng
        SELECT COUNT(1)
        INTO V_COUNT
        FROM CHI_TIET_HOA_DON_THUE_SAN CT
        WHERE CT.MAHD = :OLD.MAHD
          AND CT.IS_DELETED = 0
          AND CT.TRANGTHAI IN ('ĐANG SỬ DỤNG', 'ĐÃ HOÀN THÀNH');

        IF V_COUNT = 0 THEN
            RAISE_APPLICATION_ERROR(-20062, 'Hoa don khong co chi tiet thue san hop le de thanh toan.');
        END IF;

        -- Kiểm tra sân con không bị xóa/bảo trì
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

        -- Không còn chi tiết chờ sử dụng
        SELECT COUNT(1)
        INTO V_COUNT
        FROM CHI_TIET_HOA_DON_THUE_SAN
        WHERE MAHD = :OLD.MAHD
          AND IS_DELETED = 0
          AND TRANGTHAI IN ('ĐÃ ĐẶT CHỜ CỌC', 'ĐÃ CỌC', 'ĐÃ XÁC NHẬN');

        IF V_COUNT > 0 THEN
            RAISE_APPLICATION_ERROR(
                    -20062,
                    'Khong the thanh toan khi con chi tiet thue san chua bat dau su dung.'
                );
        END IF;
    END IF;
END;
/


-- ============================================================
-- RB63: Sau khi thanh toán, chuyển chi tiết ĐANG SỬ DỤNG → ĐÃ HOÀN THÀNH.
-- ============================================================
CREATE OR REPLACE TRIGGER TRG_AU_HOA_DON_COMPLETE_DETAILS
    AFTER UPDATE OF TRANGTHAI
    ON HOA_DON
    FOR EACH ROW
BEGIN
    IF :OLD.TRANGTHAI <> 'ĐÃ THANH TOÁN' AND :NEW.TRANGTHAI = 'ĐÃ THANH TOÁN' THEN
        PRC_CAP_NHAT_THANH_TOAN_CHI_TIET_HD(:NEW.MAHD);
    END IF;
END;
/


-- ============================================================
-- RB61: Nhập hàng sản phẩm → cộng tồn kho.
-- AFTER trigger vì chỉ side-effect lên bảng SAN_PHAM.
-- ============================================================
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
        -- Hoàn tác tồn cũ rồi áp dụng tồn mới
        IF :OLD.IS_DELETED = 0 THEN
            PRC_DIEU_CHINH_TON_SAN_PHAM(:OLD.MASP, -:OLD.SLTHUCNHAP);
        END IF;
        IF :NEW.IS_DELETED = 0 THEN
            PRC_DIEU_CHINH_TON_SAN_PHAM(:NEW.MASP, :NEW.SLTHUCNHAP);
        END IF;
    END IF;
END;
/


-- ============================================================
-- RB61: Nhập dụng cụ → cộng tồn kho.
-- ============================================================
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


-- ============================================================
-- RB61: Cập nhật tồn kho khi sử dụng dịch vụ.
-- Sản phẩm bán: trừ tồn khi ĐANG SỬ DỤNG, KHÔNG cộng lại khi ĐÃ HOÀN THÀNH.
-- Dụng cụ thuê: trừ tồn khi ĐANG SỬ DỤNG, cộng trả lại khi ĐÃ HOÀN THÀNH.
--
-- [FIX] Đổi BEFORE → AFTER vì trigger này chỉ side-effect
-- lên bảng SAN_PHAM/DUNG_CU_THE_THAO, không modify :NEW.
-- Nhất quán với TRG_CTNH_CAP_NHAT_TON_SAN_PHAM (đã dùng AFTER).
-- ============================================================
CREATE OR REPLACE TRIGGER TRG_CTHD_DV_CAP_NHAT_TON
    AFTER INSERT OR UPDATE OR DELETE
    ON CHI_TIET_HOA_DON_DICH_VU_DA_DUNG
    FOR EACH ROW
BEGIN
    -- ====== Hoàn tác hiệu ứng tồn kho cũ (khi UPDATE hoặc DELETE) ======
    IF DELETING OR UPDATING THEN
        -- Sản phẩm: cộng trả lại nếu trước đó đã trừ (ĐANG SỬ DỤNG hoặc ĐÃ HOÀN THÀNH)
        IF :OLD.MASP IS NOT NULL
            AND :OLD.IS_DELETED = 0
            AND :OLD.TRANGTHAI IN ('ĐANG SỬ DỤNG', 'ĐÃ HOÀN THÀNH') THEN
            PRC_DIEU_CHINH_TON_SAN_PHAM(:OLD.MASP, :OLD.SL);
        END IF;

        -- Dụng cụ: cộng trả lại chỉ khi đang thuê (ĐANG SỬ DỤNG)
        IF :OLD.MADC IS NOT NULL
            AND :OLD.IS_DELETED = 0
            AND :OLD.TRANGTHAI = 'ĐANG SỬ DỤNG' THEN
            PRC_DIEU_CHINH_TON_DUNG_CU(:OLD.MADC, :OLD.SL);
        END IF;
    END IF;

    -- ====== Áp dụng hiệu ứng tồn kho mới (khi INSERT hoặc UPDATE) ======
    IF INSERTING OR UPDATING THEN
        -- Sản phẩm: trừ tồn khi bán (ĐANG SỬ DỤNG hoặc ĐÃ HOÀN THÀNH)
        IF :NEW.MASP IS NOT NULL
            AND :NEW.IS_DELETED = 0
            AND :NEW.TRANGTHAI IN ('ĐANG SỬ DỤNG', 'ĐÃ HOÀN THÀNH') THEN
            PRC_DIEU_CHINH_TON_SAN_PHAM(:NEW.MASP, -:NEW.SL);
        END IF;

        -- Dụng cụ: trừ tồn chỉ khi đang thuê (ĐANG SỬ DỤNG)
        IF :NEW.MADC IS NOT NULL
            AND :NEW.IS_DELETED = 0
            AND :NEW.TRANGTHAI = 'ĐANG SỬ DỤNG' THEN
            PRC_DIEU_CHINH_TON_DUNG_CU(:NEW.MADC, -:NEW.SL);
        END IF;
    END IF;
END;
/


-- ============================================================
-- RB69: Tự động cập nhật DOANH_THU của khách hàng
-- khi hóa đơn thay đổi trạng thái/giá trị.
-- ============================================================
CREATE OR REPLACE TRIGGER TRG_CAPNHAT_DOANHTHU_KH
    AFTER INSERT OR UPDATE OF MAKH, TONGTIEN, TRANGTHAI, IS_DELETED OR DELETE
    ON HOA_DON
    FOR EACH ROW
DECLARE
    V_OLD_MONEY NUMBER(12, 2) := 0;
    V_NEW_MONEY NUMBER(12, 2) := 0;
BEGIN
    -- Tính giá trị doanh thu cũ cần trừ
    IF UPDATING OR DELETING THEN
        IF :OLD.TRANGTHAI = 'ĐÃ THANH TOÁN' AND :OLD.IS_DELETED = 0 THEN
            V_OLD_MONEY := :OLD.TONGTIEN;
        END IF;
    END IF;

    -- Tính giá trị doanh thu mới cần cộng
    IF INSERTING OR UPDATING THEN
        IF :NEW.TRANGTHAI = 'ĐÃ THANH TOÁN' AND :NEW.IS_DELETED = 0 THEN
            V_NEW_MONEY := :NEW.TONGTIEN;
        END IF;
    END IF;

    -- Áp dụng delta lên KHACH_HANG.DOANH_THU
    IF INSERTING THEN
        UPDATE KHACH_HANG SET DOANH_THU = DOANH_THU + V_NEW_MONEY WHERE MAKH = :NEW.MAKH;

    ELSIF DELETING THEN
        UPDATE KHACH_HANG SET DOANH_THU = DOANH_THU - V_OLD_MONEY WHERE MAKH = :OLD.MAKH;

    ELSIF UPDATING THEN
        IF :OLD.MAKH = :NEW.MAKH THEN
            -- Cùng khách hàng: chỉ cần delta
            UPDATE KHACH_HANG
            SET DOANH_THU = DOANH_THU + (V_NEW_MONEY - V_OLD_MONEY)
            WHERE MAKH = :NEW.MAKH;
        ELSE
            -- Đổi khách hàng: trừ cũ, cộng mới
            UPDATE KHACH_HANG SET DOANH_THU = DOANH_THU - V_OLD_MONEY WHERE MAKH = :OLD.MAKH;
            UPDATE KHACH_HANG SET DOANH_THU = DOANH_THU + V_NEW_MONEY WHERE MAKH = :NEW.MAKH;
        END IF;
    END IF;
END;
/


-- ============================================================
-- RB70: Tự động cập nhật hạng khách hàng dựa trên doanh thu.
-- Gọi FN_TIM_HANG_KHACH_HANG để xác định hạng phù hợp.
-- ============================================================
CREATE OR REPLACE TRIGGER TRG_CAPNHAT_HANG_KH
    BEFORE INSERT OR UPDATE OF DOANH_THU
    ON KHACH_HANG
    FOR EACH ROW
BEGIN
    :NEW.MA_HANG := FN_TIM_HANG_KHACH_HANG(:NEW.DOANH_THU);
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        :NEW.MA_HANG := NULL;
END;
/


-- ============================================================
-- RB71: Tự động cập nhật SO_LUONG_SAN trong KHU_VUC
-- khi thêm/xóa/chuyển sân con.
-- ============================================================
CREATE OR REPLACE TRIGGER TRG_CAPNHAT_SL_SAN_KV
    AFTER INSERT OR UPDATE OF MAKV, IS_DELETED OR DELETE
    ON SAN_CON
    FOR EACH ROW
BEGIN
    IF INSERTING THEN
        IF :NEW.IS_DELETED = 0 THEN
            UPDATE KHU_VUC
            SET SO_LUONG_SAN = SO_LUONG_SAN + 1
            WHERE MAKV = :NEW.MAKV;
        END IF;

    ELSIF DELETING THEN
        IF :OLD.IS_DELETED = 0 THEN
            UPDATE KHU_VUC
            SET SO_LUONG_SAN = SO_LUONG_SAN - 1
            WHERE MAKV = :OLD.MAKV;
        END IF;

    ELSIF UPDATING THEN
        IF :OLD.MAKV = :NEW.MAKV THEN
            -- Cùng khu vực: chỉ xử lý soft delete toggle
            IF :OLD.IS_DELETED = 0 AND :NEW.IS_DELETED = 1 THEN
                UPDATE KHU_VUC SET SO_LUONG_SAN = SO_LUONG_SAN - 1 WHERE MAKV = :NEW.MAKV;
            ELSIF :OLD.IS_DELETED = 1 AND :NEW.IS_DELETED = 0 THEN
                UPDATE KHU_VUC SET SO_LUONG_SAN = SO_LUONG_SAN + 1 WHERE MAKV = :NEW.MAKV;
            END IF;
        ELSE
            -- Chuyển khu vực: trừ cũ, cộng mới
            IF :OLD.IS_DELETED = 0 THEN
                UPDATE KHU_VUC SET SO_LUONG_SAN = SO_LUONG_SAN - 1 WHERE MAKV = :OLD.MAKV;
            END IF;
            IF :NEW.IS_DELETED = 0 THEN
                UPDATE KHU_VUC SET SO_LUONG_SAN = SO_LUONG_SAN + 1 WHERE MAKV = :NEW.MAKV;
            END IF;
        END IF;
    END IF;
END;
/
