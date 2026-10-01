package com.training.insurance.service.impl;

import com.training.insurance.entity.User;
import com.training.insurance.repository.UserRepository;
import com.training.insurance.service.AuthService;
import com.training.insurance.util.MD5Util;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public User login(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return null;
        }

        String hashedPassword = MD5Util.md5(password.trim());
        return userRepository.findByUsernameAndPassword(username.trim(), hashedPassword).orElse(null);
    }
}
