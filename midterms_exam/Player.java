package com.roshambo;

// inheritance from account to represent a user with specific properties like score
public class Player extends Account {
    private int score; // used for total match victories

    public Player(String username, String password, int score) {
        super(username, password); 
        this.score = score;
    }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
}