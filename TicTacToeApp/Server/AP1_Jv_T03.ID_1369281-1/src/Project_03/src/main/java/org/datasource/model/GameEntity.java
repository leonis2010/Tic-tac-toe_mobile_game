// ./main/java/org/datasource/model/GameEntity.java
package org.datasource.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GameEntity {
    private UUID id;
    private GameBoardEntity board;
    private boolean isPlayerTurn;
    private String status;
    private String gameType;// "PVP" или "PVE"
    
    // поля для PVP игр
    private String creatorUsername;
    private String player2Username;
    private String currentPlayerUsername;
    private String winner;
    private Date createdAt;
    private Set<String> activePlayers = new HashSet<>(); // Игроки, которые сейчас в игре

    public GameEntity() {
        this.id = UUID.randomUUID();
        this.board = new GameBoardEntity();
        this.isPlayerTurn = true;
        this.status = "IN_PROGRESS";
        this.gameType = "PVP"; // по умолчанию
        this.creatorUsername = null;
        this.player2Username = null;
        this.currentPlayerUsername = null;
        this.winner = null;
    }

    public GameEntity(UUID id, GameBoardEntity board) {
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

    public GameBoardEntity getBoard() { return board; }
    public void setBoard(GameBoardEntity board) { this.board = board; }

    public boolean isPlayerTurn() { return isPlayerTurn; }
    public void setPlayerTurn(boolean playerTurn) { isPlayerTurn = playerTurn; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    // геттеры и сеттеры
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