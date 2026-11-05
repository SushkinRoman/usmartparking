package com.example.controller;

import com.example.model.User;
import com.example.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> payload) {
        User user = authService.register(
                payload.get("nickname"),
                payload.get("email"),
                payload.get("password"),
                payload.get("role")
        );
        if (user != null) {
            return Map.of("success", true, "message", "Регистрация успешна");
        }
        return Map.of("success", false, "message", "Email уже занят");
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> payload) {
        User user = authService.login(payload.get("email"), payload.get("password"));
        if (user != null) {
            return Map.of("success", true, "role", user.getRole(), "nickname", user.getNickname());
        }
        return Map.of("success", false, "message", "Неверный email или пароль");
    }
}