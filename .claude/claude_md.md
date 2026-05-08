# CLAUDE.md — Hệ thống Quản lý & Đặt lịch Thuê Sân Thể Thao

## 1. Tổng quan dự án

Hệ thống quản lý chuỗi sân thể thao gồm nhiều chi nhánh. Hỗ trợ: đặt sân online/tại quầy, bán sản phẩm, cho thuê dụng cụ, quản lý hóa đơn, phân quyền người dùng và theo dõi doanh thu.

**Stack:**
- Backend: Java thuần túy (không dùng framework)
- UI: Java Swing + FlatLaf + icon bộ nhất quán
- Database: Oracle (JDBC)
- Architecture: **Modular MVC** — mỗi module có Model / View / Controller riêng biệt + DAO layer

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

### Quy tắc phân tầng (QUAN TRỌNG):
- **View** không được gọi DAO trực tiếp — phải qua Controller → Service → DAO
- **DAO** chỉ chứa SQL, không validate nghiệp vụ
- **Service** chứa toàn bộ business logic, validation, và điều phối transaction
- **Model** là POJO thuần — không inject dependency, không gọi DB

---

## 3. Naming Convention

| Thành phần | Convention | Ví dụ |
|---|---|---|
| Class | PascalCase | `BookingController`, `HoaDonDAO` |
| Method / Variable | camelCase | `findByMaKV()`, `tongGiaTri` |
| Constant | UPPER_SNAKE_CASE | `MAX_DEPOSIT_PERCENT` |
| DB column trong code | UPPER_SNAKE_CASE (ánh xạ từ DB) | `IS_DELETED`, `GIOBATDAU` |
| Package | lowercase | `module.booking.dao` |
| SQL alias | snake_case hoặc UPPER | khớp tên cột Oracle |

**Tên class theo module:**
- Model: `HoaDon`, `SanCon`, `BangGia`
- DAO: `HoaDonDAO`, `SanConDAO`
- Service: `HoaDonService`, `BangGiaService`
- Controller: `HoaDonController`
- View: `HoaDonPanel`, `DatSanDialog`

---

## 4. Database — Những điểm then chốt

### 4.1 Soft Delete — áp dụng TOÀN BỘ bảng
- Mọi bảng đều có cột `IS_DELETED NUMBER(1) DEFAULT 0`
- **Không bao giờ DELETE vật lý** — chỉ `UPDATE ... SET IS_DELETED = 1`
- **Mọi câu SELECT phải luôn có `WHERE IS_DELETED = 0`** (dễ quên, hay gây bug)

### 4.2 Bảng BANG_GIA — khung giờ tích hợp trực tiếp
```sql
BANG_GIA(MABG, MAKV, GIOBATDAU, GIOKETTHUC, GIA, CREATED_AT, IS_DELETED)
-- Không có bảng KHUNG_GIO riêng — giờ bắt đầu/kết thúc nằm trong BANG_GIA
-- Constraint: GIOBATDAU BETWEEN 0 AND 23, GIOKETTHUC BETWEEN 1 AND 24
-- Mỗi bộ (MAKV, GIOBATDAU, GIOKETTHUC) là duy nhất (RB67)
-- Cao điểm: 16:00–20:00 (giá cao hơn, được config qua GIA, không hard-code)
```

### 4.3 CHI_TIET_HOA_DON_THUE_SAN — giá khóa tại thời điểm đặt
```sql
-- DON_GIA_THUE được lấy từ BANG_GIA.GIA tại thời điểm INSERT (RB68)
-- Sau khi INSERT xong, DON_GIA_THUE KHÔNG thay đổi dù BANG_GIA.GIA thay đổi
-- Phải validate: MABG.MAKV == SAN_CON.MAKV (cùng khu vực)
```

### 4.4 Auto-generated IDs
```
ROLE.ROLE_ID        → format: "ROLE - N"     (ví dụ: "ROLE - 1", "ROLE - 15")
MAPPING_ID          → format: "MAPPING - N"
Các bảng khác       → business code hoặc sequence tự định nghĩa
```

### 4.5 Quan hệ User → Account → Role
```
USERS (thông tin người) 
  └─ ACCOUNT (đăng nhập) 
       └─ ACCOUNT_ROLE_GROUP (thuộc nhóm nào: OWNER/BRANCH_MANAGER/CASHIER/CUSTOMER)
            └─ ROLE_GROUP → ACCOUNT_ROLE_GROUP_MAPPING → ROLE (quyền chi tiết)
```

---

## 5. Hệ thống Phân quyền

### 5.1 Bốn nhóm quyền (GROUP_ID — dùng làm business code)

