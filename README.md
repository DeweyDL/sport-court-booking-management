<div align="center">

# 🏸 Sport Court Booking Management

[🇻🇳 Tiếng Việt](#-phiên-bản-tiếng-việt) &nbsp;|&nbsp; [🇬🇧 English](#-english-version)

</div>

---

# 🇻🇳 Phiên bản Tiếng Việt

> Tài liệu hướng dẫn cài đặt và giới thiệu tổng quan hệ thống

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

Chờ khoảng 2–3 phút để container khởi động. Kiểm tra:

```bash
docker logs oracle-sportcourt | tail -20
# Tìm dòng: "DATABASE IS READY TO USE!"
```

**Tùy chọn B: Cài đặt Oracle Database Free**

Tải từ [oracle.com/database/free](https://www.oracle.com/database/free/) và cài đặt theo hướng dẫn.

### Bước 3 — Tạo schema và user Oracle

Kết nối với tài khoản `SYS` hoặc `SYSTEM`:

```sql
CREATE USER sportcourt IDENTIFIED BY YourPassword123;
GRANT CONNECT, RESOURCE, DBA TO sportcourt;
ALTER USER sportcourt QUOTA UNLIMITED ON USERS;
```

### Bước 4 — Cấu hình kết nối database

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

> **Lưu ý:** Oracle 21c dùng `db.service=XEPDB1`. Oracle 23c Free dùng `db.service=FREEPDB1`.

### Bước 5 — Cấu hình email (tính năng quên mật khẩu)

```bash
cp src/main/resources/mail/mail.properties.example \
   src/main/resources/mail/mail.properties
```

```properties
mail.host=smtp.gmail.com
mail.port=587
mail.username=your_email@gmail.com
mail.appPassword=xxxx_xxxx_xxxx_xxxx
mail.fromName=RentSta
```

> **Tạo App Password Gmail:** [myaccount.google.com](https://myaccount.google.com) → Bảo mật → Xác minh 2 bước → Mật khẩu ứng dụng.

> Nếu bỏ qua, ứng dụng vẫn chạy bình thường — chỉ tính năng quên mật khẩu sẽ không hoạt động.

### Bước 6 — Cấu hình PayOS (thanh toán cọc)

```bash
cp src/main/resources/payos/payos.properties.example \
   src/main/resources/payos/payos.properties
```

```properties
payos.client-id=your_client_id
payos.api-key=your_api_key
payos.checksum-key=your_checksum_key
```

> Đăng ký tại [payos.vn](https://payos.vn) để lấy API key. Nếu bỏ qua, các chức năng khác vẫn hoạt động bình thường.

---

## 7. Cấu hình hệ thống

| File cần tạo | Tạo từ file mẫu | Bắt buộc |
|---|---|---|
| `src/main/resources/db/db.properties` | `db.properties.example` | **Bắt buộc** |
| `src/main/resources/mail/mail.properties` | `mail.properties.example` | Tùy chọn |
| `src/main/resources/payos/payos.properties` | `payos.properties.example` | Tùy chọn |

---

## 8. Thiết lập cơ sở dữ liệu

Kết nối vào schema `sportcourt` và chạy các script SQL theo đúng thứ tự:

```
1. SCHEMA.sql               ← Tạo toàn bộ 28 bảng
2. TRIGGERS.sql             ← Các trigger tự động
3. PROCEDURES_FUNCTIONS.sql ← Stored procedures và functions
4. SEED.SQL                 ← Dữ liệu mẫu ban đầu
5. SEED_DOANH_THU.sql       ← Dữ liệu mẫu báo cáo doanh thu
```

Vị trí file: `src/main/resources/db/`

### Chạy qua SQL\*Plus

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
SELECT COUNT(*) FROM USER_TABLES;  -- phải có 28 bảng
SELECT COUNT(*) FROM ACCOUNT;
SELECT COUNT(*) FROM CHI_NHANH;
```

---

## 9. Build và chạy chương trình

```bash
mvn clean package -DskipTests
# Output: target/App-1.0-SNAPSHOT.jar
```

**Cách 1 — Maven:**
```bash
mvn exec:java -Dexec.mainClass=com.sportcourt.App
```

**Cách 2 — JAR:**
```bash
java -jar target/App-1.0-SNAPSHOT.jar
```

**Cách 3 — IDE (Khuyến nghị IntelliJ IDEA):** Mở `src/main/java/com/sportcourt/App.java` và nhấn Run.

Cửa sổ **màn hình đăng nhập** sẽ hiện ra khi khởi động thành công.

---

## 10. Tài khoản mặc định

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

**Nguyên nhân:** Chưa tạo file `db.properties`.

```bash
cp src/main/resources/db/db.properties.example \
   src/main/resources/db/db.properties
```

### Lỗi: `ORA-01017: invalid username/password`

**Nguyên nhân:** Sai username hoặc password trong `db.properties`.  
**Giải pháp:** Kiểm tra lại `db.username` và `db.password`.

### Lỗi: `IO Error: The Network Adapter could not establish the connection`

**Nguyên nhân:** Oracle Database chưa chạy hoặc sai host/port.

```bash
docker ps | grep oracle
docker start oracle-sportcourt
```

### Lỗi: `ORA-12514: TNS:listener does not currently know of service`

**Nguyên nhân:** Sai `db.service`.

```bash
docker exec oracle-sportcourt bash -c "lsnrctl status"
```

Thường là `FREEPDB1` (Oracle 23c Free) hoặc `XEPDB1` (Oracle 21c XE).

### Lỗi: `java.lang.UnsupportedClassVersionError`

**Nguyên nhân:** JDK quá cũ.

```bash
java -version   # phải >= 17
```

### Giao diện bị lỗi font hoặc hiển thị không đúng

**Giải pháp:** Chạy `mvn clean package` để tải đầy đủ dependencies trước khi chạy.

### Email OTP không gửi được

1. Vào [myaccount.google.com](https://myaccount.google.com)
2. Bảo mật → Xác minh 2 bước → Bật
3. Mật khẩu ứng dụng → Tạo mật khẩu mới → Dán vào `mail.appPassword`

---

## Cấu trúc thư mục đầy đủ

```
Sport_Court_Booking_Management/
├── pom.xml
├── README.md
├── CONTRIBUTING.md
├── src/
│   └── main/
│       ├── java/com/sportcourt/
│       │   ├── App.java
│       │   ├── common/
│       │   └── modules/                    ← 22 module nghiệp vụ
│       └── resources/
│           ├── db/
│           │   ├── SCHEMA.sql
│           │   ├── TRIGGERS.sql
│           │   ├── PROCEDURES_FUNCTIONS.sql
│           │   ├── SEED.SQL
│           │   ├── SEED_DOANH_THU.sql
│           │   └── db.properties.example
│           ├── mail/
│           │   └── mail.properties.example
│           ├── payos/
│           │   └── payos.properties.example
│           ├── font/
│           ├── icon/
│           └── image/
└── docs/
    └── QUALITY_MANAGEMENT.md
```

---

*Nếu gặp vấn đề không có trong tài liệu, vui lòng tạo Issue trên repository hoặc liên hệ qua email: levanduy3122006@gmail.com.*

---
---

# 🇬🇧 English Version

> Installation guide and system overview

## Table of Contents

1. [Overview](#1-overview)
2. [System Architecture](#2-system-architecture)
3. [Feature Modules](#3-feature-modules)
4. [Permission System](#4-permission-system)
5. [Environment Requirements](#5-environment-requirements)
6. [Installation Guide](#6-installation-guide)
7. [System Configuration](#7-system-configuration)
8. [Database Setup](#8-database-setup)
9. [Build and Run](#9-build-and-run)
10. [Default Accounts](#10-default-accounts)
11. [Troubleshooting](#11-troubleshooting)

---

## 1. Overview

**Sport Court Booking Management** is a desktop application for managing multi-branch sports court facilities. The system covers the full operational workflow — from court booking and invoicing to staff management and revenue reporting.

### Key Features

- **Multi-branch**: Manage multiple branches with role-scoped data access
- **Dual booking channels**: Self-booking online (customers) and counter booking (staff)
- **Integrated payments**: Deposit payments via VietQR / PayOS
- **Granular permissions**: 4 role groups across 21 functions with 5 action permission types
- **Revenue reports**: Statistics by branch, date, and sport type with JFreeChart charts
- **PDF export**: Invoice export via Apache PDFBox
- **OTP email**: Password recovery via Gmail SMTP

### Technology Stack

| Component | Technology |
|---|---|
| Language | Java 17+ (no framework) |
| UI | Java Swing + FlatLaf 3.7 |
| Fonts | Lexend, Roboto, JetBrains Mono |
| Layout | MigLayout 11.3 |
| Database | Oracle Database (JDBC ojdbc17) |
| Build tool | Apache Maven |
| Charts | JFreeChart 1.5.4 |
| PDF export | Apache PDFBox 3.0.2 |
| Email | Jakarta Mail 2.0.1 (Gmail SMTP) |
| Payments | PayOS Java SDK 1.0.3 |
| QR Code | ZXing (Google) 3.5.4 |
| Logging | SLF4J + Logback |
| Date picker | LGoodDatePicker 11.2.1 |

---

## 2. System Architecture

The project follows a **Modular MVC** pattern — each business module has its own full 5-layer stack:

```
src/main/java/com/sportcourt/
├── App.java                        ← Entry point
├── common/
│   ├── db/                         ← Connection pool, JDBC utils
│   ├── style/                      ← AppFonts, FlatLaf theme, UIScale
│   └── ui/                         ← Shared Sidebar, ContentPanel
└── modules/
    ├── auth/                       ← Authentication & authorization
    ├── dashboard/                  ← Overview dashboard
    ├── branch/                     ← Branch management
    ├── area/                       ← Area management
    ├── court/                      ← Court management
    ├── cost/                       ← Pricing management
    ├── booking_management/         ← Booking management (staff)
    ├── bill/                       ← Invoice management
    ├── customer/                   ← Customer management
    ├── customer_booking/           ← Self-service booking (customer)
    ├── customer_history/           ← Customer booking history
    ├── customer_rank/              ← Customer tier management
    ├── staff/                      ← Staff management
    ├── staff_type/                 ← Staff position types
    ├── product/                    ← Product management
    ├── equipment/                  ← Sports equipment management
    ├── imports/                    ← Stock import management
    ├── supplier/                   ← Supplier management
    ├── sport_type/                 ← Sport type management
    ├── revenue/                    ← Revenue management
    ├── account/                    ← Account management
    └── user_profile/               ← Personal profile
```

**Mandatory processing flow:**

```
View  →  Controller  →  Service  →  DAO  →  Oracle DB
```

Each module consists of:

| Layer | Responsibility |
|---|---|
| `view/` | Swing panel/dialog — UI only, no direct DB calls |
| `controller/` | Handles View events, calls Service |
| `service/` | Business logic, validation, transaction coordination |
| `dao/` | Plain SQL via PreparedStatement, no business logic |
| `model/` or `entity/` | POJO mapping 1-to-1 with DB tables |
| `dto/` | Data Transfer Objects between layers |

---

## 3. Feature Modules

### 3.1 Authentication (auth)

- Login with username and password (SHA-256)
- New customer account registration
- Forgot password — OTP verification via email
- Change password
- User session management

### 3.2 Dashboard

- Role-based overview screen:
  - **Owner/Branch Manager**: revenue stats, booking counts, JFreeChart charts
  - **Cashier**: today's pending bookings list
  - **Customer**: upcoming sessions and history

### 3.3 Branch Management (branch)

- Create, update, delete (soft delete) branches
- Search by name or address
- View all areas belonging to a branch

### 3.4 Area Management (area)

- Create, update, delete sport areas within a branch
- Assign sport types to areas
- Auto-update court count via DB trigger

### 3.5 Court Management (court)

- Create, update, delete sub-courts within an area
- View detailed court information

### 3.6 Pricing Management (cost)

- Manage time slots and pricing per area
- Each slot is exactly 1 hour, no overlapping slots within the same area
- Peak hour pricing (16:00–20:00) reflected in higher rates — not hardcoded

### 3.7 Booking Management — Staff Side (booking_management)

- View list of pending booking requests
- Confirm bookings after deposit payment is received
- Check in customer upon arrival (`IN USE`)
- Cancel booking

**Booking lifecycle:**
```
PENDING DEPOSIT → DEPOSITED → CONFIRMED → IN USE → COMPLETED
                                                  ↘ CANCELLED
```

### 3.8 Invoice Management (bill)

- Create new invoices (advance booking or walk-in)
- View invoice details: court fees, services, discounts
- Add products / equipment to an open invoice
- Confirm final payment
- Export invoice as PDF
- Apply customer tier discounts and manual discounts

**Pricing formula:**
```
SUBTOTAL      = Σ court fees + Σ (qty × unit price of services)
TIER_DISCOUNT = Σ court fees × customer_tier_discount% / 100
BILL_DISCOUNT = (SUBTOTAL - TIER_DISCOUNT) × manual_discount% / 100
TOTAL         = SUBTOTAL - TIER_DISCOUNT - BILL_DISCOUNT - deposit
```

### 3.9 Customer Management (customer)

- Create, update, delete customers
- Search by name, phone number, or email
- View current customer tier and accumulated spending

### 3.10 Self-Service Booking — Customer Side (customer_booking)

- Select branch → area → court → time slot
- View pricing before confirming
- Submit booking request
- Pay deposit via VietQR QR code (PayOS) — required within 5 minutes
- Cancel booking (full deposit refund if cancelled 2+ days in advance)

### 3.11 Booking History (customer_history)

- View the full booking history of the currently logged-in customer
- View details of each booking

### 3.12 Customer Tier Management (customer_rank)

- Create, update, delete customer tiers (Bronze, Silver, Gold, Diamond, etc.)
- Configure spending thresholds and discount percentages
- Tier auto-updates via DB trigger when customer revenue changes

### 3.13 Staff Management (staff)

- Create, update, delete staff and assign to branches
- Classify by position (staff_type)
- Search and filter by branch

### 3.14 Staff Position Types (staff_type)

- Create, update, delete staff position titles

### 3.15 Product Management (product)

- Create, update, delete retail products (food, drinks, etc.)
- Inventory management — stock decremented on sale, no restock on return

### 3.16 Sports Equipment Management (equipment)

- Create, update, delete rental equipment (rackets, balls, etc.)
- Inventory management — stock decremented on rental, **restocked** on return

### 3.17 Stock Import Management (imports)

- Create stock-in records (products or equipment)
- Inventory quantities auto-updated after import
- Restricted to Branch Manager and Owner only

### 3.18 Supplier Management (supplier)

- Create, update, delete suppliers
- Link suppliers to stock import records

### 3.19 Sport Type Management (sport_type)

- Manage sport type catalog (Football, Badminton, Pickleball, Table Tennis, Tennis, etc.)
- Assign to areas for court classification

### 3.20 Revenue Management (revenue)

- Query revenue by branch, date, or time range
- Statistical charts via JFreeChart
- Export reports (download)
- Data sourced from `DOANH_THU` table, not from `KHACH_HANG.DOANH_THU`

### 3.21 Account Management (account)

- Create, update, delete user accounts — Owner only
- Assign permission groups to accounts
- Lock / unlock accounts

### 3.22 Role & Permission Management (role_permission)

- Assign / revoke permission groups — Owner only
- View the full permission matrix

### 3.23 Personal Profile (user_profile)

- View and update personal information
- Change password after login

---

## 4. Permission System

The system has **4 role groups** with different data scopes:

| Role | GROUP_ID | Data Scope |
|---|---|---|
| Owner | `OWNER` | All branches |
| Branch Manager | `BRANCH_MANAGER` | Assigned branch only |
| Cashier | `CASHIER` | Working branch only |
| Customer | `CUSTOMER` | Own data only |

Each function (`FUNCTION_ID`) is controlled by 5 action permissions:

| Permission | Meaning |
|---|---|
| `CAN_VIEW` | View list / details |
| `CAN_ADD` | Create new records |
| `CAN_EDIT` | Edit existing records |
| `CAN_DELETE` | Delete (soft delete) |
| `CAN_DOWNLOAD` | Export files / download |

---

## 5. Environment Requirements

| Software | Minimum Version | Notes |
|---|---|---|
| JDK | 17 | JDK 21 LTS or later recommended |
| Apache Maven | 3.8+ | For build and dependency management |
| Oracle Database | 21c / 23c Free | Or run via Docker |
| Git | Any | To clone the repository |
| IDE | IntelliJ IDEA / VS Code | IntelliJ IDEA recommended |

### Verify Your Environment

```bash
java -version      # must be >= 17
mvn -version       # must be >= 3.8
sqlplus -V         # if using Oracle client
```

---

## 6. Installation Guide

### Step 1 — Clone the repository

```bash
git clone <repository-url>
cd Sport_Court_Booking_Management
```

### Step 2 — Set up Oracle Database

**Option A: Docker (recommended)**

```bash
docker pull container-registry.oracle.com/database/free:latest

docker run -d \
  --name oracle-sportcourt \
  -p 1521:1521 \
  -e ORACLE_PWD=YourPassword123 \
  container-registry.oracle.com/database/free:latest
```

Wait 2–3 minutes for the container to start. Verify:

```bash
docker logs oracle-sportcourt | tail -20
# Look for: "DATABASE IS READY TO USE!"
```

**Option B: Install Oracle Database Free**

Download from [oracle.com/database/free](https://www.oracle.com/database/free/) and follow the installer.

### Step 3 — Create Oracle schema and user

Connect as `SYS` or `SYSTEM`:

```sql
CREATE USER sportcourt IDENTIFIED BY YourPassword123;
GRANT CONNECT, RESOURCE, DBA TO sportcourt;
ALTER USER sportcourt QUOTA UNLIMITED ON USERS;
```

### Step 4 — Configure database connection

```bash
cp src/main/resources/db/db.properties.example \
   src/main/resources/db/db.properties
```

Edit `src/main/resources/db/db.properties`:

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

> **Note:** Use `db.service=XEPDB1` for Oracle 21c, or `db.service=FREEPDB1` for Oracle 23c Free.

### Step 5 — Configure email (forgot password feature)

```bash
cp src/main/resources/mail/mail.properties.example \
   src/main/resources/mail/mail.properties
```

```properties
mail.host=smtp.gmail.com
mail.port=587
mail.username=your_email@gmail.com
mail.appPassword=xxxx_xxxx_xxxx_xxxx
mail.fromName=RentSta
```

> **Create a Gmail App Password:** Go to [myaccount.google.com](https://myaccount.google.com) → Security → 2-Step Verification → App Passwords.

> If skipped, the app still runs normally — only the forgot password feature will be disabled.

### Step 6 — Configure PayOS (deposit payments)

```bash
cp src/main/resources/payos/payos.properties.example \
   src/main/resources/payos/payos.properties
```

```properties
payos.client-id=your_client_id
payos.api-key=your_api_key
payos.checksum-key=your_checksum_key
```

> Register at [payos.vn](https://payos.vn) to obtain API keys. If skipped, all other features remain fully functional.

---

## 7. System Configuration

| File to create | Based on | Required |
|---|---|---|
| `src/main/resources/db/db.properties` | `db.properties.example` | **Required** |
| `src/main/resources/mail/mail.properties` | `mail.properties.example` | Optional |
| `src/main/resources/payos/payos.properties` | `payos.properties.example` | Optional |

---

## 8. Database Setup

Connect to the `sportcourt` schema and run the SQL scripts **in this exact order**:

```
1. SCHEMA.sql               ← Creates all 28 tables
2. TRIGGERS.sql             ← Automated triggers
3. PROCEDURES_FUNCTIONS.sql ← Stored procedures and functions
4. SEED.SQL                 ← Initial sample data
5. SEED_DOANH_THU.sql       ← Sample revenue report data
```

Files are located in: `src/main/resources/db/`

### Run via SQL\*Plus

```bash
sqlplus sportcourt/YourPassword123@localhost:1521/FREEPDB1

SQL> @src/main/resources/db/SCHEMA.sql
SQL> @src/main/resources/db/TRIGGERS.sql
SQL> @src/main/resources/db/PROCEDURES_FUNCTIONS.sql
SQL> @src/main/resources/db/SEED.SQL
SQL> @src/main/resources/db/SEED_DOANH_THU.sql
```

### Run via SQL Developer / DBeaver

Open each file in order and click **Run Script** (F5 in SQL Developer).

### Verify after running

```sql
SELECT COUNT(*) FROM USER_TABLES;  -- must be 28 tables
SELECT COUNT(*) FROM ACCOUNT;
SELECT COUNT(*) FROM CHI_NHANH;
```

---

## 9. Build and Run

```bash
mvn clean package -DskipTests
# Output: target/App-1.0-SNAPSHOT.jar
```

**Option 1 — Maven:**
```bash
mvn exec:java -Dexec.mainClass=com.sportcourt.App
```

**Option 2 — JAR:**
```bash
java -jar target/App-1.0-SNAPSHOT.jar
```

**Option 3 — IDE: (RecommendIntelliJ IDEA)** Open `src/main/java/com/sportcourt/App.java` and click Run.

The **login screen** will appear on successful startup. If only errors appear, refer to the [Troubleshooting](#11-troubleshooting) section.

---

## 10. Default Accounts

After running `SEED.SQL`, the following sample accounts are available:

| Role | Username | Password | Notes |
|---|---|---|---|
| Owner | `owner` | `123456` | Full system access |
| Branch Manager | `manager1` | `123456` | Manages branch 1 |
| Cashier | `cashier1` | `123456` | Branch 1 |
| Customer | `customer1` | `123456` | Customer account |

> Verify passwords against `SEED.SQL` as the actual values depend on the seed data.

---

## 11. Troubleshooting

### Error: `Cannot find db.properties`

**Cause:** The `db.properties` file has not been created.

```bash
cp src/main/resources/db/db.properties.example \
   src/main/resources/db/db.properties
```

### Error: `ORA-01017: invalid username/password`

**Cause:** Wrong username or password in `db.properties`.  
**Fix:** Verify `db.username` and `db.password` match the Oracle user you created.

### Error: `IO Error: The Network Adapter could not establish the connection`

**Cause:** Oracle Database is not running, or wrong `db.host` / `db.port`.

```bash
docker ps | grep oracle
docker start oracle-sportcourt
```

### Error: `ORA-12514: TNS:listener does not currently know of service`

**Cause:** Wrong `db.service` in `db.properties`.

```bash
docker exec oracle-sportcourt bash -c "lsnrctl status"
```

Typically `FREEPDB1` (Oracle 23c Free) or `XEPDB1` (Oracle 21c XE).

### Error: `java.lang.UnsupportedClassVersionError`

**Cause:** JDK version is too old.

```bash
java -version   # must be >= 17
```

### UI font rendering issues or incorrect display

**Fix:** Run `mvn clean package` to ensure all dependencies are downloaded before launching.

### OTP email not sending

1. Go to [myaccount.google.com](https://myaccount.google.com)
2. Security → 2-Step Verification → Enable
3. App Passwords → Create new password → Paste into `mail.appPassword`

---

## Project Structure

```
Sport_Court_Booking_Management/
├── pom.xml
├── README.md
├── CONTRIBUTING.md
├── src/
│   └── main/
│       ├── java/com/sportcourt/
│       │   ├── App.java
│       │   ├── common/
│       │   └── modules/                    ← 22 business modules
│       └── resources/
│           ├── db/
│           │   ├── SCHEMA.sql
│           │   ├── TRIGGERS.sql
│           │   ├── PROCEDURES_FUNCTIONS.sql
│           │   ├── SEED.SQL
│           │   ├── SEED_DOANH_THU.sql
│           │   └── db.properties.example
│           ├── mail/
│           │   └── mail.properties.example
│           ├── payos/
│           │   └── payos.properties.example
│           ├── font/
│           ├── icon/
│           └── image/
└── docs/
    └── QUALITY_MANAGEMENT.md
```

---

*If you encounter issues not covered in this document, please open an Issue on the repository or contact the email: levanduy3122006@gmail.com.*
