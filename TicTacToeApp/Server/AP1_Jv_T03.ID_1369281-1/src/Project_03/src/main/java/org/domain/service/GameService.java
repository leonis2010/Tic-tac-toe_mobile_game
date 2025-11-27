package org.domain.service;

import org.domain.model.Game;
import org.domain.model.GameBoard;

import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GameService {

    /**
     * Получение следующего хода алгоритмом Минимакс
     * @param game текущая игра
     * @return точка с координатами следующего хода
     */
    Point getNextMoveByMinimax(Game game);

    /**
     * Валидация игрового поля (проверка, что не изменены предыдущие ходы)
     * @param currentGame текущая игра
     * @param originalBoard оригинальное игровое поле для сравнения
     * @return true, если валидация пройдена успешно
     */
    boolean validateGameBoard(Game currentGame, GameBoard originalBoard);

    /**
     * Проверка окончания игры
     * @param game текущая игра
     * @return 1 - победили крестики, -1 - победили нолики, 0 - ничья, 2 - игра продолжается
     */
    int checkGameEnd(Game game);
    
    /**
     * Сохраняет игру в репозиторий
     * @param game игра для сохранения
     */
    void saveGame(Game game);
    
    /**
     * Получает игру из репозитория по ID
     * @param gameId ID игры
     * @return Optional с игрой
     */
    Optional<Game> findGameById(UUID gameId);

    /**
 * Получить все игры.
 * @return Список всех игр.
 */
List<Game> getAllGames();
}