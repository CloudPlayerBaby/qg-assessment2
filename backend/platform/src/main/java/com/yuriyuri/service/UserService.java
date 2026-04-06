package com.yuriyuri.service;

import com.yuriyuri.dto.LoginRequest;
import com.yuriyuri.dto.LoginResponse;
import com.yuriyuri.dto.RegisterRequest;

public interface UserService {
    void register(RegisterRequest registerRequest);
    LoginResponse login(LoginRequest loginRequest);
}
