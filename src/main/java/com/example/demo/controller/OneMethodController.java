package com.example.demo.controller;

import com.example.demo.annotation.Throttling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OneMethodController {
    @Throttling
    @GetMapping("/one-method")
    public String oneMethod() {
        return "";
    }
}
