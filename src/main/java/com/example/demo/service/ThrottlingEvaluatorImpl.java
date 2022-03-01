package com.example.demo.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;


public class ThrottlingEvaluatorImpl implements ThrottlingEvaluator {
    private final Log logger = LogFactory.getLog(getClass());

    @Override
    public String evaluate() {
        String value = null;

        HttpServletRequest servletRequest = null;
        try {
            servletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        } catch (IllegalStateException e) {
            if (logger.isErrorEnabled()) {
                logger.error("no RequestAttributes object is bound to the current thread, " +
                    "please check @Throttling configuration.", e);
            }
        }

        if (servletRequest == null) {
            if (logger.isErrorEnabled()) {
                logger.error("cannot find HttpServletRequest in RequestContextHolder while processing @Throttling " +
                    "annotation");
            }
        } else {
            value = servletRequest.getRemoteAddr();
        }

        return value;
    }
}
