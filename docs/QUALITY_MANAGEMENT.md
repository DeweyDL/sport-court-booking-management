# Quản lý Chất lượng (Quality Management)

## 1. Mục đích

Tài liệu này mô tả quy trình quản lý chất lượng cho project **Sport Court Booking Management**.

Mục đích của tài liệu là đảm bảo mỗi chức năng, lỗi cần sửa hoặc thay đổi giao diện đều được phát triển, kiểm tra, review và merge theo một quy trình thống nhất.

Nhóm sử dụng GitHub branch và Pull Request để kiểm soát chất lượng source code trước khi tích hợp thay đổi vào nhánh phát triển chính.

---

## 2. Definition of Done (DoD)

Một task, chức năng hoặc lỗi sửa chỉ được xem là **hoàn thành** khi đáp ứng đầy đủ các tiêu chí bên dưới.

### 2.1 Hoàn thành chức năng

- Chức năng đã được phát triển đúng theo yêu cầu và phạm vi đã xác định.
- Chức năng chạy ổn định trên môi trường local.
- Logic xử lý phù hợp với nghiệp vụ của hệ thống.
- Chức năng mới không làm ảnh hưởng hoặc gây lỗi cho các module đã hoàn thành trước đó.

### 2.2 Chất lượng code

- Code tuân theo cấu trúc hiện tại của project.
- Cách đặt tên class, method, biến và branch rõ ràng, dễ hiểu.
- Source code được tổ chức theo các tầng phù hợp, ví dụ:
  - DAO
  - Service
  - Controller
  - View
  - Model
- Code dễ đọc, dễ bảo trì và hạn chế lặp lại logic không cần thiết.
- Các câu truy vấn database được kiểm tra cẩn thận, đặc biệt là:
  - Tên bảng
  - Alias của bảng
  - Các cột được select
  - Điều kiện search/filter
  - Điều kiện update/delete
- Có xử lý exception khi cần thiết, đặc biệt ở các phần liên quan đến database.

### 2.3 Chất lượng giao diện

- Giao diện phải đồng bộ với style chung của hệ thống.
- Các màn hình quản lý nên thống nhất về bố cục và cách hiển thị, bao gồm:
  - Thanh tìm kiếm
  - Khu vực sắp xếp/lọc dữ liệu
  - Bảng dữ liệu
  - Các nút thao tác
  - Dialog thêm/sửa
  - Khoảng cách, padding và kích thước thành phần giao diện
- Giao diện cần được kiểm tra ở nhiều kích thước cửa sổ khác nhau để tránh lỗi layout.
- Button, input, dialog và table cần rõ ràng, dễ sử dụng.
- Các màn hình mới nên đồng bộ với các màn hình đã hoàn thiện trước đó như Customer, Court, Role hoặc Sport Type.

### 2.4 Kiểm thử

Trước khi tạo Pull Request, người phát triển cần kiểm tra chức năng trên môi trường local.

Các nội dung cần kiểm tra gồm:

- Ứng dụng khởi động thành công.
- Màn hình hoặc chức năng liên quan có thể mở được mà không phát sinh lỗi.
- Các thao tác thêm, xem, sửa, xóa và tìm kiếm hoạt động đúng nếu chức năng có hỗ trợ.
- Dữ liệu hiển thị đúng trên table và form.
- Không xuất hiện lỗi giao diện rõ ràng sau khi thay đổi.
- Các chức năng liên quan vẫn hoạt động bình thường sau khi cập nhật.

### 2.5 Yêu cầu về Git và Pull Request

- Code phải được phát triển trên branch riêng.
- Tên branch cần thể hiện rõ mục đích của công việc.

Ví dụ:

```text
feature/court
feature/role
feature/sport_type
fix/sidebar
fix/seed
fix/all_view
fix/views
```

- Phải tạo Pull Request trước khi merge vào branch tích hợp.
- Pull Request cần có tiêu đề rõ ràng.
- Nếu thay đổi lớn, Pull Request nên có mô tả ngắn gọn về nội dung đã thực hiện.
- Pull Request chỉ được merge khi chức năng hoặc lỗi sửa đã được kiểm tra.

### 2.6 Tích hợp

- Các chức năng và bug fix sau khi hoàn thành sẽ được merge vào branch `develop`.
- Branch `develop` được dùng làm nhánh tích hợp chính trong quá trình phát triển.
- Sau khi merge, nhóm cần kiểm tra lại hệ thống để đảm bảo thay đổi mới hoạt động ổn định cùng các module khác.
- Nếu phát hiện lỗi sau khi merge, nhóm sẽ tạo branch `fix/...` để sửa lỗi và tiếp tục tạo Pull Request mới.

---

## 3. Quy trình Code Review trên GitHub

Project sử dụng GitHub Pull Request để quản lý quá trình review code và kiểm soát chất lượng.

### Bước 1: Tạo branch làm việc

Mỗi chức năng hoặc lỗi cần sửa nên được phát triển trên một branch riêng.

Ví dụ tên branch:

```text
feature/court
feature/role
feature/sport_type
fix/sidebar
fix/seed
fix/all_view
fix/views
```

Cách làm này giúp nhóm dễ theo dõi mục đích của từng thay đổi và quản lý tiến độ phát triển rõ ràng hơn.

### Bước 2: Phát triển chức năng hoặc sửa lỗi

Người phát triển thực hiện chức năng hoặc sửa lỗi trên branch đã tạo.

Trong quá trình phát triển, cần chú ý:

