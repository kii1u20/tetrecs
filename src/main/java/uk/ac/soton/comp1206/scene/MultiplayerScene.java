package uk.ac.soton.comp1206.scene;


import java.lang.reflect.GenericSignatureFormatError;
import java.util.ArrayList;
import java.util.Comparator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.event.CommunicationsListener;
import uk.ac.soton.comp1206.game.Grid;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.Components.HighScoreComponent;
import uk.ac.soton.comp1206.ui.Components.Leaderboard;
import uk.ac.soton.comp1206.ui.Components.LevelComponent;
import uk.ac.soton.comp1206.ui.Components.LivesComponent;
import uk.ac.soton.comp1206.ui.Components.MultiplierComponent;
import uk.ac.soton.comp1206.ui.Components.ScoreComponent;

public class MultiplayerScene extends ChallengeScene implements CommunicationsListener {

    private TextField chatInput;
    private Text message;
    private Leaderboard leaderboard;
    private boolean generated = false;

    private VBox multiplayerBoardsHolder = new VBox();

    private SimpleListProperty<Node> userList;

    private static final Logger logger = LogManager.getLogger(MenuScene.class);


    public MultiplayerScene(GameWindow gameWindow, ObservableList<Node> userList) {
        super(gameWindow);
        this.userList = new SimpleListProperty<Node>(userList);
    }   
    
    /**
     * Build the multiplayer UI
     */
    public void build() {

        logger.info("Building " + this.getClass().getName());

        setupGame();

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("menu-background");
        root.getChildren().add(challengePane);

        var mainPane = new BorderPane();
        mainPane.setMaxWidth(gameWindow.getWidth());
        challengePane.getChildren().add(mainPane);

        board = new GameBoard(game.getGrid(),gameWindow.getWidth()/2.7,gameWindow.getWidth()/2.7);
        
        boardHolder.getChildren().add(board);
        boardHolder.setAlignment(Pos.CENTER);
        boardHolder.setSpacing(20);;
        mainPane.setCenter(boardHolder);

        topItems = new BorderPane();
        topItems.setPadding(new Insets(10.0, 10.0, 10.0, 10.0));

        sideBar = new VBox();
        sideBar.setAlignment(Pos.CENTER);
        sideBar.setSpacing(6.0);
        sideBar.setPadding(new Insets(5.0, 5.0, 5.0, 5.0));
        sideBar.setPrefWidth(sideBar.getWidth());

        var scoreComponent = new ScoreComponent(game);
        topItems.setLeft(scoreComponent);

        var livesComponent = new LivesComponent(game);
        topItems.setRight(livesComponent);

        var challengeText = new Text("Multiplayer match");
        challengeText.getStyleClass().add("title");
        topItems.setCenter(challengeText);

        mainPane.setTop(topItems);

        var incomingText = new Text("Incoming");
        incomingText.getStyleClass().add("heading");
        sideBar.getChildren().add(incomingText);

        var currentPieceBoard = new PieceBoard(3, 3, gameWindow.getWidth()/6, gameWindow.getWidth()/6);
        sideBar.getChildren().add(currentPieceBoard);
        currentPieceBoard.showCircleGuide();
        game.setCurrentPieceListener(currentPieceBoard);
        currentPieceBoard.setOnBlockClick(this::rotatePiece);

        var nextPieceBoard = new PieceBoard(3, 3, gameWindow.getWidth()/9, gameWindow.getWidth()/9);
        sideBar.getChildren().add(nextPieceBoard);
        game.setNextPieceListener(nextPieceBoard);
        nextPieceBoard.setOnBlockClick(this::swapPieces);

        mainPane.setRight(sideBar);

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

        //Add this object as a listener for messages from the server
        gameWindow.getCommunicator().addListener(this);

        //Create a new multiplayer leaderboard
        leaderboard = new Leaderboard();
        leaderboard.getScoresList().bind(((MultiplayerGame)game).getScoreList());
        leaderboard.setPadding(new Insets(5, 5, 5, 5));
        leaderboard.setMaxWidth(sideBar.getWidth());
        leaderboard.setMinWidth(sideBar.getWidth());

        sideBar.getChildren().add(0, leaderboard);
        
        message = new Text("Press T to enter a message");
        bottomUI.getChildren().add(0, message);
        message.getStyleClass().add("heading");

        chatInput = new TextField();
        chatInput.setMaxWidth(gameWindow.getWidth()/2);
        chatInput.setPromptText("Enter a message");
        chatInput.setVisible(false);
        chatInput.setOnAction(e -> {
            gameWindow.getCommunicator().send("MSG " + chatInput.getText());
            chatInput.setVisible(false);
            chatInput.clear();
        });
        bottomUI.getChildren().add(1, chatInput);

        multiplayerBoardsHolder.setSpacing(5);
        boardHolder.getChildren().add(multiplayerBoardsHolder);
        multiplayerBoardsHolder.setPrefWidth(gameWindow.getWidth()/8);
        generateGameBoards();
    }

