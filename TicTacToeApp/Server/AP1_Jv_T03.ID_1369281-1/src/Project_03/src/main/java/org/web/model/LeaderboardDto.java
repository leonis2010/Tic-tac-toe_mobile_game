// ./main/java/org/web/model/LeaderboardDto.java
package org.web.model;

public class LeaderboardDto {
    private String username;
    private int gamesPlayed;
    private int gamesWon;
    private int gamesLost;
    private int gamesDrawn;
    private double winRate;
    private int rating; // рейтинг для сортировки

    public LeaderboardDto() {}

    public LeaderboardDto(String username, int gamesPlayed, int gamesWon,
                          int gamesLost, int gamesDrawn, double winRate, int rating) {
        this.username = username;
        this.gamesPlayed = gamesPlayed;
        this.gamesWon = gamesWon;
        this.gamesLost = gamesLost;
        this.gamesDrawn = gamesDrawn;
        this.winRate = winRate;
        this.rating = rating;
    }

    // Геттеры и сеттеры
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getGamesPlayed() { return gamesPlayed; }
    public void setGamesPlayed(int gamesPlayed) { this.gamesPlayed = gamesPlayed; }

    public int getGamesWon() { return gamesWon; }
    public void setGamesWon(int gamesWon) { this.gamesWon = gamesWon; }

    public int getGamesLost() { return gamesLost; }
    public void setGamesLost(int gamesLost) { this.gamesLost = gamesLost; }

    public int getGamesDrawn() { return gamesDrawn; }
    public void setGamesDrawn(int gamesDrawn) { this.gamesDrawn = gamesDrawn; }

    public double getWinRate() { return winRate; }
    public void setWinRate(double winRate) { this.winRate = winRate; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
}