- Tuân theo cấu trúc source code hiện tại.
- Giữ code rõ ràng, dễ đọc và dễ bảo trì.
- Kiểm tra kỹ các câu truy vấn database.
- Đảm bảo giao diện đồng bộ với style hiện tại của project.
- Kiểm tra chức năng trên local trước khi tạo Pull Request.

### Bước 3: Commit và push code

Sau khi hoàn thành, người phát triển commit và push code lên GitHub.

Commit message nên mô tả ngắn gọn nội dung thay đổi.

Ví dụ:

```text
feat: add sport type management screen
fix: update sidebar navigation
fix: correct seed data
fix: improve all management views
```

### Bước 4: Tạo Pull Request

Người phát triển tạo Pull Request từ branch làm việc vào branch `develop`.

Pull Request nên có:

- Tiêu đề rõ ràng.
- Mô tả ngắn gọn nếu thay đổi có nhiều nội dung.
- Ghi chú về lỗi đã sửa hoặc các thay đổi giao diện quan trọng nếu cần.

### Bước 5: Review Pull Request

Trước khi merge, nhóm kiểm tra Pull Request dựa trên các tiêu chí sau:

- Chức năng hoạt động đúng yêu cầu.
- Code tuân theo cấu trúc project.
- Giao diện đồng bộ với các màn hình hiện có.
- Thay đổi không gây lỗi cho các module khác.
- Câu truy vấn database chính xác.
- Tên class, method, biến và branch rõ ràng.
- Không còn phần code thừa, lỗi rõ ràng hoặc chức năng chưa hoàn thiện.

### Bước 6: Merge vào branch `develop`

Nếu Pull Request đáp ứng Definition of Done, Pull Request có thể được merge vào branch `develop`.

Branch `develop` được dùng để tích hợp các chức năng và lỗi sửa đã hoàn thành trước khi đưa lên branch chính.

### Bước 7: Kiểm tra sau khi merge

Sau khi merge, nhóm kiểm tra lại ứng dụng từ branch `develop`.

Nếu phát sinh lỗi mới, nhóm tạo branch `fix/...` và lặp lại quy trình Pull Request.

---

## 4. Tình trạng chất lượng hiện tại của project

Ở giai đoạn hiện tại, project đã áp dụng quy trình làm việc theo branch và Pull Request trên GitHub.

Một số chức năng và lỗi sửa đã được merge thành công gồm:

| Pull Request | Branch | Mục đích | Branch đích | Trạng thái |
| --- | --- | --- | --- | --- |
| Feature/court | `feature/court` | Phát triển chức năng quản lý sân | `develop` | Đã merge |
| sync view with others view | `fix/court/view` | Đồng bộ giao diện quản lý sân với các màn hình khác | `develop` | Đã merge |
| Feature/role | `feature/role` | Phát triển chức năng quản lý vai trò | `develop` | Đã merge |
| fix sidebar | `fix/sidebar` | Sửa lỗi giao diện hoặc điều hướng sidebar | `develop` | Đã merge |
| Fix/seed | `fix/seed` | Sửa lỗi dữ liệu seed | `develop` | Đã merge |
| feat(sport_type): add view layer matching customer/view UI style | `feature/sport_type` | Thêm giao diện và controller cho quản lý loại thể thao | `develop` | Đã merge |
| Fix/all view | `fix/all_view` | Sửa và cải thiện nhiều màn hình view | `develop` | Đã merge |
| Fix/views | `fix/views` | Tiếp tục sửa và hoàn thiện các màn hình giao diện | `develop` | Đã merge |
| Revert "backend cost" | `revert-46-feature/cost` | Revert thay đổi chưa phù hợp để giữ ổn định cho branch develop | `develop` | Đã merge |

Các Pull Request trên cho thấy nhóm đang sử dụng GitHub để quản lý tiến độ phát triển và kiểm soát việc tích hợp code.

Quy trình chất lượng hiện tại giúp nhóm:

- Theo dõi rõ các chức năng đã hoàn thành.
- Tách riêng chức năng mới và lỗi sửa bằng các branch khác nhau.
- Kiểm tra thay đổi trước khi merge.
- Giữ branch `develop` ổn định hơn.
- Duy trì sự thống nhất giữa các màn hình quản lý.
- Giảm rủi ro lỗi khi tích hợp nhiều module.

---

## 5. Checklist kiểm tra chất lượng

Trước khi Pull Request được merge, nhóm nên kiểm tra checklist sau:

```text
[ ] Chức năng hoặc lỗi sửa đã hoàn thành đúng yêu cầu.
[ ] Ứng dụng chạy thành công trên môi trường local.
[ ] Màn hình liên quan hoạt động đúng.
[ ] Các chức năng thêm, xem, sửa, xóa, tìm kiếm đã được kiểm tra nếu có.
[ ] Giao diện đồng bộ với các màn hình hiện có.
[ ] Code tuân theo cấu trúc project.
[ ] Câu truy vấn database đã được kiểm tra.
[ ] Không thay đổi các file không liên quan.
[ ] Pull Request được tạo vào đúng branch đích.
[ ] Thay đổi không làm hỏng chức năng đã có.
```

---

## 6. Kết luận

Project **Sport Court Booking Management** sử dụng GitHub branch, Pull Request và Definition of Done để quản lý chất lượng phần mềm.

Thông qua quy trình này, nhóm có thể đảm bảo mỗi chức năng được phát triển, kiểm tra, review và tích hợp một cách có kiểm soát. Điều này giúp nâng cao chất lượng code, giảm lỗi phát sinh và giữ project ổn định trong quá trình phát triển.
