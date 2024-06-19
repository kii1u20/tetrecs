package uk.ac.soton.comp1206.game;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Observable;

import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ListChangeListener.Change;
import javafx.util.Pair;
import uk.ac.soton.comp1206.event.CommunicationsListener;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.scene.MultiplayerScene;

public class MultiplayerGame extends Game implements CommunicationsListener {

    private Communicator communicator;

    private int currentPiece;

    private ArrayList<Integer> gamePieces = new ArrayList<Integer>();

    private ArrayList<Pair<String, Integer>> pairs = new ArrayList<>();
    private ObservableList<Pair<String, Integer>> scores;
    private SimpleListProperty<Pair<String, Integer>> scoreListWrapper;

    /**
     * Create a new game with specified number of rows and columns
     * @param cols the number of colomns
     * @param rows the number of rows
     * @param communicator the communicator object
     */
    public MultiplayerGame(int cols, int rows, Communicator communicator) {
        super(cols, rows);
        this.communicator = communicator;
        communicator.addListener(this);
        getScoreProperty().addListener((observable, oldvalue, newvalue) -> {sendScoreUpdate();});
        getLivesProperty().addListener((observable, oldvalue, newvalue) -> {sendLivesUpdate();});
        fillPieces(6);
        
        scores = FXCollections.observableArrayList(pairs);
        scoreListWrapper = new SimpleListProperty<Pair<String, Integer>>(scores);
    }
  
    /**
     * Update the server on the current lives of the player
     */
    public void sendLivesUpdate() {
        sendCommunication("LIVES " + getLives());
    }

    /**
     * Update the server on the current score of the player
     */
    public void sendScoreUpdate() {
        sendCommunication("SCORE " + getScore());
    }

    /**
     * Update the server on the current board of the player
     */
    public void sendBoardChanged() {
        String valueUpdate = "BOARD";
        for (int i = 0; i < grid.getCols(); i++) {
            for (int j = 0; j < grid.getRows(); j++) {
                valueUpdate += (" " + String.valueOf(grid.get(i, j)));
            }
        }
        sendCommunication(valueUpdate);
    }

    /**
     * Request pieces from the server
     * @param pieces the number of pieces to be requested
     */
    public void fillPieces(int pieces) {
        for (int i = 0; i < pieces; i++) {
            sendCommunication("PIECE");
        }
    }

    /**
     * Send message to the server
     * @param communication the message to be send
     */
    public void sendCommunication(String communication) {
        communicator.send(communication);
    }

    /**
     * Deal with when a message is recieved
     * @param communication the message that was recieved
     */
    @Override
    public void receiveCommunication(String communication) {
        if (communication.startsWith("PIECE")) {
            var temp = communication.replace("PIECE ", "");
            gamePieces.add(new Integer(temp));
        } else if (communication.startsWith("SCORES")) {
            Platform.runLater(() -> {scoreListWrapper.clear();});
            var temp = communication.replace("SCORES ", "");
            var scores = temp.split("\n");
            for (String string : scores) {
                var userScore = string.split(":");
                var pair = new Pair<String, Integer>(userScore[0], new Integer(userScore[1]));
                Platform.runLater(() -> {
                    scoreListWrapper.add(pair);
                });
            }
            Platform.runLater(() -> {scoreListWrapper.sort(Comparator.comparing(Pair<String, Integer>::getValue).reversed());});
        }
    }

    /**
     * @return the current scores list
     */
    public SimpleListProperty<Pair<String, Integer>> getScoreList() {
        return scoreListWrapper;
    }
    
    /**
     * Update and display the current piece on the current piece board
     */
    public GamePiece spawnPiece() {
        if (gamePieces.size() >= 2) {
            GamePiece piece = GamePiece.createPiece(gamePieces.get(0));
            currentPieceListener.updatePiece(piece);
            gamePieces.remove(0);
            return piece;
        } else {
            fillPieces(4);
            synchronized(this) {
                try {
                    this.wait();
                    return spawnPiece();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * Update and display the next piece on the next piece board
     */
    public void nextPiece() {
        if (gamePieces.size() >= 2) {
            nextPiece = GamePiece.createPiece(gamePieces.get(0));
            nextPieceListener.updatePiece(nextPiece);
            gamePieces.remove(0);
        } else {
            fillPieces(4);
            synchronized(this) {
                try {
                    this.wait();
                    nextPiece();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
