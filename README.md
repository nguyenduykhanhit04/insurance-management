# Dự Án Quản Lý Thẻ Bảo Hiểm (Bài Tập 1 - Spring Boot)

Dự án được xây dựng từ đầu (from scratch) bằng **Spring Boot 3.3.5** và **Java 17**, đáp ứng đầy đủ tất cả các yêu cầu từ file tài liệu đặc tả `Project1_QL The bao hiem.xlsx` và các quy định bổ sung trong `QA.docx`.

---

## 1. Cấu Trúc Dự Án

```
insurance-management/
├── build.gradle                                  # Cấu hình dependencies (Spring Boot, JPA, Thymeleaf, H2/MySQL, Lombok, Validation)
├── settings.gradle
├── gradlew & gradlew.bat
├── src/
│   ├── main/
│   │   ├── java/com/training/insurance/
│   │   │   ├── InsuranceApplication.java          # Main Application Entrypoint
│   │   │   ├── config/
│   │   │   │   └── WebMvcConfig.java              # Cấu hình Interceptor chặn truy cập khi chưa Login (Phụ lục No.1)
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java            # MH01: Xử lý Đăng nhập & Đăng xuất
│   │   │   │   ├── InsuranceController.java       # MH02, MH03, MH04, MH05 & Export CSV
│   │   │   │   └── CompanyApiController.java      # API Ajax tải thông tin công ty (Phụ lục No.6)
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   ├── InsuranceSearchCriteria.java  # Tiêu chí tìm kiếm, phân trang (5 bản ghi/trang), order (ASC/DESC)
│   │   │   │   │   └── InsuranceFormRequest.java    # Form Đăng ký (MH04) & Cập nhật (MH05)
│   │   │   │   └── response/
│   │   │   │       ├── InsuranceItemResponse.java   # Dữ liệu hiển thị bảng danh sách & chi tiết
│   │   │   │       └── CompanyResponse.java         # Dữ liệu trả về qua Ajax cho công ty
│   │   │   ├── entity/
│   │   │   │   ├── Company.java                   # Entity map bảng tbl_company
│   │   │   │   ├── Insurance.java                 # Entity map bảng tbl_insurance
│   │   │   │   └── User.java                      # Entity map bảng tbl_user
│   │   │   ├── exception/
│   │   │   │   ├── AppException.java              # Ngoại lệ nghiệp vụ
│   │   │   │   └── GlobalExceptionHandler.java    # Bắt lỗi toàn cục & chuyển tới trang error.html (theo Q&A)
│   │   │   ├── interceptor/
│   │   │   │   └── AuthInterceptor.java          # Bảo vệ session, chặn các route /insurance/**
│   │   │   ├── repository/
│   │   │   │   ├── CompanyRepository.java         # Truy vấn tbl_company (sort ASC theo tên công ty)
│   │   │   │   ├── InsuranceRepository.java       # Truy vấn tbl_insurance & kiểm tra trùng mã thẻ
│   │   │   │   └── UserRepository.java            # Truy vấn tbl_user & tìm kiếm động JpaSpecificationExecutor
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java & AuthServiceImpl.java
│   │   │   │   ├── CompanyService.java & CompanyServiceImpl.java
│   │   │   │   └── InsuranceService.java & InsuranceServiceImpl.java
│   │   │   └── util/
│   │   │       ├── MD5Util.java                   # Mã hóa mật khẩu MD5
│   │   │       └── NameFormatter.java             # Chuẩn hóa tên theo Phụ lục No.8
│   │   └── resources/
│   │       ├── application.yml                    # Cấu hình H2 Database / MySQL, Thymeleaf
│   │       ├── messages.properties                # Danh mục thông báo lỗi và tên trường chuẩn
│   │       ├── schema.sql                         # Khởi tạo 3 bảng tbl_company, tbl_insurance, tbl_user
│   │       ├── data.sql                           # Dữ liệu mẫu (11 bản ghi kiểm thử phân trang và tìm kiếm)
│   │       ├── static/css/common.css              # Style giao diện chuẩn, đẹp mắt
│   │       └── templates/
│   │           ├── login.html                     # Giao diện MH01 - Đăng nhập
│   │           ├── insurance-list.html            # Giao diện MH02 - Danh sách & Tìm kiếm
│   │           ├── insurance-detail.html          # Giao diện MH03 - Chi tiết & Xóa
│   │           ├── insurance-form.html            # Giao diện MH04 (Đăng ký) & MH05 (Cập nhật)
│   │           └── error.html                     # Giao diện MH Error (xử lý sự cố hệ thống)
│   └── test/
│       ├── java/com/training/insurance/
│       │   ├── InsuranceApplicationTests.java
│       │   ├── NameFormatterTest.java             # Test case theo đúng ví dụ Phụ lục No.8
│       │   └── InsuranceServiceIntegrationTest.java # Kiểm thử toàn diện toàn bộ Service nghiệp vụ
│       └── resources/
│           └── application.yml                    # In-memory H2 config cho test
```

---

## 2. Chi Tiết Thực Hiện Các Màn Hình & Nghiệp Vụ

### Màn hình MH01 - Đăng nhập
- **Xác thực:** Kiểm tra username và mật khẩu đã băm MD5 trong `tbl_user`.
- **Validation:**
  - Chưa nhập username: Báo lỗi `"Hãy nhập Tên đăng nhập!"`.
  - Chưa nhập mật khẩu: Báo lỗi `"Hãy nhập Mật khẩu!"`, giữ lại username trong ô input (theo Q&A).
  - Sai tài khoản hoặc mật khẩu: Báo lỗi `"Tên đăng nhập hoặc Mật khẩu không đúng!"`, giữ lại username.
