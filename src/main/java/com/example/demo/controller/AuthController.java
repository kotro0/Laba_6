package com.example.demo.controller;

import com.example.demo.dto.RegistrationRequest;
import com.example.demo.service.UserService;
import com.example.demo.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody RegistrationRequest request) {
        try {
            // Проверка логина/пароля в БД (работает для всех ролей)
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            // Если ок — создаем сессию в PostgreSQL
            return ResponseEntity.ok(tokenService.generateTokens(request.getUsername(), "Postman-Client"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Ошибка входа: " + e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        try {
            // Обновляем токен и создаем новую запись в истории
            return ResponseEntity.ok(tokenService.refresh(refreshToken));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegistrationRequest request) {
        userService.registerUser(request, false); // Создает ROLE_USER
        return ResponseEntity.ok("Пользователь (USER) зарегистрирован");
    }

    @PostMapping("/register-admin")
    public ResponseEntity<?> registerAdmin(@RequestBody RegistrationRequest request) {
        userService.registerUser(request, true); // Создает ROLE_ADMIN
        return ResponseEntity.ok("Администратор (ADMIN) зарегистрирован");
    }
}