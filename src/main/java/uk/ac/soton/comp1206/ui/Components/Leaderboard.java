package uk.ac.soton.comp1206.ui.Components;

import java.util.ArrayList;
import java.util.Comparator;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.event.CommunicationsListener;
import uk.ac.soton.comp1206.network.Communicator;

public class Leaderboard extends ScoresList {

    private Communicator communicator;

    private ArrayList<String> deadPlayers = new ArrayList<String>();
    
    public Leaderboard() {
        super("Versus");
    }

    /**
     * Update the scores
     */
    @Override
    public void updateScores() {

        Platform.runLater(() -> {
            getChildren().clear();
            build();
            int count = 0;
            for (Pair<String, Integer> user : getScoresList()) {
                count++;
                var userScore = new Text(user.getKey() + ": " + user.getValue());
                userScore.getStyleClass().add("multiplayerScore");
                getChildren().add(userScore);
                if (count == 5) {
                    break;
                }
            }
            deadPlayer();
            reveal();
        });
    }

    /**
     * Add a dead player to the list of dead players
     * @param player the dead player
     */
    public void addDeadPlayer(String player) {
        deadPlayers.add(player);
    }

    /**
     * @return the list of dead players
     */
    public ArrayList<String> getDeadPlayers() {
        return deadPlayers;
    }

    /**
     * Handles when a player dies
     */
    public void deadPlayer() {
        var height = getHeight();
        setPrefHeight(height);
        for (String string : deadPlayers) {
            for (Node player : getChildren()) {
                if (((Text)player).getText().contains(string)) {
                    Platform.runLater(() -> {
                        player.getStyleClass().add("deadscore");});
                }
            }
        }
    }
}