- **Session & Interceptor:** Khi đăng nhập thành công, lưu `LOGIN_USER` vào session. `AuthInterceptor` chặn mọi truy cập chưa đăng nhập vào `/insurance/**`.

### Màn hình MH02 - Danh sách thẻ bảo hiểm
- **Hạng mục 1 (Tên công ty):** Dropdown select box hiển thị toàn bộ công ty đã sort tăng dần theo tên công ty. Mặc định chọn công ty đầu tiên. Khi đổi công ty (onchange), tự động reset các ô tìm kiếm và submit lại.
- **Tìm kiếm:** Cho phép tìm kiếm theo `Tên người sử dụng` (LIKE), `Mã số thẻ bảo hiểm` (LIKE), `Nơi đăng ký` (LIKE).
- **Sắp xếp:** Mặc định sắp xếp theo Tên người sử dụng tăng dần (▲). Click vào icon ▲ / ▼ để đổi chiều sắp xếp; icon đang active hiển thị dạng text/disable click.
- **Phân trang:**
  - 5 bản ghi trên 1 trang.
  - Dãy phân trang gồm 5 trang. Trang hiện tại luôn ở giữa nếu `currentPage > 2` hoặc `currentPage < totalPages - 2`.
  - Ẩn nút `<<` ở trang đầu tiên và ẩn `>>` ở trang cuối cùng.
- **Xử lý danh sách rỗng:** Nếu không tìm thấy bản ghi, ẩn nút `Export CSV` và thay bảng bằng thông báo `"Không tìm thấy thẻ bảo hiểm"`.
- **Export CSV:** Xuất file `.csv` chuẩn định dạng UTF-8 (có BOM để Excel hiển thị đúng tiếng Việt) theo đúng `Template CSV`.

### Màn hình MH03 - Chi tiết thẻ bảo hiểm
- Hiển thị đầy đủ thông tin thẻ bảo hiểm, người sử dụng và công ty.
- Link **"Quay lại"**: Quay trở lại MH02 và giữ nguyên các điều kiện tìm kiếm/phân trang trước đó.
- Nút **"Cập nhật"**: Chuyển sang MH05.
- Nút **"Xóa"**: Xóa thẻ bảo hiểm và user liên quan, sau đó quay lại MH02 đồng thời reset điều kiện tìm kiếm (theo Phụ lục No.3).

### Màn hình MH04 (Đăng ký) & MH05 (Cập nhật)
- **Xử lý Công ty (Phụ lục No.6):**
  - **Radio 1 (R1 - Công ty đã có):** Chọn công ty từ dropdown, tự động gọi AJAX tới `/api/companies/{id}` để hiển thị Tên, Địa chỉ, Email, SĐT tại vùng V1. Ẩn vùng V2.
  - **Radio 2 (R2 - Đăng ký theo công ty mới):** Ẩn vùng V1, hiển thị vùng V2 với các input: Tên công ty (*), Địa chỉ (*), Email, SĐT.
- **Format tên người sử dụng (Phụ lục No.8):**
  - Tự động bỏ dấu tiếng Việt, loại bỏ ký tự lạ và số, chỉ giữ chữ cái latin, viết hoa chữ cái đầu mỗi từ, cách nhau 1 khoảng trắng (Ví dụ: `"Tr加加加ầN 2加 vi12Ệt hÙ&*@nG "` -> `"Tran Viet Hung"`).
- **Validation nghiệp vụ:**
  - Bắt buộc nhập các trường required (`"Hãy nhập {0}!"`).
  - Mã số thẻ bảo hiểm phải đúng 10 chữ số (`"Mã số thẻ bảo hiểm phải gồm 10 chữ số!"`).
  - Kiểm tra trùng lặp mã số thẻ bảo hiểm (`"Đã tồn tại thông tin thẻ bảo hiểm!"`).
  - Kiểm tra trùng lặp username (`"Tên đăng nhập đã tồn tại!"`).
  - Kiểm tra định dạng ngày `dd/MM/yyyy` (`"Lỗi nhập sai định dạng tại hạng mục {0}!"`).
  - Kiểm tra `Ngày kết thúc thẻ BH` phải sau `Ngày bắt đầu thẻ BH`.

### Màn hình MH Error
- Tự động điều hướng và hiển thị thông báo lỗi thân thiện khi xảy ra lỗi hệ thống hoặc kết nối cơ sở dữ liệu (theo Q&A).

---

## 3. Cách Chạy và Kiểm Thử Ứng Dụng

### Chạy Tests
```bash
cd "c:\TCT\Training-DEV5\JAVA\Member_Training\Khanh2ND\Spring boot\insurance-management"
.\gradlew.bat test
```

### Khởi Chạy Ứng Dụng
```bash
.\gradlew.bat bootRun
```
Ứng dụng sẽ chạy tại địa chỉ: `http://localhost:8080/`

- **Tài khoản đăng nhập có sẵn trong dữ liệu mẫu:**
  - Username: `admin`
  - Mật khẩu: `admin`
  (Hoặc các tài khoản `user2`, `user3`, ... với mật khẩu `123456`)
- **Trang quản trị H2 Console (tùy chọn):** `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:file:./data/insurancedb;MODE=MySQL;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE`
  - Username: `sa`
  - Password: *(để trống)*
