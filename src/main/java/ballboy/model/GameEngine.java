package ballboy.model;

import ballboy.memento.CareTaker;
import ballboy.memento.Memento;
import ballboy.model.observers.Observer;

/**
 * The base interface for interacting with the Ballboy model
 */
public interface GameEngine extends Observer {
    /**
     * Return the currently loaded level
     *
     * @return The current level
     */
    Level getCurrentLevel();

    /**
     * Start the level
     */
    void startLevel();

    /**
     * Increases the bounce height of the current hero.
     *
     * @return boolean True if the bounce height of the hero was successfully boosted.
     */
    boolean boostHeight();

    /**
     * Reduces the bounce height of the current hero.
     *
     * @return boolean True if the bounce height of the hero was successfully dropped.
     */
    boolean dropHeight();

    /**
     * Applies a left movement to the current hero.
     *
     * @return True if the hero was successfully moved left.
     */
    boolean moveLeft();

    /**
     * Applies a right movement to the current hero.
     *
     * @return True if the hero was successfully moved right.
     */
    boolean moveRight();

    /**
     * Instruct the model to progress forward in time by one increment.
     */
    void tick();

    /**
     * return the value of level index
     * @return
     */
    int getLevelIndex();

    /**
     * return red score
     * @return
     */
    int getRedScore();

    /**
     * return green score
     * @return
     */
    int getGreenScore();

    /**
     * return blue score
     * @return
     */
    int getBlueScore();

    /**
     * return total score
     * @return
     */
    int getTotalScore();

    /**
     * set red score
     * @param redScore
     */
    void setRedScore(int redScore);

    /**
     * set green score
     * @param greenScore
     */
    void setGreenScore(int greenScore);

    /**
     * set blue score
     * @param blueScore
     */
    void setBlueScore(int blueScore);

    /**
     * set total score
     * @param totalScore
     */
    void setTotalScore(int totalScore);

    /**
     * return the CareTaker object
     * @return
     */
    CareTaker getCareTaker();

    /**
     * save current state to a Memento object
     * @return
     */
    Memento saveStateToMemento();

    /**
     * revert to the state stored in the Memento object
     * @param memento
     */
    void getStateFromMemento(Memento memento);
}
