// ./main/java/org/web/model/GameDto.java
package org.web.model;

import java.util.Date;
import java.util.UUID;

public class GameDto {
    private UUID id;
    private GameBoardDto board;
    private boolean isPlayerTurn;
    private String status; // "IN_PROGRESS", "PLAYER_WON", "COMPUTER_WON", "DRAW", "WAITING_FOR_PLAYERS"
    private String gameType; // "PVP" или "PVE"
    
    // ДОБАВИТЬ: поля для PVP игр
    private String creatorUsername;
    private String player2Username;
    private String currentPlayerUsername;
    private String winner;
    private Date createdAt;

    public GameDto() {
    }

    public GameDto(UUID id, GameBoardDto board) {
        this.id = id;
        this.board = board;
        this.isPlayerTurn = true;
        this.status = "IN_PROGRESS";
        this.gameType = "PVP"; // по умолчанию
        this.creatorUsername = null;
        this.player2Username = null;
        this.currentPlayerUsername = null;
        this.winner = null;
    }

    // Геттеры и сеттеры
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public GameBoardDto getBoard() { return board; }
    public void setBoard(GameBoardDto board) { this.board = board; }

    public boolean isPlayerTurn() { return isPlayerTurn; }
    public void setPlayerTurn(boolean playerTurn) { isPlayerTurn = playerTurn; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    // ДОБАВИТЬ: геттеры и сеттеры для новых полей
    public String getCreatorUsername() { return creatorUsername; }
    public void setCreatorUsername(String creatorUsername) { this.creatorUsername = creatorUsername; }
    
    public String getPlayer2Username() { return player2Username; }
    public void setPlayer2Username(String player2Username) { this.player2Username = player2Username; }
    
    public String getCurrentPlayerUsername() { return currentPlayerUsername; }
    public void setCurrentPlayerUsername(String currentPlayerUsername) { this.currentPlayerUsername = currentPlayerUsername; }
    public String getWinner() { return winner; }
    public void setWinner(String winner) { this.winner = winner; }
    public String getGameType() { return gameType; }
    public void setGameType(String gameType) { this.gameType = gameType; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}