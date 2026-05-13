# AGENTS.md — Hệ thống Quản lý & Đặt lịch Thuê Sân Thể Thao

> Tài liệu tham chiếu chính: System_description.md · DATABASE.md · CONSTRAINT-1.md
> Nguồn phân quyền chính xác: role_permission_design_v2.xlsx

---

## 1. Tổng quan dự án

Hệ thống quản lý chuỗi sân thể thao gồm nhiều chi nhánh. Hỗ trợ: đặt sân online/tại quầy, bán sản phẩm, cho thuê dụng cụ, quản lý hóa đơn, phân quyền người dùng và theo dõi doanh thu.

**Stack:**
- Backend: Java thuần túy (không dùng framework)
- UI: Java Swing + FlatLaf + icon bộ nhất quán
- Database: Oracle (JDBC)
- Architecture: **Modular MVC** — mỗi module có Model / View / Controller riêng + DAO layer

---

## 2. Kiến trúc & Cấu trúc Module

### Pattern bắt buộc cho mỗi module:
```
module/
  model/       → POJO thuần, ánh xạ 1-1 với bảng DB (không có logic nghiệp vụ)
  view/        → Swing panel/dialog/frame, chỉ xử lý UI
  controller/  → Điều phối giữa View và Service, xử lý sự kiện
  service/     → Logic nghiệp vụ, validation, transaction
  dao/         → Truy vấn SQL thuần qua JDBC, không có nghiệp vụ
```

### Quy tắc phân tầng — BẮT BUỘC:
- **View** không được gọi DAO trực tiếp — phải qua Controller → Service → DAO
- **DAO** chỉ chứa SQL, không validate nghiệp vụ
- **Service** chứa toàn bộ business logic, validation, điều phối transaction
- **Model** là POJO thuần — không inject dependency, không gọi DB

---

## 3. Naming Convention

| Thành phần | Convention | Ví dụ |
|---|---|---|
| Class | PascalCase | `BookingController`, `HoaDonDAO` |
| Method / Variable | camelCase | `findByMaKV()`, `tongGiaTri` |
| Constant | UPPER_SNAKE_CASE | `MAX_DEPOSIT_PERCENT` |
| DB column ánh xạ | UPPER_SNAKE_CASE | `IS_DELETED`, `GIOBATDAU` |
| Package | lowercase | `module.booking.dao` |

**Tên class theo module:**
- Model: `HoaDon`, `SanCon`, `BangGia`, `HangKhachHang`, `NhaCungCap`
- DAO: `HoaDonDAO`, `SanConDAO`, `BangGiaDAO`
- Service: `HoaDonService`, `DatSanService`
- Controller: `HoaDonController`, `DatSanController`
- View: `HoaDonPanel`, `DatSanDialog`

---

## 4. Database — Những điểm then chốt

### 4.1 Soft Delete — áp dụng TOÀN BỘ 28 bảng
- Mọi bảng đều có `IS_DELETED NUMBER(1) DEFAULT 0`
- **Không bao giờ DELETE vật lý** — chỉ `UPDATE ... SET IS_DELETED = 1`
- **Mọi SELECT phải có `WHERE IS_DELETED = 0`** — thiếu là bug nghiêm trọng

### 4.2 BANG_GIA — khung giờ tích hợp trực tiếp, KHÔNG có bảng KHUNG_GIO riêng
```sql
BANG_GIA(MABG, MAKV, GIOBATDAU, GIOKETTHUC, GIA, CREATED_AT, IS_DELETED)
-- RB45: GIOKETTHUC = GIOBATDAU + 1 (mỗi khung đúng 1 tiếng)
-- RB67: bộ (MAKV, GIOBATDAU, GIOKETTHUC) là duy nhất — không trùng khung giờ trong cùng khu vực
-- Giờ cao điểm 16:00–20:00 thể hiện qua GIA cao hơn, KHÔNG hardcode trong Java code
```

### 4.3 CHI_TIET_HOA_DON_THUE_SAN — giá khóa tại thời điểm đặt
```sql
-- RB68: DON_GIA_THUE tự động gán = BANG_GIA.GIA tại thời điểm INSERT
-- Bắt buộc validate: BANG_GIA.MAKV == SAN_CON.MAKV (cùng khu vực)
-- DON_GIA_THUE BẤT BIẾN sau INSERT — BANG_GIA.GIA thay đổi sau không ảnh hưởng hóa đơn cũ
```

