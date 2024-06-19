package uk.ac.soton.comp1206.ui.Components;

import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import uk.ac.soton.comp1206.game.Game;

/**
 * Component representing the current level
 */
public class LevelComponent extends VBox {
    Text title = new Text("Level");
    Text level = new Text();
    Game game;

    public LevelComponent (Game game) {
        this.setAlignment(Pos.CENTER);
        this.game = game;
        level.textProperty().bind(game.getLevelProperty().asString());
        title.getStyleClass().add("heading");
        level.getStyleClass().add("level");
        this.getChildren().add(title);
        this.getChildren().add(level);
    }
}
