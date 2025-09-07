package com.study.aiagent.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查类
 */
@RestController
@RequestMapping("/healthcheck")
public class HealthCheckController {

    @GetMapping
    public String healthCheck(){
        return "ok";
    }
}
