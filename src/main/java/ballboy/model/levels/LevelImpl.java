package ballboy.model.levels;

import ballboy.ConfigurationParseException;
import ballboy.model.Entity;
import ballboy.model.Level;
import ballboy.model.entities.ControllableDynamicEntity;
import ballboy.model.entities.DynamicEntity;
import ballboy.model.entities.StaticEntity;
import ballboy.model.entities.utilities.Vector2D;
import ballboy.model.factories.EntityFactory;
import ballboy.model.observers.Observer;
import ballboy.model.observers.Subject;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Level logic, with abstract factor methods.
 */
public class LevelImpl implements Level {

    private final List<Entity> entities = new ArrayList<>();
    private final PhysicsEngine engine;
    private final EntityFactory entityFactory;
    private ControllableDynamicEntity<DynamicEntity> hero;
    private Entity finish;
    private Entity squareCat;
    private double levelHeight;
    private double levelWidth;
    private double levelGravity;
    private double floorHeight;
    private Color floorColor;
    private boolean showWinnerText = false;

    private final double frameDurationMilli;

    private List<Observer> observers = new ArrayList<>();

    /**
     * A callback queue for post-update jobs. This is specifically useful for scheduling jobs mid-update
     * that require the level to be in a valid state.
     */
    private final Queue<Runnable> afterUpdateJobQueue = new ArrayDeque<>();

    /* used when copying an entire level */
    private LevelImpl(PhysicsEngine engine, EntityFactory entityFactory, Entity finish, double levelHeight, double levelWidth, double levelGravity, double floorHeight, Color floorColor, boolean showWinnerText, double frameDurationMilli) {
        this.engine = engine;
        this.entityFactory = entityFactory;
        this.finish = finish;
        this.levelHeight = levelHeight;
        this.levelWidth = levelWidth;
        this.levelGravity = levelGravity;
        this.floorHeight = floorHeight;
        this.floorColor = floorColor;
        this.showWinnerText = showWinnerText;
        this.frameDurationMilli = frameDurationMilli;
    }

    public LevelImpl(
            JSONObject levelConfiguration,
            PhysicsEngine engine,
            EntityFactory entityFactory,
            double frameDurationMilli) {
        this.engine = engine;
        this.entityFactory = entityFactory;
        this.frameDurationMilli = frameDurationMilli;
        initLevel(levelConfiguration);
    }

    /**
     * Instantiates a level from the level configuration.
     *
     * @param levelConfiguration The configuration for the level.
     */
    private void initLevel(JSONObject levelConfiguration) {

        this.levelWidth = ((Number) levelConfiguration.get("levelWidth")).doubleValue();
        this.levelHeight = ((Number) levelConfiguration.get("levelHeight")).doubleValue();
        this.levelGravity = ((Number) levelConfiguration.get("levelGravity")).doubleValue();

        JSONObject floorJson = (JSONObject) levelConfiguration.get("floor");
        this.floorHeight = ((Number) floorJson.get("height")).doubleValue();
        String floorColorWeb = (String) floorJson.get("color");
        this.floorColor = Color.web(floorColorWeb);

        JSONArray generalEntities = (JSONArray) levelConfiguration.get("genericEntities");
        for (Object o : generalEntities) {
            Entity entity = entityFactory.createEntity(this, (JSONObject) o);
            this.entities.add(entity);
//            if ( ((JSONObject) o).get("type").equals("enemy") ) {
//                this.enemies.add((DynamicEntity) entity);
//            }
        }

        JSONObject heroConfig = (JSONObject) levelConfiguration.get("hero");
        double maxVelX = ((Number) levelConfiguration.get("maxHeroVelocityX")).doubleValue();

        Object hero = entityFactory.createEntity(this, heroConfig);
        if (!(hero instanceof DynamicEntity)) {
            throw new ConfigurationParseException("hero must be a dynamic entity");
        }
        DynamicEntity dynamicHero = (DynamicEntity) hero;
        Vector2D heroStartingPosition = dynamicHero.getPosition();
        this.hero = new ControllableDynamicEntity<>(dynamicHero, heroStartingPosition, maxVelX, floorHeight,
                levelGravity);
        this.entities.add(this.hero);

        JSONObject finishConfig = (JSONObject) levelConfiguration.get("finish");
        this.finish = entityFactory.createEntity(this, finishConfig);
        this.entities.add(finish);

        JSONObject squareCatConfig = (JSONObject) levelConfiguration.get("squareCat");
        Entity squareCat = entityFactory.createEntity(this, squareCatConfig);
        this.squareCat = squareCat;
        this.entities.add(squareCat);
    }

    @Override
    public List<Entity> getEntities() {
//        System.out.println("getEntities size():" + entities.size());
        return Collections.unmodifiableList(entities);
    }

    /* used specifically to copy level information */
    private List<Entity> getModifiableEntities() {
        return entities;
    }

    private List<DynamicEntity> getDynamicEntities() {
        return entities.stream().filter(e -> e instanceof DynamicEntity).map(e -> (DynamicEntity) e).collect(
                Collectors.toList());
    }

    private List<StaticEntity> getStaticEntities() {
        return entities.stream().filter(e -> e instanceof StaticEntity).map(e -> (StaticEntity) e).collect(
                Collectors.toList());
    }

    @Override
    public double getLevelHeight() {
        return this.levelHeight;
    }

    @Override
    public double getLevelWidth() {
        return this.levelWidth;
    }

    @Override
    public double getHeroHeight() {
        return hero.getHeight();
    }

    @Override
    public double getHeroWidth() {
        return hero.getWidth();
    }

    @Override
    public double getFloorHeight() {
        return floorHeight;
    }

