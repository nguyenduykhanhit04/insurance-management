package com.training.insurance.controller;

import com.training.insurance.dto.response.CompanyResponse;
import com.training.insurance.entity.Company;
import com.training.insurance.exception.AppException;
import com.training.insurance.interceptor.AuthInterceptor;
import com.training.insurance.service.CompanyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CONTROLLER TEST cho CompanyApiController (REST API)
 *
 * Khác AuthController (Thymeleaf), đây là REST → trả về JSON
 * → dùng jsonPath() để kiểm tra nội dung JSON response
 */
@WebMvcTest(CompanyApiController.class)
@DisplayName("CompanyApiController - Controller Tests (MockMvc)")
class CompanyApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompanyService companyService;

    // AuthInterceptor cũng được load trong @WebMvcTest nếu đăng ký toàn cục
    // → mock để tránh redirect về /login khi test /api/**
    @MockBean
    private AuthInterceptor authInterceptor;

    @Test
    @DisplayName("GET /api/companies/{id} → tìm thấy → trả về JSON đúng (HTTP 200)")
    void getCompanyDetails_existingId_returns200WithJson() throws Exception {
        // ARRANGE
        Company company = Company.builder()
                .companyInternalId(1)
                .companyName("Cty CP Phan Mem Luvina")
                .address("Ha Noi")
                .email("contact@luvina.net")
                .telephone("0241234567")
                .build();

        when(companyService.getCompanyById(1)).thenReturn(company);

        // ACT + ASSERT
        mockMvc.perform(get("/api/companies/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())                              // HTTP 200
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // jsonPath(): kiểm tra từng field trong JSON response
                .andExpect(jsonPath("$.companyInternalId").value(1))
                .andExpect(jsonPath("$.companyName").value("Cty CP Phan Mem Luvina"))
                .andExpect(jsonPath("$.address").value("Ha Noi"))
                .andExpect(jsonPath("$.email").value("contact@luvina.net"))
                .andExpect(jsonPath("$.telephone").value("0241234567"));

        verify(companyService, times(1)).getCompanyById(1);
    }

    @Test
    @DisplayName("GET /api/companies/{id} → không tìm thấy → HTTP 200 + error view (GlobalExceptionHandler bắt)")
    void getCompanyDetails_notFound_returnsErrorView() throws Exception {
        // ARRANGE
        when(companyService.getCompanyById(999))
                .thenThrow(new AppException("Không tìm thấy thông tin công ty!"));

        // ACT + ASSERT
        // GlobalExceptionHandler.handleGeneralException() bắt mọi Exception
        // → trả về "error" view với HTTP 200 (không phải 500)
        // Đây là behavior thực tế của app - để test chính xác thực tế
        mockMvc.perform(get("/api/companies/999"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("GET /api/companies/{id} với id không phải số → HTTP 200 + error view (GlobalExceptionHandler bắt MethodArgumentTypeMismatchException)")
    void getCompanyDetails_nonNumericId_returnsErrorView() throws Exception {
        // Spring không thể convert "abc" → Integer → ném MethodArgumentTypeMismatchException
        // GlobalExceptionHandler.handleGeneralException() bắt → trả về "error" view HTTP 200
        mockMvc.perform(get("/api/companies/abc"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));
    }
}
