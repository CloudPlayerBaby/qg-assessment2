package com.yuriyuri.service;

import com.yuriyuri.dto.user.LoginRequest;
import com.yuriyuri.dto.user.LoginResponse;
import com.yuriyuri.dto.user.PasswordUpdateRequest;
import com.yuriyuri.dto.user.RegisterRequest;
import com.yuriyuri.dto.user.UserUpdateRequest;
import com.yuriyuri.entity.User;

public interface UserService {
    void register(RegisterRequest registerRequest);
    LoginResponse login(LoginRequest loginRequest);
    User getUserInfo(Long userId);
    void updateUser(Long userId, UserUpdateRequest req);
    void updatePassword(Long userId, PasswordUpdateRequest req);
}
