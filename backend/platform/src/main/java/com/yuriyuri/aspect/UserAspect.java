package com.yuriyuri.aspect;

import com.yuriyuri.annotation.RequireIdentity;
import com.yuriyuri.common.BusinessException;
import com.yuriyuri.constant.user.UserIdentity;
import com.yuriyuri.util.ThreadLocalUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

@Aspect
@Component
public class UserAspect {

    @Around("@within(com.yuriyuri.annotation.RequireIdentity) || @annotation(com.yuriyuri.annotation.RequireIdentity)")
    public Object check(ProceedingJoinPoint point) throws Throwable {
        Map<String, Object> claims = ThreadLocalUtil.get();

        if (claims == null || claims.isEmpty()) {
            throw new BusinessException("用户未登录");
        }

        Object identityObj = claims.get("identity");

        if (identityObj == null) {
            throw new BusinessException("身份信息缺失");
        }

        // 安全转换：JWT 解析后可能是 Integer 或 Long
        int identity = ((Number) identityObj).intValue();
        String identityName = identity == UserIdentity.ADMIN ? "admin" : "user";

        // 获取注解（优先方法级别，其次类级别）
        RequireIdentity requireIdentity = getAnnotation(point);
        if (requireIdentity == null) {
            return point.proceed();
        }

        // 检查是否匹配指定角色
        boolean hasPermission = Arrays.stream(requireIdentity.value())
                .anyMatch(role -> role.equals(identityName));

        if (!hasPermission) {
            throw new BusinessException("权限不足");
        }

        return point.proceed();
    }

    /**
     * 获取 RequireIdentity 注解（优先方法级别，其次类级别）
     */
    private RequireIdentity getAnnotation(ProceedingJoinPoint point) {
        // 先尝试从方法获取
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        RequireIdentity annotation = method.getAnnotation(RequireIdentity.class);
        if (annotation != null) {
            return annotation;
        }
        // 再从类获取
        return point.getTarget().getClass().getAnnotation(RequireIdentity.class);
    }
}