// ./main/java/org/web/mapper/DtoToGameMapper.java
package org.web.mapper;

import org.domain.model.Game;
import org.domain.model.GameBoard;
import org.web.model.GameBoardDto;
import org.web.model.GameDto;
import org.springframework.stereotype.Component;

@Component
public class DtoToGameMapper {

    public Game mapToDomain(GameDto gameDto) {
        if (gameDto == null) {
            return null;
        }

        GameBoard gameBoard = mapBoardToDomain(gameDto.getBoard());
        Game game = new Game(gameDto.getId(), gameBoard);
        game.setPlayerTurn(gameDto.isPlayerTurn());
        game.setStatus(gameDto.getStatus());
        game.setGameType(gameDto.getGameType());
        game.setCreatorUsername(gameDto.getCreatorUsername());
        game.setPlayer2Username(gameDto.getPlayer2Username());
        game.setCurrentPlayerUsername(gameDto.getCurrentPlayerUsername());
        game.setWinner(gameDto.getWinner());
        game.setCreatedAt(gameDto.getCreatedAt());

        return game;
    }
    
    public GameBoard mapBoardToDomain(GameBoardDto boardDto) {
        if (boardDto == null) {
            return null;
        }
        return new GameBoard(boardDto.getBoard());
    }

}