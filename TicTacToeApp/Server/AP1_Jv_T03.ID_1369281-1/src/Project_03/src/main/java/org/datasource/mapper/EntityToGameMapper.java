// ./main/java/org/datasource/mapper/EntityToGameMapper.java
package org.datasource.mapper;

import org.datasource.model.GameBoardEntity;
import org.datasource.model.GameEntity;
import org.domain.model.Game;
import org.domain.model.GameBoard;

public class EntityToGameMapper {

    public Game mapToDomain(GameEntity entity) {
        if (entity == null) return null;

        Game game = new Game(entity.getId(), mapBoardToDomain(entity.getBoard()));
        game.setPlayerTurn(entity.isPlayerTurn());
        game.setStatus(entity.getStatus());
        game.setGameType(entity.getGameType());
        game.setCreatorUsername(entity.getCreatorUsername());
        game.setPlayer2Username(entity.getPlayer2Username());
        game.setCurrentPlayerUsername(entity.getCurrentPlayerUsername());
        game.setWinner(entity.getWinner());
        game.setCreatedAt(entity.getCreatedAt());
        System.out.println("=== DEBUG: EntityToGameMapper - entity.winner: " + entity.getWinner() +
                ", game.winner: " + game.getWinner() + " ===");
        // Маппинг активных игроков
        return game;
    }

    public GameBoard mapBoardToDomain(GameBoardEntity boardEntity) {
        if (boardEntity == null) {
            return null;
        }
        return new GameBoard(boardEntity.getBoard());
    }
}