| GROUP_ID | Tên | Phạm vi |
|---|---|---|
| `OWNER` | Chủ sân | Toàn hệ thống, tất cả chi nhánh |
| `BRANCH_MANAGER` | Quản lý chi nhánh | Chỉ trong chi nhánh được gán |
| `CASHIER` | Nhân viên thu ngân | Chỉ nghiệp vụ tại quầy |
| `CUSTOMER` | Khách hàng | Chỉ chức năng khách hàng |

### 5.2 Ma trận quyền chi tiết (FUNCTION_ID → ROLE_NAME → permissions)

#### OWNER — toàn quyền (VIEW+ADD+EDIT+DELETE+DOWNLOAD = 1,1,1,1,1) trên tất cả 15 functions:
`ACCOUNT_MANAGEMENT`, `REPORT_MANAGEMENT`, `BRANCH_MANAGEMENT`, `AREA_MANAGEMENT`, `COURT_MANAGEMENT`, `PRICE_MANAGEMENT`, `BOOKING_MANAGEMENT`, `SERVICE_MANAGEMENT`, `INVOICE_MANAGEMENT`, `CUSTOMER_MANAGEMENT`, `EMPLOYEE_MANAGEMENT`, `PRODUCT_MANAGEMENT`, `EQUIPMENT_MANAGEMENT`, `IMPORT_MANAGEMENT`, `FINANCE_MANAGEMENT`

#### BRANCH_MANAGER — toàn quyền (1,1,1,1,1) trên 14 functions (không có ACCOUNT_MANAGEMENT):
`REPORT_MANAGEMENT`, `BRANCH_MANAGEMENT`, `AREA_MANAGEMENT`, `COURT_MANAGEMENT`, `PRICE_MANAGEMENT`, `BOOKING_MANAGEMENT`, `SERVICE_MANAGEMENT`, `INVOICE_MANAGEMENT`, `CUSTOMER_MANAGEMENT`, `EMPLOYEE_MANAGEMENT`, `PRODUCT_MANAGEMENT`, `EQUIPMENT_MANAGEMENT`, `IMPORT_MANAGEMENT`, `FINANCE_MANAGEMENT`

#### CASHIER — quyền giới hạn:
| FUNCTION_ID | VIEW | ADD | EDIT | DELETE | DOWNLOAD |
|---|---|---|---|---|---|
| `BOOKING_MANAGEMENT` | ✅ | ❌ | ❌ | ✅ | ❌ |
| `SERVICE_MANAGEMENT` | ❌ | ✅ | ✅ | ❌ | ❌ |
| `INVOICE_MANAGEMENT` | ✅ | ✅ | ✅ | ❌ | ✅ |
| `IMPORT_MANAGEMENT` | ✅ | ✅ | ✅ | ❌ | ✅ |

#### CUSTOMER — quyền tối thiểu:
| FUNCTION_ID | VIEW | ADD | EDIT | DELETE | DOWNLOAD |
|---|---|---|---|---|---|
| `BOOKING_MANAGEMENT` | ✅ | ✅ | ✅ | ❌ | ❌ |
| `CUSTOMER_MANAGEMENT` (profile) | ❌ | ❌ | ✅ | ❌ | ❌ |

### 5.3 Cách kiểm tra quyền trong code
```java
// Luôn kiểm tra qua ROLE trước khi cho phép action
// Lấy GROUP_ID từ ACCOUNT_ROLE_GROUP của account đang đăng nhập
// Map sang ROLE qua ACCOUNT_ROLE_GROUP_MAPPING
// Check CAN_ADD / CAN_EDIT / CAN_DELETE / CAN_VIEW / CAN_DOWNLOAD
```

---

## 6. Nghiệp vụ quan trọng — PHẢI hiểu trước khi code

### 6.1 Đặt sân online
1. KH chọn chi nhánh → khu vực → sân → khung giờ (từ BANG_GIA)
2. Gửi yêu cầu → hệ thống yêu cầu thanh toán cọc trong **5 phút** (nếu không → tự động hủy)
3. Cọc = **70% tổng tiền thuê sân** (TIENCOC = 0.7 × TONGGIATRI)
4. Cọc = 0 → khách chơi ngay (walk-in), không đặt trước

### 6.2 Hủy đặt sân
- Hủy trước **>2 ngày** so với ngày nhận sân → **hoàn 100% cọc**
- Hủy trong vòng **≤2 ngày** → **mất cọc**

### 6.3 Doanh thu tự động (trigger)
- Khi HOA_DON được set `TRANGTHAI = 'ĐÃ THANH TOÁN'` và `IS_DELETED = 0` → tự động cộng TONGTIEN vào `KHACH_HANG.DOANH_THU`
- Khi hóa đơn bị soft-delete hoặc hủy → tự động **trừ lại** doanh thu
- Thay đổi MAKH/TONGTIEN/TRANGTHAI → phải điều chỉnh doanh thu của cả hai khách hàng liên quan

