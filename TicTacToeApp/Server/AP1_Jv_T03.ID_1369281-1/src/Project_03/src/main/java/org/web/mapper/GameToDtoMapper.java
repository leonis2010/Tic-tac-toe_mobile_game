// ./main/java/org/web/mapper/GameToDtoMapper.java
package org.web.mapper;

import org.domain.model.Game;
import org.domain.model.GameBoard;
import org.web.model.GameBoardDto;
import org.web.model.GameDto;
import org.springframework.stereotype.Component;

@Component 
public class GameToDtoMapper {
public GameDto mapToDto(Game game) {
    if (game == null) {
        return null;
    }
    System.out.println("=== DEBUG: GameToDtoMapper - mapping game: " + game.getId() +
            ", type: " + game.getGameType() + " ===");

    GameBoardDto boardDto = mapBoardToDto(game.getBoard());
    GameDto gameDto = new GameDto(game.getId(), boardDto);
    gameDto.setPlayerTurn(game.isPlayerTurn());
    gameDto.setStatus(game.getStatus());
    gameDto.setGameType(game.getGameType());
    gameDto.setCreatorUsername(game.getCreatorUsername());
    gameDto.setPlayer2Username(game.getPlayer2Username());
    gameDto.setCurrentPlayerUsername(game.getCurrentPlayerUsername());
    gameDto.setWinner(game.getWinner());
    gameDto.setCreatedAt(game.getCreatedAt());
    System.out.println("=== DEBUG: GameToDtoMapper - game.winner: " + game.getWinner() +
            ", gameDto.winner: " + gameDto.getWinner() + " ===");
    return gameDto;
}

    public GameBoardDto mapBoardToDto(GameBoard gameBoard) {
        if (gameBoard == null) {
            return null;
        }
        return new GameBoardDto(gameBoard.getBoard());
    }
}