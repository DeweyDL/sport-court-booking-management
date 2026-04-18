# Sport Court Booking Management

Hệ thống quản lý và đặt lịch thuê sân thể thao cho mô hình nhiều chi nhánh.

Dự án hỗ trợ quản lý đặt sân, hóa đơn, thanh toán, khách hàng, nhân viên, sản phẩm, dụng cụ thể thao và báo cáo doanh thu.

---

## 1. Mục tiêu dự án

Dự án được xây dựng cho đồ án môn học với mục tiêu:

- Quản lý hoạt động của chuỗi sân thể thao nhiều chi nhánh
- Hỗ trợ khách hàng đặt sân online và tại quầy
- Quản lý hóa đơn, tiền cọc và thanh toán
- Quản lý chi nhánh, khu vực, sân con và bảng giá
- Quản lý khách hàng, nhân viên, sản phẩm, dụng cụ và nhập hàng
- Hỗ trợ tra cứu và lập báo cáo doanh thu

---

## 2. Các actor chính

- **Khách hàng**
- **Nhân viên thu ngân**
- **Quản lý chi nhánh**
- **Chủ sân**

Tất cả người dùng trong hệ thống đều sử dụng tài khoản và được phân quyền theo vai trò.

---

## 3. Chức năng chính

### Quản lý tài khoản
- Đăng ký
- Đăng nhập / đăng xuất
- Quên mật khẩu
- Đổi mật khẩu
- Cập nhật thông tin tài khoản

### Quản lý đặt sân
- Đặt sân online
- Hỗ trợ khách đặt sân tại quầy
- Xác nhận lịch đặt sân
- Hủy đặt sân
- Tra cứu lịch sử đặt sân

### Quản lý hóa đơn và thanh toán
- Tạo hóa đơn
- Thanh toán cọc
- Xác nhận thanh toán hóa đơn
- Ghi nhận dịch vụ phát sinh

### Quản lý vận hành
- Quản lý chi nhánh
- Quản lý khu vực
- Quản lý sân con
- Quản lý bảng giá
- Quản lý khách hàng
- Quản lý nhân viên
- Quản lý sản phẩm
- Quản lý dụng cụ thể thao
- Quản lý nhập hàng

### Báo cáo và thống kê
- Tra cứu doanh thu
- Lập báo cáo doanh thu
- Xuất báo cáo

---

## 4. Công nghệ sử dụng

- **Java 25**
- **Maven**
- **Java Swing**
- **JDBC**
- **Oracle Database**
- **Docker**
- **SLF4J + Logback**
- **JUnit 5**

Các công cụ hỗ trợ chất lượng code có thể dùng thêm:

- Checkstyle
- Spotless
- SpotBugs
- SonarLint / SonarQube

---

## 5. Kiến trúc dự án

Dự án tổ chức theo hướng **MVC kết hợp module nghiệp vụ**.

### Luồng xử lý chính

```text
View -> Controller -> Service -> Repository/DAO -> Database