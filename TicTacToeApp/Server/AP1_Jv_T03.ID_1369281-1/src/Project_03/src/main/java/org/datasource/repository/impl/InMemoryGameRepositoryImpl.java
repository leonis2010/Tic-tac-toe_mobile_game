package org.datasource.repository.impl;

import org.datasource.model.GameEntity;
import org.datasource.repository.GameRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
@Repository
public class InMemoryGameRepositoryImpl implements GameRepository {

    // Потокобезопасная коллекция для хранения игр
    private final Map<UUID, GameEntity> gameStorage = new ConcurrentHashMap<>();

    @Override
    public GameEntity save(GameEntity gameEntity) {
        if (gameEntity == null) {
            throw new IllegalArgumentException("Game entity cannot be null");
        }
        if (gameEntity.getId() == null) {
            throw new IllegalArgumentException("Game ID cannot be null");
        }
        System.out.println("=== DEBUG: Saving game to repository - ID: " + gameEntity.getId() +
                ", winner: " + gameEntity.getWinner() + " ===");

        gameStorage.put(gameEntity.getId(), gameEntity);
        return gameEntity;
    }

    @Override
public List<GameEntity> findAll() {
    System.out.println("=== DEBUG: InMemoryGameRepositoryImpl.findAll() called ===");
    // Возвращает список всех GameEntity из хранилища
    List<GameEntity> list = new ArrayList<>(gameStorage.values());
    System.out.println("=== DEBUG: InMemoryGameRepositoryImpl.findAll() returning " + list.size() + " entities ===");
    return list; // ArrayList для потокобезопасности итерации
}

    @Override
    public Optional<GameEntity> findById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }

        GameEntity gameEntity = gameStorage.get(id);

        // 👇 ДОБАВЬТЕ БОЛЕЕ ПОДРОБНОЕ ЛОГИРОВАНИЕ
        System.out.println("=== DEBUG: Retrieving game from repository ===");
        System.out.println("=== DEBUG: - ID: " + id);
        System.out.println("=== DEBUG: - Winner in storage: " + (gameEntity != null ? gameEntity.getWinner() : "GAME NOT FOUND"));
        System.out.println("=== DEBUG: - Status in storage: " + (gameEntity != null ? gameEntity.getStatus() : "GAME NOT FOUND"));
        System.out.println("=== DEBUG: - Total games in storage: " + gameStorage.size());

        // 👇 ДОПОЛНИТЕЛЬНО: выведите все ID в хранилище для диагностики
        if (gameEntity == null) {
            System.out.println("=== DEBUG: Available game IDs in storage: " + gameStorage.keySet());
        }

        return Optional.ofNullable(gameEntity);
    }
    public void deleteById(UUID id) {
        if (id != null) {
            gameStorage.remove(id);
        }
    }

    public boolean existsById(UUID id) {
        if (id == null) {
            return false;
        }
        return gameStorage.containsKey(id);
    }
}