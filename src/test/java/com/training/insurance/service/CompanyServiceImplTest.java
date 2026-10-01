package com.training.insurance.service;

import com.training.insurance.entity.Company;
import com.training.insurance.exception.AppException;
import com.training.insurance.repository.CompanyRepository;
import com.training.insurance.service.impl.CompanyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UNIT TEST cho CompanyServiceImpl
 *
 * Bổ sung thêm:
 * thenThrow()          → mock ném exception
 * assertThrows()       → kiểm tra exception được ném ra đúng loại + message
 * any(Company.class)   → Argument Matcher, match bất kỳ đối tượng Company nào
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CompanyServiceImpl - Unit Tests")
class CompanyServiceImplTest {

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private CompanyServiceImpl companyService;

    private Company companyA;
    private Company companyB;

    @BeforeEach
    void setUp() {
        companyA = Company.builder()
                .companyInternalId(1)
                .companyName("Cty A")
                .address("Ha Noi")
                .email("a@company.com")
                .telephone("0901111111")
                .build();

        companyB = Company.builder()
                .companyInternalId(2)
                .companyName("Cty B")
                .address("TP HCM")
                .build();
    }

    @Test
    @DisplayName("getAllCompanies → trả về danh sách đúng thứ tự")
    void getAllCompanies_returnsSortedList() {
        when(companyRepository.findAllByOrderByCompanyNameAsc())
                .thenReturn(List.of(companyA, companyB));

        List<Company> result = companyService.getAllCompanies();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Cty A", result.get(0).getCompanyName());
        verify(companyRepository, times(1)).findAllByOrderByCompanyNameAsc();
    }

    @Test
    @DisplayName("getAllCompanies → không có dữ liệu → trả về list rỗng")
    void getAllCompanies_returnsEmptyListWhenNoData() {
        when(companyRepository.findAllByOrderByCompanyNameAsc()).thenReturn(List.of());

        List<Company> result = companyService.getAllCompanies();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getCompanyById → tìm thấy → trả về company đúng")
    void getCompanyById_existingId_returnsCompany() {
        when(companyRepository.findById(1)).thenReturn(Optional.of(companyA));

        Company result = companyService.getCompanyById(1);

        assertNotNull(result);
        assertEquals(1, result.getCompanyInternalId());
        assertEquals("Cty A", result.getCompanyName());
        assertEquals("Ha Noi", result.getAddress());
    }

    @Test
    @DisplayName("getCompanyById → không tìm thấy → ném AppException")
    void getCompanyById_nonExistingId_throwsAppException() {
        when(companyRepository.findById(999)).thenReturn(Optional.empty());

        // assertThrows: kiểm tra đoạn code trong lambda SẼ ném đúng loại exception
        AppException exception = assertThrows(
                AppException.class,
                () -> companyService.getCompanyById(999)
        );

        assertEquals("Không tìm thấy thông tin công ty!", exception.getMessage());
        verify(companyRepository).findById(999);
    }

    @Test
    @DisplayName("saveCompany → lưu thành công → trả về company đã có ID")
    void saveCompany_validCompany_returnsSavedCompany() {
        Company newCompany = Company.builder().companyName("Cty Moi").address("Da Nang").build();
        Company savedCompany = Company.builder().companyInternalId(3).companyName("Cty Moi").address("Da Nang").build();

        // any(Company.class): match bất kỳ Company nào được truyền vào save()
        when(companyRepository.save(any(Company.class))).thenReturn(savedCompany);

        Company result = companyService.saveCompany(newCompany);

        assertNotNull(result);
        assertEquals(3, result.getCompanyInternalId());
        verify(companyRepository, times(1)).save(newCompany);
    }

    @Test
    @DisplayName("saveCompany → repository ném exception → exception được propagate")
    void saveCompany_repositoryThrowsException_exceptionPropagates() {
        Company company = Company.builder().companyName("Test").address("Test").build();

        // thenThrow(): mock ném ra exception (giả lập DB lỗi)
        when(companyRepository.save(any(Company.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> companyService.saveCompany(company));

        assertEquals("Database connection failed", ex.getMessage());
    }
}
