package com.training.insurance.controller;

import com.training.insurance.entity.User;
import com.training.insurance.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginView(HttpSession session) {
        if (session != null && session.getAttribute("LOGIN_USER") != null) {
            return "redirect:/insurance";
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String password,
            Model model,
            HttpSession session
    ) {
        if (username == null || username.trim().isEmpty()) {
            model.addAttribute("error", "Hãy nhập Tên đăng nhập!");
            return "login";
        }

        if (password == null || password.trim().isEmpty()) {
            model.addAttribute("error", "Hãy nhập Mật khẩu!");
            model.addAttribute("username", username);
            return "login";
        }

        User user = authService.login(username, password);
        if (user == null) {
            model.addAttribute("error", "Tên đăng nhập hoặc Mật khẩu không đúng!");
            model.addAttribute("username", username);
            return "login";
        }

        session.setAttribute("LOGIN_USER", user);
        return "redirect:/insurance";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login";
    }
}
