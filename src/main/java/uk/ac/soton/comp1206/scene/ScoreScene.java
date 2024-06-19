package uk.ac.soton.comp1206.scene;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Pair;
import uk.ac.soton.comp1206.component.Multimedia;
import uk.ac.soton.comp1206.event.CommunicationsListener;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.Components.ScoresList;

public class ScoreScene extends BaseScene implements CommunicationsListener {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);
    
    private Game game;

    private ArrayList<Pair<String, Integer>> pairs = new ArrayList<>();
    private ObservableList<Pair<String, Integer>> localScores;
    private SimpleListProperty<Pair<String, Integer>> scoreListWrapper;

    private ArrayList<Pair<String, Integer>> remotePairs = new ArrayList<>();
    private ObservableList<Pair<String, Integer>> remoteScores;
    private SimpleListProperty<Pair<String, Integer>> remoteScoreListWrapper;

    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> timer;

    private BorderPane mainPane;

    private boolean newScore = false;

    private Communicator communicator;

    private HBox centerComponent;

    private boolean multiplayer = false;

    private String name;


    public ScoreScene(GameWindow gameWindow, Game game, boolean multiplayer) {
        super(gameWindow);
        this.game = game;
        communicator = gameWindow.getCommunicator();
        Multimedia.playBackgroundMusic("end.wav");
        this.multiplayer = multiplayer;
    }

    /**
     * Handle a key being pressed
     */
    @Override
    public void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
            gameWindow.startMenu();
        }
    }

    /**
     * Initialize the scene
     */
    @Override
    public void initialise() {
        logger.info("Opening score screen");
    }

    /**
     * Build the UI
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        communicator.addListener(this);

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        mainPane = new BorderPane();
        mainPane.setMaxWidth(gameWindow.getWidth());
        mainPane.setMaxHeight(gameWindow.getHeight());
        mainPane.getStyleClass().add("menu-background");
        root.getChildren().add(mainPane);

        var scoreBoardText = new Text("Game Over");
        scoreBoardText.getStyleClass().add("bigtitle");
        mainPane.setTop(scoreBoardText);
        BorderPane.setAlignment(scoreBoardText, Pos.CENTER);


        localScores = FXCollections.observableArrayList(pairs);
        scoreListWrapper = new SimpleListProperty<Pair<String, Integer>>(localScores);
        loadScores();

        remoteScores = FXCollections.observableArrayList(remotePairs);
        remoteScoreListWrapper = new SimpleListProperty<Pair<String, Integer>>(remoteScores);
        loadOnlineScores();

        if (scoreListWrapper.isEmpty()) {
            newScore = true;
        } else {
            for (Pair<String,Integer> pair : scoreListWrapper) {
                if (pair.getValue() < game.getScore()) {
                    newScore = true;
                }
            }
        }

        centerComponent = new HBox();
        centerComponent.setAlignment(Pos.CENTER);
        mainPane.setCenter(centerComponent);

        //Ask the user for a name if there is a new high score
        if (newScore) {
            centerComponent.getChildren().clear();
            var newHighScoreContainer = new VBox();
            newHighScoreContainer.setAlignment(Pos.CENTER);

            var nameInput = new TextField();
            nameInput.setPromptText("Enter your name");
            newHighScoreContainer.getChildren().add(nameInput);

            var highScoreText = new Text("You got a new high score!");
            highScoreText.getStyleClass().add("title");
            newHighScoreContainer.getChildren().add(highScoreText);

            var submitButton = new Button("Submit");
            newHighScoreContainer.getChildren().add(submitButton);
            submitButton.setOnAction(e -> {
                name = nameInput.getText();
                scoreListWrapper.remove(scoreListWrapper.size() - 1);
                var newScorePair = new Pair<String, Integer>(name, new Integer(game.getScore()));
                scoreListWrapper.add(newScorePair);
                writeScores();
                timer = scheduler.scheduleWithFixedDelay(this::checkOnlineScoresArrives, 0, 100, TimeUnit.MILLISECONDS);
            });
            centerComponent.getChildren().add(newHighScoreContainer);
        } else {
            timer = scheduler.scheduleWithFixedDelay(this::checkOnlineScoresArrives, 0, 100, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Builds the score list UI
     */
    private void generateScoreList() {
        centerComponent.getChildren().clear();

        if (game.isMultiplayer()) {
            var scoreListComponent = new ScoresList("This Game");
            scoreListWrapper.bind(((MultiplayerGame)game).getScoreList());
            scoreListComponent.getScoresList().bind(scoreListWrapper);
            centerComponent.getChildren().add(scoreListComponent);
        } else {
            var scoreListComponent = new ScoresList("Local Scores");
            scoreListComponent.getScoresList().bind(scoreListWrapper);
            centerComponent.getChildren().add(scoreListComponent);
        }


        var remoteListComponent = new ScoresList("Remote Scores");
        remoteListComponent.getScoresList().bind(remoteScoreListWrapper);
        centerComponent.getChildren().add(remoteListComponent);
    }

    /**
     * Check if there was a response from the server
     */
    private void checkOnlineScoresArrives() {
        if (remoteScoreListWrapper.size() == 10) {
            writeOnlineScores();
            Platform.runLater(() -> {generateScoreList();});
            timer.cancel(false);
        }
    }

    /**
     * Ask the server for the high scores
     */
    public void loadOnlineScores() {
        communicator.send("HISCORES");
    }

    /**
     * If there is a new high score, submit it to the server
     */
    public void writeOnlineScores() {
        for (Pair<String,Integer> pair : remoteScoreListWrapper) {
            if (game.getScore() > pair.getValue()) {
                communicator.send("HISCORE " + name + ":" + game.getScore());
                remoteScoreListWrapper.remove(remoteScoreListWrapper.size() - 1);
                remoteScoreListWrapper.add(new Pair<String, Integer>(name, new Integer(game.getScore())));
                remoteScoreListWrapper.sort(Comparator.comparing(Pair<String, Integer>::getValue).reversed());
                break;
            }
        }
    }

    /**
     * Handle recieving message from the server
     */
    @Override
    public void receiveCommunication(String communication) {
        logger.info(communication);
        if (communication.contains("HISCORES")) {    
            remoteScoreListWrapper.clear();
            String[] scores = communication.split("\n");
            for (String string : scores) {
                String temp = new String(string);
                if (temp.contains("HISCORES")) {
                    var split = temp.split(" ");
                    temp = split[1];
                }
                String[] scoreArray = temp.split(":");
                Pair<String, Integer> score = new Pair<String, Integer>(scoreArray[0], new Integer(scoreArray[1]));
                remoteScoreListWrapper.add(score);
            }
        }
    }

    /**
     * Set default scores if there is no file found
     */
    private void setDefaultScores() {
        for (int i = 1; i <= 10; i++) {
            Pair<String, Integer> defaultScore = new Pair<String, Integer>("Oli", new Integer(i*1000));
            scoreListWrapper.add(defaultScore);
        }
        writeScores();
    }

    /**
     * Load high scores from a save file.
     * If there is no save file, generate default scores
     */
    public void loadScores() {
        BufferedReader scoresReader;
        File scoresFile = new File("scores.txt");
        try {
            scoresReader = new BufferedReader(new FileReader(scoresFile));
            String scoreLine = scoresReader.readLine();
            while (scoreLine != null) {
                String name = scoreLine.split(":")[0];
                String score = scoreLine.split(":")[1];
                Pair<String, Integer> scorePair = new Pair<String, Integer>(name, new Integer(score));
                scoreListWrapper.add(scorePair);
                scoreLine = scoresReader.readLine();
            }
            scoresReader.close();
        } catch (Exception e) {
            logger.error("Score file is not found");
            setDefaultScores();
        }
    }

    /**
     * Save the high scores to a save file
     */
    public void writeScores() {
        BufferedWriter scoresWriter;
        File scoresFile = new File("scores.txt");
        try {
            scoresWriter = new BufferedWriter(new FileWriter(scoresFile));
            scoreListWrapper.sort(Comparator.comparing(Pair<String, Integer>::getValue).reversed());
            for (Pair<String,Integer> pair : scoreListWrapper) {
                scoresWriter.write(pair.getKey() + ":" + pair.getValue() + "\n");
            }
            scoresWriter.close();
        } catch (Exception e) {
            logger.error("Score file is not found");
        }
    }

    /**
     * @return the score list property
     */
    public SimpleListProperty<Pair<String, Integer>> getScoresListProperty() {
        return scoreListWrapper;
    }
}