### 6.4 Hạng khách hàng tự động (trigger)
- Sau mỗi thay đổi `DOANH_THU` → tự động so với `HANG_KHACH_HANG.MUCTIEN`
- Gán `MA_HANG` = hạng có MUCTIEN cao nhất mà KH đạt được hoặc vượt qua
- Doanh thu giảm → tự động **hạ hạng**

### 6.5 Số lượng sân tự động (trigger)
- Thêm SAN_CON → `KHU_VUC.SOLUONGSAN + 1`
- Xóa mềm / chuyển khu vực SAN_CON → cập nhật SOLUONGSAN tương ứng

### 6.6 Ràng buộc không trùng lịch (RB66)
- Không tồn tại 2 dòng CHI_TIET_HOA_DON_THUE_SAN cùng `MASAN + TRUNC(NGAYTHUE) + MABG` còn hiệu lực và chưa hủy

---

## 7. Quy tắc viết SQL / DAO

```sql
-- ✅ ĐÚNG — luôn filter IS_DELETED
SELECT * FROM BANG_GIA WHERE MAKV = ? AND IS_DELETED = 0

-- ❌ SAI — quên IS_DELETED
SELECT * FROM BANG_GIA WHERE MAKV = ?

-- ✅ ĐÚNG — soft delete
UPDATE SAN_CON SET IS_DELETED = 1 WHERE MASAN = ?

-- ❌ SAI — hard delete
DELETE FROM SAN_CON WHERE MASAN = ?

-- ✅ Kiểm tra trùng lịch trước khi INSERT đặt sân
SELECT COUNT(*) FROM CHI_TIET_HOA_DON_THUE_SAN
WHERE MASAN = ? AND TRUNC(NGAYTHUE) = TRUNC(?) AND MABG = ?
  AND TRANGTHAI NOT IN ('ĐÃ HỦY') AND IS_DELETED = 0
```

**Dùng PreparedStatement — không bao giờ nối chuỗi SQL** (SQL injection).

---

## 8. Quy tắc viết Swing / UI

- Dùng **FlatLaf** làm Look and Feel — không override màu/font thủ công trừ khi cần thiết
- Icon nhất quán — dùng cùng bộ icon trong toàn project
- Tách logic khỏi View: View chỉ `controller.doAction(params)`, không xử lý DB
- Hiển thị thông báo lỗi bằng `JOptionPane` với message rõ ràng (không stacktrace raw)
- Long-running task (query DB nặng) → chạy trên `SwingWorker`, không block EDT

---

## 9. Quy tắc viết Test

- Test nghiệp vụ trong **Service layer** (không test DAO trực tiếp nếu cần mock DB)
- Luôn có test case cho:
  - Happy path
  - Edge case (đặt sân trùng giờ, hủy đúng hạn/trễ hạn, cọc = 0)
  - Kiểm tra IS_DELETED trong kết quả query
- Tên test: `methodName_scenario_expectedResult()`
  ```java
  // Ví dụ
  void createBooking_duplicateSlot_throwsException()
  void cancelBooking_within2Days_noRefund()
  ```

---

## 10. Anti-patterns — KHÔNG làm

| ❌ Không làm | ✅ Thay bằng |
|---|---|
| DELETE vật lý bất kỳ bảng nào | `UPDATE SET IS_DELETED = 1` |
| Query thiếu `IS_DELETED = 0` | Luôn thêm filter IS_DELETED |
| View gọi DAO trực tiếp | View → Controller → Service → DAO |
| Hardcode giá cao điểm | Đọc từ BANG_GIA, không hardcode "16-20h" |
| Thay đổi DON_GIA_THUE sau khi đã INSERT | DON_GIA_THUE là bất biến sau khi tạo |
| Nối chuỗi SQL | Dùng PreparedStatement với `?` |
| Xử lý dài trên EDT (Swing thread) | Dùng SwingWorker |
| Bỏ qua kiểm tra quyền | Validate GROUP_ID + CAN_* trước mọi action |

---

## 11. Danh sách FUNCTION_ID (dùng trong code kiểm tra quyền)

```
ACCOUNT_MANAGEMENT   REPORT_MANAGEMENT    BRANCH_MANAGEMENT
AREA_MANAGEMENT      COURT_MANAGEMENT     CUSTOMER_MANAGEMENT
EMPLOYEE_MANAGEMENT  PRICE_MANAGEMENT     BOOKING_MANAGEMENT
SERVICE_MANAGEMENT   INVOICE_MANAGEMENT   PRODUCT_MANAGEMENT
EQUIPMENT_MANAGEMENT IMPORT_MANAGEMENT    FINANCE_MANAGEMENT
```

## 12. Danh sách GROUP_ID (không thay đổi — business code)
```
OWNER  |  BRANCH_MANAGER  |  CASHIER  |  CUSTOMER
```
