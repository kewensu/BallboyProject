package ballboy.model.observers;


public interface Subject {

    /**
     * register a new observer to the observer list
     * @param observer
     */
    void registerObserver(Observer observer);

    /**
     * remove an observer from the observer list
     * @param observer
     */
    void removeObserver(Observer observer);

    /**
     * notify all the observers in the observer list to update with latest changes.
     * @param message
     */
    void notifyObservers(String message);

}
