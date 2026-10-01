package com.training.insurance.controller;

import com.training.insurance.dto.request.InsuranceFormRequest;
import com.training.insurance.dto.request.InsuranceSearchCriteria;
import com.training.insurance.dto.response.InsuranceItemResponse;
import com.training.insurance.entity.Company;
import com.training.insurance.entity.User;
import com.training.insurance.exception.AppException;
import com.training.insurance.interceptor.AuthInterceptor;
import com.training.insurance.service.CompanyService;
import com.training.insurance.service.InsuranceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CONTROLLER TEST cho InsuranceController - dùng MockMvc
 *
 * InsuranceController dùng AuthInterceptor để bảo vệ /insurance/**
 * → Mọi request phải có session LOGIN_USER
 * → Dùng MockHttpSession để giả lập trạng thái đã đăng nhập
 */
@WebMvcTest(InsuranceController.class)
@DisplayName("InsuranceController - Controller Tests (MockMvc)")
class InsuranceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InsuranceService insuranceService;

    @MockBean
    private CompanyService companyService;

    /**
     * Mock AuthInterceptor để cho phép request đi qua trong test
     * → thenReturn(true) = cho phép tiếp tục (đã xác thực)
     */
    @MockBean
    private AuthInterceptor authInterceptor;

    // Session giả lập trạng thái đã đăng nhập
    private MockHttpSession loggedInSession;

    // Dữ liệu dùng chung
    private Company company;
    private InsuranceItemResponse sampleInsurance;
    private List<Company> companies;

    @BeforeEach
    void setUp() throws Exception {
        // Giả lập đã đăng nhập
        loggedInSession = new MockHttpSession();
        loggedInSession.setAttribute("LOGIN_USER",
                User.builder().userInternalId(1).username("admin").build());

        // AuthInterceptor cho phép request đi qua
        when(authInterceptor.preHandle(any(), any(), any())).thenReturn(true);

        // Dữ liệu mẫu
        company = Company.builder()
                .companyInternalId(1)
                .companyName("Cty CP Phan Mem Luvina")
                .address("Ha Noi")
                .build();
        companies = List.of(company);

        sampleInsurance = InsuranceItemResponse.builder()
                .userId(5)
                .insuranceId(10)
                .companyId(1)
                .companyName("Cty CP Phan Mem Luvina")
                .username("testuser")
                .userFullName("Nguyen Van An")
                .userSexDivision("01")
                .gender("Nam")
                .birthdate(LocalDate.of(1990, 5, 15))
                .insuranceNumber("0123456789")
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .placeOfRegister("Benh Vien E")
                .build();
    }

    // =========================================================
    // NHÓM TEST: GET /insurance (danh sách)
    // =========================================================

    @Test
    @DisplayName("GET /insurance → trả về insurance-list view với danh sách dữ liệu")
    void listInsurances_returnsListViewWithData() throws Exception {
        // ARRANGE
        Page<InsuranceItemResponse> page = new PageImpl<>(
                List.of(sampleInsurance), PageRequest.of(0, 5), 1);

        when(companyService.getAllCompanies()).thenReturn(companies);
        when(insuranceService.searchInsurances(any(InsuranceSearchCriteria.class))).thenReturn(page);

        // ACT + ASSERT
        mockMvc.perform(get("/insurance").session(loggedInSession))
                .andExpect(status().isOk())
                .andExpect(view().name("insurance-list"))
                // Kiểm tra model có đủ attributes
                .andExpect(model().attributeExists("companies"))
                .andExpect(model().attributeExists("pageData"))
                .andExpect(model().attributeExists("criteria"));

        verify(companyService, times(1)).getAllCompanies();
        verify(insuranceService, times(1)).searchInsurances(any());
    }

    // =========================================================
    // NHÓM TEST: GET /insurance/detail/{id}
    // =========================================================

    @Test
    @DisplayName("GET /insurance/detail/{id} → trả về insurance-detail view")
    void getDetail_existingId_returnsDetailView() throws Exception {
        // ARRANGE
        when(insuranceService.getInsuranceDetail(10)).thenReturn(sampleInsurance);

        // ACT + ASSERT
        mockMvc.perform(get("/insurance/detail/10").session(loggedInSession))
                .andExpect(status().isOk())
                .andExpect(view().name("insurance-detail"))
                .andExpect(model().attributeExists("insurance"));

        verify(insuranceService).getInsuranceDetail(10);
    }

    // =========================================================
    // NHÓM TEST: GET /insurance/create
    // =========================================================

    @Test
    @DisplayName("GET /insurance/create → trả về form tạo mới")
    void createView_returnsFormWithEmptyData() throws Exception {
        // ARRANGE
        when(companyService.getAllCompanies()).thenReturn(companies);

        // ACT + ASSERT
        mockMvc.perform(get("/insurance/create").session(loggedInSession))
                .andExpect(status().isOk())
                .andExpect(view().name("insurance-form"))
                .andExpect(model().attribute("isEdit", false))
                .andExpect(model().attributeExists("form"))
                .andExpect(model().attributeExists("companies"));
    }

    // =========================================================
    // NHÓM TEST: POST /insurance/create
    // =========================================================

    @Test
    @DisplayName("POST /insurance/create - thành công → redirect về /insurance")
    void create_validForm_redirectsToList() throws Exception {
        // ARRANGE - createInsurance không ném exception
        doNothing().when(insuranceService).createInsurance(any(InsuranceFormRequest.class));

        // ACT + ASSERT
        mockMvc.perform(post("/insurance/create")
                        .session(loggedInSession)
                        .param("companyType", "EXIST")
                        .param("companyId", "1")
                        .param("username", "newuser01")
                        .param("userFullName", "Nguyen Van Test")
                        .param("userSexDivision", "01")
                        .param("insuranceNumber", "1234567890")
                        .param("startDate", "01/01/2026")
                        .param("endDate", "31/12/2026")
                        .param("placeOfRegister", "Benh Vien E"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/insurance"));

        verify(insuranceService, times(1)).createInsurance(any());
    }

    @Test
    @DisplayName("POST /insurance/create - thất bại (AppException) → trở lại form với error")
    void create_invalidForm_returnsFormWithError() throws Exception {
        // ARRANGE - createInsurance ném AppException
        doThrow(new AppException("Đã tồn tại thông tin thẻ bảo hiểm!"))
                .when(insuranceService).createInsurance(any(InsuranceFormRequest.class));
        when(companyService.getAllCompanies()).thenReturn(companies);

        // ACT + ASSERT
        mockMvc.perform(post("/insurance/create")
                        .session(loggedInSession)
                        .param("insuranceNumber", "0123456789"))
                .andExpect(status().isOk())               // Không redirect, ở lại form
                .andExpect(view().name("insurance-form"))
                .andExpect(model().attribute("errorMessage", "Đã tồn tại thông tin thẻ bảo hiểm!"))
                .andExpect(model().attribute("isEdit", false));
    }

    // =========================================================
    // NHÓM TEST: POST /insurance/delete/{id}
    // =========================================================

    @Test
    @DisplayName("POST /insurance/delete/{id} - thành công → redirect về /insurance")
    void delete_existingId_redirectsToList() throws Exception {
        // ARRANGE
        doNothing().when(insuranceService).deleteInsurance(10);

        // ACT + ASSERT
        mockMvc.perform(post("/insurance/delete/10").session(loggedInSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/insurance"));

        verify(insuranceService, times(1)).deleteInsurance(10);
    }

    // =========================================================
    // NHÓM TEST: GET /insurance/edit/{id}
    // =========================================================

    @Test
    @DisplayName("GET /insurance/edit/{id} → trả về form chỉnh sửa")
    void editView_existingId_returnsEditForm() throws Exception {
        // ARRANGE
        InsuranceFormRequest form = InsuranceFormRequest.builder()
                .id(10)
                .username("testuser")
                .companyType("EXIST")
                .companyId(1)
                .build();

        when(insuranceService.getFormDtoForEdit(10)).thenReturn(form);
        when(companyService.getAllCompanies()).thenReturn(companies);

        // ACT + ASSERT
        mockMvc.perform(get("/insurance/edit/10").session(loggedInSession))
                .andExpect(status().isOk())
                .andExpect(view().name("insurance-form"))
                .andExpect(model().attribute("isEdit", true))
                .andExpect(model().attributeExists("form"))
                .andExpect(model().attributeExists("companies"));
    }

    // =========================================================
    // NHÓM TEST: POST /insurance/edit
    // =========================================================

    @Test
    @DisplayName("POST /insurance/edit - thành công → redirect về /insurance")
    void edit_validForm_redirectsToList() throws Exception {
        // ARRANGE
        doNothing().when(insuranceService).updateInsurance(any(InsuranceFormRequest.class));

        // ACT + ASSERT
        mockMvc.perform(post("/insurance/edit").session(loggedInSession)
                        .param("id", "10")
                        .param("userId", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/insurance"));
    }

    @Test
    @DisplayName("POST /insurance/edit - thất bại (AppException) → trở lại form với error")
    void edit_invalidForm_returnsFormWithError() throws Exception {
        // ARRANGE
        doThrow(new AppException("Tên đăng nhập đã tồn tại!"))
                .when(insuranceService).updateInsurance(any(InsuranceFormRequest.class));
        when(companyService.getAllCompanies()).thenReturn(companies);

        // ACT + ASSERT
        mockMvc.perform(post("/insurance/edit").session(loggedInSession)
                        .param("id", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("insurance-form"))
                .andExpect(model().attribute("errorMessage", "Tên đăng nhập đã tồn tại!"))
                .andExpect(model().attribute("isEdit", true));
    }
}