    @Override
    public Color getFloorColor() {
        return floorColor;
    }

    @Override
    public double getGravity() {
        return levelGravity;
    }

    @Override
    public void update() {
        List<DynamicEntity> dynamicEntities = getDynamicEntities();

        dynamicEntities.stream().forEach(e -> {
            e.update(frameDurationMilli, levelGravity);
        });

        for (int i = 0; i < dynamicEntities.size(); ++i) {
            DynamicEntity dynamicEntityA = dynamicEntities.get(i);

            for (int j = i + 1; j < dynamicEntities.size(); ++j) {
                DynamicEntity dynamicEntityB = dynamicEntities.get(j);

                if (dynamicEntityA.collidesWith(dynamicEntityB)) {
                    dynamicEntityA.collideWith(dynamicEntityB);
                    dynamicEntityB.collideWith(dynamicEntityA);
                    if (!isHero(dynamicEntityA) && !isHero(dynamicEntityB)) {
                        engine.resolveCollision(dynamicEntityA, dynamicEntityB);
                    }
                }
            }

            for (StaticEntity staticEntity : getStaticEntities()) {
                if (dynamicEntityA.collidesWith(staticEntity)) {
                    dynamicEntityA.collideWith(staticEntity);
                    engine.resolveCollision(dynamicEntityA, staticEntity, this);
                }
            }
        }

        dynamicEntities.stream().forEach(e -> engine.enforceWorldLimits(e, this));

        afterUpdateJobQueue.forEach(j -> j.run());
        afterUpdateJobQueue.clear();

    }

    @Override
    public double getHeroX() {
        return hero.getPosition().getX();
    }

    @Override
    public double getHeroY() {
        return hero.getPosition().getY();
    }

    @Override
    public boolean boostHeight() {
        return hero.boostHeight();
    }

    @Override
    public boolean dropHeight() {
        return hero.dropHeight();
    }

    @Override
    public boolean moveLeft() {
        return hero.moveLeft();
    }

    @Override
    public boolean moveRight() {
        return hero.moveRight();
    }

    @Override
    public boolean isHero(Entity entity) {
        return entity == hero;
    }

    @Override
    public boolean isFinish(Entity entity) {
        return this.finish == entity;
    }

    @Override
    public void resetHero() {
        afterUpdateJobQueue.add(() -> this.hero.reset());
    }

    @Override
    public void finish() {
        showWinnerText = true;
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.exit();
            }
        }, 3000);
    }

    @Override
    public boolean showWinnerText() {
        return showWinnerText;
    }

    @Override
    public boolean isSquareCat(Entity entity) {
        return this.squareCat == entity;
    }

    @Override
    public boolean removeEnemy(Entity enemy) {
        /* remove an enemy */
        System.out.println("size before remove enemy:"+entities.size());
        boolean removed = entities.remove(enemy);
        System.out.println("entity list in LevelImpl: " + entities);
        System.out.println("size after remove enemy:"+ this.entities.size());
        List<Entity> entities = getEntities();
        System.out.println("invoke getEntities() to get the size of entities:"+ entities.size());

        /* if removed, that means the square cat successfully killed a slime, so notify observer(game window) to update the score */
        if (removed) {
            System.out.println("notify game window...");
            String[] split = enemy.getImage().getUrl().split("/");
            notifyObservers(split[split.length - 1]);
            System.out.println("notify game window successfully");
            return true;
        }
        return false;
    }

    @Override
    public Level copy() {
        // 1. deep copy current level with constructor
        LevelImpl copiedLevel = new LevelImpl(engine, entityFactory, finish, levelHeight, levelWidth, levelGravity, floorHeight, floorColor, showWinnerText, frameDurationMilli);
        List<Entity> copiedEntities = copiedLevel.getModifiableEntities();

        // 2.deep copy current entities, add hero and square cat to the copied level respectively
        for (Entity entity : entities) {
            // 2.1 pass in newly created level to deep copy each entity
            Entity copiedEntity = entity.copy(copiedLevel);
            copiedEntities.add(copiedEntity);
            // 2.2 set Hero instance to level if it's ControllableDynamicEntity type
            if (copiedEntity instanceof ControllableDynamicEntity) {
                copiedLevel.setHero((ControllableDynamicEntity<DynamicEntity>) copiedEntity);
            }
            // 2.3 get imageUrl to determine whether the entity is the square cat, add it to level if it's square cat
            String[] imageUrl = copiedEntity.getImage().getUrl().split("/");
            if (imageUrl[imageUrl.length - 1].equals("squarecat.jpg")) {
                copiedLevel.setSquareCat(copiedEntity);
            }

        }

        System.out.println("copyLevel.getEntities().size() after deep copy:" + copiedLevel.getEntities().size());
        // 3. return deep copied level instance.
        return copiedLevel;
    }


//    @Override
//    public Entity getHero() {
//        return hero;
//    }
//
//    @Override
//    public Entity getSquareCat() {
//        return squareCat;
//    }

    /* used specifically to deep copy a Level instance  */
    private void setHero(ControllableDynamicEntity<DynamicEntity> hero) {
        this.hero = hero;
    }

    /* used specifically to deep copy a Level instance  */
    private void setSquareCat(Entity squareCat) {
        this.squareCat = squareCat;
    }

//    @Override
//    public String toString() {
//        return "LevelImpl{" +
//                "entities=" + entities +
//                '}';
//    }


    @Override
    public void registerObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        int index = observers.indexOf(observer);
        if (index >= 0) {
            observers.remove(observer);
        }
    }

    @Override
    public void notifyObservers(String message) {
        System.out.println("send message: " + message);
        for (Observer observer : observers) {
            observer.update(message);
        }
    }
}
