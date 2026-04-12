package com.yuriyuri.interceptors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuriyuri.common.Result;
import com.yuriyuri.entity.User;
import com.yuriyuri.mapper.UserMapper;
import com.yuriyuri.util.JwtUtil;
import com.yuriyuri.util.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //令牌验证
        String token = request.getHeader("Authorization");
        //验证 token
        try {
            Map<String, Object> claims = JwtUtil.parseToken(token);

            // 检查用户是否被封禁
            Long userId = ((Number) claims.get("id")).longValue();
            User user = userMapper.selectById(userId);
            if (user == null || user.getStatus() == 0) {
                // 如果用户不存在或被封禁，返回401和特定消息
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                Result<Object> result = Result.fail(401, user == null ? "用户不存在" : "账号已被封禁");
                response.getWriter().write(objectMapper.writeValueAsString(result));
                return false;
            }

            //把业务数据存储到 ThreadLocal中
            ThreadLocalUtil.set(claims);
            //放行
            return true;
        } catch (Exception e) {
            //http响应状态码为401
            response.setStatus(401);
            //不放行
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //清空ThreadLocal中的数据
        ThreadLocalUtil.remove();
    }
}