### 4.4 Trạng thái CHI_TIET_HOA_DON_THUE_SAN (flow một chiều)
```
ĐÃ ĐẶT CHỜ CỌC → ĐÃ CỌC → ĐÃ XÁC NHẬN → ĐANG SỬ DỤNG → ĐÃ HOÀN THÀNH
                                                          ↘ ĐÃ HUỶ (từ bất kỳ bước nào)
```

### 4.5 Quan hệ User → Account → Role
```
USERS (thông tin cá nhân)
  └─ ACCOUNT (đăng nhập: USERNAME, PASSWORD_HASH, STATUS)
       └─ ACCOUNT_ROLE_GROUP (thuộc nhóm quyền nào)
            └─ ROLE_GROUP (OWNER / BRANCH_MANAGER / CASHIER / CUSTOMER)
                 └─ ACCOUNT_ROLE_GROUP_MAPPING → ROLE (quyền chi tiết theo FUNCTION_ID)
```

### 4.6 Auto-generated IDs
```
ROLE.ROLE_ID    → "ROLE - N"     (ví dụ: "ROLE - 1", "ROLE - 21")
MAPPING_ID      → "MAPPING - N"
```

### 4.7 Bảng DOANH_THU — dùng cho báo cáo, KHÔNG dùng KHACH_HANG.DOANH_THU
```sql
DOANH_THU(MADT, MACN, NOIDUNG, NGAY, TONGDOANHTHU, CREATED_AT, IS_DELETED)
-- Thống kê theo: chi nhánh (MACN), ngày (NGAY), loại thể thao (MACN→KHU_VUC→LOAI_THE_THAO)
-- KHACH_HANG.DOANH_THU = tích lũy riêng để xếp hạng KH — KHÔNG dùng để báo cáo tổng
```

---

## 5. Hệ thống Phân quyền

### 5.1 Bốn nhóm quyền (GROUP_ID — business code cố định)

| GROUP_ID | Tên | Phạm vi dữ liệu |
|---|---|---|
| `OWNER` | Chủ sân | **Tất cả chi nhánh** |
| `BRANCH_MANAGER` | Quản lý chi nhánh | **Chỉ chi nhánh được gán** (lọc theo NHAN_VIEN.MACN) |
| `CASHIER` | Nhân viên thu ngân | Chi nhánh mình làm việc |
| `CUSTOMER` | Khách hàng | Dữ liệu cá nhân của chính mình |

> OWNER kế thừa toàn bộ quyền của BRANCH_MANAGER và có thêm ACCOUNT_MANAGEMENT + ROLE_PERMISSION_MANAGEMENT.

### 5.2 Danh sách 21 FUNCTION_ID

