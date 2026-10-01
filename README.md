# Insurance Management System

Dự án **Quản lý thẻ bảo hiểm** được xây dựng bằng **Spring Boot 3.3.5** và **Java 17**, đáp ứng đầy đủ yêu cầu từ tài liệu đặc tả `Project1_QL The bao hiem.xlsx` và `QA.docx`.

---

## Tech Stack

| | |
|---|---|
| **Backend** | Spring Boot 3.3.5, Java 17 |
| **View** | Thymeleaf |
| **Database** | H2 (embedded file) · MySQL compatible |
| **ORM** | Spring Data JPA / Hibernate |
| **Build** | Gradle |
| **Testing** | JUnit 5 · Mockito · MockMvc |

---

## Cấu trúc dự án

```
insurance-management/
├── build.gradle
├── src/
│   ├── main/
│   │   ├── java/com/training/insurance/
│   │   │   ├── InsuranceApplication.java
│   │   │   ├── config/
│   │   │   │   └── WebMvcConfig.java              # Đăng ký AuthInterceptor
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java            # MH01: Đăng nhập / Đăng xuất
│   │   │   │   ├── InsuranceController.java       # MH02–MH05 + Export CSV
│   │   │   │   └── CompanyApiController.java      # REST API: lấy thông tin công ty (AJAX)
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   │   ├── InsuranceFormRequest.java  # Form đăng ký / cập nhật
│   │   │   │   │   ├── InsuranceSearchCriteria.java # Tiêu chí tìm kiếm + phân trang
│   │   │   │   │   └── LoginRequest.java
│   │   │   │   └── response/
│   │   │   │       ├── InsuranceItemResponse.java
│   │   │   │       └── CompanyResponse.java
│   │   │   ├── entity/
│   │   │   │   ├── Company.java                   # tbl_company
│   │   │   │   ├── Insurance.java                 # tbl_insurance
│   │   │   │   └── User.java                      # tbl_user
│   │   │   ├── exception/
│   │   │   │   ├── AppException.java
│   │   │   │   └── GlobalExceptionHandler.java    # Bắt lỗi toàn cục → error.html
│   │   │   ├── interceptor/
│   │   │   │   └── AuthInterceptor.java           # Bảo vệ /insurance/** bằng session
│   │   │   ├── repository/
│   │   │   │   ├── CompanyRepository.java
│   │   │   │   ├── InsuranceRepository.java
│   │   │   │   └── UserRepository.java            # JpaSpecificationExecutor (tìm kiếm động)
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java / impl/AuthServiceImpl.java
│   │   │   │   ├── CompanyService.java / impl/CompanyServiceImpl.java
│   │   │   │   └── InsuranceService.java / impl/InsuranceServiceImpl.java
│   │   │   └── util/
│   │   │       ├── MD5Util.java                   # Mã hóa mật khẩu MD5
│   │   │       └── NameFormatter.java             # Chuẩn hóa tên (Phụ lục No.8)
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── schema.sql                         # Tạo 3 bảng: tbl_company, tbl_insurance, tbl_user
│   │       ├── data.sql                           # Dữ liệu mẫu (11 bản ghi)
│   │       ├── messages.properties
│   │       └── templates/
│   │           ├── login.html                     # MH01
│   │           ├── insurance-list.html            # MH02
│   │           ├── insurance-detail.html          # MH03
│   │           ├── insurance-form.html            # MH04 (tạo mới) & MH05 (cập nhật)
│   │           └── error.html
│   └── test/
│       ├── java/com/training/insurance/
│       │   ├── InsuranceApplicationTests.java     # Context load test
│       │   ├── InsuranceServiceIntegrationTest.java # @SpringBootTest + H2
│       │   ├── entity/
│       │   │   └── UserTest.java                  # Unit test: User.getGenderText()
│       │   ├── service/
│       │   │   ├── AuthServiceImplTest.java       # Unit test + Mockito
│       │   │   ├── CompanyServiceImplTest.java    # Unit test + Mockito
│       │   │   └── InsuranceServiceImplTest.java  # Unit test + Mockito + ArgumentCaptor
│       │   ├── controller/
│       │   │   ├── AuthControllerTest.java        # MockMvc test
│       │   │   ├── CompanyApiControllerTest.java  # MockMvc test (REST JSON)
│       │   │   └── InsuranceControllerTest.java   # MockMvc test (Thymeleaf)
│       │   └── util/
│       │       ├── MD5UtilTest.java               # Unit test
│       │       └── NameFormatterTest.java         # Unit test + @ParameterizedTest
│       └── resources/
│           └── application.yml                    # H2 in-memory cho test
```

