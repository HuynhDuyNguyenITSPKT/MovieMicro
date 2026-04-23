package com.movie.micro.user_service.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api/users")
public class UserController {
    @GetMapping("/test")
    public String getMethodName() {
        return "Hello from User Service!";
    }
    
}
