package uk.ac.soton.comp1206.ui.Components;

import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import uk.ac.soton.comp1206.game.Game;

/**
 * Component representing the current high score
 */
public class HighScoreComponent extends VBox {
    Text title = new Text("High Score");
    Text highScore = new Text();
    Game game;

    public HighScoreComponent (Game game) {
        this.setAlignment(Pos.CENTER);
        this.game = game;
        highScore.textProperty().bind(game.getHighScoreProperty().asString());
        title.getStyleClass().add("heading");
        highScore.getStyleClass().add("hiscore");
        this.getChildren().add(title);
        this.getChildren().add(highScore);
    }
}
