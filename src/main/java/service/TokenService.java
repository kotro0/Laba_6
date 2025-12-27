package com.example.demo.service;

import com.example.demo.config.JwtTokenProvider;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserSessionRepository sessionRepository;
    private final UserRepository userRepository;

    /**
     * Создает новую сессию (при логине или после успешного refresh)
     * Это работает одинаково для USER и ADMIN
     */
    @Transactional
    public Map<String, String> generateTokens(String username, String deviceId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Помещаем роли в Access Token
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", user.getRoles().stream()
                .map(Role::getName)
                .toList());

        String access = jwtTokenProvider.generateAccessToken(username, claims);
        String refresh = jwtTokenProvider.generateRefreshToken(username);

        // СОЗДАЕМ НОВУЮ ЗАПИСЬ В ИСТОРИИ СЕССИЙ
        UserSession session = UserSession.builder()
                .userEmail(username)
                .deviceId(deviceId)
                .refreshToken(refresh)
                .refreshTokenExpiry(Instant.now().plusMillis(7 * 24 * 60 * 60 * 1000)) // 7 дней
                .status(SessionStatus.ACTIVE)
                .build();

        sessionRepository.save(session);

        return Map.of("accessToken", access, "refreshToken", refresh);
    }

    /**
     * Обновляет токен и сохраняет историю
     */
    @Transactional
    public Map<String, String> refresh(String oldRefreshToken) {
        // 1. Ищем старый токен в истории
        UserSession oldSession = sessionRepository.findByRefreshToken(oldRefreshToken)
                .orElseThrow(() -> new RuntimeException("Токен не найден в базе данных"));

        // 2. Проверка на безопасность (Step 4 и 5 задания)
        if (oldSession.getStatus() != SessionStatus.ACTIVE) {
            // Если кто-то пытается использовать уже "ИСПОЛЬЗОВАННЫЙ" токен — это атака!
            // Помечаем его как REVOKED (скомпрометирован)
            oldSession.setStatus(SessionStatus.REVOKED);
            sessionRepository.save(oldSession);
            throw new RuntimeException("ВНИМАНИЕ: Попытка повторного использования токена! Сессия отозвана.");
        }

        // 3. Помечаем старую запись в истории как USED (использована)
        oldSession.setStatus(SessionStatus.USED);
        sessionRepository.save(oldSession);

        // 4. Генерируем новую пару токенов и создаем НОВУЮ запись в БД (новую строку истории)
        return generateTokens(oldSession.getUserEmail(), oldSession.getDeviceId());
    }
}