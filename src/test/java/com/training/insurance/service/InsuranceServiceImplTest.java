package com.training.insurance.service;

import com.training.insurance.dto.request.InsuranceFormRequest;
import com.training.insurance.dto.request.InsuranceSearchCriteria;
import com.training.insurance.dto.response.InsuranceItemResponse;
import com.training.insurance.entity.Company;
import com.training.insurance.entity.Insurance;
import com.training.insurance.entity.User;
import com.training.insurance.exception.AppException;
import com.training.insurance.repository.CompanyRepository;
import com.training.insurance.repository.InsuranceRepository;
import com.training.insurance.repository.UserRepository;
import com.training.insurance.service.impl.InsuranceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UNIT TEST cho InsuranceServiceImpl
 *
 * Bổ sung thêm:
 * ArgumentCaptor → "bắt" tham số thực tế được truyền vào mock để kiểm tra
 * doNothing()    → mock cho void method không làm gì
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InsuranceServiceImpl - Unit Tests")
class InsuranceServiceImplTest {

    @Mock
    private InsuranceRepository insuranceRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private InsuranceServiceImpl insuranceService;

    private Company company;
    private Insurance insurance;
    private User user;
    private InsuranceFormRequest validForm;

    @BeforeEach
    void setUp() {
        company = Company.builder()
                .companyInternalId(1).companyName("Cty CP Phan Mem Luvina")
                .address("Ha Noi").email("contact@luvina.net").telephone("0241234567")
                .build();

        insurance = Insurance.builder()
                .insuranceInternalId(10).insuranceNumber("0123456789")
                .insuranceStartDate(LocalDate.of(2026, 1, 1))
                .insuranceEndDate(LocalDate.of(2026, 12, 31))
                .placeOfRegister("Benh Vien E")
                .build();

        user = User.builder()
                .userInternalId(5).username("testuser").userFullName("Nguyen Van An")
                .userSexDivision("01").birthdate(LocalDate.of(1990, 5, 15))
                .company(company).insurance(insurance)
                .build();

        validForm = InsuranceFormRequest.builder()
                .companyType("EXIST").companyId(1).username("newuser01").password("123456")
                .userFullName("Nguyen Van An").userSexDivision("01").birthdate("01/01/1990")
                .insuranceNumber("1234567890").startDate("01/01/2026").endDate("31/12/2026")
                .placeOfRegister("Benh Vien E")
                .build();
    }

    // =========================================================
    // searchInsurances
    // =========================================================

    @Test
    @DisplayName("searchInsurances → có kết quả → trả về Page đúng")
    void searchInsurances_withResults_returnsPage() {
        when(userRepository.findAll(any(Specification.class))).thenReturn(List.of(user));

        InsuranceSearchCriteria criteria = InsuranceSearchCriteria.builder()
                .companyId(1).page(0).size(5).order("ASC").build();

        Page<InsuranceItemResponse> result = insuranceService.searchInsurances(criteria);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Nguyen Van An", result.getContent().get(0).getUserFullName());
    }