| # | FUNCTION_ID | Tên chức năng | Mô tả |
|---|---|---|---|
| 1 | `DASHBOARD` | Dashboard | Màn hình tổng quan theo vai trò và phạm vi |
| 2 | `BRANCH_MANAGEMENT` | Quản lý chi nhánh | Thêm, sửa, xoá, tra cứu chi nhánh |
| 3 | `AREA_MANAGEMENT` | Quản lý khu vực | Quản lý khu vực thể thao trong chi nhánh |
| 4 | `COURT_MANAGEMENT` | Quản lý sân con | Quản lý sân con trong khu vực |
| 5 | `PRICE_MANAGEMENT` | Quản lý bảng giá | Quản lý bảng giá theo khu vực và khung giờ |
| 6 | `BOOKING_MANAGEMENT` | Quản lý đặt sân | Đặt sân tại quầy, xác nhận lịch, xác nhận khách đến |
| 7 | `INVOICE_MANAGEMENT` | Quản lý hóa đơn | Tạo, xem, xác nhận thanh toán, xuất hoá đơn |
| 8 | `SERVICE_MANAGEMENT` | Cung cấp dịch vụ | Ghi nhận sản phẩm/dụng cụ vào hoá đơn |
| 9 | `CUSTOMER_MANAGEMENT` | Quản lý khách hàng | Thêm mới, sửa, xoá, tra cứu khách hàng |
| 10 | `CUSTOMER_RANK_MANAGEMENT` | Quản lý hạng khách hàng | Quản lý hạng và mức chiết khấu |
| 11 | `EMPLOYEE_MANAGEMENT` | Quản lý nhân viên | Thêm, sửa, xoá, tra cứu nhân viên |
| 12 | `PRODUCT_MANAGEMENT` | Quản lý sản phẩm | Quản lý sản phẩm bán kèm (đồ ăn, nước uống...) |
| 13 | `EQUIPMENT_MANAGEMENT` | Quản lý dụng cụ thể thao | Quản lý dụng cụ cho thuê |
| 14 | `IMPORT_MANAGEMENT` | Quản lý nhập hàng | Quản lý phiếu nhập kho sản phẩm và dụng cụ |
| 15 | `SUPPLIER_MANAGEMENT` | Quản lý nhà cung cấp | Quản lý thông tin nhà cung cấp |
| 16 | `REVENUE_MANAGEMENT` | Quản lý tài chính / doanh thu | Tra cứu và xuất báo cáo doanh thu chi nhánh |
| 17 | `ACCOUNT_MANAGEMENT` | Quản lý tài khoản | Thêm, sửa, xoá, tra cứu tài khoản người dùng |
| 18 | `PERSONAL_PROFILE_MANAGEMENT` | Hồ sơ cá nhân | Cập nhật thông tin cá nhân và đổi mật khẩu |
| 19 | `CUSTOMER_BOOKING_SELF_SERVICE` | Đặt sân khách hàng | Khách tự đặt online, thanh toán cọc, huỷ, xem lịch sử |
| 20 | `SPORT_TYPE_MANAGEMENT` | Quản lý loại thể thao | Quản lý danh mục loại thể thao (master data) |
| 21 | `ROLE_PERMISSION_MANAGEMENT` | Phân quyền tài khoản | Gán/thu hồi nhóm quyền cho tài khoản (chỉ Owner) |

### 5.3 Ma trận phân quyền theo role

#### OWNER — toàn quyền (VIEW+ADD+EDIT+DELETE+DOWNLOAD) trên tất cả 21 functions.

---

#### BRANCH_MANAGER — toàn quyền trên 18 functions:

> Không có: `ACCOUNT_MANAGEMENT`, `ROLE_PERMISSION_MANAGEMENT` (chỉ OWNER), `CUSTOMER_BOOKING_SELF_SERVICE` (chỉ CUSTOMER)

`DASHBOARD` · `BRANCH_MANAGEMENT` · `AREA_MANAGEMENT` · `COURT_MANAGEMENT` · `PRICE_MANAGEMENT` · `BOOKING_MANAGEMENT` · `INVOICE_MANAGEMENT` · `SERVICE_MANAGEMENT` · `CUSTOMER_MANAGEMENT` · `CUSTOMER_RANK_MANAGEMENT` · `EMPLOYEE_MANAGEMENT` · `PRODUCT_MANAGEMENT` · `EQUIPMENT_MANAGEMENT` · `IMPORT_MANAGEMENT` · `SUPPLIER_MANAGEMENT` · `REVENUE_MANAGEMENT` · `PERSONAL_PROFILE_MANAGEMENT` · `SPORT_TYPE_MANAGEMENT`

---

#### CASHIER — quyền giới hạn tại quầy (không có IMPORT_MANAGEMENT):

| FUNCTION_ID | VIEW | ADD | EDIT | DELETE | DOWNLOAD |
|---|---|---|---|---|---|
| `DASHBOARD` | ✅ | ❌ | ❌ | ❌ | ❌ |
| `BOOKING_MANAGEMENT` | ✅ | ❌ | ❌ | ✅ | ❌ |
| `INVOICE_MANAGEMENT` | ✅ | ✅ | ✅ | ❌ | ✅ |
| `SERVICE_MANAGEMENT` | ❌ | ✅ | ✅ | ❌ | ❌ |
| `PERSONAL_PROFILE_MANAGEMENT` | ✅ | ❌ | ✅ | ❌ | ❌ |

---

#### CUSTOMER — tự phục vụ:

| FUNCTION_ID | VIEW | ADD | EDIT | DELETE | DOWNLOAD |
|---|---|---|---|---|---|
| `DASHBOARD` | ✅ | ❌ | ❌ | ❌ | ❌ |
| `CUSTOMER_BOOKING_SELF_SERVICE` | ✅ | ✅ | ✅ | ❌ | ❌ |
| `PERSONAL_PROFILE_MANAGEMENT` | ✅ | ❌ | ✅ | ❌ | ❌ |

