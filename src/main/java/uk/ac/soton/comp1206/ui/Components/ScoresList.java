package uk.ac.soton.comp1206.ui.Components;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;
import uk.ac.soton.comp1206.component.GameBlock;

public class ScoresList extends VBox {

    private SimpleListProperty<Pair<String, Integer>> scoresList = new SimpleListProperty<Pair<String, Integer>>();

    private String typeOfScore;

    public ScoresList(String typeOfScore) {
        super();
        this.typeOfScore = typeOfScore;
        setAlignment(Pos.CENTER);
        setPadding(new Insets(20, 20, 20, 20));
        build();
        scoresList.addListener((ListChangeListener.Change<? extends Pair<String, Integer>> e) -> {
            Platform.runLater(() -> {updateScores();});
        });
    }

    /**
     * Build the UI
     */
    public void build() {
        var scoreTypeText = new Text(typeOfScore);
        scoreTypeText.getStyleClass().add("title");
        getChildren().add(scoreTypeText);
    }

    /**
     * Update the score list
     */
    public void updateScores() {
        getChildren().clear();
        build();
        for (Pair<String,Integer> pair : scoresList) {
            var playerScore = new Text(pair.getKey() + ": " + pair.getValue());
            playerScore.getStyleClass().add("scorelist");
            getChildren().add(playerScore);
        }
        reveal();
    }

    /**
     * @return the scores list
     */
    public SimpleListProperty<Pair<String, Integer>> getScoresList() {
        return scoresList;
    }

    /**
     * Handles animating the scores
     */
    public void reveal() {
        SequentialTransition seqTransition = new SequentialTransition();
        for (Node node : getChildren()) {
            Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(node.opacityProperty(), 0)),
                new KeyFrame(Duration.millis(300), new KeyValue(node.opacityProperty(), 1))
            );
            ((Text)node).setFill(GameBlock.COLOURS[getChildren().indexOf(node)]);

            seqTransition.getChildren().add(timeline);
        }
        seqTransition.play();
    }
}
