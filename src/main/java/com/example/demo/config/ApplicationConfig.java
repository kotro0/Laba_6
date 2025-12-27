package com.example.demo.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT) // Оставляем: гарантирует, что поля маппятся только при полном совпадении имен
                .setFieldMatchingEnabled(true)                 // Добавляем: позволяет маппить приватные поля напрямую (через рефлексию)
                .setSkipNullEnabled(true)                      // Оставляем: важно для методов update (чтобы не затирать данные null-ами из DTO)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE) // Добавляем: для лучшей работы с инкапсулированными полями JPA
                .setAmbiguityIgnored(true);                    // Добавляем: если вдруг найдутся похожие имена, не будет падать с ошибкой

        return modelMapper;
    }
}