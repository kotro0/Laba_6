package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_sessions")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSession {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String userEmail;
    private String deviceId;

    @Column(length = 1000)
    private String refreshToken;

    private Instant refreshTokenExpiry;

    // Поле для истории: когда была создана эта версия токена
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Enumerated(EnumType.STRING)
    private SessionStatus status; // ACTIVE, USED, REVOKED
}