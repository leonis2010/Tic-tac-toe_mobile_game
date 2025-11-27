package org.datasource.repository;

import org.datasource.model.GameEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GameRepository {

    /**
     * Сохранение текущей игры
     * @param gameEntity игра для сохранения
     * @return сохраненная игра
     */
    GameEntity save(GameEntity gameEntity);

    /**
     * Получение текущей игры по ID
     * @param id идентификатор игры
     * @return Optional с игрой или пустой, если игра не найдена
     */
    Optional<GameEntity> findById(UUID id);

    /**
 * Найти все игры.
 * @return Список всех GameEntity.
 */
List<GameEntity> findAll(); 
}