package ballboy.model.entities.behaviour;

import ballboy.model.Level;
import ballboy.model.entities.DynamicEntity;
import ballboy.model.entities.utilities.Vector2D;

public class SquareCatBehaviourStrategy implements BehaviourStrategy {

    /*
    * indicating square cat's moving direction
    * possible state number : 1, 2, 3, 4
    * */
    private int state = 1;

    // when count adds up to ten, clear up the count and transit to next state.
    private int count = 0;

    private final Level level;

    public SquareCatBehaviourStrategy(Level level) {
        this.level = level;
    }

    @Override
    public void behave(DynamicEntity entity, double frameDurationMilli) {
        switch (state) {
            case 1:
                entity.setPosition(new Vector2D(level.getHeroX() - 50 + count, level.getHeroY() - 50));
                if (++count == 101) {
                    count = 0;
                    state = 2;
                }
                break;
            case 2:
                entity.setPosition(new Vector2D(level.getHeroX() + 50, level.getHeroY() - 50 + count));
                if (++count == 101) {
                    count = 0;
                    state = 3;
                }
                break;
            case 3:
                entity.setPosition(new Vector2D(level.getHeroX() + 50 - count, level.getHeroY() + 50));
                if (++count == 101) {
                    count = 0;
                    state = 4;
                }
                break;
            case 4:
                entity.setPosition(new Vector2D(level.getHeroX() - 50, level.getHeroY() + 50 - count));
                if (++count == 101) {
                    count = 0;
                    state = 1;
                }
                break;
            default:
                System.out.println("illegal state");

        }
    }

    @Override
    public BehaviourStrategy copy(Level level) {
        return new SquareCatBehaviourStrategy(level);
    }

}
