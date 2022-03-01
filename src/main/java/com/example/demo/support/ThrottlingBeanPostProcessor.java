package com.example.demo.support;

import com.example.demo.annotation.Throttling;
import com.example.demo.exception.ThrottlingException;
import com.example.demo.service.ThrottlingEvaluator;
import com.example.demo.service.ThrottlingService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Controller;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

public class ThrottlingBeanPostProcessor implements BeanPostProcessor {
    private final ThrottlingEvaluator throttlingEvaluator;
    private final ThrottlingService throttlingService;
    private final Map<String, Class> beanNamesToOriginalClasses;

    public ThrottlingBeanPostProcessor(ThrottlingEvaluator throttlingEvaluator, ThrottlingService throttlingService) {
        this.throttlingEvaluator = throttlingEvaluator;
        this.throttlingService = throttlingService;
        this.beanNamesToOriginalClasses = new HashMap<>();
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        for (Annotation annotation : bean.getClass().getAnnotations()) {
            if (annotation instanceof Controller || annotation instanceof RestController) {
                return bean;
            }
        }

        for (Method method : bean.getClass().getMethods()) {
            Throttling annotation = method.getAnnotation(Throttling.class);

            if (annotation != null) {
                beanNamesToOriginalClasses.put(beanName, bean.getClass());
                break;
            }
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> clazz = beanNamesToOriginalClasses.get(beanName);
        if (clazz == null) {
            return bean;
        }

        return Proxy.newProxyInstance(clazz.getClassLoader(), clazz.getInterfaces(), (proxy, method, args) -> {
            Throttling annotation = findAnnotation(clazz.getMethod(method.getName(), method.getParameterTypes()),
                Throttling.class);

            if (annotation != null) {
                String remoteAddr = throttlingEvaluator.evaluate();
                boolean isAllowed = throttlingService.throttle(remoteAddr);

                if (!isAllowed) {
                    throw new ThrottlingException();
                }
            }

            return ReflectionUtils.invokeMethod(method, bean, args);
        });
    }
}
