package org.di;

import org.datasource.repository.GameRepository;
import org.datasource.repository.impl.InMemoryGameRepositoryImpl;
import org.domain.service.GameService;
import org.domain.service.impl.GameServiceImplWithRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GameConfiguration {
// Дебаг
//    @Bean
//    public GameRepository gameRepository() {
//        return new InMemoryGameRepositoryImpl();
//    }
//
//    @Bean
//    public GameService gameService(GameRepository gameRepository) {
//        return new GameServiceImplWithRepository(gameRepository);
//    }
}