package com.example.service;

import com.example.model.User;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.*;

@Service
public class AuthService {
    private final List<User> users = new ArrayList<>();

    @PostConstruct
    public void initTestUser() {
        // Тестовый пользователь
        users.add(new User("admin", "admin@test.com", "admin123", "АДМИНИСТРАТОР"));
        users.add(new User("driver", "driver@test.com", "driver123", "ВОДИТЕЛЬ"));
    }

    public User register(String nickname, String email, String password, String role) {
        if (users.stream().anyMatch(u -> u.getEmail().equals(email))) {
            return null; // Email занят
        }
        User newUser = new User(nickname, email, password, role);
        users.add(newUser);
        return newUser;
    }

    public User login(String email, String password) {
        return users.stream()
                .filter(u -> u.getEmail().equals(email) && u.getPassword().equals(password))
                .findFirst()
                .orElse(null);
    }
}