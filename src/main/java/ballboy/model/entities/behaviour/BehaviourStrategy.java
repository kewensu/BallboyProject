package ballboy.model.entities.behaviour;

import ballboy.model.Level;
import ballboy.model.entities.DynamicEntity;

/**
 * Behaviour of a given dynamic entity. This is to be delegated to after an entity is updated.
 */
public interface BehaviourStrategy {
    void behave(
            DynamicEntity entity,
            double frameDurationMilli);

    BehaviourStrategy copy(Level level);
}