> ⚠️ Nguồn chính xác cho từng bit quyền (CAN_VIEW/ADD/EDIT/DELETE/DOWNLOAD): **role_permission_design_v2.xlsx**

### 5.4 Cách kiểm tra quyền trong code
```java
// 1. Lấy GROUP_ID từ ACCOUNT_ROLE_GROUP của account đang login
// 2. Từ GROUP_ID → ACCOUNT_ROLE_GROUP_MAPPING → ROLE_ID ứng với FUNCTION_ID cần check
// 3. Đọc CAN_ADD / CAN_EDIT / CAN_DELETE / CAN_VIEW / CAN_DOWNLOAD từ ROLE
// 4. Phạm vi dữ liệu (branch scope): kiểm tra riêng qua NHAN_VIEN.MACN
```

---

## 6. Nghiệp vụ quan trọng — PHẢI hiểu trước khi code

### 6.1 Đặt sân online (quy trình đầy đủ)
1. KH chọn chi nhánh → khu vực → sân → khung giờ (từ BANG_GIA)
2. Gửi yêu cầu → trạng thái: `ĐÃ ĐẶT CHỜ CỌC`
3. Phải thanh toán cọc trong **5 phút** — nếu không → tự động hủy
4. Cọc thành công → `ĐÃ CỌC` → Nhân viên xác nhận → `ĐÃ XÁC NHẬN`
5. Khách đến nhận sân → `ĐANG SỬ DỤNG` → Thanh toán cuối → `ĐÃ HOÀN THÀNH`

### 6.2 Phân biệt đặt trước vs. chơi ngay
```
HOA_DON.TIENCOC > 0  →  Đặt trước (online hoặc tại quầy)
HOA_DON.TIENCOC = 0  →  Chơi ngay / walk-in (nhận sân ngay, không qua bước cọc)
```

### 6.3 Quy tắc hủy và hoàn cọc
- Hủy **trước > 2 ngày** so với ngày nhận sân → **hoàn 100% cọc**
- Hủy trong vòng **≤ 2 ngày** → **mất cọc, không hoàn**
- CASHIER có quyền tự hủy đơn (DELETE trên BOOKING_MANAGEMENT)

### 6.4 Công thức tính tiền hóa đơn (RB52, RB57, RB59)
```
TONGGIATRI   = Σ DON_GIA_THUE (thuê sân) + Σ (SL × DON_GIA) (dịch vụ)

GIAM_HANG    = Σ DON_GIA_THUE × CHIETKHAU / 100
               ↑ chỉ áp dụng trên tiền thuê sân, KHÔNG áp dụng trên dịch vụ

GIAM_HOA_DON = (TONGGIATRI - GIAM_HANG) × GIAMGIA / 100
               ↑ giảm giá thủ công nhân viên (khuyến mãi/lễ) — KHÔNG tự lấy từ hạng KH

TONGTIEN     = TONGGIATRI - GIAM_HANG - GIAM_HOA_DON - TIENCOC
```

### 6.5 Ba trigger tự động tại DB (không code thủ công trong Java)

| Trigger | Điều kiện kích hoạt | Tác động |
|---|---|---|
| **Doanh thu KH** | HOA_DON → `ĐÃ THANH TOÁN` hoặc bị soft-delete | Cộng/trừ TONGTIEN vào `KHACH_HANG.DOANH_THU` |
| **Hạng KH** | KHACH_HANG.DOANH_THU thay đổi | Tự cập nhật `MA_HANG` theo `HANG_KHACH_HANG.MUCTIEN` cao nhất đạt được |
| **Số lượng sân** | SAN_CON thêm / soft-delete / đổi KV | Cập nhật `KHU_VUC.SOLUONGSAN` |

### 6.6 Tồn kho (RB61) — phân biệt sản phẩm bán vs. dụng cụ cho thuê
- **Sản phẩm** (SAN_PHAM): trừ SL_TON khi `ĐANG SỬ DỤNG`, **KHÔNG hoàn** khi `ĐÃ HOÀN THÀNH`
- **Dụng cụ** (DUNG_CU_THE_THAO): trừ SL_TON khi `ĐANG SỬ DỤNG`, **hoàn lại** khi `ĐÃ HOÀN THÀNH`
- Không cho phép xuất khi SL_TON = 0

