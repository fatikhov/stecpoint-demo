package com.example.demo.config;

import com.example.demo.service.ThrottlingEvaluator;
import com.example.demo.service.ThrottlingEvaluatorImpl;
import com.example.demo.service.ThrottlingService;
import com.example.demo.service.ThrottlingServiceImpl;
import com.example.demo.support.ThrottlingBeanPostProcessor;
import com.example.demo.support.ThrottlingInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnClass(ThrottlingBeanPostProcessor.class)
public class ThrottlingConfig {
    @Bean
    @ConditionalOnMissingBean
    public ThrottlingBeanPostProcessor throttlingBeanPostProcessor() {
        return new ThrottlingBeanPostProcessor(throttlingEvaluator(), throttlingService());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnWebApplication
    public ThrottlingInterceptor throttlingInterceptor() {
        return new ThrottlingInterceptor(throttlingEvaluator(), throttlingService());
    }

    @Bean
    @ConditionalOnWebApplication
    public WebMvcConfigurer interceptorAdapter() {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(throttlingInterceptor());
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public ThrottlingEvaluator throttlingEvaluator() {
        return new ThrottlingEvaluatorImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public ThrottlingService throttlingService() {
        return new ThrottlingServiceImpl();
    }

}
