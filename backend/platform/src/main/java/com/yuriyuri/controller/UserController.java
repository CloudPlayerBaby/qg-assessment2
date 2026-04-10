package com.yuriyuri.controller;

import com.yuriyuri.common.Result;
import com.yuriyuri.dto.user.LoginRequest;
import com.yuriyuri.dto.user.LoginResponse;
import com.yuriyuri.dto.user.PasswordUpdateRequest;
import com.yuriyuri.dto.user.RegisterRequest;
import com.yuriyuri.dto.user.UserUpdateRequest;
import com.yuriyuri.entity.User;
import com.yuriyuri.service.UserService;
import com.yuriyuri.util.ThreadLocalUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
@Tag(name = "用户管理", description = "用户注册、登录等接口")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<Void> register(@Valid @RequestBody RegisterRequest registerRequest) {
        userService.register(registerRequest);
        return Result.success();
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = userService.login(loginRequest);
        return Result.success(response);
    }

    @PatchMapping("/profile")
    @Operation(summary = "更新个人信息")
    public Result<Void> updateProfile(@Valid @RequestBody UserUpdateRequest userUpdateRequest) {
        // 从 token 获取当前用户 id
        Map<String,Object> map = ThreadLocalUtil.get();
        Long id = ((Number) map.get("id")).longValue();
        userService.updateUser(id, userUpdateRequest);
        return Result.success();
    }

    @GetMapping("/info")
    @Operation(summary = "获取用户信息")
    public Result<User> getUserInfo() {
        Map<String,Object> map = ThreadLocalUtil.get();
        Long id = ((Number) map.get("id")).longValue();
        User user = userService.getUserInfo(id);
        return Result.success(user);
    }

    @PatchMapping("/password")
    @Operation(summary = "修改密码")
    public Result<Void> updatePassword(@Valid @RequestBody PasswordUpdateRequest passwordUpdateRequest) {
        Map<String,Object> map = ThreadLocalUtil.get();
        Long id = ((Number) map.get("id")).longValue();
        userService.updatePassword(id, passwordUpdateRequest);
        return Result.success();
    }

}

