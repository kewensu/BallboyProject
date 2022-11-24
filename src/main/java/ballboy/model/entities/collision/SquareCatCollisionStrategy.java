package ballboy.model.entities.collision;

import ballboy.model.Entity;
import ballboy.model.Level;

public class SquareCatCollisionStrategy implements CollisionStrategy {

    @Override
    public void collideWith(Entity currentEntity, Entity hitEntity) {
    }

    @Override
    public CollisionStrategy copy(Level level) {
        return new SquareCatCollisionStrategy();
    }
}
