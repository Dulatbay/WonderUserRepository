package kz.wonder.wonderuserrepository.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

@Aspect
@Slf4j
@Component
public class ControllerLoggingAspect {

	@Before("execution(* kz.wonder.wonderuserrepository.controllers..*(..))")
	public void logControllerMethodCall(JoinPoint joinPoint) {
		String url = getCurrentRequestUrl();
		String token = getCurrentRequestToken();


		log.info("url: {} by user: {}", url, token);
		log.info("method in controller: {}", joinPoint.getSignature().toShortString());
		log.info("requestBody: {}", Arrays.toString(joinPoint.getArgs()));
	}

	private String getCurrentRequestUrl() {
		ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (requestAttributes == null)
			return "start from method";
		HttpServletRequest request = requestAttributes.getRequest();
		return request.getRequestURI();
	}

	private String getCurrentRequestToken() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication instanceof JwtAuthenticationToken jwtToken) {
			return jwtToken.getName();
		}

		return "Anonymous";
	}
}