package com.yuriyuri.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuriyuri.common.BusinessException;
import com.yuriyuri.dto.LoginRequest;
import com.yuriyuri.dto.LoginResponse;
import com.yuriyuri.dto.RegisterRequest;
import com.yuriyuri.entity.User;
import com.yuriyuri.mapper.UserMapper;
import com.yuriyuri.service.UserService;
import com.yuriyuri.util.JwtUtil;
import com.yuriyuri.util.Md5Util;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public void register(RegisterRequest registerRequest) {
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new BusinessException("两次密码输入不一致");
        }

        if (registerRequest.getPassword().length() < 6 || registerRequest.getPassword().length() > 20) {
            throw new BusinessException("密码长度应在6-20位之间");
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, registerRequest.getEmail())
                .or()
                .eq(User::getPhoneNumber, registerRequest.getPhoneNumber());
        User existUser = userMapper.selectOne(wrapper);
        if (existUser != null) {
            throw new BusinessException("邮箱或手机号已被注册");
        }

        User user = new User();
        BeanUtils.copyProperties(registerRequest, user);
        user.setPassword(Md5Util.encrypt(registerRequest.getPassword()));
        user.setNickname(registerRequest.getUsername());
        user.setIdentity(0);
        user.setStatus(1);

        userMapper.insert(user);
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, loginRequest.getAccount())
                .or()
                .eq(User::getPhoneNumber, loginRequest.getAccount());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException("账号或密码错误");
        }

        if (!Md5Util.encrypt(loginRequest.getPassword()).equals(user.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }

        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被封禁");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("username", user.getUsername());
        String token = JwtUtil.genToken(claims);

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setNickname(user.getNickname());
        userInfo.setEmail(user.getEmail());
        userInfo.setPhoneNumber(user.getPhoneNumber());
        userInfo.setAvatarUrl(user.getAvatarUrl());
        userInfo.setIdentity(user.getIdentity());

        return new LoginResponse(token, userInfo);
    }
}

