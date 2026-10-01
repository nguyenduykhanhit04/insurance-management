package com.training.insurance.controller;

import com.training.insurance.entity.User;
import com.training.insurance.interceptor.AuthInterceptor;
import com.training.insurance.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CONTROLLER TEST cho AuthController - dùng MockMvc
 *
 * @WebMvcTest(AuthController.class)
 *   → Chỉ load Spring MVC layer (Controller, Filter, Interceptor)
 *   → KHÔNG load Service, Repository, Database
 *   → Nhanh hơn @SpringBootTest rất nhiều
 *
 * MockMvc: Giả lập HTTP request/response mà không cần server thật
 *
 * @MockBean: Tạo mock và đăng ký nó vào Spring context
 *   (khác @Mock của Mockito thuần - dùng khi cần Spring context biết về mock)
 */
@WebMvcTest(AuthController.class)
@DisplayName("AuthController - Controller Tests (MockMvc)")
class AuthControllerTest {

    /**
     * MockMvc: Công cụ test HTTP request/response
     * Spring tự inject vào khi dùng @WebMvcTest
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * @MockBean: Tạo mock bean và đưa vào Spring ApplicationContext
     * Controller cần AuthService → phải mock nó
     */
    @MockBean
    private AuthService authService;

    /**
     * AuthInterceptor chặn /insurance/** → cần mock để tránh ảnh hưởng test controller khác
     * Ở đây @WebMvcTest(AuthController.class) nên interceptor vẫn được load
     */
    @MockBean
    private AuthInterceptor authInterceptor;

    // =========================================================
    // NHÓM TEST: GET /login
    // =========================================================

    @Test
    @DisplayName("GET /login không có session → hiển thị trang login")
    void getLogin_noSession_returnsLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())          // HTTP 200
                .andExpect(view().name("login"));    // trả về template "login"
    }

    @Test
    @DisplayName("GET /login khi đã login → redirect về /insurance")
    void getLogin_withActiveSession_redirectsToInsurance() throws Exception {
        // Tạo mock session có LOGIN_USER
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("LOGIN_USER", User.builder().username("admin").build());

        mockMvc.perform(get("/login").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/insurance"));
    }

    // =========================================================
    // NHÓM TEST: POST /login
    // =========================================================

    @Test
    @DisplayName("POST /login - username rỗng → trả về login view với error message")
    void postLogin_emptyUsername_returnsLoginWithError() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "")
                        .param("password", "admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Hãy nhập Tên đăng nhập!"));

        // AuthService không được gọi khi validation fail
        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("POST /login - password rỗng → trả về login view với error message")
    void postLogin_emptyPassword_returnsLoginWithError() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "admin")
                        .param("password", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("error", "Hãy nhập Mật khẩu!"))
                .andExpect(model().attribute("username", "admin")); // username được giữ lại

        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("POST /login - sai tài khoản → trả về login view với error message")
    void postLogin_wrongCredentials_returnsLoginWithError() throws Exception {
        // ARRANGE - AuthService trả về null (login thất bại)
        when(authService.login("admin", "wrong")).thenReturn(null);

        // ACT + ASSERT
        mockMvc.perform(post("/login")
                        .param("username", "admin")
                        .param("password", "wrong"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("error", "Tên đăng nhập hoặc Mật khẩu không đúng!"))
                .andExpect(model().attribute("username", "admin"));

        verify(authService, times(1)).login("admin", "wrong");
    }

    @Test
    @DisplayName("POST /login - đúng tài khoản → redirect về /insurance")
    void postLogin_validCredentials_redirectsToInsurance() throws Exception {
        // ARRANGE - AuthService trả về user hợp lệ
        User loggedInUser = User.builder()
                .userInternalId(1)
                .username("admin")
                .userFullName("Admin")
                .build();
        when(authService.login("admin", "admin")).thenReturn(loggedInUser);

        // ACT + ASSERT
        mockMvc.perform(post("/login")
                        .param("username", "admin")
                        .param("password", "admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/insurance"));

        verify(authService, times(1)).login("admin", "admin");
    }

    // =========================================================
    // NHÓM TEST: GET /logout
    // =========================================================

    @Test
    @DisplayName("GET /logout → session bị hủy → redirect về /login")
    void logout_withSession_invalidatesSessionAndRedirects() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("LOGIN_USER", User.builder().username("admin").build());

        mockMvc.perform(get("/logout").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("GET / → redirect về /login")
    void home_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}