---

## Chạy ứng dụng

```bash
# Khởi chạy
.\gradlew.bat bootRun
```

Truy cập: `http://localhost:8080`

| Tài khoản | Mật khẩu |
|---|---|
| `admin` | `admin` |
| `user2`, `user3`, ... | `123456` |

**H2 Console** (tùy chọn): `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:file:./data/insurancedb;MODE=MySQL;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE`
- Username: `sa` · Password: *(để trống)*

---

## Chạy Tests

```bash
.\gradlew.bat test
```

### Cấu trúc test

| Loại | Annotation | Mô tả |
|---|---|---|
| **Unit Test** | `@ExtendWith(MockitoExtension.class)` | Test logic nghiệp vụ, không cần Spring/DB |
| **Controller Test** | `@WebMvcTest` + `MockMvc` | Test HTTP request/response, không cần server thật |
| **Integration Test** | `@SpringBootTest` | Test toàn bộ flow, dùng H2 in-memory |

---

## Nghiệp vụ chính

### MH01 — Đăng nhập
- Xác thực username + mật khẩu băm MD5 trong `tbl_user`
- Validation: thiếu username/password → báo lỗi, giữ lại username trong ô input
- Đăng nhập thành công → lưu `LOGIN_USER` vào session → redirect về MH02
- `AuthInterceptor` chặn toàn bộ `/insurance/**` khi chưa đăng nhập

### MH02 — Danh sách thẻ bảo hiểm
- Dropdown công ty: sort tăng dần theo tên, onchange tự reset và tìm lại
- Tìm kiếm LIKE theo: Tên người sử dụng, Mã số thẻ, Nơi đăng ký KCB
- Sắp xếp theo Tên người sử dụng (ASC/DESC), click icon để đổi chiều
- Phân trang: 5 bản ghi/trang, dãy 5 trang, ẩn `<<` ở trang đầu và `>>` ở trang cuối
- Export CSV: UTF-8 với BOM, đúng template quy định

### MH03 — Chi tiết thẻ bảo hiểm
- Hiển thị đầy đủ thông tin thẻ, người dùng, công ty
- Quay lại giữ nguyên điều kiện tìm kiếm/phân trang
- Xóa: xóa cả `tbl_user` và `tbl_insurance`, reset về MH02

### MH04 / MH05 — Đăng ký / Cập nhật
- **Công ty (Phụ lục No.6):** Radio chọn công ty có sẵn (R1) hoặc tạo công ty mới (R2). R1 gọi AJAX `/api/companies/{id}` để hiển thị thông tin công ty
- **Format tên (Phụ lục No.8):** Bỏ dấu tiếng Việt, loại ký tự lạ/số, viết hoa chữ cái đầu mỗi từ
  - Ví dụ: `"Tr加加加ầN  2加  vi12Ệt hÙ&*@nG"` → `"Tran Viet Hung"`
- **Validation:**
  - Các trường bắt buộc: `"Hãy nhập {field}!"`
  - Mã số thẻ đúng 10 chữ số
  - Không trùng mã số thẻ / username
  - Định dạng ngày `dd/MM/yyyy`
  - Ngày kết thúc phải sau ngày bắt đầu
