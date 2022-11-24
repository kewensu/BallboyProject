package ballboy.memento;

import ballboy.model.Entity;
import ballboy.model.Level;

import java.util.List;

public class Memento {

    private Level level;

    private int levelIndex;

    private int redScore;
    private int greenScore;
    private int blueScore;
    private int totalScore;

    public Memento(Level level, int levelIndex, int redScore, int greenScore, int blueScore, int totalScore) {
        this.level = level;
        this.levelIndex = levelIndex;
        this.redScore = redScore;
        this.greenScore = greenScore;
        this.blueScore = blueScore;
        this.totalScore = totalScore;
    }

    public Level getLevel() {
        return level;
    }

    public int getLevelIndex() {
        return levelIndex;
    }

    public int getRedScore() {
        return redScore;
    }

    public int getGreenScore() {
        return greenScore;
    }

    public int getBlueScore() {
        return blueScore;
    }

    public int getTotalScore() {
        return totalScore;
    }

}