### 6.7 Ràng buộc không trùng lịch (RB66) — kiểm tra trước mỗi INSERT
```sql
SELECT COUNT(*)
FROM CHI_TIET_HOA_DON_THUE_SAN
WHERE MASAN = ?
  AND TRUNC(NGAYTHUE) = TRUNC(?)
  AND MABG = ?
  AND TRANGTHAI NOT IN ('ĐÃ HUỶ')
  AND IS_DELETED = 0
-- Nếu COUNT > 0 → từ chối, báo lỗi trùng lịch
```

### 6.8 CHI_TIET_HOA_DON_DICH_VU_DA_DUNG — MASP hoặc MADC, không được cả hai
```
RB53: (MASP IS NOT NULL AND MADC IS NULL)
   OR (MASP IS NULL AND MADC IS NOT NULL)
-- Vi phạm → không cho INSERT/UPDATE
```

---

## 7. Quy tắc viết SQL / DAO

```sql
-- ✅ ĐÚNG
SELECT * FROM BANG_GIA WHERE MAKV = ? AND IS_DELETED = 0;
UPDATE SAN_CON SET IS_DELETED = 1 WHERE MASAN = ?;

-- ❌ SAI — thiếu IS_DELETED
SELECT * FROM BANG_GIA WHERE MAKV = ?;

-- ❌ SAI — hard delete
DELETE FROM SAN_CON WHERE MASAN = ?;

-- ❌ SAI — SQL injection
String sql = "SELECT * FROM USERS WHERE USER_ID = '" + id + "'";
```

**Luôn dùng PreparedStatement với `?` — không bao giờ nối chuỗi SQL.**

---

## 8. Quy tắc viết Swing / UI

- Dùng **FlatLaf** làm Look and Feel — không override màu/font thủ công
- Icon nhất quán — dùng cùng bộ icon trong toàn project
- View chỉ gọi `controller.doAction(params)` — không xử lý DB hay business logic
- Lỗi hiển thị qua `JOptionPane` với message rõ ràng — không in raw stacktrace ra UI
- Long-running task (query nặng) → `SwingWorker` — **không block EDT**

---

## 9. Quy tắc viết Test

- Test business logic tại **Service layer**
- Tên test: `methodName_scenario_expectedResult()`
  ```java
  createBooking_duplicateSlot_throwsException()
  cancelBooking_within2Days_noRefund()
  calculateInvoice_withRankDiscount_appliesOnCourtOnly()
  ```
- Luôn có test case cho:
  - Happy path
  - Edge case: đặt trùng giờ, hủy đúng/trễ 2 ngày, cọc = 0, tồn kho = 0
  - Soft delete: record đã `IS_DELETED = 1` không xuất hiện trong kết quả

---

## 10. Anti-patterns — KHÔNG làm

| ❌ Không làm | ✅ Thay bằng |
|---|---|
| DELETE vật lý bất kỳ bảng nào | `UPDATE SET IS_DELETED = 1` |
| Query thiếu `WHERE IS_DELETED = 0` | Luôn thêm filter IS_DELETED |
| View gọi DAO trực tiếp | View → Controller → Service → DAO |
| Hardcode giờ cao điểm `16` hoặc `20` | Đọc GIA từ BANG_GIA, không hardcode |
| Sửa DON_GIA_THUE sau khi đã INSERT | DON_GIA_THUE bất biến sau khi tạo (RB68) |
| Nối chuỗi SQL | PreparedStatement với `?` |
| Xử lý nặng trực tiếp trên Swing thread | Dùng `SwingWorker` |
| Bỏ qua kiểm tra quyền trước action | Validate GROUP_ID + CAN_* trước mọi action |
| CASHIER nhập hàng | IMPORT_MANAGEMENT chỉ BRANCH_MANAGER + OWNER |
| BRANCH_MANAGER quản lý/phân quyền tài khoản | ACCOUNT_MANAGEMENT + ROLE_PERMISSION_MANAGEMENT chỉ OWNER |
| Dùng KHACH_HANG.DOANH_THU để báo cáo tổng | Dùng bảng DOANH_THU cho REVENUE_MANAGEMENT |
| MASP và MADC cùng có giá trị trong 1 dòng dịch vụ | Chỉ một trong hai được phép (RB53) |