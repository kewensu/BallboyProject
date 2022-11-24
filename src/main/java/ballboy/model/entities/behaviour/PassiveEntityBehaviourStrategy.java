package ballboy.model.entities.behaviour;

import ballboy.model.Level;
import ballboy.model.entities.DynamicEntity;

/**
 * Passive behaviour logic for dynamic entities.
 * This will do nothing.
 */
public class PassiveEntityBehaviourStrategy implements BehaviourStrategy {

    @Override
    public void behave(
            DynamicEntity entity,
            double frameDurationMilli) {
        return;
    }

    @Override
    public BehaviourStrategy copy(Level level) {
        return new PassiveEntityBehaviourStrategy();
    }
}
