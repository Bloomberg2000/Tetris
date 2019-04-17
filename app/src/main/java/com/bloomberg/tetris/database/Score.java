package com.bloomberg.tetris.database;

/**
 * 分数类的声明和实现
 */

public class Score {
    private long id;
    private long score;
    private String playerName;

    // 构造方法
    public Score() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getScore() {
        return score;
    }

    public String getScoreString() {
        return String.valueOf(score);
    }

    public void setScore(long comment) {
        this.score = comment;
    }

    public String getName() {
        return playerName;
    }

    public void setName(String comment) {
        this.playerName = comment;
    }

    // Will be used by the ArrayAdapter in the ListView
    @Override
    public String toString() {
        return String.valueOf(score) + "@" + playerName;
    }
}
