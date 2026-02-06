package com.research.common.core.aspect;

import com.research.common.core.annotation.Log;
import com.research.common.core.entity.SysOperationLog;
import com.research.common.core.mapper.SysOperationLogMapper;
import com.research.common.core.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
public class LogAspect {

    @Autowired(required = false)
    private SysOperationLogMapper operationLogMapper;

    @Around("@annotation(controllerLog)")
    public Object around(ProceedingJoinPoint joinPoint, Log controllerLog) throws Throwable {
        SysOperationLog operationLog = new SysOperationLog();
        operationLog.setTitle(controllerLog.title());
        operationLog.setType(controllerLog.businessType() != null ? controllerLog.businessType().name() : "OTHER");
        try {
            Long userId = SecurityUtils.getUserId();
            operationLog.setUserId(userId);
        } catch (Exception e) {
            operationLog.setUserId(null);
        }
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                if (request != null) {
                    operationLog.setUrl(request.getRequestURI());
                    operationLog.setMethod(request.getMethod());
                    operationLog.setIp(getClientIp(request));
                }
            }
        } catch (Exception e) {
            log.debug("get request info fail: {}", e.getMessage());
        }
        try {
            Object result = joinPoint.proceed();
            operationLog.setStatus(1);
            return result;
        } catch (Throwable e) {
            operationLog.setStatus(0);
            operationLog.setErrorMsg(e.getMessage() != null ? e.getMessage().length() > 500 ? e.getMessage().substring(0, 500) : e.getMessage() : null);
            throw e;
        } finally {
            operationLog.setCreateTime(LocalDateTime.now());
            if (operationLogMapper != null) {
                try {
                    operationLogMapper.insert(operationLog);
                } catch (Exception e) {
                    log.warn("insert operation log fail: {}", e.getMessage());
                }
            }
        }
    }

    private static String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
