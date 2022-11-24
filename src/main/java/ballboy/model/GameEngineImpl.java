package ballboy.model;

import ballboy.ConfigurationParseException;
import ballboy.ConfigurationParser;
import ballboy.memento.CareTaker;
import ballboy.memento.Memento;
import ballboy.model.entities.ControllableDynamicEntity;
import ballboy.model.entities.DynamicEntity;
import ballboy.model.factories.*;
import ballboy.model.levels.LevelImpl;
import ballboy.model.levels.PhysicsEngine;
import ballboy.model.levels.PhysicsEngineImpl;
import ballboy.model.observers.Observer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the GameEngine interface.
 * This provides a common interface for the entire game.
 */
public class GameEngineImpl implements GameEngine {

    private Level level;
    private JSONArray levelConfigs;
    private final EntityFactory entityFactoryRegistry;
    /* storing current game level in numeric format */
    private int levelIndex;

    private int redScore;
    private int greenScore;
    private int blueScore;
    private int totalScore;

    private final CareTaker careTaker = new CareTaker();

    public GameEngineImpl(Level level, JSONArray levelConfigs, EntityFactoryRegistry entityFactoryRegistry, Integer levelIndex) {
        this.level = level;
        this.levelConfigs = levelConfigs;
        this.entityFactoryRegistry = entityFactoryRegistry;
        this.levelIndex = levelIndex;
    }

    @Override
    public Level getCurrentLevel() {
        return level;
    }

    @Override
    public void startLevel() {
        // TODO: Handle when multiple levels has been implemented
        System.out.println("startLevel invoked");

        final double frameDurationMilli = 17;
        PhysicsEngine engine = new PhysicsEngineImpl(frameDurationMilli);

        /*moving from level n to level n+1, restart from level 0 if exceeding the boundary*/
        // update level index
        levelIndex = (levelIndex + 1) % levelConfigs.size();
        System.out.println("start Level " + (levelIndex + 1) );
        JSONObject levelConfig = (JSONObject) levelConfigs.get(levelIndex);
        Level level = new LevelImpl(levelConfig, engine, entityFactoryRegistry, frameDurationMilli);
        // register GameEngineImpl instance as the observer to the new level
        ((LevelImpl) level).registerObserver(this);
        this.level = level;
//        return;
    }

    @Override
    public boolean boostHeight() {
        return level.boostHeight();
    }

    @Override
    public boolean dropHeight() {
        return level.dropHeight();
    }

    @Override
    public boolean moveLeft() {
        return level.moveLeft();
    }

    @Override
    public boolean moveRight() {
        return level.moveRight();
    }

    @Override
    public void tick() {
        level.update();
    }

    @Override
    public int getLevelIndex() {
        return levelIndex;
    }

    @Override
    public void update(String message) {
        System.out.println("update score...");
        System.out.println("game window receive message: " + message);
        switch (message) {
            case "slimeRa.png":
                redScore++;
                break;
            case "slimeGa.png":
                greenScore++;
                break;
            case "slimeBa.png":
                blueScore++;
                break;
        }
    }

    @Override
    public int getRedScore() {
        return redScore;
    }

    @Override
    public int getGreenScore() {
        return greenScore;
    }

    @Override
    public int getBlueScore() {
        return blueScore;
    }

    @Override
    public int getTotalScore() {
        return totalScore;
    }

    @Override
    public void setRedScore(int redScore) {
        this.redScore = redScore;
    }

    @Override
    public void setGreenScore(int greenScore) {
        this.greenScore = greenScore;
    }

    @Override
    public void setBlueScore(int blueScore) {
        this.blueScore = blueScore;
    }

    @Override
    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    @Override
    public CareTaker getCareTaker() {
        return careTaker;
    }

    @Override
    public Memento saveStateToMemento() {
        Memento memento = new Memento(level.copy(), levelIndex, redScore, greenScore, blueScore, totalScore);
        return memento;
    }

    @Override
    public void getStateFromMemento(Memento memento) {
        if (memento == null) {
            System.out.println(" no saved state available at the moment");
            return;
        }
        Level level = memento.getLevel().copy();
        ((LevelImpl) level).registerObserver(this);
        this.level = level;
        levelIndex = memento.getLevelIndex();
        redScore = memento.getRedScore();
        greenScore = memento.getGreenScore();
        blueScore = memento.getBlueScore();
        totalScore = memento.getTotalScore();
    }

}