    @Test
    @DisplayName("searchInsurances → không có kết quả → trả về Page rỗng")
    void searchInsurances_noResults_returnsEmptyPage() {
        when(userRepository.findAll(any(Specification.class))).thenReturn(List.of());

        Page<InsuranceItemResponse> result = insuranceService.searchInsurances(
                InsuranceSearchCriteria.builder().companyId(999).page(0).size(5).build());

        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    // =========================================================
    // getInsuranceDetail
    // =========================================================

    @Test
    @DisplayName("getInsuranceDetail → tìm thấy → trả về response đúng các field")
    void getInsuranceDetail_existingId_returnsResponse() {
        when(userRepository.findByInsuranceInsuranceInternalId(10)).thenReturn(Optional.of(user));

        InsuranceItemResponse result = insuranceService.getInsuranceDetail(10);

        assertNotNull(result);
        assertEquals(5, result.getUserId());
        assertEquals(10, result.getInsuranceId());
        assertEquals("Nguyen Van An", result.getUserFullName());
        assertEquals("0123456789", result.getInsuranceNumber());
        assertEquals("Nam", result.getGender());
    }

    @Test
    @DisplayName("getInsuranceDetail → không tìm thấy → ném AppException")
    void getInsuranceDetail_notFound_throwsAppException() {
        when(userRepository.findByInsuranceInsuranceInternalId(999)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> insuranceService.getInsuranceDetail(999));
        assertEquals("Không tìm thấy thông tin thẻ bảo hiểm!", ex.getMessage());
    }

    // =========================================================
    // getFormDtoForEdit
    // =========================================================

    @Test
    @DisplayName("getFormDtoForEdit → map đúng tất cả field sang InsuranceFormRequest")
    void getFormDtoForEdit_existingId_returnsMappedForm() {
        when(userRepository.findByInsuranceInsuranceInternalId(10)).thenReturn(Optional.of(user));

        InsuranceFormRequest form = insuranceService.getFormDtoForEdit(10);

        assertNotNull(form);
        assertEquals(10, form.getId());
        assertEquals(5, form.getUserId());
        assertEquals("testuser", form.getUsername());
        assertEquals("Nguyen Van An", form.getUserFullName());
        assertEquals("01", form.getUserSexDivision());
        assertEquals("15/05/1990", form.getBirthdate());
        assertEquals("0123456789", form.getInsuranceNumber());
        assertEquals("01/01/2026", form.getStartDate());
        assertEquals("31/12/2026", form.getEndDate());
        assertEquals("Benh Vien E", form.getPlaceOfRegister());
        assertEquals("", form.getPassword()); // password luôn rỗng khi edit
        assertEquals("EXIST", form.getCompanyType());
        assertEquals(1, form.getCompanyId());
    }

    // =========================================================
    // createInsurance
    // =========================================================

    @Test
    @DisplayName("createInsurance thành công → user và insurance được lưu")
    void createInsurance_validForm_savesUserAndInsurance() {
        when(userRepository.existsByUsername("newuser01")).thenReturn(false);
        when(insuranceRepository.existsByInsuranceNumber("1234567890")).thenReturn(false);
        when(companyRepository.findById(1)).thenReturn(Optional.of(company));
        when(insuranceRepository.save(any(Insurance.class)))
                .thenReturn(Insurance.builder().insuranceInternalId(100).build());
        when(userRepository.save(any(User.class))).thenReturn(new User());

        assertDoesNotThrow(() -> insuranceService.createInsurance(validForm));
        verify(insuranceRepository, times(1)).save(any(Insurance.class));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("createInsurance → tên người dùng được format đúng (ArgumentCaptor)")
    void createInsurance_formatsUserFullName() {
        when(userRepository.existsByUsername("newuser01")).thenReturn(false);
        when(insuranceRepository.existsByInsuranceNumber("1234567890")).thenReturn(false);
        when(companyRepository.findById(1)).thenReturn(Optional.of(company));
        when(insuranceRepository.save(any(Insurance.class)))
                .thenReturn(Insurance.builder().insuranceInternalId(100).build());

        // ArgumentCaptor: "bắt" tham số thực tế được truyền vào save()
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        validForm.setUserFullName("NGUYEN   VAN   AN");
        insuranceService.createInsurance(validForm);

        verify(userRepository).save(userCaptor.capture());
        assertEquals("Nguyen Van An", userCaptor.getValue().getUserFullName());
    }

    @Test
    @DisplayName("createInsurance → username trùng → ném AppException, không lưu DB")
    void createInsurance_duplicateUsername_throwsAppException() {
        when(userRepository.existsByUsername("newuser01")).thenReturn(true);

        AppException ex = assertThrows(AppException.class,
                () -> insuranceService.createInsurance(validForm));
        assertEquals("Tên đăng nhập đã tồn tại!", ex.getMessage());
        verify(insuranceRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("createInsurance → số thẻ BH trùng → ném AppException")
    void createInsurance_duplicateInsuranceNumber_throwsAppException() {
        when(userRepository.existsByUsername("newuser01")).thenReturn(false);
        when(insuranceRepository.existsByInsuranceNumber("1234567890")).thenReturn(true);

        AppException ex = assertThrows(AppException.class,
                () -> insuranceService.createInsurance(validForm));
        assertEquals("Đã tồn tại thông tin thẻ bảo hiểm!", ex.getMessage());
    }

    @Test
    @DisplayName("createInsurance → họ tên rỗng → ném AppException, không gọi DB")
    void createInsurance_emptyFullName_throwsAppException() {
        validForm.setUserFullName("   ");

        AppException ex = assertThrows(AppException.class,
                () -> insuranceService.createInsurance(validForm));
        assertEquals("Hãy nhập Họ và Tên!", ex.getMessage());
        verifyNoInteractions(userRepository, insuranceRepository, companyRepository);
    }

    @Test
    @DisplayName("createInsurance → số thẻ BH không đúng 10 số → ném AppException")
    void createInsurance_invalidInsuranceNumberFormat_throwsAppException() {
        validForm.setInsuranceNumber("123");

        AppException ex = assertThrows(AppException.class,
                () -> insuranceService.createInsurance(validForm));
        assertEquals("Mã số thẻ bảo hiểm phải gồm 10 chữ số!", ex.getMessage());
    }

    @Test
    @DisplayName("createInsurance → ngày kết thúc trước ngày bắt đầu → ném AppException")
    void createInsurance_endDateBeforeStartDate_throwsAppException() {
        when(userRepository.existsByUsername("newuser01")).thenReturn(false);
        when(insuranceRepository.existsByInsuranceNumber("1234567890")).thenReturn(false);
        validForm.setStartDate("01/06/2026");
        validForm.setEndDate("01/01/2026");

        AppException ex = assertThrows(AppException.class,
                () -> insuranceService.createInsurance(validForm));
        assertEquals("Ngày kết thúc thẻ BH phải sau ngày bắt đầu!", ex.getMessage());
    }

    @Test
    @DisplayName("createInsurance → company không tồn tại → ném AppException")
    void createInsurance_companyNotFound_throwsAppException() {
        when(userRepository.existsByUsername("newuser01")).thenReturn(false);
        when(insuranceRepository.existsByInsuranceNumber("1234567890")).thenReturn(false);
        when(companyRepository.findById(1)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> insuranceService.createInsurance(validForm));
        assertEquals("Công ty đã chọn không tồn tại!", ex.getMessage());
    }

    // =========================================================
    // updateInsurance
    // =========================================================

    @Test
    @DisplayName("updateInsurance thành công → insurance và user được cập nhật")
    void updateInsurance_validForm_updatesInsuranceAndUser() {
        InsuranceFormRequest form = InsuranceFormRequest.builder()
                .id(10).userId(5).companyType("EXIST").companyId(1)
                .username("testuser").password("").userFullName("Nguyen Van Binh")
                .userSexDivision("01").birthdate("20/06/1992")
                .insuranceNumber("0123456789").startDate("01/01/2026").endDate("31/12/2026")
                .placeOfRegister("Benh Vien Bach Mai")
                .build();

        when(userRepository.existsByUsernameAndUserInternalIdNot("testuser", 5)).thenReturn(false);
        when(insuranceRepository.existsByInsuranceNumberAndInsuranceInternalIdNot("0123456789", 10)).thenReturn(false);
        when(insuranceRepository.findById(10)).thenReturn(Optional.of(insurance));
        when(userRepository.findByInsuranceInsuranceInternalId(10)).thenReturn(Optional.of(user));
        when(companyRepository.findById(1)).thenReturn(Optional.of(company));
        when(insuranceRepository.save(any())).thenReturn(insurance);
        when(userRepository.save(any())).thenReturn(user);

        assertDoesNotThrow(() -> insuranceService.updateInsurance(form));
        verify(insuranceRepository, times(1)).save(any(Insurance.class));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("updateInsurance → insurance không tồn tại → ném AppException")
    void updateInsurance_insuranceNotFound_throwsAppException() {
        InsuranceFormRequest form = InsuranceFormRequest.builder()
                .id(999).userId(5).companyType("EXIST").companyId(1)
                .username("testuser").userFullName("Test").userSexDivision("01")
                .insuranceNumber("0123456789").startDate("01/01/2026").endDate("31/12/2026")
                .placeOfRegister("Hospital")
                .build();

        when(userRepository.existsByUsernameAndUserInternalIdNot("testuser", 5)).thenReturn(false);
        when(insuranceRepository.existsByInsuranceNumberAndInsuranceInternalIdNot("0123456789", 999)).thenReturn(false);
        when(insuranceRepository.findById(999)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> insuranceService.updateInsurance(form));
        assertEquals("Không tìm thấy thông tin thẻ bảo hiểm cần cập nhật!", ex.getMessage());
    }

    // =========================================================
    // deleteInsurance
    // =========================================================

    @Test
    @DisplayName("deleteInsurance → xóa cả user và insurance")
    void deleteInsurance_existingId_deletesUserAndInsurance() {
        when(userRepository.findByInsuranceInsuranceInternalId(10)).thenReturn(Optional.of(user));

        insuranceService.deleteInsurance(10);

        verify(userRepository, times(1)).delete(user);
        verify(insuranceRepository, times(1)).deleteById(10);
    }

    @Test
    @DisplayName("deleteInsurance → không có user → chỉ xóa insurance")
    void deleteInsurance_noUser_onlyDeletesInsurance() {
        when(userRepository.findByInsuranceInsuranceInternalId(99)).thenReturn(Optional.empty());

        insuranceService.deleteInsurance(99);

        verify(userRepository, never()).delete(any(User.class));
        verify(insuranceRepository, times(1)).deleteById(99);
    }

    // =========================================================
    // exportCsv
    // =========================================================

    @Test
    @DisplayName("exportCsv → ghi đúng header và dữ liệu ra PrintWriter")
    void exportCsv_withData_writesCorrectCsvContent() {
        when(companyRepository.findById(1)).thenReturn(Optional.of(company));
        when(userRepository.findAll(any(Specification.class))).thenReturn(List.of(user));

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        insuranceService.exportCsv(
                InsuranceSearchCriteria.builder().companyId(1).order("ASC").build(), pw);
        String csv = sw.toString();

        assertTrue(csv.contains("Danh sách thông tin thẻ bảo hiểm"));
        assertTrue(csv.contains("Cty CP Phan Mem Luvina"));
        assertTrue(csv.contains("Họ và tên,Giới tính,Ngày sinh,Mã số thẻ bảo hiểm,Ngày bắt đầu,Ngày kết thúc,Nơi đăng ký KCB"));
        assertTrue(csv.contains("Nguyen Van An"));
        assertTrue(csv.contains("0123456789"));
    }

    @Test
    @DisplayName("exportCsv → không có dữ liệu → chỉ ghi header")
    void exportCsv_noData_writesOnlyHeader() {
        when(companyRepository.findById(1)).thenReturn(Optional.of(company));
        when(userRepository.findAll(any(Specification.class))).thenReturn(List.of());

        StringWriter sw = new StringWriter();
        insuranceService.exportCsv(
                InsuranceSearchCriteria.builder().companyId(1).build(), new PrintWriter(sw));
        String csv = sw.toString();

        assertTrue(csv.contains("Danh sách thông tin thẻ bảo hiểm"));
        assertFalse(csv.contains("Nguyen Van An"));
    }
}
