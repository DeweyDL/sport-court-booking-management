
## `CONTRIBUTING.md`

```md
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