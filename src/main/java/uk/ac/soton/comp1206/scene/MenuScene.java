package uk.ac.soton.comp1206.scene;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.soton.comp1206.App;
import uk.ac.soton.comp1206.component.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

    private ImageView title;

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    /**
     * Create a new menu scene
     * @param gameWindow the Game Window this will be displayed in
     */
    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
    }

    /**
     * Build the menu layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background");
        root.getChildren().add(menuPane);

        var mainPane = new BorderPane();
        menuPane.getChildren().add(mainPane);

        title = new ImageView(getClass().getResource("/images/TetrECS.png").toExternalForm());
        title.setFitWidth(gameWindow.getWidth()/1.2);
        title.setPreserveRatio(true);
        mainPane.setCenter(title);
        BorderPane.setAlignment(title, Pos.CENTER);
        title.rotateProperty().set(-11);

        var buttons = new VBox();
        buttons.setAlignment(Pos.CENTER);
        buttons.setSpacing(10);
        buttons.setPadding(new Insets(5.0, 5.0, 18.0, 5.0));
        mainPane.setBottom(buttons);

        var singleplayerButton = new Button("Singleplayer");
        buttons.getChildren().add(singleplayerButton);
        singleplayerButton.getStyleClass().add("menuItem");

        var multiplayerButton = new Button("Multiplayer");
        buttons.getChildren().add(multiplayerButton);
        multiplayerButton.getStyleClass().add("menuItem");

        var instructionsButton = new Button("Instructions");
        buttons.getChildren().add(instructionsButton);
        instructionsButton.getStyleClass().add("menuItem");

        var settingsButton = new Button("Settings");
        buttons.getChildren().add(settingsButton);
        settingsButton.getStyleClass().add("menuItem");

        var exitButton = new Button("Exit");
        buttons.getChildren().add(exitButton);
        exitButton.getStyleClass().add("menuItem");

        //Bind the button action to the startGame method in the menu
        singleplayerButton.setOnAction(this::startGame);
        //Bind the button action to the openMultiplayer method in the menu
        multiplayerButton.setOnAction(this::openMultiplayer);
        //Bind the button action to the openInstructions method in the menu
        instructionsButton.setOnAction(this::openInstructions);
        //Bind the button action to the openSettings method in the menu
        settingsButton.setOnAction(this::openSettings);
        //Bind the button action to shutdown the application
        exitButton.setOnAction(e -> {App.getInstance().shutdown();});

        Multimedia.playBackgroundMusic("menu.mp3");
    }

    /**
     * Play the logo animation
     */
    private void playTitleAnimation() {
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(4000), new KeyValue(title.scaleXProperty(), 0.5), new KeyValue(title.scaleYProperty(), 0.5), new KeyValue(title.rotateProperty(), 11)),
            new KeyFrame(Duration.millis(8000), new KeyValue(title.scaleXProperty(), 1.0), new KeyValue(title.scaleYProperty(), 1.0), new KeyValue(title.rotateProperty(), -11))
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    /**
     * Initialise the menu
     */
    @Override
    public void initialise() {
        logger.info("Initializing the menu");
        playTitleAnimation();
    }

    /**
     * Handle when the Start Game button is pressed
     * @param event event
     */
    private void startGame(ActionEvent event) {
        gameWindow.startChallenge();
    }

    /**
     * Handle when the instruction button is pressed
     * @param event event
     */
    private void openInstructions(ActionEvent event) {
       gameWindow.openInstructions(); 
    }

    /**
     * Handle when the multiplayer button is pressed
     * @param event event
     */
    private void openMultiplayer(ActionEvent event) {
        gameWindow.openLobby();
    }

    /**
     * Handle when the settings button is pressed
     * @param event event
     */
    private void openSettings(ActionEvent event) {
        gameWindow.openSettings();
    }

    /**
     * Handle when a key on the keyboard is pressed
     */
    @Override
    public void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
            App.getInstance().shutdown();
        }
    }

}
