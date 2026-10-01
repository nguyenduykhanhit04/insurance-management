package com.training.insurance.service;

import com.training.insurance.entity.User;

public interface AuthService {

    User login(String username, String password);
}
