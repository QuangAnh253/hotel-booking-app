# Bài tập lớn: App quản lý & Đặt phòng khách sạn
### Học phần: Lập trình thiết bị di động

Ứng dụng hỗ trợ đặt phòng khách sạn, xem tin tức du lịch và trải nghiệm các dịch vụ nghỉ dưỡng cao cấp. Hệ thống bao gồm hai phân hệ chính: **Khách hàng (Guest)** và **Quản trị viên (Admin)**.

---

## 🚀 Tính năng chính

### 👤 Phân hệ Khách hàng (Guest)
* **Đăng ký & Xác thực**: Tích hợp mã OTP qua Email (EmailJS).
* **Tìm kiếm thông minh**: Lọc phòng theo loại, giá và kiểm tra phòng trống theo ngày thực tế.
* **Đặt phòng & Lịch sử**: Đặt phòng nhanh, theo dõi trạng thái đơn hàng.
* **Tương tác**: Nhắn tin hỗ trợ (Real-time Chat), lưu phòng yêu thích, đánh giá & Rating 5 sao.

### 🔑 Phân hệ Quản trị (Admin)
* **Quản lý tài nguyên**: CRUD (Thêm, Sửa, Xóa) Phòng, Khu vui chơi, Tin tức, Hãng hàng không.
* **Vận hành đơn**: Tiếp nhận, Duyệt hoặc Hủy đơn đặt phòng của khách.
* **Hỗ trợ khách hàng**: Chat trực tiếp với khách hàng.
* **Quản lý người dùng**: Xem danh sách, phân quyền và bộ công cụ tạo dữ liệu mẫu để Demo.

---

## 🛠 Công nghệ sử dụng
* **Ngôn ngữ**: Java (Android Native).
* **Backend**: Firebase (Authentication, Firestore, Storage).
* **API**: EmailJS (Xác thực OTP).
* **Thư viện**: Glide (Xử lý hình ảnh mượt mà).

---

## ⚠️ Lưu ý quan trọng
Để ứng dụng hoạt động, bạn cần **tự thêm file `google-services.json`** từ Firebase Console cá nhân vào thư mục `/app`. Do chính sách bảo mật, file cấu hình Firebase gốc đã được đưa vào `.gitignore`.

---
**Nhóm thực hiện**: Nhóm 4
**Leader**: Quang Anh
