package com.example.demo.service;

public interface ThrottlingService {
    boolean throttle(String remoteAddr);
}
