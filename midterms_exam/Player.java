package com.roshambo;

// inheritance from account to represent a user with specific properties like score
public class Player extends Account {
    private int score; // used for total match victories
    private int matchesPlayed; // used to calculate win percentage

    public Player(String username, String password, int score, int matchesPlayed) {
        super(username, password); 
        this.score = score;
        this.matchesPlayed = matchesPlayed;
    }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    
    public int getMatchesPlayed() { return matchesPlayed; }
    public void setMatchesPlayed(int matchesPlayed) { this.matchesPlayed = matchesPlayed; }

    // calculates the win rate percentage. Returns 0 if no matches are played to prevent errors.
    public double getWinRate() {
        if (matchesPlayed == 0) {
            return 0.0;
        }
        return ((double) score / matchesPlayed) * 100.0;
    }
}