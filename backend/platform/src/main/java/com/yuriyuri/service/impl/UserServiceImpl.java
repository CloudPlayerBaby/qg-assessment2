package com.yuriyuri.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yuriyuri.common.BusinessException;
import com.yuriyuri.constant.UserIdentity;
import com.yuriyuri.dto.user.LoginRequest;
import com.yuriyuri.dto.user.LoginResponse;
import com.yuriyuri.dto.user.PasswordUpdateRequest;
import com.yuriyuri.dto.user.RegisterRequest;
import com.yuriyuri.dto.user.UserUpdateRequest;
import com.yuriyuri.entity.User;
import com.yuriyuri.mapper.UserMapper;
import com.yuriyuri.service.UserService;
import com.yuriyuri.util.JwtUtil;
import com.yuriyuri.util.Md5Util;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 注册
     * @param registerRequest
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterRequest registerRequest) {
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new BusinessException("两次密码输入不一致");
        }

        if (registerRequest.getPassword().length() < 6 || registerRequest.getPassword().length() > 20) {
            throw new BusinessException("密码长度应在6-20位之间");
        }

        //mp的条件构造器，可以抵御sql注入
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        //相当于select * from sys_user where email=? or phone_number=?
        wrapper.eq(User::getEmail, registerRequest.getEmail())
                .or()
                .eq(User::getPhoneNumber, registerRequest.getPhoneNumber());
        User existUser = userMapper.selectOne(wrapper);
        if (existUser != null) {
            throw new BusinessException("邮箱或手机号已被注册");
        }

        //这里需要封装用户
        User user = new User();
        //registerRequest有username、email、phoneNumber、password，从中拷贝过来，不需要重复写封装逻辑
        BeanUtils.copyProperties(registerRequest, user);
        //但是密码还是要md5加密，所以重新设置一下
        user.setPassword(Md5Util.encrypt(registerRequest.getPassword()));
        //昵称默认是用户名
        user.setNickname(registerRequest.getUsername());
        //默认是普通用户
        user.setIdentity(UserIdentity.NORMAL_USER);
        user.setStatus(1);

        userMapper.insert(user);
    }

    /**
     * 登录
     * @param loginRequest
     * @return
     */
    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        //构造一个条件，可以输入用户名、手机号或者邮箱号作为登陆手段
        wrapper.eq(User::getEmail, loginRequest.getAccount())
                .or()
                .eq(User::getPhoneNumber, loginRequest.getAccount());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException("账号或密码错误");
        }

        //md5加密
        if (!Md5Util.encrypt(loginRequest.getPassword()).equals(user.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }

        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被封禁");
        }

        //登陆时生成token，token包含id和username
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("username", user.getUsername());
        String token = JwtUtil.genToken(claims);

        //创建一个对象，它是LoginResponse里的UserInfo对象（lombok多层嵌套）
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setNickname(user.getNickname());
        userInfo.setEmail(user.getEmail());
        userInfo.setPhoneNumber(user.getPhoneNumber());
        userInfo.setAvatarUrl(user.getAvatarUrl());
        userInfo.setIdentity(user.getIdentity());

        //返回给controller层，结果如何前端可以直接看到（这端是ai写的，我觉得没必要）
        return new LoginResponse(token, userInfo);
    }

    /**
     * 获取用户信息
     * @param userId
     * @return
     */
    @Override
    public User getUserInfo(Long userId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getId, userId);
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new BusinessException("账号不存在");
        }
        return user;
    }

    /**
     * 修改个人信息
     * @param userId
     * @param req
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long userId,UserUpdateRequest req) {
        //简单校验一下用户是否存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getId, userId);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new BusinessException("账号不存在");
        }

        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();

        //mp的语法，eq是条件，即id=?；set是设值，第一个形参是条件，之后是set (?) values (?)
        updateWrapper.
                eq(User::getId, user.getId()).
                set(req.getNickname()!=null,User::getNickname,req.getNickname()).
                set(req.getPhoneNumber()!=null,User::getPhoneNumber,req.getPhoneNumber()).
                set(req.getAvatarUrl()!=null,User::getAvatarUrl,req.getAvatarUrl());

        // 如果没有任何字段需要更新（即三个 set 的条件都为 false）
        if (updateWrapper.getSqlSet() == null || updateWrapper.getSqlSet().isEmpty()) {
            throw new BusinessException("信息未更新");
        }
        userMapper.update(null,updateWrapper);
    }

    /**
     * 修改密码
     * @param userId
     * @param req
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Long userId, PasswordUpdateRequest req) {
        // 校验两次密码是否一致
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new BusinessException("两次密码输入不一致");
        }

        // 校验密码长度
        if (req.getNewPassword().length() < 6 || req.getNewPassword().length() > 20) {
            throw new BusinessException("密码长度应在6-20位之间");
        }

        // 查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getId, userId);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new BusinessException("账号不存在");
        }

        // 校验原密码
        if (!Md5Util.encrypt(req.getOldPassword()).equals(user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        // 更新密码
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, userId)
                .set(User::getPassword, Md5Util.encrypt(req.getNewPassword()));
        userMapper.update(null, updateWrapper);
    }
}

