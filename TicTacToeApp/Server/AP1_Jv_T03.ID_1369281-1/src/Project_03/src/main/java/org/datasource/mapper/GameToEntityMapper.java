// ./main/java/org/datasource/mapper/GameToEntityMapper.java
package org.datasource.mapper;

import org.datasource.model.GameBoardEntity;
import org.datasource.model.GameEntity;
import org.domain.model.Game;
import org.domain.model.GameBoard;

public class GameToEntityMapper {

    public GameEntity mapToEntity(Game game) {
        if (game == null) return null;

        GameEntity entity = new GameEntity(game.getId(), mapBoardToEntity(game.getBoard()));
        entity.setPlayerTurn(game.isPlayerTurn());
        entity.setStatus(game.getStatus());
        entity.setGameType(game.getGameType());
        entity.setCreatorUsername(game.getCreatorUsername());
        entity.setPlayer2Username(game.getPlayer2Username());
        entity.setCurrentPlayerUsername(game.getCurrentPlayerUsername());
        entity.setWinner(game.getWinner());
        entity.setCreatedAt(game.getCreatedAt());

        return entity;
    }

    public GameBoardEntity mapBoardToEntity(GameBoard gameBoard) {
        if (gameBoard == null) {
            return null;
        }
        return new GameBoardEntity(gameBoard.getBoard());
    }
}