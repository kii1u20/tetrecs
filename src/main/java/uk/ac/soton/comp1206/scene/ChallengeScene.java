package uk.ac.soton.comp1206.scene;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.value.WritableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.shape.*;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.Multimedia;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.Components.*;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene implements LineClearedListener, GameLoopListener {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);
    protected Game game;

    protected BorderPane topItems;
    protected VBox sideBar;
    
    protected GameBoard board;

    private int keyboardXPos = 0;
    private int keyboardYPos = 0;
    private boolean unpaintKeyboardHover = true;

    protected HBox boardHolder = new HBox();

    protected Rectangle timer;
    protected VBox bottomUI;

    /**
     * Create a new Single Player challenge scene
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
    }

    /**
     * Build the Challenge window
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        setupGame();

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("menu-background");
        root.getChildren().add(challengePane);

        //The main pane
        var mainPane = new BorderPane();
        mainPane.setMaxWidth(gameWindow.getWidth());
        challengePane.getChildren().add(mainPane);

        //The game board
        board = new GameBoard(game.getGrid(),gameWindow.getWidth()/2,gameWindow.getWidth()/2);

        boardHolder.getChildren().add(board);
        boardHolder.setAlignment(Pos.CENTER);
        boardHolder.setSpacing(20);;
        mainPane.setCenter(boardHolder);

        //Holds the score, lives and title
        topItems = new BorderPane();
        topItems.setPadding(new Insets(10.0, 10.0, 10.0, 10.0));

        //Holds the piece boards and high score, level and multiplier
        sideBar = new VBox();
        sideBar.setAlignment(Pos.CENTER);
        sideBar.setSpacing(6.0);
        sideBar.setPadding(new Insets(5.0, 5.0, 5.0, 5.0));
        sideBar.setPrefWidth(sideBar.getWidth());

        var multiplierComponent = new MultiplierComponent(game);
        sideBar.getChildren().add(multiplierComponent);

        var highScoreComponent = new HighScoreComponent(game);
        sideBar.getChildren().add(highScoreComponent);
        setHighScore();

        var levelComponent = new LevelComponent(game);
        sideBar.getChildren().add(levelComponent);

        var scoreComponent = new ScoreComponent(game);
        topItems.setLeft(scoreComponent);

        var livesComponent = new LivesComponent(game);
        topItems.setRight(livesComponent);

        var challengeText = new Text("Challange Game");
        challengeText.getStyleClass().add("title");
        topItems.setCenter(challengeText);

        mainPane.setTop(topItems);

        var incomingText = new Text("Incoming");
        incomingText.getStyleClass().add("heading");
        sideBar.getChildren().add(incomingText);

        //Display the current piece
        var currentPieceBoard = new PieceBoard(3, 3, gameWindow.getWidth()/6, gameWindow.getWidth()/6);
        sideBar.getChildren().add(currentPieceBoard);
        currentPieceBoard.showCircleGuide();
        game.setCurrentPieceListener(currentPieceBoard);
        currentPieceBoard.setOnBlockClick(this::rotatePiece);

        //Display the next piece
        var nextPieceBoard = new PieceBoard(3, 3, gameWindow.getWidth()/9, gameWindow.getWidth()/9);
        sideBar.getChildren().add(nextPieceBoard);
        game.setNextPieceListener(nextPieceBoard);
        nextPieceBoard.setOnBlockClick(this::swapPieces);

        mainPane.setRight(sideBar);

        //Holds the timer and chat bot in multiplayer
        bottomUI = new VBox();
        mainPane.setBottom(bottomUI);
        bottomUI.setAlignment(Pos.CENTER);

        var timerUI = new HBox();
        timerUI.setAlignment(Pos.CENTER_LEFT);
        bottomUI.getChildren().add(timerUI);

        timer = new Rectangle();
        timer.setWidth(gameWindow.getWidth());
        timer.setHeight(25);
        timer.setFill(Color.GREEN);
        timerUI.getChildren().add(timer);


        //Handle block on gameboard grid being clicked
        board.setOnBlockClick(this::blockClicked);
        //Handle right click on the board
        board.setOnMouseClicked(this::handleRightClick);
        //Handle moving the mouse 
        board.setOnMouseMoved(e -> {unpaintKeyboardHover();});
        
        //Handle painting the hover effect
        for (int i = 0; i < board.getCols(); i++) {
            for (int j = 0; j < board.getRows(); j++) {
                GameBlock block = board.getBlock(i, j);
                block.setOnMouseEntered(e -> {block.paintHover();});
                block.setOnMouseExited(e -> {block.unpaintHover();});
            }
        }

        game.setLineClearedListener(this);
        game.setGameLoopListener(this);
        game.getGameOverProperty().addListener(e -> {endGame();});
        game.getScoreProperty().addListener(e -> {setCurrentHighScore();});
    }

    /**
     * Swap the current and next pieces
     * @param block the block that was clicked
     */
    public void swapPieces(GameBlock block) {
        game.swapCurrentPiece();
    }

     /**
      * Handle right click on the game board
      * @param event the mouse event
      */
    public void handleRightClick(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            game.rotateCurrentPiece(1);
        }
    }

    /**
     * Rotate the current piece
     * @param block the piece to be rotated
     */
    public void rotatePiece(GameBlock block) {
        game.rotateCurrentPiece(1);
    }

    /**
     * Handle when a block is clicked
     * @param gameBlock the Game Block that was clocked
     */
    protected void blockClicked(GameBlock gameBlock) {
        game.blockClicked(gameBlock);
    }

    /**
     * Setup the game object and model
     */
    public void setupGame() {
        logger.info("Starting a new challenge");

        //Start new game
        game = new Game(5, 5);
    }

    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        logger.info("Initialising Challenge");
        Multimedia.playBackgroundMusic("game.wav");
        game.start();
    }

    /**
     * Handle key being pressed on the keyboard
     * @param event the key event of the key that was pressed
     */
    @Override
    public void handleKeyPress(KeyEvent event) {
        unpaintKeyboardHover = false;
        logger.info("Key pressed: " + event.getText().toUpperCase());
        if (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.W) {
            if (keyboardYPos != 0) {
                keyboardYPos--;
                logger.info("Moving up");
                //Move up
            }
        } else if (event.getCode() == KeyCode.DOWN || event.getCode() == KeyCode.S) {
            if (keyboardYPos < game.getRows() - 1) {
                keyboardYPos++;
                logger.info("Moving down");
                //Move down
            }
        } else if (event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.A) {
            if (keyboardXPos != 0) {
                keyboardXPos--;
                logger.info("Moving left");
                //Mode left
            }
        } else if (event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.D) {
            if (keyboardXPos < game.getCols() - 1) {
                keyboardXPos++;
                logger.info("Moving right");
                //Move right
            }
        } else if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.X) {
            game.placeBlock(keyboardXPos, keyboardYPos);
            logger.info("Placing block");
            //Place block
        } else if (event.getCode() == KeyCode.SPACE || event.getCode() == KeyCode.R) {
            game.swapCurrentPiece();
            logger.info("Swapping pieces");
            //Swap pieces
        } else if (event.getCode() == KeyCode.Q || event.getCode() == KeyCode.Z || event.getCode() == KeyCode.OPEN_BRACKET) {
            game.rotateCurrentPiece(3);
            logger.info("Rotating piece left");
            //Rotate piece left
        } else if(event.getCode() == KeyCode.E || event.getCode() == KeyCode.C || event.getCode() == KeyCode.CLOSE_BRACKET) {
            game.rotateCurrentPiece(1);
            logger.info("Rotating piece right");
            //Rotate piece right
        } else if (event.getCode() == KeyCode.ESCAPE) {
            game.shutdownGameLoop();
            if (game.isMultiplayer()) {
                gameWindow.getCommunicator().send("DIE");
            }
            logger.info("Aborting game");
            //Abort game
            gameWindow.startMenu();
        } else if (event.getCode() == KeyCode.T && game.isMultiplayer()) {
            ((MultiplayerScene)this).openChatBox();
            //Open chat box if the game is a multiplayer one
        }

        //Handle unpainting the hover effect when using the keyboard
        for (int i = 0; i < board.getCols(); i++) {
            for (int j = 0; j < board.getRows(); j++) {
                board.getBlock(i, j).unpaintHover();
            }
        }
        //Paint the hover effect when moving with the keyboard
        board.getBlock(keyboardXPos, keyboardYPos).paintHover();
    }

    /**
     * Unpaint the hover effect on moving the mouse if the keyboard was previously used
     */
    public void unpaintKeyboardHover() {
        if (!unpaintKeyboardHover) {
            unpaintKeyboardHover = true;
            board.getBlock(keyboardXPos, keyboardYPos).unpaintHover();
        }
    }

    /**
     * Shutdown the game loop and open the score screen
     */
    public void endGame() {
        if (game.getGameOverProperty().get() == true) {
            game.shutdownGameLoop();
            Platform.runLater(() -> gameWindow.openScoreScreen(game));
        }
    }

    /**
     * Play the fade out animation when clearing a {@link Line}
     * @param blocks the blocks to be animated
     */
    @Override
    public void fadeOut(HashSet<GameBlockCoordinate> blocks) {
        board.fadeOut(blocks);
    }

    /**
     * Handle the animation of the timer
     * @param time the time for the animation to run
     */
    @Override
    public void gameLoop(int time) {
       Timeline timeline = new Timeline(
        new KeyFrame(Duration.ZERO, new KeyValue(timer.fillProperty(), Color.GREEN)),
        new KeyFrame(Duration.millis(time), new KeyValue(timer.widthProperty(), 0)),
        new KeyFrame(Duration.millis(time*0.5), new KeyValue(timer.fillProperty(), Color.YELLOW)),
        new KeyFrame(Duration.millis(time*0.8), new KeyValue(timer.fillProperty(), Color.RED)),
        new KeyFrame(Duration.ZERO, new KeyValue(timer.widthProperty(), gameWindow.getWidth()))
       );
       timeline.play();
    }

    /**
     * Set the current high score of the player beats it
     */
    public void setCurrentHighScore() {
        if (game.getScore() > game.getHighScore()) {
            game.getHighScoreProperty().set(game.getScore());
        }
    }

    /**
     * Read the high score from a save file
     */
    public void setHighScore() {
        BufferedReader scoresReader;
        File scoresFile = new File("scores.txt");
        try {
            scoresReader = new BufferedReader(new FileReader(scoresFile));
            String scoreLine = scoresReader.readLine();
            String score = scoreLine.split(":")[1];
            game.getHighScoreProperty().set(new Integer(score));
            scoresReader.close();
        } catch (Exception e) {
            logger.error("Score file is not found");
        }
    }
}
