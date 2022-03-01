package com.example.demo.service;

import com.example.demo.model.ThrottlingGauge;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.ConcurrentHashMap;

public class ThrottlingServiceImpl implements ThrottlingService {
    @Value("${throttling.limit:50}")
    private int throttlingLimit;
    @Value("${throttling.mills:60000}")
    private long throttlingMills;

    private final ConcurrentHashMap<String, ThrottlingGauge> cache = new ConcurrentHashMap<>();

    @Override
    public boolean throttle(String remoteAddr) {
        ThrottlingGauge gauge = cache.getOrDefault(remoteAddr, null);
        if (gauge == null) {
            gauge = new ThrottlingGauge(throttlingLimit, throttlingMills);
            cache.put(remoteAddr, gauge);
        }

        gauge.removeEldest();

        return gauge.throttle();
    }

    @Scheduled(cron = "5 * * * * *")
    private void removeEldest() {
        //removing eldest cache
    }
}