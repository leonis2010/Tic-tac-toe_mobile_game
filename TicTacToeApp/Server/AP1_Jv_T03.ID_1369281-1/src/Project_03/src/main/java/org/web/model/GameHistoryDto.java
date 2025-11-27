// ./main/java/org/web/model/GameHistoryDto.java
package org.web.model;

import java.util.Date;
import java.util.UUID;

public class GameHistoryDto {
    private UUID id;
    private String player1Username;
    private String player2Username;
    private String gameType; // "PVP" или "PVE"
    private String result; // "WIN", "LOSE", "DRAW"
    private Date gameDate;
    private String winner;
    private String currentUserUsername; // для определения результата текущего пользователя

    public GameHistoryDto() {}

    // Конструктор
    public GameHistoryDto(UUID id, String player1Username, String player2Username,
                          String gameType, String result, Date gameDate, String winner) {
        this.id = id;
        this.player1Username = player1Username;
        this.player2Username = player2Username;
        this.gameType = gameType;
        this.result = result;
        this.gameDate = gameDate;
        this.winner = winner;
    }

    // Геттеры и сеттеры
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getPlayer1Username() { return player1Username; }
    public void setPlayer1Username(String player1Username) { this.player1Username = player1Username; }

    public String getPlayer2Username() { return player2Username; }
    public void setPlayer2Username(String player2Username) { this.player2Username = player2Username; }

    public String getGameType() { return gameType; }
    public void setGameType(String gameType) { this.gameType = gameType; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public Date getGameDate() { return gameDate; }
    public void setGameDate(Date gameDate) { this.gameDate = gameDate; }

    public String getWinner() { return winner; }
    public void setWinner(String winner) { this.winner = winner; }

    public String getCurrentUserUsername() { return currentUserUsername; }
    public void setCurrentUserUsername(String currentUserUsername) { this.currentUserUsername = currentUserUsername; }
}