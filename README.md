# Hệ thống Quản lý & Đặt lịch Thuê Sân Thể Thao

> Tài liệu hướng dẫn cài đặt và giới thiệu tổng quan hệ thống

---

## Mục lục

1. [Giới thiệu tổng quan](#1-giới-thiệu-tổng-quan)
2. [Kiến trúc hệ thống](#2-kiến-trúc-hệ-thống)
3. [Các module chức năng](#3-các-module-chức-năng)
4. [Hệ thống phân quyền](#4-hệ-thống-phân-quyền)
5. [Yêu cầu môi trường](#5-yêu-cầu-môi-trường)
6. [Hướng dẫn cài đặt](#6-hướng-dẫn-cài-đặt)
7. [Cấu hình hệ thống](#7-cấu-hình-hệ-thống)
8. [Thiết lập cơ sở dữ liệu](#8-thiết-lập-cơ-sở-dữ-liệu)
9. [Build và chạy chương trình](#9-build-và-chạy-chương-trình)
10. [Tài khoản mặc định](#10-tài-khoản-mặc-định)
11. [Xử lý sự cố thường gặp](#11-xử-lý-sự-cố-thường-gặp)

---

## 1. Giới thiệu tổng quan

**Sport Court Booking Management** là ứng dụng desktop quản lý chuỗi sân thể thao đa chi nhánh. Hệ thống hỗ trợ toàn bộ quy trình vận hành từ đặt sân, lập hóa đơn, thanh toán cho đến quản lý nhân sự và báo cáo doanh thu.

### Đặc điểm nổi bật

- **Đa chi nhánh**: Quản lý nhiều chi nhánh với phạm vi dữ liệu độc lập theo vai trò
- **Đặt sân hai kênh**: Tự đặt online (khách hàng) và đặt tại quầy (nhân viên)
- **Thanh toán tích hợp**: Hỗ trợ thanh toán cọc qua QR VietQR / PayOS
- **Phân quyền chi tiết**: 4 nhóm vai trò với 21 chức năng và 5 loại quyền hành động
- **Báo cáo doanh thu**: Thống kê theo chi nhánh, ngày, loại thể thao với biểu đồ JFreeChart
- **Xuất PDF**: Xuất hóa đơn dạng PDF bằng Apache PDFBox
- **Gửi email OTP**: Xác thực quên mật khẩu qua Gmail SMTP

### Công nghệ sử dụng

| Thành phần | Công nghệ |
|---|---|
| Ngôn ngữ | Java 17+ (không dùng framework) |
| Giao diện | Java Swing + FlatLaf 3.7 |
| Font | Lexend, Roboto, JetBrains Mono |
| Layout | MigLayout 11.3 |
| Cơ sở dữ liệu | Oracle Database (JDBC ojdbc17) |
| Build tool | Apache Maven |
| Biểu đồ | JFreeChart 1.5.4 |
| Xuất PDF | Apache PDFBox 3.0.2 |
| Email | Jakarta Mail 2.0.1 (Gmail SMTP) |
| Thanh toán | PayOS Java SDK 1.0.3 |
| QR Code | ZXing (Google) 3.5.4 |
| Logging | SLF4J + Logback |
| Date picker | LGoodDatePicker 11.2.1 |

---

## 2. Kiến trúc hệ thống

Dự án tổ chức theo **Modular MVC** — mỗi module nghiệp vụ có đầy đủ 5 tầng riêng biệt:

```
src/main/java/com/sportcourt/
├── App.java                        ← Entry point
├── common/
│   ├── db/                         ← Connection pool, JDBC utils
│   ├── style/                      ← AppFonts, FlatLaf theme, UIScale
│   └── ui/                         ← Sidebar, ContentPanel dùng chung
└── modules/
    ├── auth/                       ← Xác thực & phân quyền
    ├── dashboard/                  ← Dashboard tổng quan
    ├── branch/                     ← Quản lý chi nhánh
    ├── area/                       ← Quản lý khu vực
    ├── court/                      ← Quản lý sân con
    ├── cost/                       ← Quản lý bảng giá
    ├── booking_management/         ← Quản lý đặt sân (nhân viên)
    ├── bill/                       ← Quản lý hóa đơn
    ├── customer/                   ← Quản lý khách hàng
    ├── customer_booking/           ← Khách tự đặt sân online
    ├── customer_history/           ← Lịch sử đặt sân khách hàng
    ├── customer_rank/              ← Quản lý hạng khách hàng
    ├── staff/                      ← Quản lý nhân viên
    ├── staff_type/                 ← Quản lý loại nhân viên
    ├── product/                    ← Quản lý sản phẩm
    ├── equipment/                  ← Quản lý dụng cụ thể thao
    ├── imports/                    ← Quản lý nhập hàng
    ├── supplier/                   ← Quản lý nhà cung cấp
    ├── sport_type/                 ← Quản lý loại thể thao
    ├── revenue/                    ← Quản lý doanh thu
    ├── account/                    ← Quản lý tài khoản
    └── user_profile/               ← Hồ sơ cá nhân
```

**Luồng xử lý bắt buộc:**

```
View  →  Controller  →  Service  →  DAO  →  Oracle DB
```

Mỗi module gồm:

| Tầng | Trách nhiệm |
|---|---|
| `view/` | Swing panel/dialog — chỉ hiển thị UI, không gọi DB |
| `controller/` | Nhận sự kiện từ View, gọi Service |
| `service/` | Business logic, validation, điều phối transaction |
| `dao/` | SQL thuần qua PreparedStatement, không có nghiệp vụ |
| `model/` hoặc `entity/` | POJO ánh xạ 1-1 bảng DB |
| `dto/` | Data Transfer Object giữa các tầng |

---

## 3. Các module chức năng

### 3.1 Xác thực (auth)

- Đăng nhập với tài khoản + mật khẩu (SHA-256)
- Đăng ký tài khoản khách hàng mới
- Quên mật khẩu — gửi OTP xác thực qua email
- Đổi mật khẩu
- Quản lý session người dùng

### 3.2 Dashboard

- Màn hình tổng quan theo vai trò:
  - **Owner/Branch Manager**: thống kê doanh thu, số đơn đặt, biểu đồ JFreeChart
  - **Cashier**: danh sách đơn chờ xử lý hôm nay
  - **Customer**: lịch sắp chơi và lịch sử

### 3.3 Quản lý chi nhánh (branch)

- Thêm, sửa, xoá (soft delete) chi nhánh
- Tìm kiếm theo tên, địa chỉ
- Xem danh sách khu vực thuộc chi nhánh

### 3.4 Quản lý khu vực (area)

- Thêm, sửa, xoá khu vực thể thao trong chi nhánh
- Gắn loại thể thao cho khu vực
- Tự động cập nhật số lượng sân con (trigger DB)

### 3.5 Quản lý sân con (court)

- Thêm, sửa, xoá sân con trong khu vực
- Xem chi tiết thông tin sân

### 3.6 Quản lý bảng giá (cost)

- Quản lý khung giờ và giá theo khu vực
- Mỗi khung giờ đúng 1 tiếng, không trùng trong cùng khu vực
- Giờ cao điểm (16:00–20:00) phản ánh qua giá cao hơn — không hardcode

### 3.7 Quản lý đặt sân — phía nhân viên (booking_management)

- Xem danh sách yêu cầu đặt sân chờ xử lý
- Xác nhận lịch đặt sau khi khách đã cọc
- Xác nhận khách đến nhận sân (`ĐANG SỬ DỤNG`)
- Hủy đơn đặt sân

**Vòng đời trạng thái:**
```
ĐÃ ĐẶT CHỜ CỌC → ĐÃ CỌC → ĐÃ XÁC NHẬN → ĐANG SỬ DỤNG → ĐÃ HOÀN THÀNH
                                                         ↘ ĐÃ HUỶ
```

### 3.8 Quản lý hóa đơn (bill)

- Tạo hóa đơn mới (đặt trước hoặc chơi ngay)
- Xem chi tiết hóa đơn: tiền thuê sân, dịch vụ, chiết khấu
- Thêm sản phẩm / dụng cụ vào hóa đơn đang mở
- Xác nhận thanh toán cuối
- Xuất hóa đơn ra file PDF
- Áp dụng giảm giá hạng khách hàng và giảm giá thủ công

**Công thức tính tiền:**
```
TONGGIATRI   = Σ tiền thuê sân + Σ (SL × đơn giá dịch vụ)
GIAM_HANG    = Σ tiền thuê sân × chiết_khấu_hạng_KH / 100
GIAM_HOA_DON = (TONGGIATRI - GIAM_HANG) × giảm_giá_thủ_công / 100
TONGTIEN     = TONGGIATRI - GIAM_HANG - GIAM_HOA_DON - tiền_cọc
```

### 3.9 Quản lý khách hàng (customer)

- Thêm, sửa, xoá khách hàng
- Tìm kiếm theo tên, số điện thoại, email
- Xem thông tin hạng khách hàng hiện tại và tích lũy

### 3.10 Đặt sân tự phục vụ — phía khách hàng (customer_booking)

- Chọn chi nhánh → khu vực → sân → khung giờ
- Xem thông tin giá trước khi đặt
- Gửi yêu cầu đặt sân
- Thanh toán cọc qua QR VietQR (PayOS) — bắt buộc trong 5 phút
- Huỷ đặt sân (hoàn 100% cọc nếu trước 2 ngày)

### 3.11 Lịch sử đặt sân (customer_history)

- Xem toàn bộ lịch sử đặt sân của khách hàng đang đăng nhập
- Xem chi tiết từng lần đặt

### 3.12 Quản lý hạng khách hàng (customer_rank)

- Thêm, sửa, xoá hạng khách hàng (Đồng, Bạc, Vàng, Kim Cương,...)
- Thiết lập mức tích lũy và phần trăm chiết khấu
- Hạng tự động cập nhật qua trigger DB khi doanh thu KH thay đổi

### 3.13 Quản lý nhân viên (staff)

- Thêm, sửa, xoá nhân viên và gắn vào chi nhánh
- Phân loại theo chức danh (staff_type)
- Tìm kiếm, lọc theo chi nhánh

### 3.14 Quản lý loại nhân viên (staff_type)

- Thêm, sửa, xoá chức danh nhân viên

### 3.15 Quản lý sản phẩm (product)

- Thêm, sửa, xoá sản phẩm bán kèm (đồ ăn, nước uống,...)
- Quản lý tồn kho — trừ khi xuất, không hoàn khi trả

### 3.16 Quản lý dụng cụ thể thao (equipment)

- Thêm, sửa, xoá dụng cụ cho thuê (vợt, bóng,...)
- Quản lý tồn kho — trừ khi cho thuê, **hoàn lại** khi khách trả

### 3.17 Quản lý nhập hàng (imports)

- Tạo phiếu nhập kho (sản phẩm hoặc dụng cụ)
- Cập nhật số lượng tồn kho tự động sau khi nhập
- Chỉ Branch Manager và Owner được thực hiện

### 3.18 Quản lý nhà cung cấp (supplier)

- Thêm, sửa, xoá nhà cung cấp
- Liên kết với phiếu nhập hàng

### 3.19 Quản lý loại thể thao (sport_type)

- Quản lý danh mục loại thể thao (Bóng đá, Cầu lông, Pickleball, Bóng bàn, Tennis,...)
- Gắn vào khu vực để phân loại sân

### 3.20 Quản lý doanh thu (revenue)

- Tra cứu doanh thu theo chi nhánh, ngày, khoảng thời gian
- Biểu đồ thống kê bằng JFreeChart
- Xuất báo cáo (download)
- Dữ liệu lấy từ bảng `DOANH_THU`, không lấy từ `KHACH_HANG.DOANH_THU`

### 3.21 Quản lý tài khoản (account)

- Thêm, sửa, xoá tài khoản người dùng — chỉ Owner
- Gán nhóm quyền cho tài khoản
- Khoá / mở khóa tài khoản

### 3.22 Phân quyền tài khoản (role_permission)

- Gán / thu hồi nhóm quyền — chỉ Owner
- Xem ma trận phân quyền chi tiết

### 3.23 Hồ sơ cá nhân (user_profile)

- Xem và cập nhật thông tin cá nhân
- Đổi mật khẩu sau khi đăng nhập

---

## 4. Hệ thống phân quyền

Hệ thống gồm **4 nhóm vai trò** với phạm vi dữ liệu khác nhau:

| Vai trò | GROUP_ID | Phạm vi dữ liệu |
|---|---|---|
| Chủ sân | `OWNER` | Tất cả chi nhánh |
| Quản lý chi nhánh | `BRANCH_MANAGER` | Chi nhánh được gán |
| Nhân viên thu ngân | `CASHIER` | Chi nhánh làm việc |
| Khách hàng | `CUSTOMER` | Dữ liệu của chính mình |

Mỗi chức năng (`FUNCTION_ID`) được kiểm soát qua 5 quyền hành động:

| Bit quyền | Ý nghĩa |
|---|---|
| `CAN_VIEW` | Xem danh sách / chi tiết |
| `CAN_ADD` | Thêm mới |
| `CAN_EDIT` | Chỉnh sửa |
| `CAN_DELETE` | Xoá (soft delete) |
| `CAN_DOWNLOAD` | Xuất file / tải về |

---

## 5. Yêu cầu môi trường

| Phần mềm | Phiên bản tối thiểu | Ghi chú |
|---|---|---|
| JDK | 17 | Khuyến nghị JDK 21 LTS trở lên |
| Apache Maven | 3.8+ | Dùng để build và quản lý dependency |
| Oracle Database | 21c / 23c Free | Hoặc chạy qua Docker |
| Git | Bất kỳ | Để clone repository |
| IDE | IntelliJ IDEA / VS Code | Khuyến nghị IntelliJ IDEA |

### Kiểm tra môi trường

```bash
java -version      # cần >= 17
mvn -version       # cần >= 3.8
sqlplus -V         # nếu dùng Oracle client
```

---

## 6. Hướng dẫn cài đặt

### Bước 1 — Clone repository

```bash
git clone <repository-url>
cd Sport_Court_Booking_Management
```

### Bước 2 — Cài đặt Oracle Database

**Tùy chọn A: Docker (khuyến nghị)**

```bash
docker pull container-registry.oracle.com/database/free:latest

docker run -d \
  --name oracle-sportcourt \
  -p 1521:1521 \
  -e ORACLE_PWD=YourPassword123 \
  container-registry.oracle.com/database/free:latest
```

Chờ khoảng 2-3 phút để container khởi động. Kiểm tra:

```bash
docker logs oracle-sportcourt | tail -20
# Tìm dòng: "DATABASE IS READY TO USE!"
```

**Tùy chọn B: Cài đặt Oracle Database Free**

Tải từ [oracle.com/database/free](https://www.oracle.com/database/free/) và cài đặt theo hướng dẫn.

### Bước 3 — Tạo schema và user Oracle

Kết nối với tài khoản `SYS` hoặc `SYSTEM`:

```sql
-- Tạo user mới
CREATE USER sportcourt IDENTIFIED BY YourPassword123;
GRANT CONNECT, RESOURCE, DBA TO sportcourt;
ALTER USER sportcourt QUOTA UNLIMITED ON USERS;
```

### Bước 4 — Cấu hình kết nối database

Copy file mẫu và điền thông tin:

```bash
cp src/main/resources/db/db.properties.example \
   src/main/resources/db/db.properties
```

Mở `src/main/resources/db/db.properties` và chỉnh sửa:

```properties
db.host=localhost
db.port=1521
db.service=FREEPDB1
db.username=sportcourt
db.password=YourPassword123

db.pool.maxSize=10
db.pool.connectionTimeoutMs=10000
db.pool.validationTimeoutSeconds=3
```

> **Lưu ý:** Nếu dùng Oracle 21c thì `db.service=XEPDB1`. Nếu dùng Oracle 23c Free thì `db.service=FREEPDB1`.

### Bước 5 — Cấu hình email (tính năng quên mật khẩu)

```bash
cp src/main/resources/mail/mail.properties.example \
   src/main/resources/mail/mail.properties
```

Mở `src/main/resources/mail/mail.properties`:

```properties
mail.host=smtp.gmail.com
mail.port=587
mail.username=your_email@gmail.com
mail.appPassword=xxxx_xxxx_xxxx_xxxx
mail.fromName=RentSta
```

> **Tạo App Password Gmail:** Vào [myaccount.google.com](https://myaccount.google.com) → Bảo mật → Xác minh 2 bước → Mật khẩu ứng dụng.

> Nếu bỏ qua bước này, ứng dụng vẫn chạy bình thường — chỉ tính năng quên mật khẩu sẽ không hoạt động.

### Bước 6 — Cấu hình PayOS (thanh toán cọc)

```bash
cp src/main/resources/payos/payos.properties.example \
   src/main/resources/payos/payos.properties
```

Mở `src/main/resources/payos/payos.properties`:

```properties
payos.client-id=your_client_id
payos.api-key=your_api_key
payos.checksum-key=your_checksum_key
```

> Đăng ký tài khoản tại [payos.vn](https://payos.vn) để lấy API key.

> Nếu bỏ qua, tính năng thanh toán cọc qua QR sẽ không hoạt động — nhưng các chức năng khác vẫn bình thường.

---

## 7. Cấu hình hệ thống

### Tổng hợp các file cấu hình cần tạo

| File cần tạo | Tạo từ file mẫu | Bắt buộc |
|---|---|---|
| `src/main/resources/db/db.properties` | `db.properties.example` | **Bắt buộc** |
| `src/main/resources/mail/mail.properties` | `mail.properties.example` | Tùy chọn |
| `src/main/resources/payos/payos.properties` | `payos.properties.example` | Tùy chọn |

---

## 8. Thiết lập cơ sở dữ liệu

Kết nối vào schema `sportcourt` và chạy các script SQL theo đúng thứ tự sau:

### Thứ tự chạy SQL

```
1. SCHEMA.sql              ← Tạo toàn bộ 28 bảng
2. TRIGGERS.sql            ← Các trigger tự động (doanh thu, hạng KH, số sân)
3. PROCEDURES_FUNCTIONS.sql ← Stored procedures và functions
4. SEED.SQL                ← Dữ liệu mẫu ban đầu (tài khoản, chi nhánh, sản phẩm,...)
5. SEED_DOANH_THU.sql      ← Dữ liệu mẫu báo cáo doanh thu
```

Vị trí file: `src/main/resources/db/`

### Chạy qua SQL*Plus

```bash
sqlplus sportcourt/YourPassword123@localhost:1521/FREEPDB1

SQL> @src/main/resources/db/SCHEMA.sql
SQL> @src/main/resources/db/TRIGGERS.sql
SQL> @src/main/resources/db/PROCEDURES_FUNCTIONS.sql
SQL> @src/main/resources/db/SEED.SQL
SQL> @src/main/resources/db/SEED_DOANH_THU.sql
```

### Chạy qua SQL Developer / DBeaver

Mở từng file theo thứ tự trên và nhấn **Run Script** (F5 trong SQL Developer).

### Kiểm tra sau khi chạy

```sql
-- Kiểm tra số bảng đã tạo (phải có 28 bảng)
SELECT COUNT(*) FROM USER_TABLES;

-- Kiểm tra có dữ liệu mẫu
SELECT COUNT(*) FROM ACCOUNT;
SELECT COUNT(*) FROM CHI_NHANH;
```

---

## 9. Build và chạy chương trình

### Build với Maven

```bash
# Tải dependencies và build
mvn clean package -DskipTests

# File JAR sẽ nằm ở:
# target/App-1.0-SNAPSHOT.jar
```

### Chạy chương trình

**Cách 1 — Chạy trực tiếp qua Maven:**

```bash
mvn exec:java -Dexec.mainClass=com.sportcourt.App
```

**Cách 2 — Chạy file JAR:**

```bash
java -jar target/App-1.0-SNAPSHOT.jar
```

**Cách 3 — Chạy từ IDE:**

Mở class `src/main/java/com/sportcourt/App.java` và nhấn Run.

### Xác nhận khởi động thành công

Cửa sổ **màn hình đăng nhập** sẽ hiện ra. Nếu không thấy cửa sổ mà chỉ thấy lỗi, hãy xem phần [Xử lý sự cố](#11-xử-lý-sự-cố-thường-gặp).

---

## 10. Tài khoản mặc định

Sau khi chạy `SEED.SQL`, hệ thống có sẵn các tài khoản mẫu sau:

| Vai trò | Username | Password | Ghi chú |
|---|---|---|---|
| Chủ sân (Owner) | `owner` | `123456` | Toàn quyền hệ thống |
| Quản lý chi nhánh | `manager1` | `123456` | Quản lý chi nhánh 1 |
| Nhân viên thu ngân | `cashier1` | `123456` | Chi nhánh 1 |
| Khách hàng | `customer1` | `123456` | Tài khoản khách hàng |

> Mật khẩu chính xác phụ thuộc vào dữ liệu trong `SEED.SQL`. Kiểm tra file để xác nhận.

---

## 11. Xử lý sự cố thường gặp

### Lỗi: `Cannot find db.properties`

```
ExceptionInInitializerError: Cannot find db.properties in src/main/resources
```

**Nguyên nhân:** Chưa tạo file `db.properties`.

**Giải pháp:**
```bash
cp src/main/resources/db/db.properties.example \
   src/main/resources/db/db.properties
```

---

### Lỗi: `ORA-01017: invalid username/password`

**Nguyên nhân:** Sai username hoặc password trong `db.properties`.

**Giải pháp:** Kiểm tra lại `db.username` và `db.password`, đảm bảo khớp với user Oracle đã tạo.

---

### Lỗi: `IO Error: The Network Adapter could not establish the connection`

**Nguyên nhân:** Oracle Database chưa chạy hoặc sai `db.host`/`db.port`.

**Giải pháp:**
```bash
# Kiểm tra Docker container
docker ps | grep oracle

# Khởi động lại nếu cần
docker start oracle-sportcourt
```

---

### Lỗi: `ORA-12514: TNS:listener does not currently know of service`

**Nguyên nhân:** Sai `db.service` trong `db.properties`.

**Giải pháp:** Kiểm tra tên service đúng:
```bash
# Trong Docker container
docker exec oracle-sportcourt bash -c "lsnrctl status"
```
Thường là `FREEPDB1` (Oracle 23c Free) hoặc `XEPDB1` (Oracle 21c XE).

---

### Lỗi: `java.lang.UnsupportedClassVersionError`

**Nguyên nhân:** JDK đang dùng quá cũ.

**Giải pháp:** Nâng cấp JDK lên phiên bản 17 trở lên:
```bash
java -version   # phải >= 17
```

---

### Giao diện bị lỗi font hoặc hiển thị không đúng

**Nguyên nhân:** FlatLaf hoặc font chưa được load đúng.

**Giải pháp:** Đảm bảo đã chạy `mvn clean package` để tải đầy đủ dependencies trước khi chạy.

---

### Email OTP không gửi được

**Nguyên nhân:** Chưa cấu hình đúng App Password Gmail, hoặc tài khoản Gmail chưa bật xác minh 2 bước.

**Giải pháp:**
1. Vào [myaccount.google.com](https://myaccount.google.com)
2. Bảo mật → Xác minh 2 bước → Bật
3. Mật khẩu ứng dụng → Tạo mật khẩu mới → Dán vào `mail.appPassword`

---

## Cấu trúc thư mục đầy đủ

```
Sport_Court_Booking_Management/
├── pom.xml                                 ← Maven build config
├── README.md
├── HUONG_DAN_CAI_DAT.md                    ← File này
├── CONTRIBUTING.md
├── src/
│   └── main/
│       ├── java/com/sportcourt/
│       │   ├── App.java                    ← Entry point
│       │   ├── common/                     ← DB, style, UI dùng chung
│       │   └── modules/                    ← 22 module nghiệp vụ
│       └── resources/
│           ├── db/
│           │   ├── SCHEMA.sql
│           │   ├── TRIGGERS.sql
│           │   ├── PROCEDURES_FUNCTIONS.sql
│           │   ├── SEED.SQL
│           │   ├── SEED_DOANH_THU.sql
│           │   └── db.properties.example   ← Tạo db.properties từ đây
│           ├── mail/
│           │   └── mail.properties.example ← Tạo mail.properties từ đây
│           ├── payos/
│           │   └── payos.properties.example← Tạo payos.properties từ đây
│           ├── font/                       ← Lexend fonts
│           ├── icon/                       ← Icons UI
│           └── image/                      ← Ảnh nền, hình minh họa
└── docs/
    └── QUALITY_MANAGEMENT.md
```

---

*Nếu gặp vấn đề không có trong tài liệu, liên hệ email: levanduy3122006@gmail.com.*
