// ./main/java/org/domain/model/Game.java
package org.domain.model;

import java.util.Date;
import java.util.UUID;

public class Game {
    private UUID id;
    private GameBoard board;
    private boolean isPlayerTurn; // true - ход игрока, false - ход компьютера
    private String status;
    
    // поля для PVP игр
    private String creatorUsername; // Имя создателя игры
    private String player2Username; // Имя второго игрока (если присоединился)
    private String currentPlayerUsername; // Имя игрока, который ходит сейчас
    private String winner;
    private Date createdAt;

    public Game() {
        this.id = UUID.randomUUID();
        this.board = new GameBoard();
        this.isPlayerTurn = true;
        this.status = "IN_PROGRESS";
        this.gameType = "PVP"; // по умолчанию
        this.creatorUsername = null;
        this.player2Username = null;
        this.currentPlayerUsername = null;
        this.winner = null;
        this.createdAt = new Date();
    }

    public Game(UUID id, GameBoard board) {
        this.id = id;
        this.board = board;
        this.isPlayerTurn = true;
        this.status = "IN_PROGRESS";
        this.gameType = "PVP"; // по умолчанию
        this.creatorUsername = null;
        this.player2Username = null;
        this.currentPlayerUsername = null;
        this.winner = null;
        this.createdAt = new Date();
    }

    // Геттеры и сеттеры
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public GameBoard getBoard() { return board; }
    public void setBoard(GameBoard board) { this.board = board; }
    
    public boolean isPlayerTurn() { return isPlayerTurn; }
    public void setPlayerTurn(boolean playerTurn) { this.isPlayerTurn = playerTurn; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    private String gameType; // "PVP" или "PVE"
    
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