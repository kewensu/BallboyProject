package ballboy.view;

import ballboy.model.Entity;
import ballboy.model.GameEngine;
import ballboy.model.GameEngineImpl;
import ballboy.model.Level;
import ballboy.model.levels.LevelImpl;
import ballboy.model.observers.Observer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class GameWindow {
    private static final double VIEWPORT_MARGIN_X = 100;
    private static final double VIEWPORT_MARGIN_Y = 50;
    private final int width;
    private final int height;
    private final double frameDurationMilli;
    private final Scene scene;
    private final Pane pane;
    private final GameEngine model;
    private final List<EntityView> entityViews;
    private final BackgroundDrawer backgroundDrawer;
    private double xViewportOffset = 0.0;
    private double yViewportOffset = 0.0;

    /* text displaying current game level */
    private Text currentLevel;
    /* text displaying current red score */
    private Text redScoreText;
    /* text displaying current green score */
    private Text greenScoreText;
    /* text displaying current blue score */
    private Text blueScoreText;
    /* text displaying total score */
    private Text totalScoreText;


    /* constructor: initialize game window */
    public GameWindow(
            GameEngine model,
            int width,
            int height,
            double frameDurationMilli) {
        this.model = model;
        this.width = width;
        this.height = height;
        this.frameDurationMilli = frameDurationMilli;
        pane = new Pane();
        scene = new Scene(pane, width, height);

        entityViews = new ArrayList<>();

        KeyboardInputHandler keyboardInputHandler = new KeyboardInputHandler(model);

        scene.setOnKeyPressed(keyboardInputHandler::handlePressed);
        scene.setOnKeyReleased(keyboardInputHandler::handleReleased);

        backgroundDrawer = new BlockedBackground();
        backgroundDrawer.draw(model, pane);

        /* instantiates and add a text object to pane to show current level  */
        Text text = new Text();
        text.setText("text");
        text.setX(50.0);
        text.setY(50.0);
        currentLevel = text;
        pane.getChildren().add(text);

        /* instantiates and add a text object to pane to show red score  */
        Text redScore = new Text();
        redScore.setText("red:0");
        redScore.setX(170.0);
        redScore.setY(50.0);
        redScoreText = redScore;
        pane.getChildren().add(redScore);

        /* instantiates and add a text object to pane to show green score  */
        Text greenScore = new Text();
        greenScore.setText("green:0");
        greenScore.setX(220.0);
        greenScore.setY(50.0);
        greenScoreText = greenScore;
        pane.getChildren().add(greenScore);

        /* instantiates and add a text object to pane to show blue score  */
        Text blueScore = new Text();
        blueScore.setText("blue:0");
        blueScore.setX(280.0);
        blueScore.setY(50.0);
        blueScoreText = blueScore;
        pane.getChildren().add(blueScore);

        /* instantiates and add a text object to pane to show total score  */
        Text totalScore = new Text();
        totalScore.setText("total:0");
        totalScore.setX(340.0);
        totalScore.setY(50.0);
        totalScoreText = totalScore;
        pane.getChildren().add(totalScore);

        /* register GameEngine instance as an observer of the initial level so that changes of score could be monitored */
        LevelImpl level = (LevelImpl) model.getCurrentLevel();
        level.registerObserver(((GameEngineImpl) model));

    }

    public Scene getScene() {
        return scene;
    }

    public void run() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(frameDurationMilli),
                t -> this.draw()));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void draw() {
        model.tick();

        // constantly refresh current level of game and display it on the screen
        currentLevel.setText("current level: " + (model.getLevelIndex() + 1) );

        /*  constantly refresh the window to show latest scores */
        redScoreText.setText("red: " + model.getRedScore());
        greenScoreText.setText("green: " + model.getGreenScore());
        blueScoreText.setText("blue: " + model.getBlueScore());
        totalScoreText.setText("total: " + model.getTotalScore());

        // if showWinnerText is true, add Winner text to pane
        if (model.getCurrentLevel().showWinnerText() == true) {
            Text text = new Text();
            text.setX(300);
            text.setY(200);
            text.setText("WINNER!");
            pane.getChildren().add(text);
        }

        List<Entity> entities = model.getCurrentLevel().getEntities();
//        System.out.println("game window entities size:" + entities.size());
        for (EntityView entityView : entityViews) {
            entityView.markForDelete();
        }

        double heroXPos = model.getCurrentLevel().getHeroX();
        double viewportLeftBar = xViewportOffset + VIEWPORT_MARGIN_X;
        double viewportRightBar = viewportLeftBar + (width - 2 * VIEWPORT_MARGIN_X);

        if (heroXPos < viewportLeftBar) {
            xViewportOffset -= heroXPos - viewportLeftBar;
        } else if (heroXPos + model.getCurrentLevel().getHeroWidth() > viewportRightBar) {
            xViewportOffset += heroXPos + model.getCurrentLevel().getHeroWidth() - viewportRightBar;
        }

        heroXPos -= xViewportOffset;

        if (heroXPos < VIEWPORT_MARGIN_X) {
            if (xViewportOffset >= 0) { // Don't go further left than the start of the level
                xViewportOffset -= VIEWPORT_MARGIN_X - heroXPos;
                if (xViewportOffset < 0) {
                    xViewportOffset = 0;
                }
            }
        }

        double levelRight = model.getCurrentLevel().getLevelWidth();
        double screenRight = xViewportOffset + width - model.getCurrentLevel().getHeroWidth();
        if (screenRight > levelRight) {
            xViewportOffset = levelRight - width + model.getCurrentLevel().getHeroWidth();
        }


        double levelTop = 0.0;
        double levelBottom = model.getCurrentLevel().getLevelHeight();
        double heroYPos = model.getCurrentLevel().getHeroY();
        double heroHeight = model.getCurrentLevel().getHeroHeight();
        double viewportTop = yViewportOffset + VIEWPORT_MARGIN_Y;
        double viewportBottom = yViewportOffset + height - 2 * VIEWPORT_MARGIN_Y;

        if (heroYPos + heroHeight > viewportBottom) {
            // if below, shift down
            yViewportOffset += heroYPos + heroHeight - viewportBottom;
        } else if (heroYPos < viewportTop) {
            // if above, shift up
            yViewportOffset -= viewportTop - heroYPos;
        }

        double screenBottom = yViewportOffset + height;
        double screenTop = yViewportOffset;
        // shift back in the instance when we're near the boundary
        if (screenBottom > levelBottom) {
            yViewportOffset -= screenBottom - levelBottom;
        } else if (screenTop < 0.0) {
            yViewportOffset -= screenTop;
        }


//        double viewportBottomBar = yViewportOffset + height - VIEWPORT_MARGIN_Y;
//        double viewportTopBar = yViewportOffset + VIEWPORT_MARGIN_Y;
//
//        if (heroYPos + model.getCurrentLevel().getHeroHeight() > viewportBottomBar) {
//            yViewportOffset += (heroYPos + model.getCurrentLevel().getHeroHeight()) - viewportBottomBar;
//        } else if (heroYPos < viewportTopBar) {
//            yViewportOffset -= viewportTopBar - heroYPos;
//        }
//
//        heroYPos -= yViewportOffset;
//
//        if (heroYPos > VIEWPORT_MARGIN_Y) {
//            if (yViewportOffset >= 0) { // avoid going further than bottom of the screen
//                yViewportOffset -= heroYPos - VIEWPORT_MARGIN_Y;
//                if (yViewportOffset < 0) {
//                    yViewportOffset = 0;
//                }
//            }
//        }


        backgroundDrawer.update(xViewportOffset, yViewportOffset);


        for (Entity entity : entities) {
            boolean notFound = true;
            for (EntityView view : entityViews) {
                if (view.matchesEntity(entity)) {
                    notFound = false;
                    view.update(xViewportOffset, yViewportOffset);
                    break;
                }
            }
            if (notFound) {
                EntityView entityView = new EntityViewImpl(entity);
                entityViews.add(entityView);
                pane.getChildren().add(entityView.getNode());
            }
        }

        for (EntityView entityView : entityViews) {
            if (entityView.isMarkedForDelete()) {
                pane.getChildren().remove(entityView.getNode());
            }
        }
        entityViews.removeIf(EntityView::isMarkedForDelete);
    }



}
