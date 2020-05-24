package it.di.uniba.sms1920.teambarrella.unibarcade.database;
import androidx.annotation.NonNull;

public class Score {

    private String userId;
    private String gameId;
    private String score;

    public Score(String userId, String gameId, String score) {
        this.userId = userId;
        this.gameId = gameId;
        this.score = score;
    }

    public String getUserId() {
        return userId;
    }

    public String getGameId() {
        return gameId;
    }

    public String getScore() {
        return score;
    }

    @NonNull
    @Override
    public String toString() {
        return score;
    }
}
