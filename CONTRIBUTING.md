
# Contributing Guide

Tài liệu này mô tả cách làm việc chung của nhóm khi phát triển project **Sport Court Booking Management**.

---

## 1. Mục tiêu

Mục tiêu của quy tắc làm việc là:

- Giảm conflict khi làm việc nhóm
- Giữ code dễ đọc, dễ review, dễ merge
- Thống nhất cách đặt branch, commit và pull request
- Giữ cấu trúc project nhất quán giữa các thành viên

---

## 2. Branch strategy

### Branch chính

- `main`  
  Nhánh ổn định dùng để demo, release hoặc nộp bài

- `develop`  
  Nhánh tích hợp code chung của nhóm

### Branch làm việc

Mỗi task nên tạo branch riêng từ `develop` theo quy tắc:

- `feature/<ten-chuc-nang>`
- `fix/<ten-bug>`
- `docs/<noi-dung>`
- `chore/<cau-hinh-hoac-cong-cu>`
- `refactor/<noi-dung-refactor>`

### Ví dụ

```text
feature/auth-login
feature/booking-online
feature/invoice-payment
fix/booking-conflict
docs/update-readme
chore/logger-config
```

## 6. Quy tắc làm việc nhóm
### 6.1. Quy tắc chung
− Tôn trọng và lắng nghe ý kiến của nhau để có thể đưa ra quyết định (hợp lý) nhất. 
− Tham gia đầy đủ và đảm bảo nắm được nội dung các buổi họp, thảo luận của nhóm. Nếu  vắng phải có lý do chính đáng và đọc lại biên bản cuộc họp
− Trong quá trình làm việc đòi hỏi cần sự tập trung cao độ, không làm việc riêng và đóng  góp ý kiến cá nhân để công việc đạt hiệu suất cao.
− Phân công công việc đảm bảo sự công bằng, hợp lý với tất cả thành viên trong nhóm. 
− Thường xuyên phản hồi và kiểm tra các tin nhắn công việc trong group chat. 
− Đảm bảo tiến độ làm việc và hiệu quả công việc; nhắc nhở, đôn đốc lẫn nhau để kịp thời  hạn đã giao.
− Tạo môi trường làm việc lành mạnh, vui vẻ, hoà đồng, công bằng giữa mọi người. 
− Tránh lạm dụng quyền lực, phân chia công việc không hợp lý gây mất đoàn kết làm giảm  hiệu quả trong công việc và bất mãn với các thành viên của nhóm.
− Không để lý do cá nhân ảnh hưởng đến lợi ích của nhóm, tiến độ làm việc của mọi người. 
− Chia sẻ cởi mở suy nghĩ và kinh nghiệm học tập, làm việc để giúp đỡ nhau phát triển.

### 2. Kế hoạch họp bàn công việc:
− Tần suất gặp mặt hàng tuần: Mỗi tuần 1-2 lần.
− Các thành viên chú ý theo dõi thông báo và trao đổi thông tin qua nhóm Messenger.
− Khi nhận được tin nhắn thông báo, các thành viên phải phản hồi sớm nhất có thể để  không làm ảnh hưởng đến tiến độ công việc chung. Nếu có việc gấp hay bận dài hạn không thể  kiểm tra tin nhắn được phải thông báo để mọi người biết.
− Nhóm trưởng lên kế hoạch và liệt kê các việc cần làm khi có nhiệm vụ. Sau đó thông báo  một cuộc họp cho các thành viên để phân chia và thảo luận về nhiệm vụ đó. 
− Thời gian: thông báo họp đầu tuần hoặc trước tối thiểu 2 ngày để các thành viên chuẩn bị.

### 3. Hình thức họp nhóm:
− **Họp online** để tránh mất thời gian di chuyển cũng như tạo điều kiện thoải mái để mọi  người đều có thể tham gia.
− Có thể **họp offline** ngay sau buổi học để tạo điều kiện gặp mặt đông đủ. 
− **Địa điểm:**
  + **Họp online** thông qua một nhóm cố định trên Discord do nhóm trưởng tạo ra, lưu lại biên bản họp để các thành viên dễ dàng nắm rõ nội dung. 
  + **Họp offline** tùy theo tình hình cụ thể và sự thống nhất của các thành viên, thuận  tiện cho việc đi lại và trao đổi (thư viện,...).
− Tất cả các file liên quan đến công việc được lưu trong Discord nơi diễn ra các cuộc họp online.
− Trước mỗi buổi họp, nhóm trưởng sẽ thông báo nội dung cuộc họp và một số yêu cầu cho  các thành viên chuẩn bị để cuộc họp diễn ra nhanh chóng hơn.
− Nhóm trưởng nắm số điện thoại của các thành viên để liên hệ trong trường hợp gấp.

### 4. Quy tắc thưởng, phạt:
# 4.1. Quy tắc thưởng:
− Hoàn thành tốt mọi công việc được giao sẽ được ghi nhận và ưu tiên cộng điểm khi đánh  giá kết quả.
− Giúp đỡ, hỗ trợ đóng góp ý kiến cho thành viên khác hoàn thành công việc sẽ được 1  điểm cộng khi đánh giá kết quả.
− Có ý tưởng tốt, hỗ trợ xuất sắc cho kết quả của nhóm, giúp nhóm hoàn thiện tốt sẽ được 2  điểm cộng khi đánh giá kết quả.

# 4.2. Các quy tắc phạt:
− Trễ họp quá 10 phút thì sẽ cảnh cáo lần 1, 3 lần sẽ trừ 1 điểm vào kết quả đánh giá. − Vi phạm nguyên tắc làm việc nhóm đã đề ra sẽ trừ 1 điểm vào kết quả đánh giá. 

# 5. Đánh giá thành viên:
− **90 - 100%**: Hoàn thành rất tốt công việc, có sự chủ động và sáng tạo trong quá trình làm  việc, có tinh thần trách nhiệm cao, biết giúp đỡ những thành viên khác, xây dựng môi trường  làm việc tốt.
− **80 - 90%**: Hoàn thành tốt công việc, nộp bài đúng hạn. Tích cực trong đóng góp xây  dựng nhóm, có tinh thần trách nhiệm. Không vi phạm nguyên tắc nhóm.
− **50 - 80%**: Hoàn thành công việc, vi phạm từ 1 – 3 nguyên tắc làm việc nhóm.
− **15 - 50%**: Chưa hoàn thành công việc, vi phạm trên 3 nguyên tắc làm việc nhóm.
− **0 - 15%**: Chưa hoàn thành công việc, thường xuyên vi phạm nguyên tắc làm việc nhóm. Hợp đồng thành lập nhóm đã được thông qua và ký kết.

## Quality Management

The project follows a Definition of Done and GitHub Pull Request review process documented in:

[Quality Management](docs/QUALITY_MANAGEMENT.md)
