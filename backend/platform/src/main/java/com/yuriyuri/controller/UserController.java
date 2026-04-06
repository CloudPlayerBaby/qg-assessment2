package com.yuriyuri.controller;

import com.yuriyuri.common.Result;
import com.yuriyuri.dto.LoginRequest;
import com.yuriyuri.dto.LoginResponse;
import com.yuriyuri.dto.RegisterRequest;
import com.yuriyuri.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@CrossOrigin
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
}

