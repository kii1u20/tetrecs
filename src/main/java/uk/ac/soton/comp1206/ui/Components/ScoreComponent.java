package uk.ac.soton.comp1206.ui.Components;

import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import uk.ac.soton.comp1206.game.Game;

/**
 * Component representing the current score
 */
public class ScoreComponent extends VBox {
    Text title = new Text("Score");
    Text score = new Text();
    Game game;

    public ScoreComponent (Game game) {
        this.setAlignment(Pos.CENTER);
        this.game = game;
        score.textProperty().bind(game.getScoreProperty().asString());
        title.getStyleClass().add("title");
        score.getStyleClass().add("score");
        this.getChildren().add(title);
        this.getChildren().add(score);
    }
}
