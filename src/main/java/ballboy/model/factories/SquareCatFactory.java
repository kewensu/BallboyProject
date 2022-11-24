package ballboy.model.factories;

import ballboy.model.Entity;
import ballboy.model.Level;
import ballboy.model.entities.DynamicEntityImpl;
import ballboy.model.entities.behaviour.SquareCatBehaviourStrategy;
import ballboy.model.entities.collision.SquareCatCollisionStrategy;
import ballboy.model.entities.utilities.*;
import javafx.scene.image.Image;
import org.json.simple.JSONObject;

public class SquareCatFactory implements EntityFactory {

    @Override
    public Entity createEntity(Level level, JSONObject config) {

        double startX = ((Number) config.get("startX")).doubleValue();
        double startY = ((Number) config.get("startY")).doubleValue();
        String imageName = (String) config.getOrDefault("image", "squarecat.jpg");

        Image image = new Image(imageName);
        // preserve image ratio
        double height = 20;
        double width = height * image.getWidth() / image.getHeight();

        Vector2D startingPosition = new Vector2D(startX, startY);

        KinematicState kinematicState = new KinematicStateImpl.KinematicStateBuilder()
                .setPosition(startingPosition)
                .build();

        AxisAlignedBoundingBox volume = new AxisAlignedBoundingBoxImpl(
                startingPosition,
                height,
                width
        );


        return new DynamicEntityImpl(
                kinematicState,
                volume,
                Entity.Layer.FOREGROUND,
                new Image(imageName),
                new SquareCatCollisionStrategy(),
                new SquareCatBehaviourStrategy(level)
        );
    }
}
