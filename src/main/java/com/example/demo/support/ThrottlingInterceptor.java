package com.example.demo.support;

import com.example.demo.annotation.Throttling;
import com.example.demo.exception.ThrottlingException;
import com.example.demo.service.ThrottlingEvaluator;
import com.example.demo.service.ThrottlingService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ThrottlingInterceptor implements HandlerInterceptor {
    private final Log logger = LogFactory.getLog(getClass());
    private final ThrottlingEvaluator throttlingEvaluator;
    private final ThrottlingService throttlingService;

    public ThrottlingInterceptor(ThrottlingEvaluator throttlingEvaluator, ThrottlingService throttlingService) {
        this.throttlingEvaluator = throttlingEvaluator;
        this.throttlingService = throttlingService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Throttling annotation = handlerMethod.getMethod().getAnnotation(Throttling.class);

            if (annotation != null) {
                String remoteAddr = throttlingEvaluator.evaluate();
                boolean isHandlingAllowed = throttlingService.throttle(remoteAddr);

                if (!isHandlingAllowed) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("cannot proceed with a handling http request [" + request.getRequestURI() + "] " +
                            "due to @Throttling configuration, value=" + remoteAddr);
                    }
                    throw new ThrottlingException();
                }
            }
        }

        return true;
    }
}