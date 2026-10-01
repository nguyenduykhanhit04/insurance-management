package com.training.insurance.service;

import com.training.insurance.entity.User;
import com.training.insurance.repository.UserRepository;
import com.training.insurance.service.impl.AuthServiceImpl;
import com.training.insurance.util.MD5Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * UNIT TEST cho AuthServiceImpl
 *
 * @ExtendWith(MockitoExtension.class) → Bật Mockito, không cần Spring context
 * @Mock                               → Tạo object giả (không gọi DB thật)
 * @InjectMocks                        → Tạo instance thật, inject mock vào
 * when().thenReturn()                 → "Lập trình" hành vi cho mock
 * verify() / never()                  → Kiểm tra mock có được gọi không
 * verifyNoInteractions()              → Đảm bảo mock không bị gọi bất kỳ method nào
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl - Unit Tests")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    private User mockUser;
    private static final String VALID_USERNAME = "admin";
    private static final String VALID_PASSWORD = "admin";

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .userInternalId(1)
                .username(VALID_USERNAME)
                .password(MD5Util.md5(VALID_PASSWORD))
                .userFullName("Admin User")
                .userSexDivision("01")
                .build();
    }

    @Test
    @DisplayName("Login thành công với username và password hợp lệ")
    void login_validCredentials_returnsUser() {
        String hashedPassword = MD5Util.md5(VALID_PASSWORD);
        when(userRepository.findByUsernameAndPassword(VALID_USERNAME, hashedPassword))
                .thenReturn(Optional.of(mockUser));

        User result = authService.login(VALID_USERNAME, VALID_PASSWORD);

        assertNotNull(result);
        assertEquals(VALID_USERNAME, result.getUsername());
        verify(userRepository, times(1)).findByUsernameAndPassword(VALID_USERNAME, hashedPassword);
    }

    @Test
    @DisplayName("Login với username có khoảng trắng thừa vẫn thành công (trim)")
    void login_usernameWithWhitespace_trimsAndReturnsUser() {
        String hashedPassword = MD5Util.md5(VALID_PASSWORD);
        when(userRepository.findByUsernameAndPassword(VALID_USERNAME, hashedPassword))
                .thenReturn(Optional.of(mockUser));

        assertNotNull(authService.login("  admin  ", VALID_PASSWORD));
        verify(userRepository).findByUsernameAndPassword(VALID_USERNAME, hashedPassword);
    }

    @Test
    @DisplayName("Login thất bại khi sai password → trả về null")
    void login_wrongPassword_returnsNull() {
        String wrongHashedPassword = MD5Util.md5("wrongpassword");
        when(userRepository.findByUsernameAndPassword(VALID_USERNAME, wrongHashedPassword))
                .thenReturn(Optional.empty());

        assertNull(authService.login(VALID_USERNAME, "wrongpassword"));
    }

    @Test
    @DisplayName("Login thất bại khi username null → trả về null, không gọi DB")
    void login_nullUsername_returnsNullWithoutCallingDB() {
        assertNull(authService.login(null, VALID_PASSWORD));
        verify(userRepository, never()).findByUsernameAndPassword(anyString(), anyString());
    }

    @Test
    @DisplayName("Login thất bại khi password null → trả về null, không gọi DB")
    void login_nullPassword_returnsNullWithoutCallingDB() {
        assertNull(authService.login(VALID_USERNAME, null));
        verify(userRepository, never()).findByUsernameAndPassword(anyString(), anyString());
    }

    @Test
    @DisplayName("Login thất bại khi username rỗng → trả về null, không gọi DB")
    void login_emptyUsername_returnsNullWithoutCallingDB() {
        assertNull(authService.login("   ", VALID_PASSWORD));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Login thất bại khi username không tồn tại → trả về null")
    void login_nonExistentUsername_returnsNull() {
        String hashedPassword = MD5Util.md5(VALID_PASSWORD);
        when(userRepository.findByUsernameAndPassword("unknownuser", hashedPassword))
                .thenReturn(Optional.empty());

        assertNull(authService.login("unknownuser", VALID_PASSWORD));
        verify(userRepository).findByUsernameAndPassword("unknownuser", hashedPassword);
    }
}
