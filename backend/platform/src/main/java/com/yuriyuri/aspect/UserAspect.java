package com.yuriyuri.aspect;

import com.yuriyuri.annotation.RequireIdentity;
import com.yuriyuri.common.BusinessException;
import com.yuriyuri.constant.user.UserIdentity;
import com.yuriyuri.util.ThreadLocalUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;

@Aspect
@Component
public class UserAspect {
    @Around("@within(requireIdentity) || @annotation(requireIdentity)")
    public Object check(ProceedingJoinPoint point, RequireIdentity requireIdentity) throws Throwable {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Integer identity = (Integer) claims.get("identity");

        if (identity == null) {
            throw new BusinessException("身份信息缺失");
        }

        String identityName = identity == UserIdentity.ADMIN ? "admin" : "user";

        // 检查是否匹配指定角色
        boolean hasPermission = Arrays.stream(requireIdentity.value())
                .anyMatch(role -> role.equals(identityName));

        if (!hasPermission) {
            throw new BusinessException("权限不足");
        }

        return point.proceed();
    }
}