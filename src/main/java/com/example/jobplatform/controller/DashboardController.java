package com.example.jobplatform.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/")
    public String dashboard() {
        // This will look for src/main/resources/static/index.html 
        // because of the WebConfig forward or standard Spring Boot static handling
        return "forward:/index.html";
    }
}
