package kz.wonder.wonderuserrepository.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

@Aspect
@Slf4j
@Component
public class ControllerLoggingAspect {

    @Before("execution(* kz.wonder.wonderuserrepository.controllers..*(..))")
    public void logControllerMethodCall(JoinPoint joinPoint) {
        String url = getCurrentRequestUrl();
        String token = getCurrentRequestToken();
        Map<String, String[]> parameters = getCurrentRequestParameters();

        log.info("url: {}, method in controller: {}, parameters: {},  user: {}", url, joinPoint.getSignature().toShortString(), parameters.toString(), token);
    }

//    @AfterReturning(pointcut = "execution(* kz.wonder.wonderuserrepository.controllers.*(..))", returning = "result")
//    public void logControllerMethodReturn(JoinPoint joinPoint, Object result) {
//        String url = getCurrentRequestUrl();
//        String token = getCurrentRequestToken();
//
//        log.info("url: {}, method in controller: {}, user: {}, result: {}", url, joinPoint.getSignature().toShortString(), token, result);
//    }

    private Map<String, String[]> getCurrentRequestParameters() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert requestAttributes != null;
        HttpServletRequest request = requestAttributes.getRequest();
        return request.getParameterMap();
    }

    private String getCurrentRequestUrl() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert requestAttributes != null;
        HttpServletRequest request = requestAttributes.getRequest();
        return request.getRequestURI();
    }

    private String getCurrentRequestToken() {
        var tokenAuth = SecurityContextHolder.getContext().getAuthentication();
        if(tokenAuth == null || tokenAuth.isAuthenticated()) return "Anonymous";
        var jwt = (JwtAuthenticationToken) tokenAuth;
        return jwt.getName();
    }
}