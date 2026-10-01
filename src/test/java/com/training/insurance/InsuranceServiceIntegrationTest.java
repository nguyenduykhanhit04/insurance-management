package com.training.insurance;

import com.training.insurance.dto.request.InsuranceFormRequest;
import com.training.insurance.dto.request.InsuranceSearchCriteria;
import com.training.insurance.dto.response.InsuranceItemResponse;
import com.training.insurance.entity.User;
import com.training.insurance.exception.AppException;
import com.training.insurance.service.AuthService;
import com.training.insurance.service.CompanyService;
import com.training.insurance.service.InsuranceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class InsuranceServiceIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private InsuranceService insuranceService;

    @Test
    void testAuthServiceLoginSuccess() {
        User user = authService.login("admin", "admin");
        assertNotNull(user);
        assertEquals("admin", user.getUsername());
    }

    @Test
    void testAuthServiceLoginFail() {
        User user = authService.login("admin", "wrongpassword");
        assertNull(user);
    }

    @Test
    void testSearchInsurancesPagination() {
        InsuranceSearchCriteria criteria = InsuranceSearchCriteria.builder()
                .companyId(1)
                .page(0)
                .size(5)
                .order("ASC")
                .build();

        Page<InsuranceItemResponse> result = insuranceService.searchInsurances(criteria);
        assertNotNull(result);
        assertEquals(5, result.getContent().size());
        assertTrue(result.getTotalElements() >= 11);
        assertEquals(3, result.getTotalPages());
    }

    @Test
    void testCreateInsuranceAndFormatName() {
        InsuranceFormRequest form = InsuranceFormRequest.builder()
                .companyType("EXIST")
                .companyId(1)
                .username("testuser99")
                .password("123456")
                .userFullName("Tr加加加ầN  2加  vi12Ệt hÙ&*@nG   ")
                .userSexDivision("01")
                .birthdate("15/05/1995")
                .insuranceNumber("9999888877")
                .startDate("01/01/2026")
                .endDate("31/12/2026")
                .placeOfRegister("Bệnh Viện E")
                .build();

        insuranceService.createInsurance(form);

        InsuranceSearchCriteria criteria = InsuranceSearchCriteria.builder()
                .companyId(1)
                .insuranceNumber("9999888877")
                .build();

        Page<InsuranceItemResponse> searchRes = insuranceService.searchInsurances(criteria);
        assertEquals(1, searchRes.getTotalElements());
        assertEquals("Tran Viet Hung", searchRes.getContent().get(0).getUserFullName());
    }

    @Test
    void testDuplicateInsuranceNumberThrowsException() {
        InsuranceFormRequest form = InsuranceFormRequest.builder()
                .companyType("EXIST")
                .companyId(1)
                .username("newuser123")
                .password("123456")
                .userFullName("Le Van Test")
                .userSexDivision("01")
                .birthdate("10/10/1990")
                .insuranceNumber("0123456789")
                .startDate("01/01/2026")
                .endDate("31/12/2026")
                .placeOfRegister("Bệnh Viện E")
                .build();

        AppException ex = assertThrows(AppException.class, () -> insuranceService.createInsurance(form));
        assertEquals("Đã tồn tại thông tin thẻ bảo hiểm!", ex.getMessage());
    }

    @Test
    void testExportCsv() {
        InsuranceSearchCriteria criteria = InsuranceSearchCriteria.builder()
                .companyId(1)
                .order("ASC")
                .build();

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        insuranceService.exportCsv(criteria, pw);

        String csvOutput = sw.toString();
        assertTrue(csvOutput.contains("Danh sách thông tin thẻ bảo hiểm"));
        assertTrue(csvOutput.contains("Cty CP Phan Mem Luvina"));
        assertTrue(csvOutput.contains("Họ và tên,Giới tính,Ngày sinh,Mã số thẻ bảo hiểm,Ngày bắt đầu,Ngày kết thúc,Nơi đăng ký KCB"));
    }
}