    /**
     * Opens the chat UI
     */
    public void openChatBox() {
        chatInput.setVisible(true);
    }

    /**
     * Generates the gameboards representing other people's boards
     */
    public void generateGameBoards() {
        SequentialTransition seqTransition = new SequentialTransition();
        int count = 0;
        multiplayerBoardsHolder.getChildren().clear();
        for (Node player : userList) {
            if (!LobbyScene.nickname.equals(((Text)player).getText()) && !leaderboard.getDeadPlayers().contains(((Text)player).getText())) {
                var gameBoard = new GameBoard(new Grid(game.getCols(), game.getRows()), gameWindow.getWidth()/8, gameWindow.getWidth()/8);
                gameBoard.setUsername(((Text)player).getText()); //Set the user for this board
                var nicknameText = new Text(((Text)player).getText());
                nicknameText.getStyleClass().add("heading");
                multiplayerBoardsHolder.getChildren().add(nicknameText);
                multiplayerBoardsHolder.getChildren().add(gameBoard);

                //Animate the player's name
                Timeline timelineName = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(nicknameText.opacityProperty(), 0)),
                new KeyFrame(Duration.millis(300), new KeyValue(nicknameText.opacityProperty(), 1))
                );
                seqTransition.getChildren().add(timelineName);
                //Animate the gameboard
                Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(gameBoard.opacityProperty(), 0)),
                new KeyFrame(Duration.millis(300), new KeyValue(gameBoard.opacityProperty(), 1))
                );
                seqTransition.getChildren().add(timeline);
                count++; 
            }
            if (count == 3) {
                break;
            }
        }
        seqTransition.play();
    }

    /**
     * Handle recieving communication from the server
     */
    @Override
    public void receiveCommunication(String communication) {
        if (communication.startsWith("MSG")) {
            var temp = communication.replace("MSG ", "");
            var userMessagesComponent = temp.split("\n");
            for (String string : userMessagesComponent) {
                var nickname = string.split(":")[0];
                var msg = string.split(":")[1];
                var messageComponent = nickname + ": " + msg;
                Platform.runLater(() -> {
                    message.setText(messageComponent);
                });
            }
        } else if (communication.startsWith("DIE")) {
            var temp = communication.replace("DIE ", "");
            leaderboard.addDeadPlayer(temp);
            Platform.runLater(() -> {generateGameBoards();});
        } else if (communication.startsWith("BOARD")) {
            var temp = communication.replace("BOARD ", "");
            var username = temp.split(":")[0];
            var blockString = temp.split(":")[1];
            var blocks = blockString.split(" ");

            int countX = 0;
            int countY = 0;
            for (Node child : multiplayerBoardsHolder.getChildren()) {
                if (child instanceof GameBoard) {
                    if (((GameBoard)child).getUsername().equals(username)) {
                        var grid = ((GameBoard)child).getGridProperty();
                        for (String string : blocks) {
                            grid.set(countX, countY, new Integer(string));
                            countY++;
                            if (countY == 5) {
                                countY = 0;
                                countX++;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Setup a new multiplayer game object
     */
    public void setupGame() {
        game = new MultiplayerGame(5, 5, gameWindow.getCommunicator());
    }
}
